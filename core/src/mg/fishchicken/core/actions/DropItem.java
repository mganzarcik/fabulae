package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.PickableGameObject;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamestate.Position;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Drops the item with the supplied ID to the ground. If the item
 * is currently in the character's inventory, it will be dropped from there.
 * If the character does not posses this item, a new one will be created and dropped.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;dropItem id="skeletonKey" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class DropItem extends Action {
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (!(object instanceof PositionedThing)) {
			throw new GdxRuntimeException("DropItem can only be called on positioned things!");
		}
		String itemId = getParameter(XMLUtil.XML_ATTRIBUTE_ID);
		InventoryItem item = null;
		if (object instanceof GameCharacter) {
			Inventory inventory = ((GameCharacter)object).getInventory();
			item = inventory.removeItem(itemId);
		}
		
		if (item == null) {
			item = InventoryItem.getItem(itemId);
		}
		Position position = ((PositionedThing)object).position();
		
		new PickableGameObject(item, position.getX(), position.getY(),
				gameState.getCurrentMap());
	}

	@Override
	public void validateAndLoadFromXML(Element actionElement) {
		if (actionElement.get(XMLUtil.XML_ATTRIBUTE_ID, null) == null) {
			throw new GdxRuntimeException(XMLUtil.XML_ATTRIBUTE_ID+" must be set for action DropItem in element: \n\n"+actionElement);
		}
	}

}
