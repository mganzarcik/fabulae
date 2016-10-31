package mg.fishchicken.ui.dialog;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.ConfirmCancelKeyboardListener;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ConfirmationDialog extends MessageDialog {
	
	private TextButtonWithSound cancelButton;
	
	public ConfirmationDialog(String title, MessageDialogStyle style) {
		super(title, "", style, null);
	}
	
	public ConfirmationDialog(String title, String message, MessageDialogStyle style, OkCancelCallback<Void> callback) {
		super(title, message, style, callback);
	}
	
	public void setCallback(OkCancelCallback<Void> callback) {
		this.callback = callback;
	}
	
	protected void createButtons(Table buttonsTable, MessageDialogStyle style) {
		ConfirmationDialogStyle castStyle = (ConfirmationDialogStyle)style;
		cancelButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "no"), castStyle.cancelButtonStyle);
		buttonsTable.add(cancelButton).minWidth(castStyle.cancelButtonWidth)
				.minHeight(castStyle.cancelButtonHeight).fill()
				.padRight(castStyle.cancelButtonMarginRight);
		
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setVisible(false);
				remove();
				if (callback != null) {
					callback.onCancel();
				}
			}
		});
		
		addListener(new ConfirmCancelKeyboardListener(null, cancelButton));
		super.createButtons(buttonsTable, style);
	}
	
	@Override
	protected String getOkButtonKey() {
		return "yes";
	}
	
	public Vector2 getCancelCoordinates() {
		return cancelButton.localToStageCoordinates(new Vector2(cancelButton.getWidth()/2, cancelButton.getHeight()/2));
	}
	
	public static class ConfirmationDialogStyle extends MessageDialogStyle {
		public int cancelButtonWidth, cancelButtonHeight, cancelButtonMarginRight; 
		public TextButtonWithSoundStyle cancelButtonStyle;
	}
	
}
