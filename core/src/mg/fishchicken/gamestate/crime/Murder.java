package mg.fishchicken.gamestate.crime;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.GameCharacter;

public class Murder extends Assault {

	public Murder() {
		super();
	}
	
	public Murder(GameCharacter murderer, GameCharacter victim) {
		super(murderer, victim);
	}

	@Override
	public boolean canBePaidOff() {
		return false;
	}

	@Override
	protected int getBaseFineAmount() {
		return 0;
	}
	
	@Override
	public int getDispositionPenalty() {
		return Configuration.getMurderDispositionPenalty();
	}
	
}
