package mg.fishchicken.gamelogic.locations;

import java.util.Iterator;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.graphics.Drawable;
import mg.fishchicken.graphics.renderers.GameMapRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class TiledMapObject implements Drawable {

	private final Array<Tile> objectTilesVectors;
	private final ObjectSet<Tile> alreadySeenTiles;
	private final Array<TiledMapTileLayer> layers;
	private final GameMap map;
	private final boolean shouldDrawAsAWhole;
	private final MapTileObjectGround ground;
	private final float fogColor, visibleColor, notVisibleColor;
	private Position position;
	private float width, height;
	private float zIndex;
	private Boolean wasDiscovered;
	private boolean isVisibleByPC;
	
	public TiledMapObject(MapTileObjectGround ground, int[] objectTiles, GameMap map, TiledMapTileLayer layer,
			boolean shouldDrawAsAWhole) {
		this(ground, objectTiles, map, new Array<TiledMapTileLayer>(new TiledMapTileLayer[] { layer }),
				shouldDrawAsAWhole);
	}
	
	public TiledMapObject(MapTileObjectGround ground, int[] objectTiles, GameMap map, Array<TiledMapTileLayer> layers,
			boolean shouldDrawAsAWhole) {
		this.ground = ground;
		alreadySeenTiles = new ObjectSet<Tile>();
		objectTilesVectors = new Array<Tile>();
		for (int i = 0; i < objectTiles.length; i += 2) {
			objectTilesVectors.add(new Tile(objectTiles[i],objectTiles[i+1]));
		}
		this.shouldDrawAsAWhole = shouldDrawAsAWhole;
		this.map = map;
		this.layers = new Array<TiledMapTileLayer>(layers);
		this.position = new Position();
		wasDiscovered = false;
		isVisibleByPC = false;
		fogColor = Color.toFloatBits(Configuration.getFogColor().r,
				Configuration.getFogColor().g, Configuration.getFogColor().b,
				1f);
		visibleColor = Color.toFloatBits(1, 1, 1, 1f);
		notVisibleColor = Color.toFloatBits(0f,0f,0f,1f);
		calculateDimensions();
		removeEmptyLayers();
	}
	
	private void removeEmptyLayers() {
		Iterator<TiledMapTileLayer> layerIterator = layers.iterator();
		while (layerIterator.hasNext()) { 
			TiledMapTileLayer layer = layerIterator.next();
			boolean isEmpty = true;
			for (Tile tile: objectTilesVectors) {
				int x = tile.getX(); 
				int y = tile.getY();
				TiledMapTileLayer.Cell cell = layer.getCell(x, y);
				if (cell == null) {
					continue;
				}
				if (cell.getTile() != null) {
					isEmpty = false;
					break;
				}
			}
			if (isEmpty) {
				layerIterator.remove();
			}
		}
	}
	
	private void calculateDimensions() {
		if (ground.size() == 1) {
			Tile tile = ground.getTile(0);
			position.set(tile);
			width = 1;
			height = 1;
		} else {
			int minX=0, maxX=0, minY=0, maxY=0;
			for (int i=0; i < ground.size(); ++i) {
				Tile tile = ground.getTile(i);
				int x = tile.getX();
				int y = tile.getY();
				if (i == 0) {
					minX = maxX = x;
					minY = maxY = y;
					continue;
				}
				
				if (x < minX) {
					minX = x;
				}
				if (y < minY) {
					minY = y;
				}
				if (x > maxX) {
					 maxX = x;
				}
				if (y > maxY) {
					 maxY = y;
				}
			}
			position.set(minX, minY);
			width = maxX - minX;
			height = maxY - minY;
		}
		Vector2 tempVector = position.setVector2(MathUtil.getVector2());
		map.projectFromTiles(tempVector);
		zIndex = -tempVector.y;
		MathUtil.freeVector2(tempVector);
	}
	
	@Override
	public void draw(GameMapRenderer renderer, float deltaTime) {
		float layerTileProjectedWidth = map.getTileSizeX() * map.getScaleX();
		float layerTileProjectedHeight = map.getTileSizeY() * map.getScaleY();
		if (map.isIsometric()) {
			layerTileProjectedHeight /= 2;
		}
		float color = visibleColor;
		boolean wasDiscovered = wasDiscovered();
		
		if (!wasDiscovered) {
			color = notVisibleColor;
		}
		if (!isVisibleByPC && wasDiscovered) {
			color = fogColor;
		}
		
		for (Tile tile: objectTilesVectors) {
			int tileNumber = map.getTileId(tile.getX(), tile.getY());
			if (!shouldDrawAsAWhole && wasDiscovered) {
				color = visibleColor;
				boolean geometryVisible = map.isGeometryVisibleToPC(tile.getX(), tile.getY());
				if (!map.shouldRenderTile(tileNumber) && !geometryVisible && !alreadySeenTiles.contains(tile)) {
					color = notVisibleColor;
				} else if (!geometryVisible || !isVisibleByPC) {
					color = fogColor;
				} else {
					alreadySeenTiles.add(tile);
				}
			}
			for (TiledMapTileLayer layer : layers) {
				renderer.renderTile(deltaTime, layer, tile.getX(), tile.getY(), color, layerTileProjectedWidth, layerTileProjectedHeight, null);
			}
		}
		renderer.getSpriteBatch().setColor(1, 1, 1, 1f);
	}

	private boolean groundIsVisibleToPC() {
		for (int i=0; i < ground.size(); ++i) {
			Tile groundTile = ground.getTile(i);
			if (map.isTileVisibleToPC(groundTile.getX(), groundTile.getY())) {
				return true;
			}
		}
		return false;	
	}
	
	private boolean wasDiscovered() {
		if (!wasDiscovered) {
			for (int i=0; i < ground.size(); ++i) {
				Tile groundTile = ground.getTile(i);
				if (map.shouldRenderTile(groundTile.getX(), groundTile.getY())) {
					wasDiscovered = true;
					break;
				}
			}
		}
		return wasDiscovered;
	}

	public void recalculateVisibility() { 
		this.isVisibleByPC = wasDiscovered() && groundIsVisibleToPC();
	}
	
	@Override
	public boolean shouldDraw(Rectangle cullingRectangle) {
		for (Tile tile : objectTilesVectors) {
			int x = tile.getX();
			int y = tile.getY();
			if (cullingRectangle.contains(x, y) && map.shouldRenderTile(x, y) && map.isOnScreen(x, y)) {
				return true;
			}
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
	public Color getColor() {
		return Color.WHITE;
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
	public String toString() {
		return super.toString()+" ("+position.getX()+", "+position.getY()+")";
	}
	
	@Override
	public float getZIndex() {
		return zIndex;
	}

	@Override
	public Position position() {
		return position;
	}

	@Override
	public GameMap getMap() {
		return map;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

}
