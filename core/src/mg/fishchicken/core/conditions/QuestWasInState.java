package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
/**
 * Returns true if the supplied Quest is in the supplied state,
 * or was in the state at any point since it started.
 * <br/><br/>
 * Example:
 * <pre>
 * &lt;questWasInState quest="questId" state="stateId" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class QuestWasInState extends Condition {

	public static final String XML_STATE = "state";
	public static final String XML_QUEST = "quest";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		Quest quest = Quest.getQuest(getParameter(XML_QUEST));
		return quest.wasInState(getParameter(XML_STATE));
	}

	@Override
	protected String getStringTableNameKey() {
		return "questWasInState";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_STATE)};
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for condition QuestWasInState in element: \n\n"+conditionElement);
		}
		if (conditionElement.get(XML_STATE, null) == null) {
			throw new GdxRuntimeException(XML_STATE+" must be set for condition QuestWasInState in element: \n\n"+conditionElement);
		}
	}

}
