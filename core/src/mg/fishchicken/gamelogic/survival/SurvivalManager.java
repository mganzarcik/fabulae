package mg.fishchicken.gamelogic.survival;

import java.io.IOException;
import java.util.Iterator;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class SurvivalManager implements XMLLoadable{
	
	public static final String STRING_TABLE="survival."+Strings.RESOURCE_FILE_EXTENSION;
	
	public static final String XML_TIMES = "times";

	public enum SurvivalHazard {
		HUNGER {
			@Override
			public String getXMLName() {
				return "hunger";
			}
		},
		THIRST {
			@Override
			public String getXMLName() {
				return "thirst";
			}
		},
		SLEEPDEPRIVATION {
			@Override
			public String getXMLName() {
				return "sleepDeprivation";
			}
		};
		public abstract String getXMLName();
		
		public String getUIName() {
			return Strings.getString(STRING_TABLE, this.name());
		}
	};
	
	private ObjectMap<SurvivalHazard, SurvivalHazardConfig> hazards = new ObjectMap<SurvivalHazard, SurvivalHazardConfig>();
	
	public  SurvivalManager(FileHandle file) {
		try {
			loadFromXML(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	/**
	 * Adds all survival modifiers to the supplied ModifierCotainer that apply
	 * for the supplied Hazard and stage.
	 * 
	 * The modifiers will not actually be added as individual mods, they will be combined
	 * into one and then added to the Modifier Container, or, if the container already
	 * has a modifier created for this survival hazard, combined with that existing modifier.
	 * 
	 * @param mc
	 * @param hazard
	 * @param stage
	 */
	public void addModifiers(ModifierContainer mc, SurvivalHazard hazard, int stage) {
		Modifier existingModifier = findModifierFor(mc, hazard);
		
		Iterator<Modifier> modifiers = getModifiersFor(hazard, stage);
		if (existingModifier == null && modifiers.hasNext()) {
			existingModifier = new Modifier(getModifierId(hazard), "");
			mc.addModifier(existingModifier);
		}
		while (modifiers.hasNext()) {
			existingModifier.add(modifiers.next());
		}
		if (existingModifier != null) {
			existingModifier.setName("["+hazard.getUIName()+"] "+getStageName(hazard, stage));
		}
		mc.onModifierChange();
	}
	
	private static Modifier findModifierFor(ModifierContainer mc, SurvivalHazard hazard) {
		String modifierId = getModifierId(hazard);
		Iterator<Modifier> iterator = mc.getModifiers();
		while (iterator.hasNext()) {
			Modifier mod = iterator.next();
			String id = mod.getId();
			if (id != null && id.equals(modifierId)) {
				return mod;
			}
		}
		return null;
	}
	
	/**
	 * Removes all survival modifiers from the supplied ModifierCotainer that were added
	 * there for the supplied Hazard and stage.
	 * @param mc
	 * @param hazard
	 * @param stage -if is is equal to 1, all modifiers for the supplied hazard will be removed
	 */
	public void removeModidifiers(ModifierContainer mc, SurvivalHazard hazard, int stage) {
		if (stage == 1) {
			removeModidifiers(mc, hazard);
			return;
		}
		Modifier existingModifier = findModifierFor(mc, hazard);
		if (existingModifier == null) {
			return;
		}
		Iterator<Modifier> modifiers = getModifiersFor(hazard, stage);
		while (modifiers.hasNext()) {
			existingModifier.substr(modifiers.next());
		}
		existingModifier.setName("["+hazard.getUIName()+"] "+getStageName(hazard, stage-1));
		mc.onModifierChange();
	}
	
	/**
	 * Removes all survival modifiers from the supplied ModifierCotainer that were added
	 * there for the supplied Hazard, regardless of stage.
	 * @param mc
	 * @param hazard
	 */
	public static void removeModidifiers(ModifierContainer mc, SurvivalHazard hazard) {
		String modifierPrefix = getModifierId(hazard);
		Iterator<Modifier> iterator = mc.getModifiers();
		while (iterator.hasNext()) {
			Modifier mod = iterator.next();
			String id = mod.getId();
			if (id != null && id.equals(modifierPrefix)) {
				iterator.remove();
			}
		}
		mc.onModifierChange();
	}
	
	public String getStageName(SurvivalHazard hazard, int stage) {
		return hazards.get(hazard).getName(stage);
	}
	
	private static String getModifierId(SurvivalHazard hazard) {
		return "__survival"+hazard.toString();
	}
	
	public Iterator<Modifier> getModifiersFor(SurvivalHazard hazard, int stage) {
		return hazards.get(hazard).getModifiers(stage);
	}

	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		
		for (SurvivalHazard hazard : SurvivalHazard.values()) {
			Element hazardElement = root.getChildByName(hazard.getXMLName());
			hazards.put(hazard, new SurvivalHazardConfig(hazardElement));
		}	
	}
	
	private static class SurvivalHazardConfig {
		private ObjectMap<Integer, SurvivalHazardStage> stages = new ObjectMap<Integer, SurvivalHazardStage>();
		private SurvivalHazardStage defaultStage;
		
		private SurvivalHazardConfig(Element element) {
			for (int i = 0; i < element.getChildCount(); ++i) {
				Element stageElement = element.getChild(i);
				String name = stageElement.get(XMLUtil.XML_ATTRIBUTE_NAME, "");
				String timesString = stageElement.get(XML_TIMES, null);
				Integer times = timesString != null ? Integer.valueOf(timesString) : null;
				
				SurvivalHazardStage stage = new SurvivalHazardStage(name);
				XMLUtil.readModifiers(stage, stageElement);
				if (times == null) {
					defaultStage = stage;
				} else {
					this.stages.put(times, stage);
				}
			}
		}
		
		public Iterator<Modifier> getModifiers(int stage) {
			return getStage(stage).getModifiers();
		}
		
		public String getName(int stage) {
			return getStage(stage).getName();
		}
		
		public SurvivalHazardStage getStage(int stage) {
			if (stages.containsKey(stage)) {
				return stages.get(stage);
			}
			return defaultStage;
		}
	}
	
	private static class SurvivalHazardStage implements ModifierContainer {
		private Array<Modifier> modifiers = new Array<Modifier>();
		private String name;
		
		private SurvivalHazardStage(String name) {
			this.name = name;
		}
		
		@Override
		public String getName() {
			return Strings.getString(name);
		}

		@Override
		public void onModifierChange() {	
		}
		
		@Override
		public void addModifier(Modifier modifier) {
			modifiers.add(modifier);
		}

		@Override
		public Iterator<Modifier> getModifiers() {
			return modifiers.iterator();
		}
	}
	
}
