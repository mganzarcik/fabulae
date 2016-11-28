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
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.graphics.Drawable;
import mg.fishchicken.maps.Cell;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Keys;

public class IsometricGameMapRenderer extends GameMapRenderer {

	private float[] vertices = new float[20];
	private IsometricDrawableSorter drawableSorter;

	public IsometricGameMapRenderer(GameState gameState, GameMap map,
			PlayerCharacterController controller) {
		super(gameState, map, controller);
		drawableSorter = new IsometricDrawableSorter();
	}
	
	@Override
	public float getTileWidth(TiledMapTileLayer layer) {
		return layer.getTileWidth() * unitScaleX * 0.5f;
	}
	
	@Override
	public float getTileHeight(TiledMapTileLayer layer) {
		return layer.getTileHeight() * unitScaleY * 0.5f;
	}
	
	public void renderTileLayer(float delta, TiledMapTileLayer layer) {

		boolean gridLayer = layer.getProperties().get(PROP_RENDER_GRID) != null;
		boolean renderGrid = gridLayer && shouldRenderGrid();

		float halfTileWidth = getTileWidth(layer);
		float halfTileHeight = getTileHeight(layer);
		
		/*
		 * TODO: more advanced culling, see
		 * http://www.java-gaming.org/index.php?topic=24922.0 
		 * need to decide at some point what to use
		 * 
		 * int startX = (int) MathUtils.clamp(lowLeft.x - 4, 0, layer.getWidth()
		 * - 1); int minY = MathUtils.clamp(row1 - 1, 0, layer.getHeight() - 1);
		 * int maxY = MathUtils.clamp(row2 + 5, 0, layer.getHeight() - 1); int
		 * minX = MathUtils.clamp(col1 - 4, 0, layer.getWidth() - 1); int maxX =
		 * MathUtils.clamp(col2 + 3, 0, layer.getWidth() - 1);
		 * 
		 * boolean nBump = false, mBump = false; int n = 0, nBuffer = 1; int m =
		 * 1, mBuffer = 0;
		 * 
		 * for (int y = maxY; y >= minY; y--) { for (int x = startX - n; x <
		 * startX + m; x++) { int tileNumber = x+y*map.getWidth(); if
		 * (map.shouldRenderTile(tileNumber)) { renderTile(layer, x, y,
		 * map.getTileColor(tileNumber), halfTileWidth, halfTileHeight); } } if
		 * (!nBump) { n++; if ((startX - n) == minX) { nBump = true; } } else {
		 * if (nBuffer > 0) { nBuffer--; } else { n--; } }
		 * 
		 * if (!mBump) { m++; if ((startX + m) == maxX) { mBump = true; } } else
		 * { if (mBuffer > 0) { mBuffer--; } else { m--; } } }
		 */

		Rectangle rectangle = getCullingRectangle();
		int xMin = (int) rectangle.x;
		int yMin = (int) rectangle.y;
		int xMax = (int) (xMin+rectangle.width);
		int yMax = (int) (yMin+rectangle.height);
		
		for (int row = yMax; row >= yMin; row--) {
			for (int col = xMin; col <= xMax; col++) {
				int tileNumber = col + row * (int)map.getMapWidth();
				if (map.shouldRenderTile(tileNumber)) {
					renderTile(delta, layer, col, row, getTileColor(tileNumber),
							halfTileWidth, halfTileHeight, null);
				}
			}
		}

		if (gridLayer) {
			renderTransitions(yMin, yMax, xMin, xMax, halfTileWidth,
					halfTileHeight);
		}

		if (renderGrid) {
			renderGrid (yMin, yMax, xMin, xMax, halfTileWidth, halfTileHeight, layer);
		}
	}
	
	@Override
	public void renderTile(float delta, TiledMapTileLayer layer, int col, int row,
			float color, float halfTileWidth, float halfTileHeight, TextureRegion texture) {
		float x = (col * halfTileWidth) + (row * halfTileWidth);
		float y = (row * halfTileHeight) - (col * halfTileHeight);

		final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
		if (cell == null)
			return;
		final TiledMapTile tile = cell.getTile();
		if (tile != null) {
			final boolean flipX = cell.getFlipHorizontally();
			final boolean flipY = cell.getFlipVertically();
			final int rotations = cell.getRotation();
			if (cell instanceof Cell) {
				((Cell) cell).updateStateTime(delta);
			}
			TextureRegion region = texture != null ? texture : tile.getTextureRegion();

			float x1 = x
					- (texture != null ? 0 : (tile.getProperties().get("xoffset", Integer.class) * unitScaleX));
			float y1 = y
					- (texture != null ? 0 : (tile.getProperties().get("yoffset", Integer.class) * unitScaleY));
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
			getSpriteBatch().draw(region.getTexture(), vertices, 0, 20);
		}
	}
	
	public void renderGrid (int yMin, int yMax, int xMin, int xMax,
			float halfTileWidth, float halfTileHeight, TiledMapTileLayer layer) {
		spriteBatch.setColor(1, 1, 1, 0.3f);
		TextureRegion texture = map.getGridTexture();
		float width = map.getTileSizeX() * 2 * unitScaleX;
		float height = map.getTileSizeY() * unitScaleY;
		for (int row = yMax; row >= yMin; row--) {
			for (int col = xMin; col <= xMax; col++) {
				float x = (col * halfTileWidth) + (row * halfTileWidth);
				float y = (row * halfTileHeight) - (col * halfTileHeight);

				final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
				if (cell == null) {
					continue;
				}
				if (cell.getTile() != null && map.shouldRenderTile(col, row)) {
					spriteBatch.draw(texture, x, y,width,height);
				}
			}
		}
		spriteBatch.setColor(1, 1, 1, 1);
	}

	private void renderTransitions(int row1, int row2, int col1, int col2,
			float halfTileWidth, float halfTileHeight) {
		spriteBatch.setColor(0, 0, 1, 0.5f);
		Keys<Vector2> coordinates = map.getTransitionTileCoordinates();
		for (Vector2 coordinate : coordinates) {
			if (map.shouldRenderTile((int) coordinate.x, (int) coordinate.y)
					&& coordinate.x >= col1 && coordinate.x <= col2
					&& coordinate.y >= row1 && coordinate.y <= row2) {
				float x = (coordinate.x * halfTileWidth)
						+ (coordinate.y * halfTileWidth);
				float y = (coordinate.y * halfTileHeight)
						- (coordinate.x * halfTileHeight);
				spriteBatch.draw(map.getTransitionTexture(), x, y,
						map.getTileSizeX() * 2 * unitScaleX,
						map.getTileSizeY() * unitScaleY);
			}
		}
		spriteBatch.setColor(1, 1, 1, 1);
	}

	@Override
	protected Array<Drawable> sortDrawables(Array<Drawable> drawables) {
		return drawableSorter.sort(drawables);
	}
}
