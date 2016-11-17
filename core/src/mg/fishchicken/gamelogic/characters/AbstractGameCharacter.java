package mg.fishchicken.gamelogic.characters;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import box2dLight.Light;
import box2dLight.ViewConeLight;
import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.EmptyTrack;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.OrientedThing;
import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.util.ColorUtil;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.Pair;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.FadeAction;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroupGameObject;
import mg.fishchicken.gamelogic.characters.los.CircularLineOfSight;
import mg.fishchicken.gamelogic.characters.los.LineOfSight;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.factions.FactionContainer;
import mg.fishchicken.gamelogic.locations.CombatGameMap.CombatMapInitializationData;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locations.transitions.Transition;
import mg.fishchicken.gamelogic.time.GameCalendarDate;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.graphics.animations.AnimatedGameObject;
import mg.fishchicken.graphics.animations.Animation;
import mg.fishchicken.graphics.animations.AttackAnimation;
import mg.fishchicken.graphics.animations.CharacterAnimationMap;
import mg.fishchicken.graphics.lights.GamePointLight;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.graphics.models.CharacterModel;
import mg.fishchicken.graphics.renderers.GameMapRenderer;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.Path.Step;

public abstract class AbstractGameCharacter extends AnimatedGameObject implements AssetContainer, FactionContainer,
		OrientedThing {

	public static final String STRING_TABLE = "character."+Strings.RESOURCE_FILE_EXTENSION;
	
	public static final class State {
		public static final String IDLE = "idle"; 
		public static final String WALK = "walk"; 
		public static final String USE = "use";
		public static final String ATTACKMELEE = "attackMelee"; 
		public static final String ATTACKRANGED = "attackRanged";
		public static final String ONHIT = "onHit";
		public static final String CAST = "cast";
		public static final String SLEEP = "sleep";
		public static final String DEATH = "death";
		public static final String SNEAKING = "sneaking";
		public static final String SNEAKINGIDLE = "sneakingIdle";
		
		private State() {
		}
	}
 
	private static final int VISIBLE_TILES_LOOKUP_RESET = 9999;
	private static final Orientation DEFAULT_ORIENTATION = Orientation.RIGHT;
	
	// state properties
	protected Orientation s_orientation = DEFAULT_ORIENTATION;
	protected String s_state = State.IDLE;
	private CharacterModel s_model;
	private AudioProfile s_audioProfile;
	private float s_stateChangeDelay;
	private String s_newState;
	private String s_name;
	private String s_description;
	private Faction s_faction;
	private boolean s_noCircle;
	private boolean s_sightDisabled;
	private ObjectMap<Faction, Pair<GameCalendarDate, Integer>> temporaryHostility;
	private Set<String> visitedLocations = new  HashSet<String>(); // list of locations the char visited during his life
	private ObjectSet<GameLocation> currentLocations = new ObjectSet<GameLocation>(); // list of locations the char is currently at
	private ObjectSet<GameLocation> newCurrentLocations = new ObjectSet<GameLocation>();
	
	// non state properties
	protected CharacterAnimationMap animations;
	private Brain brain;
	private LineOfSight lineOfSight;
	private ViewConeLight viewCone;
	private int[] visibleTilesLookup;
	private int visibleTilesLookupCounter;
	private PositionArray visibleArea;
	private PositionArray viewConeArea;
	private CharacterCircle characterCircle, destinationIndicator;
	private Array<LightDescriptor> lightDescriptors;
	private ObjectMap<LightDescriptor, Light> lights;
	private Tile lastKnownEnemyPosition;
	protected ObjectSet<AbstractGameCharacter> tempSet;
	private Position tempPosition;
	private boolean needOffsetRecalculation;
	private boolean switchedMap;
	
	/**
	 * Empty constructor for game loading.
	 */
	public AbstractGameCharacter() {
		super();
		init();
	}
	
	public AbstractGameCharacter(String id,String type) {
		super(id, type);
		init();
	}
	
	private void init() {
		// width and height of characters is always one, since they occupy only one tile
		setWidth(1);
		setHeight(1);
		temporaryHostility = new ObjectMap<Faction, Pair<GameCalendarDate, Integer>>();
		s_stateChangeDelay = -1;
		s_newState = null;
		s_noCircle = false;
		s_sightDisabled = false;
		visibleTilesLookupCounter = 1;
		lights = new ObjectMap<LightDescriptor, Light>();
		lightDescriptors = new Array<LightDescriptor>();
		characterCircle = new CharacterCircle(this);
		destinationIndicator = new CharacterCircle(this);
		destinationIndicator.addAction(FadeAction.class, 0.5f, true, 0f, 0.5f);
		visibleArea = new PositionArray();
		viewConeArea = new PositionArray();
		setPlayingAnimation(true);
		tempSet = new ObjectSet<AbstractGameCharacter>();
		tempPosition = new Position();
		brain = createBrain();
	}
	
	protected Brain createBrain() {
		return new Brain(this);
	}
	
	@Override
	public void draw(GameMapRenderer renderer, float deltaTime) {
		if (characterCircle != null && isActive() && !s_noCircle) {
			characterCircle.draw(renderer.getSpriteBatch(), deltaTime);
		}
		if (destinationIndicator != null && shouldRenderDestination()) {
			MoveToAction moveAction = getActiveAction(MoveToAction.class);
			Path currentPath = moveAction != null ? moveAction.getCurrentPath() : null;
			if (currentPath != null && currentPath.getLength() > 0) {
				Step step = currentPath.getLastStep();
				tempPosition.set(step.getX(), step.getY());
				destinationIndicator.setPosition(tempPosition);
				destinationIndicator.draw(renderer.getSpriteBatch(), deltaTime);
			}
		}
		super.draw(renderer, deltaTime);
	}
	
	public abstract boolean shouldRenderDestination();

	public void resetCharacterCircleColor() {
		Color color = ColorUtil.WHITE_FIFTY;
		if (belongsToPlayerFaction()) {
			color = ColorUtil.GREEN_FIFTY;
		} else {
			if (isHostileTowardsPlayer()) {
				color = ColorUtil.RED_FIFTY;
			} 
		}
		characterCircle.setColor(color);
		destinationIndicator.setColor(color);
	}
	
	@Override
	public void addAction(Action a, boolean init, Object... parameters) {
		super.addAction(a, init, parameters);
		if (a instanceof MoveToAction) {
			// this will make the indicator fade in nicely
			destinationIndicator.getColor().a = 0.5f;
		}
	}
	
	/**
	 * Whether or not this character is asleep.
	 */
	public abstract boolean isAsleep();
	
	/**
	 * Returns true if this character is invisible.
	 * 
	 * @return
	 */
	public boolean isInvisible() {
		return false;
	}

	/**
	 * Returns true if this character is sneaking.
	 * 
	 * @return
	 */
	public boolean isSneaking() {
		return false;
	}
	
	/**
	 * Returns true if this character is detecting traps.
	 * 
	 * @return
	 */
	public boolean isDetectingTraps() {
		return false;
	}
	
	@Override
	public boolean isAlwaysBehind() {
		return !isActive();
	}
	
	public void addVisitedLocation(String locationId) {
		visitedLocations.add(locationId);
	}
	
	/**
	 * Returns true if this character ever visited a GameLocation with the supplied ID.
	 * 
	 * Please note this works with IDs and not internal IDs, so it should only be used
	 * with locations where the ID is guaranteed to be unique.
	 * 
	 * @param locationId
	 * @return
	 */
	public boolean visitedLocation(String locationId) {
		return visitedLocations.contains(locationId);
	}
	
	@Override
	public void update(float deltaTime) {
		if (switchedMap) {
			switchedMap = false;
			GameMap map = getMap();
			map.onEntry(this);
			addVisitedLocation(map.getId());
		}
		if (needOffsetRecalculation) {
			recalculateOffsets();
		}
		super.update(deltaTime);
		
		if (isActive()) {
			if (s_newState != null) {
				s_stateChangeDelay -= deltaTime;
				if (s_stateChangeDelay <= 0) {
					setState(s_newState);
					s_newState = null;
					s_stateChangeDelay = -1;
				}
			}
			
			
			brain.update(deltaTime);
			updateTemporaryHostility();
		}
		
		Color newViewConeColor = getViewConeColor();
		if (viewCone != null && !newViewConeColor.equals(viewCone.getColor())) {
			viewCone.setColor(newViewConeColor);
		}
	}
	
	@Override
	public Faction getFaction() {
		if (s_faction == null) {
			return Faction.NO_FACTION;
		}
		return s_faction;
	}

	public void setFaction(Faction faction) {
		if (s_faction != null) {
			s_faction.removeMember(this);
		}
		faction.addMember(this);
		this.s_faction = faction;
	}
	
	public boolean belongsToPlayerFaction() {
		// identity check OK here since this is a singleton
		return Faction.PLAYER_FACTION == getFaction();
	}
	
	/**
	 * Returns true if this character can talk to the supplied character.
	 * 
	 * @param talker
	 * @return
	 */
	public boolean canTalkTo(GameObject talker) {
		if (talker instanceof AbstractGameCharacter) {
			AbstractGameCharacter cgo = (AbstractGameCharacter) talker;
			if (cgo.belongsToPlayerFaction() && belongsToPlayerFaction()) {
				return false;
			}
			return !Faction.areHostile(this, cgo);
		}
		return true;
	}
	
	/**
	 * Returns true if this character can trade with the supplied character.
	 * @param character
	 * @return
	 */
	public boolean canTradeWith(GameCharacter character) {
		return canTradeWith(character.getFaction());
	}
	
	/**
	 * Returns true if this character can trade with members of the
	 * supplied faction.
	 * 
	 * @param faction
	 * @return
	 */
	public boolean canTradeWith(Faction faction) {
		return getFaction().getDispositionTowards(faction) > -50;
	}
	
	/**
	 * Returns the ID of the dialogue assigned to this character.
	 */
	public abstract String getDialogueId();
	
	/**
	 * Returns the GameCharacter that will represent this AbstractGameCharacter
	 * in dialogues or other actions where a single GameCharacter is required. 
	 * This is usually the character itself, but can be a leader of
	 * a group for example if the AbstractGameCharacter represents a group.
	 */
	public abstract GameCharacter getRepresentative();
	
	/**
	 * Makes this character temporarily hostile to the supplied faction for the supplied
	 * number of game time hours.
	 * 
	 * If the character already is temporarily hostile towards the faction, this will
	 * reset the timer. If the character is hostile to the faction due to disposition,
	 * this will have no effect. 
	 * 
	 * @param faction
	 * @param timeToStayHostile - number of hours to stay hostile
	 */
	public void makeTemporarilyHostileTowards(Faction faction, int timeToStayHostile) {
		if (!getFaction().isHostileTowards(faction)) {
			temporaryHostility.put(faction, new Pair<GameCalendarDate, Integer>(new GameCalendarDate(GameState.getCurrentGameDate()), timeToStayHostile));
		}
		resetCharacterCircleColor();
	}
	
	/**
	 * Returns true if this character is currently hostile towards the player.
	 * @return
	 */
	public boolean isHostileTowardsPlayer() {
		return isHostileTowards(Faction.PLAYER_FACTION);
	}
	
	/**
	 * Returns true if this character is currently hostile towards the supplied faction.
	 * @param faction
	 * @return
	 */
	@Override
	public boolean isHostileTowards(Faction faction) {
		return getFaction().isHostileTowards(faction) || temporaryHostility.containsKey(faction);
	}
	
	private void updateTemporaryHostility() {
		// only update this outside of combat
		if (gameState.getCurrentMap() == null || GameState.isCombatInProgress()) {
			return;
		}
		Iterator<Entry<Faction, Pair<GameCalendarDate,Integer>>> iterator = temporaryHostility.entries().iterator();
		GameCalendarDate currDate = GameState.getCurrentGameDate();
		boolean changed = false;
		while (iterator.hasNext()) {
			Entry<Faction, Pair<GameCalendarDate,Integer>> entry = iterator.next();
			GameCalendarDate startDate = new GameCalendarDate(entry.value.getLeft());
			Integer durationHours = entry.value.getRight();
			
			startDate.addToHour(durationHours);
			if (startDate.compareTo(currDate) < 0) {
				iterator.remove();
				changed = true;
			}
		}
		if (changed) {
			resetCharacterCircleColor();
		}
	}
	
	/**
	 * Returns true if the character sees any characters 
	 * that it is hostile towards.
	 * 
	 * Invisible characters do not start combat.
	 * 
	 * @return
	 */
	public boolean shouldStartCombat() {
		// super quick checks
		if (getMap() == null || getMap().isWorldMap() || GameState.isCombatInProgress()) {
			return false; 
		}
		
		boolean inPlayerFaction = belongsToPlayerFaction();
		
		if (!inPlayerFaction && !isHostileTowardsPlayer()) {
			return false;
		}
		
		// slower check
		return hasEnemiesInSight(inPlayerFaction);
	}
	
	private CharacterGroupGameObject shouldSwitchToCombatMap() {
		if (getMap() == null || !getMap().isWorldMap()) {
			return null;
		}
		
		ObjectSet<GameObject> gosOnMyTile = getMap().getAllGameObjectsAt(position.getX(), position.getY(), true, CharacterGroupGameObject.class);
		for (GameObject go : gosOnMyTile) {
			if (Faction.areHostile(this, (CharacterGroupGameObject)go) && (this.belongsToPlayerFaction() || ((CharacterGroupGameObject)go).belongsToPlayerFaction())) {
				return (CharacterGroupGameObject)go;
			}
		}
		return null;
	}
	
	public Tile getLastKnownEnemyPosition() {
		return lastKnownEnemyPosition;
	}
	
	private void setLastKnownEnemyPosition(int  x, int y) {
		if (lastKnownEnemyPosition == null) {
			lastKnownEnemyPosition = new Tile();
		}
		lastKnownEnemyPosition.set(x, y);
	}
	
	protected void clearLastKnownEnemyPosition() {
		lastKnownEnemyPosition = null;
	}
	
	@Override
	public boolean isVisibleToPC() {
		if (isActive() && belongsToPlayerFaction()) {
			return true;
		}
		return super.isVisibleToPC();
	}	
	
	/**
	 * Returns the nearest enemy in sight.
	 * @return
	 */
	public AbstractGameCharacter getNearestEnemyInSight() {
		tempSet.clear();
		getAllEnemiesInSight(tempSet);
		AbstractGameCharacter nearest = null;
		float minDistance = 0;
		Vector2 enemyPosition = MathUtil.getVector2();
		Vector2 myPosition = MathUtil.getVector2().set(position.getX(), position.getY());
		for (AbstractGameCharacter enemy : tempSet) {
			float distance = enemyPosition.set(enemy.position.getX(), enemy.position.getY()).dst(myPosition);
			if (nearest == null) {
				nearest = enemy;
				minDistance = distance;
			} else {
				if (distance < minDistance) {
					minDistance = distance;
					nearest = enemy;
				}
			}
		}
		return nearest;
	}
	
	/**
	 * Returns an array of all characters in sight radius that fulfill the supplied filters requirements.
	 * 
	 * @param filters - an array of filters that the characters must fulfill. Can be empty.
	 * @return
	 */
	public boolean getAllCharactersInSightRadius(ObjectSet<AbstractGameCharacter> returnValue, CharacterFilter... filters) {
		return getAllCharactersInArea(returnValue, getVisibleArea(), filters);
	}
	
	/**
	 * Returns an array of all characters in the view cone that fulfill the supplied filters requirements.
	 * 
	 * @param filters - an array of filters that the characters must fulfill. Can be empty.
	 * @return
	 */
	public boolean getAllCharactersInViewCone(ObjectSet<AbstractGameCharacter> returnValue, CharacterFilter... filters) {
		return getAllCharactersInArea(returnValue, getViewConeArea(), filters);
	}
	
	/**
	 * Returns an array of all characters in the supplied area that fulfill the supplied filters requirements.
	 * 
	 * @param returnValue - the set into which to store the result. Can be null. In that case, only true / false will be returned
	 * indicating whether any characters were found
	 * @param filters - an array of filters that the characters must fulfill. Can be empty.
	 * @return true if any characters were found, false otherwise
	 */
	private boolean getAllCharactersInArea(ObjectSet<AbstractGameCharacter> returnValue, PositionArray area, CharacterFilter... filters) {
		if (area == null) {
			return false;
		}
		if (returnValue == null) {
			tempSet.clear();
		}
		
		ObjectSet<AbstractGameCharacter> setToUse = returnValue != null ? returnValue : tempSet; 
		
		if (!getMap().getAllObjectsInArea(setToUse, area, AbstractGameCharacter.class)) {
			return false;
		}
		
		setToUse.remove(this);
		
		if (filters.length > 0) {
			Iterator<AbstractGameCharacter> iterator = setToUse.iterator();
			while (iterator.hasNext()) {
				AbstractGameCharacter go = iterator.next();
				for (CharacterFilter filter : filters) {
					if (filter.shouldFilter(this, go)) {
						iterator.remove();
						break;
					}
				}
			}
		}
		
		return setToUse.size > 0;
	}
	
	/**
	 * Returns an array of all visible characters in sight.
	 * 
	 * @return
	 * @see AbstractGameCharacter#getAllCharactersInSightRadius(CharacterFilter)
	 */
	public boolean getAllCharactersInSight(ObjectSet<AbstractGameCharacter> returnValue) {
		return getAllCharactersInSightRadius(returnValue, CharacterFilter.VISIBLE);
	}
	
	/**
	 * Returns an array of all visible, allied characters in sight.
	 * 
	 * @return
	 * @see AbstractGameCharacter#getAllCharactersInSightRadius(CharacterFilter)
	 */
	public boolean getAllAlliesInSight(ObjectSet<AbstractGameCharacter> returnValue) {
		return getAllCharactersInSightRadius(returnValue, CharacterFilter.ALLIED, CharacterFilter.VISIBLE);
	}
	
	public boolean hasEnemiesInSight() {
		return hasEnemiesInSight(false);
	}
	
	public boolean hasEnemiesInSight(boolean ignoreSleeping) {
		return ignoreSleeping ? 
				getAllCharactersInSightRadius(null, CharacterFilter.HOSTILE, CharacterFilter.VISIBLE, CharacterFilter.AWAKE) :
				getAllEnemiesInSight(null);
	}
	
	/**
	 * Returns an array of all visible enemies in sight. Enemies are characters
	 * that are hostile towards this character, or that this character
	 * is hostile towards.
	 * 
	 * @return
	 * @see AbstractGameCharacter#getAllCharactersInSightRadius(CharacterFilter)
	 */
	public boolean getAllEnemiesInSight(ObjectSet<AbstractGameCharacter> returnValue) {
		return getAllCharactersInSightRadius(returnValue, CharacterFilter.HOSTILE, CharacterFilter.VISIBLE);
	}
	
	/**
	 * Lets all enemies in sight of this character know the position
	 * of this character.
	 * 
	 */
	public void broadcastMyPositionToEnemiesInSight() {
		tempSet.clear();
		getAllEnemiesInSight(tempSet);
		for (AbstractGameCharacter character : tempSet) {
			character.setLastKnownEnemyPosition((int)position.getX(), (int)position.getY());
		}
	}	
	
	/**
	 * Lets all allies visible to the character 
	 * know that there is an enemy nearby and where exactly he is.
	 * 
	 */
	public void alertOthersToEnemyPresence() {
		AbstractGameCharacter enemy = getNearestEnemyInSight();
		if (enemy == null && lastKnownEnemyPosition == null) {
			return;
		}
		Tile enemyPosition = enemy != null ? enemy.position().tile() : lastKnownEnemyPosition;
		ObjectSet<AbstractGameCharacter> allies = new ObjectSet<AbstractGameCharacter>();
		getAllAlliesInSight(allies);
		for (AbstractGameCharacter ally : allies) {
			if (!ally.isAwareOfEnemyPosition()) {
				ally.setLastKnownEnemyPosition(enemyPosition.getX(), enemyPosition.getY());
			}
		}
	}
	
	/**
	 * Returns true if this character either sees directly, 
	 * or remembers a position of any enemy.
	 * 
	 * @return
	 */
	public boolean isAwareOfEnemyPosition() {
		if (lastKnownEnemyPosition != null) {
			return true;
		}
		
		return hasEnemiesInSight();
	}
	
	private void createLights(GameMap map) {
		if (map == null || map.isDisposed())  {
			return;
		}
		lineOfSight = createLineOfSight(map);
		updateSightPosition(map, lineOfSight);
		viewCone = createViewCone(map);
		updateLightPosition(map, viewCone);
		
		lights.clear();
		for (LightDescriptor lightDescriptor : lightDescriptors) {
			createNonCastingLight(map, lightDescriptor);
		}		
	}
	
	protected ViewConeLight createViewCone(GameMap map) {
		ViewConeLight viewCone = null;
		if (!map.isWorldMap() && !s_sightDisabled) {
			viewCone = new ViewConeLight(this, map, 30, getViewConeColor(), Configuration.getSightRadiusLocal() / 2f, 45);
		}
		return viewCone;
	}
	
	protected Color getViewConeColor() {
		return Color.WHITE;
	}
	
	protected LineOfSight createLineOfSight(GameMap map) {
		if (s_sightDisabled) {
			return null;
		}
		LineOfSight los = new CircularLineOfSight(map
				.getFogOfWarRayHandler(), 100,
				map.isWorldMap() ? Configuration.getSightRadiusWorld()
						: Configuration.getSightRadiusLocal(), position.getX(),
						position.getY(), map);
		return los;
	}
	
	/**
	 * Returns a line of sight of this character. Can be null if the character has LOS disabled.
	 * @return
	 */
	public LineOfSight getLineOfSight() {
		return lineOfSight;
	}
	
	public void addLight(LightDescriptor light) {
		if (light != null) {
			lightDescriptors.add(light);
			if (getMap() != null) {
				createNonCastingLight(getMap(), light);
			}
		}
	}
	
	public void removeLight(LightDescriptor light) {
		if (light != null) {
			lightDescriptors.removeValue(light, false);
			if (getMap() != null && lights.get(light) != null) {
				lights.get(light).remove();
				lights.remove(light);
			}
		}
	}
	
	protected void createNonCastingLight(GameMap map, LightDescriptor lightDescriptor) {
		GamePointLight newLight = new GamePointLight(map.getLightsRayHandler(), 100,lightDescriptor.lightColor, lightDescriptor.lightRadius, this);
		newLight.setStaticLight(true);
		newLight.setXray(true);
		newLight.setSoft(true);
		newLight.setSoftnessLenght(3);
		newLight.setId("characterLight"+getInternalId());
		lights.put(lightDescriptor, newLight);
		updateLightPosition(map, newLight);
	}
	
	public void updateLightsPosition(boolean updateLineOfSight) {
		for (Light light : lights.values()) {
			updateLightPosition(getMap(), light);
		}
		if (lineOfSight != null && updateLineOfSight) {
			updateSightPosition(getMap(), lineOfSight);
		}
		if (viewCone != null) {
			updateLightPosition(getMap(), viewCone);
			
		}
	}
	
	private void updateSightPosition(GameMap map, LineOfSight sight) {
		if (sight == null) {
			return;
		}
		Vector2 tempVector = MathUtil.getVector2().set(position.tile().getX() + 0.5f, position.tile().getY() + 0.5f);
		map.projectFromTiles(tempVector);
		sight.setPosition(tempVector.x, tempVector.y);
		MathUtil.freeVector2(tempVector);
	}
	
	private void updateLightPosition(GameMap map, Light light) {
		// only do this if we belong to a non disposed map
		// otherwise the lights will be disposed as well
		if (light != null && !map.isDisposed()) {
			Vector2 tempVector = MathUtil.getVector2().set(position.getX()+0.5f, position.getY()+0.5f);
			map.projectFromTiles(tempVector);
			light.setPosition(tempVector.x, tempVector.y);
			MathUtil.freeVector2(tempVector);
		}
	}
	
	/**
	 * Sets the orientation (facing) of this character.
	 * If null is supplied, a default orientation is assumed.
	 */
	public void setOrientation(Orientation o) {
		if (o == null) {
			o = DEFAULT_ORIENTATION;
		}
		if (o != s_orientation) {
			s_orientation = o;
			float stateTime = 0;
			if (State.WALK == getState()) {
				stateTime = getAnimationStateTime();
			}
			if (animations != null) {
				setAnimation(animations.getAnimation(s_state, o, getMap() != null && !getMap().isDisposed() && getMap().isMapLoaded()), stateTime);
			}
			if (viewCone != null) {
				viewCone.setOrientation(o);
			}
		}
	}
	
	public Orientation getOrientation() {
		return s_orientation;
	}
	
	/**
	 * Number of tiles traveled per second.
	 * @return
	 */
	public abstract float getSpeed();
	
	public String getState() {
		return s_state;
	}
		
	/**
	 * Sets the animation state of this character to the supplied value.
	 * 
	 * Does nothing if the character is inactive (dead) or asleep.
	 * 
	 * @param s
	 */
	public void setState(String s) {
		setState(s, 0);
	}
	
	/**
	 * Sets the animation state of this character to the supplied value
	 * after the supplied number of seconds has elapsed.
	 * 
	 * Does nothing if the character is inactive (dead), asleep, not on the current map, or already in the supplied state.
	 * 
	 * @param state
	 * @param delay
	 */
	public void setState(String state, float delay) {
		if (!isActive()) {
			return;
		}
		if (delay == 0) {
			if (!state.equals(s_state) && isActive()) {
				s_state = state;
				if (animations != null) {
					setAnimation(animations.getAnimation(s_state, s_orientation, getMap() != null && !getMap().isDisposed() && getMap().isMapLoaded()));
				}
			}
		} else {
			s_newState = state;
			s_stateChangeDelay = delay;
		}
	}
	
	@Override
	public void removeAllActions() {
		super.removeAllActions();
		brain.removeActions();
	}
	
	@Override
	public void removeAction(Action a) {
		brain.removeAction(a);
		super.removeAction(a);
	}
	
	public float getCircleOffsetX() {
		if (getMap().isIsometric()) {
			return getMap().getTileSizeX()/2;
		}
		return 0;
	}
	
	public float getCircleOffsetY() {
		if (getMap().isIsometric()) {
			return -getMap().getTileSizeY()/2+5;
		}
		return 5;
	}
	
	public CharacterCircle getCharacterCircle() {
		return characterCircle;
	}
	
	@Override
	public Animation getAnimation() {
		if (super.getAnimation() == null && animations != null) {
			Animation animation = animations.getAnimation(s_state, s_orientation, !getMap().isDisposed());
			if (animation == null) {
				Log.log("Could not find animation for character {0} and state {1}, falling back to Idle.", LogType.ERROR, this.getInternalId(), s_state);
				animation = animations.getAnimation(State.IDLE, s_orientation, !getMap().isDisposed());
			}
			setAnimation(animation, getAnimationStateTime());
		}
		return super.getAnimation();
	}
	
	/**
	 * Gets the audio track with the supplied id from the
	 * audio profile of this character, or an EmptyTrack
	 * if no such track exists or if the character does not have an audio profile.
	 * @param id
	 * @return
	 */
	public AudioTrack<?> getTrack(String id) {
		return s_audioProfile != null ? s_audioProfile.getTrack(id) : EmptyTrack.INSTANCE;
	}
	
	public AudioProfile getAudioProfile() {
		return s_audioProfile;
	}
	
	public void setModel(CharacterModel model, AudioProfile profile) throws IOException {
		s_model = model;
		s_audioProfile = profile;
		animations = new CharacterAnimationMap(s_model, s_audioProfile, getSpeed());
		setAnimation(null);
		recalculateOffsets();
	}

	public CharacterModel getModel() {
		return s_model;
	}
	
	@Override
	protected void changedPosition() {
		super.changedPosition();
		if (belongsToPlayerFaction()) {
			handleTransitions();
		}
		updateLightsPosition(false);
	}
	
	@Override
	protected void changedTile() {
		super.changedTile();
		updateLightsPosition(true);
		updateVisibleArea();
		
		if (shouldStartCombat()) {
			gameState.startCombat();
		}
		
		GameMap map = getMap();
		
		CharacterGroupGameObject enemy = shouldSwitchToCombatMap();
		if (enemy != null) {
			CombatMapInitializationData combatMapData = new CombatMapInitializationData(map.getId(), enemy.getGroup(), position.tile(), position.prevTile());
			removeAllVerbActions();
			gameState.switchToCombatMap(combatMapData);
			return;
		}
		
		handleStealth();
		updateLocations();
	}
	
	private void handleTransitions() {
		GameMap map = getMap();
		Tile currTile = position.tile();
		
		if (!position.equals(currTile.getX(), currTile.getY())) {
			return;
		}
		Transition transition = map.getTransitionAt(currTile.getX(), currTile.getY());
		if (transition != null) {
			// for combat maps, all group members must be in the same transition in order to be able to leave
			if (map.isCombatMap()) {
				Array<GameCharacter> allPCs = GameState.getPlayerCharacterGroup().getPlayerCharacters();
				for (GameCharacter pc : allPCs) {
					if (!transition.equals(map.getTransitionAt(pc.position().tile()))) {
						Log.logLocalized("cannotLeaveAlone", LogType.COMBAT);
						return;
					}
				}
			}
			if (GameState.isCombatInProgress() && !map.isCombatMap()) {
				Log.logLocalized("cannotLeave", LogType.COMBAT);
			} else {
				gameState.switchToMap(transition.getTargetMap(), new Tile(transition.getTargetX(), transition.getTargetY()));
			}
		}
	}
	
	/**
	 * Returns a set of locations this character is currently at.
	 * Modifying the returned list is unsafe.
	 * @return
	 */
	public ObjectSet<GameLocation> getCurrentLocations() {
		return currentLocations;
	}
	
	private void updateLocations() {
		newCurrentLocations.clear();
		Tile currTile = position.tile();
		getMap().getLocationsAt(newCurrentLocations, currTile.getX(), currTile.getY());
		for (GameLocation location : newCurrentLocations) {
			if (!currentLocations.contains(location)){
				location.onEntry(this);
			} else {
				currentLocations.remove(location);
			}
			addVisitedLocation(location.getId());
		}
		Iterator<GameLocation> iterator = currentLocations.iterator();
		while(iterator.hasNext()) {
			iterator.next().onExit(this);
			iterator.remove();
		}
		
		iterator = newCurrentLocations.iterator();
		while(iterator.hasNext()) {
			currentLocations.add(iterator.next());
			iterator.remove();
		}
	}
	
	/**
	 * Calculates and stores all the locations the character is currently at.
	 * 
	 * This is important to call when a map is loaded that contains this character
	 * so that OnExit events can be fired on the character accurately. 
	 */
	public void calculateCurrentLocations() {
		if (getMap() == null) {
			return;
		}
		for (GameLocation loc : currentLocations) {
			loc.onExit(this);
		}
		currentLocations.clear();
		getMap().getLocationsAt(currentLocations, (int)position.getX(), (int)position.getY());
	}
	
	protected void handleStealth() {
		if (getMap() == null || getMap().isWorldMap()) {
			return;
		}
		tempSet.clear();
		getAllCharactersInViewCone(tempSet);
		Iterator<AbstractGameCharacter> iterator = tempSet.iterator();
		Faction myFaction = getFaction();
		while(iterator.hasNext()) {
			AbstractGameCharacter character = iterator.next();
			if (!(character instanceof GameCharacter)) {
				continue;
			}
			
			GameCharacter gameCharacter = (GameCharacter)character;
			
			if (!gameCharacter.isSneaking()) {
				continue;
			}
			
			if (myFaction == character.getFaction()) {
				continue;
			}
			
			if (gameCharacter.isInvisible()) {
				continue;
			}
			
			Log.logLocalized("detectedBy", LogType.CHARACTER,  gameCharacter.getName(), getName());
			gameCharacter.setIsSneaking(false);
			gameCharacter.setIsInvisible(false);
		}
	}
	
	@Override
	public Color getHighlightColor(float x, float y) {
		return UsableGameObject.shouldHighlightUsable(this, x, y) ? Color.WHITE : null;
	}
	
	@Override
	public int getHighlightAmount(float x, float y) {
		return 1;
	}
	
	@Override
	public void setMap(GameMap map) {
		if (CoreUtil.equals(map, getMap())) {
			return;
		}
		
		if(getMap() != null && !getMap().isDisposed()) {
			for (Light light : lights.values()) {
				light.remove();
			}
		}
		
		createLights(map);
		visibleTilesLookup = null;

		if (map != null) {
			switchedMap = true;
		}
		
		if (getMap() != null) {
			getMap().onExit(this);
			for (GameLocation loc : currentLocations) {
				loc.onExit(this);
			}
			currentLocations.clear();
		}
		
		super.setMap(map);
		if (map == null) {
			return;
		}
		recalculateOffsets();
	}
	
	private void recalculateOffsets() {
		GameMap map = getMap();
		if (map == null) {
			return;
		} else if (!map.isMapLoaded()) {
			needOffsetRecalculation = true;
			return;
		}
		needOffsetRecalculation = false;
		if (animations == null) {
			setOffsets(0, 0);
			return;
		}
		Vector2 offset = animations.getMiddleOffset(s_state, s_orientation);
		if (map.isIsometric()) {
			setOffsets((-offset.x + map.getTileSizeX())* getScaleX(), -offset.y * getScaleY());
		} else {
			setOffsets((-offset.x + map.getTileSizeX()/2)*getScaleX(), (-offset.y+map.getTileSizeY()/2)*getScaleY());
		}
	}
	
	public String getName() {
		return Strings.getString(s_name);
	}

	public void setName(String name) {
		this.s_name = name;
	}
	
	/**
	 * Returns a localized, user-readable description of this character.
	 */
	public String getDescription() {
		return Strings.getString(s_description);
	}

	/**
	 * Sets a localized, user-readable description of this character.
	 */
	public void setDescription(String description) {
		s_description = description;
	}
		
	@Override
	public void onHit(Projectile projectile, GameObject user) {
		setState(State.ONHIT);
	}
	
	public void onAttack(GameCharacter attacker) {
		onAttack(attacker, 0);
	}
	
	public void onAttack(GameCharacter attacker, float delay) {
		setState(State.ONHIT, delay);
	}
	
	/**
	 * Sets all tiles that are in the LOS of this PC
	 * as discovered and revealed.
	 * 
	 * @return
	 */
	public void updateVisibleArea() {
		updateVisibleArea(false);
	}
	
	protected void clearVisibleArea() {
		if (visibleTilesLookupCounter > VISIBLE_TILES_LOOKUP_RESET) {
			visibleTilesLookupCounter = 1;
			Arrays.fill(getVisibleTileLookup(), 0);
		} else {
			++visibleTilesLookupCounter;
		}
		visibleArea.clear();
		viewConeArea.clear();
	}
	
	protected void addToVisibleArea(int x, int y) {
		if (getMap() == null || lineOfSight == null) {
			return;
		}
		visibleArea.add(x, y);
		getVisibleTileLookup()[x+y*getMap().getMapWidth()] = visibleTilesLookupCounter;
	}
	
	private int[] getVisibleTileLookup() {
		int mapWidth = getMap().getMapWidth();
		int mapHeight = getMap().getMapHeight();
		
		if (visibleTilesLookup == null) {
			visibleTilesLookupCounter = 1;
			visibleTilesLookup = new int[mapWidth*mapHeight];
		}
		return visibleTilesLookup;
	}
	
	/**
	 * Sets all tiles that are in the LOS of this PC
	 * as discovered and revealed.
	 * 
	 * If recalculateLOS is true, this will also
	 * recalculate the actual LOS using raycasting.
	 * This should only be done if the caller is sure
	 * the LOS is outdated, since it is an expensive operation.
	 * 
	 * @return
	 */
	protected void updateVisibleArea(boolean recalculateLOS) {
		if (lineOfSight == null || getMap() == null) {
			return;
		}
		
		GameMap map = getMap();
		
		if (recalculateLOS) {
			// this will ensure the LOS has our latest position and that it recalculates itself
			updateSightPosition(map, lineOfSight);
		}
		
		boolean isPlayerControlled = belongsToPlayerFaction();
		clearVisibleArea();
		
		// if we are not active, we are done here, since we can't see anything
		if (!isActive()) {
			return;
		}
		
		int mapWidth = map.getMapWidth();
		int mapHeight = map.getMapHeight();
		
		int sightRadius = getMap().isWorldMap() ? Configuration.getSightRadiusWorld() : Configuration.getSightRadiusLocal();
		
		int[] visibleTilesLookup = getVisibleTileLookup();
		Orientation coneOrientation = getOrientation();
		if (getMap().isIsometric()) {
			coneOrientation = coneOrientation.getAntiClockwise();
		}
		PositionArray sightCone = MathUtil.getCone((int)position.getX(), (int)position.getY(), 90, sightRadius/2, coneOrientation);
		PositionArray visibleTiles= lineOfSight.getVisibleTiles();
		for (int i = 0; i < visibleTiles.size(); ++i) {
			int x = visibleTiles.getX(i);
			int y = visibleTiles.getY(i);
			if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
				continue;
			}
			visibleArea.add(x, y);
			visibleTilesLookup[x+y*mapWidth] = visibleTilesLookupCounter;
			if (isPlayerControlled) {
				map.markTileAsSeen(x, y);
			}
			if (sightCone.contains(x, y)) {
				viewConeArea.add(x, y);
			}
		}

		if (isPlayerControlled) {	
			map.recalculateMTOVisibility();
		}
	}
	
	protected PositionArray getVisibleArea() {
		return visibleArea;
	}
	
	protected PositionArray getViewConeArea() {
		return viewConeArea;
	}
	
	/**
	 * Returns true if this character can see the
	 * supplied Character.
	 * 
	 * @param go
	 * @return
	 */
	public boolean canSee(AbstractGameCharacter character) {
		if (character.isInvisible()) {
			return false;
		}
		return character.isSneaking() ? getViewConeArea().contains(
				character.position().tile()) : canSee((GameObject) character);
	}
	
	/**
	 * Returns true if this character can see the
	 * supplied GameObject.
	 * 
	 * @param go
	 * @return
	 */
	public boolean canSee(GameObject go) {
		return canSeeTile((int)go.position().getX(), (int)go.position().getY());
	}
	
	/**
	 * Returns true if this PC can see the tile 
	 * with the specified coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean canSeeTile(int x, int y) {
		if (x < 0 || y < 0 || x> getMap().getMapWidth() || y > getMap().getMapHeight()) {
			return false;
		}
		return canSeeTile(x+y*getMap().getMapWidth());
	}
	
	/**
	 * Returns true if this PC can see the tile 
	 * with the specified number. Usually x + y*mapWidth.
	 * @param tileNumber
	 * @return
	 */
	public boolean canSeeTile(int tileNumber) {
		if (tileNumber < 0 || visibleTilesLookup == null || tileNumber >= visibleTilesLookup.length) {
			return false;
		}
		return visibleTilesLookup[tileNumber] == visibleTilesLookupCounter;
	}
	
	/**
	 * Whether or not this character can see map geometry (for example building tiles, 
	 * trees, and so on) on the supplied coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean canSeeGeometry(int x, int y) {
		if (lineOfSight == null) {
			return false;
		}
		Vector2 tilePosition = MathUtil.getVector2().set(x, y);
		getMap().projectFromTiles(tilePosition);
		boolean returnValue = lineOfSight.containsInRadius(tilePosition.x, tilePosition.y)
				&& lineOfSight.isContainedInVisibleShapePolygon(x, y);
		MathUtil.freeVector2(tilePosition);
		return returnValue;
	}
	
	/**
	 * Whether the the attack animation that is currently playing reached the frame
	 * that depicts this character scoring a hit.
	 * 
	 *  Returns true if the animation currently playing is not an attack animation.
	 * @return
	 */
	public boolean hasAtackAnimationHit() {
		Animation currentAnimation = getAnimation();
		if (currentAnimation instanceof AttackAnimation) {
			return ((AttackAnimation)getAnimation()).targetHit(getAnimationStateTime());
		}
		return true;
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		characterCircle.gatherAssets(assetStore);
		if (animations != null) {
			animations.gatherAssets(assetStore);
		}
		
		if (s_audioProfile != null) {
			s_audioProfile.gatherAssets(assetStore);
		}
		
		if (getRepresentative() != null && !this.equals(getRepresentative())) {
			String portrait = getRepresentative().getPortraitFile();
			if (portrait != null) {
				assetStore.put(portrait, Texture.class);
			}
			getRepresentative().getInventory().gatherAssets(assetStore);
		}
	}
	
	@Override
	public float getProjectileOriginXOffset() {
		return s_model != null ? s_model.getProjectileOriginXOffset() : super.getProjectileOriginXOffset();
	}
	
	@Override
	public float getProjectileOriginYOffset() {
		return s_model != null ? s_model.getProjectileOriginYOffset() : super.getProjectileOriginYOffset();
	}
	
	public Brain brain() {
		return brain;
	}
	
	@Override
	public void clearAssetReferences() {
		characterCircle.clearAssetReferences();
		if (animations != null) {
			animations.clearAssetReferences();
		}
		if (s_audioProfile != null) {
			s_audioProfile.clearAssetReferences();
		}
		super.setAnimation(null);
	}

	@Override
	public void undispose() {
		super.undispose();
		createLights(getMap());
	}
	
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);	
		
		if (s_model != null) {
			try {
				animations = new CharacterAnimationMap(s_model, s_audioProfile, getSpeed());
			} catch (IOException e) {
				throw new GdxRuntimeException("Problem loading animation for character "+getInternalId(),e);
			}
		}
		
		characterCircle.setColor(ColorUtil.WHITE_FIFTY);
		destinationIndicator.setColor(ColorUtil.WHITE_FIFTY);
		
		brain.loadFromXML(root);
		
		getFaction().addMember(this);
		
		Element tempHostilityElement = root.getChildByName(XML_TEMPORARY_HOSTILITY);
		if (tempHostilityElement != null) {
			for(int i = 0; i < tempHostilityElement.getChildCount(); ++i) {
				Element hostilityElement = tempHostilityElement.getChild(i);
				Integer duration = Integer.parseInt(hostilityElement.get(XML_DURATION));
				GameCalendarDate start = new GameCalendarDate(gameState.getCalendar());
				start.readFromXML(hostilityElement.getChildByName(XML_START));
				temporaryHostility.put(Faction.getFaction(hostilityElement.getName()), new Pair<GameCalendarDate, Integer>(start, duration));
			}
		}
		
		resetCharacterCircleColor();
		
		Element visited = root.getChildByName(XML_VISITED);
		if (visited != null) {
			String visitedLocations = visited.getText();
			String[] locations = visitedLocations.split(",");
			for (String location : locations) {
				this.visitedLocations.add(location.trim());
			}
		}
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		
		brain.writeToXML(writer);
		
		if (temporaryHostility.size > 0) {
			writer.element(XML_TEMPORARY_HOSTILITY);
			for(Entry<Faction, Pair<GameCalendarDate, Integer>> hostility : temporaryHostility) {
				writer.element(hostility.key.getId());
				writer.element(XML_START);
				hostility.value.getLeft().writeToXML(writer);
				writer.pop();
				writer.element(XML_DURATION, hostility.value.getRight());
				writer.pop();
			}
			writer.pop();
		}
		
		if (visitedLocations.size() > 0) {
			writer.element(XML_VISITED);
			StringBuilder text = StringUtil.getFSB();
			int i = 0;
			for (String location : visitedLocations) {
				++i;
				text.append(location);
				if (i < visitedLocations.size()) {
					text.append(", "); 
				}
			}
			writer.text(text);
			StringUtil.freeFSB(text);
			writer.pop();
		}
	}

}