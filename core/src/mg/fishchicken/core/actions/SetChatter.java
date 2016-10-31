package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Sets chatter of the supplied character to the one with the supplied id.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;setChatter chatter="chatterId" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class SetChatter extends Action {

	public static final String XML_CHATTER = "chatter";
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			((GameCharacter) object).setChatter(getParameter(XML_CHATTER));
		} 
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_CHATTER, null) == null) {
			throw new GdxRuntimeException(XML_CHATTER+" must be set for action SetChatter in element: \n\n"+conditionElement);
		}
	}

}
