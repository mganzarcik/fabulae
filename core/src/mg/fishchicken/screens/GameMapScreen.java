package mg.fishchicken.screens;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.input.CameraController;
import mg.fishchicken.core.input.MainInputProcessor;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.graphics.renderers.GameMapRenderer;
import mg.fishchicken.graphics.renderers.IsometricGameMapRenderer;
import mg.fishchicken.graphics.renderers.OrthogonalGameMapRenderer;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class GameMapScreen implements Screen {
	
	private OrthographicCamera camera;
    private MainInputProcessor mip;
    private GameMap map;
    private CameraController cameraController;
    private PlayerCharacterController groupController;
	private GameMapRenderer gameMapRendererIso;
	private GameMapRenderer gameMapRendererOrto;
	private GameMapRenderer renderer;
	private GameState gameState;
	
    public GameMapScreen(GameMap map, GameState global) {
    	this.gameState = global;
		camera = new OrthographicCamera(getCameraWidth(map, Gdx.graphics.getWidth()),
				getCameraHeight(map, Gdx.graphics.getHeight()));
		cameraController = new CameraController(gameState, camera);
		groupController = gameState.getPlayerCharacterController();
		init(map);
    }
    
	private float getCameraHeight(GameMap map, final int height) {
		return (int) (height / map.getTileSizeY() / Configuration.getMapScale());
	}
    
    private int getCameraWidth(GameMap map, final int width) {
    	return (int) (width / map.getTileSizeX() / Configuration.getMapScale());
    }
    
    public void init(GameMap map) {
    	this.map = map;
    	
    	if (map.isIsometric()) {
    		if (gameMapRendererIso == null) {
    			gameMapRendererIso = new IsometricGameMapRenderer(gameState, map, groupController);
    		}
    		renderer = gameMapRendererIso;
    	} else {
    		if (gameMapRendererOrto == null) {
    			gameMapRendererOrto = new OrthogonalGameMapRenderer(gameState, map, groupController);
    		}
    		renderer = gameMapRendererOrto;
    	}
    	renderer.setMap(map);
    	map.setCamera(camera);
		mip = new MainInputProcessor(camera, map);
		cameraController.setMap(map);
		mip.registerInputConsumer(cameraController);
		mip.registerInputConsumer(groupController);
		groupController.reset();
		
		Vector2 startPost = map.projectFromTiles(map.getStartCoordinates());
		camera.position.x = startPost.x;
		camera.position.y = startPost.y;
		
		Gdx.input.setInputProcessor(new InputMultiplexer(UIManager.getStage(), mip));
		UIManager.refreshPCPanel();
    }
    
	@Override
	public void render(float delta) {
		// get any missing assets
		Assets.getAssetManager().update();
		
		// update phase
		gameState.update(delta, camera);
		UIManager.updateUI(delta);
		cameraController.updateCamera(delta);		
		camera.update();
		
		// render phase
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		renderer.render(delta, camera);
		UIManager.drawUI();
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportHeight = getCameraHeight(map, height);
		camera.viewportWidth = getCameraWidth(map, width);
		renderer.resize();
		cameraController.limitCamera();
		UIManager.onResize(width, height);
		gameState.screenResized();
	}

	@Override
	public void show() {
		
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		map.dispose();
		if (gameMapRendererIso != null) {
			gameMapRendererIso.dispose();
		}
		if (gameMapRendererOrto != null) {
			gameMapRendererOrto.dispose();
		}
	}

}
