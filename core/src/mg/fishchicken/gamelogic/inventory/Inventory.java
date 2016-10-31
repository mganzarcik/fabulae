package mg.fishchicken.gamelogic.inventory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.items.Armor;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemGroup;
import mg.fishchicken.gamelogic.inventory.items.ItemOwner;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entries;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * This represents an Inventory that contains three different bags - backpack,
 * quickslots and equipped slots.
 * 
 * All of these bags can contain different InventoryItems. The bags are
 * internally represented as Maps that map item slots (Integers) into
 * InventoryItems. Basically these are tables of items.
 * 
 * Items can be removed and added to the bags, while the Inventory takes care to
 * ensure that one item can only belong to one Inventory. This makes exchanging
 * InventoryItems easy, since the caller does not need to manage it at all.
 * 
 * Inventory implements Loadable.
 * 
 * @author Annun
 * 
 */
public abstract class Inventory implements AssetContainer, XMLSaveable  {

	public static final String STRING_TABLE = "inventory."+Strings.RESOURCE_FILE_EXTENSION;

	public static final String XML_INVENTORY = "inventory";
	public static final String XML_ITEM = "item";
	public static final String XML_ATTRIBUTE_GROUPS = "groups";
	public static final String XML_ATTRIBUTE_SLOT = "slot";
	public static final String XML_ATTRIBUTE_STACK_SIZE = "stackSize";
	public static final String XML_ATTRIBUTE_OWNER_CHARACTER = "ownerCharacter";
	public static final String XML_ATTRIBUTE_OWNER_FACTION= "ownerFaction";
	public static final String XML_ATTRIBUTE_OWNER_FIXED = "ownerFixed";
	public static final String XML_ATTRIBUTE_MIN_STACK_SIZE = "minStackSize";
	public static final String XML_ATTRIBUTE_MAX_STACK_SIZE = "maxStackSize";
	
	public static final ItemSlot[] HAND_SLOTS = {ItemSlot.LEFTHAND, ItemSlot.RIGHTHAND};
	
	public enum ItemSlot {
		HEAD(1), TORSO(2), LEGS(3), FEET(4), ARMS(5), 
		LEFTHAND(6),RIGHTHAND(7),LEFTRING(8),RIGHTRING(9),
		AMULET(10), BELT(11), CLOAK(12);

		private int slot;

		private ItemSlot(int slot) {
			this.slot = slot;
		}

		public int getSlot() {
			return slot;
		}

		public String getUIString() {
			return Strings.getString(AbstractGameCharacter.STRING_TABLE,toString());
		}
	}

	public enum BagType {
		QUICKUSE, BACKPACK, EQUIPPED, MERCHANT;
	}
	
	public static final ItemSlot[] armorSlots = { ItemSlot.ARMS, ItemSlot.BELT,
			ItemSlot.FEET, ItemSlot.HEAD, ItemSlot.LEFTHAND, ItemSlot.LEGS,
			ItemSlot.RIGHTHAND, ItemSlot.TORSO };

	private ObjectMap<BagType, IntMap<InventoryItem>> bags;
	private InventoryContainer parentContainer;

	public Inventory(InventoryContainer ic) {
		bags = new ObjectMap<BagType, IntMap<InventoryItem>>();
		for (BagType bag : BagType.values()) {
			bags.put(bag, new IntMap<InventoryItem>());
		}
		this.parentContainer = ic;
	}
	
	public InventoryContainer getParentContainer() {
		return parentContainer;
	}

	/**
	 * Returns the item with the specified id from this Inventory, or null if
	 * such item cannot be found.
	 * 
	 * The item is not actually removed from the intenvory when returned!
	 * 
	 * @param id
	 * @return
	 */
	public InventoryItem getItem(String id) {
		id = id.toLowerCase(Locale.ENGLISH);
		for (BagType bagType : BagType.values()) {
			IntMap<InventoryItem> bag = bags.get(bagType);
			for (InventoryItem item : bag.values()) {
				if (id.equals(item.getId())) {
					return item;
				}
			}
		}

		return null;
	}

