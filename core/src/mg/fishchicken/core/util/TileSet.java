package mg.fishchicken.core.util;

import com.badlogic.gdx.utils.IntSet;

/**
 * Represents an ordered set of tiles, which are a pair of x, y integer coordinates. 
 * Each tile can only be in the set once. 
 *
 */
public class TileSet {

	PositionArray array;
	IntSet set;	
	int mapWidth;
	
	public TileSet(int mapWidth) {
		this.mapWidth = mapWidth;
		set = new IntSet();
		array = new PositionArray();
	}
	
	/**
	 * Returns true if the tile was not already in the set. 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean add(int x, int y) {
		if (set.add(getTileId(x, y))) {
			array.add(x, y);
			return true;
		}
		return false;
	}
	
	public void addAll(PositionArray array) {
		for (int i = 0; i < array.size(); ++i) {
			add(array.getX(i), array.getY(i));
		}
	}
	
	public boolean contains(int x, int y) {
		return set.contains(getTileId(x, y));
	}
	
	public void clear() {
		set.clear();
		array.clear();
	}
	
	/**
	 * Gets the tiles in this set as a IntArray.
	 * 
	 * Note is is NOT safe to modify the returned array.
	 * 
	 * @return
	 */
	public PositionArray getTiles() {
		return array;
	}
	
	private int getTileId(int x, int y) {
		return x+y*mapWidth;
	}
	
}
