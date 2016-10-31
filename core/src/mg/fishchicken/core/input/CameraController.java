package mg.fishchicken.core.input;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.input.MainInputProcessor.EventType;
import mg.fishchicken.core.util.InputUtil;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/**
 * Controls the camera above the displayed map.
 * 
 * @author Annun
 *
 */
public class CameraController implements InputConsumer {
	final Vector3 last = new Vector3(-1, -1, -1);
	final Camera camera;
	private GameMap map;
	private int keyScrollingX;
	private int keyScrollingY;
	private int edgeScrollingX;
	private int edgeScrollingY;
	private GameState gameState;
	private MainInputProcessor mip;

	/**
	 * Constructs a new CameraController for the supplied camera.
	 * 
	 * Please note that if you do not associated the controller
	 * with a GameMap as well by calling setMap(GameMap), the camera
	 * will be able to move freely and will not be limited
	 * by bounds of any map.
	 * 
	 * @param camera
	 */
	public CameraController (GameState gameState, Camera camera) {
		this.camera = camera;
		keyScrollingX = 0;
		keyScrollingY = 0;
		edgeScrollingX = 0;
		edgeScrollingY = 0;
		this.gameState = gameState;
	}
	
	/**
	 * Sets the map of this camera to the supplied map.
	 * 
	 * @param map
	 * @return
	 */
	public void setMap(GameMap map) {
		this.map = map;
	}

	@Override
	public boolean touchDragged (float x, float y, int pointer) {
		//TODO make camera controllable by dragging somehow
		/*
		if (!(last.x == -1 && last.y == -1)) {
			camera.position.add((last.x - x)/Configuration.TileSize, (y-last.y)/Configuration.TileSize, 0);
			if (map != null) {
				map.limitCamera(camera);
			}
		}
		last.set(x, y, 0);
		*/
		return false;
	}
	@Override
	public boolean keyDown(int keycode) {
		if (InputUtil.isArrowKey(keycode)) {
			keyScrollingX = Keys.LEFT == keycode ? -1 : (Keys.RIGHT == keycode ? 1 : keyScrollingX);
			keyScrollingY = Keys.DOWN == keycode ? -1 : (Keys.UP == keycode ? 1 : keyScrollingY);
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (InputUtil.isArrowKey(keycode)) {
			if (Keys.LEFT == keycode || Keys.RIGHT == keycode) {
				keyScrollingX = 0;
			} else if (Keys.DOWN == keycode || Keys.UP == keycode) {
				keyScrollingY= 0;
			}
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(float screenX, float screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(float screenX, float screenY, int  pointer, int button) {
		return false;
	}

	@Override
	public boolean mouseMoved(float screenX, float  screenY) {
		int offset = Configuration.getScrollAreaStartOffset();
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		if (screenX < offset) {
			edgeScrollingX = -1;
		} else if (screenX > width - offset ) {
			edgeScrollingX = 1;
		} else {
			edgeScrollingX = 0;
		}
		if (screenY < offset) {
			edgeScrollingY = 1;
		} else if (screenY > height - offset) {
			edgeScrollingY = -1;
		} else {
			edgeScrollingY = 0;
		}
		return false;
	}

	@Override
	public boolean scrolled(float  amount) {
		/*float mod = amount < 0 ? -1f : 1f;
		camera.viewportWidth += mod;
		camera.viewportHeight += mod;*/
		return false;
	}


	@Override
	public boolean touchDragFinished(float screenX, float screenY, int pointer, int button) {
		//last.set(-1, -1, -1);
		return false;
	}
	
	@Override
	public boolean needsInputTranslated(EventType eventtype) {
		return false;
	}

	@Override
	public void setMainInputProcessor(MainInputProcessor mip) {
		this.mip = mip;
	}
	
	/**
	 * This should be called each frame to update the camera
	 * in case it is being scrolled.
	 * @param deltaTime
	 */
	public void updateCamera(float deltaTime) {
		int scrollX = keyScrollingX | edgeScrollingX;
		int scrollY = keyScrollingY | edgeScrollingY;
		if (scrollX != 0 || scrollY != 0) {
			float scrollAmount = deltaTime * Configuration.getScrollSpeed();
			camera.position.add(scrollX *scrollAmount, scrollY*scrollAmount, 0);
			if (map != null) {
				limitCamera();
			}
			gameState.cameraMoved();
			// notify all controllers that the mouse cursor positions
			// has changed within the game world, since the camera has moved
			if (mip != null) {
				mip.mouseMoved(Gdx.input.getX(), Gdx.input.getY());
			}
		}
	}

	
	/**
	 * Sets the camera coordinates so that it
	 * cannot leave the map area.
	 * 
	 * @param camera
	 */
	public void limitCamera() {
		// make sure the camera cannot leave the map
		float xMin =  camera.viewportWidth/2f;
		float xMax = map.getMapWidth()*2f - camera.viewportWidth/2f;
		float yMin = -map.getMapHeight()/2f + camera.viewportHeight/2f;
		float yMax = map.getMapHeight()-map.getMapHeight()/2f - camera.viewportHeight/2f;
		if (!map.isIsometric()) {
			xMin = (float) Math.ceil(camera.viewportWidth/2);
			xMax = map.getMapWidth() <  camera.viewportWidth ? map.getMapWidth() / 2f : map.getMapWidth() - camera.viewportWidth/2;
			yMin = (float) Math.ceil(camera.viewportHeight/2);
			yMax = map.getMapHeight() <  camera.viewportHeight ? map.getMapHeight() / 2f : map.getMapHeight()-camera.viewportHeight/2;
		} 
		
		if (camera.position.x < xMin) {
			camera.position.x = xMin;
		}
		if (camera.position.x > (int)xMax)  {
			camera.position.x = (int)xMax;
		}
		if (camera.position.y < yMin) {
			camera.position.y = yMin;
		}
		if (camera.position.y > (int)yMax) {
			camera.position.y = (int)yMax;
		}
	}
	
}
