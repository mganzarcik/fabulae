package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.PlayerCharacter;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Gives the specified amount of experience to the supplied
 * player character. If anything else is supplied,
 * the experience is instead given to the whole player
 * character group, equally divided among active characters. 
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;giveExperience amount="50" targetObject="__pcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class GiveExperience extends Action {

	public static final String XML_AMOUNT = "amount";
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof PlayerCharacter) {
			((PlayerCharacter) object).stats().giveExperience(Integer.parseInt(getParameter(XML_AMOUNT)));
		} else {
			Array<GameCharacter> playerCharacters = GameState.getPlayerCharacterGroup().getPlayerCharacters();
			int playerCharacterCount = 0;
			for (GameCharacter character : playerCharacters) {
				if (character.isActive()) {
					++ playerCharacterCount;
				}
			}
			
			int expToAward = Integer.parseInt(getParameter(XML_AMOUNT)) / playerCharacterCount;
			
			for (GameCharacter character : playerCharacters) {
				if (character.isActive()) {
					character.stats().giveExperience(expToAward);
				}
			}
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_AMOUNT, null) == null) {
			throw new GdxRuntimeException(XML_AMOUNT+" must be set for action GiveExperience in element: \n\n"+conditionElement);
		}
	}

}
