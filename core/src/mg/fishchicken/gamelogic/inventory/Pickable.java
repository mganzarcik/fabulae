package mg.fishchicken.gamelogic.inventory;

import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

public interface Pickable {

	/**
	 * Pick up this Pickable and store it in
	 * the supplied InventoryContainer.
	 * 
	 * @param container
	 * @return true if the item was picked up, false if an error occured
	 */
	public boolean pickUp(InventoryContainer container);
	
	/**
	 * Gets the ID of the InventoryItem this Pickable contains.
	 * @return
	 */
	public String getInventoryItemId();
	
	/**
	 * Get the InventoryItem this Pickable represents.
	 * 
	 * @return
	 */
	public InventoryItem getInventoryItem();
}
