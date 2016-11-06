package mg.fishchicken.core.actions;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;

/**
 * Adds the supplied amount to the HP of the supplied
 * player character.  
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;addToHP amount="50" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class AddToHP extends Action {

	public static final String XML_AMOUNT = "amount";
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			((GameCharacter) object).stats().addToHP(Float.parseFloat(getParameter(XML_AMOUNT)));
		} 
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_AMOUNT, null) == null) {
			throw new GdxRuntimeException(XML_AMOUNT+" must be set for action AddToHP in element: \n\n"+conditionElement);
		}
	}

}
