package mg.fishchicken.ui.dialog;

import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.dialog.ConfirmationDialog.ConfirmationDialogStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class InputDialog extends BorderedWindow {

	private TextButtonWithSound okButton, cancelButton;
	private Label messageLabel;
	private TextField textField;
	protected OkCancelCallback<String> callback;
	
	public InputDialog(String title, String message, InputDialogStyle style, OkCancelCallback<String> callback) {
		super(title, style);
		
		this.callback = callback;
		
		messageLabel = new Label(message, style.messageStyle);
		messageLabel.setWrap(true);
		
		textField = new TextField("", style.textFieldStyle);
		textField.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (KeyBindings.CONFIRM.is(keycode)) {
					if (textField.getText().trim().length() < 1) {
						return false;
					}
					okButton.toggle();
					return true;
				} else if (KeyBindings.CANCEL.is(keycode)) {
					cancelButton.toggle();
				}
				
				return false;
			}
		});
		
		add(messageLabel).fill().width(style.messageWidth);
		row();
		add(textField).fill().width(style.messageWidth).padTop(style.textFieldMarginTop);
		row();
		Table buttonsTable = new Table();
		createButtons(buttonsTable, style);
		add(buttonsTable).center().padTop(style.buttonsMarginTop);
		pack();
	}
	
	protected void createButtons(Table buttonsTable, InputDialogStyle style) {
		cancelButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "cancel"), style.cancelButtonStyle);
		buttonsTable.add(cancelButton).width(style.cancelButtonWidth)
				.height(style.cancelButtonHeight).fill()
				.padRight(style.cancelButtonMarginRight);
		
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				hideAndRemove();
				if (callback != null) {
					callback.onCancel();
				}
			}
		});
		
		okButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "ok"), style.okButtonStyle);
		buttonsTable.add(okButton).width(style.okButtonWidth).height(style.okButtonHeight).fill();
		
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ok();
			}
		});
	}
	
	@Override
	public void act(float delta) {
		okButton.setDisabled(textField.getText().trim().length() < 1);
		super.act(delta);
	}
	
	private void ok() {
		hideAndRemove();
		if (callback != null) {
			callback.onOk(textField.getText());
		}
	}
	
	private void hideAndRemove() {
		setVisible(false);
		remove();
	}
	
	public void setMessage(String message) {
		messageLabel.setText(message);
	}
	
	public void clearInput() {
		textField.setText("");
	}
	
	public void focusOnInput() {
		if (getStage() != null) {
			getStage().setKeyboardFocus(textField);
		}
	}

	public static class InputDialogStyle extends ConfirmationDialogStyle {
		private TextFieldStyle textFieldStyle;
		private int textFieldMarginTop;
	}
	
}
