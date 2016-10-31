package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;

/**
 * Single target at any range
 * that must already be injured.
 * 
 * @author ANNUN
 *
 */
public class SingleInjured extends Single {
	
	@Override
	public boolean isValidTarget() {
		if (!super.isValidTarget()) {
			return false;
		}
		GameObject target = targets.get(0);
		if (target instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) target;
			return character.stats().getHPAct() < character.stats().getHPMax();
		}
		return false;
	}
	
	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "singleInjured");
	}
}
