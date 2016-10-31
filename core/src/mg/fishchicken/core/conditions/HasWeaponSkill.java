package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamestate.characters.Skills;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Returns true if the supplied character has a weapon skill
 * larger or equal to the value of the parameter minimumSkillRank. 
 * If the parameter is not defined, it is considered 1.
 * <br /><br /> 
 * Any of the following skills are considered weapon skills: 
 * <ul>
 *  <li>Axe
 *  <li>Dagger
 *  <li>Staff
 *  <li>Sword
 *  <li>Bow
 *  <li>Thrown
 * </ul>
 *  
 * Example:
 * 
 * <pre>
 * &lt;hasWeaponSkill minimumSkillRank="3" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class HasWeaponSkill extends HasSkill {

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			Skills skills = ((GameCharacter) object).stats().skills();
			String minRankStr = getParameter(XML_MINUMUM_SKILL_LEVEL);
			int minRank = minRankStr != null ? Integer.parseInt(minRankStr) : 1;
			if (skills.getSkillRank(Skill.AXE) >= minRank) {
				return true;
			}
			if (skills.getSkillRank(Skill.DAGGER) >= minRank) {
				return true;
			}
			if (skills.getSkillRank(Skill.STAFF) >= minRank) {
				return true;
			}
			if (skills.getSkillRank(Skill.SWORD) >= minRank) {
				return true;
			}
			if (skills.getSkillRank(Skill.BOW) >= minRank) {
				return true;
			}
			if (skills.getSkillRank(Skill.THROWN) >= minRank) {
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

	@Override
	public String toUIString() {
		StringBuilder fsb = StringUtil.getFSB();
		fsb.append(Strings.getString(STRING_TABLE, "anyWeaponSkill"));
		if (getParameter(XML_MINUMUM_SKILL_LEVEL) != null) {
			fsb.append(": ");
			fsb.append(getParameter(XML_MINUMUM_SKILL_LEVEL));
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue;
	}

}
