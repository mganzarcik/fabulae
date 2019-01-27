package mg.fishchicken.core.conditions;

import java.util.Locale;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.Gender;
/**
 * Returns true if the gender of the character is the same 
 * as the one supplied.
 * 
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;roleEqualTo gender="male"  targetObject="__pcAtDialogue" /&gt;
 * </pre>  
 * @author ANNUN
 *
 */
public class GenderEqualTo extends Condition {

	public static final String XML_GENDER = "gender";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			return Gender.valueOf(getParameter(XML_GENDER).toUpperCase(Locale.ENGLISH))
					.equals(((GameCharacter) object).stats().getGender());
		}
		return false;
	}

	@Override
	protected String getStringTableNameKey() {
		return "GenderEqualTo";
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_GENDER) == null) {
			throw new GdxRuntimeException(XML_GENDER+" must be set for condition GenderEqualTo in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_GENDER)};
	}

}
