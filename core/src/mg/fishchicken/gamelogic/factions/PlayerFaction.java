package mg.fishchicken.gamelogic.factions;

import mg.fishchicken.core.i18n.Strings;


/**
 * This represents the player faction.
 * 
 * Player faction is neutral towards everyone except itself and
 * this cannot be changed.
 * 
 */
public class PlayerFaction extends Faction {
	
	public PlayerFaction() {
		super("player");
	}
	
	@Override
	public String getName() {
		return Strings.getString(STRING_TABLE, "player");
	}
	
	protected boolean shouldBeSaved() {
		return false;
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
	public int getDisposition(String factionId) {
		if (getId().equals(factionId)) {
			return 100;
		}
		return 0;
	}
}
