package mg.fishchicken.graphics.renderers;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

/**
 * Simple renderer to draw filled polygons. The polygon supplied to the renderer
 * should have vertices scaled and positioned in the map coordinate system.
 * 
 * The renderer will not automatically update if the polygon's vertices or
 * positions changes - it needs to be notified manually via
 * {@link #setPolygon(Polygon, GameMap)}.
 *
 */
public class FilledPolygonRenderer {

	private GameMap map;
	private Polygon polygon;
	private PolygonRegion polyReg;
	private int[] boundingRectangleTiles;

	/**
	 * @param polygon
	 *            - in tile coordinates.
	 * @param map
	 *            - will be used to transform the vertices properly for rendering
	 */
	public FilledPolygonRenderer(Polygon polygon, GameMap map) {
		this.polygon = polygon;
		this.map = map;
	}

	public void setPolygon(Polygon polygon, GameMap map) {
		TextureRegion mapSolidTile = Assets.getTextureRegion(Configuration.getFileOrthogonalMapSolidWhiteTileTexture());
		// create a new region of size 1x1 in the middle of the solid tile texture
		TextureRegion pixel = new TextureRegion(mapSolidTile);
		float[] vertices = MathUtil.transformVerticesFromTileToScreen(
				polygon.getTransformedVertices(), map);
		polyReg = createdPolygonRegion(pixel, vertices);
		Rectangle rec = polygon.getBoundingRectangle();
		int width = (int) rec.getWidth();
		int height = (int) rec.getHeight();
		int x = (int) rec.getX();
		int y = (int) rec.getY();
		boundingRectangleTiles = new int[width * height * 2];

		int index = 0;
		for (int i = x; i < x + width; ++i) {
			for (int j = y; j < y + height; ++j) {
				boundingRectangleTiles[index++] = i;
				boundingRectangleTiles[index++] = j;
			}
		}
	}
	
	protected PolygonRegion createdPolygonRegion(TextureRegion pixel, float[] vertices) {
		return new PolygonRegion(pixel, vertices,
				new EarClippingTriangulator().computeTriangles(vertices)
				.toArray());
	}

	/**
	 * Gets the coordinates of the tiles that represent the filled bounding
	 * rectangle of the polygon this renderer renders.
	 * 
	 * @return
	 */
	public int[] getBoundingRectangleTiles() {
		return boundingRectangleTiles;
	}

	public void render(PolygonSpriteBatch batch) {
		// lazy init of the polyReg to make sure the map is fully loaded
		if (polyReg == null) {
			setPolygon(polygon, map);
		}
		batch.draw(polyReg, 0, 0);
	}

}