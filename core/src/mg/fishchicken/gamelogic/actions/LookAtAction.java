package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.OrientedThing;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.util.Orientation;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Simple action that makes the supplied GameObject implementing the OrientedThing interface
 * look at the supplied coordinates or PositionedThing. 
 * <br /><br />
 * Parameters:
 * <ol>
 *  <li>target - PositionedThing - where to look
 * </ol>
 * or
 * <ol>
 *  <li>x - float- where to look
 *  <li>y - float- where to look
 * </ol>
 * @author Annun
 *
 */
public class LookAtAction extends BasicAction {
	
	private float targetX, targetY;
	private GameObject go;
	private boolean isFinished;
		
	public LookAtAction() {
	}
	
	/**
	 * Create a new LookAtAction action with the given target coordinates
	 */
	public LookAtAction(ActionsContainer ac, float x, float y) {
		init(ac, x, y);
	}
	
	/**
	 * Create a new LookAtAction action with the given target coordinates
	 */
	public LookAtAction(ActionsContainer ac, PositionedThing target) {
		init(ac, target);
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameObject) || !(ac instanceof OrientedThing)) {
			throw new GdxRuntimeException("LookAtAction only works on GameObjects implementing OrientedThing!");
		}
		isFinished = false;
		this.go = (GameObject) ac;
		if (parameters.length == 1) { 
			PositionedThing target = (PositionedThing) parameters[0];
			targetX = target.position().getX();
			targetY = target.position().getY();
		} else if  (parameters.length == 2) { 
			targetX = (Float)parameters[0];
			targetY = (Float)parameters[1];
		} 
	}

	@Override
	public void update(float deltaTime) {
		isFinished = true;
		if (go != null) {
			((OrientedThing)go).setOrientation(Orientation.calculateOrientationToTarget(go.getMap().isIsometric(), go.position().getX(), go.position().getY(), targetX, targetY));
		}
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	@Override
	public void reset() {
		
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.targetX = actionElement.getFloat(Action.XML_ATTRIBUTE_X, -1);
		if (targetX < 0) {
			throw new GdxRuntimeException("x must be specified for LookAtAction");
		}
		this.targetY = actionElement.getFloat(Action.XML_ATTRIBUTE_Y, -1);
		if (targetY < 0) {
			throw new GdxRuntimeException("y must be specified for LookAtAction");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_X, targetX);
		writer.attribute(XML_ATTRIBUTE_Y, targetY);
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}
}
