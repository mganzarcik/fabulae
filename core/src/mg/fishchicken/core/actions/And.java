package mg.fishchicken.core.actions;

import groovy.lang.Binding;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * An And Action executes all actions it contains.
 * @author ANNUN
 *
 */
public class And extends Action {

	protected Array<Action> actions = new Array<Action>();
	
	public And() {
	}
	
	public And(Action... actions) {
		this.actions.addAll(actions);
	}
	
	protected boolean evaluate(Object object, Binding parameters) {
		for (Action action : actions) {
			action.execute(object, parameters);
		}
		return true;
	}
	
	@Override
	protected void run(Object object, Binding parameters) {
		for (Action action : actions) {
			action.run(object, null);
		}
	}
	
	@Override
	public void validateAndLoadFromXML(Element actionElement) {
		for (int i = 0; i < actionElement.getChildCount(); ++i) {
			Element childActionElement = actionElement.getChild(i);
			actions.add(Action.getAction(childActionElement));	
		}
		if (actions.size < 1) {
			throw new GdxRuntimeException("Action And must have at least one child action in element "+actionElement);
		}
	}
}
