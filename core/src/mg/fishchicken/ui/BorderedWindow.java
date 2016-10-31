package mg.fishchicken.ui;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.MathUtil;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class BorderedWindow extends Window {

	public BorderedWindow(BorderedWindowStyle style) {
		this("", style);
	}
	
	public BorderedWindow(String title, BorderedWindowStyle style) {
		super(title, style);
		getTitleLabel().setAlignment(Align.left);
		padTop(style.borderTop);
		padLeft(style.borderLeft);
		padRight(style.borderRight);
		padBottom(style.borderBottom);
		// add a new listener that always returns true so that we never let any clicks go through us
		addListener(new ClickListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0 && button != 0) return false;
				return true;
			}
		});
	}
	
	@Override
	protected void drawStageBackground(Batch batch, float parentAlpha, float x,
			float y, float width, float height) {
		Drawable background = getStyle().stageBackground;
		float drawableWidth = background.getMinWidth();
		float drawableHeight = background.getMinHeight();
		if (drawableWidth < width || drawableHeight < height) {
			Vector2 tempVector = MathUtil.getVector2();
			Stage stage = getStage();
			x = stage.getWidth()/2 - drawableWidth/2;
			y = stage.getHeight()/2 - drawableHeight/2;
			//stageToLocalCoordinates(tempVector.set(x, y));
			//x = tempVector.x;
			//y = tempVector.y;
			MathUtil.freeVector2(tempVector);
			width = background.getMinWidth();
			height = background.getMinHeight();
		}
		super.drawStageBackground(batch, parentAlpha, x, y, width, height);
		
	}
	
	@Override
	public void setVisible(boolean visible) {
		boolean wasVisible = isVisible();
		super.setVisible(visible);
		
		if (visible && !wasVisible && getStage() != null) {
			getStage().setKeyboardFocus(this);
		}
		
		BorderedWindowStyle style = getStyle();
		if (visible && style.showSound != null) {
			style.showSound.play(Configuration.getUIEffectsVolume());
		} else if (!visible && style.hideSound != null) {
			style.hideSound.play(Configuration.getUIEffectsVolume());
		}
	}
	
	@Override
	protected void setStage(Stage stage) {
		super.setStage(stage);
		if (stage != null) {
			stage.setKeyboardFocus(this);
		}
	}
	
	public void setTitle(String title) {
		getTitleLabel().setText(title);
	}
	
	@Override
	public void setStyle(WindowStyle style) {
		if (!(style instanceof BorderedWindowStyle)) {
			throw new GdxRuntimeException("BorderedWindow only supports BorderedWindowStyle and its descendants.");
		}
		super.setStyle(style);
	}
	
	@Override
	public BorderedWindowStyle getStyle() {
		return (BorderedWindowStyle)super.getStyle();
	}
	
	
	public static class BorderedWindowStyle extends WindowStyle {
		public int borderTop = 30, borderBottom = 10, borderLeft = 10, borderRight = 10;
		private Sound showSound, hideSound;
	}
}
