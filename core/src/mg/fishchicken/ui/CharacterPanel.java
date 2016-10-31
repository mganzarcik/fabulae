package mg.fishchicken.ui;

import mg.fishchicken.gamelogic.characters.GameCharacter;

public abstract class CharacterPanel extends BorderedWindow {

	public CharacterPanel(BorderedWindowStyle style) {
		super(style);
	}
	
	public CharacterPanel(String title, BorderedWindowStyle style) {
		super(title, style);
	}

	public abstract void loadCharacter(GameCharacter character);
}
