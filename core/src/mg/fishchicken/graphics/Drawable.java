package mg.fishchicken.graphics;

import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.graphics.renderers.GameMapRenderer;
import mg.fishchicken.graphics.renderers.IsometricDrawableSorter;
import mg.fishchicken.graphics.renderers.OrthogonalDrawOrderComparator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;


public interface Drawable extends PositionedThing {
	
	/**
	 * Draw the drawable using the supplied SpriteBatch.
	 * 
	 * @param spriteBatch
	 * @param deltaTime
	 */
	public void draw(GameMapRenderer renderer, float deltaTime);
	
	/**
	 * Gets the z-index of the Drawable, which dictates
	 * in which order should the drawables be drawn (lower value
	 * means it will be drawn first).
	 * 
	 * This is used mostly in the {@link OrthogonalDrawOrderComparator}. 
	 * The {@link IsometricDrawableSorter} uses more complex logic 
	 * to determine draw order.
	 * @return
	 */
	public float getZIndex();
	
	public float getWidth();
	
	public float getHeight();
	
	/**
	 * Whether or not should the drawable be drawn.
	 * 
	 * The cullingRectangle parameter is optional and can be null.
	 * 
	 * @return
	 */
	public boolean shouldDraw(Rectangle cullingRectangle);
	
	/**
	 * If true, this drawable is always drawn behind all other drawables (i.e. first).
	 * 
	 * @return
	 */
	public boolean isAlwaysBehind();
	
	/**
	 * If true, this drawable is always drawn if fron of all other drawables (i.e. last).
	 * 
	 * @return
	 */
	public boolean isAlwaysInFront();
	
	public Color getColor();
	
	/**
	 * Returns the color to use to highlight this Drawable if the mouse cursor
	 * is at the supplied tile coordinates. If null is returned, the Drawable
	 * should not be highlighted at all.
	 * 
	 * @return
	 */
	public Color getHighlightColor(float x, float y);
	
	/**
	 * Returns the amount of highlight that should be applied to this Drawable if the mouse cursor
	 * is at the supplied tile coordinates. If 0 is returned, the Drawable
	 * should not be highlighted at all.
	 * 
	 * @return
	 */
	public int getHighlightAmount(float x, float y);
}
