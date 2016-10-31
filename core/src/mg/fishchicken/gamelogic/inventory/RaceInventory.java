package mg.fishchicken.gamelogic.inventory;

import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

public class RaceInventory extends Inventory {
	private InventoryCheckResult checkResult = new InventoryCheckResult();

	public RaceInventory(InventoryContainer ic) {
		super(ic);
	}

	@Override
	public Object getConditionTarget() {
		return null;
	}

	@Override
	public void onInventoryClose() {
	}

	@Override
	public InventoryCheckResult canAddItem(InventoryItem item) {
		return checkResult.setAllowedStackSize(item.getStackSize()).setError(null);
	}

	@Override
	public void addItem(InventoryItem item) {
		addToBag(BagType.BACKPACK, item);
	}

	@Override
	protected void onItemAdd(InventoryItem item, BagType bagType) {
	}

	@Override
	protected void onItemRemove(InventoryItem item, BagType bagType) {
	}

}
