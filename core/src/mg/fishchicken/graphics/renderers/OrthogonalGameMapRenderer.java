package mg.fishchicken.graphics.renderers;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import java.util.Comparator;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.graphics.Drawable;
import mg.fishchicken.maps.Cell;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Keys;

public class OrthogonalGameMapRenderer extends GameMapRenderer {
	
	private float[] vertices = new float[20];
	private Comparator<Drawable> drawableComparator;
	
	public OrthogonalGameMapRenderer(GameState gameState, GameMap map, PlayerCharacterController controller) {
		super(gameState, map, controller);
		drawableComparator = new OrthogonalDrawOrderComparator();
	}
	
	@Override
	public float getTileHeight(TiledMapTileLayer layer) {
		return layer.getTileHeight() * unitScaleY;
	}
	
	@Override
	public float getTileWidth(TiledMapTileLayer layer) {
		return layer.getTileWidth() * unitScaleX;
	}
	
	@Override
	public void renderTileLayer (float delta, TiledMapTileLayer layer) {
		
		boolean overHeadLayer = map.getOverheadLayers().contains(layer, false);
		
		float sunColor = map.getLightsRayHandler().ambientLight.toFloatBits();
		
		boolean gridLayer = layer.getProperties().get(PROP_RENDER_GRID) != null;
		boolean renderGrid = gridLayer && shouldRenderGrid();
		
		final float layerTileWidth = getTileWidth(layer);
		final float layerTileHeight = getTileHeight(layer);
		
		int mapWidth = map.getMapWidth();
		int mapHeight = map.getMapHeight();
		Rectangle rectangle = getCullingRectangle();
		int xMin = MathUtils.clamp((int) rectangle.x, 0, mapWidth);
		int yMin = MathUtils.clamp((int) rectangle.y, 0, mapHeight);
		int xMax = MathUtils.clamp((int) (xMin+rectangle.width), 0, mapWidth);
		int yMax = MathUtils.clamp((int) (yMin+rectangle.height), 0, mapHeight);
		
		float xStart = xMin * layerTileWidth;	
		
		for (int row = yMin; row < yMax; row++) {
			for (int col = xMin; col < xMax; col++) {
				int tileNumber = col+row*map.getMapWidth();
				if (map.shouldRenderTile(tileNumber)) {
					renderTile(delta, layer, col, row, overHeadLayer ?  sunColor : getTileColor(tileNumber), layerTileWidth, layerTileHeight, null);
				}
			}
		}
		
		if (gridLayer) {
			renderTransitions(yMin, yMax, xMin, xMax, layerTileWidth, layerTileHeight);
		}
		
		if (renderGrid) {
			renderGrid(yMin, yMax, xMin, xMax, xStart, layerTileWidth, layerTileHeight, layer);
		}
	}
	
