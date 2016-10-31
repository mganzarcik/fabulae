package mg.fishchicken.pathfinding.heuristics;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.pathfinding.AStarHeuristic;
import mg.fishchicken.pathfinding.PathableTiledMap;

/**
 * A heuristic that uses the tile that is closest to the target
 * as the next best tile.
 * 
 * @author Kevin Glass
 */
public class ClosestHeuristic implements AStarHeuristic {
	/**
	 * @see AStarHeuristic#getCost(PathableTiledMap, Mover, int, int, int, int)
	 */
	public float getCost(PathableTiledMap map, GameObject mover, int x, int y, int tx, int ty) {		
		float dx = tx - x;
		float dy = ty - y;
		
		float result = (float) (Math.sqrt((dx*dx)+(dy*dy)));
		
		return result;
	}

}
