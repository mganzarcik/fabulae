package mg.fishchicken.tweening;

import mg.fishchicken.core.PositionedThing;
import box2dLight.Light;

public class LightTweenAccessor extends PositionedThingTweenAccessor {

	public static final int DISTANCE = 4;
	@Override
	public int getValues(PositionedThing object, int tweenType, float[] returnValues) {
		if (tweenType == DISTANCE) {
			returnValues[0] =  ((Light)object).getDistance();
		} else {
			return super.getValues(object, tweenType, returnValues);
		}
		return 1;
	}

	@Override
	public void setValues(PositionedThing object, int tweenType, float[] newValues) {
		if (tweenType == DISTANCE) {
			((Light)object).setDistance(newValues[0]);
		} else {
			super.setValues(object, tweenType, newValues);
		}
	}

}
