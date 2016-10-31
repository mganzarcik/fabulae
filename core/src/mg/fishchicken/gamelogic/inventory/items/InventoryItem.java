package mg.fishchicken.gamelogic.inventory.items;

import groovy.lang.Binding;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.actions.Action;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.conditions.Condition.ConditionResult;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.InventoryCheckResult;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.graphics.models.CharacterModel;
import mg.fishchicken.graphics.models.ItemModel;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class InventoryItem implements AssetContainer, ModifierContainer, XMLLoadable, ThingWithId {
	
	public static final String STRING_TABLE = "items."+Strings.RESOURCE_FILE_EXTENSION;
	public static final String XML_EMITTED_LIGHT = "emittedLight";
	public static final String XML_ON_PICKUP = "onPickUp";
	public static final String XML_ON_EQUIP = "onEquip";
	public static final String XML_EQUIP_CONDITION = "equipCondition";
	public static final String XML_SLOTS = "slots";
	private static ObjectMap<String, String> items = new ObjectMap<String, String>();
	
	private String s_id;
	private String s_name;
	private String s_description;
	private String s_inventoryIconFile;
	private String s_mapIconFile;
	private String s_model;
	private int s_cost;
	private int s_weight;
	private int s_maxStackSize;
	private ItemOwner owner;
	private Condition s_onPickUpCondition, s_onEquipCondition;
	private Action s_onPickUpAction, s_onEquipAction;
	private Array<InventoryItem> stack;
	private int slot;
	private boolean infinite;
	private Inventory inventory;
	private BagType inventoryBag;
	private Array<Modifier> s_modifiers;
	private LightDescriptor s_emittedLight; 
	private Condition s_equipCondition;
	private ItemSlot[] s_allowedSlots;
		
	/**
	 * Returns a new instance of the InventoryItem with
	 * the supplied id. This item can be used for anything.
	 * 
	 * @param id
	 * @return
	 */
	public static InventoryItem getItem(String id) {
		return getItemPrototype(id).createNewInstance();
	}
	
	/**
	 * Returns the prototype (blueprint) inventory item that was loaded from XML
	 * files into the AssetManager. The same instance of the item is returned
	 * each time this method is called.
	 * 
	 * This item should NEVER be assigned to any game object or used in any way
	 * other than querying for it's parameters (like name or description), since
	 * that can break the game.
	 * 
	 * If you want to get an instance of the item that you can use for anything,
	 * call getItem(String).
	 * 
	 * @param id
	 * @return
	 */
	public static InventoryItem getItemPrototype(String id) {
		return Assets.get(items.get(id.toLowerCase(Locale.ENGLISH)), InventoryItem.class).createNewInstance();
	}
	
	/**
	 * Gathers all InventoryItems and registers them in the AssetManager
	 * so that they can be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherInventoryItems() throws IOException {
		Assets.gatherAssets(Configuration.getFolderItems(), "xml", InventoryItem.class, items);
	}
	
	public InventoryItem() {
		s_maxStackSize = 0; 
		s_weight = 0;
		stack = new Array<InventoryItem>();
		s_modifiers = new Array<Modifier>();
		s_allowedSlots = new ItemSlot[0];
		owner = new ItemOwner();
		setSlot(0);
	}
	
	public InventoryItem(FileHandle file) throws IOException {
		this();
		s_id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		loadFromXML(file);
	}
	
	public String getId() {
		return s_id;
	}
	
	public String getInventoryIconFile() {
		return s_inventoryIconFile;
	}
	
	public String getMapIconFile() {
		return s_mapIconFile;
	}

	/**
	 * Gets the final model that should be used to render this item for the given character.
	 * @param character
	 * @return
	 */
	public ItemModel getFinalItemModel(GameCharacter character) {
		CharacterModel characterModel = character.getModel();
		if (characterModel == null || s_model == null) {
			return null;
		}
		String modelId = characterModel.getItemModelIdPrefix() + s_model;
		ItemModel itemModel = ItemModel.getModel(modelId);
		if (itemModel == null) {
			itemModel = ItemModel.getModel(s_model);
		}
		return itemModel;
	}
	
	/**
	 * Returns the unlocalized name of this Inventory Item. It may
	 * be human readable, or in a stringTable#key format.
	 * 
	 * @return
	 */
	public String getRawName() {
		return s_name;
	}

	/**
	 * Returns a localized, user-readable name of this item.
	 */
	public String getName() {
		return Strings.getString(s_name);
	}

	/**
	 * Sets the localized, user-readable name of this item.
	 * 
	 * @param s_name
	 */
	public void setName(String s_name) {
		this.s_name = s_name;
	}

	/**
	 * Returns a localized, user-readable description of this item.
	 */
	public String getDescription() {
		return Strings.getString(s_description);
	}

	/**
	 * Sets a localized, user-readable description of this item.
	 */
	public void setDescription(String s_description) {
		this.s_description = s_description;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public BagType getInventoryBag() {
		return inventoryBag;
	}

	public void setInventoryBag(BagType inventoryBag) {
		this.inventoryBag = inventoryBag;
	}

	/**
	 * Returns an Array of ConditionResults if the supplied container can only use this item
	 * under certain conditions, or null if there are no conditions and the item can be used freely.
	 * 
	 * @param container
	 * @return
	 */
	public Array<ConditionResult> canBeUsedBy(InventoryContainer container) {
		return canBeAddedToQuickUse(container);
	}
	
	/**
	 * Returns an Array of ConditionResults if the supplied container can only have this item in the quick use slot
	 * under certain conditions, or null if there are no conditions and the item can be placed there freely.
	 * 
	 * @param container
	 * @return
	 */
	protected Array<ConditionResult> canBeAddedToQuickUse(InventoryContainer container) {
		Array<ConditionResult> result = new Array<ConditionResult>();
		result.add(new ConditionResult(Strings.getString(Condition.STRING_TABLE, "isUsable"), false));
		return result;
	}
	
	/**
	 * Evaluates the equip requirements for this item for the supplied inventory container
	 * and returns an array that will contain the result for each condition
	 * associated with the item.
	 * 
	 * @return null if there are no requirements, the array instead
	 */
	public Array<ConditionResult> evaluateEquipRequirements(GameCharacter container) {
		if (s_equipCondition != null) {
			Binding binding = new Binding();
			binding.setVariable("slot", slot);
			binding.setVariable("item", this);
			return s_equipCondition.evaluateWithDetails(container.getInventory().getConditionTarget(), binding);
		}
		return null;
	}
	
	
	public boolean isTwoHanded() {
		return false;
	}
	
	/**
	 * Returns how many times the supplied item can be added to the given container. 
	 * 0 means the item cannot be added at all.
	 * 
	 * @param slot
	 * @return
	 */
	public int canBeAddedTo(InventoryContainer container) {
		return canBeAddedTo(null, -1, container);
	}
	
	/**
	 * Returns how many times the supplied item can be added to the given slot
	 * in the given bag by the given container. 0 means the item cannot be added
	 * to the slot at all.
	 * 
	 * @param slot
	 * @return
	 */
	public int canBeAddedTo(BagType bag, int slot, InventoryContainer container) {
		if (BagType.EQUIPPED.equals(bag)) {
			if (!checkEquippedSlot(slot, container)) {
				return 0;
			}
			
			if (s_equipCondition != null) {
				Binding binding = new Binding();
				binding.setVariable("slot", slot);
				binding.setVariable("item", this);
				return s_equipCondition.execute(container.getInventory().getConditionTarget(), binding) ? getStackSize() : 0;
			}
		} else if (BagType.QUICKUSE.equals(bag)) {
			if (!Condition.areResultsOk(canBeAddedToQuickUse(container))) {
				return 0;
			}
		}
		
		InventoryCheckResult checkResult = container.getInventory().canAddItem(this);
		if (checkResult.getError() != null) {
			Log.log(checkResult.getError(), LogType.INVENTORY);
		}
		
		return checkResult.getAllowedStackSize();
	}

	/**
	 * By default, items can only be equipped in the allowed slots.
	 * 
	 * In case of hands, they can only be equipped if the hand is free (i.e. no
	 * two handed weapon in the other hand is blocking it and item is either not
	 * two handed, or it is two handed and both hands are empty).
	 * 
	 * @param slot
	 * @param container
	 * @return
	 */
	protected boolean checkEquippedSlot(int slot, InventoryContainer container) {
		for (ItemSlot allowedSlot : getAllowedSlots()) {
			if (allowedSlot.getSlot() == slot) {
				if (ItemSlot.RIGHTHAND.getSlot() == slot
								&& (container.getInventory().getEquipped(ItemSlot.LEFTHAND) == null || !container
										.getInventory().getEquipped(ItemSlot.LEFTHAND)
										.isTwoHanded())
				
						|| ItemSlot.LEFTHAND.getSlot() == slot
								&& (container.getInventory()
										.getEquipped(ItemSlot.RIGHTHAND) == null || !container
										.getInventory().getEquipped(ItemSlot.RIGHTHAND)
										.isTwoHanded())
						|| (ItemSlot.RIGHTHAND.getSlot() != slot && ItemSlot.LEFTHAND.getSlot() != slot)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public ItemSlot[] getAllowedSlots() {
		return s_allowedSlots;
	}
	
	/**
	 * Returns true if this item can be equipped to the given slot 
	 * by the given container.
	 * 
	 * @param slot
	 * @return
	 */
	public boolean canBeEquipped(ItemSlot slot, InventoryContainer container) {
		return canBeAddedTo(BagType.EQUIPPED, slot.getSlot(), container) > 0;
	}
	
	/**
	 * Returns true if this item can be unequipped from the given slot
	 * by the given container.
	 * 
	 * @param slot
	 * @param container
	 * @return
	 */
	public boolean canBeUnequipped(int slot, InventoryContainer container) {
		return true;
	}
	
	/**
	 * Returns true if this item can be unequipped from the given slot
	 * by the given container.
	 * 
	 * @param slot
	 * @param container
	 * @return
	 */
	public boolean canBeUnequipped(ItemSlot slot, InventoryContainer container) {
		return canBeUnequipped(slot.getSlot(), container);
	}
	
	/**
	 * Returns true if this item is stackable with the supplied item and the
	 * stack has sufficient capacity.
	 * 
	 * @param item
	 * @return
	 */
	public boolean isStackable(InventoryItem item) {
		return s_id.equals(item.getId())
				&& owner.equals(item.getOwner())
				&& (isInfinite() || s_maxStackSize > stack.size
						+ item.getStackSize());
	}
	
	/**
	 * Whether or not this item is infinite, i.e. there are
	 * infinite items on it's stack.
	 *  
	 * @return
	 */
	public boolean isInfinite() {
		return infinite;
	}
	
	/**
	 * Sets this item as infinite, which means its stack size is infinite.
	 * @return
	 */
	public void setInfinite(boolean value) {
		this.infinite = value;
	}
	
	/**
	 * Adds the supplied item to the stack of this item.
	 * 
	 * If the supplied item has a stack of its own, that stack is moved from the
	 * original item to this item as well.
	 * 
	 * @param newItem
	 */
	public void addToStack(InventoryItem newItem) {
		// for infinite items, we just do nothing
		if (isInfinite()) {
			return;
		}
		int stackSize = newItem.getStackSize();
		for (int i = 0; i < stackSize; ++i) {
			stack.add(newItem.removeFromStack());
		}
	}
	
	/**
	 * Removes the last item from the stack and returns it.
	 * 
	 * @return
	 */
	public InventoryItem removeFromStack() {
		if (isInfinite()) {
			return createNewInstance();
		}
		return stack.size > 0 ? stack.pop() : this;
	}
	
	/**
	 * Return's the size of this item's stack.
	 * 
	 * This item itself is considered parts of its own stack, therefore the size
	 * will always be at least 1.
	 * 
	 * @return
	 */
	public int getStackSize() {
		if (isInfinite()) {
			return 1;
		}
		return stack.size+1;
	}
	
	/**
	 * Whether or not this item is held by the group and not individual PCs.
	 * 
	 * Such items disappear as an item when added to a PCs inventory
	 * and the amount in the stack is added to the group's relevant counter.
	 * 
	 * Default implementation returns true if the item is gold, water, or ration.
	 * @return
	 */
	public boolean isGroupHeldItem() {
		return isGold() || isWater() || isFood();
	}
	
	public boolean isGold() {
		return false;
	}
	
	public boolean isWater() {
		return false;
	}
	
	public boolean isFood() {
		return false;
	}
	
	public void executePlayerPickUpAction(GameCharacter pickerUpper) {
		Binding params = new Binding();
		params.setVariable("item", this);
		params.setVariable(Condition.PARAM_INITIAL_OBJECT, this);
		if (s_onPickUpCondition != null) {
			if (!s_onEquipCondition.execute(pickerUpper, params)) {
				return;
			}
		}
		if (s_onPickUpAction != null) {
			s_onPickUpAction.execute(pickerUpper, params);
		}
	}
	
	public void executePlayerEquipAction(GameCharacter equipper) {
		Binding params = new Binding();
		params.setVariable("item", this);
		params.setVariable(Condition.PARAM_INITIAL_OBJECT, this);
		if (s_onEquipCondition != null) {
			if (!s_onEquipCondition.execute(equipper, params)) {
				return;
			}
		}
		if (s_onEquipAction != null) {
			s_onEquipAction.execute(equipper, params);
		}
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		assetStore.put(s_inventoryIconFile, Texture.class);
		assetStore.put(s_mapIconFile, Texture.class);
	}
	
	@Override
	public void clearAssetReferences() {
	}
	
	/**
	 * Get weight of the item in grams.
	 * @return
	 */
	public int getWeight() {
		return s_weight;
	}

	/**
	 * Set weight of the item in grams.
	 * @return
	 */
	public void setWeight(int s_weight) {
		this.s_weight = s_weight;
	}
	
	@Override
	public void onModifierChange() {	
	}
	
	public void addModifier(Modifier modifier) {
		s_modifiers.add(modifier);
	}
	
	public void removeModifier(Modifier modifier) {
		s_modifiers.removeValue(modifier, false);
	}
	
	@Override
	public Iterator<Modifier> getModifiers() {
		return s_modifiers.iterator();
	}
	
	public LightDescriptor getEmittedLight() {
		return s_emittedLight;
	}
	
	public ItemOwner getOwner() {
		return owner;
	}
	
	/**
	 * Gets the item's cost if it were to be traded between
	 * the two supplied characters, either by being sold 
	 * to the trader, or bought from him.
	 * 
	 * This method takes into account the persuasion
	 * skill of both parties and using that calculates 
	 * the total cost of the item.
	 *
	 * Note this returns the cost of the whole stack.
	 * 
	 * @param customer
	 * @param trader
	 * @param buying - whether the item is to be bought from the trader
	 * @return
	 */
	public int getTradingCost(GameCharacter customer, GameCharacter trader,
			boolean buying) {
		if (customer == null || trader == null) {
			return getCost();
		}
		
		float basePriceMultiplier = buying ?  1.25f : 0.25f;
		int disposition = trader.getFaction().getDispositionTowards(customer);
		if (disposition == 100) {
			basePriceMultiplier = buying ?  1f : 0.5f;
		} else if (disposition >= 75) {
			basePriceMultiplier = buying ?  1.1f : 0.4f;
		} else if (disposition >= 50) {
			basePriceMultiplier = buying ?  1.15f : 0.35f;
		} else if (disposition >= 25) {
			basePriceMultiplier = buying ?  1.2f : 0.3f;
		} else if (disposition >= 0) {
			basePriceMultiplier = buying ?  1.25f : 0.25f;
		} else if (disposition >= -25) {
			basePriceMultiplier = buying ?  1.35f : 0.15f;
		}
		
		float priceModifier = buying ? 
				basePriceMultiplier
					- 0.05f * customer.stats().skills().getSkillRank(Skill.PERSUASION) 
					+ 0.05f	* trader.stats().skills().getSkillRank(Skill.PERSUASION) 
				
					:
						
				basePriceMultiplier 
					+ 0.05f * customer.stats().skills().getSkillRank(Skill.PERSUASION) 
					- 0.05f * trader.stats().skills().getSkillRank(Skill.PERSUASION);
		if (priceModifier < 0.05f) {
			priceModifier = 0.05f;
		}
		return (int) Math.ceil(getCost() * priceModifier);
	}

	/**
	 * Gets the cost of this item. If the item is a stack, this will
	 * return the cost of the whole stack.
	 * @return
	 */
	public int getCost() {
		return s_cost * getStackSize();
	}

	/**
	 * This will set the cost of this item
	 * per one piece.
	 * 
	 */
	public void setCostPerItem(int cost) {
		s_cost = cost;
	}

	public void loadFromXML(FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		loadFromXML(root);
	}
	
	public void loadFromXML(Element root) throws IOException {
		Element properties = root.getChildByName(XMLUtil.XML_PROPERTIES);
		XMLUtil.readPrimitiveMembers(this, properties);
		XMLUtil.readPrimitiveMembers(owner, properties);
		if (s_inventoryIconFile != null) {
			s_inventoryIconFile = Configuration.addModulePath(s_inventoryIconFile);
		}
		if (s_mapIconFile != null) {
			s_mapIconFile = Configuration.addModulePath(s_mapIconFile);
		}
		XMLUtil.readModifiers(this, root.getChildByName(XMLUtil.XML_MODIFIERS));
		
		Element triggersElement = root.getChildByName(XMLUtil.XML_TRIGGERS);

		if (triggersElement != null) {
			Element trigger = triggersElement.getChildByName(XML_ON_EQUIP);
			if (trigger != null) {
				if (trigger.getChildByName(XMLUtil.XML_CONDITION) != null) {
					s_onEquipCondition = Condition.getCondition(trigger.getChildByName(XMLUtil.XML_CONDITION).getChild(0));
				}
				s_onEquipAction = Action.getAction(trigger.getChildByName(XMLUtil.XML_ACTION));
			}
			trigger = triggersElement.getChildByName(XML_ON_PICKUP);
			if (trigger != null) {
				if (trigger.getChildByName(XMLUtil.XML_CONDITION) != null) {
					s_onPickUpCondition = Condition.getCondition(trigger.getChildByName(XMLUtil.XML_CONDITION).getChild(0));
				}
				s_onPickUpAction = Action.getAction(trigger.getChildByName(XMLUtil.XML_ACTION));
			}
		}
		
		Element conditionElement = root.getChildByName(XML_EQUIP_CONDITION);
		if (conditionElement != null&& conditionElement.getChildCount() == 1) {
			s_equipCondition = Condition.getCondition(conditionElement.getChild(0));
		}
		if (properties != null) {
			String slots = properties.get(XML_SLOTS, null);
			if (slots != null) {
				String[] slotNames = slots.split(",");
				s_allowedSlots = new ItemSlot[slotNames.length];
				
				for (int i = 0; i < slotNames.length; ++i) {
					s_allowedSlots[i] = ItemSlot.valueOf(slotNames[i].trim().toUpperCase(Locale.ENGLISH));
				}
			}
		}
	}
	
	/**
	 * Creates a new copy of this InventoryItem as if it 
	 * was just loaded from the xml file that this item was loaded from.
	 * 
	 * Note that this is not a deep copy - only master data
	 * info about the InventoryItem will be copied - any transactional data,
	 * like stack size, stack contents, or containing Inventory will be discarded.
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public InventoryItem createNewInstance() {
		try {
			InventoryItem copy =this.getClass().getConstructor().newInstance();
			try {
				Class<?> objectClass = this.getClass();
				while (objectClass != null && objectClass != Object.class) {
					Field[] fields = objectClass.getDeclaredFields();

					for (Field field : fields) {
						if (!field.getName().startsWith("s_")) {
							continue;
						}
						field.setAccessible(true);
						Object fieldValue = field.get(this);
						if (fieldValue instanceof Array<?>) {
							Array newArray = new Array();
							for (Object arrayItem : (Array)fieldValue) {
								if (arrayItem instanceof Modifier) {
									newArray.add(((Modifier) arrayItem).copy());
								} else {
									newArray.add(arrayItem);
								}
							}
							fieldValue = newArray;
						}
						field.set(copy, fieldValue);
						field.setAccessible(false);
					}
					objectClass = objectClass.getSuperclass();
				}
			} catch (IllegalAccessException e) {
				throw new GdxRuntimeException(e);
			}
			return copy;
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		} 
	}

}