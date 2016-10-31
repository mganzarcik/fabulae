package mg.fishchicken.core;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.audio.AudioOriginator;
import mg.fishchicken.core.projectiles.ProjectileTarget;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.ActionFilter;
import mg.fishchicken.gamelogic.actions.ActionsContainer;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.Effect.PersistentEffect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.GameObjectPosition;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.Variables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * This is the core class of the game. GameObjects represent everything that is
 * not tiles that is visible on the map for the player and that the player can
 * possibly interact with.
 * 
 * Every GO is identified by an ID, is registered either to Global or to the
 * GameMap that contains it, has a position, width, height and color, has a list
 * of Actions that are being executed on it and a list of custom variables.
 * 
 * Actions added to the GO are all executed each frame until they are finished,
 * at which point they are automatically removed. There is no Action queuing
 * system in place - if the caller wishes to queue / chain actions, he should
 * use a chaining Action.
 * 
 * @author Annun
 * 
 */
public abstract class GameObject implements ActionsContainer, VariableContainer, XMLSaveable, AudioOriginator, ProjectileTarget, PositionedThing, ColoredThing, ThingWithId {

	public static final String XML_TEMPORARY_HOSTILITY= "temporaryHostility";
	public static final String XML_DURATION = "duration";
	public static final String XML_START= "start";
	public static final String XML_DAMAGE_QUEUE = "damageQueue";
	public static final String XML_DAMAGE_INFO = "damageInfo";
	public static final String XML_COMBAT_END_SEARCH_ACTION = "combatEndSearchAction";
	public static final String XML_CURRENT_AI_ACTION = "currentAIAction";
	public static final String XML_AI_BACKUP = "aiBackup";
	public static final String XML_VISITED = "visited";
	public static final String XML_CURRENT = "current";
	public static final String XML_ON_DEATH = "onDeath";
	public static final String XML_FORBIDDEN_ACTIONS = "forbiddenActions";
	public static final String XML_FORBIDDEN_BY = "forbiddenBy";
	public static final String XML_ATTRIBUTE_IMPLEMENTATION = "implementation";
	
	public static final String ANIMATION_INFOS_EXTENSION = "csv";
	
	protected static GameState gameState; 
	
	static void setGameState(GameState gameState) {
		GameObject.gameState = gameState;
	}
	
	private String s_id, s_internalId;
	protected GameObjectPosition position;
	private float s_width = 0, s_height = 0;
	private boolean s_global = false;
	private boolean s_active = true;
	private Color s_color;
	private boolean s_shouldBeSaved = true;
	private Variables variables;
	private Array<Action> actions = new Array<Action>();
	private ObjectMap<String, Array<String>> forbiddenActions = new ObjectMap<String, Array<String>>();
	private String s_type;
	private int s_soundRadius;
	protected float s_projectileOriginXOffset, s_projectileOriginYOffset;
	
	private ObjectMap<Integer, Action> actionSlots = new ObjectMap<Integer, Action>();
	private ObjectMap<Class<? extends Action>, Action> actionCache = new ObjectMap<Class<? extends Action>, Action>();
	private GameMap map;
	private Vector2 cameraPosition;
	
	/**
	 * No-parameters constructor for game loading.
	 */
	public GameObject() {
		forbiddenActions = new ObjectMap<String, Array<String>>();
		position = new GameObjectPosition();
		variables = new Variables();
		cameraPosition = new Vector2();
		this.s_color = new Color(Color.WHITE);
	}
	
	public GameObject(String id, String type) {
		this();
		this.s_internalId = calculateInternalId(id, type);
		this.s_type = type;
		this.s_id = id;
		s_soundRadius = 10;
	}
	
	public GameObject(GameState gameState, String id, String type, Position position, int width, int height) {
		this(id, type);
		this.position.set(position);
		this.s_width = width;
		this.s_height = height;
	}
	
	protected String calculateInternalId(String id, String type) {
		return (type+"#"+this.getClass().getSimpleName()+gameState.getNextId()).toLowerCase(Locale.ENGLISH);
	}
	
