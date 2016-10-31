package mg.fishchicken.ui.dialog;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.ConfirmCancelKeyboardListener;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MessageDialog extends BorderedWindow {

	private Label messageLabel;
	protected OkCancelCallback<Void> callback;
	
	public MessageDialog(String title, MessageDialogStyle style) {
		this(title, "", style, null);
	}
	
	public MessageDialog(String title, String message, MessageDialogStyle style) {
		this(title, message, style, null);
	}
	
	public MessageDialog(String title, String message, MessageDialogStyle style, OkCancelCallback<Void> callback) {
		super(title, style);
		
		this.callback = callback;
		
		messageLabel = new Label(message, style.messageStyle);
		messageLabel.setWrap(true);
		
		buildBody(style);
		row();
		Table buttonsTable = new Table();
		createButtons(buttonsTable, style);
		add(buttonsTable).center().padTop(style.buttonsMarginTop);
		pack();
	}
	
	protected void buildBody(MessageDialogStyle style) {
		add(messageLabel).fill().width(style.messageWidth);
	}
	
	protected void createButtons(Table buttonsTable, MessageDialogStyle style) {
		final TextButtonWithSound okButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, getOkButtonKey()), style.okButtonStyle);
		buttonsTable.add(okButton).minWidth(style.okButtonWidth).minHeight(style.okButtonHeight).fill();
		
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setVisible(false);
				remove();
				if (callback != null) {
					callback.onOk(null);
				}
			}
		});
		addListener(new ConfirmCancelKeyboardListener(okButton, null));
	}
	
	protected String getOkButtonKey() {
		return "ok";
	}
	
	public void setMessage(String message) {
		messageLabel.setText(message);
	}

	public static class MessageDialogStyle extends BorderedWindowStyle {
		public int buttonsMarginTop, okButtonWidth, okButtonHeight, messageWidth; 
		public LabelStyle messageStyle;
		public TextButtonWithSoundStyle okButtonStyle;
	}
	
}
