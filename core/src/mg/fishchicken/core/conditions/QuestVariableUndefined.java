package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied variable is not defined
 * on the supplied Quest.
 *   
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;questVariableUndefined name="variableName" quest="questId" /&gt;
 * </pre> 
 *
 * @author ANNUN
 *
 */
public class QuestVariableUndefined extends VariableUndefined {
	
	public static final String XML_QUEST = "quest";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) { 
		return super.evaluate(Quest.getQuest(getParameter(XML_QUEST)), parameters);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		super.validateAndLoadFromXML(conditionElement);
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for condition QuestVariableUndefined in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "isUndefinedInQuest";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_VARIABLE_NAME), getParameter(XML_QUEST)};
	}
}