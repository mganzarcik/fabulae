package mg.fishchicken.core;

import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;

public interface PositionedThing {

	public Position position();
	
	public GameMap getMap();
}