	@Override
	public void renderTile(float delta, TiledMapTileLayer layer, int col, int row, float color, float layerTileWidth, float layerTileHeight, TextureRegion texture) {
		final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
		if(cell == null) {
			return;
		}
		final TiledMapTile tile = cell.getTile();
		if (tile == null) {
			return;
		}
		
		final boolean flipX = cell.getFlipHorizontally();
		final boolean flipY = cell.getFlipVertically();
		final int rotations = cell.getRotation();
		
		if (cell instanceof Cell) {
			((Cell) cell).updateStateTime(delta);
		}
		
		TextureRegion region = texture != null ? texture : tile.getTextureRegion();
		
		float x1 = col *layerTileWidth;
		float y1 = row * layerTileHeight;
		float x2 = x1 + region.getRegionWidth() * unitScaleX;
		float y2 = y1 + region.getRegionHeight() * unitScaleY;
		
		float u1 = region.getU();
		float v1 = region.getV2();
		float u2 = region.getU2();
		float v2 = region.getV();
		
		vertices[X1] = x1;
		vertices[Y1] = y1;
		vertices[C1] = color;
		vertices[U1] = u1;
		vertices[V1] = v1;
		
		vertices[X2] = x1;
		vertices[Y2] = y2;
		vertices[C2] = color;
		vertices[U2] = u1;
		vertices[V2] = v2;
		
		vertices[X3] = x2;
		vertices[Y3] = y2;
		vertices[C3] = color;
		vertices[U3] = u2;
		vertices[V3] = v2;
		
		vertices[X4] = x2;
		vertices[Y4] = y1;
		vertices[C4] = color;
		vertices[U4] = u2;
		vertices[V4] = v1;							
		
		if (flipX) {
			float temp = vertices[U1];
			vertices[U1] = vertices[U3];
			vertices[U3] = temp;
			temp = vertices[U2];
			vertices[U2] = vertices[U4];
			vertices[U4] = temp;
		}
		if (flipY) {
			float temp = vertices[V1];
			vertices[V1] = vertices[V3];
			vertices[V3] = temp;
			temp = vertices[V2];
			vertices[V2] = vertices[V4];
			vertices[V4] = temp;
		}
		if (rotations != 0) {
			switch (rotations) {
				case Cell.ROTATE_90: {
					float tempV = vertices[V1];
					vertices[V1] = vertices[V2];
					vertices[V2] = vertices[V3];
					vertices[V3] = vertices[V4];
					vertices[V4] = tempV;

					float tempU = vertices[U1];
					vertices[U1] = vertices[U2];
					vertices[U2] = vertices[U3];
					vertices[U3] = vertices[U4];
					vertices[U4] = tempU;									
					break;
				}
				case Cell.ROTATE_180: {
					float tempU = vertices[U1];
					vertices[U1] = vertices[U3];
					vertices[U3] = tempU;
					tempU = vertices[U2];
					vertices[U2] = vertices[U4];
					vertices[U4] = tempU;									
					float tempV = vertices[V1];
					vertices[V1] = vertices[V3];
					vertices[V3] = tempV;
					tempV = vertices[V2];
					vertices[V2] = vertices[V4];
					vertices[V4] = tempV;
					break;
				}
				case Cell.ROTATE_270: {
					float tempV = vertices[V1];
					vertices[V1] = vertices[V4];
					vertices[V4] = vertices[V3];
					vertices[V3] = vertices[V2];
					vertices[V2] = tempV;

					float tempU = vertices[U1];
					vertices[U1] = vertices[U4];
					vertices[U4] = vertices[U3];
					vertices[U3] = vertices[U2];
					vertices[U2] = tempU;									
					break;
				}
				default: break;
			}								
		}
		spriteBatch.draw(region.getTexture(), vertices, 0, 20);
	}
	
	public void renderGrid (int row1, int row2, int col1, int col2, float xStart, float tileWidth, float tileHeight, TiledMapTileLayer layer) {
		spriteBatch.setColor(1, 1, 1, 0.3f);
		if (xStart < 0) {
			xStart = 0;
		}
		float xMax = map.getWidth();
		float y = row1 * tileHeight;
		TextureRegion texture = map.getGridTexture();
		float width = map.getTileSizeX() * map.getScaleX();
		float height = map.getTileSizeY() * map.getScaleY();
		for (int row = row1; row < row2; row++) {
			float x = xStart;
			if (x < 0) {
				x = 0;
			}
			for (int col = col1; col < col2 && x < xMax; col++) {
				final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
				if (cell != null && cell.getTile() != null && map.shouldRenderTile(col, row)) {
					spriteBatch.draw(texture, x, y, width, height);
				}
				x += tileWidth;
			}
			y += tileHeight;
		}		
		spriteBatch.setColor(1, 1, 1, 1);
	}
	
	public void renderTransitions (int row1, int row2, int col1, int col2, float tileWidth, float tileHeight) {
		spriteBatch.setColor(1, 1, 1, 0.3f);
		Keys<Vector2> coordinates = map.getTransitionTileCoordinates();
		for (Vector2 coordinate : coordinates) {
			if (map.shouldRenderTile((int) coordinate.x, (int) coordinate.y)
					&& coordinate.x >= col1 && coordinate.x <= col2
					&& coordinate.y >= row1 && coordinate.y <= row2) {
				spriteBatch.draw(map.getTransitionTexture(),coordinate.x*tileWidth,coordinate.y*tileHeight, map.getTileSizeX()
						* map.getScaleX(), map.getTileSizeY() * map.getScaleY());
			}
					
		}
		spriteBatch.setColor(1, 1, 1, 1);
	}
	
	@Override
	protected Array<Drawable> sortDrawables(Array<Drawable> drawables) {
		drawables.sort(drawableComparator);
		return drawables;
	}
}
