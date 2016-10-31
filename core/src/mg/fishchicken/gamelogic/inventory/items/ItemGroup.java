package mg.fishchicken.gamelogic.inventory.items;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ItemGroup extends Array<String> {

	public static final String XML_ITEMS = "items";
	
	public enum BasicGroup {
		Common, Uncommon, Rare, Unique, Armor, Weapon, Potion, Wand, Simple
	}
	
	private static ObjectMap<String, ItemGroup> itemGroups = new ObjectMap<String, ItemGroup>();

	/**
	 * Gets the ItemGroup identified by the given name.
	 * 
	 * @param groupName
	 * @return
	 */
	public static ItemGroup get(String groupName) {
		return itemGroups.get(groupName.toLowerCase(Locale.ENGLISH));
	}
	
	/**
	 * Adds the supplied ItemGroup into the list of global item groups
	 * under the supplied name.
	 * 
	 * @param groupName
	 * @param newGroup
	 */
	public static void add(String groupName, ItemGroup newGroup) {
		itemGroups.put(groupName, newGroup);
	}
	
	/**
	 * This will load all item groups into the system.
	 * 
	 * ItemGroups can either be defined in their own files, or they
	 * can be defined on the items themselves.
	 * 
	 * @throws IOException
	 */
	public static void loadItemGroups() throws IOException {
		FileHandle groupsFolder = Gdx.files.internal(Configuration.getFolderItemGroups());
		if (!groupsFolder.isDirectory()) {
			groupsFolder = Gdx.files.internal("bin/classes/"+Configuration.getFolderItemGroups());
		}
		FileHandle[] files = groupsFolder.list(".xml");
		for (FileHandle groupfile : files) { 
			new ItemGroup(groupfile.nameWithoutExtension());
		}
	}
	
	/**
	 * Returns a random item that belongs to all of the groups
	 * that are specified in the supplied comma-delimited String.
	 *  
	 * @param groupNames
	 * @return
	 */
	public static InventoryItem getRandomItemFromGroups(String groupNames) {
		String[] names = groupNames.split(",");
		Array<String> intersection = null;
		for (String name : names) {
			ItemGroup group = itemGroups.get(name.trim());
			if (group != null) {
				if (intersection == null) {
					intersection = group;
				} else {
					intersection = group.intersect(intersection);
				}
			}
		}
		
		InventoryItem returnValue = null;
		
		if (intersection != null && intersection.size > 0) {
			Random random = GameState.getRandomGenerator();
			int index = random.nextInt(intersection.size);
			returnValue = GameState.getItem(intersection.get(index));
		}
		
		return returnValue;
	}
	
	
	private String name;
	
	public ItemGroup(String groupName) throws IOException {
		itemGroups.put(groupName.toLowerCase(Locale.ENGLISH), this);
		name = groupName;
		loadFromXML(Gdx.files.internal(Configuration.getFolderItemGroups()+groupName+".xml"));
	}

	public ItemGroup(String groupName, String itemId) {
		itemGroups.put(groupName.toLowerCase(Locale.ENGLISH), this);
		name = groupName;
		add(itemId);
	}
	
	public void loadFromXML(FileHandle groupFile) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(groupFile);
		XMLUtil.readPrimitiveMembers(this, root.getChildByName(XMLUtil.XML_PROPERTIES));
		Element itemsElement = root.getChildByName(XML_ITEMS);
		if (itemsElement != null) {
			String[] items = itemsElement.getText().split(",");
			for (String item : items) {
				add(item.trim());
			}
		}
	}
	
	public String getName() {
		return name; 
	}
	
	/**
	 * Returns the result of an intersection of this ItemGroup
	 * with the supplied Array of item ids.
	 * 
	 * @param group
	 * @return
	 */
	public Array<String> intersect(Array<String> group) {
		Array<String> returnValue = new Array<String>();
		for (String itemId : this) {
			if (group.contains(itemId, false)) {
				returnValue.add(itemId);
			}
		}
		return returnValue;
	}
	
	@Override
	public boolean equals(Object object) {
		if (super.equals(object)) {
			return CoreUtil.equals(name, ((ItemGroup)object).name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
