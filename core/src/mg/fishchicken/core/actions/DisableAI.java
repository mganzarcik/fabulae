package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Disables AI on the supplied character.
 * <br /><br />
 * Examples:
 * 
 * <pre>
 * &lt;disableAI character="characterId" /&gt;
 * </pre>
 *   
 * @author annun
 *
 */
public class DisableAI extends Action {
	
	public static final String XML_CHARACTER = "character";

	@Override
	protected void run(Object object, Binding parameters) {
		String character = getParameter(XML_CHARACTER);
		((GameCharacter) (character == null ? object : GameState.getGameObjectById(character))).brain().disable();
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

}
