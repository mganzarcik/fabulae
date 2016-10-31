package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Fires the supplied event on the supplied quest.
 * <br /><br />
 * If the quest is not specified explicitly in the parameters,
 * the action will fire the event on the quest it is attached to.
 * <br /><br />
 * Otherwise, it will assert.
 * <br /><br />
* Example:
 * 
 * <pre>
 * 	&lt;fireQuestEvent quest="questId" event="eventId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class FireQuestEvent extends Action {

	public static final String XML_EVENT = "event";
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
		
		quest.processEvent(getParameter(XML_EVENT));
	}

	@Override
	public void validateAndLoadFromXML(Element actionElement) {
		if (actionElement.get(XML_EVENT, null) == null) {
			throw new GdxRuntimeException(XML_EVENT+" must be set for action FireQuestEvent in element: \n\n"+actionElement);
		}
	}

}
