package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied NonPlayerCharacter
 * can trade with the player.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;canTradeWithPC targetObject = "__npcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class CanTradeWithPC extends Condition {

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			return ((GameCharacter)object).canTradeWith(Faction.PLAYER_FACTION);
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

	@Override
	protected String getStringTableNameKey() {
		return "canTradeWith";
	}

}
