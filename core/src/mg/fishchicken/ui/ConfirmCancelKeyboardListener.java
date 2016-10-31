package mg.fishchicken.ui;

import mg.fishchicken.core.configuration.KeyBindings;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class ConfirmCancelKeyboardListener extends InputListener {
	
	private Button okButton, cancelButton;
	
	public ConfirmCancelKeyboardListener(Button okButton, Button cancelButton) {
		this.okButton = okButton;
		this.cancelButton = cancelButton;
	}
	
	@Override
	public boolean keyUp(InputEvent event, int keycode) {
		if (KeyBindings.CONFIRM.is(keycode) && okButton != null) {
			okButton.toggle();
			return true;
		} else if (KeyBindings.CANCEL.is(keycode) && cancelButton != null) {
			cancelButton.toggle();
			return true;
		}
		return false;
	}
}
