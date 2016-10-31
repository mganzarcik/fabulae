package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Opens the trading panel between the PC and NPC at dialogue if possible.
 * <br /><br />
 * If the NPC currently cannot trade with the PC, a user-friend error message will be 
 * displayed instead.
 * <br /><br />
 * Can only be used in dialogues.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;startTrading /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class StartTrading extends Action {

	@Override
	protected void run(Object object, Binding parameters) {
		GameCharacter pc = (GameCharacter) parameters.getVariable(PARAM_PC_AT_DIALOGUE);
		GameCharacter npc = (GameCharacter) parameters.getVariable(PARAM_NPC_AT_DIALOGUE);
		
		if (npc.canTradeWith(Faction.PLAYER_FACTION)) {
			UIManager.displayTradingInventory(pc, npc);
		} else {
			UIManager.displayMessage(npc.getName(), Strings.getString(Faction.STRING_TABLE,"CannotTrade"));
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		
	}

}
