package mg.fishchicken.ui.selectbox;

import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

public class SelectBoxWithSound<T> extends SelectBox<T> {
	
	public SelectBoxWithSound(SelectBoxWithSoundStyle style) {
		super(style);
	}

	
	protected void onShow (Actor selectBoxList, boolean below) {
		super.onShow(selectBoxList, below);
		SelectBoxStyle style = getStyle();
		if (style instanceof SelectBoxWithSoundStyle) {
			SelectBoxWithSoundStyle castStyle = (SelectBoxWithSoundStyle) style;
			if (castStyle.showSound != null) {
				castStyle.showSound.play(Configuration.getUIEffectsVolume());
			}
		}
	}

	protected void onHide (Actor selectBoxList) {
		super.onHide(selectBoxList);
		SelectBoxStyle style = getStyle();
		if (style instanceof SelectBoxWithSoundStyle) {
			SelectBoxWithSoundStyle castStyle = (SelectBoxWithSoundStyle) style;
			if (castStyle.hideSound != null) {
				castStyle.hideSound.play(Configuration.getUIEffectsVolume());
			}
		}
	}
	
	public static class SelectBoxWithSoundStyle extends SelectBoxStyle {
		private Sound showSound, hideSound;
	}
}
