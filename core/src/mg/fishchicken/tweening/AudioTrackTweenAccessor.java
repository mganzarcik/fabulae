package mg.fishchicken.tweening;

import mg.fishchicken.audio.AudioTrack;
import aurelienribon.tweenengine.TweenAccessor;

public class AudioTrackTweenAccessor implements TweenAccessor<AudioTrack<?>> {

	public static final int VOLUME = 1;
	@Override
	public int getValues(AudioTrack<?> object, int tweenType, float[] returnValues) {
		if (tweenType == VOLUME) {
			returnValues[0] =  ((AudioTrack<?>)object).getBaseVolume();
		} 
		return 1;
	}

	@Override
	public void setValues(AudioTrack<?> object, int tweenType, float[] newValues) {
		if (tweenType == VOLUME) {
			((AudioTrack<?>)object).setBaseVolume(newValues[0]);
		} 
	}

}

