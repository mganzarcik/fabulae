package mg.fishchicken.gamelogic.dialogue;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;


public class Banter extends DialogueItem {
	
	private String characterId;
	
	public Banter(Dialogue dialogue) {
		super(dialogue);
	}

	public String getCharacterId() {
		return characterId;
	}

	public void setCharacterId(String characterId) {
		this.characterId = characterId;
	}

	@Override
	public GameObject getConditionObject() {
		return GameState.getGameObjectByInternalId(characterId);
	}
	
}
