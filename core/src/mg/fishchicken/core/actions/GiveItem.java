package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Transfers the specified item between the characters engaged in dialogue.
 * 
 * If the supplied object is the NPC, it will be transfered to the PC, and vice versa.
 * <br /><br />
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
		
		if (object.equals(pc)) {
			targetCharacter = npc;
			sourceCharacter = pc;
		} else {
			targetCharacter = pc;
			sourceCharacter = npc;
		}
		
		InventoryItem item = sourceCharacter.getInventory().getItem(getParameter(XML_ITEM));
		if (item != null) {
			targetCharacter.getInventory().addItem(item);
			Log.logLocalized("itemReceived", LogType.INVENTORY, item.getName(), targetCharacter.getName());
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_ITEM, null) == null) {
			throw new GdxRuntimeException(XML_ITEM+" must be set for action GiveItem in element: \n\n"+conditionElement);
		}
	}

}
