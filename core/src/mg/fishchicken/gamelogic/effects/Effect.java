package mg.fishchicken.gamelogic.effects;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import groovy.lang.Binding;
import groovy.lang.Script;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.projectiles.ProjectileTarget;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.GroovyUtil;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.time.GameCalendarDate;

/**
 * Effect is basically a Groovy script. Effects can be attached to
 * different things like usables and are executed on some player action.
 * 
 * Each effect takes two parameters when it is executed, the user of 
 * the effect (the initiator) and the target of the effect.
 * 
 * @author Annun
 *
 */
public class Effect implements XMLLoadable, ThingWithId {

	private static ObjectMap<String, String> effects = new ObjectMap<String, String>();
	
	public static final String XML_DESCRIPTION = "description";
	public static final String XML_EXTRA_PARAMETERS = "extraParameters";
	public static final String XML_DESCRIPTION_PARAMETERS = "descriptionParameters";
	public static final String XML_PARAMETER = "parameter";
	public static final String XML_TYPE = "type";
	public static final String XML_ON_HIT = "onHit";
	public static final String XML_ON_END = "onEnd";
	public static final String XML_PERSISTENT = "persistent";
	public static final String XML_CONDITION = "condition";
	public static final String XML_EFFECT = "effect";
	public static final String XML_CONTAINER = "container";
	public static final String XML_DURATION = "duration";
	
	public static final String USER = "user";
	public static final String TARGET = "target";
	public static final String TARGET_AREA = "targetArea";
	public static final String TURN = "turn";
	
	private String id;
	@XMLField(fieldPath="description.text")
	private String description  = null;
	@XMLField(fieldPath="indicator.effectId")
	private String indicator = null;
	@XMLField(fieldPath="indicator.delay")
	private float indicatorDelay = 0f;
	@XMLField(fieldPath="indicator.xOffset")
	private float indicatorXOffset = 0f;
	@XMLField(fieldPath="indicator.yOffset")
	private float indicatorYOffset = 0f;
	@XMLField(fieldPath="visible")
	private boolean isVisible;
	private Array<String> types;
	//private String s_onEndMessage;
	private Script descriptionParamsScript = null;
	private Script onHitScript = null;
	private Script persistentScript = null;
	private Script onEndScript = null;
	private Script durationScript = null;
	private Script conditionScript = null;
	private Array<EffectParameterDefinition> parameters;
	
