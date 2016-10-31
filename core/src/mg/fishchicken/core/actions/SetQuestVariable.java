package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * 
 * Sets a variable with the supplied name to the supplied value
 * on the supplied Quest.
 * <br /><br />
 * The value will be stored as plain String.
 * <br /><br />
 * Example:
 * <br /><br />
 * <pre>
 * &lt;setQuestVariable name="variableName" value="variableValue" quest="questId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class SetQuestVariable extends SetVariable {

	public static final String XML_QUEST = "quest";
	
	@Override
	protected void run(Object object, Binding parameters) {
		super.run(Quest.getQuest(getParameter(XML_QUEST)), parameters);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_QUEST, null) == null) {
			throw new GdxRuntimeException(XML_QUEST+" must be set for action SetQuestVariable in element: \n\n"+conditionElement);
		}
	}

}
