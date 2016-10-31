package mg.fishchicken.core;

public interface FastForwardCallback {

	public enum InterruptReason { AMBUSH, COMBAT };
	
	/**
	 * Called once the time has been fast forwarded successfully.
	 */
	public void onFinished();
	
	/**
	 * Called if the player cancelled the time fast forwarding.
	 * @param timePassed
	 */
	public void onCancelled(float timePassed);
	
	/**
	 * Called if the time fast forwarding was interrupted by external reasons.
	 * 
	 * If false is returned, fast forwarding will continue and the reasons will be ignored. 
	 * 
	 * If true is returned, it will be interrupted.
	 * 
	 * @param reason
	 * @param timePassed - time that has passed from the start of the fast forward to the interruption, in game hours
	 * @return
	 */
	public boolean onInterrupted(InterruptReason reason, float timePassed);
}
