package mg.fishchicken.gamelogic.locks;

import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.locks.Lock;

public interface Lockable extends Trapable {
	
	public Lock getLock();
	/**
	 * Gets the tile that represents the ground of this Lockable. This is tile
	 * from which characters can attempt to unlock it.
	 * @return
	 */
	public Tile getGround();
}
