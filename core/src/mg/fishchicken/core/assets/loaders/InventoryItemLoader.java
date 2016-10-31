package mg.fishchicken.core.assets.loaders;

import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemGroup;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class InventoryItemLoader extends SimpleAsynchronousLoader<InventoryItem, InventoryItemLoader.InventoryItemParameter> {
	
	public InventoryItemLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public InventoryItemLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@SuppressWarnings("unchecked")
	@Override
	public InventoryItem load (AssetManager assetManager, String fileName, FileHandle itemFile, InventoryItemParameter parameter) {
		try {
			XmlReader xmlReader = new XmlReader();
			Element root = xmlReader.parse(itemFile);
			InventoryItem newItem = null;
			
			Element properties = root.getChildByName(XMLUtil.XML_PROPERTIES);
			if (properties != null) {
				
				String groupNames = properties.getAttribute(Inventory.XML_ATTRIBUTE_GROUPS, "");
				
				String[] groups = groupNames.split(",");
				for (String group : groups) {
					group = group.trim();
					ItemGroup existingGroup = ItemGroup.get(group);
					if (existingGroup != null) {
						existingGroup.add(itemFile.nameWithoutExtension());
					} else {
						ItemGroup.add(group, new ItemGroup(group, itemFile.nameWithoutExtension()));
					}
				}
			}
			
			String implementationClassName = root.getName();
			implementationClassName = InventoryItem.class.getPackage().getName()+"."+StringUtil.capitalizeFirstLetter(implementationClassName);
		
			Class<? extends InventoryItem> itemClass = (Class<? extends InventoryItem>) Class.forName(implementationClassName);
			newItem =  itemClass.getConstructor(FileHandle.class).newInstance(resolve(fileName));
			return newItem;
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		} 
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, InventoryItemParameter parameter) {
		return null;
	}

	static public class InventoryItemParameter extends AssetLoaderParameters<InventoryItem> {
	}

}
