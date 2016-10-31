package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.VariableContainer;
import mg.fishchicken.gamestate.Variables;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * 
 * Sets a variable with the supplied name to the supplied value
 * on the supplied VariableContainer.
 * <br /><br />
 * The value will be stored as plain String.
 * <br /><br />
 * Example:
 * <br /><br />
 * <pre>
 * &lt;setVariable name="variableName" value="variableValue" targetObject="__npcAtDialogue" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class SetVariable extends Action {

	public static final String XML_VARIABLE_NAME = "name";
	public static final String XML_VARIABLE_VALUE = "value";
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof VariableContainer) {
			object = ((VariableContainer)object).variables();
		}
			
		if (object instanceof Variables) {
			((Variables)object).setVariable(getParameter(XML_VARIABLE_NAME), getParameter(XML_VARIABLE_VALUE));
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_VARIABLE_NAME, null) == null) {
			throw new GdxRuntimeException(XML_VARIABLE_NAME+" must be set for condition VariableTrue in element: \n\n"+conditionElement);
		}
	}

}
