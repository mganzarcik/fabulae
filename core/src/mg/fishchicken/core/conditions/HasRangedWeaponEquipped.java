package mg.fishchicken.core.conditions;

import mg.fishchicken.core.i18n.Strings;

/**
 * Returns true if the supplied character has a ranged weapon equipped
 * in any hand.
 * <br /><br />
 * If the optional parameter weaponSkill is defined, the weapon
 * must be of that skill type.
 * <br /><br />
 * If the optional  parameter dualWielding is set to true, a ranged
 * weapon must be equipped in both hands of the character.
 *  
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;hasRangedWeaponEquipped weaponSkill="Thrown" dualWielding = "true" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class HasRangedWeaponEquipped extends HasMeleeWeaponEquipped{

	@Override
	protected boolean checkingForMelee() {
		return false;
	}
	
	@Override
	public String toUIString() {
		return Strings.getString(STRING_TABLE, "rangedWeaponEquipped");
	}
}
