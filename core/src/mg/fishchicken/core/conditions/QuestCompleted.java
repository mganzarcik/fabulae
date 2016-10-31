package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied Quest has been completed by the player.
 * <br/><br/>
 * Example:
 * <pre>
 * &lt;questCompleted quest="questId" /&gt;
 * </pre>
 */
public class QuestCompleted extends Condition {

	public static final String XML_QUEST = "quest";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		return Quest.getQuest(getParameter(XML_QUEST)).isFinished();
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "questCompleted";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_QUEST)};
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for condition QuestCompleted in element: \n\n"+conditionElement);
		}
	}

}
