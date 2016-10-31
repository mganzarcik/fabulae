package mg.fishchicken.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Locale;
import java.util.Random;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.core.FastForwardCallback.InterruptReason;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.GameLoader;
import mg.fishchicken.core.saveload.GameSaver;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.combat.CombatManager;
import mg.fishchicken.gamelogic.crime.CrimeManager;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.locations.CombatGameMap.CombatMapInitializationData;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.survival.SurvivalManager;
import mg.fishchicken.gamelogic.time.GameCalendar;
import mg.fishchicken.gamelogic.time.GameCalendarDate;
import mg.fishchicken.gamelogic.weather.Weather.WeatherRenderer;
import mg.fishchicken.gamelogic.weather.WeatherManager;
import mg.fishchicken.gamestate.GameObjectPosition;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.Variables;
import mg.fishchicken.gamestate.crime.Crime;
import mg.fishchicken.graphics.lights.GameConeLight;
import mg.fishchicken.graphics.lights.GamePointLight;
import mg.fishchicken.screens.start.StartGameScreen;
import mg.fishchicken.tweening.AudioTrackTweenAccessor;
import mg.fishchicken.tweening.ColorTweenAccessor;
import mg.fishchicken.tweening.LightTweenAccessor;
import mg.fishchicken.tweening.PositionedThingTweenAccessor;
import mg.fishchicken.tweening.ViewConeTweenAccessor;
import mg.fishchicken.tweening.WeatherParticleEmitterTweenAccessor;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.dialog.ProgressDialogCallback;
import mg.fishchicken.ui.dialog.ProgressDialogSettings;
import mg.fishchicken.ui.saveload.SaveGameDetails;
import aurelienribon.tweenengine.Tween;
import box2dLight.ViewConeLight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.WeatherParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

public class GameState implements VariableContainer {
	
	private ObjectMap<String, Array<GameObject>> gameObjectsById = new ObjectMap<String, Array<GameObject>>();
	private ObjectMap<String, Array<GameObject>> unassignedLocalGameObjectsById = new ObjectMap<String, Array<GameObject>>();
	private ObjectMap<String, GameObject> gameObjectsByInternalId = new ObjectMap<String, GameObject>();
	private ObjectMap<String, GameObject> unassignedLocalGameObjectsByInternalId = new ObjectMap<String, GameObject>();
	private ObjectMap<String, Array<GameObject>> gameObjectsByType = new ObjectMap<String, Array<GameObject>>();
	private ObjectMap<String, GameLocation> locationsByInternalId = new ObjectMap<String, GameLocation>();
	private ObjectMap<String, Array<GameLocation>> locationsById = new ObjectMap<String, Array<GameLocation>>();
	private ObjectMap<String, ObjectSet<GameMap>> undisposedMapsByGroup = new ObjectMap<String, ObjectSet<GameMap>>();
	private ObjectMap<String, GameMap> mapsById = new ObjectMap<String, GameMap>();
	private GameMap currentMap;
	private GameCalendarDate currentDate;
	private GameCalendar calendar;
	private Variables variables;
	
	private SurvivalManager survivalManager;
	private CombatManager combatManager;
	private WeatherManager weatherManager;
	private CrimeManager crimeManager;

	private GameSaver gameSaver;
	private GameLoader gameLoader;
	
	private PlayerCharacterGroup theGroup;
	private PlayerCharacterController playerController;
	
	private boolean isPaused = false; 
	private boolean playerPaused = false;
	private boolean playerUnpaused = false;
	
	private Random s_randomGenerator; 
	private int s_idCounter = 0; 
	private float globalDelta = 0;
	private float fastForwardBy = 0f;
	private float fastForwarded = 0f;
	private boolean fastForwarding = false; 
	private FastForwardCallback fastForwardCallback = null;
	private CharacterGroup fastForwardRandomEncounter = null;
	private float fastForwardRandomAfter;
	private FishchickenGame game;
	private static GameState gameState;
	
	static {
		Tween.setCombinedAttributesLimit(4);
		Tween.registerAccessor(Color.class, new ColorTweenAccessor());
		Tween.registerAccessor(GameObject.class, new PositionedThingTweenAccessor());
		Tween.registerAccessor(GamePointLight.class, new LightTweenAccessor());
		Tween.registerAccessor(GameConeLight.class, new LightTweenAccessor());
		Tween.registerAccessor(WeatherParticleEmitter.class, new WeatherParticleEmitterTweenAccessor());
		Tween.registerAccessor(AudioTrack.class, new AudioTrackTweenAccessor());
		Tween.registerAccessor(ViewConeLight.class, new ViewConeTweenAccessor());
	}
	
