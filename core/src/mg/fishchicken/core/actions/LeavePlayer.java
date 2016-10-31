package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Makes the supplied PlayerCharacter leave the Player's
 * group.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;leavePlayer targetObject="__npcAtDialogue"/&gt;
 * </pre>
 * <p>or:</p> 
 * <pre>
 * &lt;leavePlayer character="characterId"/&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class LeavePlayer extends Action {

	public static final String XML_CHARACTER = "character";
	
	@Override
	protected void run(Object object, Binding parameters) {
		String character = getParameter(XML_CHARACTER);
		GameState.getPlayerCharacterGroup()
				.removeMember(
						character != null ? (GameCharacter) GameState.getGameObjectById(character)
								: (GameCharacter) object);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

}
