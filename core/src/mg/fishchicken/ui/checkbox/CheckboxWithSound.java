package mg.fishchicken.ui.checkbox;

import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class CheckboxWithSound extends CheckBox {

	public CheckboxWithSound(String text, CheckBoxWithSoundStyle style) {
		super(text, style);
		attachSoundListener();
	}

	private void attachSoundListener() {
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				if (isDisabled()) {
					return false;
				}
				CheckBoxStyle style = getStyle();
				if (style instanceof CheckBoxWithSoundStyle) {
					CheckBoxWithSoundStyle castStyle = (CheckBoxWithSoundStyle) style;
					if (castStyle.clickSound != null) {
						castStyle.clickSound.play(Configuration.getUIEffectsVolume());
					}
				}
				return false;
			}
		});
	}

	public static class CheckBoxWithSoundStyle extends CheckBoxStyle {
		private Sound clickSound;
	}
}
