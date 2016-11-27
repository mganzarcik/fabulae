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
 * <pre>
 * &lt;characterInParty targetObject = "__npcAtDialogue" /&gt;
 * </pre>
 *   
 * @author annun
 *
 */
public class CharacterInParty extends Condition {

	public static final String XML_CHARACTER_ID = "character";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		String id = getParameter(XML_CHARACTER_ID);
		if (id == null) {
			id = ((GameObject)object).getId();
		}
		return GameState.isMemberOfPlayerGroup(id);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_CHARACTER_ID, null) == null && conditionElement.get(PARAM_TARGET_OBJECT, null) == null) {
			throw new GdxRuntimeException(XML_CHARACTER_ID+" or "+PARAM_TARGET_OBJECT+" must be set for condition CharacterInParty in element: \n\n"+conditionElement);
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
			if (id != null) {
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
			} else {
				// TODO: this is not great, these should be localized, but this is a terrible edge case, so maybe later (yeah, right...)
				name = getParameter(PARAM_TARGET_OBJECT);
			}
			return new Object[]{name};
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
}
