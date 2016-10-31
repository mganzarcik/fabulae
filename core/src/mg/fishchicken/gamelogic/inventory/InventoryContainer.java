package mg.fishchicken.gamelogic.inventory;

import mg.fishchicken.gamelogic.inventory.items.InventoryItem;


public interface InventoryContainer {
	
	public String getName();
	
	public Inventory getInventory();
	
	public boolean isOwnerOf(InventoryItem item);
	
}
