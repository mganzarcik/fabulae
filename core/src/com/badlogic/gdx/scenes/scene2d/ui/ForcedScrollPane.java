package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Extension of the libGDX ScrollPane that forces the vertical scroll
 * bar to be always visible by default (this can still be turned off
 * by calling setForceScroll. It also turns off overscroll and fading
 * for scroll bars by default.
 * 
 * The main difference however, is that when the scroll bar is shown
 * but the content is not big enough to be scrolled, the scrollbar's
 * knob will not be drawn at all, instead of being stretched to the full length
 * of the bar as in the libGDX implementation. 
 * 
 * Placed in the badlogic package to get access to some
 * package-visible properties.
 * @author ANNUN
 *
 */
public class ForcedScrollPane extends ScrollPane {
	
	public ForcedScrollPane (Actor widget) {
		super(widget, new ScrollPaneStyle());
		init();
	}
	public ForcedScrollPane(Actor widget, Skin skin) {
		super(widget, skin);
		init();
	}
	
	public ForcedScrollPane(Actor widget, Skin skin, String styleName) {
		super(widget, skin, styleName);
		init();
	}
	
	public ForcedScrollPane (Actor widget, ScrollPaneStyle style) {
		super(widget, style);
		init();
	}

	private void init() {
		setForceScroll(false, true);
		setFadeScrollBars(false);		
		setOverscroll(false, false);
	}
	
	@Override
	public void layout() {
		super.layout();

		if (scrollX && hKnobBounds.width == areaWidth) {
			hKnobBounds.width = 0;
		}
		if (scrollY && vKnobBounds.height == areaHeight) {
			vKnobBounds.height = 0;
		}
	}

}
