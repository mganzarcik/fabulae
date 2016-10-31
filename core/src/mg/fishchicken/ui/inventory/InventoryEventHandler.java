package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamestate.crime.Theft;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class InventoryEventHandler implements EventListener {

	public static final String USE_BUTTON_NAME = "useButton";
	
	private GameState gameState;
	private GameCharacter displayedCharacter;
	protected InventoryItemButton draggedFrom;
	
	public InventoryEventHandler(GameState gameState) {
		this.gameState = gameState;
	}
	
	public void setDisplayedCharacter(GameCharacter displayedCharacter) {
		this.displayedCharacter = displayedCharacter;
	}
	
	@Override
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.touchDown.equals(inputEvent.getType())) {
				clicked(inputEvent, inputEvent.getTarget());
			}
		}
		return false;
	}
	
	public boolean clicked (InputEvent inputEvent, Actor actor) {
		if (actor instanceof Image) {
			actor = ((Image)actor).getParent();
		}
		
		if (actor instanceof InventoryItemButton) {
			InventoryItemButton clickedSlot = (InventoryItemButton)actor;
			InventoryItem slotItem = clickedSlot.getItem();
			InventoryItem draggedItem = UIManager.getDraggedItem();
			InventoryContainer clickedContainer = clickedSlot.getInventory().getParentContainer();
			
			boolean playersGroupItem = (draggedItem != null
					&& draggedItem.isGroupHeldItem()
					&& containerIsPlayerControlled(clickedContainer));
					
			if (draggedItem != null && (slotItem == null || playersGroupItem)) {
				putItemDown(clickedSlot, draggedItem, Buttons.RIGHT != inputEvent.getButton(), playersGroupItem);
				return true;
			} else if (slotItem != null && draggedItem == null) {
				if (KeyBindings.MOVE_TO_JUNK.isPressed() && clickedContainer != GameState.getPlayerCharacterGroup()) {
					moveToJunk(slotItem);
				} else if (KeyBindings.MOVE_TO_INVENTORY.isPressed() && clickedContainer != displayedCharacter) {
					moveToInventory(slotItem);
				} else {
					pickItemUp(clickedSlot, Buttons.RIGHT != inputEvent.getButton());
				}
				return true;
			} else if (slotItem != null && draggedItem != null) {
				if (Buttons.RIGHT == inputEvent.getButton() && draggedItem.isStackable(slotItem)) {
					pickItemUp(clickedSlot, false);
				} else {
					swapOrCombineItems(clickedSlot, slotItem, draggedItem);
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean containerIsPlayerControlled(InventoryContainer container) {
		return (container instanceof AbstractGameCharacter && ((AbstractGameCharacter) container)
				.belongsToPlayerFaction()) || container instanceof PlayerCharacterGroup;
	}
	
	protected boolean putItemDown(InventoryItemButton clickedSlot, InventoryItem draggedItem, boolean wholeStack, boolean playersGroupItem) {
		BagType clickedBag = clickedSlot.getBagType();
		
		if (wholeStack == false && draggedItem.getStackSize() == 1) {
			wholeStack = true;
		}
		
		// check if the item is being stolen
		InventoryContainer draggedToContainer = clickedSlot.getContainer();
		if (draggedToContainer != draggedFrom.getContainer()
				&& !draggedToContainer.isOwnerOf(draggedItem)
				&& containerIsPlayerControlled(draggedToContainer)) {
			if (gameState.getCrimeManager().registerNewCrime(new Theft(displayedCharacter, draggedItem))) {
				return true;
			}
		}
		
		int allowedCount = draggedItem.canBeAddedTo(clickedBag, clickedSlot
				.getSlot(), clickedSlot.getInventory().getParentContainer());
		
		// do nothing if the item cannot be added to the slot
		if (allowedCount < 1) {
			return false;
		}
		
		if (!wholeStack) {
			allowedCount = 1;
		}
		
		int stackSize= draggedItem.getStackSize();
		Inventory destinationInventory = playersGroupItem ? GameState
				.getPlayerCharacterGroup().getInventory() : clickedSlot
				.getInventory(); 
				
		for (int i = 0; i < allowedCount; ++i) {
			InventoryItem item = draggedItem.removeFromStack();
			if (playersGroupItem) {
				destinationInventory.addItem(item);
			} else {
				destinationInventory.addToBag(clickedBag, item, clickedSlot.getSlot());
			}
		}
		
		if (allowedCount == stackSize) {
			UIManager.setDraggedItem(null, null);
			draggedFrom = null;
		}
		
		return true;
	}
	
	protected void swapOrCombineItems(InventoryItemButton clickedSlot, InventoryItem clickedSlotItem, InventoryItem draggedItem) {
		boolean canPutDown = true;
		BagType clickedBag = clickedSlot.getBagType();
		
		if (BagType.EQUIPPED == clickedBag
				&& !clickedSlotItem.canBeUnequipped(
						clickedSlot.getSlot(),
						clickedSlot.getInventory().getParentContainer())) {
			return;
		}
		
		// do nothing if the item we want to swap is infinite, since these cannot ever be moved
		if(clickedSlotItem.isInfinite() && !clickedSlotItem.isStackable(draggedItem)) {
			return;
		}
		
		// do nothing if the item cannot be added to the given slot
		if (draggedItem.canBeAddedTo(clickedBag, clickedSlot.getSlot(),
						clickedSlot.getInventory().getParentContainer()) < 1) {
			canPutDown = false;
		}
		
		if (canPutDown) {
			UIManager.setDraggedItem(clickedSlot.getItem());
			// if the item was combined
			if (clickedSlot.getInventory().addToBag(clickedBag, draggedItem, clickedSlotItem.getSlot()) == null) {
				UIManager.setDraggedItem(null, null);
			}
		}
		draggedFrom = clickedSlot;
	}
	
	protected int moveToInventory(InventoryItem item) {
		Inventory groupBag = displayedCharacter.getInventory();
		int amount = item.canBeAddedTo(displayedCharacter);
		for (int i = 0; i < amount; ++i) {
			groupBag.addItem(item.removeFromStack());
		}
		return amount;
	}
	
	protected int moveToJunk(InventoryItem item) {
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		Inventory groupBag = pcg.getInventory();
		int amount = item.canBeAddedTo(pcg);
		for (int i = 0; i < amount; ++i) {
			groupBag.addItem(item.removeFromStack());
		}
		return amount;
	}
	
	protected void pickItemUp(InventoryItemButton clickedSlot, boolean wholeStack) {
		
		if (BagType.EQUIPPED == clickedSlot.getBagType()
				&& !clickedSlot.getItem().canBeUnequipped(
						clickedSlot.getSlot(),
						clickedSlot.getInventory().getParentContainer())) {
			return;
		}
		
		if (wholeStack == false && clickedSlot.getItem().getStackSize() == 1) {
			wholeStack = true;
		}
		InventoryItem clickedItem = clickedSlot.getInventory().removeFromBag(clickedSlot.getBagType(), clickedSlot.getSlot(), wholeStack);
		clickedItem.setSlot(clickedSlot.getSlot());
		clickedItem.setInventoryBag(clickedSlot.getBagType());
		if (UIManager.getDraggedItem() == null) {
			UIManager.setDraggedItem(clickedItem, clickedSlot);
		} else {
			UIManager.getDraggedItem().addToStack(clickedItem);
		}
		draggedFrom = clickedSlot;
	}
	
	public InventoryItemButton getDraggedFrom() {
		return draggedFrom;
	}
}
