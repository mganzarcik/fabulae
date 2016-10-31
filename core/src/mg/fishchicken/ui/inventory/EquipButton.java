package mg.fishchicken.ui.inventory;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.ImageButtonWithSound.ImageButtonWithSoundStyle;

public class EquipButton extends InventoryItemButton {
	
	private EquipButtonStyle style;
	
	public EquipButton(EquipButtonStyle style, int slot, Inventory inventory, GameCharacter merchant) {
		this(style, slot, inventory, BagType.EQUIPPED, merchant);
	}
	
	public EquipButton(EquipButtonStyle style, int slot, Inventory inventory, BagType bagType, GameCharacter merchant) {
		super(style, slot, inventory, bagType, merchant);
		this.style = style;
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		InventoryItem draggedItem = UIManager.getDraggedItem();
		
		
		if (draggedItem != null && draggedItem.canBeAddedTo(getBagType(), getSlot(), getInventory().getParentContainer()) < 1) {
			setStyle(style.cannotEquipStyle);
		} else if (getItem() == null) {
			setStyle(style.emptyStyle);
		} else {
			setStyle(style.occupiedStyle);
		}
	}
	
	public static class EquipButtonStyle extends InventoryItemButtonStyle {
		private ImageButtonWithSoundStyle cannotEquipStyle;
	}
}
