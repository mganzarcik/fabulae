package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied Quest has been started.
 * <br/><br/>
 * Example:
 * <pre>
 * &lt;questStarted quest="questId" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class QuestStarted extends Condition {

	public static final String XML_QUEST = "quest";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		return Quest.getQuest(getParameter(XML_QUEST)).isStarted();
	}

	@Override
	protected String getStringTableNameKey() {
		return "questStarted";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return  new Object[]{getParameter(XML_QUEST)};
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for condition QuestStarted in element: \n\n"+conditionElement);
		}
	}

}
