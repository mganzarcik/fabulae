package mg.fishchicken.pathfinding;

import mg.fishchicken.gamestate.Position;

public interface TileBlocker {

	public Position position();
	
	public boolean isBlockingPath();
}
