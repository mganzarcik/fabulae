package mg.fishchicken.gamelogic.factions;


public interface FactionContainer {

	/**
	 * Returns the faction this container belongs to.
	 * 
	 * This never returns null.
	 * @return
	 */
	public Faction getFaction();
	
	/**
	 * Returns true if this container is hostile towards the supplied faction.
	 * 
	 * @param faction
	 * @return
	 */
	public boolean isHostileTowards(Faction faction);
}
