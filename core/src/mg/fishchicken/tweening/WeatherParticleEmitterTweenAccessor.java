package mg.fishchicken.tweening;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.g2d.WeatherParticleEmitter;

public class WeatherParticleEmitterTweenAccessor implements TweenAccessor<WeatherParticleEmitter> {

	public static final int MAX_PARTICLE_COUNT = 1;
	
	@Override
	public int getValues(WeatherParticleEmitter target, int tweenType, float[] returnValues) {
		if (tweenType == MAX_PARTICLE_COUNT) {
			returnValues[0] = target.getMaxParticleCount();
			return 1;
		}
		return 0;
	}

	@Override
	public void setValues(WeatherParticleEmitter target, int tweenType, float[] newValues) {
		if (tweenType == MAX_PARTICLE_COUNT) {
			target.setMaxParticleCount((int)Math.ceil(newValues[0]));
		}
	}

}
