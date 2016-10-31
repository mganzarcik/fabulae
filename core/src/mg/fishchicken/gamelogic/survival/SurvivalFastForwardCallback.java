package mg.fishchicken.gamelogic.survival;

import mg.fishchicken.core.FastForwardCallback;
import mg.fishchicken.ui.UIManager;

public abstract class SurvivalFastForwardCallback implements FastForwardCallback {
	
	@Override
	public void onCancelled(float timePassed) {	
	}
	
	@Override
	public boolean onInterrupted(InterruptReason reason, float timePassed) {
		onFinished();
		if (reason == InterruptReason.AMBUSH) {
			UIManager.hideCampPanel();
		}
		return true;
	}

}
