package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Makes the supplied PlayerCharacter join the Player's
 * group.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;joinPlayer targetObject="__npcAtDialogue" /&gt;
 * </pre>
 * <p>or:</p> 
 * <pre>
 * &lt;joinPlayer character="characterId"/&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class JoinPlayer extends Action {
	
	public static final String XML_CHARACTER = "character";

	@Override
	protected void run(Object object, Binding parameters) {
		String characterId = getParameter(XML_CHARACTER);
		GameCharacter character = characterId != null ? (GameCharacter) GameState.getGameObjectById(characterId)
				: (GameCharacter) object;
		if (!GameState.getPlayerCharacterGroup().addMember(character, true, true)) {
			// TODO implement logic for handling the case where the NPC can no longer join the player
		} else {
			character.removeAllActions();
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

}
