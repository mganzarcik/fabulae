package mg.fishchicken.pathfinding;

import mg.fishchicken.core.GameObject;

/**
 * The context describing the current path finding state
 * 
 * @author kevin
 */
public interface PathFindingContext {
	/**
	 * Get the object being moved along the path if any
	 * 
	 * @return The object being moved along the path
	 */
	public GameObject getMover();
	
	/**
	 * Get the distance that has been searched to reach this point
	 * 
	 * @return The distance that has been search to reach this point
	 */
	public int getSearchDistance();
}