	/**
	 * The unique identifier of the game object.
	 * 
	 * There should only ever be exactly one GO with this ID, 
	 * regardless of its type.
	 * 
	 * @return
	 */
	public String getInternalId() {
		return s_internalId;
	}
	
	/**
	 * Sets the user-defined identifier of the game object.
	 * 
	 * There is no guarantee that only one GO with this ID exists.
	 * 
	 * @return
	 */
	public void setId(String id) {
		s_id = id;
	}
	
	/**
	 * The user-defined identifier of the game object.
	 * 
	 * There is no guarantee that only one GO with this ID exists.
	 * 
	 * @return
	 */
	public String getId() {
		return s_id;
	}
	
	/**
	 * Returns the type of this object. This is the name
	 * of the "blueprint" from which this GO was created. It can
	 * be a class name, a XML file, or anything else that makes 
	 * sense in this context depending on the particular GO
	 * implementation.
	 * 
	 * @return
	 */
	public String getType() {
		return s_type;
	}
	
	/**
	 * Returns the human readable, localized name of this GO.
	 * 
	 * Default implementation just returns the ID, subclasses
	 * should implement their own logic.
	 * 
	 * @return
	 */
	public String getName() {
		return getInternalId();
	}
	
	/**
	 * This is called on every Global game object
	 * and on every local game object belonging to the active map
	 * every frame before they are rendered. 
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime) {
		
		// check for position changes in case they were triggered externally
		handlePositionChanges();
		
		if (!isActive()) {
			return;
		}
		
		for (int i = actions.size-1; i >=0 ; --i) {
			Action a = actions.get(i);
			if (!a.isPaused()) {
				a.update(deltaTime);
				if (a.isFinished()) {
					removeAction(a);
				}
			} else {
				// if we are paused but not currently assigned to a slot and the slot is empty, 
				// lets get in there and resume
				if (!actionSlots.containsKey(a.getActionSlot())) {
					actionSlots.put(a.getActionSlot(), a);
					a.resume();
				}
			}
		}
		
		// check for position changes again in case something was changed by one of our actions
		handlePositionChanges();
	}
	
	private void handlePositionChanges() {
		if (getMap() == null) {
			return;
		}
		if (position.hasChanged()) {
			changedPosition();
		}
		if (position.hasChangedTile()) {
			changedTile();
		}
	}
	
	/**
	 * This will remove this GO from everywhere and mark it as inactive and unsavable.
	 */
	public void remove() {
		setActive(false);
		setMap(null);
		setShouldBeSaved(false);
		gameState.removeGameObject(this);
		gameState.removeUnassignedGameObject(this);
	}
	
	/**
	 * Adds a new Action of the specified type and with the specified parameters
	 * to this GO.
	 * 
	 * The action will then be called every frame until it is finished. After
	 * being finished, it will be removed from this GO.
	 * 
	 * If there is another action in the slot used by the new action that is currently in progress,
	 * it will be removed before the new action replaces it.
	 * 
	 * @param actionClass
	 * @param parameters
	 * @return the added instance of the action
	 */
	public <T extends Action> T addAction(Class<T> actionClass, Object... parameters) {
		if (!canPerformAction(actionClass)) {
			return null;
		}
		T action = getActionInstance(actionClass);
		addAction(action, true, parameters);
		return action;
	}
	
	/**
	 * Gets the cost of performing the supplied action on the supplied target.
	 * @param action
	 * @param target
	 * @return
	 */
	public int getCostForAction(Class<? extends Action> action, Object target) {
		return 0;
	}
	
	/**
	 * Remove the supplied Action from this GO.
	 * 
	 * @param a
	 */
	public void removeAction(Action a) {
		if (a == null) {
			return;
		}
		a.onRemove(this);
		actions.removeValue(a, false);
		actionSlots.remove(a.getActionSlot());
		// put it into the cache so that it can be reused
		actionCache.put(a.getClass(), a);
	}
	
