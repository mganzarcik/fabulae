package mg.fishchicken.core.input;


import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class MainInputProcessor implements InputProcessor {

	private Array<InputConsumer> consumers;
	private Camera camera;
	private GameMap map;
	private boolean dragInProgress = false;
	private Pool<Vector3> vectorPool = Pools.get(Vector3.class);
	
	public enum EventType {
		KeyDown, KeyUp, KeyTyped, TouchDown, TouchUp,TouchDragged, MouseMoved, Scrolled
	}
	
	public MainInputProcessor(Camera camera, GameMap map) {
		this.camera = camera;
		this.map = map;
		consumers = new Array<InputConsumer>();
	}
	
	public GameMap getMap() {
		return map;
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public void registerInputConsumer(InputConsumer consumer) {
		consumers.add(consumer);
		consumer.setMainInputProcessor(this);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		boolean returnValue = false; 
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			returnValue = returnValue || consumer.keyDown(keycode);
		}
		return returnValue;
	}

	@Override
	public boolean keyUp(int keycode) {
		boolean returnValue = false; 
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			returnValue = returnValue || consumer.keyUp(keycode);
		}
		return returnValue;
	}

	@Override
	public boolean keyTyped(char character) {
		boolean returnValue = false; 
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			returnValue = returnValue || consumer.keyTyped(character);
		}
		return returnValue;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		boolean returnValue = false; 
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			if (consumer.needsInputTranslated(EventType.TouchDown)) {
				Vector3 tempVector = vectorPool.obtain();
				tempVector.set(screenX, screenY, 0);
				camera.unproject(tempVector);
				returnValue = returnValue || consumer.touchDown(tempVector.x, tempVector.y, pointer, button);
				vectorPool.free(tempVector);
			} else {
				returnValue = returnValue || consumer.touchDown(screenX, screenY, pointer, button);
			}
		}
		return returnValue;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		boolean returnValue = false; 
		
		Vector3 touchCoordinates = vectorPool.obtain();
		Vector3 touchCoordinatesTranslated = vectorPool.obtain();
		touchCoordinates.set(screenX, screenY, 0);
		translateFromWindowToCamera(touchCoordinatesTranslated.set(touchCoordinates));
		
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			Vector3 coordinates = touchCoordinates;
			if (consumer.needsInputTranslated(EventType.TouchUp)) {
				coordinates = touchCoordinatesTranslated;
			}
			returnValue = returnValue || dragInProgress ? consumer
					.touchDragFinished(coordinates.x, coordinates.y, pointer,
							button) : consumer.touchUp(coordinates.x,
									coordinates.y, pointer, button);
				
		} 
		vectorPool.free(touchCoordinates);
		vectorPool.free(touchCoordinatesTranslated);
		dragInProgress = false;
		return returnValue;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		dragInProgress = true;
		boolean returnValue = false; 
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			returnValue = returnValue || consumer.touchDragged(screenX, screenY, pointer);
		}
		return returnValue;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		boolean returnValue = false; 
		
		Vector3 touchCoordinates = vectorPool.obtain();
		Vector3 touchCoordinatesTranslated = vectorPool.obtain();
		touchCoordinates.set(screenX, screenY, 0);
		translateFromWindowToCamera(touchCoordinatesTranslated.set(touchCoordinates));
		
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			Vector3 coordinates = touchCoordinates;
			if (consumer.needsInputTranslated(EventType.MouseMoved)) {
				coordinates = touchCoordinatesTranslated;
			}
			returnValue = returnValue || consumer.mouseMoved(coordinates.x, coordinates.y);
		}
		return returnValue;
	}

	@Override
	public boolean scrolled(int amount) {
		boolean returnValue = false; 
		for (int i = 0; i < consumers.size; ++i) {
			InputConsumer consumer = consumers.get(i);
			returnValue = returnValue || consumer.scrolled(amount);
		}
		return returnValue;
	}

	
	public void translateFromCameraToWindow(Vector2 vector) {
		Vector3 tempVector = vectorPool.obtain();
		tempVector.set(vector.x, vector.y, 0);
		camera.project(tempVector);
		vector.set(tempVector.x, tempVector.y);
		vectorPool.free(tempVector);
	}
	
	
	public Vector3 translateFromWindowToCamera(Vector3 vector) {
		camera.unproject(vector);
		return vector;
	}

}
