package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.characters.GameCharacter;

public class Behind extends SeveralAdjacent {
	
	public Behind() {
		super();
		length = 1;
	}

	@Override
	public boolean isValidTarget() {
		if (targets.size > 0) {
			GameObject target =targets.get(0);
			if (target instanceof GameCharacter) {
				Orientation requiredOrientation =  Orientation.calculateOrientationToTarget(getUser(), target);
				return Orientation.atLeastOneDirectionMatches(requiredOrientation,((GameCharacter)target).getOrientation());
			}
		}
		return false;
	}
	
	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "behind");
	}
}
