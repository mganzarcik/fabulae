package mg.fishchicken.core.actions;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;

/**
 * Adds the supplied amount to the SP of the supplied
 * player character.  
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;addToSP amount="50" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class AddToSP extends Action {

	public static final String XML_AMOUNT = "amount";
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			((GameCharacter) object).stats().addToSP(Integer.parseInt(getParameter(XML_AMOUNT)));
		} 
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_AMOUNT, null) == null) {
			throw new GdxRuntimeException(XML_AMOUNT+" must be set for action AddToSP in element: \n\n"+conditionElement);
		}
	}

}
