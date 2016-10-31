package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied Character or CharacterGroup
 * has the supplied Item.
 * <br /><br />
 * Examples:
 * 
 * <pre>
 * &lt;hasItem targetObject="__npcAtDialogue" item="itemId" /&gt;
 * &lt;hasItem targetObject="__player" item="itemId" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class HasItem extends Condition {

	public static final String XML_ITEM_NAME = "item";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		String itemId = getParameter(XML_ITEM_NAME);
		if (object instanceof GameCharacter) {
			return ((GameCharacter) object).getInventory().getItem(itemId) != null;
		} else if (object instanceof CharacterGroup) {
			for (GameCharacter character : ((CharacterGroup)object).getMembers()) {
				if (character.getInventory().getItem(itemId) != null) {
					return true;
				}
			}
			
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_ITEM_NAME, null) == null) {
			throw new GdxRuntimeException(XML_ITEM_NAME+" must be set for condition HasItem in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "hasItem";
	}

	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{InventoryItem.getItemPrototype(getParameter(XML_ITEM_NAME)).getName()};
	}
}
