package mg.fishchicken.gamelogic.locations;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.AttackAction;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.actions.PickUpAction;
import mg.fishchicken.gamelogic.actions.TalkToAction;
import mg.fishchicken.gamelogic.actions.UseGameObjectAction;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.inventory.ItemPile;
import mg.fishchicken.gamelogic.inventory.Pickable;
import mg.fishchicken.gamelogic.inventory.PickableGameObject;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.locations.transitions.Transition;
import mg.fishchicken.gamelogic.locks.TransitionLock;
import mg.fishchicken.gamelogic.time.Sun;
import mg.fishchicken.gamelogic.traps.TransitionTrap;
import mg.fishchicken.gamelogic.traps.TrapLocation;
import mg.fishchicken.gamelogic.weather.WeatherProfile;
import mg.fishchicken.gamestate.SaveablePolygon;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.graphics.Drawable;
import mg.fishchicken.graphics.ShapeDrawer;
import mg.fishchicken.graphics.TextDrawer;
import mg.fishchicken.graphics.particles.ParticleEffectManager;
import mg.fishchicken.graphics.renderers.FilledPolygonRenderer;
import mg.fishchicken.pathfinding.AStarPathFinder;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.PathableTiledMap;
import mg.fishchicken.pathfinding.TileBlocker;
import aurelienribon.tweenengine.TweenManager;
import box2dLight.Light;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class GameMap extends GameLocation implements PathableTiledMap, Disposable, SkillCheckModifier {

	public static final String STRING_TABLE = "maps."+Strings.RESOURCE_FILE_EXTENSION;
	public static final String XML_FOG_OF_WAR = "fogOfWar";
	public static final String XML_TRANSITION_LOCKS = "transitionLocks";
	public static final String XML_TRANSITION_TRAPS= "transitionTraps";
	public static final String XML_TRANSITION_LOCK = "transitionLock";
	public static final String XML_TRANSITION_TRAP = "transitionTrap";
	public static final String XML_WEATHER_MODIFIERS = "weatherModifiers";
	public static final String LAYER_SPECIAL = "Special";
	
	private static Matrix4 isoTransform;
	private static Matrix4 invIsoTransform;
	
	static {
		isoTransform = new Matrix4();
		isoTransform.idt();
		isoTransform.translate(0.0f, 0.5f, 0.0f);
		isoTransform.scale((float) (Math.sqrt(2.0)),
				(float) (Math.sqrt(2.0) / 2.0), 1.0f);
		isoTransform.rotate(0.0f, 0.0f, 1.0f, -45.0f);
		invIsoTransform = new Matrix4(isoTransform);
		invIsoTransform.inv();
	}
	
	// these are all package visible
	// because they are directly accessed by the GameMapLoader
	boolean[] blockedTiles;
	boolean[] unavailableTiles;
	float[] moveCosts;
	Array<TiledMapObject> mapTileObjects;
	Array<Drawable> drawables;
	Array<TileBlocker> blockers;
	Array<GameObject> gameObjects;
	ObjectMap<String, Array<GameObject>> gameObjectsByType;
	ObjectMap<Class<?>, Array<GameObject>> gameObjectsByClass;
	private TiledMap tiledMap; // lazy init!
	private float tileSizeX, tileSizeY;
	TiledMapTileLayer colisionLayer; // lazy init, is initialized at the same time as tiledMap
	ObjectMap<Vector2, Transition> transitionTiles;
	Array<ShapeDrawer> shapeDrawers;
	Array<TransitionLock> transitionLocks;
	Array<TransitionTrap> transitionTraps;
	
	protected Array<GameLocation> locations;
	private ObjectMap<TrapLocation, FilledPolygonRenderer> traps;
	private boolean isIsometric;
	private boolean isWorldMap;
	private boolean isInterior;
	private boolean isDisposed;
	private float s_sunlightMultiplier;
	private boolean s_startsRevealed;
	private String s_mapGroup;
	private Array<WeatherProfile> weatherProfileModifiers;
	
	private String gridTextureFile;
	private String transitionTextureFile;
	private String solitTileTextureFile;
	private TextureRegion gridTexture;
	private TextureRegion transitionTexture;
	private TextureRegion solidTileTexture;
	
	private Array<TextDrawer> textDrawers;
	private Vector2 startCoordinates;
	private Array<TiledMapTileLayer> groundLayers, overheadLayers, alwaysAboveLayers; // lazy init, is initialized at the same time as tiledMap
	private AStarPathFinder pathFinder;
	private AssetMap myAssets; //contains all assets this map controls, including assets of all objects on the map

	private boolean renderGrid;
	private RayHandler fogOfWarRayHandler, lightsRayHandler, viewConesRayHandler;
	private Sun sun;
	private int[] fogOfWar;
	private ObjectSet<GameObject>[] gameObjectTileMap;
	private World fogOfWarWorld;
	private World lightsWorld;
	private Box2DDebugRenderer box2DDebugRenderer;
	private Camera camera;
	private boolean mapLoaded;
	private TweenManager tweenManager, unpausableTweenManager;
	private ParticleEffectManager particleEffectManager;
	private Vector3 tempVector;
	private ObjectSet<GameObject> tempSet;
	private ObjectSet<GameLocation> tempLocations;

	/**
	 * Empty constructor for game loading.
	 */
	public GameMap() {
		super();
		init();
	}
	
	public GameMap(String id) {
		super(id, id, null);
		init();
	}
	
	private void init() {
		tempVector = new Vector3();
		tempSet = new ObjectSet<GameObject>();
		s_sunlightMultiplier = 1f;
		drawables = new Array<Drawable>();
		blockers = new Array<TileBlocker>();
		textDrawers = new Array<TextDrawer>();
		myAssets = new AssetMap();
		gameObjects = new Array<GameObject>(true, 16, GameObject.class);
		gameObjectsByType = new ObjectMap<String, Array<GameObject>>();
		gameObjectsByClass = new ObjectMap<Class<?>, Array<GameObject>>();
		locations =  new Array<GameLocation>();
		tempLocations =  new ObjectSet<GameLocation>();
		traps = new ObjectMap<TrapLocation, FilledPolygonRenderer>();
		groundLayers = new Array<TiledMapTileLayer>();
		overheadLayers = new Array<TiledMapTileLayer>();
		alwaysAboveLayers = new Array<TiledMapTileLayer>();
		transitionTiles = new ObjectMap<Vector2, Transition>();
		shapeDrawers = new Array<ShapeDrawer>();
		weatherProfileModifiers = new Array<WeatherProfile>();
		transitionLocks = new Array<TransitionLock>();
		transitionTraps = new Array<TransitionTrap>();
		mapTileObjects = new Array<TiledMapObject>(); 
		renderGrid = false;
		isDisposed = false;
		tweenManager = new TweenManager();
		unpausableTweenManager = new TweenManager();
		
		RayHandler.useDiffuseLight(true);
		
		fogOfWarWorld = new World(new Vector2(0, 0), true);
		lightsWorld =  new World(new Vector2(0, 0), true);
		box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true, true);
		createFogOfWarRayHandler();
		createLightsRayHandler();
		createViewConesRayHandler();
		mapLoaded = false;
		super.setMap(null);
	}
	
	/**
	 * Sets the dimensions of the map. This must be called at least once before the map can be used.
	 * @param width
	 * @param height
	 */
	void setDimensions(int width, int height) {
		SaveablePolygon polygon = new SaveablePolygon(new float[] {0, 0,  width, 0,  width,  height, 0,  height});
		polygon.setPosition(0, 0);
		setPolygon(polygon);
	}
	
	/**
	 * Gets this map. This is pointless to call on game maps, but needs
	 * to be implemented for the AudioOriginator interface.
	 */
	@Override
	public GameMap getMap() {
		return this;
	}
	
	/**
	 * This does nothing for GameMaps.
	 */
	@Override
	public void setMap(GameMap map) {
		// NOOP since you cannot set a map to belong to a different map
	}
	
	/**
	 * Width of the map in tiles.
	 */
	public int getMapWidth() {
		return (int) super.getWidth();
	}
	
	/**
	 * Height of the map in tiles. 
	 */
	public int getMapHeight() {
		return (int) super.getHeight();
	}
	
	/**
	 * Marks the map as loaded.
	 */
	void setMapLoaded() {
		mapLoaded = true;
	}
	
	/**
	 * Returns true if the map has been fully loaded into memory
	 * and all objects it requires have been created.
	 * @return
	 */
	public boolean isMapLoaded() {
		return mapLoaded;
	}
	
	public void notifyGOTileChanged(GameObject go, int newX, int newY, int oldX, int oldY) {
		if (gameObjectTileMap == null) {
			recalculateGameObjectTileMap();
		}
		double goWidth = Math.ceil(go.getWidth());
		double goHeight = Math.ceil(go.getHeight());
		for(int i = 0; i < goWidth; ++i) {
			for(int j = 0; j < goHeight; ++j) {
				if (oldX != -1) {
					gameObjectTileMap[getTileId(oldX+i, oldY+j)].remove(go);
				}
				gameObjectTileMap[getTileId(newX+i, newY+j)].add(go);
			}
		}
		
	}
	
	/**
	 * Called when the camera has moved.
	 */
	public void cameraMoved() {
		for (Light light : lightsRayHandler.lightList) {
			light.makeDirty();
		}
	}
	
	/**
	 * Called when this map is current and is going to be switched for another map.
	 */
	public void currentMapWillChange() {
		// first kill all ongoing tweens
		tweenManager.killAll();
		unpausableTweenManager.killAll();
		
		// get rid of removable GOs
		Array<GameObject> gosToRemove = new Array<GameObject>();
		for (GameObject go : gameObjects) {
			if (go.isMapRemovable()) {
				gosToRemove.add(go);
			}
		}
		
		for (GameObject go : gosToRemove) {
			removeGameObject(go);
		}
	}
	
	void setTiledMap (TiledMap map) {
		this.tiledMap = map;
		MapProperties props = map.getProperties();
		this.tileSizeX = Float.valueOf(props.get("tilewidth").toString());
		this.tileSizeY = Float.valueOf(props.get("tileheight").toString());
		this.isIsometric = "isometric".equals(props.get("orientation", String.class));
		if (isIsometric) {
			tileSizeX /= 2;
		}
		this.isWorldMap = props.get("worldMap", String.class) != null;
		this.isInterior = props.get("interior", String.class) != null;
	}
	
	/**
	 * Gets the raw TiledMap associated with this GameMap.
	 * 
	 * @return
	 */
	public TiledMap getTiledMap() {
		return tiledMap;
	}
	
	/**
	 * Called when the map is loaded, before it is displayed.
	 */
	public void onLoad() {
		recalculateGameObjectTileMap();
		updateCharacterVisibleArea();
	}
	
	/**
	 * Returns the coordinates where the PCs should
	 * appear when they enter this map.
	 * 
	 * @return
	 */
	public Vector2 getStartCoordinates() {
		if (startCoordinates == null) {
			MapLayer npcLayer = tiledMap.getLayers().get(LAYER_SPECIAL);
			if (npcLayer != null && npcLayer.getObjects().getCount() > 0) {
				MapObject startLocation = npcLayer.getObjects().get(0);
				if (startLocation instanceof EllipseMapObject) {
					Ellipse center = ((EllipseMapObject)startLocation).getEllipse();
					startCoordinates = new Vector2((int)(center.x/getTileSizeX()), (int)(center.y/getTileSizeY()));
				}
			}
		}
		return startCoordinates;
	}
	
	/**
	 * Returns the orientation the PCs should
	 * have when they enter this map.
	 * 
	 * Default implementation just returns null
	 * since we don't care and like to live like a rebel.
	 * 
	 * @return
	 */
	public Orientation getStartOrientation() {
		return null;
	}
	
	/**
	 * Set the coordinates where the PCs should
	 * appear when a game is started at this map.
	 * 
	 */
	public void setStartCoordinates(float startX, float startY) {
		if (startCoordinates == null) {
			startCoordinates = new Vector2();
		}
		startCoordinates.set(startX, startY);
	}
	
	/**
	 * Gets the x tile size in pixels. This is the size of the 
	 * sides of the tile parallel with the X axis as if they were projected
	 * in orthogonal projection.
	 * 
	 * @return
	 */
	public float getTileSizeX() {
		return tileSizeX;
	}
	
	/**
	 * Gets the y tile size in pixels. This is the size of the 
	 * sides of the tile parallel with the Y axis as if they were projected
	 * in orthogonal projection.
	 * 
	 * @return
	 */
	public float getTileSizeY() {
		return tileSizeY;
	}
	
	/**
	 * Gets the x scale of the map.
	 * A ratio between pixels and x tile size.
	 * 
	 * @return
	 */
	public float getScaleX() {
		return 1f / getTileSizeX();
	}
	
	/**
	 * Gets the y scale of the map.
	 * A ratio between pixels and y tile size.
	 * 
	 * @return
	 */
	public float getScaleY() {
		return 1f / getTileSizeY();
	}
	
	/**
	 * Geths the weather modifiers for this map.
	 * @return
	 */
	public Array<WeatherProfile> getWeatherModifiers() {
		return weatherProfileModifiers;
	}
	
	/**
	 * Get the layers that are considered ground
	 * for the purpose of rendering and pathfinding.
	 * 
	 * @return
	 */
	public Array<TiledMapTileLayer> getGroundLayers() {
		return groundLayers;
	}
	
	/**
	 * Get the layers that are considered overhead
	 * for the purpose of rendering.
	 * 
	 * @see GameMapLoader#PROPERTY_OVERHEAD_LAYER
	 */
	public Array<TiledMapTileLayer> getOverheadLayers() {
		return overheadLayers;
	}
	
	/**
	 * Get the layers that are considered always above
	 * for the purpose of rendering.
	 * 
	 * @see GameMapLoader#PROPERTY_ALWAYSABOVE_LAYER
	 */
	public Array<TiledMapTileLayer> getAlwaysAboveLayers() {
		return alwaysAboveLayers;
	}
	
	/**
	 * Adds the supplied GameObject to this map.
	 * 
	 * If the GO is Drawable, it will get rendered with the map.
	 * @param go
	 */
	public void addGameObject(GameObject go) {
		if (!gameObjects.contains(go,false)) {
			if (go instanceof Pickable || go instanceof ItemPile) {
				if (handlePickablesAndItemPiles(go)) {
					return;
				}
			}
			
			gameObjects.add(go);
			if (go instanceof Drawable) {
				addDrawable((Drawable)go);
			}
			if (go instanceof TileBlocker) {
				blockers.add((TileBlocker)go);
			}
			if (go instanceof TextDrawer) {
				textDrawers.add((TextDrawer)go);
			} if (go instanceof ShapeDrawer) {
				shapeDrawers.add((ShapeDrawer)go);
			}
			
			Array<GameObject> byType = null;
			String goType = go.getType().toLowerCase(Locale.ENGLISH);
			if (gameObjectsByType.containsKey(goType)) {
				byType = gameObjectsByType.get(goType);
			} else {
				byType = new Array<GameObject>();
				gameObjectsByType.put(goType, byType);
			}
			byType.add(go);
			
			Array<GameObject> byClass = null;
			Class<?> goClass = go.getClass();
			if (gameObjectsByClass.containsKey(goClass)) {
				byClass = gameObjectsByClass.get(goClass);
			} else {
				byClass = new Array<GameObject>();
				gameObjectsByClass.put(goClass, byClass);
			}
			byClass.add(go);
			
			Tile tile = go.position().tile();
			notifyGOTileChanged(go, tile.getX(), tile.getY(), -1, -1);
		}
	}
	
	/**
	 * Adds the supplied Drawable to this map.
	 * 
	 * @param drawable
	 */
	public void addDrawable(Drawable drawable) {
		drawables.add(drawable);
	}
	
	/**
	 * Removes the supplied Drawable from this map.
	 * @param drawable
	 */
	public void removeDrawable(Drawable drawable) {
		drawables.removeValue(drawable, false);
	}
	
	/**
	 * Only one pickable or ItemPile can belong to a given tile. If another one would
	 * be placed on the same tile, it is instead combined with the existing pickable / container.
	 * 
	 * @param go
	 * @return true if the new GO was combined and does not need to be added to the map
	 */
	private boolean handlePickablesAndItemPiles(GameObject go) {
		Tile tile = go.position().tile();
		ObjectSet<GameObject> existingGOs = getAllGameObjectsAt(tile.getX(), tile.getY(), true, Pickable.class, ItemPile.class);
		if (existingGOs.size == 1) {
			GameObject existingGO =  existingGOs.iterator().next();
			if (existingGO instanceof Pickable) {
				Pickable existingPickable = (Pickable)existingGO;
				if (go instanceof Pickable) {
					createNewPileFromPickable(existingGO, ((Pickable)go).getInventoryItem());
					go.remove();
					return true;
				} else {
					((ItemPile)go).getInventory().addItem(existingPickable.getInventoryItem());
					existingGO.remove();
				}
			} else if (existingGO instanceof ItemPile) {
				if (go instanceof Pickable) {
					((ItemPile)existingGO).getInventory().addItem(((Pickable)go).getInventoryItem());
					go.remove();
					return true;
				} else {
					((ItemPile)existingGO).getInventory().moveAllItems(((ItemPile) go).getInventory());
					existingGO.remove();
				}
			}
		}
		return false;
	}
	
	private void createNewPileFromPickable(GameObject pickable, InventoryItem itemToAdd) {
		try {
			ItemPile newPile = new ItemPile(pickable.getInternalId()+"Pile", pickable.position());
			newPile.getInventory().addItem(itemToAdd);
			newPile.getInventory().addItem(((Pickable)pickable).getInventoryItem());
			pickable.remove();
			newPile.setMap(this);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	/**
	 * Removes the supplied GameObject from this map.
	 * 
	 * The GO will no longer be rendered.
	 * 
	 * @param go
	 */
	public void removeGameObject(GameObject go) {
		gameObjects.removeValue(go, false);
		if (go instanceof Drawable) {
			removeDrawable((Drawable)go);
		}
		if (go instanceof TextDrawer) {
			textDrawers.removeValue((TextDrawer)go, false);
		}
		if (go instanceof TileBlocker) {
			blockers.removeValue((TileBlocker)go, false);
		}
		if (gameObjectsByType.containsKey(go.getType())) {
			gameObjectsByType.get(go.getType()).removeValue(go, false);
		}
		if (gameObjectsByClass.containsKey(go.getClass())) {
			gameObjectsByClass.get(go.getClass()).removeValue(go, false);
		}
		if (gameObjectTileMap != null) {
			for (ObjectSet<GameObject> objects : gameObjectTileMap) {
				objects.remove(go);
			}
		}
		
	}
	
	/**
	 * Returns the ID of a combat map that the game should switch to if 
	 * combat occurs on the supplied tile.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String getCombatMapId(int x, int y) {
		String mapId = null;
		// first check if any locations are on the tile and if they are,
		// if any of the define combat maps
		tempLocations.clear();
		getLocationsAt(tempLocations, x, y);
		for (GameLocation location : tempLocations) {
			mapId = location.getCombatMapId();
			if (mapId != null) {
				break;
			}
		}
		
		// if we did not get anything from child locations, 
		// try it from ourselves
		if (mapId == null) {
			mapId = getCombatMapId();
		}
		return mapId;
	}
	
	public void addLocation(GameLocation loc) {
		if (!locations.contains(loc, false)) {
			locations.add(loc);
		}
		if (loc instanceof TrapLocation) {
			traps.put((TrapLocation)loc, ((TrapLocation)loc).createRenderer());
		}
	}
	
	public boolean removeLocation(GameLocation loc) {
		boolean returnValue = locations.removeValue(loc, false);
		if (returnValue && (loc instanceof TrapLocation)) {
			traps.remove((TrapLocation)loc);
		}
		return returnValue;
	}
	
	public void getTrapsToDraw(ObjectMap<TrapLocation, FilledPolygonRenderer> returnValue, Rectangle cullingRectangle) {
		returnValue.clear();
		for (Entry<TrapLocation, FilledPolygonRenderer> entry : traps.entries()) {
			if (!entry.key.getTrap().isDisarmed() && entry.key.getTrap().isDetected()) {
				// TODO: this is not exactly precise and would not work
				// if the trap was larger than the culling rectangle without any
				// vertices in it 
				if (MathUtil.containsAnyVertex(cullingRectangle, entry.key.s_polygon)) {
					returnValue.put(entry.key, entry.value);
				}
			}
		}
	}
	
	/**
	 * Returns the Location that belongs to this map
	 * and that contains coordinates x, y in its bounding
	 * rectangle.
	 * 
	 * x and y must be positive, otherwise an empty Array
	 * is returned.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public void getLocationsAt(ObjectSet<GameLocation> returnValue, int x, int y) {
		if (x > 0 && y > 0) {
			for (GameLocation location : locations) {
				if (location.contains(x, y)) {
					returnValue.add(location);
				}
			}
		}
	}
	
	/**
	 * Returns the first not disarmed TrapLocation that belongs to this map
	 * and that contains coordinates x, y in its bounding
	 * rectangle.
	 * 
	 * x and y must be positive, otherwise null is returned.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public TrapLocation getTrapLocationAt(float x, float y) {
		if (x > 0 && y > 0) {
			for (TrapLocation location : traps.keys()) {
				if (!location.getTrap().isDisarmed() && location.contains(x, y)) {
					return location;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the first not disarmed and detected TrapLocation that belongs to this map
	 * and that contains coordinates x, y in its bounding
	 * rectangle.
	 * 
	 * x and y must be positive, otherwise null is returned.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public TrapLocation getDetectedTrapLocationAt(float x, float y) {
		if (x > 0 && y > 0) {
			for (TrapLocation location : traps.keys()) {
				if (!location.getTrap().isDisarmed() && location.getTrap().isDetected() && location.contains(x, y)) {
					return location;
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets the enemy group that should represent the random encounter for the supplied
	 * coordinates and level, if any.
	 * 
	 * @param x
	 * @param y
	 * @param level
	 * @return
	 */
	public CharacterGroup getRandomEncounter(Tile tile, int level) {
		Array<CharacterGroup> viableGroups = new Array<CharacterGroup>();
		int dangerousness =  getDangerousness();
		tempLocations.clear();
		getLocationsAt(tempLocations, tile.getX(), tile.getY());
		for (GameLocation loc : tempLocations) {
			if (loc.getOwnerFaction().isHostileTowardsPlayer()) {
				viableGroups.addAll(loc.getRandomEncounterGroups());
				if (dangerousness < loc.getDangerousness()) {
					dangerousness = loc.getDangerousness();
				}
			}
		}
		
		if (getOwnerFaction().isHostileTowardsPlayer()) {
			viableGroups.addAll(getRandomEncounterGroups());
		}
		
		if (viableGroups.size < 1) {
			return null;
		}
		
		int chance = Configuration.getRandomEncountersBaseChance() + dangerousness;
		if (GameState.getRandomGenerator().nextInt(100) >= chance) {
			return null;
		}
				
		Iterator<CharacterGroup> iterator = viableGroups.iterator();
		
		int levelTolerance = Configuration.getRandomEncountersLevelTolerance();
		
		while (iterator.hasNext()) {
			CharacterGroup group = iterator.next();
			int groupLevel = group.getLevel();
			if (groupLevel >= 0 && (groupLevel < level-levelTolerance || groupLevel > level+levelTolerance)) {
				iterator.remove();
			}
		}
		
		try {
			CharacterGroup encounter = viableGroups.random();
			if (encounter != null) {
				encounter = new CharacterGroup(encounter);
				encounter.setShouldBeSaved(false); // we do not save random encounters
			}
			return encounter;
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	/**
	 * Gets the activity modifier for the supplied tile.
	 * 
	 * This will check if any locations are on the tile and if so, average their
	 * modifiers (skipping those where the modifier is zero).
	 * 
	 * If there are none, the modifier of the map itself will be returned.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getActivityModifier(Tile tile, TerrainActivity activity) {
		return getActivityModifier(tile.getX(), tile.getY(), activity);
	}
	
	/**
	 * Gets the activity modifier for the supplied tile.
	 * 
	 * This will check if any locations are on the tile and if so, average their
	 * modifiers (skipping those where the modifier is zero).
	 * 
	 * If there are none, the modifier of the map itself will be returned.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getActivityModifier(int x, int y, TerrainActivity activity) {
		tempLocations.clear();
		getLocationsAt(tempLocations, x, y);
		
		float modifierSum = 0;
		float count = 0;
		
		for (GameLocation location : tempLocations) {
			int mod = location.getActivityModifier(activity);
			if (mod != 0) {
				modifierSum += mod;
				++count;
			}
		}
		
		if (count > 0) {
			return Math.round(modifierSum / count);
		}
		
		return getActivityModifier(activity);
	}
	
	/**
	 * Will try to play any camp music associated with the supplied coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return the music that started playing if any, null otherwise
	 */
	public AudioTrack<?> playCampMusic(int x, int y) {
		tempLocations.clear();
		getLocationsAt(tempLocations, x, y);
		
		Array<AudioTrack<?>> campMusic = new Array<AudioTrack<?>>();
		
		for (GameLocation location : tempLocations) {
			campMusic.addAll(location.getCampMusic());
		}
		
		if (campMusic.size < 1) {
			campMusic.addAll(getCampMusic());
		}
		
		AudioTrack<?> music = campMusic.random();
		
		if (music != null) {
			if (music.playIfRollSuccessfull()) {
				return music;
			}
		}
		return null;
	}
	
	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		if (Skill.HUNTING == skill) {
			return getActivityModifier(skillUser.position().tile(), TerrainActivity.HUNTING);
		} else if (Skill.SCOUTING == skill) {
			return getActivityModifier(skillUser.position().tile(), TerrainActivity.WATER_SEARCHING);
		} else if (Skill.SNEAKING == skill) {
			int modifier = getActivityModifier(skillUser.position().tile(), TerrainActivity.SNEAKING);
			Vector2 tempVector = projectFromTiles(skillUser.position().setVector2(MathUtil.getVector2()));
			if ((sun.isSet() || isInterior()) && lightsRayHandler.pointAtShadow(tempVector.x, tempVector.y)) {
				modifier += Configuration.getDarknessStealthModifier();
			}
			MathUtil.freeVector2(tempVector);
			return modifier;
		} else if (Skill.SWIMMING == skill) {
			return getActivityModifier(skillUser.position().tile(), TerrainActivity.SWIMMING);
		} else if (Skill.CLIMBING == skill) {
			return getActivityModifier(skillUser.position().tile(), TerrainActivity.CLIMBING);
		}
		return 0;
	}
	
	
	/**
	 * Marks the tile with the supplied coordinates
	 * as seen (discovered) by a player character.
	 * 
	 * @param x
	 * @param y
	 */
	public void markTileAsSeen(int x, int y) {
		fogOfWar[getTileId(x, y)] = 1;
	}
	
	/**
	 * Recalculates visibility of MapTileObjects
	 * on this map.
	 * 
	 * This will determine whether or not the
	 * MTO will be rendered and whether it will
	 * be under fog or not.
	 * 
	 * @param fieldOfView
	 * @return
	 */
	public void recalculateMTOVisibility() {
		for (TiledMapObject mto : mapTileObjects) {
			mto.recalculateVisibility();
		}
	}
	
	
	/** Updates the visible area for all
	 * NonPlayerCharacters on this map.
	 * 
	 * Warning, this can be an expensive call.
	 * 
	 * This only does anything if the map is fully loaded.
	 */
	public void updateCharacterVisibleArea() {
		updateCharacterVisibleArea(-1, -1, false);
	}
	
	/** 
	 * Updates the visible area for
	 * GameCharacters that have the supplied coordinates
	 * within their line of sight radius on this map.
	 * 
	 * Warning, this can be an expensive call.
	 * 
	 * This only does anything if the map is fully loaded.
	 * 
	 * If both x any y are negative, all characters are updated.
	 * 
	 * @param x
	 * @param y
	 */
	public void updateCharacterVisibleArea(float x, float y, boolean recalculateLOS) {
		if (!mapLoaded) {
			return;
		}
		for (GameObject go : gameObjects) {
			if (go instanceof GameCharacter) {
				if ((x < 0 && y < 0)
						|| (MathUtil.distance(x, y, go.position().getX(), go.position().getY()) <= (isWorldMap() ? Configuration
								.getSightRadiusWorld() : Configuration
								.getSightRadiusLocal()))) {
					((GameCharacter) go).updateVisibleArea(recalculateLOS);
				}
			}
		}
	}
	
	/**
	 * Returns true if the supplied tile is currently on screen.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isOnScreen(int x, int y) {
		camera.project(projectFromTiles(tempVector.set(x, y, 0)));
		return tempVector.x > 0 && tempVector.y > 0 && tempVector.x < Gdx.graphics.getWidth() && tempVector.y < Gdx.graphics.getHeight();
	}
	
	/**
	 * Returns true if the tile at the specified coordinates
	 * should be rendered.
	 * 
	 * Tiles are not rendered if they have not been
	 * discovered yet.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean shouldRenderTile(int x, int y) {
		return shouldRenderTile(x+y*getMapWidth());
	}
	
	/**
	 * Returns true if the specified tile 
	 * should be rendered.
	 * 
	 * @param tileNumber - the ID of the tile to check. Usually calculated as x + y*mapWidth
	 * @return
	 */
	public boolean shouldRenderTile(int tileNumber) {
		if (s_startsRevealed) {
			return true;
		}
		if (tileNumber < 0 || tileNumber >= fogOfWar.length) {
			return false;
		}
		return fogOfWar[tileNumber] != 0;
	}
	
	@Override
	protected boolean isOccupiedByPC() {
		return gameState.getCurrentMap() == this;
	}
	
	/**
	 * Returns true if the supplied tile coordinate
	 * is directly visible to any PC.
	 * @param tile
	 * @return
	 */
	public boolean isTileVisibleToPC(Tile tile) {
		return isTileVisibleToPC(tile.getX(), tile.getY());
	}
	
	/**
	 * Returns true if the supplied tile coordinate
	 * is directly visible to any PC.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isTileVisibleToPC(int x, int y) {
		return isTileVisibleToPC(getTileId(x, y));
	}

	/**
	 * Returns true if any character in this group can see map geometry (for
	 * example building tiles, trees, and so on) on the supplied coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isGeometryVisibleToPC(int x, int y) {
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		for (GameCharacter member : pcg.getPlayerCharacters()) {
			if (member.canSeeGeometry(x, y)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the supplied tile
	 * is directly visible to any member of this group on the same map.
	 * 
	 * @param tileNumber - the ID of the tile to check. Usually calculated as x + y*mapWidth
	 * @param - the PC whose LOS we want to check - if null, al members of this group are checked
	 * @return
	 */
	public boolean isTileVisibleToPC(int tileNumber) {
		if (!shouldRenderTile(tileNumber)) {
			return false;
		}
		if (isWorldMap) {
			return GameState.getPlayerCharacterGroup().getGroupGameObject().canSeeTile(tileNumber);
		}
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		for (GameCharacter member : pcg.getPlayerCharacters()) {
			if (this == member.getMap() && member.canSeeTile(tileNumber)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the TransitionLock on this map
	 * with the specified ID, or null
	 * if no such TransitionLock exists.
	 * 
	 * @param id
	 * @return
	 */
	public TransitionLock getTransitionLock(String id) {
		for (TransitionLock transitionLock : transitionLocks) {
			if (id.equalsIgnoreCase(transitionLock.getId())) {
				return transitionLock;
			}
		}
		return null;
	}
	
	/**
	 * Returns the TransitionTrap on this map
	 * with the specified ID, or null
	 * if no such TransitionTrap exists.
	 * 
	 * @param id
	 * @return
	 */
	public TransitionTrap getTransitionTrap(String id) {
		for (TransitionTrap transitionTrap : transitionTraps) {
			if (id.equalsIgnoreCase(transitionTrap.getId())) {
				return transitionTrap;
			}
		}
		return null;
	}
	
	/**
	 * Returns the transition that belongs to this map
	 * and that contains coordinates x, y in its shape.
	 * 
	 * Does not return door transitions.
	 * 
	 * @param tile
	 * @return
	 */
	public Transition getTransitionAt(Tile tile) {
		return getTransitionAt(tile.getX(), tile.getY());
	}
	
	/**
	 * Returns the transition that belongs to this map
	 * and that contains coordinates x, y in its shape.
	 * 
	 * Does not return door transitions.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Transition getTransitionAt(int x, int y) {
		Vector2 testVector = MathUtil.getVector2().set(x, y);
		Transition returnValue = transitionTiles.get(testVector);
		MathUtil.freeVector2(testVector);
		return returnValue;
	}
	
	/**
	 * Returns an array containing the coordinates of all transition tiles
	 * on the map.
	 * 
	 * @return
	 */
	public Keys<Vector2> getTransitionTileCoordinates() {
		return transitionTiles.keys();
	}
	
	/**
	 * Returns all active game objects of the supplied type.
	 * 
	 * @param the array which should be filled by the found GOs
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends GameObject> void getAllGameObjects(Array<T> returnValue, Class<T> type) {
		for (Class<?> goClass : gameObjectsByClass.keys()) {
			if (type.isAssignableFrom(goClass)) {
				Array<GameObject> gos = gameObjectsByClass.get(goClass);
				for (GameObject go : gos) {
					if (go.isActive()) {
						returnValue.add((T)go);
					}
				}
			}
		}
	}
	
	/**
	 * Returns all game objects of the specified type that 
	 * exist on the game map. Inactive GOs are included 
	 * based on the includeInactive parameter.
	 * 
	 * Modifying the returned array will do nothing.
	 * 
	 * The type is case insensitive.
	 * 
	 * @param type
	 * @return
	 */
	public Array<GameObject> getGameObjectsOfType(String type, boolean includeInactive) {
		Array<GameObject> returnValue = gameObjectsByType.get(type.toLowerCase(Locale.ENGLISH));
		if (returnValue == null) {
			returnValue = new Array<GameObject>();
		}
		
		if (!includeInactive) {
			Iterator<GameObject> objects = returnValue.iterator();
			while (objects.hasNext()) {
				if (!objects.next().isActive()) {
					objects.remove();
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns the game object belonging to this map
	 * with the specified id. Inactive GOs are included.
	 * 
	 * If no such GO exists, null is returned.
	 * @param id
	 * @param useInternal if supplied, internal IDs will be used instead of IDs
	 * @param classes is supplied, the GO will be only returned if it has or inherits
	 * from one of the supplied classes
	 * @return
	 */
	public GameObject getGameObject(String id, boolean useInternal, Class<?>... classes) {
		boolean noTypes = classes.length < 1;
		for (GameObject go : gameObjects) {
			if ((useInternal && id.equalsIgnoreCase(go.getInternalId())) || id.equalsIgnoreCase(go.getId())) {
				if (noTypes) {
					return go;
				}
				for (Class<?> clazz : classes) {
					if (clazz.isAssignableFrom(go.getClass())) {
						return go;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the GameObject that belongs to this map and that contains
	 * coordinates x, y in its bounding rectangle. Only one game object of one of
	 * the specified types is returned if there are multiple on the give tile.
	 * 
	 * If no types are specified, any GO found is returned.
	 * 
	 * Does not return inactive GOs.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public GameObject getGameObjectAt(float x, float y, Class<?>... types) {
		return getAllGameObjectsAt(null, x, y, false, false, true, types);
	}
	
	/**
	 * Returns all GameObjects that belongs to this map and that contain
	 * coordinates x, y in their bounding rectangle. Only game objects of one of
	 * the specified types are returned.
	 * 
	 * If no types are specified, all GOs found are returned.
	 * 
	 * Does not return inactive GOs.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ObjectSet<GameObject> getAllGameObjectsAt(float x, float y, Class<?>... types) {
		return getAllGameObjectsAt(x, y, false, types);
	}
	
	/**
	 * Returns all GameObjects that belong to this map and that contain
	 * coordinates x, y in their bounding rectangle, or, if onTile, is true,
	 * that belong to the specified tile. Only game objects of one of
	 * the specified types are returned.
	 * 
	 * If no types are specified, all GOs found are returned.
	 * 
	 * Does not return inactive GOs.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ObjectSet<GameObject> getAllGameObjectsAt(float x, float y, boolean onTile, Class<?>... types) {
		ObjectSet<GameObject> returnValue = new ObjectSet<GameObject>();
		getAllGameObjectsAt(returnValue, x, y, onTile, false, types);
		return returnValue;
	}
	
	/**
	 * Returns all GameObjects that belongs to this map and that contain
	 * coordinates x, y in their bounding rectangle, or, if onTile, is true,
	 * that belongs to the specified tile. Only game objects of one of
	 * the specified types are returned.
	 * 
	 * If no types are specified, all GOs found are returned.
	 * 
	 * Will return inactive GOs if includeInactive is set to true.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public <T extends GameObject> void getAllGameObjectsAt(ObjectSet<T> returnValue, float x, float y, boolean onTile, boolean includeInactive, Class<?>... types) {
		getAllGameObjectsAt(returnValue, x, y, onTile, includeInactive, false, types);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends GameObject> GameObject getAllGameObjectsAt(ObjectSet<T> returnValue, float x, float y, boolean onTile, boolean includeInactive, boolean returnFirst,  Class<?>... types) {
		if (gameObjectTileMap == null) {
			return null;
		}
		
		GameObject firstFound = null;
		int min = onTile ? 0: -3;
		int max = onTile ? 0: 3;
		int maxLength = gameObjectTileMap.length;
		int intX = (int) x;
		int intY = (int) y;
		boolean noTypes = types.length == 0;
		
		for (int i = min; i <= max; ++i) {
			for (int j = min; j <= max; ++j) {
				int id = getTileId(intX+i, intY+j);
				if (id < 0 || id >= maxLength) {
					continue;
				}
				for (GameObject go : gameObjectTileMap[id]) {
					if ((includeInactive || go.isActive())
							&& (onTile || go.contains(x, y))) {
						Class<?> goClass = go.getClass();
						boolean canAdd = noTypes;
						for (Class<?> type : types) {
							if (type.isAssignableFrom(goClass)) {
								canAdd = true;
								break;
							}
						}
						if (canAdd) {
							if (firstFound == null) {
								firstFound = go;
							}
							if (returnValue != null) {
								returnValue.add((T)go);
							}
							if (returnFirst) {
								return go;
							}
						}
					}
				}
			}
		}
		
		return firstFound;
	}

	/**
	 * Returns a set of all GOs that are currently located in the supplied
	 * area. The are is a list of x,y coordinates in the following format:
	 * x1,y1,x2,y2,x3,y3,...
	 * 
	 * Does not return inactive GOs.
	 * @param <T>
	 * 
	 * @param area
	 * @return
	 */
	public <T extends GameObject> boolean getAllObjectsInArea(ObjectSet<T> returnValue, PositionArray area, Class<?>... types) {
		return getAllObjectsInArea(returnValue, area, false, types);
	}
	

	/**
	 * Returns a set of all GOs that are currently located in the supplied
	 * area. The are is a list of x,y coordinates in the following format:
	 * x1,y1,x2,y2,x3,y3,...
	 * 
	 * Will return inactive GOs if includeInactive is set to true.
	 * 
	 * @param area
	 * @return
	 */
	public <T extends GameObject> boolean getAllObjectsInArea(ObjectSet<T> returnValue, PositionArray area, boolean includeInactive, Class<?>... types) {
		boolean foundAny = false;
		for (int i = 0; i < area.size(); ++i) {
			int xc = area.getX(i);
			int yc = area.getY(i);
			foundAny = (getAllGameObjectsAt(returnValue, xc+0.5f, yc+0.5f, false, includeInactive, false, types) != null) || foundAny;
		}
		return foundAny;
	}
	
	/**
	 * Returns all Drawables that this map contains.
	 * 
	 * @return
	 */
	public Array<Drawable> getDrawables() {
		return drawables;
	}
	
	/**
	 * Gets all TextDrawers that this map contains.
	 * @return
	 */
	public Array<TextDrawer> getTextDrawers() {
		return textDrawers;
	}
	
	/**
	 * Gets all shape drawers that this map contains.
	 * @return
	 */
	public Array<ShapeDrawer> getShapeDrawers() {
		return shapeDrawers;
	}

	/**
	 * Returns the number by which any sunlight in this area should
	 * be multiplied. 
	 * @return
	 */
	public float getSunlightMultiplier() {
		return s_sunlightMultiplier;
	}
	
	/**
	 * Returns the name of the grup this map belongs to, or null
	 * if it does not belong to any.
	 * 
	 * Maps within the same group are not disposed when the player moves
	 * from one to the other.
	 * @return
	 */
	public String getMapGroup() {
		return s_mapGroup;
	}
	
	/**
	 * Whether or not this map uses isometric projection.
	 * 
	 * @return
	 */
	public boolean isIsometric() {
		return isIsometric;
	}
	
	
	/**
	 * Whether or not this map is a world (overland) map.
	 * @return
	 */
	public boolean isWorldMap() {
		return isWorldMap;
	}
	
	/**
	 * Whether or not this map is a combat map.
	 * @return
	 */
	public boolean isCombatMap() {
		return false;
	}
	
	/**
	 * Returns true if this map is currently displayed to the player.
	 * @return
	 */
	public boolean isCurrentMap() {
		return this.equals(gameState.getCurrentMap());
	}
	
	
	/**
	 * Returns the multiplier that real world time
	 * should be multiplied by in order to get game time.
	 * 
	 * It is usually different on world maps and local maps.
	 * @return
	 */
	public int getGameTimeMultiplier() {
		return isWorldMap() ? Configuration
				.getWorldGameTimeMultiplier() : Configuration
				.getLocalGameTimeMultiplier();
	}
	
	/**
	 * Whether or not this map is an interior map.
	 * 
	 * Interior maps do not render weather.
	 * 
	 * @return
	 */
	public boolean isInterior() {
		return isInterior;
	}
	
	/**
	 * Turns the grid on or off depending on
	 * its current state.
	 * 
	 */
	public void toggleGrid() {
		renderGrid = !renderGrid;
	}
	
	/**
	 * Sets whether or not we should render the
	 * grid on this map.
	 * 
	 * @param value
	 */
	public void setRenderGrid(boolean value) {
		renderGrid = value;
	}
	
	/**
	 * Whether or not we should render the grid on
	 * this map.
	 * 
	 * @return
	 */
	public boolean getRenderGrid() {
		return renderGrid;
	}
	
	/**
	 * Gets the texture that should be used to display the grid of this map.
	 * @return
	 */
	public TextureRegion getGridTexture() {
		if (gridTexture == null) {
			gridTexture = Assets.getTextureRegion(gridTextureFile);
		}
		return gridTexture;
	}
	
	/**
	 * Gets the texture that should be used to display the transitions of this map.
	 * @return
	 */
	public TextureRegion getTransitionTexture() {
		if (transitionTexture == null) {
			transitionTexture = Assets.getTextureRegion(transitionTextureFile);
		}
		return transitionTexture;
	}
	
	/**
	 * Gets the texture that should be used to render a tile of solid white color on this map.
	 * @return
	 */
	public TextureRegion getSolidTileTexture() {
		if (solidTileTexture == null) {
			solidTileTexture = Assets.getTextureRegion(solitTileTextureFile);
		}
		return solidTileTexture;
	}

	/**
	 * Gets the instance of the PathFinder associated with this map.
	 * @return
	 */
	public AStarPathFinder getPathFinder() {
		if (pathFinder == null) {
			pathFinder = new AStarPathFinder(this, gameState.getPlayerCharacterController(), Math.max(this.getMapHeight(), this.getMapWidth()), true);
		}
		return pathFinder;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	/**
	 * Finds and returns the path on this map from [fromX, fromY] to [toX, toY] that needs
	 * to be traversed by the supplied Mover.
	 * 
	 * The path will be empty if there is no valid path to the destination.
	 * @return
	 */
	public Path findPath(GameObject mover, float toX, float toY) {
		return getPathFinder().findPath(mover, (int)mover.position().getX(), (int)mover.position().getY(), (int)toX, (int)toY);
	}
	
	/**
	 * Returns true if the supplied tile is not available for pathfinding and
	 * should be completely ignored.
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	@Override
	public boolean tileUnavailable(int tx, int ty) {
		int tileId = getTileId(tx, ty);
		return tx < 0 || ty < 0 || tx >= getMapWidth() || ty >= getMapHeight() || unavailableTiles[tileId];
	}
	
	/**
	 * Check if the given location is blocked, i.e. blocks movement of 
	 * the supplied mover.
	 * 
	 * @param context The context describing the pathfinding at the time of this request
	 * @param tx The x coordinate of the tile we're moving to
	 * @param ty The y coordinate of the tile we're moving to
	 * @return True if the location is blocked
	 */
	@Override
	public boolean blocked(GameObject mover, int tx, int ty) {
		return blocked(mover, tx, ty, true, GameState.isCombatInProgress());
	}
	
	/**
	 * Check if the given location is blocked, i.e. blocks movement of 
	 * the supplied mover.
	 * 
	 * @param context The context describing the pathfinding at the time of this request
	 * @param tx The x coordinate of the tile we're moving to
	 * @param ty The y coordinate of the tile we're moving to
	 * @param unrevealedLogic if true, unrevelaed tiles will get special handling and will
	 * be considered unblocked if more that one tile away from PCs
	 * @param oneCharPerTile if true, only one character is allowed on each tile - this means
	 * all occupied tiles are considered blocked
	 * @return True if the location is blocked
	 */
	public boolean blocked(GameObject mover, int tx, int ty, boolean unrevealedLogic, boolean oneCharPerTile) {		
		// quick check if we are outside of the map
		if (tileUnavailable(tx, ty)) {
			return true;
		}
		
		int tileId = getTileId(tx, ty);
		if (mover instanceof GameCharacter) {
			if (unrevealedLogic && !s_startsRevealed && fogOfWar != null
					&& fogOfWar[tileId] == 0) {
				GameCharacter pc = (GameCharacter)mover;
				boolean pcMember = GameState.getPlayerCharacterGroup().containsPlayerCharacter(pc);
				boolean npcMember = !pcMember && GameState.getPlayerCharacterGroup().containsCharacter(pc);
				boolean combatInProgress = GameState.isCombatInProgress();
				// if the location is not revealed yet, it is considered unblocked for PCs
				// if it is more than 1 tile away and combat is not in progress
				if (!combatInProgress && pcMember && (Math.abs(pc.position().getX()-tx) > 1 && Math.abs(pc.position().getY()-ty) > 1)) {
					return false;
				} 
				
				// for NPCs that are part of the player group, not discovered tiles are unpassable
				// the same is also true for everyone during combat
				if (npcMember || combatInProgress) {
					return true;
				}
			}
			
			// for NPCs that are part of the player group, not visible tiles are unpassable
			/*this is currently disabled, because the char can end up in a not visible tile if all the PCs move and
			 * he would have no way of getting out of there - need to design around that
			 * if (npcMember && !Global.getPlayerCharacterGroup().isTileVisibleToPC(fovIndex)) {
				return false;
			}*/
		}
		
		// if we are blocked because our tiles are set as blocking, we are done
		if (blockedTiles[tileId]) {
			return true;
		}
		
		// check for any tile blockers that could be blocking us
		for (int i = 0; i < blockers.size; ++i) {
			TileBlocker blocker = blockers.get(i);
			if (!blocker.isBlockingPath()) {
				continue;
			}
			if (tx == (int)blocker.position().getX() && ty == (int)blocker.position().getY()) {
				return true;
			}
		}
		
		// during combat, only one character can occupy a given tile
		if (oneCharPerTile) {
			tempSet.clear();
			getAllGameObjectsAt(tempSet, tx, ty, true, false, GameCharacter.class);
			if ((!tempSet.contains(mover) && tempSet.size > 0) || tempSet.size > 1) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Finds a tile that is not blocked for the supplied mover
	 * around the supplied x, y position and in the supplied radius.
	 * 
	 * Will return x, y immediately in case that position is unblocked as well.
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector2 getUnblockedTile(int x, int y, int radius, GameObject mover, boolean oneCharPerTile, PositionArray tilesToIngore) {
		if (!blocked(mover, x, y, false, oneCharPerTile) && (tilesToIngore == null || !tilesToIngore.contains(x, y))) {
			return new Vector2(x, y);
		}
		
		for (int r = 1; r <= radius; ++r) {	
			for (int i = -r; i <= r; ++i) {
				int xt = x+i;
				int yt = y+r;
				if (!blocked(mover, xt, yt, false, oneCharPerTile) && (tilesToIngore == null || !tilesToIngore.contains(xt, yt))) {
					return new Vector2(xt, yt);
				}
				xt = x+r;
				yt = y+i;
				if (!blocked(mover, xt, yt, false, oneCharPerTile) && (tilesToIngore == null || !tilesToIngore.contains(xt, yt))) {
					return new Vector2(xt, yt);
				}
				xt = x+i;
				yt = y-r;
				if (!blocked(mover, xt, yt, false, oneCharPerTile) && (tilesToIngore == null || !tilesToIngore.contains(xt, yt))) {
					return new Vector2(xt, yt);
				}
				xt = x-r;
				yt = y+i;
				if (!blocked(mover, xt, yt, false, oneCharPerTile) && (tilesToIngore == null || !tilesToIngore.contains(xt, yt))) {
					return new Vector2(xt, yt);
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets all tiles unblocked in the specified radius around x, y for the supplied 
	 * mover and stored them in the supplied position array. The array is not cleared
	 * before being used!
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param mover
	 * @param destinationArray
	 */
	public void getUnblockedTiles(int x, int y, int radius, GameObject mover, boolean oneCharPerTile, PositionArray destinationArray) {
		if (!blocked(mover, x, y, false, oneCharPerTile)) {
			destinationArray.add(x, y);
		}
		
		for (int r = 1; r <= radius; ++r) {	
			for (int i = -r; i <= r; ++i) {
				int xt = x+i;
				int yt = y+r;
				if (!blocked(mover, xt, yt, false, oneCharPerTile)) {
					destinationArray.add(xt, yt);
				}
				xt = x+i;
				yt = y-r;
				if (!blocked(mover, xt, yt, false, oneCharPerTile)) {
					destinationArray.add(xt, yt);
				}
				xt = x+r;
				yt = y+i;
				if (!blocked(mover, xt, yt, false, oneCharPerTile)) {
					destinationArray.add(xt, yt);
				}
				xt = x-r;
				yt = y+i;
				if (!blocked(mover, xt, yt, false, oneCharPerTile)) {
					destinationArray.add(xt, yt);
				}
			}
		}
	}

	@Override
	public float getMoveCost(AbstractGameCharacter mover, int tx, int ty) {
		return moveCosts[getTileId(tx, ty)];
	}
	
	@Override
	public float getAPMoveCost(AbstractGameCharacter mover, int tx, int ty) {
		return 1;
	}
	
	/**
	 * Returns the class of the Action that the supplied action performer should perform on the
	 * supplied target or null if no valid action can be found.
	 * 
	 * @param actionPerformer
	 * @param target
	 * @return
	 */
	public Class<? extends Action> getActionForTarget(GameObject actionPerformer, GameObject target) {
		if (!gameState.getPlayerCharacterController().onlyMoveActionAllowed()) {
			if (target instanceof AbstractGameCharacter) {
				AbstractGameCharacter character = (AbstractGameCharacter) target;
				if (!GameState.isCombatInProgress() && character.canTalkTo(actionPerformer)
						&& actionPerformer.canPerformAction(TalkToAction.class)) {
					return TalkToAction.class;
				} else if (!isWorldMap() && character.isHostileTowardsPlayer() && actionPerformer.canPerformAction(AttackAction.class)) {
					return AttackAction.class;
				}
			} else if (target instanceof PickableGameObject && actionPerformer.canPerformAction(PickUpAction.class)) {
				return PickUpAction.class;
			} else if (target instanceof UsableGameObject
					&& actionPerformer.canPerformAction(UseGameObjectAction.class)
					&& ((UsableGameObject) target).isUsable()) {
				return UseGameObjectAction.class;
			} 
		}
		if (actionPerformer.canPerformAction( MoveToAction.class)) {
			return MoveToAction.class;
		}
		
		return null;
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		super.gatherAssets(assetStore);
		for (GameObject go : gameObjects) {
			if (go instanceof AssetContainer) {
				((AssetContainer)go).gatherAssets(assetStore);
			}
		}
		for (GameLocation gl : locations) {
			gl.gatherAssets(assetStore);
		}
		
		if (isWorldMap()) {
			GameState.getPlayerCharacterGroup().getGroupGameObject()
					.gatherAssets(assetStore);
		}
		
		GameState.getPlayerCharacterGroup().gatherAssets(assetStore);
		
		if (isIsometric) {
			gridTextureFile = Configuration.getFileIsometricMapGridTexture();
			transitionTextureFile = Configuration.getFileIsometricMapTransitionTexture();
			solitTileTextureFile = Configuration.getFileIsometricMapSolidWhiteTileTexture();
		} else {
			gridTextureFile = Configuration.getFileOrthogonalMapGridTexture();
			transitionTextureFile = Configuration.getFileOrthogonalMapTransitionTexture();
			solitTileTextureFile = Configuration.getFileOrthogonalMapSolidWhiteTileTexture();
		}
		myAssets.clear();
		myAssets.putAll(assetStore);		
	}
	
	/**
	 * Adds the supplied asset to this map.
	 * 
	 * This means the map will attempt to unload the asset when the map is disposed.
	 * 
	 * This will not actually load the asset! It is the responsibility of the
	 * caller to ensure the asset is properly loaded and does not get unloaded
	 * before the map is unloaded.
	 * 
	 * @param filename
	 * @param assetClass
	 */
	public void addAsset(String filename, Class<?> assetClass) {
		myAssets.put(filename, assetClass);
	}
	
	public RayHandler getLightsRayHandler() {
		return lightsRayHandler;
	}
	
	public World getLightsWorld() {
		return lightsWorld;
	}

	public RayHandler getViewConesRayHandler() {
		return viewConesRayHandler;
	}
	
	public RayHandler getFogOfWarRayHandler() {
		return fogOfWarRayHandler;
	}
	
	public World getFogOfWarWorld() {
		return fogOfWarWorld;
	}
	
	public Box2DDebugRenderer getBox2DDebugRenderer() {
		return box2DDebugRenderer;
	}

	/**
	 * Resets the fog of war array, 
	 * setting every tile to not seen.
	 */
	public void resetFogOfWar() {
		fogOfWar = new int[getMapWidth()*getMapHeight()];
		for (int i =0; i < fogOfWar.length; ++i) {
			fogOfWar[i] = 0;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void resetGameObjectTileMap() {
		gameObjectTileMap = new ObjectSet[getMapWidth()*getMapHeight()];
		for (int i =0; i < gameObjectTileMap.length; ++i) {
			gameObjectTileMap[i] = new ObjectSet<GameObject>();
		}
	}
	
	private void recalculateGameObjectTileMap() {
		resetGameObjectTileMap();
		for (GameObject go : gameObjects) {
			Tile tile = go.position().tile();
			notifyGOTileChanged(go, tile.getX(), tile.getY(), -1, -1);
		}
	}
	
	/**
	 * Completely unloads this GameMap.
	 * 
	 * This will first call dispose on the map 
	 * and then clear all internal references to
	 * GOs so that they can be garbage collected. 
	 */
	public void unload() {
		dispose();
		removeEverything();
	}
	
	@Override
	protected void stopSoundsOnExit() {
		for (AudioTrack<?> track : soundTracks.get(AMBIENT)) {
			track.stopTrack();
		}
		for (AudioTrack<?> track : soundTracks.get(ON_ENTRY)) {
			track.stopTrack();
		}
		for (AudioTrack<?> track : soundTracks.get(COMBAT)) {
			track.stopTrack();
		}
		for (AudioTrack<?> track : soundTracks.get(CAMP)) {
			track.stopTrack();
		}
	}
	
	/**
	 * This clears all internal references to
	 * GOs and locations so that they can be garbage collected. 
	 */
	protected void removeEverything() {
		gameObjects.clear();
		gameObjectsByClass.clear();
		gameObjectsByType.clear();
		drawables.clear();
		blockers.clear();
		locations.clear();
		mapTileObjects.clear();
		transitionLocks.clear();
		transitionTraps.clear();
		transitionTiles.clear();
		traps.clear();
	}
	
	/**
	 * Returns true if this map has been disposed. This means 
	 * that it will return true only if dispose() has been called on this map
	 * and undispose() has not yet been called afterwards.
	 * 
	 * Do not use this to determine if the map's assets are loaded! 
	 * New maps with unloaded assets will return false here.
	 * 
	 * @return
	 */
	public boolean isDisposed() {
		return isDisposed;
	}
	
	/**
	 * Disposes of this map.
	 * 
	 * Also unloads all assets used by this map.
	 */
	@Override
	public void dispose() {
		if (isDisposed) {
			return;
		}
		Music.stopPlayingMusic();
		tweenManager.killAll();
		unpausableTweenManager.killAll();
		
		if (particleEffectManager != null) {
			particleEffectManager.killAll();
			particleEffectManager = null;
		}
		fogOfWarRayHandler.dispose();
		fogOfWarRayHandler = null;
		lightsRayHandler.dispose();
		lightsRayHandler = null;
		viewConesRayHandler.dispose();
		viewConesRayHandler = null;
		box2DDebugRenderer.dispose();
		fogOfWarWorld.dispose();
		fogOfWarWorld = null;
		lightsWorld.dispose();
		lightsWorld = null;
		
		for (GameLocation location : locations) {
			location.clearAssetReferences();
		}
		
		for (GameObject go : gameObjects) {
			if (go instanceof Disposable) {
				((Disposable) go).dispose();
			} 
			if (go instanceof AssetContainer) {
				((AssetContainer) go).clearAssetReferences();
			}
		}
		
		if (isWorldMap()) {
			GameState.getPlayerCharacterGroup().getGroupGameObject().clearAssetReferences();
		}

		for (TiledMapObject mto : mapTileObjects) {
			drawables.removeValue(mto, false);
		}
		mapTileObjects.clear();

		if (mapLoaded) {
			for (Entry<String, Class<?>> entry: myAssets) {
				if (!Configuration.isGlobalAsset(entry.key)) {
					Assets.getAssetManager().unload(entry.key);
				}
			}
			myAssets.clear();

			if (transitionTexture != null) {
				transitionTexture = null;
			}
			if (gridTexture != null) {
				gridTexture = null;
			}
			if (solidTileTexture != null) {
				solidTileTexture = null;
			}
			Assets.getAssetManager().unload(Configuration.getFolderMaps()+getId()+".tmx");
		}
		isDisposed = true;
		mapLoaded = false;
	}
	
	/**
	 * Recreates all disposables.
	 */
	public void undispose() {
		if (!isDisposed) {
			return;
		}
		box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true, true);
		fogOfWarWorld = new World(new Vector2(0, 0), true);
		lightsWorld = new World(new Vector2(0, 0), true);
		createFogOfWarRayHandler();
		createLightsRayHandler();
		createViewConesRayHandler();
		
		isDisposed = false;
		
		for (int i = 0; i < gameObjects.size; ++i) {
			gameObjects.get(i).undispose();
		}
		
		for (Entry<TrapLocation, FilledPolygonRenderer> entry : traps.entries()) {
			traps.put(entry.key, entry.key.createRenderer());
		}
	}
	
	@Override
	public void update(float deltaTime, Camera camera) {
		sun.update(!isWorldMap());
		updateLocalGameObjects(deltaTime);
		super.update(deltaTime, camera);
		for (GameLocation loc : locations) {
			loc.update(deltaTime, camera);
		}
		
		fogOfWarRayHandler.setCombinedMatrix(camera.combined);
		fogOfWarRayHandler.update();
		unpausableTweenManager.update(deltaTime);
		if (!GameState.isPaused()) {
			tweenManager.update(deltaTime);
			if (particleEffectManager != null) {
				particleEffectManager.update(deltaTime);
			}
		}
	};
	
	/**
	 * Updates all game objects that belong directly to this map.
	 * 
	 * This should be called every frame.
	 * 
	 * @param deltaTime
	 */
	public void updateLocalGameObjects(float deltaTime) {
		if (GameState.isPaused()) {
			return;
		}
		for (GameObject go : gameObjects.items) {
			if (go != null) {
				go.update(deltaTime);
			}
		}
	}
	
	@Override
	public boolean shouldModifyVolume() {
		return false;
	}
	
	/**
	 * Transforms the supplied vector from the tile coordinate system
	 * into the camera coordinate system.
	 * 
	 * This does nothing for orthogonal maps.
	 * 
	 * @param vector
	 * @return the supplied vector, transformed
	 */
	public Vector2 projectFromTiles(Vector2 vector) {
		if (isIsometric) {
			tempVector.set(vector.x,vector.y,0).mul(isoTransform);
			vector.set(tempVector.x, tempVector.y);
		}
		return vector;
	}
	
	/**
	 * Transforms the supplied vector from the tile coordinate system
	 * into the camera coordinate system.
	 * 
	 * This does nothing for orthogonal maps.
	 * 
	 * @param vector
	 * @return the supplied vector, transformed
	 */
	public Vector3 projectFromTiles(Vector3 vector) {
		if (isIsometric) {
			vector.mul(isoTransform);
		}
		return vector;
	}
	
	/**
	 * Transforms the supplied vector from the camera coordinate system
	 * into the tile coordinate system.
	 * 
	 * This does nothing for orthogonal maps.
	 * @param vector
	 * @return the supplied vector, transformed
	 */
	public Vector2 projectToTiles(Vector2 vector) {
		if (isIsometric) {
			tempVector.set(vector.x,vector.y,0).mul(invIsoTransform);
			vector.set(tempVector.x, tempVector.y);
		}
		return vector;
	}
	
	/**
	 * Transforms the supplied vector from the camera coordinate system
	 * into the tile coordinate system.
	 * 
	 * This does nothing for orthogonal maps.
	 * @param vector
	 * @return the supplied vector, transformed
	 */
	public Vector3 projectToTiles(Vector3 vector) {
		if (isIsometric) {
			vector.mul(invIsoTransform);
		}
		return vector;
	}
	
	private void createLightsRayHandler() {
		lightsRayHandler = new RayHandler(lightsWorld);
		lightsRayHandler.setCulling(true);
		lightsRayHandler.setBlur(true);
		lightsRayHandler.setShadows(true);
		if (sun == null) {
			sun = new Sun(this, lightsRayHandler);
		} else {
			sun.reset(lightsRayHandler);
		}
	}
	
	private void createFogOfWarRayHandler() {
		fogOfWarRayHandler = new RayHandler(fogOfWarWorld);
		fogOfWarRayHandler.setCulling(true);
		fogOfWarRayHandler.setBlur(false);
		fogOfWarRayHandler.setShadows(true);
		fogOfWarRayHandler.setAmbientLight(new Color(0.1f, 0.1f, 0.1f, 1.0f));
	}
	
	private void createViewConesRayHandler() {
		viewConesRayHandler = new RayHandler(fogOfWarWorld);
		viewConesRayHandler.setCulling(true);
		viewConesRayHandler.setBlur(true);
		viewConesRayHandler.setShadows(true);
		viewConesRayHandler.setAmbientLight(new Color(0.5f, 0.5f, 0.5f, 1.0f));
	}
	
	public void writeAllGameObjectsToXML(XmlWriter writer) throws IOException {
		 for (GameObject go : gameObjects) {
			 if (!go.shouldBeSaved()) {
				 continue;
			 }
			 writer.element(go.getClass().getName());
			 go.writeToXML(writer);
			 writer.pop();
		 }
	}
	
	public void writeAllLocationsToXML(XmlWriter writer) throws IOException {
		 for (GameLocation location: locations) {
			 writer.element(location.getClass().getName());
			 location.writeToXML(writer);
			 writer.pop();
		 }
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		writer.element(XML_FOG_OF_WAR);
		writer.text(fogOfWar.length+" ");
		for (int i = 0; i <fogOfWar.length; ++i) {
			if (fogOfWar[i] == 1) {
				writer.text(i+" ");
			}
		}
		writer.pop();
		
		writer.element(XML_TRANSITION_LOCKS);
		for (TransitionLock lock : transitionLocks) {
			writer.element(XML_TRANSITION_LOCK);
			lock.writeToXML(writer);
			writer.pop();
		}
		writer.pop();
		
		writer.element(XML_TRANSITION_TRAPS);
		for (TransitionTrap trap : transitionTraps) {
			writer.element(XML_TRANSITION_TRAP);
			trap.writeToXML(writer);
			writer.pop();
		}
		writer.pop();
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		Element fogOfWarElement = root.getChildByName(XML_FOG_OF_WAR);
		if (fogOfWarElement != null) {
			String text = fogOfWarElement.getText();
			String[] tokens = text.split(" ");
			
			fogOfWar = new int[Integer.parseInt(tokens[0].trim())];
			for (int i =0; i <fogOfWar.length; ++i) {
				fogOfWar[i] = 0;
			}
			
			for (int i = 1; i < tokens.length; ++i) {
				int index = -1;
				try {
					index = Integer.parseInt(tokens[i].trim());
					fogOfWar[index] = 1;
				} catch (NumberFormatException e) {
					continue;
				}
				
			}
		}
		Element locksElement = root.getChildByName(XML_TRANSITION_LOCKS);
		if (locksElement != null) {
			for (int i = 0; i < locksElement.getChildCount(); ++i) {
				Element lockElement = locksElement.getChild(i);
				transitionLocks.add(new TransitionLock(lockElement));
			}
		}
		Element trapsElement = root.getChildByName(XML_TRANSITION_TRAPS);
		if (trapsElement != null) {
			for (int i = 0; i < trapsElement.getChildCount(); ++i) {
				Element trapElement = trapsElement.getChild(i);
				transitionTraps.add(new TransitionTrap(trapElement));
			}
		}
		Element weatherModifiersElement = root.getChildByName(XML_WEATHER_MODIFIERS);
		if (weatherModifiersElement != null) {
			String[] profiles = weatherModifiersElement.getText().split(",");
			for (String profileId : profiles) {
				WeatherProfile profile = WeatherProfile.getWeatherProfile(profileId.trim());
				if (profile.isModifier()) {
					weatherProfileModifiers.add(profile);
				} else {
					throw new GdxRuntimeException("Weather Profile "+profile.getId()+" is not a modifier and cannot be set for map "+getId());
				}
			}
		}
	}

	public TweenManager getTweenManager(boolean unpausable) {
		return unpausable ? unpausableTweenManager : tweenManager;
	}
	
	public ParticleEffectManager getParticleEffectManager() {
		if (particleEffectManager == null) {
			particleEffectManager = new ParticleEffectManager();
		}
		return particleEffectManager;
	}
	
	public int getTileId(int x, int y) {
		return x+y*getMapWidth();
	}

}
