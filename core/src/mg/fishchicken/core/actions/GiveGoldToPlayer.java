package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Gives the specified amount of gold to the player.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;GiveGoldToPlayer amount="50" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class GiveGoldToPlayer extends Action {

	public static final String XML_AMOUNT = "amount";
	
	@Override
	protected void run(Object object, Binding parameters) {
		int amount  = Integer.parseInt(getParameter(XML_AMOUNT));
		GameState.getPlayerCharacterGroup().addGold(amount);
		Log.logLocalized(mg.fishchicken.gamelogic.actions.Action.STRING_TABLE, "goldReceived", LogType.INVENTORY, amount);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_AMOUNT, null) == null) {
			throw new GdxRuntimeException(XML_AMOUNT+" must be set for action GiveGoldToPlayer in element: \n\n"+conditionElement);
		}
	}

}
