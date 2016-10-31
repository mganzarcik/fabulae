package mg.fishchicken.ui.button;

import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ImageButtonWithSound extends ImageButton {

	public ImageButtonWithSound(ImageButtonWithSoundStyle style) {
		super(style);
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
				if (style instanceof ImageButtonWithSoundStyle) {
					ImageButtonWithSoundStyle castStyle = (ImageButtonWithSoundStyle) style;
					if (castStyle.clickSound != null) {
						castStyle.clickSound.play(Configuration.getUIEffectsVolume());
					}
				}
				return false;
			}
		});
	}
	
	public static class ImageButtonWithSoundStyle extends ImageButtonStyle {
		public Sound clickSound;
	}
}
