package mg.fishchicken.gamelogic.inventory;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.PlayerCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.survival.SurvivalManager;

public class PlayerCharacterGroupInventory extends Inventory {

	private InventoryCheckResult checkResult = new InventoryCheckResult();
	private PlayerCharacterGroup pcg;
	
	public PlayerCharacterGroupInventory(PlayerCharacterGroup pcg) {
		super(pcg);
		this.pcg = pcg;
	}

	@Override
	public void onItemAdd(InventoryItem item, BagType bagType) {
		item.executePlayerPickUpAction((PlayerCharacter)pcg.getGroupLeader());
	}
	
	@Override
	public void onItemRemove(InventoryItem item, BagType bagType) {
	}

	@Override
	public InventoryCheckResult canAddItem(InventoryItem item) {
		int stackSize = item.getStackSize();
		if (item.isWater()) {
			float remainingSpace = pcg.getMaxWater() - pcg.getWater();
			if (remainingSpace < stackSize) {
				return checkResult
						.setAllowedStackSize((int) Math.ceil(remainingSpace))
						.setError(Strings.getString(SurvivalManager.STRING_TABLE, "cannotCarryMoreWater"));
			}
		} else if (item.isFood()) {
			float remainingSpace = pcg.getMaxFood() - pcg.getFood();
			if (remainingSpace < stackSize) {
				return checkResult
						.setAllowedStackSize((int) Math.ceil(remainingSpace))
						.setError(Strings.getString(SurvivalManager.STRING_TABLE, "cannotCarryMoreFood"));
			}
		}
		return checkResult.setAllowedStackSize(stackSize).setError(null);
	}
	
	
	@Override
	public void addItem(InventoryItem item) {
		if (item.isGold()) {
			pcg.addGold(item.getStackSize());
			removeFromPreviousInventry(item);
			onItemAdd(item, BagType.BACKPACK);
		} else if (item.isWater()) {
			pcg.addWater(item.getStackSize());
			removeFromPreviousInventry(item);
			onItemAdd(item, BagType.BACKPACK);
		} else if (item.isFood()) {
			pcg.addFood(item.getStackSize());
			removeFromPreviousInventry(item);
			onItemAdd(item, BagType.BACKPACK);
		} else {
			addToBag(BagType.BACKPACK, item);
		}
	}
	
	@Override
	public void onInventoryClose() {
	}
	
	@Override
	public Object getConditionTarget() {
		return pcg;
	}
	
}


