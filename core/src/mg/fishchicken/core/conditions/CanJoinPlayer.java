package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the character can join the player. 
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;canJoinPlayer targetObject="__npcAtDialogue" /&gt;
 * </pre>
 *
 */
public class CanJoinPlayer extends Condition {

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		return GameState.getPlayerCharacterGroup().getPlayerCharacters().size < Configuration.getMaxCharactersInGroup();
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "";
	}

}
