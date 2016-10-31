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
 * <br /><br />
 * Example:
 * 
 * <pre>
 * 	&lt;displayContainerInventory /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class DisplayContainerInventory extends Action {
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof InventoryContainer) {
			GameCharacter pc = GameState.getPlayerCharacterGroup().getGroupLeader(true);
			if (pc != null) {
				UIManager.displayContainerInventory(pc, (InventoryContainer)object);
			}
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

}
