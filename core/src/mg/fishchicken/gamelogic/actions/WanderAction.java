package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the NonPlayerCharacter wander around in the supplied
 * radius around his actual location. Each second there is
 * a check whether or not the character should move based
 * on the supplied chance to move.
 *  * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>radius - int - the wander radius
 * 	<li>chanceToMove - int - chance to move every second in percent
 *  <li>duration - int - how long (in seconds) will the character wander - this is not
 *  a precise time when he will stop moving - instead, after this time, this action
 *  will stop issuing any more MoveTo actions and will finish. However, if 
 *  there is still an active MoveTo action on the char after the duration is over,
 *  the move will be finished in its entirety before the WanderAction is removed. Default is
 *  -1, which means infinite duration. If set to -2, exactly one MoveTo action will be issued
 *  and then this action will finish.
 *  <li>visibleOnly - boolean - if true, the action will only update if the character is currently visible to the player.
 *  This saves precious CPU cycles if we do not neet the character to do anything if not seen by the player.
 * </ol>
 * @author Annun
 *
 */
public class WanderAction extends MoveToAction {

	public static final int DURATION_INFINITE = -1;
	public static final int DURATION_ONCE = -2;
	
	private int radius, chanceToMove;
	private int centerX, centerY;
	private float timeCounter = 0;
	private float totalTime = 0;
	private int duration;
	private AbstractGameCharacter character;
	private boolean isFinished;
	private boolean visibleOnly;
		
	public WanderAction() {
	}
	
	/**
	 * Create a new WanderAction that will issue exactly one random MoveTo action and then finish
	 * @param character
	 * @param radius
	 * @param chanceToMove
	 */
	public WanderAction(AbstractGameCharacter character, int radius, int chanceToMove) {
		init(character, radius, chanceToMove, DURATION_ONCE);
	}
	
	public WanderAction(AbstractGameCharacter character, int radius, int chanceToMove, int duration) {
		init(character, radius, chanceToMove, duration);
	}
	
	public WanderAction(AbstractGameCharacter character, int radius, int chanceToMove, int duration, boolean visibleOnly) {
		init(character, radius, chanceToMove, duration);
		this.visibleOnly = visibleOnly;
	}
	
	public WanderAction(AbstractGameCharacter character, Tile center, int radius, int chanceToMove, int duration) {
		init(character, radius, chanceToMove, duration);
		this.centerX = center.getX();
		this.centerY = center.getY();
	}
	
	public WanderAction(AbstractGameCharacter character, Tile center, int radius, int chanceToMove, int duration,  boolean visibleOnly) {
		this(character, center, radius, chanceToMove, duration);
		this.visibleOnly = visibleOnly;
	}
	
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof AbstractGameCharacter)) {
			throw new GdxRuntimeException("WanderAction only works on AbstractGameCharacter!");
		}
		this.character = (AbstractGameCharacter)ac;
		// we will mark center coordinates as uninitialized, since the supplied character
		// might not have been fully initialized itself - we will init the coords in the update
		// method, since by then everything should be set
		this.centerX = -1;
		this.centerY = -1;
		reset();
		duration = DURATION_INFINITE;
		if (parameters.length > 1) { 
			this.radius = (Integer) parameters[0];
			this.chanceToMove = (Integer) parameters[1];
			if (parameters.length > 2) {
				this.duration = (Integer) parameters[2];
			}
			if (parameters.length > 3) {
				this.visibleOnly = (Boolean) parameters[3];
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		if (visibleOnly && !character.isVisibleToPC()) {
			return;
		}
		if (centerX < 0) {
			Tile position = character.position().tile();
			centerX = position.getX();
			centerY = position.getY();
		}
		timeCounter += deltaTime;
		if (duration != DURATION_INFINITE) {
			totalTime += deltaTime;
		}
		if (timeCounter < 1 || !noMoreSteps()) {
			super.update(deltaTime);
		}
		else if (duration == DURATION_INFINITE || duration == DURATION_ONCE || duration > totalTime){
			boolean resetTimeCounter = true;
			// we move only if we rolled enough to beat the chance to move
			// or if we are already walking (can happen after a game is loaded)
			if (MathUtils.random(100) < chanceToMove || GameCharacter.State.WALK.equals(character.getState())) {
				super.init(character, getNumberInRadius(centerX, character.getMap().getMapWidth()), getNumberInRadius(centerY, character.getMap().getMapHeight()));
				if (duration == DURATION_ONCE) {
					duration = 0;
					timeCounter = 2;
					resetTimeCounter = false;
				}
			}
			if (resetTimeCounter) {
				timeCounter = 0;
			}
		} else {
			isFinished = true;
		}
	}
	
	@Override
	protected boolean finishIfNoMoreSteps() {
		if (noMoreSteps()) {
			character.setState(GameCharacter.State.IDLE);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isFinished() {
		return super.isFinished() || isFinished;
	}
	
	@Override
	public void reset() {
		timeCounter = 1;
		totalTime = 0;
		isFinished = false;
	}
	
	private int getNumberInRadius(float center, float max) {
		return (int)MathUtils.random(Math.max(1, center-radius), Math.min(center+radius, max-1));
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.radius = actionElement.getInt(XML_ATTRIBUTE_RADIUS, 10);
		this.chanceToMove = actionElement.getInt(XML_ATTRIBUTE_CHANCE_TO_MOVE, 10);
		this.duration = actionElement.getInt(XML_ATTRIBUTE_DURATION, DURATION_INFINITE);
		this.totalTime = actionElement.getFloat(XML_ATTRIBUTE_TOTAL_TIME, 0);
		this.visibleOnly = actionElement.getBoolean(XML_ATTRIBUTE_VISIBLE_ONLY, false);
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_RADIUS, radius)
				.attribute(XML_ATTRIBUTE_CHANCE_TO_MOVE, chanceToMove)
				.attribute(XML_ATTRIBUTE_DURATION, duration)
				.attribute(XML_ATTRIBUTE_TOTAL_TIME, totalTime)
				.attribute(XML_ATTRIBUTE_VISIBLE_ONLY, visibleOnly);
	}
}
