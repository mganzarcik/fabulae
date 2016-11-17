package mg.fishchicken.gamelogic.locations;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.Line;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.Role;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroupGameObject;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.PickableGameObject;
import mg.fishchicken.gamelogic.locations.transitions.Transition;
import mg.fishchicken.gamelogic.traps.TrapLocation;
import mg.fishchicken.gamestate.SaveablePolygon;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.graphics.lights.GameConeLight;
import mg.fishchicken.graphics.lights.GamePointLight;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.maps.TiledMapLoader;
import box2dLight.FixtureUserData;
import box2dLight.FixtureUserData.UserDataType;
import box2dLight.Light;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;

// TODO the code here is even more disgusting than the rest, it really needs proper refactoring
public class GameMapLoader extends TiledMapLoader {

	public static final float[] ISOMETRIC_VERTICES = new float[] { 0f, 0f, 1f, 0.5f, 2f, 0f, 1f, -0.5f };
	public static final float[] ORTHOGONAL_VERTICES = new float[] { 0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f };

	// layer names
	public static final String LAYER_NPCS = "NPCs";
	public static final String LAYER_GROUPS = "Groups";
	public static final String LAYER_LOCATIONS = "Locations";
	public static final String LAYER_LOS_BLOCKERS = "LOSBlockers";
	public static final String LAYER_OBJECTS = "Objects";
	public static final String LAYER_LIGHTS = "Lights";
	public static final String LAYER_TRANSITIONS = "Transitions";
	public static final String LAYER_ITEMS = "Items";
	public static final String LAYER_CONTAINERS = "Containers";
	public static final String LAYER_USABLES = "Usables";
	public static final String LAYER_TRAPS = "Traps";

	// general properties
	/**
	 * Object id. Using "fid" instead of simple "id" since Tiled adds its own ID
	 * fields.
	 */
	public static final String PROPERTY_ID = "fid";
	/**
	 * File name from which the object should be loaded. Path is taken from the
	 * configuration based on the object type. If empty, it is assumed the file
	 * has the same name as the ID of the object.
	 */
	public static final String PROPERTY_FILE = "file";

	/**
	 * X offset in pixels that should be used when the object is drawn.
	 */
	public static final String PROPERTY_XOFFSET = "xOffset";

	/**
	 * Y offset in pixels that should be used when the object is drawn.
	 */
	public static final String PROPERTY_YOFFSET = "yOffset";

	// item properties
	/**
	 * Optional. An id of the character that owns this item. If supplied, any
	 * value in the factionOwner field will be ignored.
	 */
	public static final String PROPERTY_OWNER_CHARACTER = "ownerCharater";
	/**
	 * Optional. An id of the faction that owns this item.
	 */
	public static final String PROPERTY_OWNER_FACTION = "ownerFaction";

	/**
	 * Optional. Whether or not the owner information is fixed and won't get
	 * lost when the item is sold. Prevents the 'laundering' of the item.
	 */
	public static final String PROPERTY_OWNER_FIXED = "ownerFixed";

	// usable game object properties
	/**
	 * Orientation of the UsableGameObject. Values are identical to those of the
	 * Orientation enum, case insensitive.
	 */
	public static final String PROPERTY_ORIENTATION = "orientation";

	// tiled map object properties
	/**
	 * List of layers that contain tiles of this object. Optional, if not
	 * defined, all object layers will be included.
	 */
	public static final String PROPERTY_LAYERS = "layers";

	// light properties
	/**
	 * Rotation angle of the light in degree. 0 degrees is to the right (3
	 * o'clock) and goes up counter clockwise.
	 */
	public static final String PROPERTY_ROTATION = "rotation";

	/**
	 * Whether or not the light is static. Default is true. Static lights only
	 * receive updates if their state changes. This saves CPU processing.
	 */
	public static final String PROPERTY_STATIC = "static";

	/**
	 * Whether or not the light is xray. Default is false. Xray lights do not
	 * collide with any geometry, saving a lot of CPU processing.
	 */
	public static final String PROPERTY_XRAY = "xray";

	/**
	 * How many rays the lights will have. More means more detailed lights and
	 * shadows. Default is 50. Less means faster.
	 */
	public static final String PROPERTY_RAYS = "rays";

	/**
	 * Whether the light is ambient. Ambient lights never cast shadows (have
	 * xray = true) and are rendered on top of overhead layers.
	 * 
	 * Their intensity is also not affected by the sun and they are not
	 * considered when determining if a character is standing in darkness or not
	 * when calculating stealth modifiers.
	 * 
	 * Default is false.
	 */
	public static final String PROPERTY_AMBIENT = "ambient";

	// NPC properties
	/**
	 * Type of character.
	 * 
	 * Allowed values are PC and NPC. Default is NPC.
	 */
	public static final String PROPERTY_TYPE = "type";

	/**
	 * Role of a player character. If defined, the map loader will first look
	 * for a player character created during start game with this role and if
	 * found, is that character. If no such character is found, it will instead
	 * look for the character the standard way, using the name of the map
	 * object.
	 */
	public static final String PROPERTY_ROLE = "role";