	public static Effect getEffect(String id) {
		return Assets.get(effects.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Gathers all Effects and registers them in the AssetManager
	 * so that they can be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherEffects() throws IOException {
		Assets.gatherAssets(Configuration.getFolderEffects(), "xml", Effect.class, effects);
	}
	
	/**
	 * Creates a new effect by loading it from the supplied xml file.
	 * 
	 * The id of the effect will be same as the filename without the extension.
	 * 
	 * @param id
	 * @throws IOException 
	 */
	public Effect(FileHandle file) throws IOException {
		this.id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		parameters = new Array<EffectParameterDefinition>();
		isVisible = true;
		types = new Array<String>();
		loadFromXML(file);
	}
	
	public String getId() {
		return id;
	}
	
	/**
	 * Whether or not this Effect is visible
	 * on the effects summary screen of a character.
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return isVisible;
	}
	
	/**
	 * Executes the duration script of this effect, which will determine the duration
	 * of any persistent effects this effect has. The duration is in turns, which can be
	 * translated to seconds by using {@link Configuration#getCombatTurnDurationInGameSeconds()}.
	 * 
	 * @param context
	 * @return
	 */
	private Float getDuration(Binding context) {
		if (durationScript == null) {
			return 0f;
		}
		
		durationScript.setBinding(context);
		Object returnValue = durationScript.run();
		if (returnValue == null) {
			return 0f;
		}
		if (returnValue instanceof Float) {
			return (Float) returnValue;
		}
		if (returnValue instanceof Integer) {
			return ((Integer)returnValue).floatValue();
		}
		if (returnValue instanceof String) {
			try {
				return Float.parseFloat((String)returnValue);
			} catch (NumberFormatException e) {
				throw new GdxRuntimeException(e);
			}
		}
		throw new GdxRuntimeException("Could not determine effect duration for effect "+getId()+", duration script is not empty but did not return a number.");
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
		
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		XMLUtil.readPrimitiveMembers(this, root);
		onHitScript = XMLUtil.readScript(id, root.getChildByName(XML_ON_HIT), onHitScript);
		durationScript = XMLUtil.readScript(id, root.getChildByName(XML_DURATION), durationScript);
		onEndScript = XMLUtil.readScript(id, root.getChildByName(XML_ON_END), onEndScript);
		persistentScript = XMLUtil.readScript(id, root.getChildByName(XML_PERSISTENT), persistentScript);
		conditionScript = XMLUtil.readScript(id, root.getChildByName(XML_CONDITION), conditionScript);

		Element descriptionElement = root.getChildByName(XML_DESCRIPTION);
		if (descriptionElement != null) {
			descriptionParamsScript = XMLUtil.readScript(id, descriptionElement.getChildByName(XML_EXTRA_PARAMETERS), descriptionParamsScript);
		}
		
		Element parametersElement = root.getChildByName(XMLUtil.XML_PARAMETERS);
		if (parametersElement != null)  {
			for (int i = 0; i < parametersElement.getChildCount(); ++i) {
				Element parameterElement = parametersElement.getChild(i);
				parameters.add(EffectParameterDefinition.readFromXML(parameterElement));
			}
		}
		
		Element typesElement = root.getChildByName(XML_TYPE);
		if (typesElement != null && !typesElement.getText().isEmpty()) {
			String[] types = typesElement.getText().split(",");
			for (String type : types) {
				this.types.add(type.trim().toUpperCase(Locale.ENGLISH));
			}
		}
	}
	
	/**
	 * Returns true if this effect is 
	 * of the supplied type.
	 * 
	 * @param types
	 * @return
	 */
	public boolean isOfType(String type) {
		return types.contains(type.trim().toUpperCase(Locale.ENGLISH), false);
	}
	
	/**
	 * Gets a human readable description of this effect.
	 * 
	 * @param parameters
	 * @return
	 */
	public String getDescription(Object user, Array<EffectParameter> parameters) {
		if (description == null) {
			return null;
		}
		
		return Strings.getString(description, buildDescriptionParameters(user, parameters));
	}
	
	private Object[] buildDescriptionParameters(Object user, Array<EffectParameter> parameters) {
		Binding context = new Binding();
		context.setVariable(USER, user);
		
		if (parameters != null) {
			for (EffectParameter param : parameters) {
				context.setVariable(param.getName(),  param.getValue());
			}
		}
		
		int size = 1;
		if (parameters != null) {
			size += parameters.size;
		}
		List<?> additionalScriptParams = null;
		if (descriptionParamsScript != null) {
			descriptionParamsScript.setBinding(context);
			Object returnValue = descriptionParamsScript.run();
			if (returnValue instanceof List) {
				additionalScriptParams = (List<?>) returnValue;
			}
		}
		
		if (additionalScriptParams != null) {
			size += additionalScriptParams.size();
		}
		
		Object[] params = new Object[size];

		params[0] = getDuration(context);
		int i = 1;
		if (additionalScriptParams != null && additionalScriptParams.size() > 0) {
			for (Object param : additionalScriptParams) {
				params[i++] = param;
			}
		}
		if (parameters != null) {
			for (EffectParameter param : parameters) {
				params[i++] = param.getValue();
			}
		}
		return params;
	}
	
	/**
	 * Returns an array describing which parameters this effect
	 * accepts.
	 * 
	 * @return
	 */
	public Array<EffectParameterDefinition> getParameterDefinitions() {
		return parameters;
	}
	
	/**
	 * Executes this effect as if it was originating from the supplied container,
	 * initiated by the user and targeted on the target with the supplied parameters.
	 * 
	 * This will run the OnHit action, and if the effect has a positive duration,
	 * create a persistent effect from it and attach it to the target.
	 * 
	 * @param effectContainer
	 * @param user
	 * @param target
	 * @param parameters
	 */
	public void executeEffect(EffectContainer effectContainer, GameObject user, ProjectileTarget target,  Array<EffectParameter> parameters) {
		
		if (!target.filterUnviableTargets(this, effectContainer, user)) {
			return;
		}
		
		Binding context = new Binding();
		context.setVariable(USER, user);
		
		if (parameters != null) {
			for (EffectParameter param : parameters) {
				context.setVariable(param.getName(), param.getValue());
			}
		}
		
		float duration=getDuration(context);
		
		context.setVariable(TARGET_AREA, target);
		
		if (onHitScript != null) {
			for (GameObject go : target.getGameObjects()) {
				context.setVariable(TARGET, go);
				onHitScript.setBinding(context);
				onHitScript.run();
			}
		}

		if (duration > 0) {
			target.addPersistentEffect(effectContainer, this, duration, user);
		}
	}

	@Override
	public String toString() {
		return id;
	}
	
	public static class PersistentEffect implements XMLSaveable {
		private Effect s_parentEffect;
		private String s_name;
		private float s_timeToNextTurn;
		private GameCalendarDate dateToEnd;
		private int s_turn;
		private String s_userId;
		private Binding context;
		private GameObject target;
		private GameObject user;
		private Array<EffectParameter> parameters;
		private Object[] descriptionParameters;
		private boolean s_isFinished;
		
		public PersistentEffect(Element xmlElement, GameObject target) throws IOException {
			loadFromXML(xmlElement);
			context = null;
			this.target = target;
		}
		
		public PersistentEffect(EffectContainer container, Effect parentEffect, float duration, GameObject user, GameObject target) {
			this.s_parentEffect = parentEffect;
			dateToEnd = new GameCalendarDate(GameState.getCurrentGameDate());
			dateToEnd.addToSecond(Configuration.getCombatTurnDurationInGameSeconds() * duration * 2);
			s_timeToNextTurn = 1;
			s_turn = 0;
			s_isFinished = false;
			s_userId = user.getInternalId();
			s_name = container.getRawName();
			parameters = container.getEffects().get(parentEffect);
			descriptionParameters = parentEffect.buildDescriptionParameters(user, parameters);
			this.user = user;
			this.target = target;
			getContext();
			if (parentEffect.indicator != null && target.getMap() != null) {
				target.getMap().getParticleEffectManager().attachParticleEffect(target, parentEffect.indicator,parentEffect.indicatorDelay, s_parentEffect.indicatorXOffset, s_parentEffect.indicatorYOffset);
			}
		}
		
		/**
		 * Returns the Id of this persistent effect.
		 * 
		 * This is always the same as the Id of the effect
		 * this was created from.
		 * 
		 * @return
		 */
		public String getId() {
			return s_parentEffect.id;
		}
		
		/**
		 * Gets the user friendly, localized name of this effect.
		 * @return
		 */
		public String getName() {
			return Strings.getString(s_name);
		}
		
		/**
		 * Gets the description of this persistent effect. Will return null
		 * if the PE is not fully initialized yet.
		 * @return
		 */
		public String getDescription() {
			return Strings.getString(s_parentEffect.description, descriptionParameters);
		}
		
		/**
		 * Whether or not this Persistent Effect is visible
		 * on the effects summary screen of a character.
		 * 
		 * @return
		 */
		public boolean isVisible() {
			return s_parentEffect.isVisible;
		}
		
		/**
		 * Returns true if this persistent effect is 
		 * of all the supplied types.
		 * 
		 * @param types
		 * @return
		 */
		public boolean isOfTypes(String... types) {
			for (String type : types) {
				if (!s_parentEffect.isOfType(type)) {
					return false;
				}
			}
			return true;
		}
		
		private Binding getContext() {
			if (context == null) {
				if (user == null) {
					user = GameState.getGameObjectByInternalId(s_userId);
				}
				if (user != null) {
					context = new Binding();
					context.setVariable(USER, user);
					context.setVariable(TARGET, target);
					for (EffectParameter param : parameters) {
						context.setVariable(param.getName(), param.getValue());
					}
				}
			}
			return context;
		}
		
		/**
		 * Returns false if this effect has a defined
		 * condition and it is no longer valid.
		 */
		public boolean isConditionValid() {
			// if we don't have a condition, or don't have a context (meaning we are not yet fully loaded), just return true
			if (GameState.isLoadingGame() || s_parentEffect.conditionScript == null) {
				return true;
			}
			s_parentEffect.conditionScript.setBinding(getContext());
			
			return GroovyUtil.evaluateCondition(s_parentEffect.conditionScript);
		}
		
		/**
		 * Executes this persistent effect. This will
		 * lower the remaining duration by the supplied delta, 
		 * and, if the effect finishes this turn, it will return true.
		 * The caller is then responsible for calling finish() on the 
		 * effect to execute any OnEnd activities associated with it.
		 * 
		 * @delta the amount of turns passed since the last execution
		 * 
		 * @return true if the persistent effect is now finished
		 * and should be finished and removed, false otherwise.
		 */
		public boolean executePersistentEffect(float delta) {
			GameCalendarDate currDate = GameState.getCurrentGameDate();

			s_timeToNextTurn -= delta;
			// if enough time has passed to pass a whole turn,
			// execute per-turn effects if we have any
			if (s_timeToNextTurn <= 0) {
				s_timeToNextTurn = 1;
				++s_turn;
				if (s_parentEffect.persistentScript != null) {
					getContext().setVariable(TURN, s_turn);
					s_parentEffect.persistentScript.setBinding(getContext());
					s_parentEffect.persistentScript.run();
				}
			}
			
			if (dateToEnd == null || dateToEnd.compareTo(currDate) <= 0) {
				return true;
			}
			
			if (s_parentEffect.indicator != null && target.getMap() != null && target.getMap().getParticleEffectManager()
					.getCount(target, s_parentEffect.indicator) < 1) {
				target.getMap().getParticleEffectManager().attachParticleEffect(target, s_parentEffect.indicator,0, s_parentEffect.indicatorXOffset, s_parentEffect.indicatorYOffset);
			}
			
			return false;
		}
		
		/**
		 * Marks this persistent effect as finished.
		 * 
		 * This will run any OnEnd actions the effect has defined,
		 * kill associated particle effect indicators and remove
		 * the persistent effect from the target.
		 * 
		 */
		public void finish() {
			if (s_isFinished) {
				return;
			}
			s_isFinished = true;
			dateToEnd = null;
			if (s_parentEffect.onEndScript != null) {
				s_parentEffect.onEndScript.setBinding(getContext());
				s_parentEffect.onEndScript.run();
			}
			if (s_parentEffect.indicator != null && target.getMap() != null) {
				target.getMap().getParticleEffectManager().kill(target, s_parentEffect.indicator);
			}
		}

		@Override
		public void writeToXML(XmlWriter writer) throws IOException {
			writer.element(XML_EFFECT);
			XMLUtil.writePrimitives(this, writer);
			
			if (dateToEnd != null) {
				writer.element(XMLUtil.XML_END_DATE);
				dateToEnd.writeToXML(writer);
				writer.pop();
			}
			
			writer.element(XMLUtil.XML_PARAMETERS);
			for (EffectParameter param : parameters) {
				param.writeToXML(writer);
			}
			writer.pop();
			
			writer.element(XML_DESCRIPTION_PARAMETERS);
			for (Object param : descriptionParameters) {
				writer.element(XML_PARAMETER);
				writer.text(param);
				writer.pop();
			}
			writer.pop();
			
			writer.pop();
		}

		@Override
		public void loadFromXML(Element root) throws IOException {
			XMLUtil.readPrimitiveMembers(this, root);
			
			Element dateElement = root.getChildByName(XMLUtil.XML_END_DATE);
			if (dateElement != null) {
				dateToEnd = new GameCalendarDate(GameState.getCurrentGameDate());
				dateToEnd.readFromXML(dateElement);
			}
			
			Element paramsElement = root.getChildByName(XMLUtil.XML_PARAMETERS);
			parameters = new Array<EffectParameter>();
			for (int i = 0; i < paramsElement.getChildCount(); ++i) {
				parameters.add(new EffectParameter(paramsElement.getChild(i)));
			}
			
			paramsElement = root.getChildByName(XML_DESCRIPTION_PARAMETERS);
			descriptionParameters= new Object[paramsElement.getChildCount()];
			for (int i = 0; i < paramsElement.getChildCount(); ++i) {
				// this will convert everything into strings, but this should be okay as long 
				// as no special formatting is used in the message format of the description
				descriptionParameters[i] = paramsElement.getChild(i).getText();  
			}
		}
	}
	
	public static class EffectParameterDefinition {
		private String name;
		private String s_type = "";
		private boolean s_mandatory;
		
		public static EffectParameterDefinition readFromXML(Element parameterElement) {
			EffectParameterDefinition returnValue = new EffectParameterDefinition();
			XMLUtil.readPrimitiveMembers(returnValue, parameterElement);
			returnValue.name = parameterElement.getName();
			return returnValue;
		}
		
		public String getName() {
			return name;
		}
		
		public String getType() {
			return s_type;
		}
		
		public boolean isMandatory() {
			return s_mandatory;
		}
	}
	
	/**
	 * Gets the effects of this Perk as a human
	 * readable string.
	 * 
	 * This does not include any modifiers
	 * associated with this Perk.
	 * 
	 * @return null if there are no associated effects, the string otherwise
	 */
	public static String getEffectsAsString(EffectContainer ec, Object user) {
		ObjectMap<Effect, Array<EffectParameter>> effects = ec.getEffects();
		if (effects.size == 0) {
			return null;
		}
		StringBuilder fsb = StringUtil.getFSB();
		int i = 0;
		for (Effect effect : effects.keys()) {
			String desc = effect.getDescription(user, effects.get(effect));
			if (desc == null) {
				continue;
			}
			if (i > 0) {
				fsb.append("\n");
			}
			fsb.append(desc);
			++i;
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue;
	}

}

