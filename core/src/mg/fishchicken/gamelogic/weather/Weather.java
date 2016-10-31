package mg.fishchicken.gamelogic.weather;

import java.io.IOException;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.GraphicsUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.weather.WeatherManager.PrecipitationAmount;
import mg.fishchicken.gamelogic.weather.WeatherParticleEffectPool.PooledWeatherEffect;
import mg.fishchicken.tweening.WeatherParticleEmitterTweenAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Weather controls all effects related to the current weather.
 * 
 * It draws all the weather particles and controls the sounds.
 * 
 * When created, it decides itself whether it is rain or snow
 * based on the supplied temperature.
 * 
 * @author ANNUN
 *
 */
public class Weather implements TweenCallback, XMLSaveable {

	private WeatherRenderer renderer = new WeatherRenderer();
	private WeatherManager weatherManager;
	private PooledWeatherEffect weatherEffect;
	private PrecipitationAmount s_amount;
	private boolean s_isRain;
	private boolean s_isStopped;
	private boolean s_isStarting;
	private boolean s_isStopping;
	private int s_remainingDuration;
	private boolean isPaused;
	private AudioTrack<?> continuousSound;
	private Array<AudioTrack<?>> randomSounds;
	private float audioStateTimeSounds = 0;
	
	/**
	 * True if we should try to resize the weather effect on the first call of the
	 * Update method. This is set to true if the initial call to resize fails
	 * because the camera is null. This should only ever happen during game load.
	 */
	private boolean resizeOnUpdate = false;
	private Integer targetParticleCount;
	private TweenCallback additionalCallback;
	private Tween[] tweens;
	
	public Weather(Element element, WeatherManager weatherManager) throws IOException {
		this.weatherManager = weatherManager;
		loadFromXML(element);
	}
	
	public Weather(WeatherManager weatherManager, int temperature, PrecipitationAmount amount, WeatherProfile profile) {
		this(weatherManager, temperature, amount, MathUtils.random(profile.getPrecipitationDurationMin(), profile.getPrecipitationDurationMax()), profile);
	}
	
	
	public Weather(WeatherManager weatherManager, int temperature, PrecipitationAmount amount, int duration, WeatherProfile profile) {
		s_isRain = temperature >= Configuration.getSnowTemperatureThreshold();
		this.weatherManager = weatherManager;
		this.s_amount = amount;
		s_isStopped = true;
		targetParticleCount = null;
		s_remainingDuration = duration;
		initWeatherEffects(profile);
		mapChanged(weatherManager.gameState.getCurrentMap());
	}
	
	private void initWeatherEffects(WeatherProfile profile) {
		if (s_isRain) {
			weatherEffect = profile.getRainEffect(s_amount);
			randomSounds = profile.randomSoundsRain.get(s_amount);
		} else {
			weatherEffect = profile.getSnowEffect(s_amount);
			randomSounds = profile.randomSoundsSnow.get(s_amount);
		}
		
		continuousSound = profile.getContinousSound(s_amount, s_isRain);
		if (continuousSound != null) {
			continuousSound = continuousSound.createCopy();
			continuousSound.setLooping(true);
		}
		if (weatherEffect != null) {
			for (ParticleEmitter emitter : weatherEffect.getEmitters()) {
				emitter.setContinuous(true);
			}
			resizeFromCamera();
			targetParticleCount = getMaxParticleCount();
		}
	}
	
	public WeatherRenderer getRenderer() {
		return renderer;
	}
	
	public void resizeFromCamera() {
		GameMap currentMap = weatherManager.gameState.getCurrentMap();
		if ((currentMap == null || currentMap.getCamera() == null) && !resizeOnUpdate) {
			resizeOnUpdate = true;
			return;
		}
		Camera camera = currentMap.getCamera();
		resizeOnUpdate = false;
		if (weatherEffect != null) {
			stopAllTweens();
			// add 30 here to make sure the weather is a little larger
			// than the view area so that we don't see gaps with no particles
			// when scrolling
			float newSize = 30+Math.max(camera.viewportHeight, camera.viewportWidth);
			// resize everything to the new size, including our target
			// max number of particles
			float multiplier = GraphicsUtil.resize(weatherEffect.getEmitters(), newSize);
			if (targetParticleCount != null) {
				targetParticleCount = (int) (targetParticleCount * multiplier);
			}
			/*
			 * If we are currently running,
			 * we need to reset the effect to take
			 * the new values into consideration.
			 * 
			 * In addition, if we are actually just starting or stopping 
			 * (so there is a Tween in progress), we need to restart the Tween
			 * and start from beginning, since otherwise we would have
			 * a LOT of particles created at the same instant which 
			 * would cause the effect to fade in and fade out
			 * as a large number of particles is added and removed.
			 */
			if (!s_isStopped) {
				float duration = calculateTweenDuration(!s_isStarting);
				setMaxParticleCount(s_isStarting ? 0 : targetParticleCount);
				weatherEffect.reset();
				if (s_isStarting) {
					tween(duration, targetParticleCount, TweenEquations.easeNone);
				} else if (s_isStopping) {
					tween(duration, 0, TweenEquations.easeNone);
				}
			}
		}
	}
	
