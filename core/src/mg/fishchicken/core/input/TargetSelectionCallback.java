package mg.fishchicken.core.input;

import mg.fishchicken.gamelogic.effects.targets.TargetType;

public interface TargetSelectionCallback {

	public void targetSelectionCompleted(Targetable targetable, TargetType effectTarget);
	public void targetSelectionCancelled(Targetable targetable, TargetType effectTarget);
}
