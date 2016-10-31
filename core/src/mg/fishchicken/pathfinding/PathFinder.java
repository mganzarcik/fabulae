package mg.fishchicken.pathfinding;

import mg.fishchicken.core.GameObject;

/**
 * A description of an implementation that can find a path from one 
 * location on a tile map to another based on information provided
 * by that tile map.
 * 
 * @see PathableTiledMap
 * @author Kevin Glass
 */
public interface PathFinder {

	/**
	 * Find a path from the starting location provided (sx,sy) to the target
	 * location (tx,ty) avoiding blockages and attempting to honour costs 
	 * provided by the tile map.
	 * 
	 * @param mover The entity that will be moving along the path. This provides
	 * a place to pass context information about the game entity doing the moving, e.g.
	 * can it fly? can it swim etc.
	 * 
	 * @param sx The x coordinate of the start location
	 * @param sy The y coordinate of the start location
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @param path the path to store the result in
	 * @param addLastStepEvenIfBlocked whether the last step should be included in the path even if it is blocked 
	 */
	public void findPath(GameObject mover, int sx, int sy, int tx, int ty, Path path, boolean addLastStepEvenIfBlocked);
}
