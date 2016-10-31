package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.VariableContainer;
import mg.fishchicken.gamestate.Variables;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied variable is false
 * on the supplied VariableContainer or Variables.
 * 
 * The variable can either be stored as boolean, 
 * or as a String not equal to the value "true".
 * 
 * This will also return true if the variable is 
 * not defined. 
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;variableFalse name="variableName" targetObject="__npcAtDialogue" /&gt;
 * </pre> 
 *
 * @author ANNUN
 *
 */
public class VariableFalse extends Condition {

	public static final String XML_VARIABLE_NAME = "name";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) { 
		if (object instanceof VariableContainer) {
			object = ((VariableContainer)object).variables();
		}
			
		if (object instanceof Variables) {
			return !((Variables)object).getBooleanVariable(getParameter(XML_VARIABLE_NAME));
		}
		
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_VARIABLE_NAME, null) == null) {
			throw new GdxRuntimeException(XML_VARIABLE_NAME+" must be set for condition VariableFalse in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "isFalse";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_VARIABLE_NAME)};
	}
}