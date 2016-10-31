package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Starts the supplied quest.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * 	&lt;startQuest quest="questId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class StartQuest extends Action {

	public static final String XML_QUEST = "quest";

	
	@Override
	protected void run(Object object, Binding parameters) {
		Quest quest = null;
		if (object instanceof Quest) {
			quest = (Quest) object;
		}
		
		String questId = getParameter(XML_QUEST);
		if (questId != null) {
			quest = Quest.getQuest(questId);
		}
		
		if (quest == null) {
			throw new GdxRuntimeException("Could not find quest with id "+questId+" for action "+getClass().getName());
		}
		
		quest.start();
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for action StartQuest in element: \n\n"+conditionElement);
		}
	}

}
