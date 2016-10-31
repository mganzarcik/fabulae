package mg.fishchicken.ui.slider;

import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SliderWithSound extends Slider {

	public SliderWithSound(float min, float max, float stepSize,
			boolean vertical, SliderWithSoundStyle style) {
		super(min, max, stepSize, vertical, style);
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
				SliderStyle style = getStyle();
				if (style instanceof SliderWithSoundStyle) {
					SliderWithSoundStyle castStyle = (SliderWithSoundStyle) style;
					if (castStyle.clickSound != null) {
						castStyle.clickSound.play(Configuration.getUIEffectsVolume());
					}
				}
				return false;
			}
		});
	}

	public static class SliderWithSoundStyle extends SliderStyle {
		private Sound clickSound;
	}
}
