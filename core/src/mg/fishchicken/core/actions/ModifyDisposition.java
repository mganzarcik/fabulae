package mg.fishchicken.core.actions;

import mg.fishchicken.gamelogic.factions.Faction;
import groovy.lang.Binding;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Modifies the disposition of the supplied Faction towards 
 * the Player by the supplied Value.
 * <br><br>
* Example:
 * 
 * <pre>
 * 	&lt;modifyDisposition faction="factionId" value="value" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class ModifyDisposition extends Action {

	public static final String XML_FACTION = "faction";
	public static final String XML_VALUE = "value";

	@Override
	protected void run(Object object, Binding parameters) {
		Faction.getFaction(getParameter(XML_FACTION)).modifyDispositionTowardsPlayer(
				Integer.valueOf(getParameter(XML_VALUE)));
	}

	@Override
	public void validateAndLoadFromXML(Element actionElement) {
		if (actionElement.get(XML_FACTION, null) == null) {
			throw new GdxRuntimeException(XML_FACTION+" must be set for action ModifyDisposition in element: \n\n"+actionElement);
		}
		String value = actionElement.get(XML_VALUE, null);
		if (value == null) {
			throw new GdxRuntimeException(XML_VALUE+" must be set for action ModifyDisposition in element: \n\n"+actionElement);
		}
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e){
			throw new GdxRuntimeException(XML_VALUE+" must be an Integer for action ModifyDisposition in element: \n\n"+actionElement);
		}
	}

}
