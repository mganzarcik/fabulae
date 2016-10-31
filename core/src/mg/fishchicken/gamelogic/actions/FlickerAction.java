package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.ColoredThing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the ColoredThing flicker for a time by setting
 * its alpha to zero and back repeatedly.
 * 
 * Parameters:
 * 	<ol>
 * 	<li>duration - int (in seconds), default 1s
 * 	<li>one flick duration - float (in seconds), default 0.2
 *  </ol>
 * @author Annun
 *
 */
public class FlickerAction extends BasicAction {

	// duration of the flicker in seconds
	public int duration = 1;
	public float flickDuration = 0.2f; 
	private float stateTime;
	private Color originalColor = new Color();
	private ActionsContainer ac;
	private ColoredThing ct;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof ColoredThing)) {
			throw new GdxRuntimeException("FlickerAction only works on ColoredThing!");
		}
		stateTime = 0;
		this.ac = ac;
		this.ct = (ColoredThing)ac;
		originalColor.set(ct.getColor());
		duration = 1;
		flickDuration = 0.2f; 
		if (parameters.length > 0) {
			duration = (Integer) parameters[0];
		}
		if (parameters.length > 1) {
			flickDuration = (Float) parameters[1];
		}
	}
	

	@Override
	public void reset() {
		init(ac, duration, flickDuration);
	}

	@Override
	public void update(float deltaTime) {
		stateTime += deltaTime;
		float modulo = stateTime % flickDuration;
		if (modulo > 0 && modulo < flickDuration/2 && !isFinished()) {
			ct.getColor().set(Color.CLEAR);
		} else {
			ct.getColor().set(originalColor);
		}
	}

	@Override
	public boolean isFinished() {
		if (duration == -1) {
			return false;
		}
		return stateTime > duration;
	}
	
	@Override
	public boolean isBlockingInCombat() {
		return false;
	}
	
	/**
	 * This is not pausable, since it is a graphical effect only.
	 */
	@Override
	public void pause() {
		return;
	}
	
	@Override
	public void onRemove(ActionsContainer ac) {
		ct.setColor(originalColor);
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		duration = actionElement.getInt(Action.XML_ATTRIBUTE_DURATION, duration);
		flickDuration = actionElement.getFloat(Action.XML_ATTRIBUTE_FLICK_DURATION, flickDuration);
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_DURATION, duration)
				.attribute(XML_ATTRIBUTE_FLICK_DURATION, flickDuration);
	}
}
