package mg.fishchicken.gamestate.crime;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Assault extends Crime<GameCharacter> {

	public Assault() {
		
	}
	
	public Assault(GameCharacter assaulter, GameCharacter victim) {
		super(assaulter, victim);
	}

	@Override
	public boolean canBePaidOff() {
		return true;
	}

	@Override
	protected int getBaseFineAmount() {
		return Configuration.getAssaultBaseFine();
	}
	
	@Override
	public Faction getVictimFaction() {
		return getCrimeTarget().getFaction();
	}
	
	@Override
	public int getDispositionPenalty() {
		return Configuration.getAssaultDispositionPenalty();
	}

	@Override
	protected void writeTargetToXml(XmlWriter writer) throws IOException {
		writer.text(getCrimeTarget().getInternalId());
	}

	@Override
	protected GameCharacter readTargetFromXml(Element targetElement) throws IOException {
		return (GameCharacter) GameState.getGameObjectByInternalId(targetElement.getText());
	}

}