	/**
	 * Returns an Array containing all Armor (including Shields)
	 * that is currently equipped.
	 * 
	 * Equipped clothing that is not an armor is 
	 * not included.
	 * 
	 * 
	 * @return
	 */
	public Array<Armor> getAllEquippedArmor() {
		Array<Armor> returnValue = new Array<Armor>();
		
		for (ItemSlot slot : armorSlots) {
			InventoryItem item = getEquipped(slot);
			if (item instanceof Armor) {
				returnValue.add((Armor)item);
			}
		}
		
		return returnValue;
	}
	
	/**
	 * Gets the currently equipped item from the given slot.
	 * 
	 * @return
	 */
	public InventoryItem getEquipped(ItemSlot slot) {
		return getEquipped(slot.getSlot());
	}
	
	/**
	 * Gets the currently equipped item from the given slot.
	 * 
	 * @return
	 */
	public InventoryItem getEquipped(int slot) {
		return bags.get(BagType.EQUIPPED).get(slot);
	}

	/**
	 * Gets the bag of the supplied type.
	 * 
	 * @param bagType
	 * @return
	 */
	public IntMap<InventoryItem> getBag(BagType bagType) {
		return bags.get(bagType);
	}
	
	/**
	 * Removes the supplied item from any inventory it currently belongs to.
	 * 
	 * @param item
	 * @return true if the item was actually removed from anywhere,
	 * 		false if not, becuause it did not belong anywhere
	 */
	protected static boolean removeFromPreviousInventry(InventoryItem item) {
		// remove from previous owner if we had one
		if (item.getInventory() != null) {
			return item.getInventory().removeFromBag(item.getInventoryBag(), item,
					true) != null;
		}
		return false;
	}

	/**
	 * Adds the supplied InventoryItem to the first free slot in the supplied
	 * bag.
	 * 
	 * @param bagType
	 * @param item
	 * @return
	 */
	public InventoryItem addToBag(BagType bagType, InventoryItem item) {
		return addToBag(bagType, item, null);
	}

	/**
	 * Adds the supplied InventoryItem to the supplied slot in the bag. If the
	 * slot is already occupied, and the item there is not stackable, the item
	 * there will be replaced by the new one and returned by this method.
	 * 
	 * Otherwise the new item is just added to the stack and null is returned.
	 * 
	 * If the supplied item already belonged to a different inventory, it will
	 * be removed from there.
	 * 
	 * @param bagType
	 * @param item
	 * @param slot
	 *            - if it is null, the first empty slot is used
	 * @return
	 */
	public InventoryItem addToBag(BagType bagType, InventoryItem item,
			Integer slot) {
		IntMap<InventoryItem> bag = bags.get(bagType);
		InventoryItem existingItem = null;

		// remove from previous owner if we had one
		removeFromPreviousInventry(item);
		
		if (slot == null) {
			slot = findAcceptableSlot(bag, item);
		}

		if (bag.containsKey(slot)) {
			existingItem = bag.get(slot);
		}
		if (existingItem != null && existingItem.isStackable(item)) {
			existingItem.addToStack(item);
			existingItem = null;
		} else {
			item.setSlot(slot);
			item.setInventory(this);
			item.setInventoryBag(bagType);
			bag.put(slot, item);
			if (existingItem != null) {
				existingItem.setInventory(null);
				onItemRemove(existingItem, bagType);
			}
		}
		onItemAdd(item, bagType);
		return existingItem;
	}

	private int findAcceptableSlot(IntMap<InventoryItem> bag, InventoryItem item) {
		int slot = 0;
		// first check for occupied slot that's stackable with the item
		for (Entry<InventoryItem> entry: bag.entries()) {
			if (entry.value.isStackable(item)) {
				return entry.key;
			}
		}
		// then search for an embty slot
		while (bag.containsKey(slot)) {
			++slot;
		}
		return slot;
	}

	/**
	 * Removes the supplied item from this inventory
	 * if it is in any of the bags.
	 * 
	 * Returns the removed item.
	 * 
	 * @param itemId
	 * @return
	 */
	public InventoryItem removeItem(String itemId) {
		InventoryItem item = getItem(itemId);
		if (item == null) {
			return null;
		}
		return removeFromBag(item.getInventoryBag(), item);
	}
	
