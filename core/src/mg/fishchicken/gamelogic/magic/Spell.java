package mg.fishchicken.gamelogic.magic;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Spell extends Perk {

	public static final String STRING_TABLE = "spells."+Strings.RESOURCE_FILE_EXTENSION;
	public static final String XML_FOCI = "foci";
	public static final String XML_ATTRIBUTE_CONSUMED = "consumed";
	
	private static ObjectMap<String, String> spells = new ObjectMap<String, String>();

	public static Spell getSpell(String id) {
		return Assets.get(spells.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns true if a spell with the specified id exists, or will exist
	 * once global assets are loaded.
	 * 
	 * @param id
	 * @return
	 */
	public static boolean spellExists(String id) {
		return Assets.assetExists(Configuration.getFolderSpells(), id, "xml");
	}
	
	/**
	 * Returns an Array of all loaded spells.
	 * 
	 * @return
	 */
	public static Array<Spell> getAllSpells() {
		Array<Spell> returnValue = new Array<Spell>();
		for (String id: spells.keys()) {
			returnValue.add(getSpell(id));
		}
		return returnValue;
	}

	/**
	 * Gathers all Spells and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherSpells() throws IOException {
		Assets.gatherAssets(Configuration.getFolderSpells(), "xml", Spell.class, spells);
	}
	
	/**
	 * Gathers all Spells and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherSpellImages() throws IOException {
		AssetMap assetStore = new AssetMap(); 
		for (String id: spells.keys()) {
			getSpell(id).gatherAssets(assetStore);
		}
		for(Entry<String, Class<?>> entry : assetStore) {
			Assets.getAssetManager().load(entry.key, entry.value);
		}
	}
	
	private ObjectMap<String, Boolean> foci;
	private boolean s_canBeCast; // default is true
	
	public Spell(FileHandle file) throws IOException {
		super(file);
	}
	
	/**
	 * Always returns true - spells are always activated.
	 * 
	 * @return
	 */
	public boolean isActivated() {
		return true;
	}
	
	/**
	 * Spells are never considered attacks (they can be hostile,
	 * but they never use weapon "to hit" calculations).
	 */
	@Override
	public boolean isAttack() {
		return false;
	}
	
	/**
	 * Returns a map containing all the items
	 * that the spell needs in order to be cast successfully
	 * as keys and whether or not they will be consumed by 
	 * casting as values.
	 *  
	 * @return
	 */
	public ObjectMap<String, Boolean> getFoci() {
		return foci;
	}
	
	@Override
	public boolean canBeActivated(GameCharacter character) {
		if (!super.canBeActivated(character)) {
			return false;
		}
		
		Inventory inventory = character.getInventory();
		
		for (String focusId : foci.keys()) {
			if (inventory.getItem(focusId) == null) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Whether or not this spell can be cast at all.
	 * 
	 * Some special spells can never be cast by the player directly
	 * and are only invoked in special circumstances (such as via script
	 * or in dialogue).
	 * 
	 * Please note that this does not affect the result of
	 * {@link #canBeActivated(GameCharacter)} - if 
	 * this returns false, canBeActivate may still return true,
	 * since that one only evaluates direct activation requirements
	 * for the spell.
	 * @return
	 */
	public boolean canBeCast() {
		return s_canBeCast;
	}
	
	@Override
	public void executeEffects(AbstractGameCharacter user, TargetType target) {
		super.executeEffects(user, target);
		if (user instanceof InventoryContainer) {
			Inventory inventory = ((InventoryContainer) user).getInventory();
			for (Entry<String, Boolean> focusEntry : foci)  {
				if (focusEntry.value) {
					InventoryItem item = inventory.removeItem(focusEntry.key);
					if (item != null) {
						Log.logLocalized("itemDestroyed", LogType.INVENTORY, item.getName());
					}
				}
			}
		}
	}
	
	@Override
	public void loadFromXML(Element root) {
		s_canBeCast = true;
		super.loadFromXML(root);
		foci = new ObjectMap<String, Boolean>();
		Element fociElement = root.getChildByName(XML_FOCI);
		if (fociElement != null) {
			for (int i = 0; i < fociElement.getChildCount(); ++i) {
				Element focusElement = fociElement.getChild(i);
				String itemID = focusElement.getAttribute(XMLUtil.XML_ATTRIBUTE_ID);
				boolean consumed = focusElement.getBooleanAttribute(XML_ATTRIBUTE_CONSUMED, false);
				foci.put(itemID, consumed);
			}
		}
	}

}