	public void setPosition(float x, float y) {
		if (weatherEffect != null) {
			if (GameState.isPaused()) {
				setAttached(true);
			}
			weatherEffect.setPosition(x, y);
			if (GameState.isPaused()) {
				setAttached(false);
			}
		}
	}
	
	private float calculateTweenDuration(boolean toStop) {
		float duration = toStop ? Configuration.getPrecipitationEndLenght() :  Configuration.getPrecipitationStartLenght();
		if (s_isStarting || s_isStopping) {
			if (targetParticleCount == 0) {
				return 0;
			}
			float coef = duration / targetParticleCount;  
			if (toStop) {
				duration = coef * getMaxParticleCount();
			} else {
				duration = coef * Math.abs(targetParticleCount- getMaxParticleCount());
			}
		}
		return duration;
	}
	
	private void setAttached(boolean value) {
		if (weatherEffect != null) {
			for (ParticleEmitter pe : weatherEffect.getEmitters()) {
				pe.setAttached(value);
			}
		}
	}
	
	/**
	 * Updates the weather effect. Only call
	 * this if you want to update but do not want 
	 * to draw the effect. Call draw otherwise.
	 * 
	 * @param batch
	 * @param deltaTime
	 */
	public void update(float deltaTime, GameMap map) {
		if (resizeOnUpdate) {
			resizeFromCamera();
		}
		if (isPaused || s_isStopped) {
			return;
		}
		if (weatherEffect != null) {
			updateTweenTargets();
			weatherEffect.update(deltaTime);
			updateSounds(deltaTime);
			updateDuration(deltaTime, map);
		} 
	}
	
	private void updateTweenTargets() {
		if (s_isStarting && tweens != null) {
			for (Tween tween : tweens) {
				tween.target(targetParticleCount);
			}
		}
	}
	
	private void updateSounds(float deltaTime) {
		if (continuousSound != null) {
			continuousSound.update(deltaTime);
		}
		
		if (randomSounds != null && randomSounds.size > 0 && !s_isStarting && !s_isStopping) {
			audioStateTimeSounds += deltaTime;
			
			if (audioStateTimeSounds > Configuration.getAudioUpdateIntervalWeather()) {
				audioStateTimeSounds = 0;
				AudioTrack<?> track = randomSounds.random();
				if (track != null) {
					track.playIfRollSuccessfull();
				}
			}
		}
	}
	
	private void updateDuration(float deltaTime, GameMap map) {
		if (!s_isStarting) {
			s_remainingDuration -= (deltaTime * map.getGameTimeMultiplier());
			if (s_remainingDuration < 0) {
				if (!s_isStopping) {
					stopGently();
				} 
			}
		} 
	}
	
	public boolean isSameAmount(Weather weatherToCompare) {
		return s_amount == weatherToCompare.s_amount;
	}
	
	public boolean isSameType(Weather weatherToCompare) {
		return (s_isRain && weatherToCompare.s_isRain) ||  (!s_isRain && !weatherToCompare.s_isRain);
	}
	
	public boolean isRain() {
		return s_isRain;
	}
	
	public void startImmediately() {
		if (weatherEffect != null) {
			s_isStopped = false;
			s_isStarting = false;
			s_isStopping = false;
			setMaxParticleCount(targetParticleCount);
			weatherEffect.start();
			if (continuousSound != null) {
				continuousSound.play();
			}
		}
	}
	
	public void startGently() {
		//System.out.println("Rain gently: starting: "+s_isStarting+"; stopping: "+s_isStopping+"; stopped: "+s_isStopped+"; paused: "+isPaused);
		if (!s_isStarting) {
			if (s_isRain) {
				Log.logLocalized("rainStart", LogType.WEATHER);
			} else {
				Log.logLocalized("snowStart", LogType.WEATHER);
			}
			if (weatherEffect != null) {
				setMaxParticleCount(0);
				weatherEffect.start();
				float duration = calculateTweenDuration(false);
				tween(duration, targetParticleCount, TweenEquations.easeInCubic);
				if (continuousSound != null) {
					continuousSound.fadeIn(duration);
				}
			}
			s_isStarting = true;
			s_isStopped = false;
		}
	}
	
	public void stopGently() {
		stopGently(null);
	}
	
