package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.magic.Spell;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied character can learn the supplierSpell.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;hasSpell id = "Fireball" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class CanLearnSpell extends Condition {

	private static final String XML_SPELL_ID = "id";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) object;
			return Spell.getSpell(getParameter(XML_SPELL_ID)).canBeLearned(character);
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		String spellId = conditionElement.get(XML_SPELL_ID, null);
		if (spellId == null) {
			throw new GdxRuntimeException(XML_SPELL_ID+" must be set for condition CanLearnSpell in element: \n\n"+conditionElement);
		}
		if (!Spell.spellExists(spellId)) {
			throw new GdxRuntimeException(XML_SPELL_ID+" contains invalid value "+spellId+", which is not an existing spell in condition CanLearnSpell in element: \n\n"+conditionElement);
		}
	}

	@Override
	public String toUIString() {
		return Spell.getSpell(getParameter(XML_SPELL_ID)).getLearnRequirementsAsString();
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "";
	}

}