	/**
	 * Creates a new game state. This will discard any previous game state and inject the new
	 * state everywhere where game state is needed. This should therefore only ever
	 * be called before loading a new game module. Otherwise it will just ruin everyone's day.
	 * @param game
	 */
	public GameState(FishchickenGame game) {
		this.game = game;
		gameState = this;
		theGroup = new PlayerCharacterGroup(this);
		GameObject.setGameState(this);
		Action.setGameState(this);
		Condition.setGameState(this);
		GameLocation.setGameState(this);
		Crime.setGameState(this);
		
		playerController = new PlayerCharacterController(this, theGroup);
		combatManager = new CombatManager(this);
		weatherManager = new WeatherManager(this);
		crimeManager = new CrimeManager(this);
		survivalManager = new SurvivalManager(Gdx.files.internal(Configuration.getFileSurvivalConfiguration()));
		gameLoader =  new GameLoader(this, game, weatherManager, mapsById);
		variables = new Variables();
	}
	
	public void setIdCounter(int value) {
		s_idCounter = value;
	}
	
	public int getCurrentId() {
		return s_idCounter;
	}
	
	/**
	 * Gets and increments the counter for unique IDs.
	 */
	public int getNextId() {
		return s_idCounter++;
	}
	
	public GameMap getCurrentMap() {
		return currentMap;
	}
	
	public void setCurrentMap(GameMap map) {
		currentMap = map;
		weatherManager.mapChanged(map);
	}
	
	public void switchToMap(String mapId,  Tile startPos) {
		// remove any NPCs before switching map, since they cannot change maps
		theGroup.removeAllNonPlayerCharacters();
		endCombat();
		pauseGame();
		if (currentMap != null) {
			currentMap.currentMapWillChange();
		}
		game.switchToMap(mapId, currentMap, startPos, false);
		currentMap = null;
	}
	
	/**
	 * Switches the game to a combat map.
	 * 
	 * @param fromX x coordinate of the tile on the world map where we should return the characters once they exit the combat map victorious
	 * @param fromY y coordinate of the tile on the world map where we should return the characters once they exit the combat map victorious
	 * @param escapeX x coordinate of the tile on the world map where we should return the characters if escaping
	 * @param escapeY y coordinate of the tile on the world map where we should return the characters if escaping
	 * @param enemyGroup the group of enemies the player will fight on the map
	 */
	public void switchToCombatMap(CombatMapInitializationData data)  {
		handleFastForwardInterrupt(InterruptReason.AMBUSH);
		
		if (fastForwarding) {
			return;
		}
		
		if (currentMap == null) {
			Log.log("Cannot switch to combat map, no current map set.", LogType.ERROR);
			return;
		}
		String mapId = currentMap.getCombatMapId(data.fromX, data.fromY);
		if (mapId == null) {
			Log.log("Cannot switch to combat map from map {0}, no combat maps defined for position {1},{2}.", LogType.ERROR, currentMap.getId(), data.fromX, data.fromY);
		}
		// remove any NPCs before switching map, since they cannot change maps
		theGroup.removeAllNonPlayerCharacters();
		endCombat();
		pauseGame();
		currentMap.currentMapWillChange();
		game.switchToCombatMap(mapId, currentMap, data);
		currentMap = null;
	}
	
	public void screenResized() {
		weatherManager.resized();
	}
	
	public void cameraMoved() {
		weatherManager.cameraMoved();
		if (currentMap != null) {
			currentMap.cameraMoved();
		}
	}
	
	public void clearLocations() {
		locationsByInternalId.clear();
		locationsById.clear();
		mapsById.clear();
		undisposedMapsByGroup.clear();
	}
	
