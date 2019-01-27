package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter.State;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.Path.Step;

/**
 * Moves the NonPlayerCharacter to the specified 
 * coordinates using A* to calculate the optimal
 * path through the map.
 * <br /><br />
 * Parameters:
 * 	<ol>
 * 		<li>path - mg.fishchicken.pathfinding.Path - the path that the character should follow
 * </ol>
 * OR
 * <ol>
 * 		<li> Tile - coordinate of the destination
 * 		<li> shouldIncludeLastStep - boolean - whether or not the last step of the calculated path (the target) should actually be stepped on
 * </ol>
 * OR
 * <ol>
 * 		<li> x - int - x coordinate of the destination
 * 		<li> y - int - y coordinate of the destination
 * 		<li> shouldIncludeLastStep - boolean - whether or not the last step of the calculated path (the target) should actually be stepped on
 * </ol>
 * @author Annun
 *
 */
public class MoveToAction extends BasicAction {

	private AbstractGameCharacter character;
	private Position characterPosition;
	private int previousX, previousY;
	private int targetX, targetY;
	private Path path;
	private int pathIndex = 1;
	private boolean isFinished;
	private boolean isPausing = false;
	private int lastStepModifier;
	
	public MoveToAction() {
	}
	
	public MoveToAction(AbstractGameCharacter character, Vector2 target) {
		this(character, (int)target.x, (int)target.y);
	}
	
	public MoveToAction(AbstractGameCharacter character, Tile tile) {
		init(character, tile);
	}
	
