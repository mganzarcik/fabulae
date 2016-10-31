package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.ui.UIManager;

public class TradingEventHandler extends InventoryEventHandler {
	private TradingPanel tradingPanel;
	
	public TradingEventHandler(GameState gameState) {
		super(gameState);
	}
	
	public void setTradingPanel(TradingPanel tradingPanel) {
		this.tradingPanel = tradingPanel;
	}
	
	protected void swapOrCombineItems(InventoryItemButton clickedSlot, InventoryItem clickedSlotItem, InventoryItem draggedItem) {
		
		if (clickedSlot.getContainer()
				.equals(tradingPanel.getDisplayedMerchant())) {
			if (!canAfford(clickedSlotItem, true)) {
				return;
			}
			if (cannotSellStolen(draggedItem, clickedSlot.getContainer())) {
				Log.logLocalized("stolenItemSellFail", LogType.CRIME, tradingPanel.getDisplayedCustomer().getName(), tradingPanel.getDisplayedMerchant().getName());
				return;
			}
			
			if (getDraggedFrom().getContainer().equals(tradingPanel.getDisplayedCustomer()) || getDraggedFrom().getContainer().equals(GameState.getPlayerCharacterGroup())) {
				sellItem(draggedItem);
			} 
		}
		
		super.swapOrCombineItems(clickedSlot, clickedSlotItem, draggedItem);
	}
	
	@Override
	protected void pickItemUp(InventoryItemButton clickedSlot,
			boolean wholeStack) {
		InventoryContainer clickedContainer = clickedSlot.getContainer();

		if (clickedContainer.equals(tradingPanel.getDisplayedMerchant())
				&& !canAfford(clickedSlot.getItem(), wholeStack)) {
			return;
		}
		
		super.pickItemUp(clickedSlot, wholeStack);
	}
	
	@Override
	protected int moveToInventory(InventoryItem item) {
		if (item.getInventory().getParentContainer()
				.equals(tradingPanel.getDisplayedMerchant())
				&& !canAfford(item, true)) {
			return 0;
		}
		boolean itemBelongsToMerchant = item.getInventory().getParentContainer()
				.equals(tradingPanel.getDisplayedMerchant());
		int amount = super.moveToInventory(item);
		if (itemBelongsToMerchant) {
			buyItem(item, amount);
		}
		return amount;
	}
	
	@Override
	protected int moveToJunk(InventoryItem item) {
		boolean itemBelongsToMerchant = item.getInventory().getParentContainer()
				.equals(tradingPanel.getDisplayedMerchant());
		if ((itemBelongsToMerchant && !canAfford(item, true))) {
			return 0;
		}
		int amount = super.moveToJunk(item);
		if (itemBelongsToMerchant) {
			buyItem(item, amount);
		}
		return amount;
	}
	
	private boolean canAfford(InventoryItem item, boolean wholeStack) {
		int cost = item.getTradingCost(
				tradingPanel.getDisplayedCustomer(),
				tradingPanel.getDisplayedMerchant(),
				true);
		if (wholeStack) {
			cost *= item.getStackSize();
		} else if (UIManager.getDraggedItem() != null) {
			cost += UIManager.getDraggedItem().getStackSize() * cost;
		}

		if (cost > GameState.getPlayerCharacterGroup().getGold()) {
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean putItemDown(InventoryItemButton clickedSlot,
			InventoryItem draggedItem, boolean wholeStack, boolean playersGroupItem) {
		
		InventoryContainer clickedContainer = clickedSlot.getContainer();
		InventoryContainer fromContainer = getDraggedFrom().getContainer();
		
		GameCharacter merchant = tradingPanel.getDisplayedMerchant();
		GameCharacter customer = tradingPanel.getDisplayedCustomer();
		
		boolean comesFromCustomer = fromContainer.equals(customer)
				|| fromContainer.equals(GameState.getPlayerCharacterGroup());
		boolean goesToCustomer = clickedContainer.equals(customer)
				|| clickedContainer.equals(GameState.getPlayerCharacterGroup());

		if (cannotSellStolen(draggedItem, clickedContainer)) {
			Log.logLocalized("stolenItemSellFail", LogType.CRIME, customer.getName(), merchant.getName());
			return false;
		}
		
		boolean wasPutDown = super.putItemDown(clickedSlot, draggedItem, wholeStack, playersGroupItem);
		
		if (wasPutDown) {
			if (comesFromCustomer && !goesToCustomer) {
				sellItem(draggedItem);
			} else if (!comesFromCustomer && goesToCustomer){
				buyItem(draggedItem);
			}
		} 
		return wasPutDown;
	}
	
	private boolean cannotSellStolen(InventoryItem item, InventoryContainer clickedContainer) {
		GameCharacter merchant = tradingPanel.getDisplayedMerchant();
		
		if (!isDraggedFromMerchant() && merchant != null
				&& merchant.equals(clickedContainer)
				&& merchant.isOwnerOf(item) && !item.getOwner().isEmpty()) {
			return true;
		}
		return false;
	}
	
	private void buyItem(InventoryItem item) {
		buyItem(item, item.getStackSize());
	}
	
	private void buyItem(InventoryItem item, int amount) {
		int goldAmount = item.getTradingCost(UIManager.getDisplayedCharacter(), UIManager.getDisplayedMerchant(), true)*amount;
		tradingPanel.getDisplayedMerchant().addGold(goldAmount);
		tradingPanel.getDisplayedCustomer().addGold(-goldAmount);
	}
	
	private void sellItem(InventoryItem item) {
		GameCharacter merchant = tradingPanel.getDisplayedMerchant();
		GameCharacter customer = tradingPanel.getDisplayedCustomer();
		if (!merchant.isOwnerOf(item) && !item.getOwner().isFixed()) {
			item.getOwner().clear();
		}
		int goldAmount = item.getTradingCost(customer, merchant, false)*item.getStackSize();
		merchant.addGold(-goldAmount);
		customer.addGold(goldAmount);
	}
	
	public boolean isDraggedFromMerchant() {
		if (getDraggedFrom() != null && tradingPanel.getDisplayedMerchant() != null) {
			return tradingPanel.getDisplayedMerchant().equals(getDraggedFrom().getContainer());
		}
		return false;
	}
}