	public boolean addLocation(GameLocation location) {
		if (!locationsByInternalId.containsKey(location.getInternalId())) {
			locationsByInternalId.put(location.getInternalId(), location);
			Array<GameLocation> locations = locationsById.get(location.getId());
			if (locations == null) {
				locations = new Array<GameLocation>();
				locationsById.put(location.getId(), locations);
			}
			if (!locations.contains(location, false)) {
				locations.add(location);
			}
			if (location instanceof GameMap) {
				mapsById.put(location.getId(), (GameMap)location);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the GameMap with the supplied id.
	 * 
	 * Only maps that have been loaded at some point
	 * during the game are returned. 
	 * 
	 * @param internalId
	 * @return
	 */
	public GameMap getMapById(String id) {
		return mapsById.get(id);
	}
	
	/**
	 * Returns the first GameLocation with the supplied id.
	 * 
	 * Only locations that have been loaded at some point
	 * during the game are returned. 
	 * 
	 * Note that there might be more locations with the same ID, 
	 * but only one will be returned.
	 * 
	 * @param internalId
	 * @return
	 */
	public GameLocation getLocationById(String id) {
		id = id.toLowerCase(Locale.ENGLISH);
		Array<GameLocation> locations = locationsById.get(id);
		return locations != null && locations.size > 0 ? locations.first() : null;
	}
	
	/**
	 * Returns the GameLocation with the supplied internal id.
	 * 
	 * Only locations that have been loaded at some point
	 * during the game are returned. 
	 * 
	 * @param internalId
	 * @return
	 */
	public GameLocation getLocationByIternalId(String internalId) {
		return locationsByInternalId.get(internalId);
	}
	
	
	/**
	 * Adds the supplied map to the list of undisposed maps.
	 * 
	 * The map is only added if it is not disposed and belongs to a map group.
	 * @param map
	 */
	public void addUndisposedMap(GameMap map) {
		String group = map.getMapGroup();
		if (group != null && !map.isDisposed()) {
			ObjectSet<GameMap> maps = undisposedMapsByGroup.get(group);
			if (maps == null) {
				maps = new ObjectSet<GameMap>();
				undisposedMapsByGroup.put(group, maps);
			}
			maps.add(map);
		}
	}
	
	/**
	 * Returns true if the supplied map belongs to a map group and is tracked as undisposed.
	 * @param map
	 * @return
	 */
	public boolean belongsToUndisposed(GameMap map) {
		String mapGroup = map.getMapGroup();
		ObjectSet<GameMap> maps = mapGroup != null ? getUndisposedMaps(mapGroup) : null;
		return maps != null && maps.contains(map);
	}
	
	/**
	 * Returns all maps that are undisposed and belong to the supplied map group.
	 * @param group
	 * @return
	 */
	public ObjectSet<GameMap> getUndisposedMaps(String group) {
		return undisposedMapsByGroup.get(group);
	}
	
	public void clearGameObjects() {
		gameObjectsById.clear();
		gameObjectsByInternalId.clear();
		gameObjectsByType.clear();
		unassignedLocalGameObjectsById.clear();
		unassignedLocalGameObjectsByInternalId.clear();
	}
	
	/**
	 * Adds the supplied GO to the game state. This will
	 * ensure that as long as the game object is marked as global,
	 * it will be updated even when a different map
	 * is currently active.
	 * 
	 * @param go
	 * @return
	 */
	public boolean addGameObject(GameObject go) {
		if (!gameObjectsByInternalId.containsKey(go.getInternalId())) {
			gameObjectsByInternalId.put(go.getInternalId(), go);
			
			Array<GameObject> gosById = gameObjectsById.get(go.getId());
			if (gosById == null) {
				gosById = new Array<GameObject>();
				gameObjectsById.put(go.getId(), gosById);
			}
			if (!gosById.contains(go, false)) {
				gosById.add(go);
			}
			
			Array<GameObject> byType = null;
			if (gameObjectsByType.containsKey(go.getType())) {
				byType = gameObjectsByType.get(go.getType());
			} else {
				byType = new Array<GameObject>();
				gameObjectsByType.put(go.getType(), byType);
			}
			byType.add(go);
			return true;
		}
		return false;
	}
	
	/**
	 * Adds the supplied GO to Global as unassigned (i.e. 
	 * not belonging to any map, but not Global).
	 * 
	 * All non global GOs without a map should be stored here
	 * if they are to be queried and saved / loaded successfully.
	 * 
	 * @param go
	 * @return
	 */
	public void addUnassignedGameObject(GameObject go) {
		unassignedLocalGameObjectsByInternalId.put(go.getInternalId(), go);
		
		Array<GameObject> gosById = unassignedLocalGameObjectsById.get(go.getId());
		if (gosById == null) {
			gosById = new Array<GameObject>();
			gameObjectsById.put(go.getId(), gosById);
		}
		if (!gosById.contains(go, false)) {
			gosById.add(go);
		}
	}
	
	/**
	 * Removes the supplied GO from Global as unassigned (i.e. 
	 * not belonging to any map, but not Global).
	 * 
	 * @param go
	 * @return
	 */
	public void removeUnassignedGameObject(GameObject go) {
		unassignedLocalGameObjectsByInternalId.remove(go.getInternalId());
		
		Array<GameObject> gosById = unassignedLocalGameObjectsById.get(go.getId());
		if (gosById != null) {
			gosById.removeValue(go, false);
		}
	}
	
	/**
	 * Removes the GO from Global.
	 * 
	 * @param go
	 * @see #addGameObject(GameObject)
	 */
	public void removeGameObject(GameObject go) {
		gameObjectsByInternalId.remove(go.getInternalId());
		if (gameObjectsByType.containsKey(go.getType())) {
			gameObjectsByType.get(go.getType()).removeValue(go, false);
		}
		Array<GameObject> gosById = gameObjectsById.get(go.getId());
		if (gosById != null) {
			gosById.removeValue(go, false);
		}
	}

	/**
	 * Returns all global game objects of the specified type.
	 * 
	 * Inactive GOs are included.
	 * 
	 * Modifying the returned array will do nothing.
	 * 
	 * @param type
	 * @return
	 */
	public Array<GameObject> getGlobalGameObjectsOfType(String type) {
		Array<GameObject> returnValue = new Array<GameObject>();
		
		if (gameObjectsByType.containsKey(type)) {
			returnValue.addAll(gameObjectsByType.get(type)); 
		}
		return returnValue;
	}
	
	private GameObject getGameObjectById(String id, boolean useInternal, Class<?>... classes) {
		id = id.toLowerCase(Locale.ENGLISH);
		boolean noTypes = classes.length < 1;
		
		// search global objects
		GameObject returnValue = null;
		if (useInternal) {
			returnValue = gameObjectsByInternalId.get(id);
		} else {
			Array<GameObject> gosById = gameObjectsById.get(id); 
			if (gosById != null && gosById.size > 0) {
				if (noTypes) {
					returnValue = gosById.first();
				} else {
					for (int i = 0; i < gosById.size && returnValue == null; ++i) {
						GameObject go = gosById.get(i);
						for (Class<?> clazz : classes) {
							if (clazz.isAssignableFrom(go.getClass())) {
								returnValue = go;
								break;
							}
						}
					}
				}
			}
		}
		
		// search unassigned game objects
		if (returnValue == null) {
			if (useInternal) {
				returnValue = unassignedLocalGameObjectsByInternalId.get(id);
			} else {
				Array<GameObject> gosById = unassignedLocalGameObjectsById.get(id); 
				if (gosById != null && gosById.size > 0) {
					if (noTypes) {
						returnValue = gosById.first();
					} else {
						for (int i = 0; i < gosById.size && returnValue == null; ++i) {
							GameObject go = gosById.get(i);
							for (Class<?> clazz : classes) {
								if (clazz.isAssignableFrom(go.getClass())) {
									returnValue = go;
									break;
								}
							}
						}
					}
				}
			}
		}
		
		// search objects on the current map
		if (returnValue == null && currentMap != null) {
			returnValue = currentMap.getGameObject(id, useInternal, classes);
		}
		
		// search objects on other known maps
		if (returnValue == null) {
			for (GameLocation loc : locationsByInternalId.values()) {
				if (loc instanceof GameMap) {
					returnValue = ((GameMap)loc).getGameObject(id, useInternal, classes);
					if (returnValue != null) {
						break;
					}
				}
				
			}
		}
		
		return returnValue;
	}
	
	/**
	 * Returns the current in-game time.
	 * 
	 * @return
	 */
	private GameCalendarDate getGameDate() {
		if (currentDate == null) {
			currentDate = new GameCalendarDate(getCalendar());
		} 
		return currentDate;
	}
	
	public void resetGameDate() {
		currentDate = null;
	}
	
	public GameCalendar getCalendar() {
		if (calendar == null) {
			calendar = new GameCalendar(Gdx.files.internal(Configuration.getFileCalendar()));
		}
		return calendar;
	}
	
	public SurvivalManager getSurvivalManager() {
		return survivalManager;
	}
	
	public CrimeManager getCrimeManager() {
		return crimeManager;
	}
	
	public WeatherRenderer getCurrentWeatherRenderer() {
		return weatherManager.getCurrentWeatherRenderer();
	}
	
	/**
	 * Moves the game time forward (and only forward) by the supplied number of hours.
	 * 
	 * This not only changes the time, but actually simulates everything that happened
	 * during that time and updates the game state accordingly.
	 * 
	 * @param hours
	 */
	public void fastForwardTimeBy(ProgressDialogSettings settings, final FastForwardCallback callback, boolean canHaveRandomEncounter) {
		unpauseGame();
		fastForwarding = true;
		fastForwardRandomEncounter = null;
		fastForwardBy = settings.end*3600;
		if (currentMap != null) {
			fastForwardBy = fastForwardBy / currentMap.getGameTimeMultiplier();
			
			PlayerCharacterGroup group = getPlayerCharacterGroup();
			AbstractGameCharacter character = currentMap.isWorldMap() ? group.getGroupGameObject() : group.getPlayerCharacters().get(0);
			// determine if an ambush will occur during this fast forward
			fastForwardRandomEncounter = canHaveRandomEncounter ? currentMap.getRandomEncounter(character.position().tile(), group.getAverageLevel(true)) : null;
			if (fastForwardRandomEncounter != null) {
				fastForwardRandomAfter = MathUtils.random(0, fastForwardBy);
			}
		}
		
		fastForwarded = 0;
		fastForwardCallback = callback;
		
		UIManager.displayProgressDialog(settings, new ProgressDialogCallback() {
			@Override
			public void onCancelled(int currentValue) {
				fastForwarding = false;
				callback.onCancelled(currentValue);
			}
		});
	}
	
	private float updateFastForward(float deltaTime, Camera camera) {
		if (fastForwarding && fastForwarded >= fastForwardBy) {
			fastForwarding = false;
			UIManager.hideProgressDialog();
			if (fastForwardCallback != null) {
				fastForwardCallback.onFinished();
			}
			fastForwardCallback = null;
		}
		
		if (fastForwarding && !isPaused) {
			int currentTimeMultiplier = currentMap.getGameTimeMultiplier();
			deltaTime = Configuration.getFastForwardStep()
					* Math.max(Configuration.getWorldGameTimeMultiplier(),
							Configuration.getLocalGameTimeMultiplier())
					/ currentTimeMultiplier;
			fastForwarded += deltaTime;
			UIManager.updateProgressDialog((fastForwarded / 3600) * currentTimeMultiplier);
			if (fastForwardRandomEncounter != null && fastForwarded >= fastForwardRandomAfter) {
				handleFastForwardRandomEncounter();
			}
		}
		return deltaTime;
	}
	
	private void handleFastForwardRandomEncounter() {
		PlayerCharacterGroup group = getPlayerCharacterGroup();
		GameCharacter bestScout = group.getMemberWithHighestSkill(Skill.SCOUTING);
		
		if (bestScout.stats().skills().getSkillRank(Skill.SCOUTING) > 0) {
			if (bestScout.stats().rollSkillCheck(Skill.SCOUTING, fastForwardRandomEncounter)) {
				fastForwardRandomEncounter = null;
				Log.logLocalized("ambushAvoided", LogType.COMBAT);
			}
		}
		if (fastForwardRandomEncounter != null) {
			if (currentMap.isWorldMap()) {
				GameObjectPosition groupPosition= group.getGroupGameObject().position();
				CombatMapInitializationData data = new CombatMapInitializationData(
						currentMap.getId(), fastForwardRandomEncounter,
						groupPosition.tile(),
						groupPosition.tile());
				fastForwardRandomEncounter = null;
				switchToCombatMap(data);
			} else {
				handleFastForwardInterrupt(InterruptReason.AMBUSH);
				if (!fastForwarding) {
					fastForwardRandomEncounter.setMap(currentMap, true);
					startCombat();
					Vector2 tempVector = bestScout.position().setVector2(MathUtil.getVector2());
					Array<GameCharacter> unpositioned = fastForwardRandomEncounter.setPosition(tempVector, bestScout.getOrientation().getOpposite(), currentMap);
					MathUtil.freeVector2(tempVector);
					// if we were not able to position any enemy, we skip the encounter and move one
					if (unpositioned.size == fastForwardRandomEncounter.getMembers().size) {
						fastForwardRandomEncounter.setMap(null, true);
						endCombat();
					} else {
						for (GameCharacter character : unpositioned) {
							Log.log("Character {0} could not be positioned, killing silently.", LogType.ERROR, character.getInternalId());
							character.setActive(false);
							character.setMap(null);
						}
					}
					fastForwardRandomEncounter = null;
				}
			}
		}
	}
	
	private void handleFastForwardInterrupt(InterruptReason reason) {
		if (fastForwarding) {
			if (fastForwardCallback != null) {
				fastForwarding = !fastForwardCallback.onInterrupted(reason, (fastForwarded/3600)*currentMap.getGameTimeMultiplier());
			} else {
				fastForwarding = false;
			}
			
			if (!fastForwarding) {
				UIManager.hideProgressDialog();
				if (InterruptReason.AMBUSH == reason) {
					Log.logLocalized("ambushed", LogType.COMBAT);
				}
			}
		}
	}
	
	/**
	 * Updates the global game time. Uses local or world map multipliers
	 * based on the current map. 
	 * 
	 * This affects everything from time of day to the current date.
	 * 
	 * If there is no current map, the time is not updated.
	 * 
	 * @param deltaTime
	 */
	public void updateGameTime(float deltaTime) {
		if (currentMap == null) {
			return;
		}
		getGameDate().addToSecond(deltaTime * (float)currentMap.getGameTimeMultiplier());
	}
	
	/**
	 * Updates the global game time by adding the supplied number of seconds
	 * to it. This affects everything from time of day to the current date.
	 * 
	 * @param deltaTime
	 */
	public void updateGameTimeWithGameSeconds(float gameSeconds) {
		getGameDate().addToSecond(gameSeconds);
	}
	
	/**
	 * Updates the game state by the supplied number of real world seconds.
	 * 
	 * This updates everything that represents the game state, including the 
	 * weather, all maps and locations, game objects and the UI
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime, Camera camera) {
		deltaTime = updateFastForward(deltaTime, camera);
		updateGameState(deltaTime, camera);
	}
	
	private void updateGameState(float gameDeltaTime, Camera camera) {
		// map must be updated first, because it updates all ongoing actions, 
		// which might actually switch to a different map
		// which is something all following updates must be aware of
		if (currentMap != null) {
			currentMap.update(gameDeltaTime, camera);
		}
		
		if (isPaused) {
			return;
		}
		
		if (!isCombatInProgress()) {
			updateGameTime(gameDeltaTime);
		}
		
		globalDelta += gameDeltaTime / ((float)Configuration.getWorldGameTimeMultiplier() / Configuration.getLocalGameTimeMultiplier());
		 
		if (globalDelta > Configuration.getGlobalGameObjectsUpdateInterval()) {
			for (GameObject go : gameObjectsByInternalId.values()) {
				if (go.isGlobal() && !currentMap.equals(go.getMap())) {
					go.update(globalDelta);
				}
			}
			globalDelta = 0;
		}
		
		
		if (currentMap != null) {
			combatManager.update(gameDeltaTime);
			weatherManager.update(gameDeltaTime);
			if (currentMap.isWorldMap() && !fastForwarding && !playerUnpaused
					&& !theGroup.hasActiveAction(Action.VERB_ACTIONS)) {
				pauseGame();
				return;
			}
		}
	}

	public void pauseGame() {
		pauseGame(false);
	}
	
	public void pauseGame(boolean playerPaused) {
		if (!playerPaused && fastForwarding) {
			return;
		}
		if (!isPaused) {
			weatherManager.pause();
		}
		this.playerPaused = playerPaused;
		this.playerUnpaused = false;
		isPaused = true;
	}
	
	public void unpauseGame() {
		unpauseGame(false);
	}
	
	public void unpauseGame(boolean playerUnpaused) {
		if (isPaused) {
			weatherManager.resume();
		}
		isPaused = false;
		this.playerUnpaused = playerUnpaused;
		this.playerPaused = false;
	}
	
	/**
	 * Toggles the stealth mode, either enabling or disabling
	 * sneaking on currently selected player-controlled characters.
	 * 
	 */
	public void toggleStealth() {
		setStealth(!isAnySelectedPCSneaking());
	}
	
	/**
	 * Enables of disables the stealth mode, 
	 * based on the supplied value.
	 * @param value
	 */
	public void setStealth(boolean value) {
		if (currentMap != null && !currentMap.isWorldMap()) {
			theGroup.setStealth(value);
		}
	}
	
	public void startCombat() {
		handleFastForwardInterrupt(InterruptReason.AMBUSH);
		if (fastForwarding) {
			return;
		}
		
		setStealth(false);
		combatManager.startCombat();
		UIManager.startCombatMode();
		unpauseGame();
		currentMap.setRenderGrid(true);
	}
	
	public void switchToNextSide() {
		combatManager.switchToNextSide();
	}
	
	public void endCombat() {
		if (combatManager.isCombatInProgress()) {
			combatManager.endCombat();
			UIManager.endCombatMode();
			playerController.stopTargetSelection(false);
			currentMap.setRenderGrid(false);
		}
	}
	
	public void readRandomGenerator(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		s_randomGenerator = (Random) ois.readObject();
	}

	/**
	 * This will return an instance of the Random.
	 * If the instance does not exist yet, a new one
	 * will be created with a random seed.
	 * 
	 * @return
	 */
	private Random getRandom() {
		if (s_randomGenerator == null)  {
			s_randomGenerator = new Random();
		}
		return s_randomGenerator;
	}
	
	public PlayerCharacterController getPlayerCharacterController() {
		return playerController;
	}
	
	/**
	 * Saves the current game to the supplied slot.
	 * @param slot
	 */
	public void saveGame(String slot, String name) {
		if (combatManager.isCombatInProgress()) {
			UIManager.displayMessage(Strings.getString(UIManager.STRING_TABLE, "information"), Strings.getString(CombatManager.STRING_TABLE, "CannotSave"));
			return;
		}
		if (currentMap.isCombatMap()) {
			UIManager.displayMessage(Strings.getString(UIManager.STRING_TABLE, "information"), Strings.getString(CombatManager.STRING_TABLE, "CannotSaveOnCombatMaps"));
			return;
		}
		
		if (fastForwarding) {
			UIManager.displayMessage(Strings.getString(UIManager.STRING_TABLE, "information"), Strings.getString(UIManager.STRING_TABLE, "CannotSave"));
			return;
		}
		
		if (gameSaver == null) {
			gameSaver = new GameSaver(this, weatherManager, s_randomGenerator, locationsByInternalId, unassignedLocalGameObjectsByInternalId);
		}
		
		gameSaver.saveGame(new SaveGameDetails(slot, name, this));
	}
	
	/**
	 * Loads a game saved to the supplied slot. Calls the onOk method of the callback (if supplied)
	 * once the game was loaded successfully, onError otherwise.
	 * 
	 * @param slot
	 */
	public void loadGame(String slot, OkCancelCallback<GameMap> callback) {
		gameLoader.loadGame(slot, callback);
	}
	
	/**
	 * This will completely unload the current game, disposing of all
	 * currently loaded game assets (not master data), reseting the UI,
	 * clearing the player character group and removing all state.
	 * 
	 * It will then show the main menu screen.
	 * 
	 */
	public void exitGameToMainMenu() {
		exitGameToMainMenu(null);
	}
	
	/**
	 * This will completely unload the current game, disposing of all
	 * currently loaded game assets (not master data), reseting the UI,
	 * clearing the player character group and removing all state.
	 * 
	 * It will then show the main menu screen. If error is not null, it
	 * will also immediately display the supplied error message.
	 * 
	 */
	public void exitGameToMainMenu(String error) {
		gameLoader.unloadGame();
		game.setScreen(new StartGameScreen(game, this, error));
	}

	/**
	 * Returns the player character group. This group contains all
	 * characters currently controlled by the player. 
	 * 
	 * @return
	 */
	public static PlayerCharacterGroup getPlayerCharacterGroup() {
		return gameState.theGroup;
	}
	
	/**
	 * Returns true if the player can currently end combat.
	 * 
	 * @return
	 */
	public static boolean canPlayerEndCombat() {
		return gameState.combatManager.canPlayerEndCombat();
	}
	
	/**
	 * Returns the current in-game time.
	 * 
	 * @return
	 */
	public static GameCalendarDate getCurrentGameDate() {
		return gameState.getGameDate();
	}
	
	/**
	 * Returns the current in-game temperature.
	 * 
	 * @return
	 */
	public static int getCurrentTemperature() {
		return  gameState.weatherManager.getCurrentTemperature();
	}
	
	/**
	 * Returns true if the game is currently being loaded.
	 * 
	 * @return
	 */
	public static boolean isLoadingGame() {
		return  gameState.gameLoader.isLoadingGame();
	}

	/**
	 * Returns true if the came is currently in combat mode
	 * and it is player's turn.
	 *  
	 * @return
	 */
	public static boolean isPlayersTurn() {
		return  gameState.combatManager.isPlayersTurn();
	}

	/**
	 * Returns true if the game is currently in combat mode.
	 * @return
	 */
	public static boolean isCombatInProgress() {
		return  gameState.combatManager.isCombatInProgress();
	}
	
	/**
	 * Returns true if any currently selected player character is sneaking.
	 * @return
	 */
	public static boolean isAnySelectedPCSneaking() {
		return gameState.theGroup.isAnySelectedPCSneaking();
	}
	
	/**
	 * Returns true if the game is currently paused.
	 * 
	 * @return
	 */
	public static boolean isPaused() {
		return  gameState.isPaused;
	}
	
	/**
	 * Returns true if the game is paused and was paused manually by a player
	 * and not automatically due to something happening.
	 * 
	 * @return
	 */
	public static boolean wasPausedByPlayer() {	
		return  gameState.playerPaused;
	}
	
	/**
	 * This will return an instance of the Random.
	 * If the instance does not exist yet, a new one
	 * will be created with a random seed.
	 * 
	 * @return
	 */
	public static Random getRandomGenerator() {
		return gameState.getRandom();
	}
	
	/**
	 * Returns a new instance of an item with the supplied id.
	 * @param itemId
	 * @return
	 */
	public static InventoryItem getItem(String itemId) {
		return InventoryItem.getItem(itemId);
	}
	
	/**
	 * Returns the first found GameObject with the specified id.
	 * 
	 * If the object is already loaded and is global or belongs to the current
	 * map, it is returned fully constructed.
	 * 
	 * If the object belongs to a map previously visited but not currently 
	 * loaded, it is returned, but with assets disposed.
	 * 
	 * If the object isn't loaded, or is not global or part of a known map
	 * map, null is returned.
	 * 
	 * @param internalId
	 * @param classes - if supplied, the game object will not only have to have
	 * the supplied id, but also be of one of the supplied classes, or inherit
	 * from one of them
	 * @return
	 */
	public static GameObject getGameObjectById(String id, Class<?>... classes) {
		return gameState.getGameObjectById(id, false, classes);
	}
	
	/**
	 * Returns the GameObject with the specified internal id.
	 * 
	 * If the object is already loaded and is global or belongs to the current
	 * map, it is returned fully constructed.
	 * 
	 * If the object belongs to a map previously visited but not currently 
	 * loaded, it is returned, but with assets disposed.
	 * 
	 * If the object isn't loaded, or is not global or part of a known map
	 * map, null is returned.
	 * 
	 * @param internalId
	 * @return
	 */
	public static GameObject getGameObjectByInternalId(String internalId) {
		return gameState.getGameObjectById(internalId, true);
	}

	/**
	 * Gets all game objects of the supplied type that belong to the currently displayed map.
	 * 
	 * @param type
	 * @return
	 */
	public static Array<GameObject> getGameObjectsOfType(String type) {
		return getGameObjectsOfType(null, type);
	}
	
	/**
	 * Gets all game objects of the supplied type that belong to the map with the supplied id. If
	 * the id is null, the current map will be used instead.
	 * 
	 * The map must have been loaded already at some point (i.e. it must be "known" to the player)
	 * in order for this to return any results.
	 * 
	 * @param mapId
	 * @param type
	 * @return
	 */
	public static Array<GameObject> getGameObjectsOfType(String mapId, String type) {
		GameMap map = (GameMap) (mapId == null ? gameState.getCurrentMap() : gameState.getLocationByIternalId(mapId));
		if (map == null) {
			return new Array<GameObject>();
		}
		return map.getGameObjectsOfType(type, false);
	}
	
	/**
	 * Returns true if the player character group contains a player character
	 * with the same id as the one supplied.
	 * 
	 * @param playerCharacterId
	 * @return
	 */
	public static boolean isMemberOfPlayerGroup(String playerCharacterId) {
		return getPlayerCharacterGroup().containsPlayerCharacter(playerCharacterId);
	}

	@Override
	public Variables variables() {
		return variables;
	}
}

