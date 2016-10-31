package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied character has the supplied
 * item equipped.
 *  
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;hHasItemEquipped item="item_id" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class HasItemEquipped extends Condition {

public static final String XML_ITEM_NAME = "item";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		String itemId = getParameter(XML_ITEM_NAME);
		InventoryItem item = null;
		if (object instanceof GameCharacter) {
			item = ((GameCharacter) object).getInventory().getItem(itemId);
		} else if (object instanceof CharacterGroup) {
			for (GameCharacter character : ((CharacterGroup)object).getMembers()) {
				item = character.getInventory().getItem(itemId);
			}
			
		}
		if (item != null && item.getInventoryBag() == BagType.EQUIPPED) {
			return true;
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_ITEM_NAME, null) == null) {
			throw new GdxRuntimeException(XML_ITEM_NAME+" must be set for condition HasItemEquipped in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "hasItemEquipped";
	}

	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{InventoryItem.getItemPrototype(getParameter(XML_ITEM_NAME)).getName()};
	}
}
