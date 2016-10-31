package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.magic.Spell;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied character knows the Spell.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;hasSpell id = "Fireball" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class KnowsSpell extends Condition {

	private static final String XML_SPELL_ID = "id";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) object;
			return character.hasSpell(Spell.getSpell(getParameter(XML_SPELL_ID)));
		}
		return false;
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		String spellId = conditionElement.get(XML_SPELL_ID, null);
		if (spellId == null) {
			throw new GdxRuntimeException(XML_SPELL_ID+" must be set for condition KnowsSpell in element: \n\n"+conditionElement);
		}
		if (!Spell.spellExists(spellId)) {
			throw new GdxRuntimeException(XML_SPELL_ID+" contains invalid value "+spellId+", which is not an existing spell in condition KnowsSpell in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "knowsSpell";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{Spell.getSpell(getParameter(XML_SPELL_ID)).getName()};
	}
	
}
