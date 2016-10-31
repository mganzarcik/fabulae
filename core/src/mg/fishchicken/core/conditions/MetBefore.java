package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied NonPlayerCharacter
 * has been met by the Player before.
 * <br /><br />
 * Example: 
 * <pre>
 * &lt;metBefore targetObject="__npcAtDialogue" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class MetBefore extends Condition {

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			return ((GameCharacter)object).getMetNPCBefore();
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "metBefore";
	}
}
