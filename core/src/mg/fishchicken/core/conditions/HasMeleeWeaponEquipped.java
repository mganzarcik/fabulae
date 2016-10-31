package mg.fishchicken.core.conditions;

import groovy.lang.Binding;

import java.util.Locale;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.Weapon;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied character has a melee weapon equipped
 * in any hand.
 * <br /><br />
 * If the optional parameter weaponSkill is defined, the weapon
 * must be of that skill type.
 * <br /><br />
 * If the optional  parameter dualWielding is set to true, a melee
 * weapon must be equipped in both hands of the character.
 *  
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;hasMeleeWeaponEquipped weaponSkill="Dagger" dualWielding = "true" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class HasMeleeWeaponEquipped extends Condition {

	private static final String XML_WEAPON_SKILL = "weaponSkill";
	private static final String XML_DUAL_WIELDING = "dualWielding";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) object;
			String weaponSkillString = getParameter(XML_WEAPON_SKILL);
			boolean dualWieldingOnly = Boolean.valueOf(getParameter(XML_DUAL_WIELDING));
			
			InventoryItem leftHand = character.getInventory().getEquipped(ItemSlot.LEFTHAND);
			InventoryItem rightHand = character.getInventory().getEquipped(ItemSlot.RIGHTHAND);
			Skill leftHandSkill = null;
			Skill righHandSkill = null;
			if (!(leftHand instanceof Weapon) && !(rightHand instanceof Weapon)) {
				return false;
			} 
			
			boolean hasRightType = false;
			
			boolean isDualWielding = true;
			
			if (leftHand instanceof Weapon) {
				leftHandSkill = ((Weapon) leftHand).getWeaponSkill();
				hasRightType = !((Weapon) leftHand).isRanged();
				if (!checkingForMelee()) {
					hasRightType = !hasRightType;
				}
			} else {
				isDualWielding = false;
			}
			
			if (rightHand instanceof Weapon) {
				righHandSkill = ((Weapon) rightHand).getWeaponSkill();
				hasRightType |= checkingForMelee() ? !((Weapon) rightHand)
						.isRanged() : ((Weapon) rightHand).isRanged();
			} else {
				isDualWielding = false;
			}
			
			if (!hasRightType || (!isDualWielding && dualWieldingOnly)) {
				return false;
			}
			
			// if no type was specified, any weapon will do, so we are done
			if (weaponSkillString == null) {
				return true;
			}
			
			Skill weaponSkill = Skill.valueOf(weaponSkillString.toUpperCase(Locale.ENGLISH));
			if (!weaponSkill.equals(righHandSkill) && !weaponSkill.equals(leftHandSkill)) {
				return false;
			}
			
			return true;
		}
		return false;
	}
	
	protected boolean checkingForMelee() {
		return true;
	}
	
	@Override
	protected String getStringTableNameKey() {
		return Boolean.valueOf(getParameter(XML_DUAL_WIELDING)) ?  "meleeWeaponEquippedDual" : "meleeWeaponEquipped";
	}
	
	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		String skillName = conditionElement.get(XML_WEAPON_SKILL, null);
		if (skillName != null) {
			try {
				Skill.valueOf(skillName.toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException e){
				throw new GdxRuntimeException(XML_WEAPON_SKILL+" contains invalid value "+skillName+", which is not an existing skill in condition "+this.getClass().getSimpleName()+" in element: \n\n"+conditionElement);
			}
		}
	}

}
