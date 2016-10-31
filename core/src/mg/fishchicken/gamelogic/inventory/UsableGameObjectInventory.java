package mg.fishchicken.gamelogic.inventory;

import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

public class UsableGameObjectInventory extends Inventory {

	private InventoryCheckResult checkResult = new InventoryCheckResult();
	private UsableGameObject ugo;
	
	public UsableGameObjectInventory(UsableGameObject ugo) {
		super(ugo);
		this.ugo = ugo;
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
	public void onItemAdd(InventoryItem item, BagType bagType) {
		ugo.processEvent("itemAdded", null);
	}

	@Override
	public void onItemRemove(InventoryItem item, BagType bagType) {
		ugo.processEvent("itemRemoved", null);
	}
	
	@Override
	public void onInventoryClose() {
		ugo.processEvent("inventoryClosed", ugo);
	}
	
	@Override
	public Object getConditionTarget() {
		return ugo;
	}

	
}
