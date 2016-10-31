package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.Shield;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied character has a shield 
 * equipped in any of his hands.
 * 
 * @author ANNUN
 *
 */
public class HasShieldEquipped extends Condition {

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) object;
			
			InventoryItem leftHand = character.getInventory().getEquipped(ItemSlot.LEFTHAND);
			InventoryItem rightHand = character.getInventory().getEquipped(ItemSlot.RIGHTHAND);

			if (!(leftHand instanceof Shield) && !(rightHand instanceof Shield)) {
				return false;
			} 
			
			return true;
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "shieldEquipped";
	}
}
