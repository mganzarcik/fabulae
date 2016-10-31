package mg.fishchicken.gamelogic.characters.perks;

import com.badlogic.gdx.utils.Array;

public interface PerksContainer {

	public static final String XML_PERK = "perk";
	public static final String XML_PERKS = "perks";
	
	/**
	 * Adds the supplied Perk to this PerkContainer.
	 * @param perk
	 */
	public void addPerk(Perk perk);
	
	/**
	 * Returns all Perks this PerkContainer contains.
	 * 
	 * @return
	 */
	public Array<Perk> getPerks();
}
