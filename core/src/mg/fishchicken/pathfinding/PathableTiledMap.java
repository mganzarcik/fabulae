package mg.fishchicken.pathfinding;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;

/**
 * The description for the data we're pathfinding over. This provides the contract
 * between the data being searched (i.e. the in game map) and the path finding
 * generic tools
 * 
 * @author Kevin Glass
 */
public interface PathableTiledMap {
	/**
	 * Get the width of the tile map. 
	 * 
	 * @return The number of tiles across the map
	 */
	public int getMapWidth();

	/**
	 * Get the height of the tile map. 
	 * 
	 * @return The number of tiles down the map
	 */
	public int getMapHeight();
	
	/**
	 * Check if the given location is blocked, i.e. blocks movement of 
	 * the supplied mover.
	 * 
	 * @param context The context describing the pathfinding at the time of this request
	 * @param tx The x coordinate of the tile we're moving to
	 * @param ty The y coordinate of the tile we're moving to
	 * @return True if the location is blocked
	 */
	public boolean blocked(GameObject mover, int tx, int ty);
	
	/**
	 * Returns true if the supplied tile is not available for pathfinding and
	 * should be completely ignored.
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	public boolean tileUnavailable(int tx, int ty);
	
	/**
	 * Get the cost of moving through the given tile. This can be used to 
	 * make certain areas more desirable. A simple and valid implementation
	 * of this method would be to return 1 in all cases.
	 * 
	 * @param context The context describing the pathfinding at the time of this request
	 * @param tx The x coordinate of the tile we're moving to
	 * @param ty The y coordinate of the tile we're moving to
	 * @return The relative cost of moving across the given tile
	 */
	public float getMoveCost(AbstractGameCharacter mover, int tx, int ty);
	
	/**
	 * Get the actions points cost of moving through the given tile.
	 * 
	 * @param context The context describing the pathfinding at the time of this request
	 * @param tx The x coordinate of the tile we're moving to
	 * @param ty The y coordinate of the tile we're moving to
	 * @return The action points cost of moving across the given tile
	 */
	public float getAPMoveCost(AbstractGameCharacter mover, int tx, int ty);
}


