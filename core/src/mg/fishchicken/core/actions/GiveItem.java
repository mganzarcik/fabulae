package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Transfers the specified item between the characters engaged in dialogue.
 * 
 * If the supplied object is the NPC, it will be transfered to the PC, and vice versa.
 * <br /><br />
 * 
 * If there is no source character, of the character does not possess the item
 * a new item instance will be created on the fly. It will be assumed
 * the item's assets are already loaded.
 * Example:
 * 
 * <pre>
 * &lt;GiveItem item="secretKey" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class GiveItem extends Action {

	public static final String XML_ITEM = "item";
	
	@Override
	protected void run(Object object, Binding parameters) {
		GameCharacter targetCharacter, sourceCharacter;
		GameCharacter npc = (GameCharacter) parameters.getVariable(PARAM_NPC_AT_DIALOGUE);
		GameCharacter pc = (GameCharacter) parameters.getVariable(PARAM_PC_AT_DIALOGUE);
		
		if (CoreUtil.equals(object, pc)) {
			targetCharacter = npc;
			sourceCharacter = pc;
		} else {
			targetCharacter = pc;
			sourceCharacter = npc;
		}
		
		String itemId = getParameter(XML_ITEM);
		InventoryItem item = sourceCharacter != null ? sourceCharacter.getInventory().getItem(itemId) : null;
		if (item == null) {
			item = InventoryItem.getItem(itemId);
		}
		
		targetCharacter.getInventory().addItem(item);
		Log.logLocalized("itemReceived", LogType.INVENTORY, item.getName(), targetCharacter.getName());
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_ITEM, null) == null) {
			throw new GdxRuntimeException(XML_ITEM+" must be set for action GiveItem in element: \n\n"+conditionElement);
		}
	}

}
