package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.locations.GameLocation;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied GameLocation
 * has already been visited by the Player.
 * 
 * If locationId is not supplied, it is assumed
 * the target object itself is a location.
 * <p>
 * Example:
 * 
 * <pre>
 * &lt;alreadyVisited location="locationId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class AlreadyVisited extends Condition {
	
	public static final String XML_LOCATION = "location";

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameLocation) {
			return ((GameLocation) object).alreadyVisited();
		} else {
			GameLocation location = gameState.getLocationById(getParameter(XML_LOCATION));
			return location == null ? false : location.alreadyVisited();
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "alreadyVisited";
	}

}
