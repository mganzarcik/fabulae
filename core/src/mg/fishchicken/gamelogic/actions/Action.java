package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamestate.characters.Stats;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * An Action represents something that should be executed on a ActionContainer over
 * time. An Action can have a duration, but can also be infinite. It updates the
 * ActionContainers state every frame.
 * 
 * Actions can be paused and resumed and very importantly, their instances can
 * be reused.
 * 
 * Every action implements an init method that resets its state so that it can
 * be reused with a new ActionContainer.
 * 
 * Actions can also belong to a specific slot on the ActionContainer. Only one Action can
 * occupy a given slot, which can be used to for example make sure a Character
 * does not have two Move actions active at the same time.
 * 
 * @author Annun
 * 
 */
public abstract class Action {
	
	/**
	 * A list of all actions that are considered "verbs" in the game logic, i.e. commands that can
	 * be given to characters in the game (like move to, pick up, etc.).
	 */
	public static final Array<Class<? extends Action>> VERB_ACTIONS = new Array<Class<? extends Action>>();
	
	/**
	 * Verbs that, when controlled by the player, only full player characters can use. 
	 * 
	 * This does not limit their use by NPCs when controlled by the AI.
	 */
	public static final Array<Class<? extends Action>> PC_ONLY_VERBS= new Array<Class<? extends Action>>();
	
	static {
		VERB_ACTIONS.add(MoveToAction.class);
		VERB_ACTIONS.add(AttackAction.class);
		VERB_ACTIONS.add(UseInventoryItemAction.class);
		VERB_ACTIONS.add(UsePerkAction.class);
		VERB_ACTIONS.add(CastSpellAction.class);
		VERB_ACTIONS.add(PickUpAction.class);
		VERB_ACTIONS.add(TalkToAction.class);
		VERB_ACTIONS.add(WanderAction.class);
		VERB_ACTIONS.add(UseGameObjectAction.class);
		
		PC_ONLY_VERBS.add(UseInventoryItemAction.class);
		PC_ONLY_VERBS.add(PickUpAction.class);
		PC_ONLY_VERBS.add(UseGameObjectAction.class);
		PC_ONLY_VERBS.add(TalkToAction.class);
	}
	public static String getUINameForAction(Class<? extends Action> actionClass) {
		String name = actionClass.getSimpleName();
		if (name.endsWith("Action")) {
			name = name.substring(0, name.lastIndexOf("Action"));
		}
		
		return Strings.getString(STRING_TABLE, name);
	}
	
	public static final String STRING_TABLE = "actions."+Strings.RESOURCE_FILE_EXTENSION;
	
	public static final String XML_ATTRIBUTE_PAUSABLE_ONLY = "pausableOnly";
	public static final String XML_ATTRIBUTE_X = "x";
	public static final String XML_ATTRIBUTE_Y = "y";
	public static final String XML_ATTRIBUTE_MAP = "map";
	public static final String XML_ATTRIBUTE_RADIUS = "radius";
	public static final String XML_ATTRIBUTE_SPEED = "speed";
	public static final String XML_ATTRIBUTE_DELAY = "delay";
	public static final String XML_ATTRIBUTE_LOOP= "loop";
	public static final String XML_ATTRIBUTE_CHANCE_TO_MOVE = "chanceToMove";
	public static final String XML_ATTRIBUTE_TARGET = "target";
	public static final String XML_ATTRIBUTE_PERK = "perk";
	public static final String XML_ATTRIBUTE_SPELL= "spell";
	public static final String XML_ATTRIBUTE_DURATION = "duration";
	public static final String XML_ATTRIBUTE_RESET_STATE = "resetState";
	public static final String XML_ATTRIBUTE_TOTAL_TIME = "totalTime";
	public static final String XML_ATTRIBUTE_VISIBLE_ONLY = "visibleOnly";
	public static final String XML_ATTRIBUTE_FLICK_DURATION = "flickDuration";
	public static final String XML_ATTRIBUTE_TEXT = "text";
	public static final String XML_ATTRIBUTE_STATE = "state";
	public static final String XML_ATTRIBUTE_STATE_TIME = "stateTime";

