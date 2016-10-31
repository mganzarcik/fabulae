package mg.fishchicken.ui.map;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.tools.MinimapGenerator;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;

/**
 * A minimap component. Supports changing of the camera position by dragging the camera
 * indicator around or by clicking on the map.
 *  
 * @author ANNUN
 *
 */
public class Map extends Table implements Disposable, EventListener {
	private Texture mapTexture;
	private MapScreenIndicator indicator = null;
	private float xRatio, yRatio;
	private GameMap gameMap;
	private boolean indicatorDragged = false;
	private float dragStartX, dragStartY;
	private GameState gameState;
	
	public Map(GameMap map, GameState gameState, int width, int height, ButtonStyle mapIndicatorStyle) {
		mapTexture = MinimapGenerator.generate(map, gameState, width, height);
		this.gameMap = map;
		this.gameState = gameState;
		calculateRatios(width, height);
		
		Image image = new Image(mapTexture);
		image.addListener(this);
		// only add the current camera position indicator in case 
		// the camera does not show the whole map already
		if (xRatio > 1 || yRatio > 1) {
			indicator = new MapScreenIndicator(mapIndicatorStyle);
		}
		
		WidgetGroup group = new WidgetGroup();
		group.addActor(image);
		if (indicator != null) {
			group.addActor(indicator);
			indicator.addListener(this);
		}
		group.setWidth(width);
		group.setHeight(height);
		group.invalidate();
		add(group).width(width).height(height);
	}
	
	private void calculateRatios(int myWidth, int myHeight) {
		float mapRealWidth;
		float mapRealHeight;
		if (gameMap.isIsometric()) {
			mapRealWidth = (gameMap.getWidth() + gameMap.getHeight()) * gameMap.getTileSizeX();
			mapRealHeight = (gameMap.getWidth() + gameMap.getHeight()) * gameMap.getTileSizeY() * 0.5f;
		} else {
			mapRealWidth = gameMap.getWidth() * gameMap.getTileSizeX();
			mapRealHeight = gameMap.getHeight() * gameMap.getTileSizeY();
		}
		xRatio = mapRealWidth / myWidth;
		yRatio = mapRealHeight / myHeight;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if (indicator != null) {
			updateIndicatorSize();
			if (!indicatorDragged) {
				udpateIndicatorPositionBasedOnCamera();
			}
		}
	}
	
	private void udpateIndicatorPositionBasedOnCamera() {
		Camera camera = gameMap.getCamera();
		float newX = ((camera.position.x - (camera.viewportWidth / 2)) * gameMap.getTileSizeX()) / xRatio;
		float newY;
		if (gameMap.isIsometric()) {
			newY = ((camera.position.y - (camera.viewportHeight / 2) + (gameMap.getWidth() + gameMap.getHeight()) / 4) * gameMap.getTileSizeY()) / yRatio;
		} else {
			newY = ((camera.position.y - (camera.viewportHeight / 2)) * gameMap.getTileSizeY()) / yRatio;
		}
		
		indicator.setPosition(newX, newY);
	}
	
	private void udpateCameraPositionBasedOnIndicator() {
		Camera camera = gameMap.getCamera();
		float newX =  ((indicator.getX() + indicator.getWidth() / 2) * xRatio) / gameMap.getTileSizeX();
		float newY = ((indicator.getY() +  indicator.getHeight() / 2 - (gameMap.isIsometric() ? mapTexture.getHeight()  / 2 : 0)) * yRatio) / gameMap.getTileSizeY();
		camera.position.set(newX, newY, camera.position.z);
		gameState.cameraMoved();
	}
	
	private void updateIndicatorSize() {
		indicator.setWidth(Gdx.graphics.getWidth() / xRatio);
		indicator.setHeight(Gdx.graphics.getHeight() / yRatio);
	}
 	
	public void dispose() {
		setVisible(false);
		remove();
		mapTexture.dispose();
	}

	@Override
	public boolean handle(Event e) {
		if (indicator != null && (e instanceof InputEvent)) {
			InputEvent event = (InputEvent) e;
			Type type = event.getType();
			if (indicator == event.getTarget()) {
				if (Type.touchDown.equals(type)) {
					indicatorDragged = true;
					Vector2 vector2 = MathUtil.getVector2();
					event.toCoordinates(indicator, vector2);
					dragStartX = vector2.x;
					dragStartY = vector2.y;
					MathUtil.freeVector2(vector2);
					return true;
				}
			}
			if (Type.touchDragged.equals(type) && indicatorDragged) {
				Vector2 vector2 = MathUtil.getVector2();
				event.toCoordinates(indicator, vector2);
				updateIndicatorPosition(indicator.getX() + vector2.x - dragStartX, indicator.getY() + vector2.y - dragStartY);
				MathUtil.freeVector2(vector2);
				return true;
			}
			if (Type.touchUp.equals(type)) { 
				indicatorDragged = false;
				return true;
			}
			
			if (Type.touchDown.equals(type)) { 
				Vector2 vector2 = MathUtil.getVector2();
				event.toCoordinates(event.getTarget(), vector2);
				updateIndicatorPosition(vector2.x - indicator.getWidth()/2, vector2.y - indicator.getHeight() / 2);
				MathUtil.freeVector2(vector2);
				return true;
			}
		}
		return true;
	}
	
	private void updateIndicatorPosition(float newX, float newY) {
		newX = MathUtils.clamp(newX, 0, mapTexture.getWidth() - indicator.getWidth());
		newY = MathUtils.clamp(newY, 0, mapTexture.getHeight() - indicator.getHeight());
		indicator.setPosition(newX, newY);
		udpateCameraPositionBasedOnIndicator();
	}
}
