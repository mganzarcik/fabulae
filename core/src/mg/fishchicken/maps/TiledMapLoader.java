package mg.fishchicken.maps;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Extending the LibGDX TmxMapLoader to fix a few things.
 * 
 * @author Annun
 *
 */
public class TiledMapLoader extends TmxMapLoader {

	/** Creates loader
	 * 
	 * @param resolver */
	public TiledMapLoader (FileHandleResolver resolver) {
		super(resolver);
	}
	
	/**
	 * Loads the map data, given the XML root element and an {@link ImageResolver} used
	 * to return the tileset Textures. Fixes the issue of not loading map orientation properly.
	 * @param root the XML root element 
	 * @param tmxFile the Filehandle of the tmx file
	 * @param imageResolver the {@link ImageResolver}
	 * @return the {@link TiledMap}
	 */
	protected TiledMap loadTilemap(Element root, FileHandle tmxFile, ImageResolver imageResolver) {
		TiledMap map = new TiledMap();
		
		String mapOrientation = root.getAttribute("orientation", null);
		int mapWidth = root.getIntAttribute("width", 0);
		int mapHeight = root.getIntAttribute("height", 0);
		int tileWidth = root.getIntAttribute("tilewidth", 0);
		int tileHeight = root.getIntAttribute("tileheight", 0);
		String mapBackgroundColor = root.getAttribute("backgroundcolor", null);
		
		MapProperties mapProperties = map.getProperties();
		if (mapOrientation != null) {
			mapProperties.put("orientation", mapOrientation);
		}
		mapProperties.put("width", mapWidth);
		mapProperties.put("height", mapHeight);
		mapProperties.put("tilewidth", tileWidth);
		mapProperties.put("tileheight", tileHeight);
		if (mapBackgroundColor != null) {
			mapProperties.put("backgroundcolor", mapBackgroundColor);
		}
		mapWidthInPixels = mapWidth * tileWidth;
		mapHeightInPixels = mapHeight * tileHeight;
		
		Element properties = root.getChildByName("properties");
		if (properties != null) {
			loadProperties(map.getProperties(), properties);
		}
		Array<Element> tilesets = root.getChildrenByName("tileset");
		for (Element element : tilesets) {
			loadTileSet(map, element, tmxFile, imageResolver);
			root.removeChild(element);
		}
		for (int i = 0, j = root.getChildCount(); i < j; i++) {
			Element element = root.getChild(i);
			String name = element.getName();
			if (name.equals("layer")) {
				loadTileLayer(map, element);
			} else if (name.equals("objectgroup")) {
				loadObjectGroup(map, element);
			}
		}
		return map;
	}
	
	/**
	 * Loads the specified tileset data, adding it to the collection of the specified map, given the XML element, the tmxFile 
	 * and an {@link ImageResolver} used to retrieve the tileset Textures.
	 *
	 * <p>
	 * Default tileset's property keys that are loaded by default are:
	 * </p>
	 *
	 * <ul>
	 * <li><em>firstgid</em>, (int, defaults to 1) the first valid global id used for tile numbering</li>
	 * <li><em>imagesource</em>, (String, defaults to empty string) the tileset source image filename</li>
	 * <li><em>imagewidth</em>, (int, defaults to 0) the tileset source image width</li>
	 * <li><em>imageheight</em>, (int, defaults to 0) the tileset source image height</li>
	 * <li><em>tilewidth</em>, (int, defaults to 0) the tile width</li>
	 * <li><em>tileheight</em>, (int, defaults to 0) the tile height</li>
	 * <li><em>margin</em>, (int, defaults to 0) the tileset margin</li>
	 * <li><em>spacing</em>, (int, defaults to 0) the tileset spacing</li>
	 * <li><em>xoffset</em>, (int, defaults to 0) number of pixels by which each tile should be offset on the x axis during rendering</li>
	 * <li><em>yoffset</em>, (int, defaults to 0) number of pixels by which each tile should be offset on the y axis during rendering</li>
	 * </ul>
	 *
	 * <p>
	 * The values are extracted from the specified Tmx file, if a value can't be found then the default is used.
	 * </p>
	 * @param map the Map whose tilesets collection will be populated
	 * @param element the XML element identifying the tileset to load
	 * @param tmxFile the Filehandle of the tmx file
	 * @param imageResolver the {@link ImageResolver}
	 */
	protected void loadTileSet(TiledMap map, Element element, FileHandle tmxFile, ImageResolver imageResolver) {
		super.loadTileSet(map, element, tmxFile, imageResolver);
		
		if (element.getName().equals("tileset")) {
			String name = element.get("name", null);
			if (name == null) {
				return;
			}
			TiledMapTileSet ts = map.getTileSets().getTileSet(name);
			Element offsets = element.getChildByName("tileoffset");		
			int xOffset = offsets == null ? 0 :  Integer.parseInt(offsets.getAttribute("x", "0"));
			int yOffset = offsets == null ? 0 : Integer.parseInt(offsets.getAttribute("y", "0"));
			
			ts.getProperties().put("xoffset", xOffset);
			ts.getProperties().put("yoffset", yOffset);
			int id = ts.getProperties().get("firstgid", Integer.class);
			// put the offsets as properties into each tile and load animated tiles
			for (int i = id; i < id+ts.size(); ++i) {
				TiledMapTile tile = ts.getTile(i);
				tile.getProperties().put("xoffset", xOffset);
				tile.getProperties().put("yoffset", yOffset);
				if (tile.getProperties().get("animationFrames") != null) {
					String animationFrames = (String) tile.getProperties().get("animationFrames");
					float fps = Float.valueOf((String) tile.getProperties().get("fps"));
					boolean randomDelay = tile.getProperties().get("randomDelay") != null;
					boolean randomAnimation = tile.getProperties().get("randomAnimation") != null;
					
					Array<StaticTiledMapTile> frameTiles = new Array<StaticTiledMapTile>();
					frameTiles.add((StaticTiledMapTile) tile);
					if (animationFrames.contains("-")) {
						String[] startEnd = animationFrames.split("-");
						int end = Integer.valueOf(startEnd[1]);
						for (int j = Integer.valueOf(startEnd[0]); j < end; ++j) {
							StaticTiledMapTile frame = (StaticTiledMapTile)ts.getTile(i+j);
							frameTiles.add(frame);
						}
					} else {
						for (String frameId : animationFrames.split(",")) {
							StaticTiledMapTile frame = (StaticTiledMapTile)ts.getTile(i+Integer.parseInt(frameId.trim()));
							frameTiles.add(frame);
						}
					}
					AnimatedTile aTile = new AnimatedTile((1f/fps),frameTiles,randomDelay, randomAnimation);
					aTile.getProperties().putAll(tile.getProperties());
					ts.putTile(i, aTile);
				}
			}
		}		
	}
	
	protected Cell createTileLayerCell (boolean flipHorizontally, boolean flipVertically, boolean flipDiagonally) {
		Cell cell = new mg.fishchicken.maps.Cell();
		if (flipDiagonally) {
			if (flipHorizontally && flipVertically) {
				cell.setFlipHorizontally(true);
				cell.setRotation(Cell.ROTATE_270);
			} else if (flipHorizontally) {
				cell.setRotation(Cell.ROTATE_270);
			} else if (flipVertically) {
				cell.setRotation(Cell.ROTATE_90);
			} else {
				cell.setFlipVertically(true);
				cell.setRotation(Cell.ROTATE_270);
			}
		} else {
			cell.setFlipHorizontally(flipHorizontally);
			cell.setFlipVertically(flipVertically);
		}
		return cell;
	}
}
