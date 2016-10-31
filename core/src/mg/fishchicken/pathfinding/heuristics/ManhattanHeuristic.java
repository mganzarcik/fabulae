package mg.fishchicken.pathfinding.heuristics;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.pathfinding.AStarHeuristic;
import mg.fishchicken.pathfinding.PathableTiledMap;

/**
 * A heuristic that drives the search based on the Manhattan distance
 * between the current location and the target
 * 
 * @author Kevin Glass
 */
public class ManhattanHeuristic implements AStarHeuristic {
	/** The minimum movement cost from any one square to the next */
	private int minimumCost;
	
	/**
	 * Create a new heuristic 
	 * 
	 * @param minimumCost The minimum movement cost from any one square to the next
	 */
	public ManhattanHeuristic(int minimumCost) {
		this.minimumCost = minimumCost;
	}
	
	/**
	 * @see AStarHeuristic#getCost(PathableTiledMap, Mover, int, int, int, int)
	 */
	public float getCost(PathableTiledMap map, GameObject mover, int x, int y, int tx,
			int ty) {
		return minimumCost * (Math.abs(x-tx) + Math.abs(y-ty));
	}

}
