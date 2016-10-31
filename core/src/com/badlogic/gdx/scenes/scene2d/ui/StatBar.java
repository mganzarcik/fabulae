package com.badlogic.gdx.scenes.scene2d.ui;

import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public abstract class StatBar extends ProgressBar {

	private GameCharacter character;
	
	public StatBar(GameCharacter character, boolean vertical,
			StatBarStyle style) {
		super(0, 1, 1, vertical, style);
		this.character = character;
	}
	
	@Override
	public void act(float delta) {
		setRange(0, getStatMax(character));
		setValue(getStatCurr(character));
		super.act(delta);
	}
	
	@Override
	public float getPrefHeight() {
		return ((StatBarStyle)getStyle()).height;
	}
	
	@Override
	public float getPrefWidth() {
		return ((StatBarStyle)getStyle()).width;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		ProgressBarStyle style = this.getStyle();
		boolean disabled = this.disabled;
		final Drawable knob = (disabled && style.disabledKnob != null) ? style.disabledKnob : style.knob;
		final Drawable bg = (disabled && style.disabledBackground != null) ? style.disabledBackground : style.background;
		final Drawable knobBefore = (disabled && style.disabledKnobBefore != null) ? style.disabledKnobBefore : style.knobBefore;
		final Drawable knobAfter = (disabled && style.disabledKnobAfter != null) ? style.disabledKnobAfter : style.knobAfter;

		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();
		float knobHeight = knob == null ? 0 : knob.getMinHeight();
		float knobWidth = knob == null ? 0 : knob.getMinWidth();
		float value = getVisualValue();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		float min = getMinValue();
		float max = getMaxValue();
		
		if (vertical) {
			bg.draw(batch, x + (int)((width - bg.getMinWidth()) * 0.5f), y, width, height);

			float positionHeight = height - (bg.getTopHeight() + bg.getBottomHeight());
			float knobHeightHalf = 0;
			if (min != max) {
				if (knob == null) {
					knobHeightHalf = knobBefore.getMinHeight() * 0.5f;
					position = (value - min) / (max - min) * (positionHeight - knobHeightHalf);
					position = Math.min(positionHeight - knobHeightHalf, position);
				}
				else {
					knobHeightHalf = knobHeight * 0.5f;
					position = (value - min) / (max - min) * (positionHeight - knobHeight);
					position = Math.min(positionHeight - knobHeight, position) + bg.getBottomHeight();
				}
				position = Math.max(0, position);
			}

			if (knobBefore != null) {
				float offset = 0;
				offset = bg.getTopHeight();
				knobBefore.draw(batch, x + (int)((width - knobBefore.getMinWidth()) * 0.5f), y + offset, width,
						(int)(position + knobHeightHalf));
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + (int)((width - knobAfter.getMinWidth()) * 0.5f), y + (int)(position + knobHeightHalf),
						width, height - (int)(position + knobHeightHalf));
			}
			if (knob != null) knob.draw(batch, x + (int)((width - knobWidth) * 0.5f), (int)(y + position), knobWidth, knobHeight);
		} else {
			bg.draw(batch, x, y + (int)((height - bg.getMinHeight()) * 0.5f), width, height);

			float positionWidth = width - (bg.getLeftWidth() + bg.getRightWidth());
			float knobWidthHalf = 0;
			if (min != max) {
				if (knob == null) {
					knobWidthHalf = knobBefore.getMinWidth() * 0.5f;
					position = (value - min) / (max - min) * (positionWidth - knobWidthHalf);
					position = Math.min(positionWidth - knobWidthHalf, position);
				}
				else {
					knobWidthHalf = knobWidth * 0.5f;
					position = (value - min) / (max - min) * (positionWidth - knobWidth);
					position = Math.min(positionWidth - knobWidth, position) + bg.getLeftWidth();
				}
				position = Math.max(0, position);
			}

			if (knobBefore != null) {
				float offset = bg.getLeftWidth();
				knobBefore.draw(batch, x + offset, y + (int)((height - knobBefore.getMinHeight()) * 0.5f), (int)(position + knobWidthHalf),
						height);
			}
			if (knobAfter != null) {
				knobAfter.draw(batch, x + (int)(position + knobWidthHalf), y + (int)((height - knobAfter.getMinHeight()) * 0.5f),
						width - (int)(position + knobWidthHalf), height);
			}
			if (knob != null) knob.draw(batch, (int)(x + position), (int)(y + (height - knobHeight) * 0.5f), knobWidth, knobHeight);
		}
	}
	
	abstract protected float getStatMax(GameCharacter character);
	abstract protected float getStatCurr(GameCharacter character);

	static public class StatBarStyle extends ProgressBarStyle {

		public int width, height;
		
		public StatBarStyle () {
			super();
		}

		public StatBarStyle (Drawable background, Drawable knob) {
			super(background, knob);
		}

		public StatBarStyle (StatBarStyle style) {
			super(style);
			this.width = style.width;
			this.height = style.height;
		}
	}
	
}