	/**
	 * Removes all actions from this GO.
	 */
	public void removeAllActions() {
		for (Action a : actions) {
			removeAction(a);
		}
	}
	
	public void removeAllVerbActions() {
		for (Action a : actions) {
			if (a.isVerbAction()) {
				removeAction(a);
			}
		}
	}
	
	public void pauseAllActions() {
		for (Action a : actions) {
			a.pause();
		}
	}
	
	public void resumeAllActions() {
		for (Action a : actions) {
			a.resume();
		}
	}
	
	protected boolean hasAnyBlockingAction() {
		for (Action a : actions) {
			if (a.isBlockingInCombat()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets an instance of an action with the supplied class that is currently active on this GO
	 * or null if no such action exists.
	 * 
	 * This DOES consider class inheritance.
	 * @param <T>
	 * 
	 * @param actionClass
	 * @return
	 */
	public <T extends Action> T getActiveAction(Class<T> actionClass) {
		return getActiveAction(actionClass, null);
	}
	
	/**
	 * Gets an instance of an action with the supplied class that is currently active on this GO
	 * or null if no such action exists.
	 * 
	 * This DOES consider class inheritance.
	 * 
	 * @param actionClass
	 * @param filter - if supplied, any actions that would be filtered by this filter will be ignored by this method
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Action> T getActiveAction(Class<T> actionClass, ActionFilter filter) {
		for (Action a : actions) {
			if (!a.isPaused() && !a.isFinished() && (filter == null || !filter.shouldFilter(a))) {
				if (actionClass.isAssignableFrom(a.getClass())) {
					return (T) a;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns true if this GO currently has any active (non-paused)
	 * action of any of the supplied types.
	 * 
	 * This DOES consider class inheritance.
	 * 
	 * @param actionClasses
	 * @param filter - if supplied, any actions that would be filtered by this filter will be ignored by this method
	 * @return
	 */
	public boolean hasActiveAction(Array<Class<? extends Action>> actionClasses) {
		return hasActiveAction(actionClasses, null);
	}
	
	/**
	 * Returns true if this GO currently has any active (non-paused)
	 * action of any of the supplied types.
	 * 
	 * This DOES consider class inheritance.
	 * 
	 * @param actionClasses
	 * @param filter - if supplied, any actions that would be filtered by this filter will be ignored by this method
	 * @return
	 */
	public boolean hasActiveAction(Array<Class<? extends Action>> actionClasses, ActionFilter filter) {
		for (Action a : actions) {
			if (!a.isPaused() && !a.isFinished() && (filter == null || !filter.shouldFilter(a))) {
				for (Class<? extends Action> actionClass : actionClasses) {
					if (actionClass.isAssignableFrom(a.getClass())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** 
	 * Adds the supplied action to this GameObject.
	 * @param a the action to add
	 * @param init whether the action should be initialized using this game object and the supplied parameters
	 * @param parameters the parameters to use when initializing the action, if init == true
	 */
	public void addAction(Action a, boolean init, Object... parameters) {
		if (init) {
			a.init(this, parameters);
		}
		
		if (a.getActionSlot() > -1) {
			Action existingAction = actionSlots.get(a.getActionSlot());
			if (existingAction != null) {
				removeAction(existingAction);
			}
		}
		actionSlots.put(a.getActionSlot(),a);
		actions.add(a);
	}
	
	private <T extends Action> T getActionInstance(Class<T> actionClass) {
		try {
			@SuppressWarnings("unchecked")
			T action = (T) actionCache.remove(actionClass);
			if (action == null) {
				action = ClassReflection.newInstance(actionClass);
			}		
			return (T) action;
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		} 
	}
	
	/**
	 * Returns true if the GO can perform the supplied action.
	 * 
	 * @param actionClass
	 * @return
	 */
	public boolean canPerformAction(Class<? extends Action> actionClass) {
		for (Array<String> forbidden : forbiddenActions.values()) {
			if (forbidden.contains(actionClass.getSimpleName(), false)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Forbids the GO from performing the supplied actions.
	 * <br />
	 * Note that you can forbid the same action multiple times. 
	 * <br />
	 * It will need to be allowed the same number of times before
	 * it can actually be used. This is a feature, no a bug. :)
	 * <br />
	 * This is used if for example different spell effects
	 * disable the character in similar way - all the effects
	 * that disable some action would need to expire before
	 * the actions can be used.
	 * 
	 * @param actionClasses
	 */
	@SuppressWarnings("unchecked")
	public void forbidActions(String forbidderId, Class<? extends Action>... actionClasses) {
		forbiddenActions.remove(forbidderId);
		Array<String> forbidden = new Array<String>();
		for (Class<? extends Action> actionClass : actionClasses) {
			forbidden.add(actionClass.getSimpleName());
		}
		forbiddenActions.put(forbidderId, forbidden);
	}
	
	/**
	 * Forbids the GO from performing the supplied actions
	 * as if they were forbidden by the supplied forbidder.
	 * <br />
	 * This is used if for example different spell effects
	 * disable the character in similar way - all the effects
	 * that disable some action would need to expire before
	 * the actions can be used.
	 * 
	 * @param actionClasses
	 */
	public void forbidActions(String forbidderId, Array<Class<? extends Action>> actionClasses) {
		forbiddenActions.remove(forbidderId);
		Array<String> forbidden = new Array<String>();
		for (Class<? extends Action> actionClass : actionClasses) {
			forbidden.add(actionClass.getSimpleName());
		}
		forbiddenActions.put(forbidderId, forbidden);
	}
	
	/**
	 * Allows the GO to perform the supplied actions that were forbidden by the forbidder.
	 * 
	 * It may still be unable to perform them in case some other forbidder also forbade them.
	 * @param actionClass
	 */
	public void allowActions(String forbidder, @SuppressWarnings("unchecked") Class<? extends Action>... actionClasses) {
		Array<String> forbidden = forbiddenActions.get(forbidder);
		if (forbidden != null) {
			for (Class<? extends Action> actionClass : actionClasses) {
				forbidden.removeValue(actionClass.getSimpleName(), false);
			}
		}
	}
	
	/**
	 * Allows the GO to perform the supplied actions that were forbidden by the forbidder.
	 * 
	 * It may still be unable to perform them in case some other forbidder also forbade them.
	 * @param actionClass
	 */
	public void allowActions(String forbidder, Array<Class<? extends Action>> actionClasses) {
		Array<String> forbidden = forbiddenActions.get(forbidder);
		if (forbidden != null) {
			for (Class<? extends Action> actionClass : actionClasses) {
				forbidden.removeValue(actionClass.getSimpleName(), false);
			}
		}
	}
	
	/**
	 * Allows the GO to perform all actions previously forbidden
	 * by the supplied forbidder
	 */
	public void allowAllActions(String forbidder) {
		forbiddenActions.remove(forbidder);
	}
	
	/**
	 * Allows the GO to perform all actions.
	 */
	public void allowAllActions() {
		forbiddenActions.clear();
	}
	
	/*
	 * Default implementation does nothing. Subclasses should
	 * override this if they want to support persistent effects.
	 * 
	 */
	@Override
	public void addPersistentEffect(EffectContainer container, Effect effect, float duration, GameObject user) {
	}
	
	/*
	 * Default implementation does nothing. Subclasses should
	 * override this if they want to support persistent effects.
	 * 
	 */
	@Override
	public void removePersitentEffect(String id) {
	}
	
	/*
	 * Default implementation returns an empty array. Subclasses should
	 * override this if they want to support persistent effects.
	 * 
	 */
	@Override
	public Array<PersistentEffect> getPersistentEffectsByType(String... types) {
		return new Array<PersistentEffect>();
	}
	
	/**
	 * Returns the x-coordinate of this GO in the camera 
	 * coordinate system.
	 * @return
	 */
	public float getXCamera() {
		return cameraPosition.x;
	}
	
	/**
	 * Returns the y-coordinate of this GO in the camera 
	 * coordinate system.
	 * @return
	 */
	public float getYCamera() {
		return cameraPosition.y;
	}
	
	/**
	 * Returns the offset from the current position of the GO
	 * where any projectile originating from the GO should
	 * start its path.
	 * 
	 * In tile coordinate system, based on orthogonal projection.
	 * 
	 * @return
	 */
	public float getProjectileOriginXOffset() {
		return s_projectileOriginXOffset;
	}
	
	/**
	 * Returns the offset from the current position of the GO
	 * where any projectile originating from the GO should
	 * start its path.
	 * 
	 * In tile coordinate system, based on orthogonal projection.
	 * 
	 * @return
	 */
	public float getProjectileOriginYOffset() {
		return s_projectileOriginYOffset;
	}
	
	public float getWidth() {
		return s_width;
	}
	public void setWidth(float width) {
		this.s_width = width;
	}
	
	public float getHeight() {
		return s_height;
	}
	
	public void setHeight(float height) {
		this.s_height = height;
	}
	
	
	/**
	 * Gets the current color of this GO.
	 * 
	 * This always returns the same instance that is used
	 * internally by the GO, so change the returned value 
	 * with caution.
	 */
	public Color getColor() {
		return  s_color;
	}
	
	/**
	 * Sets the color of this GO.
	 */
	public void setColor(Color newColor) {
		s_color = new Color(newColor);
	}
	
	/**
	 * Returns the map this object belongs to.
	 * 
	 * @return
	 */
	public GameMap getMap() {
		return map;
	}
	
	/**
	 * Moves the GO to the supplied map.
	 * 
	 * This will remove it from the map it was on before.
	 * 
	 * @param map
	 */
	public void setMap(GameMap map) {
		if (this.map != null) {
			if (this.map.equals(map)) {
				return;
			}
			this.map.removeGameObject(this);
		} 
		if(map != null) {
			if (this.map == null) {
				gameState.removeUnassignedGameObject(this);
			}
			map.addGameObject(this);
		} else {
			gameState.addUnassignedGameObject(this);
		}
		this.map = map;
		position.markAsChanged();
	}
	
	/**
	 * Returns true if the GO contains the supplied coordinates
	 * (in map coordinate system).
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(float x, float y) {
		float posX = position.getX();
		float posY = position.getY();
		return posX <= x && posX+getWidth() > x && posY <= y && posY+getHeight()> y; 
	}
	
	/**
	 * Whether or not this GO should be saved when the game is saved.
	 * 
	 * @return
	 */
	public boolean shouldBeSaved() {
		return s_shouldBeSaved;
	}
	
	/**
	 * Sets whether or not this GO should be saved when the game is saved.
	 * Use with caution.
	 * 
	 * @param shouldBeSaved
	 */
	public void setShouldBeSaved(boolean shouldBeSaved) {
		this.s_shouldBeSaved = shouldBeSaved;
	}
	
	public boolean isGlobal() {
		return s_global;
	}

	public void setGlobal(boolean isGlobal) {
		this.s_global = isGlobal;
		if (isGlobal) {
			gameState.addGameObject(this);
		} else {
			gameState.removeGameObject(this);
		}
	}
	
	/**
	 * This is called whenever a GameMap containing this GO
	 * is undisposed. It should contain logic that
	 * recreates any assets that became invalid when 
	 * the map was disposed.
	 */
	public void undispose() {
		
	}
	
	/**
	 * The default implementation for GOs
	 * always returns false.
	 */
	@Override
	public boolean alreadyVisited() {
		return false;
	}

	/**
	 * Returns true if this GO is currently visible by any player character.
	 * 
	 * @return
	 */
	public boolean isVisibleToPC() {
		return getMap() == null ? false : getMap().isTileVisibleToPC(position.tile());
	}
	
	@Override
	public float getSoundRadius() {
		return s_soundRadius;
	}
	
	@Override
	public Vector2 getSoundOrigin() {
		return new Vector2(position.getX(),position.getY());
	}
	
	@Override
	public float getDistanceToPlayer() {
		Vector2 origin = getSoundOrigin();
		return GameState.getPlayerCharacterGroup().getDistanceToTheNearestMember(origin.x, origin.y, getMap());
	}
	
	@Override
	public boolean shouldModifyVolume() {
		return true;
	}

	@Override
	public float getTargetX() {
		return position.getX();
	}

	@Override
	public float getTargetY() {
		return position.getY();
	}	
	
	@Override
	public GameObject[] getGameObjects() {
		return new GameObject[]{this};
	}
	
	@Override
	public boolean filterUnviableTargets(Effect effect,
			EffectContainer effectContainer, GameObject user) {
		return true;
	}
	
	@Override
	public String toString() {
		return getInternalId();
	}

	/**
	 * Returns true if this game object is considered active. Inactive game objects
	 * do not update their actions and do not handle position changes.
	 * @return
	 */
	public boolean isActive() {
		return s_active;
	}
	
	/**
	 * Returns true if this GO can be safely removed from a game map
	 * on a map change.
	 * @return
	 */
	public boolean isMapRemovable() {
		return !isActive();
	}

	public void setActive(boolean active) {
		s_active = active;
	}
	
	@Override
	public float getSize() {
		return getWidth() > getHeight() ? getWidth() : getHeight();
	}
	
	protected void changedPosition() {
		position.resetChanged();
		if (getMap() != null) {
			cameraPosition.set(position.getX(), position.getY());
			getMap().projectFromTiles(cameraPosition);
		}
	}
	
	/**
	 * Fired whenever the GO changes a tile.
	 */
	protected void changedTile() {
		if (getMap() != null) {
			getMap().notifyGOTileChanged(this, position.tile().getX(), position.tile().getY(),
					position.prevTile().getX(), position.prevTile().getY());
		}
		position.resetChangedTile();
	}
	
	public GameObjectPosition position() {
		return position;
	}
	
	@Override
	public Variables variables() {
		return variables;
	}
	
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root.getChildByName(XMLUtil.XML_PROPERTIES));
		variables.loadFromXML(root);
		position.loadFromXML(root);
		
		XMLUtil.readActions(this, root.getChildByName(XMLUtil.XML_ACTIONS));
		
		Element forbiddenActionsElement = root.getChildByName(XML_FORBIDDEN_ACTIONS);
		if (forbiddenActionsElement != null) {
			for (int i = 0; i < forbiddenActionsElement.getChildCount(); ++i) {
				Element forbiddenByElement = forbiddenActionsElement.getChild(i);
				String forbiddenBy = forbiddenByElement.getAttribute(XMLUtil.XML_ATTRIBUTE_ID);
				Array<String> forbidden = new Array<String>();
				for (int j = 0; j < forbiddenByElement.getChildCount(); ++j) {
					forbidden.add(StringUtil.capitalizeFirstLetter(forbiddenByElement.getChild(j).getName()));
				}
				forbiddenActions.put(forbiddenBy, forbidden);
			}
		}
		
		if (isGlobal()) {
			gameState.addGameObject(this);
		}
		if (map == null) {
			gameState.addUnassignedGameObject(this);
		}
	}
	
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XMLUtil.XML_PROPERTIES);
		XMLUtil.writePrimitives(this, writer);
		writer.pop();
		
		position.writeToXML(writer);
		variables.writeToXML(writer);
		
		if (actions.size > 0) {
			writer.element(XMLUtil.XML_ACTIONS);
			for (Action action : actions) {
				action.writeToXML(writer);
			}
			writer.pop();
		}
		
		if (forbiddenActions.size > 0) {
			writer.element(XML_FORBIDDEN_ACTIONS);
			for (String forbiddenBy : forbiddenActions.keys()) {
				writer.element(XML_FORBIDDEN_BY);
				writer.attribute(XMLUtil.XML_ATTRIBUTE_ID, forbiddenBy);
				for (String actionClass : forbiddenActions.get(forbiddenBy)) {
					writer.element(actionClass).pop();
				}
				writer.pop();
			}
			writer.pop();
		}
	}
}
