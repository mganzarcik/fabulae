package mg.fishchicken.screens;

import java.util.Locale;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.locations.CombatGameMap;
import mg.fishchicken.gamelogic.locations.CombatGameMap.CombatMapInitializationData;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locations.GameMapLoader;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.screens.ModuleLoadingScreen.LoadingScreenStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.loading.LoadingIndicator;
import mg.fishchicken.ui.loading.LoadingScreenBackground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectSet;

public class LoadingMapScreen extends BasicStageScreen {

	private FishchickenGame game;
	private GameState gameState;
	private GameMap oldMap, newMap;
	private String mapId;
	private Tile startPos;
	private boolean removeActions;
	private boolean isLoadingSavegame;
	private LoadingScreenBackground background;
	private LoadingIndicator loadingIndicator;
	private boolean mapLoaded, removedFromOld;

	public LoadingMapScreen(FishchickenGame game, GameState global) {
		super();
		this.game = game;
		this.gameState = global;
		removeActions = false;
	}

	public void init(String newMapId, GameMap oldMap, Tile startPos, boolean isLoadingSavegame,
			CombatMapInitializationData combatMapData, LoadedCallback callback) {
		// if we are already loading this map, do nothing
		if (this.mapId != null && this.mapId.equalsIgnoreCase(newMapId)) {
			return;
		}
		// turn off input
		Gdx.input.setInputProcessor(null);

		mapId = newMapId;
		this.oldMap = oldMap;

		this.startPos = startPos;
		
		this.removedFromOld = false;

		prepareForLoading(newMapId, combatMapData, callback);

		removeActions = !isLoadingSavegame;
		this.isLoadingSavegame = isLoadingSavegame;
		if (background != null) {
			background.reset(Configuration
					.getLoadingScreensConfiguration()
					.getConfigurationForScreenType("locations"), mapId);
		}
	}
	
	private void buildLoadingIndicator() {
		LoadingScreenStyle style = UIManager.getSkin().get(LoadingScreenStyle.class);
		background = new LoadingScreenBackground(Configuration
				.getLoadingScreensConfiguration()
				.getConfigurationForScreenType("locations"), mapId);
		stage.addActor(background);
		loadingIndicator = new LoadingIndicator(style.loadingIndicatorStyle);
		stage.addActor(loadingIndicator);
		WindowPosition.CENTER.position(loadingIndicator);
	}

