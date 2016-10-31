package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Does nothing for the supplied amount of seconds and then finishes.
 * <br /><br />
 * Parameters:
 * <ol>
 *  <li>duration - float - how long (in seconds) will the action do nothing
 * </ol>
 * @author Annun
 *
 */
public class WaitAction extends BasicAction {
	
	private float totalTime = 0;
	private float duration;
		
	public WaitAction() {
	}
	
	/**
	 * Create a new Wait action with the given duration
	 * @param character
	 * @param radius
	 * @param chanceToMove
	 */
	public WaitAction(float duration) {
		init(null, duration);
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (parameters.length == 1) { 
			this.duration = (Float) parameters[0];
		}
	}

	@Override
	public void update(float deltaTime) {
		totalTime += deltaTime;
	}

	@Override
	public boolean isFinished() {
		return totalTime > duration;
	}
	
	@Override
	public void reset() {
		totalTime = 0;
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.duration = actionElement.getFloat(Action.XML_ATTRIBUTE_DURATION, 0);
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_DURATION, duration);
	}

	@Override
	public boolean isBlockingInCombat() {
		return true;
	}
}
