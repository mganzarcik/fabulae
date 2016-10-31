package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.Race;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true of the race of the character is the same 
 * as the one supplied.
 * 
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;raceEqualTo race="elf"  targetObject="__pcAtDialogue" /&gt;
 * </pre>  
 * @author micha
 *
 */
public class RaceEqualTo extends Condition {

	public static final String XML_RACE = "race";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			Race race = ((GameCharacter)object).stats().getRace();
			return race != null && getParameter(XML_RACE).equalsIgnoreCase(race.getId());
		}
		return false;
	}

	@Override
	protected String getStringTableNameKey() {
		return "RaceEqualTo";
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_RACE) == null) {
			throw new GdxRuntimeException(XML_RACE+" must be set for condition RaceEqualTo in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_RACE)};
	}

}
