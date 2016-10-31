package mg.fishchicken.tweening;

import mg.fishchicken.core.PositionedThing;
import aurelienribon.tweenengine.TweenAccessor;

public class PositionedThingTweenAccessor implements TweenAccessor<PositionedThing> {
	public static final int X = 1;
	public static final int Y = 2;
	public static final int XY = 3;

	@Override
	public int getValues(PositionedThing ac, int tweenType, float[] returnValues) {
		if (X == tweenType) {
			returnValues[0] =  ac.position().getX();
			return 1;
		}
		if (Y == tweenType) {
			returnValues[0] =  ac.position().getY();
			return 1;
		}
		if (XY == tweenType) {
			returnValues[0] =  ac.position().getX();
			returnValues[1] =  ac.position().getY();
			return 2;
		}
		return 0;
	}

	@Override
	public void setValues(PositionedThing ac, int tweenType, float[] newValues) {
		if (X == tweenType) {
			ac.position().setX(newValues[0]);
		}
		if (Y == tweenType) {
			ac.position().setY(newValues[0]);
		}
		if (XY == tweenType) {
			ac.position().set(newValues[0], newValues[1]);
		}
	}
}
