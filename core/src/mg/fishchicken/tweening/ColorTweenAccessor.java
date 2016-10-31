package mg.fishchicken.tweening;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Color;

public class ColorTweenAccessor implements TweenAccessor<Color> {
	public static final int RED = 1;
	public static final int GREEN = 2;
	public static final int BLUE = 3;
	public static final int ALPHA = 4;

	@Override
	public int getValues(Color sun, int tweenType, float[] returnValues) {
		if (RED == tweenType) {
			returnValues[0] =  sun.r;
		}
		if (GREEN == tweenType) {
			returnValues[0] =  sun.g;
		}
		if (BLUE == tweenType) {
			returnValues[0] =  sun.b;
		}
		if (ALPHA == tweenType) {
			returnValues[0] =  sun.a;
		}
		return 1;
	}

	@Override
	public void setValues(Color sun, int tweenType, float[] newValues) {
		if (RED == tweenType) {
			sun.r = newValues[0];
		}
		if (GREEN == tweenType) {
			sun.g = newValues[0];
		}
		if (BLUE == tweenType) {
			sun.b = newValues[0];
		}
		if (ALPHA == tweenType) {
			sun.a = newValues[0];
		}
	}
}