	/**
	 * Removes all items from all bags of this inventory.
	 */
	public void clear() {
		for (ObjectMap.Entry<BagType, IntMap<InventoryItem>> bag : bags.entries()) {
			Iterator<Entry<InventoryItem>> iterator = bag.value.iterator();
			while (iterator.hasNext()) {
				InventoryItem item = iterator.next().value;
				item.setInventory(null);
				onItemRemove(item, bag.key);
				iterator.remove();
			}
		}
	}
	
	
	/**
	 * Copies all items from this inventory into the destination inventory
	 */
	public void copyAllItemsTo(Inventory destinationInventory) {
		for (BagType bag : BagType.values()) {
			IntMap<InventoryItem> sourceBag = getBag(bag);
			for (Entry<InventoryItem> slot : sourceBag.entries()) {
				InventoryItem item = slot.value;
				if (item != null) {
					InventoryItem newCopy = item.createNewInstance();
					if (item.getStackSize() > 1) {
						for (int i = 1; i < item.getStackSize(); ++i) {
							newCopy.addToStack(newCopy.createNewInstance());
						}
					}
					destinationInventory.addToBag(bag, newCopy, slot.key);
				}
			}
		}
	}
	
	
	/**
	 * Removes the item in the supplied slot from the bag and returns it. If the
	 * slot was empty, null is returned.
	 * 
	 * If the item was stackable, its stack is decreased and the item removed
	 * from the stack is returned.
	 * 
	 * @param bagType
	 * @param slot
	 * @return
	 */
	public InventoryItem removeFromBag(BagType bagType, InventoryItem item) {
		return removeFromBag(bagType, item.getSlot(), false);
	}

	/**
	 * Removes the item in the supplied slot from the bag and returns it. If the
	 * slot was empty, null is returned.
	 * 
	 * 
	 * @param bagType
	 * @param slot
	 * @param removeWholeStack
	 *            - if true, the whole stack is removed at once
	 * @return
	 */
	public InventoryItem removeFromBag(BagType bagType, InventoryItem item,
			boolean removeWholeStack) {
		return removeFromBag(bagType, item.getSlot(), removeWholeStack);
	}

	/**
	 * Removes the item in the supplied slot from the bag and returns it. If the
	 * slot was empty, null is returned.
	 * 
	 * If the item was stackable and removeWholeStack is false, its stack is
	 * decreased and the item removed from the stack is returned.
	 * 
	 * @param bagType
	 * @param slot
	 * @param removeWholeStack
	 *            - if true, the whole stack is removed at once
	 * @return
	 */
	public InventoryItem removeFromBag(BagType bagType, Integer slot,
			boolean removeWholeStack) {
		IntMap<InventoryItem> bag = bags.get(bagType);
		InventoryItem existingItem = bag.get(slot);
		if (existingItem == null) {
			return null;
		}
		if (existingItem.isInfinite() || (!removeWholeStack && existingItem.getStackSize() > 1)) {
			existingItem = existingItem.removeFromStack();
		} else {
			bag.remove(slot);
		}
		existingItem.setInventory(null);
		onItemRemove(existingItem, bagType);
		return existingItem;
	}

	/**
	 * Moves all items from this Inventory's bags into the destinationInventory's
	 * bags.
	 * 
	 * @param destinationInventory
	 */
	public void moveAllItems(Inventory destinationInventory) {
		for (BagType bag : BagType.values()) {
			moveAllItems(bag, bag, destinationInventory);
		}
	}
		
	
	/**
	 * Moves all items from this Inventory's fromBag into the destinationInventory's
	 * destinationBag.
	 * 
	 * @param fromBag
	 * @param destinationBag
	 * @param destinationInventory
	 */
	public void moveAllItems(BagType fromBag, BagType destinationBag, Inventory destinationInventory) {
		IntMap<InventoryItem> bag = bags.get(fromBag);
		Keys iterator = bag.keys();
		while (iterator.hasNext) {
			InventoryItem item = removeFromBag(fromBag, iterator.next(), true);
			if (item != null) {
				destinationInventory.addToBag(destinationBag, item);
			}
		}
	}
	
