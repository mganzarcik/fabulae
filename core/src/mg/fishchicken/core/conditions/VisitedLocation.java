package mg.fishchicken.core.conditions;

import groovy.lang.Binding;

import java.util.Locale;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied NonPlayerCharacter (or PlayerCharacterGroup) visited
 * the given location.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;visitedLocation location="PalaceGardens" targetObject="__npcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class VisitedLocation extends Condition {

public static final String XML_LOCATION_NAME = "location";

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			return ((GameCharacter)object).visitedLocation(getParameter(XML_LOCATION_NAME).toLowerCase(Locale.ENGLISH));
		} else if (object instanceof PlayerCharacterGroup) {
			return ((PlayerCharacterGroup)object).visitedLocation(getParameter(XML_LOCATION_NAME).toLowerCase(Locale.ENGLISH));
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_LOCATION_NAME, null) == null) {
			throw new GdxRuntimeException(XML_LOCATION_NAME+" must be set for condition VisitedLocation in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "visited";
	}
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_LOCATION_NAME)};
	}

}
