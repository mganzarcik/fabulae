package mg.fishchicken.gamelogic.factions;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;


/**
 * This represents an empty faction.
 * 
 * Empty faction is neutral towards everyone and this cannot be changed.
 * 
 */
public class NoFaction extends Faction {
	
	public NoFaction() {
		super("__noFaction");
	}
	
	protected boolean shouldBeSaved() {
		return false;
	}

	@Override
	public String getName() {
		return Strings.getString(STRING_TABLE, "noFaction");
	}
	
	@Override
	public void addMember(AbstractGameCharacter member) {
	}
	
	@Override
	public void removeMember(AbstractGameCharacter member) {
	}
	
	@Override
	public void setDisposition(Faction faction, int disposition) {
	}
	
	@Override
	public void setDisposition(String factionId, int disposition) {
	}
	
	@Override
	public void modifyDisposition(String factionId, int modifier) {
	}
	
	@Override
	public int getDispositionTowards(Faction faction) {
		return 0;
	}
	
	@Override
	public int getDisposition(String factionId) {
		return 0;
	}
	
	@Override
	public String getDispositionTowardsPlayerAsString() {
		return Strings.getString(Faction.STRING_TABLE,"NotCare");
	}

}
