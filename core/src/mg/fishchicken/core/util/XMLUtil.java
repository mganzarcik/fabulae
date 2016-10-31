package mg.fishchicken.core.util;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Locale;

import mg.fishchicken.audio.AudioContainer;
import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Music;
import mg.fishchicken.audio.Sound;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.projectiles.ProjectileType.ScalingMethod;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.ActionsContainer;
import mg.fishchicken.gamelogic.characters.AIScript;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.Gender;
import mg.fishchicken.gamelogic.characters.Race;
import mg.fishchicken.gamelogic.characters.Role;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.characters.perks.PerksContainer;
import mg.fishchicken.gamelogic.dialogue.Chatter;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.Effect.EffectParameterDefinition;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.effects.EffectParameter;
import mg.fishchicken.gamelogic.effects.MissingParameterException;
import mg.fishchicken.gamelogic.effects.targets.TargetTypeContainer;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.items.Armor.ArmorClass;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.gamelogic.magic.SpellsContainer;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;
import mg.fishchicken.gamelogic.traps.TrapType;
import mg.fishchicken.gamelogic.weather.WeatherManager.PrecipitationAmount;
import mg.fishchicken.gamelogic.weather.WeatherProfile;
import mg.fishchicken.gamestate.SaveablePolygon;
import mg.fishchicken.graphics.ParticleEffectManager;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.graphics.models.CharacterModel;

