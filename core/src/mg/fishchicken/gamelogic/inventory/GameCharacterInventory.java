package mg.fishchicken.gamelogic.inventory;

import java.io.IOException;
import java.util.Iterator;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.characters.Stats;
import mg.fishchicken.graphics.models.ItemModel;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class GameCharacterInventory extends Inventory {

	private InventoryCheckResult checkResult = new InventoryCheckResult();
	private GameCharacter character;
	
	public GameCharacterInventory(GameCharacter character) {
		super(character);
		this.character = character;
	}
	
	@Override
	public Object getConditionTarget() {
		return character;
	}
	/**
	 * Stores the supplied item in the character's backpack,
	 * using the first free slot.
	 * 
	 * @param item
	 */
	public void addItem(InventoryItem item) {
		if (character.belongsToPlayerFaction() && item.isGroupHeldItem()) {
			GameState.getPlayerCharacterGroup().getInventory().addItem(item);
			return;
		}
		addToBag(BagType.BACKPACK, item);
	}
	
	@Override
	public void onItemAdd(InventoryItem item, BagType bagType) {
		if (character.belongsToPlayerFaction() && !GameState.isLoadingGame()) {
			item.executePlayerPickUpAction(character);
		}
		if (BagType.EQUIPPED.equals(bagType)){
			// only add modifiers if we are not loading a savegame
			// otherwise they will be loaded separately
			Stats stats = character.stats();
			if (!GameState.isLoadingGame()) {
				stats.modifyLoad(item.getWeight());
				Iterator<Modifier> modifiers = item.getModifiers();
				while (modifiers.hasNext()) {
					stats.addModifier(modifiers.next());
				}
				item.executePlayerEquipAction(character);
			}
			character.addLight(item.getEmittedLight());
			
			ItemModel itemModel = item.getFinalItemModel(character);
			if (itemModel != null) {
				character.addItemModel(itemModel, item.getSlot());
			}
		}
	}

	@Override
	public void onItemRemove(InventoryItem item, BagType bagType) {
		if (BagType.EQUIPPED.equals(bagType)){
			Stats stats = character.stats();
			stats.modifyLoad(-item.getWeight());
			Iterator<Modifier> modifiers = item.getModifiers();
			while (modifiers.hasNext()) {
				stats.removeModifier(modifiers.next());
			}
			character.removeLight(item.getEmittedLight());
			character.removeItemModel(item.getSlot());
		}
	}
	
	@Override
	public InventoryCheckResult canAddItem(InventoryItem item) {
		if (character.belongsToPlayerFaction() && item.isGroupHeldItem()) {
			return GameState.getPlayerCharacterGroup().getInventory().canAddItem(item);
		}
		return checkResult.setAllowedStackSize(item.getStackSize()).setError(null);
	}
	
	@Override
	public void onInventoryClose() {
	}
	
	/**
	 * Causes this character to drop all equipped weapons
	 * (not shields) to the ground.
	 * 
	 */
	public void dropEquippedWeapons() {
		InventoryItem rightHand = getEquipped(ItemSlot.RIGHTHAND);
		InventoryItem leftHand = getEquipped(ItemSlot.LEFTHAND);
		if (rightHand instanceof Weapon) {
			dropItemToTheGround(rightHand);
		}
		if (leftHand instanceof Weapon) {
			dropItemToTheGround(leftHand);
		}
	}
	
	private void dropItemToTheGround(InventoryItem item) {
		removeFromBag(item.getInventoryBag(), item, true);
		Position position = character.position();
		new PickableGameObject(item, position.getX(), position.getY(), character.getMap());
		Log.logLocalized("itemDropped", LogType.INVENTORY, item.getName(), character.getName());
	}
	
	public void dropEverythingToTheGround() {
		if (getTotalNumberOfItems() > 0) {
			try {
				ItemPile newPile = new ItemPile(character.getInternalId()+"Pile", character.position());
				moveAllItems(BagType.EQUIPPED, BagType.BACKPACK, newPile.getInventory());
				moveAllItems(BagType.QUICKUSE, BagType.BACKPACK, newPile.getInventory());
				moveAllItems(BagType.BACKPACK, BagType.BACKPACK, newPile.getInventory());
				newPile.setMap(character.getMap());
			} catch (IOException e) {
				throw new GdxRuntimeException(e);
			}
		}
	}
	
}