	/**
	 * Returns the total number of items in all bags in this Inventory.
	 * 
	 * @return
	 */
	public int getTotalNumberOfItems() {
		int returnValue = 0;
		for (IntMap<InventoryItem> bag : bags.values()) {
			returnValue += bag.size;
		}
		return returnValue;
	}
	
	/**
	 * Returns the total value of all items in the supplied bag
	 * for trading purposes.
	 * 
	 * @param bag
	 * @return
	 * @see InventoryItem#getTradingCost(GameCharacter, GameCharacter, boolean)
	 */
	public int getTotalTradingCost(BagType bag,  GameCharacter customer, GameCharacter trader,
			boolean buying) {
		int returnValue = 0;
		IntMap<InventoryItem> items = bags.get(bag);
		for (InventoryItem item : items.values()) {
			returnValue += item.getTradingCost(customer, trader, buying);
		}
		return returnValue;
	}
	
	/**
	 * Returns the object that should be supplied as the context
	 * for any item conditions evaluated on this inventory container.
	 * 
	 * This includes things like use and equip conditions.
	 * @return
	 */
	public abstract Object getConditionTarget();
	
	/**
	 * This is called after the player
	 * finished interacting with this Container and closed
	 * the Inventory screen.
	 * 
	 */
	public abstract void onInventoryClose();
	
	/**
	 * Returns whether this container can accept the supplied item. The result
	 * will contain a human-readable error message in case it cannot accept the
	 * whole stack. It also will always contain the max number of pieces of the
	 * item it can accept.
	 * 
	 * @param item
	 * @return
	 */
	public abstract InventoryCheckResult canAddItem(InventoryItem item);
	
	/**
	 * Adds a new item to this InventoryContainer's
	 * inventory. Which Bag will be used as the default
	 * is up to the implementation.
	 * 
	 * @param item
	 */
	public abstract void addItem(InventoryItem item);
	
	/**
	 * Callback method that is called
	 * whenever a new item is added to the specified
	 * bag of this container.
	 * 
	 * @param item
	 */
	protected abstract void onItemAdd(InventoryItem item, BagType  bagType);
	
	/**
	 * Callback method that is called
	 * whenever a new item is removed from the specified
	 * bag of this container.
	 * 
	 * @param item
	 */
	protected abstract void onItemRemove(InventoryItem item, BagType  bagType);
		
	/**
	 * Returns an array of all items in all of the bags in the inventory.
	 * 
	 * @return
	 */
	public Array<InventoryItem> getAllItems() {
		Array<InventoryItem> returnValue = new Array<InventoryItem>();
		for (IntMap<InventoryItem> bag : bags.values()) {
			for (InventoryItem item : bag.values()) {
				returnValue.add(item);
			}
		}
		return returnValue;
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		for (IntMap<InventoryItem> bag : bags.values()) {
			for (InventoryItem item : bag.values()) {
				item.gatherAssets(assetStore);
			}
		}
	}
	
	@Override
	public void clearAssetReferences() {
		for (IntMap<InventoryItem> bag : bags.values()) {
			for (InventoryItem item : bag.values()) {
				item.clearAssetReferences();
			}
		}
	}
	
	/**
	 * Writes the Inventory into XML using the supplied XmlWriter.
	 * 
	 * The output XML will look like this:
	 * 
	 * <pre>
	 * &lt;Inventory&gt;
	 * 	&lt;BagType&gt;
	 * 		&lt;Item id="itemId" slot="inventorySlot" /&gt;
	 * 		...
	 * 	&lt;/BagType&gt;
	 * &lt;/Inventory&gt;
	 * </pre>

	 */
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(Inventory.XML_INVENTORY);
		