	protected static GameState gameState;	 
	
	public static void setGameState(GameState gameState) {
		Action.gameState = gameState;
	}
	
	public abstract void init(ActionsContainer ac, Object... parameters);
	public abstract void update(float deltaTime);
	public abstract boolean isFinished();
	
	/** 
	 * If an action is blocking in combat, it will
	 * prevent any other action from taking place.
	 * 
	 * @return
	 */
	public abstract boolean isBlockingInCombat();
	public abstract void loadFromXML(Element actionElement);
	public abstract void writeToXML(XmlWriter writer) throws IOException;
	public abstract void pause();
	public abstract void resume();
	
	/**
	 * Resets this action to its initial state.
	 */
	public abstract void reset();
	
	/**
	 * Whether or not this action is currently paused.
	 * @return
	 */
	public abstract boolean isPaused();
	
	/**
	 * Whether or not this action is considered a "verb" in the game logic, i.e. command that can
	 * be given to characters in the game (like move to, pick up, etc.).
	 * 
	 * @return
	 */
	public abstract boolean isVerbAction();
	
	/**
	 * Is called when the action gets removed
	 * from the supplied action container. 
	 * @param ac
	 */
	public abstract void onRemove(ActionsContainer ac);
	
	/**
	 * Each action is assigned a slot - if the slot is a non negative number, it
	 * means only one action can occupy that slot at a given time.
	 * 
	 * If multiple actions with the same slot are added to the game object, only
	 * the last one will be executed.
	 * 
	 * Actions with negative slot are considered to occupy no slot at all and
	 * multiple can be active at the same time.
	 */
	public abstract int getActionSlot();
	
	/**
	 * Gets the AP cost for the supplied action, if used on the supplied target by something
	 * with the supplied Stats.
	 * 
	 * @param stats
	 * @param actionClass
	 * @param target
	 * @return
	 */
	public static int getAPCostForAction(Stats stats, Class<? extends Action> actionClass, Object target) {
		if (AttackAction.class.equals(actionClass)) {
			return stats.getAPCostToAttack();
		} else if (PickUpAction.class.equals(actionClass)) {
			return Configuration.getAPCostPickUp();
		} else if (UseGameObjectAction.class.equals(actionClass) && target instanceof UsableGameObject) {
			return ((UsableGameObject)target).getApCostToUse();
		} else if (LockpickAction.class.equals(actionClass)) {
			return Configuration.getAPCostUseItem();
		} else if (DisarmTrapAction.class.equals(actionClass)) {
			return Configuration.getAPCostDisarmTrap();
		}
		return 0;
	}

	/**
	 * Creates a new Action object from the supplied XML element
	 * but does not initialize it.
	 * 
	 * @param actionElement
	 * @return
	 */
	public static Action readFromXML(Element actionElement) {
		return readFromXML(actionElement, null);
	}

	/**
	 * Creates a new Action object from the supplied XML element
	 * and initializes it using the supplied actions container.
	 * If the supplied container is null, the action is not initialized.
	 * 
	 * The new action is then returned, but it is NOT added to the
	 * container.
	 * @param actionElement
	 * @param acToInit
	 * @return
	 */
	public static Action readFromXML(Element actionElement, ActionsContainer acToInit) {
		try {
			Class<? extends Action> actionClass = getActionClassForSimpleName(actionElement.getName());
			Action actionInstance = actionClass.newInstance(); 
			if (acToInit != null) {
				actionInstance.init(acToInit);
			}
			actionInstance.loadFromXML(actionElement);
			return actionInstance;
		} catch (ClassNotFoundException e) {
			throw new GdxRuntimeException(e);
		} catch (InstantiationException e) {
			throw new GdxRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	/**
	 * Gets the Action class from the supplied simple class name.
	 * 
	 * @param simpleName
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends Action> getActionClassForSimpleName(String simpleName) throws ClassNotFoundException {
		String className = Action.class.getPackage().getName()+"."+StringUtil.capitalizeFirstLetter(simpleName);
		return (Class<? extends Action>) Class.forName(className);
	}
}
