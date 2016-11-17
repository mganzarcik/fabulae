package mg.fishchicken.gamelogic.actions;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter.State;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Sets the animation state of the supplied GameCharacter to the supplied value.
 * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>state - String - name of the state to use, see {@link AbstractGameCharacter.State}
 *	<li>duration - float - optional. After how many seconds should this action finish. Important to specify for states
 *		that have looped animations otherwise the action will never finish. If -1 is supplied, the action will only finish
 *		once the animation is finished (which is never for looped animations). -1 is the default value.
 *	<li>resetState - boolean - optional. If true, the character will be set back to Idle state once this action finishes. Otherwise
 *		it will remain in whatever state the Action set it to. Default is true.
 * </ol>
 * 
 * @author ANNUN
 *
 */
public class PlayStateAnimationAction extends BasicAction {
	
	private AbstractGameCharacter character;
	private String state;
	private boolean finished;
	private float duration;
	private float stateTime;
	private boolean resetState;
	
	public PlayStateAnimationAction() {
	}
	
	public PlayStateAnimationAction(GameCharacter character, String stateName) {
		init(character, stateName);
	}
	
	public PlayStateAnimationAction(GameCharacter character, String stateName, boolean resetState) {
		init(character, stateName, -1f, resetState);
	}
	
	public PlayStateAnimationAction(GameCharacter character, String state, float duration) {
		init(character, state, duration);
	}
	
	public PlayStateAnimationAction(GameCharacter character, String state, float duration, boolean resetState) {
		init(character, state, duration, resetState);
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		duration = -1;
		stateTime = 0;
		finished = false;
		resetState = true;
		if (!(ac instanceof AbstractGameCharacter)) {
			throw new GdxRuntimeException(this.getClass().getName()+ " can only be used on AbstractGameCharacters!");
		}
		character = (AbstractGameCharacter) ac;
		if (parameters.length > 0) {
			state = ((String) parameters[0]).toLowerCase(Locale.ENGLISH); 
		}
		
		if (parameters.length > 1) {
			duration = (float) parameters[1];
		}
		
		if (parameters.length > 2) {
			resetState = (boolean) parameters[2];
		}		
	}

	@Override
	public void update(float deltaTime) {
		if (character.getState() != state) {
			character.setState(state);
		} else {
			if (duration != -1) {
				stateTime += deltaTime;
			}
			if (character.isAnimationFinished() || (stateTime >= duration && duration != -1)) {
				if (resetState) {
					character.setState(State.IDLE);
				}
				finished = true;
			}
		}
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void reset() {
		finished = false;
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		state = actionElement.get(XML_ATTRIBUTE_STATE, null);
		if (state == null) {
			throw new GdxRuntimeException("state cannot be null");
		}
		duration = actionElement.getFloat(XML_ATTRIBUTE_DURATION, -1);
		resetState = actionElement.getBoolean(XML_ATTRIBUTE_RESET_STATE, true);
		stateTime = actionElement.getFloat(XML_ATTRIBUTE_STATE_TIME, 0f);
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_STATE, state)
				.attribute(XML_ATTRIBUTE_DURATION, duration)
				.attribute(XML_ATTRIBUTE_RESET_STATE, resetState)
				.attribute(XML_ATTRIBUTE_STATE_TIME, stateTime);
	}

}
