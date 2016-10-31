package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * 
 * Rolls a skill check of the supplied character against 
 * the NPC character currently engaged in dialogue.
 * <br /><br />
 * Example:
 * <pre>
 *  &lt;rollSkillCheckAgainstNPC skill="Persuasion" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * 
 */
public class RollSkillCheckAgainstNPC extends Action {

	public static final String XML_SKILL_NAME = "skill";
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			((GameCharacter) object).stats().rollSkillCheck(getParameter(XML_SKILL_NAME), (SkillCheckModifier) parameters.getVariable(PARAM_NPC_AT_DIALOGUE));
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_SKILL_NAME, null) == null) {
			throw new GdxRuntimeException(XML_SKILL_NAME+" must be set for action RollSkillCheck in element: \n\n"+conditionElement);
		}
	}

}
