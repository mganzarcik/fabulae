package mg.fishchicken.ui.button;

import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class TextButtonWithSound extends TextButton {

	public TextButtonWithSound(TextButtonWithSoundStyle style) {
		this("", style);
	}
	
	public TextButtonWithSound(String text, final TextButtonWithSoundStyle style) {
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
				ButtonStyle style = getStyle();
				if (style instanceof TextButtonWithSoundStyle) {
					TextButtonWithSoundStyle castStyle = (TextButtonWithSoundStyle) style;
					if (castStyle.clickSound != null) {
						castStyle.clickSound.play(Configuration.getUIEffectsVolume());
					}
				}
				return false;
			}
		});
	}

	public static class TextButtonWithSoundStyle extends TextButtonStyle {
		private Sound clickSound;
	}
}