	// Layer properties
	/**
	 * Marks the layer as a ground layer. Ground layers are always rendered
	 * first and underneath any highlights (for example combat paths of target
	 * selection).
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_GROUND_LAYER = "ground";
	/**
	 * Marks the layer as always above. Always above layers are rendered above
	 * all other layers and drawables, but they still receive full lighting.
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_ALWAYSABOVE_LAYER = "alwaysAbove";
	/**
	 * Marks the layer as overhead. Overhead layers are always rendered last on
	 * top of everything, including weather. Additionally, they only receive
	 * ambient lighting.
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_OVERHEAD_LAYER = "overhead";
	/**
	 * Marks the layer as a collision layer. Collision layers are never rendered
	 * an are used only to define pathing and line of sight collisions.
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_COLISIONS_LAYER = "colisions";
	/**
	 * Marks the layer as an object layer. Object layers are never directly
	 * rendered, but any tile objects they contain will by loaded by the map and
	 * rendered if visible.
	 * 
	 * These are used to separate map geometry (trees, walls, etc) that requires
	 * special rendering from map ground.
	 * 
	 * No value required.
	 * 
	 * @see mg.fishchicken.gamelogic.locations.TiledMapObject
	 */
	public static final String PROPERTY_OBJECT_LAYER = "objects";
	/**
	 * Marks the layer as a locations layer. Locations layers define
	 * various GameLocations. By default, any object layer called "Locations"
	 * is loaded as a locations layer. If more locations layers are 
	 * needed (for example to better organize the map), this property 
	 * can be used to mark other layers as locations layers.
	 * 
	 * No value required.
	 * 
	 */
	public static final String PROPERTY_LOCATIONS_LAYER = "locations";
	

	// tile properties
	/**
	 * If defined, the tile with this property is considered impassable for the
	 * purposes of path finding.
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_BLOCKED = "blocked";
	/**
	 * If defined, the tile with this property is considered completely
	 * unreachable to the player. This means any attempt to path to the tile
	 * will be immediately stopped, even if the tile is not yet revealed.
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_UNAVAILABLE = "unavailable";
	/**
	 * If defined, the tile with this property is considered opaque for the line
	 * of sight calculations.
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_BLOCK_LIGHT = "blockLight";

	/**
	 * If defined, the tile with this property will use the supplied value as
	 * move cost for the purposes of pathfinding. Higher cost means the tile
	 * will be less likely to be chosen as a part of a path.
	 * 
	 * A default moveCost is 1. Anything less will be more desirable, anything
	 * more will be less desirable.
	 * 
	 * No value required.
	 */
	public static final String PROPERTY_MOVE_COST = "moveCost";

	// map geometry properties (specified on tiles)
	/**
	 * The position of this tile relative to the position of the "ground" of the
	 * object this tile belongs to.
	 * 
	 * For example, for a tile representing a part of a tree, this would be its
	 * relative position to the tile representing the trunk of the tree.
	 * 
	 * Since objects can have multiple ground tiles, this is a list.
	 * 
	 * List of vectors (x,y values separated by ;), if not set, the tile is not
	 * considered part of any object. If 0,0, the tile is a ground object tile.
	 * 
	 */

	public static final String PROPERTY_GROUND_TILE_OFFSET = "ground";
	/**
	 * An offset that should be applied to all values in the ground property
	 * when calculating the relative position to the ground tiles.
	 * 
	 * This is useful when defining larger objects, since you can just copy the
	 * ground property for all tiles of the object and then just modify this
	 * offset instead of modifying every value in the list.
	 * 
	 * x,y
	 */
	public static final String PROPERTY_GROUND_TILE_OFFSET_MOD = "offsetModifier";
	/**
	 * If set, the object will either be drawn completely, or not at all based
	 * on whether or not its ground it visible.
	 * 
	 * If false, each tile of the object will be drawn separately based on
	 * whether or not it is visible.
	 * 
	 * boolean, default is false
	 */
	public static final String PROPERTY_SHOULD_DRAW_AS_A_WHOLE = "shouldDrawAsAWhole";

	// trap properties (can also be defined on doors)
	/**
	 * The unique ID of the trap that should be assigned to the door.
	 * 
	 * String, default is null
	 */
	public static final String PROPERTY_TRAP_ID = "trapId";
	/**
	 * Whether or not the assigned trap is disarmed
	 * 
	 * String, default is false
	 */
	public static final String PROPERTY_DISARMED = "disarmed";
	/**
	 * Whether or not the assigned trap is detected
	 * 
	 * String, default is false
	 */
	public static final String PROPERTY_DETECTED = "detected";

	// door properties
	/**
	 * The unique ID of the door.
	 * 
	 * String, required
	 */
	public static final String PROPERTY_DOOR_ID = "doorId";
	/**
	 * Whether or not the door can be opened using the lock pick skill.
	 * 
	 * boolean, default is true
	 */
	public static final String PROPERTY_PICKABLE = "pickable";
	/**
	 * Whether or not the door is unlocked and can be opened freely.
	 * 
	 * boolean, default is true
	 */
	public static final String PROPERTY_UNLOCKED = "unlocked";
	/**
	 * Level of the lock on the door. This defined the skill level required to
	 * lockpick it.
	 * 
	 * Integer, default is 0
	 */
	public static final String PROPERTY_LOCK_LEVEL = "lockLevel";
	/**
	 * The ID of the item the player must have in inventory in order to be able
	 * to open the lock without lock-picking.
	 * 
	 * If null, no special item is required.
	 * 
	 * String, default is null.
	 */
	public static final String PROPERTY_KEY = "key";

	// transition properties - note the ID of the target map
	// is defined in the Name of the Transition map object
	// and not as a separate property
	/**
	 * The coordinates where the player should appear on the new map.
	 * 
	 * x, y, required
	 */
	public static final String PROPERTY_TARGET = "target";
	/**
	 * The coordinates of the tile where the player character must stand in
	 * order to enter the transition.
	 * 
	 * x, y, required
	 */
	public static final String PROPERTY_GROUND = "ground";

	public static final String VALUE_PC = "PC";
	public static final String VALUE_NPC = "NPC";

