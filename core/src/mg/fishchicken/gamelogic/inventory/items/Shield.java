package mg.fishchicken.gamelogic.inventory.items;

import java.io.IOException;

import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;

import com.badlogic.gdx.files.FileHandle;

public class Shield extends Armor {

	public Shield() {
		super();
	}
	
	public Shield(FileHandle file) throws IOException {
		super(file);
	}
	
	@Override
	public boolean canBeEquippedDuringCombat() {
		return true;
	}
	
	@Override
	public ItemSlot[] getAllowedSlots() {
		ItemSlot[] returnValue = super.getAllowedSlots();
		if (returnValue.length < 1) {
			returnValue = Inventory.HAND_SLOTS;
		}
		return returnValue;
	}
	
	@Override
	public boolean canBeUnequipped(int slot, InventoryContainer container) {
		return true;
	}

	@Override
	public boolean isTwoHanded() {
		return false;
	}
	
}