package mg.fishchicken.pathfinding.heuristics;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.pathfinding.AStarHeuristic;
import mg.fishchicken.pathfinding.PathableTiledMap;

/**
 * A heuristic that uses the tile that is closest to the target
 * as the next best tile. In this case the sqrt is removed
 * and the distance squared is used instead
 * 
 * @author Kevin Glass
 */
public class ClosestSquaredHeuristic implements AStarHeuristic {

	/**
	 * @see AStarHeuristic#getCost(PathableTiledMap, Mover, int, int, int, int)
	 */
	public float getCost(PathableTiledMap map, GameObject mover, int x, int y, int tx, int ty) {		
		float dx = tx - x;
		float dy = ty - y;
		
		return ((dx*dx)+(dy*dy));
	}

}