import org.codehaus.groovy.runtime.InvokerHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class XMLUtil {
	public static final String XML_ACTIONS = "actions";
	public static final String XML_BACKUP_ACTIONS = "backupActions";
	public static final String XML_VARIABLES = "variables";
	public static final String XML_PROPERTIES = "properties";
	public static final String XML_MODIFIERS = "modifiers";
	public static final String XML_VARIABLE = "variable";
	public static final String XML_MUSIC = "music";
	public static final String XML_SOUND = "sound";
	public static final String XML_EFFECTS = "effects";
	public static final String XML_TARGET = "target";
	public static final String XML_PROJECTILE = "projectile";
	public static final String XML_FILENAME = "filename";
	public static final String XML_IMPORT = "import";
	public static String XML_SOUNDS = "sounds";

	public static final String XML_ATTRIBUTE_CHANCE_TO_PLAY = "chanceToPlay";
	public static final String XML_ATTRIBUTE_VOLUME_MOD = "volumeModifier";
	public static final String XML_ATTRIBUTE_ID = "id";
	public static final String XML_ATTRIBUTE_TYPE = "type";
	public static final String XML_ATTRIBUTE_X = "x";
	public static final String XML_ATTRIBUTE_Y = "y";

	public static final String XML_ATTRIBUTE_NAME = "name";
	public static final String XML_ATTRIBUTE_VALUE = "value";
	public static final String XML_ATTRIBUTE_FILENAME = "filename";
	public static final String XML_DESCRIPTION = "description";
	public static final String XML_ACTION = "action";
	public static final String XML_CONDITION = "condition";
	public static final String XML_TRIGGERS = "triggers";
	public static final String XML_PARAMETERS = "parameters";
	public static final String XML_TYPE = "type";
	public static final String XML_INTARRAY = "IntArray";
	public static final String XML_BOOLEAN = "boolean";
	public static final String XML_STRING = "String";
	public static final String XML_INTEGER = "int";
	public static final String XML_FLOAT = "float";
	public static final String XML_STRINGARRAY = "StringArray";
	public static final String XML_FLOATARRAY = "FloatArray";

	private XMLUtil() {
	}

	public static void handleImports(XMLLoadable importable, FileHandle parentFile, Element root) throws IOException {
		Array<Element> imports = root.getChildrenByName(XMLUtil.XML_IMPORT);
		for (Element singleImport : imports) {
			String filename = singleImport.get(XMLUtil.XML_FILENAME);
			FileHandle file = parentFile.parent().child(filename);
			if (!file.exists()) {
				throw new GdxRuntimeException("Import " + file.path() + " from import for " + importable
						+ " does not exist.");
			}
			importable.loadFromXMLNoInit(file);
		}
	}

	public static Script readScript(String scriptId, Element scriptElement) {
		return readScript(scriptId, scriptElement, null);
	}
	
	public static Script readScript(String scriptId, Element scriptElement, Script defaultScript) {
		if (scriptElement == null) {
			return defaultScript;
		}
		
		String id = scriptElement.getAttribute(XML_ATTRIBUTE_ID, null);
		if (id != null) {
			URLClassLoader cl = null;
			try {
				FileHandle scriptsFolder = Gdx.files.internal(Configuration.getFolderCompiledScripts());
				if (!scriptsFolder.isDirectory()) {
					scriptsFolder = Gdx.files.internal(Assets.BIN_FOLDER+Configuration.getFolderCompiledScripts());
				}
				
				if (scriptsFolder.exists()) {
					File dirFile = scriptsFolder.file();
					File scriptFile = new File(dirFile, id+".class");
					if (scriptFile.canRead()) {
					    URL url = dirFile.toURI().toURL();
					    URL[] urls = new URL[]{url};
					    cl = new URLClassLoader(urls);
					    Class<?> scriptClass = cl.loadClass(id);
						return InvokerHelper.createScript(scriptClass, new Binding());
					}
				}
			} catch (ClassNotFoundException | RuntimeException | MalformedURLException e) {
				// do nothing and just build the class from the text
			} finally {
				if (cl != null) {
					StreamUtils.closeQuietly(cl);
				}
			}
		}
		return GroovyUtil.createScript(scriptId, scriptElement.getText());
	}
	
	/**
	 * Reads the target type from the supplied element.
	 * 
	 * @param ttc
	 * @param targetElement
	 */
	public static void readTargetType(TargetTypeContainer ttc, Element targetElement) {
		if (targetElement == null) {
			return;
		}
		targetElement = targetElement.getChild(0);
		if (targetElement != null) {
			ttc.setTargetType(StringUtil.capitalizeFirstLetter(targetElement.getName()),
					readScript("targetType_" + ttc.getId(), targetElement));
		}
	}

	/**
	 * Read the Effects information from the suppled XML element and loads them
	 * into the supplied EffectContainer.
	 * 
	 * The XML element should contain children in the following format:
	 * 
	 * <pre>
	 * &lt;effectId effectParameter1 = "value1" effectParameter2 = "value2" ... /&gt;
	 * </pre>
	 * 
	 * @param ec
	 * @param effectsElement
	 */
	public static void readEffect(EffectContainer ec, Element effectsElement) {
		if (effectsElement == null) {
			return;
		}
		for (int i = 0; i < effectsElement.getChildCount(); ++i) {
			Element effectElement = effectsElement.getChild(i);
			String effectId = effectElement.getName();
			Effect effect = Effect.getEffect(effectId);
			if (effect == null) {
				throw new GdxRuntimeException("Effect " + effectId + " does not exist, but is required for "
						+ ec.getId());
			}
			ec.addEffect(effect, readEffectParameters(ec, effectElement, effect));
		}
	}

	private static Array<EffectParameter> readEffectParameters(EffectContainer ec, Element effectElement, Effect effect) {
		Array<EffectParameter> parameters = new Array<EffectParameter>();
		for (EffectParameterDefinition parameter : effect.getParameterDefinitions()) {
			EffectParameter param;
			try {
				param = new EffectParameter(parameter, effectElement);
			} catch (MissingParameterException e) {
				throw new GdxRuntimeException("Parameter " + parameter.getName() + " is mandatory for Effect "
						+ effect.getId() + " but it was not specified for item " + ec.getId(), e);
			}
			if (!param.isNull()) {
				parameters.add(param);
			}
		}
		return parameters;
	}

	/**
	 * Reads Sounds and returns an array with them. All sounds defined in the
	 * soundElementName, that is a child of the root element, will be loaded.
	 * 
	 * @param soundArray
	 * @param rootElement
	 * @param soundElementName
	 */
	public static Array<Sound> readSounds(Element rootElement, String soundElementName) {
		Array<Sound> soundArray = new Array<Sound>();
		Element soundElement = rootElement.getChildByName(soundElementName);
		if (soundElement != null && soundElement.getChildCount() > 0) {
			for (int i = 0; i < soundElement.getChildCount(); ++i) {
				Sound sound = new Sound();
				sound.loadFromXML(soundElement.getChild(i));
				soundArray.add(sound);
			}
		}
		return soundArray;
	}

	/**
	 * Read the audio information from the suppled XML element and loads them
	 * into the supplied AudioOriginator.
	 * 
	 * The XML element should contain children in the following format:
	 * 
	 * <pre>
	 * &lt;music&gt;
	 * 		&lt;musicType chanceToPlay? volumeModifier?&gt;
	 * 			&lt;track filename="pathToFile" /&gt;
	 * 		&lt;musicType/&gt;
	 * &lt;music /&gt;
	 * 
	 * &lt;sound&gt;
	 * 		&lt;soundType chanceToPlay? volumeModifier?&gt;
	 * 			&lt;track filename="pathToFile" /&gt;
	 * 		&lt;soundType/&gt;
	 * &lt;sound /&gt;
	 * </pre>
	 * 
	 * @param vc
	 * @param variablesElement
	 */
	public static void readAudio(AudioContainer ac, Element containerRoot) {
		if (containerRoot != null) {
			Element musicElement = containerRoot.getChildByName(XML_MUSIC);
			readTracks(ac, musicElement, Music.class);

			Element soundElement = containerRoot.getChildByName(XML_SOUND);
			readTracks(ac, soundElement, Sound.class);
		}
	}
	
	public static void readTracks(AudioContainer ac, Element tracksRoot) {
		readTracks(ac, tracksRoot, null);
	}

	protected static void readTracks(AudioContainer ac, Element tracksRoot, Class<? extends AudioTrack<?>> trackType) {
		if (tracksRoot != null) {
			try {
				for (int i = 0; i < tracksRoot.getChildCount(); ++i) {
					Element typeElement = tracksRoot.getChild(i);
					readTracks(typeElement, trackType, ac, typeElement.getName().toLowerCase(Locale.ENGLISH));
				}
			} catch (Exception e) {
				throw new GdxRuntimeException(e);
			}
		}
	}

	public static <T extends AudioTrack<?>> Array<AudioTrack<?>> readTracks(Element typeElement, Class<T> trackType) {
		try {
			return readTracks(typeElement, trackType, null, null);
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends AudioTrack<?>> Array<AudioTrack<?>> readTracks(Element typeElement, Class<T> trackType,
			AudioContainer ao, String type) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ReflectionException {
		Array<AudioTrack<?>> returnValue = new Array<AudioTrack<?>>();
		if (typeElement == null) {
			return returnValue;
		}
		String chanceToPlayString = typeElement.getAttribute(XML_ATTRIBUTE_CHANCE_TO_PLAY, null);
		Integer chanceToPlay = chanceToPlayString != null ? Integer.valueOf(chanceToPlayString) : null;

		String volumeModString = typeElement.getAttribute(XML_ATTRIBUTE_VOLUME_MOD, null);
		Float volumeMod = volumeModString != null ? Float.valueOf(volumeModString) : null;
		for (int i = 0; i < typeElement.getChildCount(); ++i) {
			Element trackElement = typeElement.getChild(i);
			Class<T> clazz = trackType; 
			if (clazz == null) {
				String className = StringUtil.capitalizeFirstLetter(trackElement.getName());
				clazz = ClassReflection.forName(AudioTrack.class.getPackage().getName()+"."+className);
			}
			T newTrack = clazz.getConstructor().newInstance();
			
			if (chanceToPlay != null) {
				newTrack.setChanceToPlay(chanceToPlay);
			}
			if (volumeMod != null) {
				newTrack.setVolumeModifier(volumeMod);
			}
			newTrack.loadFromXML(trackElement);
			if (ao != null) {
				ao.addTrack(newTrack, type);
			}
			returnValue.add(newTrack);
		}
		return returnValue;
	}

	/**
	 * Read the actions from the suppled XML element and loads them into the
	 * supplied ActionsContainer.
	 * 
	 * The XML element should contain children in the following format:
	 * 
	 * <pre>
	 * &lt;actionClassName parameter1Name="parameter1Value" parameter2Name="parameter2Value" ... /&gt;
	 * </pre>
	 * 
	 * @param ac
	 * @param actionsElement
	 */
	@SuppressWarnings({ "unchecked" })
	public static void readActions(ActionsContainer ac, Element actionsElement) {
		if (actionsElement != null) {
			for (int i = 0; i < actionsElement.getChildCount(); ++i) {
				Element actionElement = actionsElement.getChild(i);
				String implementationClassName = actionElement.getName();
				implementationClassName = Action.class.getPackage().getName() + "."
						+ StringUtil.capitalizeFirstLetter(implementationClassName);
				try {
					Class<? extends Action> actionClass = (Class<? extends Action>) ClassReflection
							.forName(implementationClassName);
					Action newAction = ac.addAction(actionClass);
					if (newAction != null) {
						newAction.loadFromXML(actionElement);
					}

				} catch (ReflectionException e) {
					throw new GdxRuntimeException(e);
				}
			}
		}
	}

	/**
	 * Read the modifiers from the suppled XML element and loads them into the
	 * supplied ModifierContainer.
	 * 
	 * The XML element should contain children in the following format:
	 * 
	 * <pre>
	 * &lt;Modifier modifiableAttributeName1="value" .. /&gt;
	 * </pre>
	 * 
	 * @param ModifierContainer
	 * @param modifiersElement
	 */
	public static void readModifiers(ModifierContainer sc, Element modifiersElement) {
		if (modifiersElement != null) {
			for (int i = 0; i < modifiersElement.getChildCount(); ++i) {
				Element modifierElement = modifiersElement.getChild(i);
				Modifier modifier = new Modifier();
				modifier.setName(sc.getName());
				modifier.loadFromXML(modifierElement);
				sc.addModifier(modifier);
			}
		}
	}

	/**
	 * Writes the modifiers from ModifierContainer using the supplied XmlWriter.
	 * 
	 * The output XML snippet will look like this:
	 * 
	 * <pre>
	 * &lt;Modifiers&gt;
	 * 	&lt;Modifier modifiableAttributeName1="value" .. /&gt;
	 * 	...
	 * &lt;/Modifiers&gt;
	 * </pre>
	 * 
	 * @param ModifierContainer
	 * @param modifiersElement
	 */
	public static void writeModifiers(ModifierContainer sc, XmlWriter writer) throws IOException {
		writer.element(XML_MODIFIERS);
		Iterator<Modifier> modifiers = sc.getModifiers();
		while (modifiers.hasNext()) {
			Modifier mod = modifiers.next();
			mod.writeToXML(writer);
		}
		writer.pop();
	}

	/**
	 * Reads the perks from the suppled XML element and loads them into the
	 * supplied PerksContainer.
	 * 
	 * The XML element should contain children in the following format:
	 * 
	 * <pre>
	 * &lt;perkId /&gt;
	 * </pre>
	 * 
	 * @param pc
	 * @param perksElement
	 */
	public static void readPerks(PerksContainer pc, Element perksElement) {
		if (perksElement != null) {
			for (int i = 0; i < perksElement.getChildCount(); ++i) {
				Element perkElement = perksElement.getChild(i);
				pc.addPerk(Perk.getPerk(perkElement.get(XML_ATTRIBUTE_ID)));
			}
		}
	}

	/**
	 * Reads the spells from the suppled XML element and loads them into the
	 * supplied SpellsContainer.
	 * 
	 * The XML element should contain children in the following format:
	 * 
	 * <pre>
	 * &lt;spellId /&gt;
	 * </pre>
	 * 
	 * @param pc
	 * @param spellsElement
	 */
	public static void readSpells(SpellsContainer pc, Element spellsElement) {
		if (spellsElement != null) {
			for (int i = 0; i < spellsElement.getChildCount(); ++i) {
				Element spellElement = spellsElement.getChild(i);
				pc.addSpell(Spell.getSpell(spellElement.get(XML_ATTRIBUTE_ID)));
			}
		}
	}

	/**
	 * Writes the perks from the suppled PerksContainer using the supplied
	 * XmlWriter.
	 * 
	 * The resulting XML snippet will look like this:
	 * 
	 * <pre>
	 * &lt;Perks&gt;
	 * 	&lt;perkId1 /&gt;
	 * 	...
	 * &lt;/Perks&gt;
	 * </pre>
	 * 
	 * @param PerksContainer
	 * @param XmlWriter
	 */
	public static void writePerks(PerksContainer sc, XmlWriter writer) throws IOException {
		writer.element(PerksContainer.XML_PERKS);
		for (Perk perk : sc.getPerks()) {
			writer.element(PerksContainer.XML_PERK).attribute(XML_ATTRIBUTE_ID, perk.getId()).pop();
		}
		writer.pop();
	}

	/**
	 * Writes the spells from the suppled SpellsContainer using the supplied
	 * XmlWriter.
	 * 
	 * The resulting XML snippet will look like this:
	 * 
	 * <pre>
	 * &lt;Spells&gt;
	 * 	&lt;spellId1 /&gt;
	 * 	...
	 * &lt;/Spells&gt;
	 * </pre>
	 * 
	 * @param SpellsContainer
	 * @param XmlWriter
	 */
	public static void writeSpells(SpellsContainer sc, XmlWriter writer) throws IOException {
		writer.element(SpellsContainer.XML_SPELLS);
		for (Spell spell : sc.getSpells()) {
			writer.element(SpellsContainer.XML_SPELL).attribute(XML_ATTRIBUTE_ID, spell.getId()).pop();
		}
		writer.pop();
	}

	/**
	 * This will read and set all class members from the supplied XML element
	 * that either have their name prefixed with "s_" or that are annotated with
	 * {@link XMLField}. If they are annotated, the annotation should either
	 * define the path to the field value in the XML element (can use dotted
	 * notation to specify child elements), or it will be assumed the path is
	 * the same as the field's name. If they are prefixed with "s_", the path
	 * will be the name of the field without the prefix.
	 * 
	 * If they are not contained in the XML element, they are skipped.
	 * 
	 * For example, if the Element looks like this:
	 * 
	 * <pre>
	 * &lt;SomeProperties propertyA = "1" propertyB = "2" /&gt;
	 * </pre>
	 * 
	 * The supplied object will have s_propertyA set to 1 and s_propertyB set to
	 * 2.
	 * 
	 * @param object
	 *            - the object on which to set the members
	 * @param elementToRead
	 *            - the XML element from which to read the properties
	 */
	public static void readPrimitiveMembers(Object object, Element elementToRead) {
		if (elementToRead == null) {
			return;
		}
		try {
			Class<?> objectClass = object.getClass();
			while (objectClass != null && objectClass != Object.class) {
				Field[] fields = objectClass.getDeclaredFields();

				for (Field field : fields) {
					XMLField annotation = field.getAnnotation(XMLField.class);
					if (annotation == null && !field.getName().startsWith("s_")) {
						continue;
					}

					String fieldPath = null;
					if (annotation != null) {
						fieldPath = annotation.fieldPath();
						if (fieldPath.isEmpty()) {
							fieldPath = field.getName();
						}
					} else {
						fieldPath = field.getName().replaceFirst("s_", "");
					}

					String xmlValue = getDottedValue(elementToRead, fieldPath);

					if (xmlValue != null) {
						Class<?> fieldType = field.getType();
						field.setAccessible(true);
						if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
							field.set(object, Integer.valueOf(xmlValue));
						} else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
							field.set(object, Float.valueOf(xmlValue));
						} else if (String.class.equals(fieldType)) {
							field.set(object, xmlValue);
						} else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
							field.set(object, Boolean.valueOf(xmlValue));
						} else if (Orientation.class.equals(fieldType)) {
							field.set(object, Orientation.valueOf(xmlValue.toUpperCase(Locale.ENGLISH)));
						} else if (Color.class.equals(fieldType)) {
							field.set(object, Color.valueOf(xmlValue));
						} else if (Skill.class.equals(fieldType)) {
							field.set(object, Skill.valueOf(xmlValue.toUpperCase(Locale.ENGLISH)));
						} else if (Gender.class.equals(fieldType)) {
							field.set(object, Gender.valueOf(xmlValue));
						} else if (Race.class.equals(fieldType)) {
							field.set(object, Race.getRace(xmlValue));
						} else if (Chatter.class.equals(fieldType)) {
							field.set(object, Chatter.getChatter(xmlValue));
						} else if (ArmorClass.class.equals(fieldType)) {
							field.set(object, ArmorClass.valueOf(xmlValue.toUpperCase(Locale.ENGLISH)));
						} else if (PrecipitationAmount.class.equals(fieldType)) {
							field.set(object, PrecipitationAmount.valueOf(xmlValue.toUpperCase(Locale.ENGLISH)));
						} else if (ScalingMethod.class.equals(fieldType)) {
							field.set(object, ScalingMethod.valueOf(xmlValue.toUpperCase(Locale.ENGLISH)));
						} else if (GameObject.class.isAssignableFrom(fieldType)) {
							field.set(object, GameState.getGameObjectByInternalId(xmlValue));
						} else if (LightDescriptor.class.equals(fieldType)) {
							field.set(object, LightDescriptor.getLightDescriptor(xmlValue));
						} else if (Faction.class.equals(fieldType)) {
							field.set(object, Faction.getFaction(xmlValue));
						} else if (AIScript.class.equals(fieldType)) {
							field.set(object, AIScript.getAIScript(xmlValue));
						} else if (ParticleEffect.class.equals(fieldType)) {
							field.set(object, ParticleEffectManager.getParticleEffect(xmlValue));
						} else if (WeatherProfile.class.equals(fieldType)) {
							field.set(object, WeatherProfile.getWeatherProfile(xmlValue));
						} else if (Effect.class.equals(fieldType)) {
							field.set(object, Effect.getEffect(xmlValue));
						} else if (Locale.class.equals(fieldType)) {
							field.set(object, Locale.forLanguageTag(xmlValue));
						} else if (SaveablePolygon.class.equals(fieldType)) {
							field.set(object, new SaveablePolygon(xmlValue));
						} else if (TrapType.class.equals(fieldType)) {
							field.set(object, TrapType.getTrap(xmlValue));
						} else if (AudioProfile.class.equals(fieldType)) {
							field.set(object, AudioProfile.getAudioProfile(chooseRandom(xmlValue)));
						} else if (CharacterModel.class.equals(fieldType)) {
							field.set(object, CharacterModel.getModel(chooseRandom(xmlValue)));
						} else if (Role.class.equals(fieldType)) {
							field.set(object, Role.getRole(xmlValue));
						}
						field.setAccessible(false);
					}
				}
				objectClass = objectClass.getSuperclass();
			}
		} catch (IllegalArgumentException e) {
			throw new GdxRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	private static String chooseRandom(final String value) {
		if (StringUtil.nullOrEmptyString(value)) {
			return value;
		}
		if (!value.contains(",")) {
			return value;
		}
		String[] split = value.split(",");
		return new Array<String>(split).random().trim();
	}

	public static String getDottedValue(Element element, String fieldPath) {
		if (fieldPath.contains(".")) {
			String childElementName = fieldPath.substring(0, fieldPath.indexOf("."));
			String childNames[] = getLowerAndCapitalStart(childElementName);
			Element child = element.getChildByName(childNames[0]);
			if (child == null) {
				element.getChildByName(childNames[1]);
			}
			if (child != null) {
				return getDottedValue(child, fieldPath.substring(fieldPath.indexOf(".") + 1, fieldPath.length()));
			} else {
				return null;
			}
		} else {
			String attributeName[] = getLowerAndCapitalStart(fieldPath);
			String xmlValue = element.get(attributeName[0], null);
			if (xmlValue == null) {
				xmlValue = element.get(attributeName[1], null);
			}
			return xmlValue;
		}
	}

	public static void writePrimitives(Object object, XmlWriter writer) throws IOException {
		writePrimitives(object, writer, false);
	}

	/**
	 * This will write out using the supplied writer all class members from the
	 * supplied Object that have their name "s_".
	 * 
	 * They will not be written out if they are null.
	 * 
	 * @param object
	 *            - the object from which to read the members
	 * @param writer
	 *            - the XmlWriter which to use to write the members
	 */
	public static void writePrimitives(Object object, XmlWriter writer, boolean asElements) throws IOException {
		try {
			Class<?> objectClass = object.getClass();
			while (objectClass != null && objectClass != Object.class) {
				Field[] fields = objectClass.getDeclaredFields();

				for (Field field : fields) {
					if (!field.getName().startsWith("s_")) {
						continue;
					}
					field.setAccessible(true);
					Object fieldValue = field.get(object);
					field.setAccessible(false);
					if (fieldValue != null) {
						if (asElements) {
							writer.element(field.getName().replaceFirst("s_", ""), fieldValue.toString());
						} else {
							writer.attribute(field.getName().replaceFirst("s_", ""), fieldValue.toString());
						}
					}
				}
				objectClass = objectClass.getSuperclass();
			}
		} catch (IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}
	}

	/**
	 * Returns a primitive array that will as first element contain the supplied
	 * string with its first letter converted to lower case and as second
	 * element the string with the first letter converted to upper case.
	 * 
	 * So if "ChickEn" is supplied, this will return [0] = chickEn; [1] =
	 * ChickEn
	 * 
	 * If the supplied string is empty or null, both return values will be
	 * empty.
	 */
	public static String[] getLowerAndCapitalStart(String string) {
		String[] returnValue = new String[2];
		if (string == null || string.length() < 1) {
			returnValue[0] = "";
			returnValue[1] = "";
		} else {
			String firstCharacter = string.substring(0, 1);
			String restOfTheString = string.substring(1);
			returnValue[0] = firstCharacter.toLowerCase(Locale.ENGLISH) + restOfTheString;
			returnValue[1] = firstCharacter.toUpperCase(Locale.ENGLISH) + restOfTheString;
		}
		return returnValue;
	}

	/**
	 * Parses the supplied InputStream without closing it at the end using the
	 * supplied XmlReader.
	 * 
	 * @param inStream
	 * @return
	 */
	public static Element parseNonCLosing(XmlReader parser, InputStream inStream) {
		try {
			InputStreamReader inReader = new InputStreamReader(inStream, "UTF-8");
			char[] data = new char[1024];
			int offset = 0;
			while (true) {
				int length = inReader.read(data, offset, data.length - offset);
				if (length == -1)
					break;
				if (length == 0) {
					char[] newData = new char[data.length * 2];
					System.arraycopy(data, 0, newData, 0, data.length);
					data = newData;
				} else
					offset += length;
			}
			return parser.parse(data, 0, offset);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

}