		for (BagType bagType : BagType.values()) {
			IntMap<InventoryItem> bag = getBag(bagType);
			if (bag.size > 0) {
				writer.element(bagType.toString().toLowerCase(Locale.ENGLISH));
				Entries<InventoryItem> bagIterator = bag.entries();
				while (bagIterator.hasNext) {
					Entry<InventoryItem> entry =  bagIterator.next();
					writer.element(Inventory.XML_ITEM)
							.attribute(XMLUtil.XML_ATTRIBUTE_ID, entry.value.getId())
							.attribute(XML_ATTRIBUTE_SLOT, entry.key)
							.attribute(XML_ATTRIBUTE_STACK_SIZE,
									entry.value.getStackSize());
					ItemOwner itemOwner = entry.value.getOwner();
					String ownerId =itemOwner.getOwnerCharacterId();
					if (ownerId != null) {
						writer.attribute(XML_ATTRIBUTE_OWNER_CHARACTER, ownerId);
					}
					Faction faction = itemOwner.getOwnerFaction();
					if (faction != null) {
						writer.attribute(XML_ATTRIBUTE_OWNER_FACTION, faction);
					}
					writer.pop();
					
				}
				writer.pop();
			}
		}
		writer.pop();
	}


	/**
	 * Read the inventory from the suppled XML element.
	 * 
	 * The XML element should contain children in the following format:
	 * <pre>
	 * &lt;BagType&gt;
	 * 	&lt;Item id="itemId" slot="inventorySlot" /&gt;
	 * 	...
	 * &lt;/BagType&gt;
	 * </pre>
	 */
	@Override
	public void loadFromXML(Element root) throws IOException {
		Element inventoryElement = root.getChildByName(XML_INVENTORY);
		
		if (inventoryElement == null) {
			return;
		}
		
		for (int i = 0; i< inventoryElement.getChildCount(); ++i) {
			Element bagTypeElement = inventoryElement.getChild(i);
			BagType bagType = BagType.valueOf(bagTypeElement.getName().toUpperCase(Locale.ENGLISH));
			for (int j = 0; j< bagTypeElement.getChildCount(); ++j) {
				Element itemElement = bagTypeElement.getChild(j);
				String itemId = itemElement.getAttribute(XMLUtil.XML_ATTRIBUTE_ID, null);
				InventoryItem item = null;
				if (itemId != null) {
					item = GameState.getItem(itemId);
				} else {
					String groups = itemElement.getAttribute(XML_ATTRIBUTE_GROUPS, null);
					if (groups != null) {
						item = ItemGroup.getRandomItemFromGroups(groups);
					}
				}
				
				if (item == null) {
					continue;
				}
				String slotAttribute = itemElement.getAttribute(XML_ATTRIBUTE_SLOT, null);
				Integer slot = null;
				try {
					slot = slotAttribute == null ? null : Integer.parseInt(slotAttribute);
				} catch (NumberFormatException e) {
					slot = ItemSlot.valueOf(slotAttribute.toUpperCase(Locale.ENGLISH)).getSlot();
				}
				
				item.getOwner().set(
						itemElement.getAttribute(XML_ATTRIBUTE_OWNER_CHARACTER,
								null),
						Faction.getFaction(itemElement.getAttribute(
								XML_ATTRIBUTE_OWNER_FACTION, null)),
						itemElement.getBoolean(XML_ATTRIBUTE_OWNER_FIXED, false));
				
				addToBag(bagType, item, slot);
				
				Integer stackSize =  Integer.parseInt(itemElement.getAttribute(XML_ATTRIBUTE_STACK_SIZE, "1"));
				
				String maxStack = itemElement.getAttribute(XML_ATTRIBUTE_MAX_STACK_SIZE, null);
				String minStack = itemElement.getAttribute(XML_ATTRIBUTE_MIN_STACK_SIZE, null);
				
				if (maxStack != null && itemElement.getAttribute(XML_ATTRIBUTE_STACK_SIZE, null) == null) {
					int min = 0;
					if (minStack != null) {
						min = Integer.parseInt(minStack);
					}
					int max = Integer.parseInt(maxStack);
					
					stackSize = min + GameState.getRandomGenerator().nextInt(max+1-min);
				}
				if (stackSize < 0) {
					item.setInfinite(true);
				} else {
					for (int k = 1; k < stackSize; ++k) {
						InventoryItem newItem = item.createNewInstance();
						newItem.getOwner().set(item.getOwner());
						if (!item.isStackable(newItem)) {
							break;
						}
						item.addToStack(newItem);
					}
				}
			}
		}
	}
}
