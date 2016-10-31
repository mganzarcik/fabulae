package mg.fishchicken.screens.start;

import mg.fishchicken.gamelogic.characters.PlayerCharacter;

public interface CharacterCreationButtonCallback {

	public void onCreate(CharacterCreationCallback characterSetter);
	
	public void onEdit(PlayerCharacter character);
	
	public void onDelete(PlayerCharacter character);
	
	public static interface CharacterCreationCallback {
		public void setCharacter(PlayerCharacter character);
	}
}
