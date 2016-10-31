package mg.fishchicken.core.conditions;

import groovy.lang.Binding;

import java.util.Locale;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied character has the supplied skill of at least
 * rank 1, or, if the optional parameter minimumskillLevel is defined, of at
 * least that rank. If the optional useBase parameter is set to true, the
 * condition will use a base skill rank (without modifiers) to perform the check
 * in case it is higher than the modified skill rank. <br />
 * <br />
 * Example:
 * 
 * <pre>
 * &lt;hasSkill skill = "Hunting" rank="3" useBase="true" /&gt;
 * </pre>
 * 
 * @author ANNUN
 * 
 */
public class HasSkill extends Condition {

	private static final String XML_SKILL_NAME = "skill";
	protected static final String XML_MINUMUM_SKILL_LEVEL = "rank";
	protected static final String XML_USE_BASE = "useBase";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) object;
			Skill skill = Skill.valueOf(getParameter(XML_SKILL_NAME).toUpperCase(Locale.ENGLISH));
			int rank = character.stats().skills().getSkillRank(skill);
			if (Boolean.valueOf(getParameter(XML_MINUMUM_SKILL_LEVEL))) {
				if (rank < character.stats().skills().getBaseSkillRank(skill)) {
					rank = character.stats().skills().getBaseSkillRank(skill);
				}
			}
			String minRank = getParameter(XML_MINUMUM_SKILL_LEVEL);
			if (minRank == null)  {
				return rank > 0;
			} else {
				return rank >= Integer.parseInt(minRank);
			}
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		String skillName = conditionElement.get(XML_SKILL_NAME, null);
		if (skillName == null) {
			throw new GdxRuntimeException(XML_SKILL_NAME+" must be set for condition HasSkill in element: \n\n"+conditionElement);
		}
		try {
			Skill.valueOf(skillName.toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException e){
			throw new GdxRuntimeException(XML_SKILL_NAME+" contains invalid value "+skillName+", which is not an existing skill in condition HasSkill in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "hasSkill";
	}
	@Override
	protected Object[] getStringNameParams() {
		return new Object[] {
				Skill.valueOf(
						getParameter(XML_SKILL_NAME)
								.toUpperCase(Locale.ENGLISH)).toUIString(),
				getParameter(XML_MINUMUM_SKILL_LEVEL) != null ? getParameter(XML_MINUMUM_SKILL_LEVEL)
						: "1" };
	}

}
