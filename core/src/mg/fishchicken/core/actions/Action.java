package mg.fishchicken.core.actions;

import java.lang.reflect.Modifier;

import groovy.lang.Binding;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Basic action used in xml files to make stuff happen.
 * 
 * Each action defined in an xml file has to have a corresponding class in this package.
 * 
 * @author ANNUN
 *
 */
public abstract class Action extends Condition {

	/**
	 * Creates a new Action instance from the supplied XML element.
	 * 
	 * This works both with action elements directly, where one action
	 * representing the action element is returned, and with elements
	 * containing multiple child action elements. In this case,
	 * an And action is constructed and all the children included in it.
	 * @param conditionElement
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Action getAction(Element actionElement) {
		if (actionElement == null) {
			return null;
		}
		String implementationClassName = Action.class.getPackage().getName()
				+ "." + StringUtil.capitalizeFirstLetter(actionElement.getName());
		Class<? extends Action> actionClass;
		try {
			actionClass = (Class<? extends Action>) Class
					.forName(implementationClassName);
			if (Modifier.isAbstract(actionClass.getModifiers())) {
				throw new ClassNotFoundException();
			}
		} catch (ClassNotFoundException e) {
			actionClass = And.class;
		}
		try {
			Action newAction = actionClass.newInstance();
			newAction.validateAndLoadFromXML(actionElement);
			readParameters(newAction, actionElement);
			return newAction;
		} catch (Exception e) {
			throw new GdxRuntimeException("Cannot load action from element: \n\n"+actionElement,e);
		}
	}
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		run(object, parameters);
		return true;
	}
	
	@Override
	public Array<ConditionResult> evaluateWithDetails(Object object, Binding parameters) {
		run(object, parameters);
		return new Array<ConditionResult>();
	}
	
	@Override
	public String toUIString() {
		return "";
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "";
	}
	
	/**
	 * Runs this Action on the supplied object.
	 * @param object
	 * @param parameters
	 */
	protected abstract void run(Object object, Binding parameters);
}
