package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Enables AI on the supplied character.
 * <br /><br />
 * Examples:
 * 
 * <pre>
 * &lt;enableAI character="characterId" /&gt;
 * </pre>
 *   
 * @author annun
 *
 */
public class EnableAI extends Action {
	
	public static final String XML_CHARACTER = "character";

	@Override
	protected void run(Object object, Binding parameters) {
		String character = getParameter(XML_CHARACTER);
		((GameCharacter) (character == null ? object : GameState.getGameObjectById(character))).enableAI();
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

}
