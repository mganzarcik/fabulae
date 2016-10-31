package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the NonPlayerCharacter look around randomly. Each second there is
 * a check whether or not the character should change orientation based 
 * on the supplied chance to rotate.
 *  * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>chanceToMove - int - chance to rotate every second in percent
 *  <li>duration - int - how long (in seconds) will the character look around. Default is
 *  -1, which means infinite duration. If set to -2, exactly one LookAroundAction action will be issued
 *  and then this action will finish.
 *  <li>forbidden - Orientation[] - orientations that the character can never assume.
 * </ol>
 * @author Annun
 *
 */
public class LookAroundAction extends BasicAction {

	public static final int DURATION_INFINITE = -1;
	public static final int DURATION_ONCE = -2;
	
	private int chance;
	private float timeCounter = 0;
	private float totalTime = 0;
	private int duration;
	private AbstractGameCharacter character;
	private boolean isFinished;
	private Array<Orientation> forbidden;
		
	public LookAroundAction() {
	}
	
	/**
	 * Create a new LookAroundAction that will issue exactly one random rotation action and then finish
	 * @param character
	 * @param chance
	 */
	public LookAroundAction(AbstractGameCharacter character, int chance) {
		init(character, chance, DURATION_ONCE);
	}
	
	public LookAroundAction(AbstractGameCharacter character, int chance, int duration) {
		init(character, chance, duration);
	}
	
	public LookAroundAction(AbstractGameCharacter character, int chance, int duration, Orientation... forbidden) {
		init(character, chance, duration, forbidden);
	}	
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof AbstractGameCharacter)) {
			throw new GdxRuntimeException("LookAroundAction only works on AbstractGameCharacter!");
		}
		this.character = (AbstractGameCharacter)ac;
		reset();
		duration = DURATION_INFINITE;
		forbidden = new Array<Orientation>();
		if (parameters.length > 0) { 
			this.chance = (Integer) parameters[0];
			if (parameters.length > 1) {
				this.duration = (Integer) parameters[1];
				
				if (parameters.length > 2) {
					this.forbidden.addAll((Orientation[]) parameters[2]);
				}
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		timeCounter += deltaTime;
		if (duration != DURATION_INFINITE) {
			totalTime += deltaTime;
		}
		if (timeCounter >= 1){
			boolean resetTimeCounter = true;
			// we change only if we rolled enough to beat the chance to move
			if (MathUtils.random(100) < chance) {
				Orientation current = character.getOrientation();
				Orientation clockwise = current.getClockwise();
				Orientation antiClockwise = current.getAntiClockwise();
				Orientation random = forbidden.contains(clockwise, true) ? antiClockwise : 
										(forbidden.contains(antiClockwise, true) ? clockwise : 
											(MathUtils.random(1) == 0 ? clockwise : antiClockwise)); 
				character.setOrientation(random);
				if (duration == DURATION_ONCE) {
					duration = 0;
					timeCounter = 2;
					resetTimeCounter = false;
				}
			}
			if (resetTimeCounter) {
				timeCounter = 0;
			}
		} else if (duration != DURATION_INFINITE && duration != DURATION_ONCE && duration <= totalTime){
			isFinished = true;
		}
	}
	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	@Override
	public void reset() {
		timeCounter = 1;
		totalTime = 0;
		isFinished = false;
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.chance = actionElement.getInt(XML_ATTRIBUTE_CHANCE_TO_MOVE, 10);
		this.duration = actionElement.getInt(XML_ATTRIBUTE_DURATION, DURATION_INFINITE);
		this.totalTime = actionElement.getFloat(XML_ATTRIBUTE_TOTAL_TIME, 0);
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_CHANCE_TO_MOVE, chance)
				.attribute(XML_ATTRIBUTE_DURATION, duration)
				.attribute(XML_ATTRIBUTE_TOTAL_TIME, totalTime);
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}
}