	private Array<TiledMapTileLayer> mapTileObjectLayers; // lazy init, is
															// initialized at
															// the same time as
															// tiledMap
	private GameMap gameMap;

	public GameMapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle tmxFile,
			com.badlogic.gdx.maps.tiled.TmxMapLoader.Parameters parameter) {
		super.loadAsync(manager, fileName, tmxFile, parameter);

		gameMap = ((Parameters) parameter).map;
		mapTileObjectLayers = new Array<TiledMapTileLayer>();
		boolean loadTransactional = ((Parameters) parameter).loadTransactional;

		gameMap.setTiledMap(map);

		Array<TiledMapTileLayer> groundLayers = gameMap.getGroundLayers();
		groundLayers.clear();
		Array<TiledMapTileLayer> overheadLayers = gameMap.getOverheadLayers();
		overheadLayers.clear();
		Array<TiledMapTileLayer> alwaysAboveLayers = gameMap.getAlwaysAboveLayers();
		alwaysAboveLayers.clear();
		mapTileObjectLayers.clear();
		gameMap.colisionLayer = null;
		for (MapLayer layer : map.getLayers()) {
			boolean groundLayer = layer.getProperties().get(PROPERTY_GROUND_LAYER) != null;
			boolean collisionLayer = layer.getProperties().get(PROPERTY_COLISIONS_LAYER) != null;
			boolean objectLayer = layer.getProperties().get(PROPERTY_OBJECT_LAYER) != null;
			boolean overheadLayer = layer.getProperties().get(PROPERTY_OVERHEAD_LAYER) != null;
			boolean alwaysAboveLayer = layer.getProperties().get(PROPERTY_ALWAYSABOVE_LAYER) != null;

			if (groundLayer) {
				groundLayers.add((TiledMapTileLayer) layer);
			}
			if (objectLayer) {
				mapTileObjectLayers.add((TiledMapTileLayer) layer);
			}
			if (collisionLayer) {
				gameMap.colisionLayer = (TiledMapTileLayer) layer;
			}
			if (overheadLayer) {
				overheadLayers.add((TiledMapTileLayer) layer);
			}
			if (alwaysAboveLayer) {
				alwaysAboveLayers.add((TiledMapTileLayer) layer);
			}
		}

		loadBlockedTiles();
		calculateMoveCosts();

		gameMap.setDimensions(groundLayers.get(0).getWidth(), groundLayers.get(0).getHeight());

		try {
			if (loadTransactional) {
				gameMap.resetFogOfWar();
				loadItems();
				loadUsables();
			}

			gameMap.loadFromXML(Gdx.files.internal(Configuration.getFolderLocations() + gameMap.getId() + ".xml"));

			if (loadTransactional) {
				loadLocations();
				loadTraps();
			}

			if (!gameMap.isCombatMap()) {
				loadTransitions();
			}
			loadLights();
			buildLightBlockFixtures();
			loadTiledMapObjects();

			if (loadTransactional) {
				if (gameMap.isWorldMap()) {
					loadCharacterGroups();
				}
				loadCharacters();
			}
			gameMap.setMapLoaded();
			if (((Parameters) parameter).onLoaded != null) {
				((Parameters) parameter).onLoaded.finishedLoading(manager, fileName, GameMap.class);
			}
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	private void loadBlockedTiles() {
		Cell cell;
		int width = map.getProperties().get("width", Integer.class);
		int height = map.getProperties().get("height", Integer.class);
		gameMap.blockedTiles = new boolean[width * height];
		gameMap.unavailableTiles = new boolean[width * height];

		for (int x = 0; x < width; ++x) {
			outer: for (int y = 0; y < height; ++y) {
				if (gameMap.colisionLayer != null) {
					cell = gameMap.colisionLayer.getCell(x, y);
					if (cell != null) {
						MapProperties tileProperties = cell.getTile().getProperties();
						if (tileProperties.get(GameMapLoader.PROPERTY_UNAVAILABLE) != null) {
							gameMap.unavailableTiles[x + y * width] = true;
							continue;
						}
						if (tileProperties.get(GameMapLoader.PROPERTY_BLOCKED) != null) {
							gameMap.blockedTiles[x + y * width] = true;
							continue;
						}
					}
				}

				for (TiledMapTileLayer groundLayer : gameMap.getGroundLayers()) {
					cell = groundLayer.getCell(x, y);
					if (cell != null) {
						MapProperties tileProperties = cell.getTile().getProperties();
						if (tileProperties.get(GameMapLoader.PROPERTY_UNAVAILABLE) != null) {
							gameMap.unavailableTiles[x + y * width] = true;
							continue;
						}
						if (tileProperties.get(GameMapLoader.PROPERTY_BLOCKED) != null) {
							gameMap.blockedTiles[x + y * width] = true;
							continue outer;
						}
					}
				}
			}
		}
	}

	private void calculateMoveCosts() {
		Cell cell;
		int width = map.getProperties().get("width", Integer.class);
		int height = map.getProperties().get("height", Integer.class);
		gameMap.moveCosts = new float[width * height];

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				float moveCost = -1;
				for (MapLayer layer : gameMap.getTiledMap().getLayers()) {
					if (layer instanceof TiledMapTileLayer) {
						cell = ((TiledMapTileLayer) layer).getCell(x, y);
						if (cell != null) {
							MapProperties tileProperties = cell.getTile().getProperties();
							Object cost = tileProperties.get(GameMapLoader.PROPERTY_MOVE_COST);
							if (cost != null) {
								float newCost = Float.valueOf((String) cost);
								if (newCost > moveCost) {
									moveCost = newCost;
								}
							}
						}
					}
				}
				if (moveCost < 0) {
					moveCost = 1;
				}
				gameMap.moveCosts[x + y * width] = moveCost;
			}
		}
	}

	private void loadCharacters() throws IOException {
		MapLayer npcLayer = map.getLayers().get(LAYER_NPCS);
		if (npcLayer == null) {
			return;
		}
		MapObjects npcs = npcLayer.getObjects();
		for (MapObject mapNpc : npcs) {
			if (mapNpc.getName() == null) {
				continue;
			}
			String type = mapNpc.getName();
			String role = (String) mapNpc.getProperties().get(PROPERTY_ROLE);
			String id = (String) mapNpc.getProperties().get(PROPERTY_ID);
			if (id == null) {
				id = type;
			}

			GameCharacter character = null;

			character = GameCharacter.loadCharacter(id,
					Gdx.files.internal(Configuration.getFolderCharacters() + type + ".xml"), gameMap);

			if (role != null) {
				GameCharacter playerCreatedCharacter = GameState.getPlayerCharacterGroup().getCreatedCharacter(
						Role.getRole(role));
				if (playerCreatedCharacter != null) {
					playerCreatedCharacter.setId(character.getId());
					playerCreatedCharacter.brain().setAIScript(character.brain().getAIScript());
					playerCreatedCharacter.setDialogueId(character.getDialogueId());
					int level = character.stats().getLevel();
					while (playerCreatedCharacter.stats().getLevel() < level) {
						playerCreatedCharacter.stats().levelUp();
					}
					playerCreatedCharacter.getInventory().clear();
					playerCreatedCharacter.setOrientation(character.getOrientation());
					character.getInventory().copyAllItemsTo(playerCreatedCharacter.getInventory());
					playerCreatedCharacter.stats().setExperienceValue(character.stats().getExperienceValue());
					playerCreatedCharacter.stats().setExperience(
							Configuration.getExperienceTable().getRequiredExperienceTotalForLevel(level));

					character.remove();
					character = playerCreatedCharacter;
					character.undispose();
					character.setMap(gameMap);
				}
			}

			Vector2 position = getPositionFromMapObject(mapNpc);
			character.position().set((int) (position.x / gameMap.getTileSizeX()),
					(int) (position.y / gameMap.getTileSizeY()));
		}
	}

	private void loadCharacterGroups() throws IOException {
		MapLayer groupsLayer = map.getLayers().get(LAYER_GROUPS);
		if (groupsLayer == null) {
			return;
		}
		MapObjects groups = groupsLayer.getObjects();
		for (MapObject mapGroup : groups) {
			if (mapGroup.getName() == null) {
				continue;
			}
			String groupFile = mapGroup.getName();
			String id = (String) mapGroup.getProperties().get(PROPERTY_ID);
			if (id == null) {
				id = groupFile;
			}

			Vector2 position = getPositionFromMapObject(mapGroup);
			CharacterGroupGameObject group = new CharacterGroupGameObject(id, Gdx.files.internal(Configuration
					.getFolderGroups() + groupFile + ".xml"), gameMap);
			group.position().set((int) (position.x / gameMap.getTileSizeX()),
					(int) (position.y / gameMap.getTileSizeY()));
		}
	}

	private void loadUsables() throws IOException {
		MapLayer itemsLayer = map.getLayers().get(LAYER_USABLES);
		if (itemsLayer == null) {
			return;
		}
		MapObjects items = itemsLayer.getObjects();
		for (MapObject item : items) {
			Polygon polygon = getPolygonFromMapObject(item);
			if (item.getName() == null) {
				continue;
			}
			String type = item.getName();
			Object idValue = item.getProperties().get(PROPERTY_ID);
			String id = idValue instanceof String ? (String) idValue : null;
			if (id == null) {
				id = type;
			}

			UsableGameObject newItem = new UsableGameObject(id, Gdx.files.internal(Configuration.getFolderUsables()
					+ type + ".xml"), transformTiledPolygon(gameMap, polygon), gameMap);

			String orientationProp = item.getProperties().get(PROPERTY_ORIENTATION, null, String.class);
			if (orientationProp != null) {
				newItem.setOrientation(Orientation.valueOf(orientationProp.toUpperCase(Locale.ENGLISH)));
			}

			String groundProp = item.getProperties().get(PROPERTY_GROUND, null, String.class);
			if (groundProp != null) {
				String[] coords = groundProp.split(",");
				newItem.setGround(Integer.parseInt(coords[0].trim()), Integer.parseInt(coords[1].trim()));
			}
			newItem.setOffsets(newItem.getXOffset() + getInteger(item, PROPERTY_XOFFSET, 0) * gameMap.getScaleX(),
					newItem.getYOffset() + getInteger(item, PROPERTY_YOFFSET, 0) * gameMap.getScaleY());
		}
	}

	private void loadItems() throws IOException {
		MapLayer itemsLayer = map.getLayers().get(LAYER_ITEMS);
		if (itemsLayer == null) {
			return;
		}
		MapObjects items = itemsLayer.getObjects();
		for (MapObject item : items) {
			Vector2 position = getPositionFromMapObject(item);
			PickableGameObject newItem = new PickableGameObject(item.getName());
			newItem.getOwner().set(item.getProperties().get(PROPERTY_OWNER_CHARACTER, String.class),
					Faction.getFaction(item.getProperties().get(PROPERTY_OWNER_FACTION, String.class)),
					Boolean.valueOf(item.getProperties().get(PROPERTY_OWNER_FIXED, "false", String.class)));
			newItem.position().set(position.x / gameMap.getTileSizeX(), position.y / gameMap.getTileSizeY());
			newItem.setMap(gameMap);
			newItem.setOffsets(newItem.getXOffset() + getInteger(item, PROPERTY_XOFFSET, 0) * gameMap.getScaleX(),
					newItem.getYOffset() + getInteger(item, PROPERTY_YOFFSET, 0) * gameMap.getScaleY());
		}
	}

	private int getInteger(MapObject object, String property, int defaultValue) {
		return Integer.valueOf(object.getProperties().get(property, Integer.toString(defaultValue), String.class));
	}

	private void loadLights() throws IOException {
		MapLayer locationsLayer = map.getLayers().get(LAYER_LIGHTS);
		if (locationsLayer == null) {
			return;
		}
		MapObjects lights = locationsLayer.getObjects();
		for (MapObject light : lights) {
			if (light instanceof EllipseMapObject) {
				MapProperties lightProperties = light.getProperties();
				String lightId = lightProperties.get(PROPERTY_ID, null, String.class);

				Ellipse center = ((EllipseMapObject) light).getEllipse();
				Vector2 tempVector = MathUtil.getVector2().set((center.x / gameMap.getTileSizeX()) + 0.5f,
						(center.y / gameMap.getTileSizeY()) + 0.5f);
				gameMap.projectFromTiles(tempVector);
				LightDescriptor descriptor = LightDescriptor.getLightDescriptor(light.getName());
				int rays = Integer.valueOf(lightProperties.get(PROPERTY_RAYS, "50", String.class));
				boolean xray = Boolean.valueOf(lightProperties.get(PROPERTY_XRAY, "false", String.class));
				boolean staticLight = Boolean.valueOf(lightProperties.get(PROPERTY_STATIC, "true", String.class));
				RayHandler handler = gameMap.getLightsRayHandler();
				Light createdLight;
				if (!descriptor.isConeLight) {
					createdLight = new GamePointLight(lightId, handler, gameMap, descriptor, tempVector.x,
							tempVector.y, rays, gameMap.isIsometric());
				} else {
					String rotation = lightProperties.get(PROPERTY_ROTATION, "0", String.class);
					createdLight = new GameConeLight(lightId, handler, gameMap, descriptor, tempVector.x, tempVector.y,
							Float.valueOf(rotation), rays);
				}
				createdLight.setXray(xray);
				createdLight.setStaticLight(staticLight);
				MathUtil.freeVector2(tempVector);
			} else if (light instanceof PolylineMapObject) {
				Polyline polyline = ((PolylineMapObject) light).getPolyline();
				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyDef.BodyType.StaticBody;
				float startX = polyline.getX() / gameMap.getTileSizeX();
				float startY = polyline.getY() / gameMap.getTileSizeY();
				Vector2 projectedStart = MathUtil.getVector2().set(startX, startY);
				gameMap.projectFromTiles(projectedStart);
				bodyDef.position.set(projectedStart.x, projectedStart.y);
				Body groundBody = gameMap.getLightsWorld().createBody(bodyDef);
				float[] vertices = polyline.getVertices();
				Vector2 tempVector = MathUtil.getVector2();
				for (int i = 0; i < vertices.length; i += 4) {
					if (i != 0) {
						i -= 2;
					}
					tempVector.set(startX + vertices[i] / gameMap.getTileSizeX(),
							startY + vertices[i + 1] / gameMap.getTileSizeY());
					gameMap.projectFromTiles(tempVector);
					tempVector.sub(projectedStart);
					float x1 = tempVector.x;
					float y1 = tempVector.y;

					tempVector.set(startX + vertices[i + 2] / gameMap.getTileSizeX(), startY + vertices[i + 3]
							/ gameMap.getTileSizeY());
					gameMap.projectFromTiles(tempVector);
					tempVector.sub(projectedStart);
					float x2 = tempVector.x;
					float y2 = tempVector.y;
					EdgeShape shape = new EdgeShape();
					shape.set(x1, y1, x2, y2);
					FixtureDef fixture = new FixtureDef();
					fixture.shape = shape;
					fixture.density = 1f;
					groundBody.createFixture(fixture).setUserData(
							new FixtureUserData(UserDataType.LIGHT_BLOCKER, (String) light.getProperties().get(
									PROPERTY_ID)));
					shape.dispose();
				}
				MathUtil.freeVector2(tempVector);
				MathUtil.freeVector2(projectedStart);
			}
		}
	}

	private void loadLocations() throws IOException {
		
		for (MapLayer layer : map.getLayers()) {
			if (LAYER_LOCATIONS.equals(layer.getName()) || layer.getProperties().get(PROPERTY_LOCATIONS_LAYER) != null) {
				loadLocations(layer, false);
			}
		}
	}

	private void loadTraps() throws IOException {
		loadLocations(map.getLayers().get(LAYER_TRAPS), true);
	}

	private void loadLocations(MapLayer locationsLayer, boolean loadTraps) throws IOException {
		if (locationsLayer == null) {
			return;
		}
		MapObjects locations = locationsLayer.getObjects();
		for (MapObject location : locations) {
			String locId = location.getProperties().get(PROPERTY_ID, location.getName(), String.class)
					.toLowerCase(Locale.ENGLISH);
			String locType = location.getName();
			SaveablePolygon locPolygon = transformTiledPolygon(gameMap, getPolygonFromMapObject(location));
			GameLocation newLocation = loadTraps ? new TrapLocation(locId, locType, locPolygon,
					Boolean.valueOf(location.getProperties().get(PROPERTY_DETECTED, "false", String.class)),
					Boolean.valueOf(location.getProperties().get(PROPERTY_DISARMED, "false", String.class)))
					: new GameLocation(locId, locType, locPolygon);
			newLocation.setMap(gameMap);
			newLocation.loadFromXML(Gdx.files.internal(Configuration.getFolderLocations() + locType + ".xml"));
		}

		for (GameObject go : gameMap.gameObjects) {
			if (go instanceof GameCharacter) {
				((GameCharacter) go).calculateCurrentLocations();
			}
		}
	}

	private void loadTransitions() {
		MapLayer locationsLayer = map.getLayers().get(LAYER_TRANSITIONS);
		if (locationsLayer == null) {
			return;
		}
		Array<Transition> transitions = new Array<Transition>();
		MapObjects locations = locationsLayer.getObjects();
		for (MapObject location : locations) {
			MapObject mapObject = (MapObject) location;
			String[] coords = ((String) mapObject.getProperties().get(PROPERTY_TARGET)).split(",");
			Tile target = new Tile(Integer.parseInt(coords[0].trim()), Integer.parseInt(coords[1].trim()));

			Polygon polygon = getPolygonFromMapObject(mapObject);
			Transition newTransition = new Transition(gameMap, mapObject.getName(), target.getX(), target.getY(),
					transformTiledPolygon(gameMap, polygon));
			transitions.add(newTransition);
		}
		buildTransitionTiles(transitions);
	}

	private void buildTransitionTiles(Array<Transition> transitions) {
		for (int i = 0; i < gameMap.getMapWidth(); ++i) {
			for (int j = 0; j < gameMap.getMapHeight(); ++j) {
				for (Transition transition : transitions) {
					if (transition.contains(i, j)) {
						gameMap.transitionTiles.put(new Vector2(i, j), transition);
					}
				}
			}
		}
	}

	private void buildLightBlockFixtures() {
		Vector2 tempVector = MathUtil.getVector2();
		float[] vertices = ORTHOGONAL_VERTICES;
		if (gameMap.isIsometric()) {
			vertices = ISOMETRIC_VERTICES;
		}

		// for tiles that are blocking sight because of their properties
		// we collect all of their edges, completely
		// eliminating those that occur twice to join
		// neighboring polygons and also
		// merge edges into longer edges where possible
		HashSet<Line> edges = new HashSet<Line>();
		for (int x = 0; x < gameMap.getMapWidth(); ++x) {
			for (int y = 0; y < gameMap.getMapHeight(); ++y) {
				for (MapLayer layer : map.getLayers()) {
					if (!(layer instanceof TiledMapTileLayer)) {
						continue;
					}
					Cell cell = ((TiledMapTileLayer) layer).getCell(x, y);
					if (cell == null) {
						continue;
					}
					MapProperties tileProperties = cell.getTile().getProperties();
					String blocked = (String) tileProperties.get(PROPERTY_BLOCK_LIGHT);
					if (blocked == null) {
						continue;
					}
					tempVector.set(x, y);
					gameMap.projectFromTiles(tempVector);

					addEdgeOrMerge(edges, new Line(vertices[0] + tempVector.x, vertices[1] + tempVector.y, vertices[2]
							+ tempVector.x, vertices[3] + tempVector.y));
					addEdgeOrMerge(edges, new Line(vertices[2] + tempVector.x, vertices[3] + tempVector.y, vertices[4]
							+ tempVector.x, vertices[5] + tempVector.y));
					addEdgeOrMerge(edges, new Line(vertices[4] + tempVector.x, vertices[5] + tempVector.y, vertices[6]
							+ tempVector.x, vertices[7] + tempVector.y));
					addEdgeOrMerge(edges, new Line(vertices[6] + tempVector.x, vertices[7] + tempVector.y, vertices[0]
							+ tempVector.x, vertices[1] + tempVector.y));
				}
			}
		}

		for (Line edge : edges) {
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.StaticBody;
			Body groundBody = gameMap.getFogOfWarWorld().createBody(bodyDef);
			EdgeShape shape = new EdgeShape();
			shape.set(edge.startX, edge.startY, edge.endX, edge.endY);
			FixtureDef fixture = new FixtureDef();
			fixture.shape = shape;
			fixture.density = 1f;
			groundBody.createFixture(fixture).setUserData(new FixtureUserData(UserDataType.LOS_BLOCKER_TILE));
			shape.dispose();
		}

		Array<MapLayer> layersToProcess = new Array<MapLayer>();
		MapLayer losBlockersLayer = map.getLayers().get(LAYER_LOS_BLOCKERS);
		if (losBlockersLayer != null) {
			layersToProcess.add(losBlockersLayer);
		}
		MapLayer objectsLayer = map.getLayers().get(LAYER_OBJECTS);
		if (objectsLayer != null) {
			layersToProcess.add(objectsLayer);
		}
		for (MapLayer layer : layersToProcess) {
			MapObjects mapObjects = layer.getObjects();
			int counter = 0;
			for (MapObject mapObject : mapObjects) {
				float[] myVertices = null;
				Polyline polyline = null;
				Polygon polygon = null;
				float startX = 0;
				float startY = 0;
				if (!Boolean.valueOf(mapObject.getProperties().get(PROPERTY_BLOCK_LIGHT, "true", String.class))) {
					continue;
				}
				UserDataType dataType = Boolean.valueOf((String) mapObject.getProperties().get(
						PROPERTY_GROUND_TILE_OFFSET)) ? UserDataType.LOS_BLOCKER_POLYGON_GROUND
						: UserDataType.LOS_BLOCKER_POLYGON;
				if (mapObject instanceof PolylineMapObject) {
					polyline = ((PolylineMapObject) mapObject).getPolyline();
					myVertices = polyline.getVertices();
					startX = polyline.getX() / gameMap.getTileSizeX();
					startY = polyline.getY() / gameMap.getTileSizeY();
					dataType = UserDataType.LOS_BLOCKER_LINE;
				} else {
					polygon = getPolygonFromMapObject(mapObject);
					myVertices = polygon.getVertices();
					startX = polygon.getX() / gameMap.getTileSizeX();
					startY = polygon.getY() / gameMap.getTileSizeY();
				}

				Object id = mapObject.getProperties().get(PROPERTY_ID);
				String polygonId = id instanceof String ? (String) id : null;
				if (polygonId == null) {
					polygonId = Integer.toString(counter);
				}
				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyDef.BodyType.StaticBody;
				Vector2 projectedStart = MathUtil.getVector2();
				projectedStart.set(startX, startY);
				gameMap.projectFromTiles(projectedStart);
				bodyDef.position.set(projectedStart.x, projectedStart.y);
				Body groundBody = gameMap.getFogOfWarWorld().createBody(bodyDef);

				for (int i = 0; i < myVertices.length; i += 4) {
					if (i != 0) {
						i -= 2;
					}
					tempVector.set(startX + myVertices[i] / gameMap.getTileSizeX(), startY + myVertices[i + 1]
							/ gameMap.getTileSizeY());
					gameMap.projectFromTiles(tempVector);
					tempVector.sub(projectedStart);
					float x1 = tempVector.x;
					float y1 = tempVector.y;

					tempVector.set(startX + myVertices[i + 2] / gameMap.getTileSizeX(), startY + myVertices[i + 3]
							/ gameMap.getTileSizeY());
					gameMap.projectFromTiles(tempVector);
					tempVector.sub(projectedStart);
					float x2 = tempVector.x;
					float y2 = tempVector.y;
					EdgeShape shape = new EdgeShape();
					shape.set(x1, y1, x2, y2);
					FixtureDef fixture = new FixtureDef();
					fixture.shape = shape;
					fixture.density = 1f;
					groundBody.createFixture(fixture).setUserData(
							dataType == UserDataType.LOS_BLOCKER_LINE ? new FixtureUserData(dataType, polygonId,
									polyline) : new FixtureUserData(dataType, polygonId, polygon));
					shape.dispose();
				}
				MathUtil.freeVector2(projectedStart);
				++counter;
			}
		}
		MathUtil.freeVector2(tempVector);
	}

	private void addEdgeOrMerge(HashSet<Line> edges, Line edge) {
		for (Line oldEdge : edges) {
			if (oldEdge.canCombine(edge) && !edge.equals(oldEdge)) {
				oldEdge.combine(edge);
				return;
			}
		}
		edges.add(edge);
	}

	private void loadTiledMapObjects() {
		MapLayer objectsLayer = map.getLayers().get(LAYER_OBJECTS);
		if (objectsLayer != null) {
			MapObjects mapObjects = objectsLayer.getObjects();
			Array<MapTileObjectGround> grounds = new Array<MapTileObjectGround>();
			Array<MapObject> nonGroundMapObjects = new Array<MapObject>();
			for (MapObject mapObject : mapObjects) {
				boolean isGround = Boolean.valueOf((String) mapObject.getProperties().get(PROPERTY_GROUND_TILE_OFFSET));
				if (!isGround) {
					nonGroundMapObjects.add(mapObject);
					continue;
				}
				MapTileObjectGround objectGround = new MapTileObjectGround(mapObject.getName(),
						getTilesFromPolygon(getPolygonFromMapObject(mapObject)));
				grounds.add(objectGround);
			}
			for (MapObject mapObject : nonGroundMapObjects) {
				Polygon polygon = getPolygonFromMapObject(mapObject);
				for (MapTileObjectGround ground : grounds) {
					String groundName = ground.getName();
					boolean nameEmpty = StringUtil.nullOrEmptyString(groundName);
					if ((nameEmpty && ground.isContainedIn(transformTiledPolygon(gameMap, polygon)))
							|| (!nameEmpty && groundName.equals(mapObject.getName()))) {
						IntArray tiles = getTilesFromPolygon(polygon);
						String layerNames = mapObject.getProperties().get(PROPERTY_LAYERS, String.class);
						Array<TiledMapTileLayer> layers = mapTileObjectLayers;
						if (!StringUtil.nullOrEmptyString(layerNames)) {
							String[] splits = layerNames.split(",");
							layers = new Array<TiledMapTileLayer>();
							for (String layerName : splits) {
								for (TiledMapTileLayer layer : mapTileObjectLayers) {
									if (layerName.trim().equals(layer.getName())) {
										layers.add(layer);
									}
								}
							}
						}
						if (tiles.size > 0) {
							TiledMapObject mto = new TiledMapObject(ground, tiles.toArray(), gameMap, layers,
									Boolean.valueOf(mapObject.getProperties().get(PROPERTY_SHOULD_DRAW_AS_A_WHOLE,
											"false", String.class)));
							gameMap.drawables.add(mto);
							gameMap.mapTileObjects.add(mto);
						}
					}
				}
			}
		}

		for (TiledMapTileLayer layer : mapTileObjectLayers) {
			ObjectMap<MapTileObjectGround, IntArray> overheadTileMap = new ObjectMap<MapTileObjectGround, IntArray>();
			ObjectMap<MapTileObjectGround, Boolean> shouldDrawWholeMap = new ObjectMap<MapTileObjectGround, Boolean>();
			for (int x = 0; x < gameMap.getMapWidth(); ++x) {
				for (int y = 0; y < gameMap.getMapHeight(); ++y) {
					Cell cell = ((TiledMapTileLayer) layer).getCell(x, y);
					if (cell == null) {
						continue;
					}
					TiledMapTile tile = cell.getTile();
					if (tile == null) {
						continue;
					}
					int xOffset = 0;
					int yOffset = 0;

					String offsets = (String) tile.getProperties().get(PROPERTY_GROUND_TILE_OFFSET);
					if (offsets == null) {
						continue;
					}
					String shouldDrawAsAWhole = tile.getProperties().get(PROPERTY_SHOULD_DRAW_AS_A_WHOLE, "false",
							String.class);

					String offsetsMod = (String) tile.getProperties().get(PROPERTY_GROUND_TILE_OFFSET_MOD);
					int xOffsetMod = 0, yOffsetMod = 0;
					if (offsetsMod != null) {
						String[] splits = offsetsMod.split(",");
						xOffsetMod = Integer.parseInt(splits[0].trim());
						yOffsetMod = Integer.parseInt(splits[1].trim());
					}
					String[] offsetsSplits = offsets.split(";");
					MapTileObjectGround objectGround = new MapTileObjectGround();
					for (String offset : offsetsSplits) {
						String[] splits = offset.split(",");
						if (splits.length != 2) {
							continue;
						}
						xOffset = Integer.parseInt(splits[0].trim());
						yOffset = Integer.parseInt(splits[1].trim());

						int modifiedX = x + xOffset + xOffsetMod;
						int modifiedY = y - yOffset - yOffsetMod;
						Tile groundTile = new Tile(modifiedX, modifiedY);
						objectGround.addTile(groundTile);
					}
					IntArray offsetTiles = overheadTileMap.get(objectGround);
					if (offsetTiles == null) {
						offsetTiles = new IntArray();
						overheadTileMap.put(objectGround, offsetTiles);
					}
					offsetTiles.add(x);
					offsetTiles.add(y);
					Boolean shouldDrawAsAWholeValue = shouldDrawWholeMap.get(objectGround);
					if (shouldDrawAsAWholeValue == null) {
						shouldDrawAsAWholeValue = false;
					}
					shouldDrawWholeMap
							.put(objectGround, shouldDrawAsAWholeValue || Boolean.valueOf(shouldDrawAsAWhole));
				}

			}
			for (MapTileObjectGround key : overheadTileMap.keys()) {
				TiledMapObject mto = new TiledMapObject(key, overheadTileMap.get(key).toArray(), gameMap, layer,
						shouldDrawWholeMap.get(key));
				gameMap.drawables.add(mto);
				gameMap.mapTileObjects.add(mto);
			}
		}
	}

	private Vector2 getPositionFromMapObject(MapObject mapObject) {
		if (mapObject instanceof PolygonMapObject) {
			Polygon polygon = ((PolygonMapObject) mapObject).getPolygon();
			return new Vector2(polygon.getX(), polygon.getY());
		} else if (mapObject instanceof RectangleMapObject) {
			Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();
			return new Vector2(rectangle.getX(), rectangle.getY());
		} else if (mapObject instanceof EllipseMapObject) {
			Ellipse ellipse = ((EllipseMapObject) mapObject).getEllipse();
			return new Vector2(ellipse.x, ellipse.y);
		} else if (mapObject instanceof CircleMapObject) {
			Circle circle = ((CircleMapObject) mapObject).getCircle();
			return new Vector2(circle.x, circle.y);
		}
		throw new GdxRuntimeException("Only Polygons, Rectangles, Ellipses and Circles are supported!");
	}

	private Polygon getPolygonFromMapObject(MapObject mapObject) {
		if (mapObject instanceof PolygonMapObject) {
			return ((PolygonMapObject) mapObject).getPolygon();
		} else if (mapObject instanceof RectangleMapObject) {
			return MathUtil.polygonFromRectangle(((RectangleMapObject) mapObject).getRectangle());
		}
		throw new GdxRuntimeException("Only Polygons and Rectangles are supported!");
	}

	private IntArray getTilesFromPolygon(Polygon polygon) {
		IntArray tiles = new IntArray();
		Polygon transformedPolygon = transformTiledPolygon(gameMap, polygon);
		Rectangle rectangle = transformedPolygon.getBoundingRectangle();
		int startX = (int) rectangle.getX();
		int startY = (int) rectangle.getY();
		int endX = startX + (int) rectangle.getWidth();
		int endY = startY + (int) rectangle.getHeight();
		for (int x = startX; x <= endX; ++x) {
			for (int y = startY; y <= endY; ++y) {
				if (transformedPolygon.contains(x, y)) {
					tiles.add(x);
					tiles.add(y);
				}
			}
		}
		return tiles;
	}

	private static SaveablePolygon transformTiledPolygon(GameMap map, Polygon polygon) {
		SaveablePolygon returnValue = new SaveablePolygon(polygon.getVertices());
		returnValue.setPosition(polygon.getX() / map.getTileSizeX(), polygon.getY() / map.getTileSizeX());
		returnValue.setScale(1 / map.getTileSizeX(), 1 / map.getTileSizeY());
		return returnValue;
	}

	public static class Parameters extends TiledMapLoader.Parameters {
		private final boolean loadTransactional;
		private final GameMap map;
		private final LoadedCallback onLoaded;

		public Parameters(GameMap map, boolean loadTransactional, LoadedCallback onLoaded) {
			super();
			this.map = map;
			this.loadTransactional = loadTransactional;
			this.onLoaded = onLoaded;
		}
	}

}
