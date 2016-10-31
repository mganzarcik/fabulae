package mg.fishchicken.ui.formation;

import mg.fishchicken.ui.button.TextButtonWithSound;

public class FormationEditorButton extends TextButtonWithSound {
	
	private TextButtonWithSoundStyle emptyStyle, occupiedStyle;
	private Integer characterIndex;
	
	public FormationEditorButton(TextButtonWithSoundStyle emptyStyle, TextButtonWithSoundStyle occupiedStyle) {
		super("", emptyStyle);
		this.characterIndex = null;
		this.occupiedStyle = occupiedStyle; 
		this.emptyStyle = emptyStyle;
	}

	public void setCharacterIndex(Integer index) {
		this.characterIndex = index;
		if (characterIndex != null) {
			setText(""+(characterIndex+1));
			setStyle(occupiedStyle);
		} else {
			setText("");
			setStyle(emptyStyle);
		}
	}
	
	public Integer getCharacterIndex() {
		return characterIndex;
	}
}
