package mg.fishchicken.graphics;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.graphics.renderers.GameMapRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class DrawableGameObject extends GameObject implements Drawable {

	private TextureRegion texture; 
	private float xOffset, yOffset;
	
	/**
	 * Empty constructor for game loading.
	 */
	public DrawableGameObject() {
		super();
	}
	
	public DrawableGameObject(String id, String type) {
		super(id, type);
	}

	public void setTexture(TextureRegion texture) {
		this.texture =texture;  
	}
	
	public TextureRegion getTexture() {
		return texture;
	}
	
	/**
	 * Sets the offsets as well - drawable GOs
	 * are rendered in the middle of the map tile by default.
	 */
	@Override
	public void setMap(GameMap map) {
		super.setMap(map);
		if (map != null && map.isIsometric()) {
			setOffsets(((map.getTileSizeX()/2)*map.getScaleX()), -((map.getTileSizeX()/2)*map.getScaleY()));
		}
	}
	
	/**
	 * Sets the coordinate offsets that should be used when drawing the
	 * animations.
	 * 
	 * @param xOffset
	 * @param yOffset
	 */
	public void setOffsets(float xOffset, float yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	
	public float getXOffset() {
		return xOffset;
	}
	
	public float getYOffset() {
		return yOffset;
	}
	
	public void draw(GameMapRenderer renderer, float deltaTime) {
		Vector2 projectedCoordinates = MathUtil.getVector2();
		getMap().projectFromTiles(position.setVector2(projectedCoordinates));
		renderer.getSpriteBatch().draw(getTexture(), projectedCoordinates.x
						+ xOffset, projectedCoordinates.y + yOffset,
				getTexture().getRegionWidth() * getMap().getScaleX(), getTexture()
						.getRegionHeight() * getMap().getScaleY());
		MathUtil.freeVector2(projectedCoordinates);
	}

	@Override
	public boolean shouldDraw(Rectangle cullingRectangle) {
		if (getMap() != null) {
			if (!getMap().isCurrentMap()) {
				return false;
			}
			if (cullingRectangle != null && !cullingRectangle.contains(position.getX(), position.getY())) {
				return false;
			}
			return isVisibleToPC();
		}
		return false;
	}

	@Override
	public boolean isAlwaysBehind() {
		return false;
	}
	
	@Override
	public boolean isAlwaysInFront() {
		return false;
	}
	
	@Override
	public Color getHighlightColor(float x, float y) {
		return null;
	}
	
	@Override
	public int getHighlightAmount(float x, float y) {
		return 0;
	}
	
	@Override
	public float getZIndex() {
		return -getYCamera();
	}
}
