package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.VariableContainer;
import mg.fishchicken.gamestate.Variables;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;


/**
 * Returns true if the supplied variable defined
 * on the supplied Variables or VariableContainer is equal to the supplied value.
 * 
 * This does string comparisons. Case sensitive.
 *   
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;variableEqualTo name="variableName" value="variableValue"  targetObject="__npcAtDialogue" /&gt;
 * </pre> 
 *
 * @author ANNUN
 *
 */
public class VariableEqualTo extends Condition {

	public static final String XML_VARIABLE_NAME = "name";
	public static final String XML_VARIABLE_VALUE = "value";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) { 
		if (object instanceof VariableContainer) {
			object = ((VariableContainer)object).variables();
		}
			
		if (object instanceof Variables) {
			return getParameter(XML_VARIABLE_VALUE).equals(
					((Variables) object).getStringVariable(getParameter(XML_VARIABLE_NAME)));
		}
		
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_VARIABLE_NAME, null) == null) {
			throw new GdxRuntimeException(XML_VARIABLE_NAME+" must be set for condition VariableUndefined in element: \n\n"+conditionElement);
		}
		
		if (conditionElement.get(XML_VARIABLE_VALUE, null) == null) {
			throw new GdxRuntimeException(XML_VARIABLE_VALUE+" must be set for condition VariableUndefined in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "isEqualTo";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_VARIABLE_NAME), getParameter(XML_VARIABLE_VALUE)};
	}
}