	@Override
	public void render(float delta) {
		if (!removedFromOld && oldMap != null) {
			if (oldMap.isWorldMap()) {
				GameState.getPlayerCharacterGroup().getGroupGameObject().setMap(null);
				if (removeActions) {
					GameState.getPlayerCharacterGroup().getGroupGameObject().removeAllActions();
				}
			} else {
				CharacterGroup.setMap(GameState.getPlayerCharacterGroup(), null, removeActions);
			}
			removedFromOld = true;
		}
		
		String loadingWhat = load();
		boolean loaded = loadingWhat == null;

		if (!loaded) {
			if (loadingIndicator == null) {
				buildLoadingIndicator();
			} 
			loadingIndicator.setWhat(loadingWhat);
			super.render(delta);
		} else {
			if (loadingIndicator != null) {
				loadingIndicator.remove();
				loadingIndicator = null;
			}
			GameMap map = newMap;
			if (startPos != null) {
				map.setStartCoordinates(startPos.getX(), startPos.getY());
			}

			if (!isLoadingSavegame) {
				Vector2 startCoords = map.getStartCoordinates();
				if (startCoords == null) {
					throw new GdxRuntimeException("Map "+map.getId()+" has no start coordinates defined!");
				}
				Array<GameCharacter> unpositioned = GameState
						.getPlayerCharacterGroup().setPosition(
								startCoords,
								map.getStartOrientation(), map);
				if (unpositioned.size > 0) {
					Log.log("Unpositioned characters found when loading map {0}, positionining on leader.",
							LogType.ERROR, map.getId());
				}
				for (GameCharacter character : unpositioned) {
					character.position().set(startCoords);
				}
				// reset the changed position state for every member to make
				// sure we do not remember coordinates from the previous map
				for (GameCharacter member : GameState.getPlayerCharacterGroup()
						.getMembers()) {
					member.position().markAsChanged();
				}
			} 
			
			if (map.isWorldMap()) {
				gameState.setStealth(false);
				GameState.getPlayerCharacterGroup().selectAll();
				GameState.getPlayerCharacterGroup().getGroupGameObject()
						.setMap(map);
			} else {
				CharacterGroup.setMap(GameState.getPlayerCharacterGroup(), map,
						removeActions);
			}

			map.onLoad();
			
			game.displayLocalMapScreen(map);
			gameState.setCurrentMap(map);
			
			if (oldMap != null) {
				String newGroup = map.getMapGroup();
				String oldGroup = oldMap.getMapGroup();
				if (!CoreUtil.equals(newGroup, oldGroup)) {
					if (oldGroup != null) {
						ObjectSet<GameMap> maps = gameState.getUndisposedMaps(oldGroup);
						if (maps != null) {
							for (GameMap undisposedMap : maps) {
								undisposedMap.dispose();
							}
							maps.clear();
						}
					}
					if (!oldMap.isDisposed()) {
						oldMap.dispose();
					}
				} else {
					if (oldGroup != null) {
						gameState.addUndisposedMap(oldMap);
					} else {
						oldMap.dispose();
					}
				}
			}
			
			gameState.unpauseGame();
			mapId = null;
		}

	}
	
	/**
	 * Sets the ID of the map to load. This is just the ID, not the internal id.
	 * 
	 * @param mapId
	 * @param combatMapData
	 */
	public void prepareForLoading(String mapId, CombatMapInitializationData combatMapData, LoadedCallback callback) {
		Music.stopPlayingMusic();
		mapId = mapId.toLowerCase(Locale.ENGLISH);
		newMap = gameState.getMapById(mapId);
		boolean loadTransactional = false;
		
		if (newMap == null) {
			if (combatMapData == null) {
				newMap = new GameMap(mapId);
			} else {
				newMap = new CombatGameMap(mapId);
			}
			loadTransactional = true;
		} else {
			newMap.undispose();
		}

		if (combatMapData != null) {
			((CombatGameMap) newMap).init(combatMapData);
		}

		if (!gameState.belongsToUndisposed(newMap)) {
			Assets.getAssetManager().load(
					Configuration.getFolderMaps() + mapId + ".tmx", TiledMap.class, new GameMapLoader.Parameters(newMap, loadTransactional, callback));
			mapLoaded = false;
		} else {
			mapLoaded = true;
		}
	}
	
	/**
	 * This uses AssetManager to asynchronously load the map and all assets.
	 * 
	 * It will return true only once everything is loaded.
	 * 
	 * This should be called repeatedly from the render method until true is
	 * returned.
	 * 
	 * 
	 * @return
	 */
	public String load() {
		if (!mapLoaded) {
			if (Assets.getAssetManager().update()) {
				mapLoaded = true;				
				AssetMap assetsToLoad = new AssetMap();
				newMap.gatherAssets(assetsToLoad);

				for (Entry<String, Class<?>> entry : assetsToLoad) {
					if (!Configuration.isGlobalAsset(entry.key)) {
						Assets.getAssetManager().load(entry.key,
								entry.value);
					}
				}
				return Strings.getString(UIManager.STRING_TABLE, "mapAssets");
			}
		} else {
			if (Assets.getAssetManager().update()) {
				return null;
			}
			return Strings.getString(UIManager.STRING_TABLE, "mapAssets");
		}
		return Strings.getString(UIManager.STRING_TABLE, "map");
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (loadingIndicator != null) {
			WindowPosition.CENTER.position(loadingIndicator);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (background != null) {
			background.dispose();
		}
	}

}
