package mg.fishchicken.gamestate.locks;

import mg.fishchicken.gamestate.ObservableState;

public class Lock extends ObservableState<Lock, Lock.LockChanges> {
	
	public static final String XML_LOCK = "lock";
	
	private LockChanges changes;
	private boolean s_isPickable;
	private boolean s_isLocked;
	private int s_lockLevel;
	private String s_keyId;

	public Lock() {
		this(true, false, 0, null);
	}
	
	public Lock(boolean pickable, boolean locked, int lockLevel, String keyId){
		changes = new LockChanges();
		s_isPickable = pickable;
		s_isLocked = locked;
		s_keyId = keyId;
		s_lockLevel = lockLevel;
	}
	
	public Lock(Lock lock) {
		this(lock.s_isPickable, lock.s_isLocked, lock.s_lockLevel, lock.s_keyId);
	}
	
	public boolean isPickable() {
		return s_isPickable;
	}

	public void setPickable(boolean isPickable) {
		changes.setOld(this);
		s_isPickable = isPickable;
		changed(changes);
	}

	public boolean isLocked() {
		return s_isLocked;
	}

	public void setLocked(boolean isLocked) {
		changes.setOld(this);
		s_isLocked = isLocked;
		changed(changes);
	}

	public String getKeyId() {
		return s_keyId;
	}

	public void setKeyId(String keyId) {
		changes.setOld(this);
		s_keyId = keyId;
		changed(changes);
	}

	public int getLockLevel() {
		return s_lockLevel;
	}

	public void setLockLevel(int lockLevel) {
		changes.setOld(this);
		s_lockLevel = lockLevel;
		changed(changes);
	}
	
	public static class LockChanges {
		private boolean wasLocked;
		private boolean wasPickable;
		private String oldKeyId;
		private int oldLockLevel;
		
		private void setOld(Lock lock) {
			wasLocked = lock.s_isLocked;
			oldKeyId = lock.s_keyId;
			oldLockLevel = lock.s_lockLevel;
			wasPickable = lock.s_isPickable;
		}
		
		public boolean wasLocked() {
			return wasLocked;
		}
		
		public boolean wasPickable() {
			return wasPickable;
		}
		
		public String getOldKeyId() {
			return oldKeyId;
		}
		
		public int getOldLockLevel() {
			return oldLockLevel;
		}
	}

}
