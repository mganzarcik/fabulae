package mg.fishchicken;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.locations.CombatGameMap.CombatMapInitializationData;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.screens.GameMapScreen;
import mg.fishchicken.screens.LoadingMapScreen;
import mg.fishchicken.screens.ModuleLoadingScreen;
import mg.fishchicken.screens.ModuleSelectionScreen;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;

public class FishchickenGame extends Game {

	private GameMapScreen localMapScreen;
	private LoadingMapScreen loadingMapScreen;
	private GameState gameState;
	
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
	
	@Override
	public void create() {
		setScreen(new ModuleLoadingScreen(this));
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		Configuration.setScreenWidth(width);
		Configuration.setScreenHeight(height);
	};
	
	@Override
	public void dispose () {
		gameState = null;
		Configuration.writeOptions(Gdx.files);
		disposeScreens();
		super.dispose();
		UIManager.dispose();
		Assets.getAssetManager().dispose();
	}
	
	private void disposeScreens() {
		if (loadingMapScreen != null) {
			loadingMapScreen.dispose();
			loadingMapScreen = null;
		}
		if (localMapScreen != null) {
			localMapScreen.dispose();
			localMapScreen = null;
		}
	}
	
	/**
	 * This will make the game first load the map 
	 * (displaying the LoadingMapScreen) and then 
	 * actually display the map
	 * @param mapId
	 */
	public void switchToMap(String mapId) {
		switchToMap(mapId, null, null, false, null, null);
	}
	
	/**
	 * This will make the game first load the map 
	 * (displaying the LoadingMapScreen) and then 
	 * actually display the map
	 * @param mapId
	 */
	public void switchToMapAfterLoading(String mapId,LoadedCallback callback) {
		switchToMap(mapId, null, null, true, null, callback);
	}
	
	/**
	 * This will make the game first load the map 
	 * (displaying the LoadingMapScreen) and then 
	 * actually display the map.
	 * 
	 * @param mapId
	 */
	public void switchToMap(String mapId, GameMap oldMap, Tile startPos, boolean isLoadingSavegame) {
		switchToMap(mapId, oldMap, startPos, isLoadingSavegame, null, null);
	}
	
	/**
	 * This will make the game first load the map 
	 * (displaying the LoadingMapScreen) and then 
	 * actually display the map
	 * @param mapId
	 */
	public void switchToCombatMap(String mapId, GameMap oldMap, CombatMapInitializationData data) {
		switchToMap(mapId, oldMap, null, false, data,  null);
	}
	
	/**
	 * This will make the game first load the map 
	 * (displaying the LoadingMapScreen) and then 
	 * actually display the map
	 * @param newMapId
	 */
	public void switchToMap(String newMapId, GameMap oldMap, Tile startPos, boolean isLoadingSavegame, CombatMapInitializationData data, LoadedCallback callback) {
		if (loadingMapScreen == null) {
			loadingMapScreen = new LoadingMapScreen(this, gameState);
		} 
		
		loadingMapScreen.init(newMapId, oldMap, startPos, isLoadingSavegame, data, callback);
		setScreen(loadingMapScreen);
	}
	
	/**
	 * This is usually called by the LoadingMapScreen once the loading is done
	 * to actually display the map.
	 * 
	 * @param map
	 * @return
	 */
	public void displayLocalMapScreen(GameMap map) {
		if (localMapScreen == null) {
			localMapScreen =  new GameMapScreen(map, gameState);
		} else {
			localMapScreen.init(map);
		}
		setScreen(localMapScreen);
	}
	
	public void displayModuleSelectionScreen() {
		setScreen(new ModuleSelectionScreen(this));
	}
	
	public void reloadGame() {
		gameState = null;
		disposeScreens();
		Assets.getAssetManager().clear();
		setScreen(new ModuleLoadingScreen(this));
	}
}


