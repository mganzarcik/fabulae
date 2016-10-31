package mg.fishchicken.gamestate.locks;

import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.Observer;
import mg.fishchicken.gamestate.locks.Lock.LockChanges;

/**
 * A simple observer that disarms a trap if the lock it observes becomes unlocked.
 *
 */
public class TrapDisarmingLockObserver implements Observer<Lock, LockChanges>{

	private Trapable trapable;
	
	public TrapDisarmingLockObserver(Trapable trapable) {
		this.trapable = trapable;
	}
	
	@Override
	public void hasChanged(Lock lock, LockChanges changes) {
		// if we were just unlocked, also disarm the trap
		if(changes.wasLocked() && !lock.isLocked()) {
			if (trapable.getTrap() != null) {
				trapable.getTrap().setDisarmed(true);
			}
		}
	}

}
