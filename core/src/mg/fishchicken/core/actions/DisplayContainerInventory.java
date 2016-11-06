package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Displays the inventory of the supplied InventoryContainer and of the leader of 
 * the player character group so that items can be moved back and forth.
 * 
 * If the optional id is supplied, the inventory of a InventoryContainer with this name
 * will be displayed.
 * 
 * <br /><br />
 * Example:
 * 
 * <pre>
 * 	&lt;displayContainerInventory id="containerId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class DisplayContainerInventory extends Action {
	
	public static final String XML_CONTAINER_ID = "id";
	
	@Override
	protected void run(Object object, Binding parameters) {
		InventoryContainer ic = null;
		String id = getParameter(XML_CONTAINER_ID);
		
		if (id != null) {
			ic = (InventoryContainer) GameState.getGameObjectById(id, InventoryContainer.class);
		} else if (object instanceof InventoryContainer) {
			ic = (InventoryContainer)object;
		} 
		
		GameCharacter pc = GameState.getPlayerCharacterGroup().getGroupLeader(true);
		if (ic != null && pc != null) {
			UIManager.displayContainerInventory(pc, ic);
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

}
