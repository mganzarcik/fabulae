package mg.fishchicken.ui.toolbar;

import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.ui.inventory.InventoryItemButton;

public class QuickUseButton extends InventoryItemButton {
	
	public QuickUseButton(InventoryItemButtonStyle style, int slot,
			Inventory inventory, BagType bagType) {
		super(style, slot, inventory, bagType, null);
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		InventoryItem item = getItem();
		setDisabled(item == null || !Condition.areResultsOk(item.canBeUsedBy(getInventory().getParentContainer())));
	}
	
	@Override
	public void setDisabled(boolean isDisabled) {
		boolean wasDisabled = isDisabled();
		super.setDisabled(isDisabled);
		if (itemIcon != null && wasDisabled != isDisabled) {
			float alpha = itemIcon.getColor().a;
			itemIcon.getColor().a = isDisabled ? alpha / 2f : 1; 
		}
	}

}
