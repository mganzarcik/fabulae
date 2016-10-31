package mg.fishchicken.gamestate.crime;

import java.io.IOException;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.locations.GameLocation;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Trespass extends Crime<GameLocation>{

	public Trespass() {
		
	}
	
	public Trespass(GameCharacter perpetrator, GameLocation location) {
		super(perpetrator, location);
	}

	@Override
	public boolean canBePaidOff() {
		return true;
	}

	@Override
	public Faction getVictimFaction() {
		return getCrimeTarget().getOwnerFaction();
	}
	
	@Override
	protected int getBaseFineAmount() {
		return Configuration.getTrespassBaseFine();
	}
	
	@Override
	public int getDispositionPenalty() {
		return Configuration.getTrespassDispositionPenalty();
	}

	@Override
	protected void writeTargetToXml(XmlWriter writer) throws IOException {
		writer.text(getCrimeTarget().getId());
	}

	@Override
	protected GameLocation readTargetFromXml(Element targetElement) throws IOException {
		return gameState.getLocationByIternalId(targetElement.getText());
	}

}
