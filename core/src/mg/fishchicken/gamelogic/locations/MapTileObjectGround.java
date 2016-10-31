package mg.fishchicken.gamelogic.locations;

import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

/**
 * This represents an array of tiles
 * that form the ground of a MapTileObject.
 * 
 * Two MapTileObjectGround objects are considered equal
 * if they contain the same tiles in their arrays (even if they
 * are in different order).
 * 
 * @author Annun
 *
 */
public class MapTileObjectGround {
	private Array<Tile> groundTiles;
	private String name;
	
	public MapTileObjectGround() {
		this.groundTiles = new Array<Tile> ();
		this.name = null;
	}
	
	public MapTileObjectGround(String name, IntArray tiles) {
		this();
		addTiles(tiles);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Returns true if all the tiles of this ground object
	 * are contained in the supplied polygon.
	 * @param polygon
	 * @return
	 */
	public boolean isContainedIn(Polygon polygon) {
		for (Tile tile : groundTiles) {
			if (!polygon.contains(tile.getX(), tile.getY())) {
				return false;
			}
		}
		return true;
	}

	public Tile getTile(int i) {
		return groundTiles.get(i);
	}
	
	public void addTiles(IntArray tiles) {
		for (int i = 0; i < tiles.size; i += 2) {
			addTile(new Tile(tiles.get(i), tiles.get(i+1)));
		}
	}
	
	public void addTile(Tile tile) {
		if (!groundTiles.contains(tile, false)) {
			groundTiles.add(tile);
		}
	}
	
	public boolean containsTile(Tile tile) {
		return groundTiles.contains(tile, false);
	}
	
	public int size() {
		return groundTiles.size;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (!(obj instanceof MapTileObjectGround)) {
			return false;
		}
		
		MapTileObjectGround toCompare = (MapTileObjectGround) obj;
		
		if (toCompare.size() != size()) {
			return false;
		}
		
		boolean returnValue = true;
		for (Tile tile : groundTiles) {
			returnValue = returnValue && toCompare.containsTile(tile);
		}
		
		return returnValue;
	}
	
	@Override
	public int hashCode() {
		int returnValue = 0;
		
		for (Tile tile : groundTiles) {
			returnValue += tile.hashCode();
		}
		return returnValue;
	}
	
	
}
