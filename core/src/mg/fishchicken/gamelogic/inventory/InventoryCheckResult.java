package mg.fishchicken.gamelogic.inventory;

public class InventoryCheckResult {
	
	private int allowedStackSize;
	private String error;
	
	public int getAllowedStackSize() {
		return allowedStackSize;
	}

	public InventoryCheckResult setAllowedStackSize(int allowedStackSize) {
		this.allowedStackSize = allowedStackSize;
		return this;
	}

	public String getError() {
		return error;
	}

	public InventoryCheckResult setError(String error) {
		this.error = error;
		return this;
	}
	

}