	public void stopGently(TweenCallback callback) {
		//System.out.println("Stop gently: starting: "+s_isStarting+"; stopping: "+s_isStopping+"; stopped: "+s_isStopped+"; paused: "+isPaused);
		s_isStarting = false;
		if (!s_isStopping && !s_isStopped) {
			if (weatherEffect != null) {
				s_isStopping = true;
				additionalCallback = callback;
				float duration = calculateTweenDuration(true);
				//System.out.println("Stopping duration: "+duration);
				tween(duration, 0, TweenEquations.easeOutCubic);
				if (continuousSound != null) {
					continuousSound.fadeOut(duration);
				}
			} else {
				s_isStopped = true;
			}
		}
	}
	
	public void stopImmediately() {
		s_isStopped = true;
		s_isStopping = false;
		s_isStarting = false;
		stopAllTweens();
		if (continuousSound != null) {
			continuousSound.stopTrack();
		}
	}
	
	private void stopAllTweens() {
		tweens = null;
		for (ParticleEmitter pe : weatherEffect.getEmitters()) {
			weatherManager.getTweenManager().killTarget(pe);
		}
	}
	
	public void pause() {
		if (s_isStopped) {
			return;
		}
		isPaused = true;
		if (continuousSound != null) {
			continuousSound.stopTrack();
		}
	}
	
	public void resume() {
		if (s_isStopped) {
			return;
		}
		isPaused = false;
		if (continuousSound != null) {
			continuousSound.play(false);
		}
	}
	
	private void tween(float duration, int target, TweenEquation equation) {
		stopAllTweens();
		int i = 0;
		tweens = new Tween[weatherEffect.getEmitters().size];
		for (ParticleEmitter pe : weatherEffect.getEmitters()) {
			tweens[i++] = Tween
				.to(pe, WeatherParticleEmitterTweenAccessor.MAX_PARTICLE_COUNT, duration)
				.ease(equation)
				.target(target)
				.setCallback(this).start(weatherManager.getTweenManager());
		}
	}
	
	private int getMaxParticleCount() {
		if (weatherEffect != null && weatherEffect.getEmitters().size > 0) {
			return weatherEffect.getEmitters().get(0).getMaxParticleCount();
		}
		return 0;
	}
	
	private void setMaxParticleCount(int count) {
		if (weatherEffect != null) {
			for (ParticleEmitter pe : weatherEffect.getEmitters()) {
				pe.setMaxParticleCount(count);
			}
		}
	}
	
	public boolean isStarting() {
		return s_isStarting;
	}
	
	public boolean isStopping() {
		return false;
	}
	
	public boolean isStopped() {
		return s_isStopped;
	}

	@Override
	public void onEvent(int type, BaseTween<?> source) {
		if (TweenCallback.COMPLETE == type) {
			//System.out.println("On event: starting: "+s_isStarting+"; stopping: "+s_isStopping+"; stopped: "+s_isStopped+"; paused: "+isPaused);
			if (s_isStarting) {
				s_isStarting = false;
			} else if (s_isStopping) {
				if (s_isRain) {
					Log.logLocalized("rainStop", LogType.WEATHER);
				} else {
					Log.logLocalized("snowStop", LogType.WEATHER);
				}
				stopImmediately();
			}
			if (additionalCallback != null) {
				additionalCallback.onEvent(type, source);
			}
		}
	}
	
	public void free() {
		if (weatherEffect != null) {
			weatherEffect.free();
			weatherEffect = null;
		}
	}
	
	public void mapChanged(GameMap newMap) {
		if (continuousSound != null) {
			if (newMap == null) {
				continuousSound.stopTrack();
			} else if (newMap.isInterior()) {
				continuousSound.setVolumeModifier(Configuration.getInteriorSoundVolumeModifier());
			} else {
				continuousSound.setVolumeModifier(1);
			}
		}
	}

	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		XMLUtil.writePrimitives(this, writer);
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
		initWeatherEffects(weatherManager.determineCurrentWeatherProfile());
		if (!s_isStopped && weatherEffect != null) {
			startImmediately();
		}
	}
	
	public class WeatherRenderer {
		
		private WeatherRenderer() {
			
		}
		
		/**
		 * Updates and draws the weather effect.
		 * 
		 * @param batch
		 * @param deltaTime
		 */
		public void draw(GameMap map, Batch batch, float deltaTime) {
			if (resizeOnUpdate) {
				resizeFromCamera();
			}
			if (s_isStopped) {
				return;
			}
			if (weatherEffect != null) {
				if (!isPaused) {
					updateSounds(deltaTime);
					updateTweenTargets();
					weatherEffect.draw(map, batch, deltaTime);
					updateDuration(deltaTime, map);
				} else {
					weatherEffect.draw(map, batch);
				}
			}
		}
	}
}
