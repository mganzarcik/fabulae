package mg.fishchicken.gamelogic.characters;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.characters.los.CircularLineOfSight;
import mg.fishchicken.gamelogic.characters.los.LineOfSight;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.files.FileHandle;

public class PlayerCharacter extends GameCharacter implements XMLLoadable {
	
	/**
	 * Empty constructor for game loading.
	 */
	public PlayerCharacter() {
		super();
	}
	
	/**
	 * Empty constructor for character creation.
	 */
	public PlayerCharacter(String id, String type) {
		super(id, type);
	}
	
	public PlayerCharacter(String id, FileHandle characterFile) throws IOException {
		this(id, characterFile, null);
	}
	
	public PlayerCharacter(String id, FileHandle characterFile, GameMap map) throws IOException {
		super(id, characterFile, map);
	}
	
	@Override
	public <T extends Action> T addAction(Class<T> actionClass,
			Object... parameters) {
		T action = super.addAction(actionClass, parameters);
		
		if (isMemberOfPlayerGroup()) {
			GameState.getPlayerCharacterGroup().getGroupGameObject().actionAdded(this, action);
		}
		
		return action;
	}
	
	@Override
	protected LineOfSight createLineOfSight(GameMap map) {
		LineOfSight los = new CircularLineOfSight(map
				.getFogOfWarRayHandler(), 360,
				map.isWorldMap() ? Configuration.getSightRadiusWorld()
						: Configuration.getSightRadiusLocal(), position.getX(),
						position.getY(), map);
		return los;
	}

}
