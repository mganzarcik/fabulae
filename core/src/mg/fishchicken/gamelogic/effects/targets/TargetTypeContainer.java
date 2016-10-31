package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.ThingWithId;
import groovy.lang.Script;

public interface TargetTypeContainer extends ThingWithId {

	public void setTargetType(String targetType, Script targetScript);
}
