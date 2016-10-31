package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.VariableContainer;
import mg.fishchicken.gamestate.Variables;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied variable is not defined
 * on the supplied VariableContainer or Variables.
 *   
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;variableUndefined name="variableName" targetObject="__npcAtDialogue" /&gt;
 * </pre> 
 *
 * @author ANNUN
 *
 */
public class VariableUndefined extends Condition {

	public static final String XML_VARIABLE_NAME = "name";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) { 
		if (object instanceof VariableContainer) {
			object = ((VariableContainer)object).variables();
		}
			
		if (object instanceof Variables) {
			return ((Variables)object).getVariable(getParameter(XML_VARIABLE_NAME)) == null;
		}
		
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_VARIABLE_NAME, null) == null) {
			throw new GdxRuntimeException(XML_VARIABLE_NAME+" must be set for condition VariableUndefined in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "isUndefined";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_VARIABLE_NAME)};
	}
}