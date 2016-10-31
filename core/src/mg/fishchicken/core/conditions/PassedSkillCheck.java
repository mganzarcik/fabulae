package mg.fishchicken.core.conditions;

import groovy.lang.Binding;

import java.util.Locale;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the last skill check that was done
 * for the supplied NonPlayerCharacter and the supplied
 * skill was passed.
 * <br/><br/>
 * This is usually called after a call to RollSkillCheckAgainstNPC.
 * <br/><br/>
 * Example:
 * <pre>
 * &lt;passedSkillCheck skill="Persuasion" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class PassedSkillCheck extends Condition {

	public static final String XML_SKILL_NAME = "skill";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			return ((GameCharacter) object).stats().passedSkillCheck(getParameter(XML_SKILL_NAME));
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_SKILL_NAME, null) == null) {
			throw new GdxRuntimeException(XML_SKILL_NAME+" must be set for condition PassedSkillCheck in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "passedSkillCheck";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{Skill.valueOf(getParameter(XML_SKILL_NAME).toUpperCase(Locale.ENGLISH)).toUIString()};
	}

}
