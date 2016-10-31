package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied variable is false
 * on the supplied VariableContainer.
 * 
 * The variable can either be stored as boolean, 
 * or as a String not equal to the value "true".
 * 
 * This will also return true if the variable is 
 * not defined. 
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;questVariableFalse name="variableName" quest="questId" /&gt;
 * </pre> 
 *
 * @author ANNUN
 *
 */
public class QuestVariableFalse extends VariableFalse {
	
	public static final String XML_QUEST = "quest";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) { 
		return super.evaluate(Quest.getQuest(getParameter(XML_QUEST)), parameters);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		super.validateAndLoadFromXML(conditionElement);
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for condition QuestVariableFalse in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "isFalseInQuest";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_VARIABLE_NAME), getParameter(XML_QUEST)};
	}
}