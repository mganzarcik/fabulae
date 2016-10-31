package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied variable defined
 * on the supplied Quest is equal to the supplied value.
 * 
 * This does string comparisons.
 *   
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;questVariableEqualTo quest="questId" name="variableName" value="variableValue" /&gt;
 * </pre> 
 *
 * @author ANNUN
 *
 */
public class QuestVariableEqualTo extends VariableEqualTo {
	
	public static final String XML_QUEST = "quest";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) { 
		return super.evaluate(Quest.getQuest(getParameter(XML_QUEST)), parameters);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		super.validateAndLoadFromXML(conditionElement);
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for condition QuestVariableEqualTo in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "isEqualToInQuest";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_VARIABLE_NAME), getParameter(XML_VARIABLE_VALUE), getParameter(XML_QUEST)};
	}
}