package mg.fishchicken.ui.saveload;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class DeletableSavedGameButton extends SavedGameButton {

	private TextButtonWithSound deleteButton;
	
	public DeletableSavedGameButton(final SaveGameDetails game,
			final DeletableSavedGameButtonStyle style, final OkCancelCallback<Void> deleteConfirmationCallback) {
		super(game, style);
		
		deleteButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "deleteSave"), style.deleteButtonStyle);
		deleteButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				event.stop();
				UIManager.displayConfirmation(
					Strings.getString(UIManager.STRING_TABLE, "deleteSaveQuestion"), 
					Strings.getString(UIManager.STRING_TABLE, "deleteSaveConfirmation", game.getName()), 
					deleteConfirmationCallback);
			}
		});
		add(deleteButton)
				.width(style.deleteButtonWidth)
				.height(style.deleteButtonHeight)
				.pad(style.deleteButtonMarginTop,
						style.deleteButtonMarginLeft,
						style.deleteButtonMarginBottom,
						style.deleteButtonMarginRight);
	}
	
	@Override
	public boolean isOver() {
		return super.isOver() && !deleteButton.isOver();
	}
	
	public static class DeletableSavedGameButtonStyle extends SavedGameButtonStyle {
		private TextButtonWithSoundStyle deleteButtonStyle;
		private int deleteButtonWidth, deleteButtonHeight,
				deleteButtonMarginLeft, deleteButtonMarginRight, deleteButtonMarginTop, deleteButtonMarginBottom;
	}

}
