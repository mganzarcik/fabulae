package mg.fishchicken.gamelogic.combat;

import java.io.Serializable;
import java.util.Comparator;

import mg.fishchicken.gamelogic.characters.GameCharacter;

public class InitiativeComparator implements Comparator<GameCharacter>, Serializable{
	
	private static final long serialVersionUID = 2708234753349359453L;

	@Override
	public int compare(GameCharacter o1, GameCharacter o2) {
		return Integer.compare(o1.stats().getAPMax(), o2.stats().getAPMax());
	}

}