	public MoveToAction(AbstractGameCharacter character, int x, int y) {
		init(character, x, y);
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof AbstractGameCharacter)) {
			throw new GdxRuntimeException("MoveToAction only works on AbstractGameCharacter!");
		}
		this.character = (AbstractGameCharacter) ac;
		this.characterPosition = character.position();
		previousX = (int) characterPosition.getX();
		previousY = (int) characterPosition.getY();
		targetX = -1;
		targetY = -1; 
		isFinished = false;
		resetPathIndex();
		if (parameters.length > 0) {
			lastStepModifier = 0;
			if (parameters[0] instanceof Path) {
				path = (Path)parameters[0];
				if (path.getLength() != 0) {
					Step lastStep = path.getStep(path.getLength()-1);
					targetX = lastStep.getX();
					targetY = lastStep.getY();
				} else {
					isFinished = true;
				}
			} else if (parameters[0] instanceof Tile) {
				targetX = ((Tile)parameters[0]).getX();
				targetY = ((Tile)parameters[0]).getY();
				path = null;
				if (parameters.length >= 2) {
					lastStepModifier = (Boolean)parameters[1] ? 0 : 1;
				}
			} else {
				targetX = (Integer)parameters[0];
				targetY = (Integer)parameters[1];
				path = null;
				if (parameters.length >= 3) {
					lastStepModifier = (Boolean)parameters[2] ? 0 : 1;
				}
			}
		}
		
		if (targetX >= 0 && targetY >= 0 && character.getMap() != null) {
			calculatePathIfRequired();
			finishIfNoMoreSteps();
		} else {
			isFinished = true;
		}
	}
	
	private void calculatePathIfRequired() {
		if (path == null) {
			path = character.getMap().findPath(character, targetX, targetY);	
			for (int i = path.getLength()-1, j = lastStepModifier; j > 0 && i >= 0; --j, --i) {
				path.removeStep(i);
			}
			// if the last step is blocked and we did not already remove any steps, then remove it
			Step lastStep = path.getLastStep();
			if (lastStep != null && lastStepModifier == 0 && character.getMap().blocked(character, lastStep.getX(), lastStep.getY())) {
				path.removeStep(path.getLength()-1);
			}
			resetPathIndex();
		}
	}
	
	private void resetPathIndex() {
		pathIndex = 1;
		if (!character.position().equals(targetX, targetY) && previousX == targetX && previousY == targetY) {
			pathIndex = 0;
		}
	}
	
	@Override
	public int getActionSlot() {
		return MoveToAction.class.hashCode();
	}

	@Override
	public void update(float deltaTime) {
		if (character != null && !isFinished) {
			calculatePathIfRequired();
			Step nextStep = path.getStep(pathIndex);
			if (character.position().equals(nextStep.getX(), nextStep.getY())) {
				previousX = nextStep.getX();
				previousY =  nextStep.getY();
				
				if (GameState.isCombatInProgress() && character instanceof GameCharacter ) {
					((GameCharacter)character).stats().addToAP(-nextStep.getMoveCost());
				}
				
				if (isPausing) {
					character.setState(State.IDLE);
					isPausing = false;
					super.pause();
					return;
				}
				++pathIndex;
				if (finishIfNoMoreSteps()) {
					return;
				}
				nextStep = path.getStep(pathIndex);
				if (character.getMap().blocked(character, nextStep.getX(), nextStep.getY())) {
					path = null;
					update(deltaTime);
					return;
				}
			}
			moveCharacterToStep(character, nextStep, deltaTime);
		}
	}
	
	/**
	 * Finishes the current MoveTo action
	 * if there are no more steps.
	 * 
	 * This puts the moving character back to Idle.
	 * 
	 * @return true if the action was finished, false otherwise
	 */
	protected boolean finishIfNoMoreSteps() {
		if (noMoreSteps()) {
			isFinished = true;
			character.setState(GameCharacter.State.IDLE);
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if there are no more steps
	 * in the current path.
	 * 
	 * @return
	 */
	public boolean noMoreSteps() {
		if (path == null
				|| pathIndex >= path.getLength()
				|| (character instanceof GameCharacter && (GameState.isCombatInProgress() && ((GameCharacter)character).stats().getAPAct() < path.getStep(pathIndex).getMoveCost()))
				|| !character.canPerformAction(MoveToAction.class)
				|| (pathIndex > 0 && path.getStep(pathIndex-1).isEndStep())) {
			return true;
		}
		
		// for the last step, we have special handling - if it is blocked, we are done
		if (pathIndex == path.getLength()-1) {
			Step nextStep = path.getStep(pathIndex);
			if (character.getMap().blocked(character, nextStep.getX(), nextStep.getY())) {
				path = null;
				return true;	
			}
		}
		return false;
	}

	/**
	 * Moves the supplied character closer to the supplied step
	 * based on the supplied delta time and his speed.
	 * 
	 * @param character
	 * @param step
	 * @param deltaTime
	 */
	private void moveCharacterToStep(AbstractGameCharacter character, Step step, float deltaTime) {
		
		float xDif =step.getX() - characterPosition.getX();
		float yDif =step.getY() - characterPosition.getY();
		int xSign = (int) (xDif / Math.abs(xDif));
		int ySign = (int) (yDif / Math.abs(yDif));
		
		float speed =  character.getSpeed();
		GameMap characterMap = character.getMap();
		if (characterMap != null) {
			if (character.getMap().isWorldMap()) {
				speed *= Configuration.getWorldMapSpeedMultiplier();
			}
			if (characterMap.equals(gameState.getCurrentMap()) && GameState.isCombatInProgress()) {
				speed *= Configuration.getCombatSpeedMultiplier();
			}
		}
		xDif = xDif == 0 ? 0 : xSign * speed * deltaTime;
		yDif = yDif == 0 ? 0 : ySign * speed * deltaTime;
		
		// this is here so that characters don't move twice as fast when moving
		// diagonally to the left or right on isometric maps
		if (character.getMap().isIsometric()
				&& ((xDif < 0 && yDif < 0) || (xDif > 0 && yDif > 0))) {
			xDif = xDif / 2;
			yDif = yDif / 2;
		}
		float newX = characterPosition.getX()+xDif;
		if (xDif < 0 && newX  <step.getX()) {
			newX=step.getX();
		} else if (xDif > 0 && newX  >step.getX()) {
			newX = step.getX();
		}
		
		float newY = characterPosition.getY()+yDif;
		if (yDif < 0 && newY  <step.getY()) {
			newY =step.getY();
		} else if (yDif > 0 && newY  >step.getY()) {
			newY =step.getY();
		}
		
		character.setOrientation(Orientation.calculateOrientationToTarget(character.getMap().isIsometric(),xSign, ySign));
		character.setState(GameCharacter.State.WALK);
		characterPosition.set(newX, newY);
	}
	
	/**
	 * Resume the MoveTo action from where 
	 * it was paused.
	 */
	@Override
	public void resume() {
		if (character != null && !noMoreSteps()) {
			character.setState(State.WALK);
			path = null;
			calculatePathIfRequired();
		}
		isPausing = false;
		super.resume();
	}
	
	/**
	 * This will pause the current MoveTo action.
	 * 
	 * The character will not stop moving immediately
	 * though - he will finish moving into the next tile
	 * and only stop afterwards.
	 */
	@Override
	public void pause() {
		//if we need to pause, first finish the current path step
		isPausing = true;
	}
	

	@Override
	public void reset() {
		init(character,targetX, targetY);
	}
	
	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	@Override
	public boolean isBlockingInCombat() {
		return !isFinished();
	}

	@Override
	public void onRemove(ActionsContainer ac) {
		if (character != null) {
			character.setState(GameCharacter.State.IDLE);
		}
		isFinished = true;
	}
	
	public Path getCurrentPath() {
		return path;
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.targetX = actionElement.getInt(Action.XML_ATTRIBUTE_X, -1);
		this.targetY  = actionElement.getInt(Action.XML_ATTRIBUTE_Y, -1);
		if (targetX < 0 || targetY < 0) {
			throw new GdxRuntimeException("MoveToAction must have x and y attributes specified! (Character +"+character+")");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_X, targetX)
				.attribute(XML_ATTRIBUTE_Y, targetY);
	}
}


