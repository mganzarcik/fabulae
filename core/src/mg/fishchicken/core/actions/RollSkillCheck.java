package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * 
 * Rolls a skill check for the supplied character. If there is a NPC at dialogue,
 * he will be used as a skill check modifier, together with any map the supplied character
 * is currently at.
 * <br /><br />
 * Example:
 * <pre>
 *  &lt;rollSkillCheck skill="Persuasion" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * 
 */
public class RollSkillCheck extends Action {

	public static final String XML_SKILL_NAME = "skill";
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) object;
			character.stats().rollSkillCheck(getParameter(XML_SKILL_NAME), character.getMap(), (SkillCheckModifier) parameters.getVariable(PARAM_NPC_AT_DIALOGUE));
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_SKILL_NAME, null) == null) {
			throw new GdxRuntimeException(XML_SKILL_NAME+" must be set for action rollSkillCheck in element: \n\n"+conditionElement);
		}
	}

}
