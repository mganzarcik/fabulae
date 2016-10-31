package mg.fishchicken.gamelogic.traps;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamestate.traps.Trap;
import mg.fishchicken.pathfinding.AStarPathFinder;
import mg.fishchicken.pathfinding.Path;

public interface Trapable extends PositionedThing {

	/**
	 * Gets the trap this trapable is trapped with.
	 * 
	 * Can be null in case it is not currently trapped.
	 * 
	 * @return
	 */
	public Trap getTrap();
	
	/**
	 * Gets the game object that should be considered the originator
	 * of the trap's projectile and effect.
	 * @return
	 */
	public GameObject getOriginatorGameObject();
	
	/**
	 * Find a path for the mover that's safe to walk without triggering this 
	 * Trapable's trap that would  move the mover close enough to be able
	 * to disarm it.
	 * 
	 * @param path - path to set
	 * @param mover
	 * @param pathFinder
	 * @return the set path. This will be the same object as the one supplied, with calculated steps
	 */
	public Path findSafeDisarmPath(Path path, GameObject mover, AStarPathFinder pathFinder, Class<? extends Action> action);
}
