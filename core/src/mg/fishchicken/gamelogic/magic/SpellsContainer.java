package mg.fishchicken.gamelogic.magic;

import com.badlogic.gdx.utils.Array;

public interface SpellsContainer {

	public static final String XML_SPELLS = "spells";
	public static final String XML_SPELL = "spell";
	
	/**
	 * Adds the supplied Spell to this SpellsContainer.
	 * @param perk
	 */
	public void addSpell(Spell spell);
	
	/**
	 * Returns all Spells this SpellsContainer contains.
	 * 
	 * @return
	 */
	public Array<Spell> getSpells();
}
