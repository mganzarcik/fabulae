package mg.fishchicken.core.conditions;

import groovy.lang.Binding;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied character is currently a member
 * of the player character group. The character has to be a full
 * member, not an NPC or a temporary member. It will return true
 * for dead or inactive party members as well!
 * <br /><br />
 * Examples:
 * 
 * <pre>
 * &lt;characterInParty character="characterId" /&gt;
 * </pre>
 *   
 * @author annun
 *
 */
public class CharacterInParty extends Condition {

	public static final String XML_CHARACTER_ID = "character";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		return GameState.isMemberOfPlayerGroup(getParameter(XML_CHARACTER_ID));
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_CHARACTER_ID, null) == null) {
			throw new GdxRuntimeException(XML_CHARACTER_ID+" must be set for condition CharacterInParty in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "characterInParty";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		try {
			String id = getParameter(XML_CHARACTER_ID);
			String name = null;
			GameObject go = GameState.getGameObjectById(id);
			if (go == null) {
				// if the character has not been loaded yet, load it now,
				// get the name and dispose of it
				// this will probably incur a performance hit
				go = GameCharacter.loadCharacter(id);
				name = go.getName();
				go.remove();
			} else {
				name = go.getName();
			}
			return new Object[]{name};
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
}
