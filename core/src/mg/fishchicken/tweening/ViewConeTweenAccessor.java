package mg.fishchicken.tweening;

import aurelienribon.tweenengine.TweenAccessor;
import box2dLight.Light;
import box2dLight.ViewConeLight;

public class ViewConeTweenAccessor implements TweenAccessor<ViewConeLight> {

	public static final int DIRECTION = 1;
	@Override
	public int getValues(ViewConeLight object, int tweenType, float[] returnValues) {
		if (tweenType == DIRECTION) {
			returnValues[0] =  ((ViewConeLight)object).getDirection();
		}
		return 1;
	}

	@Override
	public void setValues(ViewConeLight object, int tweenType, float[] newValues) {
		if (tweenType == DIRECTION) {
			((Light)object).setDirection(newValues[0]);
		} 
	}

}
