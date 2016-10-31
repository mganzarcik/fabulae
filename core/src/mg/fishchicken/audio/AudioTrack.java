package mg.fishchicken.audio;

import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.tweening.AudioTrackTweenAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class AudioTrack<T extends Disposable> implements AssetContainer, TweenCallback {
	private static final float DEFAULT_FADE_DURATION = 1; 
	private static final float MAX_BASE_VOLUME = 1f;
	
	private String s_filename;
	private int s_chanceToPlay;
	private AudioOriginator audioOriginator;
	private boolean s_replay;
	private boolean s_loop;
	private float s_volumeModifier;
	private float s_delay;
	protected float delayedPlayIn;
	private boolean isFadingOut;
	private boolean isFadingIn;
	private float volume;
	private float baseVolume;
	private Tween currentTween;
	
	public AudioTrack() {
		s_chanceToPlay = -1;
		s_replay = true;
		s_volumeModifier = 1;
		isFadingOut = isFadingIn = false;
		s_delay = 0f;
		s_loop = false;
		this.baseVolume = MAX_BASE_VOLUME;
	}

	protected abstract float getConfigurationVolumeModifier();
	
	public abstract void setTrack(T track);
	
	/**
	 * This will stop this AudioTrack from playing. 
	 * 
	 * It will decide whether it needs to fade it away, or just 
	 * stop it immediately.
	 */
	public abstract void stop(); 
	
	/**
	 * Stops the track immediately.
	 */
	abstract public void stopTrack(); 
	
	/**
	 * Plays the track immediately.
	 */
	protected abstract void playTrack(); 
		
	
	/**
	 * Gets the base volume the track should be playing at.
	 * @return
	 */
	public float getBaseVolume() {
		return baseVolume;
	}
	
	/**
	 * Sets the base volume the track should be playing at.
	 * 
	 * This is the volume that is used to calculate the actual 
	 * track volume by applying all modifiers to it.
	 * 
	 * @return
	 */
	public void setBaseVolume(float volume){
		baseVolume = volume;
	}

	public void setAudioOriginator(AudioOriginator audioOriginator) {
		this.audioOriginator = audioOriginator;
	}
	
	/**
	 * Updates this audiotrack in time. Used
	 * mostly for fading away tracks.
	 * 
	 * This should be called each frame.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime) {
		if (delayedPlayIn > 0) {
			delayedPlayIn -= deltaTime;
			if (delayedPlayIn <= 0) {
				delayedPlayIn = 0;
				playTrack();
			}
		}
		modifyVolume(); 
		if (currentTween != null) {
			currentTween.update(deltaTime);
		}
	}
	
	/**
	 * Will play this track if the rollResult was successful
	 * when compared to this track's chance to play.
	 * 
	 * @param rollResult
	 * @return true if the musiv will play, false otherwise
	 */
	public boolean playIfRollSuccessfull() {
		return playIfRollSuccessfull(null, MathUtils.random(100));
	}
	
	/**
	 * Will play this track if the rollResult was successful
	 * when compared to this track's chance to play.
	 * 
	 * @param rollResult
	 * @param ao
	 * @return true if the musiv will play, false otherwise
	 */
	public boolean playIfRollSuccessfull(AudioOriginator ao) {
		return playIfRollSuccessfull(ao, MathUtils.random(100));
	}
	
	private boolean playIfRollSuccessfull(AudioOriginator ao, int rollResult) {
		setAudioOriginator(ao);
		
		if (!shouldPlay(rollResult)) {
			return false;
		}
		play();
		
		return true;
	}
	
	/**
	 * Will play this sound effect.
	 * 
	 * The effect itself might start playing a little later
	 * depending on whether it has any delay defined.
	 * 
	 */
	public void play() {
		play(true);
	}
	
	/**
	 * Will play this sound effect as if it originated
	 * from the supplied originator.
	 * 
	 * The effect itself might start playing a little later
	 * depending on whether it has any delay defined.
	 * 
	 */
	public void play(AudioOriginator ao) {
		setAudioOriginator(ao);
		play();
	}
	
	/**
	 * Will play this sound effect.
	 * 
	 * The effect itself might start playing a little later
	 * depending on whether it has any delay defined.
	 * 
	 * @param resetVolume if true, the volume of the track will be reset
	 * to the default value before playback starts.
	 */
	public void play(boolean resetVolume) {
		if (resetVolume) {
			baseVolume = 1;
		}
		
		modifyVolume();
	
		if (s_delay <= 0) {
			playTrack();
		} else {
			delayedPlayIn = s_delay;
		}
	}
	
	protected boolean shouldPlay(int rollResult) {
		// if we already visited this location and should not replay, don't play
		if (!s_replay && audioOriginator!= null && audioOriginator.alreadyVisited()) {
			return false;
		}
		return s_chanceToPlay == -1 || rollResult < s_chanceToPlay;
	}
	
	public void setVolumeModifier(float modifier) {
		s_volumeModifier = modifier;
	}
	
	public void setVolume(float volume) {
		if (volume < 0) {
			volume = 0;
		}
		this.volume = volume;
	}
	
	public float getVolume() {
		return volume;
	}

	public void setFilename(String fileName) {
		s_filename = fileName;
	}
	
	public String getFilename() {
		return s_filename;
	}
	
	public void setChanceToPlay(int value) {
		s_chanceToPlay = value;
	}
	
	public boolean isLooping() {
		return s_loop;
	}
	
	public void setLooping(boolean value) {
		s_loop = value;
	}
	
	public void fadeOut() {
		fadeOut(DEFAULT_FADE_DURATION);
	}
	
	/** 
	 * This will slowly fade out the track from its current volume to 
	 * zero over the supplied duration, stopping it at the end.
	 * 
	 * @param duration
	 */
	public void fadeOut(float duration) {
		if (currentTween != null) {
			currentTween.free();
		}
		isFadingIn = false;
		isFadingOut = true;
		currentTween = Tween.to(this, AudioTrackTweenAccessor.VOLUME, duration).target(0).setCallback(this).start();
	}
	
	/**
	 * This will start playing the track, slowly fading it in from zero volume
	 * to the full volume. The length of the fading effect is supplied as a parameter.
	 * 
	 * Any delay defined is ignored.
	 * 
	 * @param duration
	 */
	public void fadeIn(float duration) {
		if (currentTween != null) {
			currentTween.free();
		}
		isFadingOut = false;
		isFadingIn = true;
		stopTrack(); // calling this to make sure we don't start playing twice
		volume = 0;
		baseVolume = 0;
		playTrack();
		currentTween = Tween.to(this, AudioTrackTweenAccessor.VOLUME, duration).target(MAX_BASE_VOLUME).setCallback(this).start();
	}
	
	public void initFrom(AudioTrack<?> track) {
		s_filename = track.s_filename;
		s_chanceToPlay = track.s_chanceToPlay;
		audioOriginator = track.audioOriginator;
		s_replay = track.s_replay;
		s_loop = track.s_loop;
		s_volumeModifier = track.s_volumeModifier;
		s_delay = track.s_delay;
		delayedPlayIn = track.delayedPlayIn;
		isFadingOut = false;
		isFadingIn = false;
		currentTween = null;
		baseVolume = MAX_BASE_VOLUME;
		modifyVolume();
	}
	
	public AudioTrack<T> createCopy() {
		try {
			@SuppressWarnings("unchecked")
			AudioTrack<T> copy = this.getClass().getConstructor().newInstance();
			copy.initFrom(this);
			return copy;
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		} 
	}
	
	protected void modifyVolume() {
		setVolume(modifyVolume(audioOriginator, baseVolume, s_volumeModifier));
	}
	
	/**
	 * Modifies the supplied volume based on the location of the supplied
	 * audioOriginator and its distance from the PCs. It also applies the
	 * premodifier to the volume before any further calculations. This allows
	 * the caller to specify a modifier that is independent of the audioOriginator state.
	 * 
	 * @param audioOriginator
	 * @param baseVolume
	 * @param premodifier
	 * @return
	 */
	public float modifyVolume(AudioOriginator audioOriginator, float baseVolume, float premodifier) {
		baseVolume = baseVolume * premodifier * getConfigurationVolumeModifier();
		if (audioOriginator == null || !audioOriginator.shouldModifyVolume()) {
			return baseVolume;
		}
		Vector2 soundOrigin = audioOriginator.getSoundOrigin();
		float soundRadius = audioOriginator.getSoundRadius();
		Camera camera = audioOriginator.getMap().getCamera();
		float mod = 1;
		
		// first modify the volume by the distance from the nearest PC
		float distance = audioOriginator.getDistanceToPlayer();
		if (distance > soundRadius+1) {
			mod = 0;
		} else {
			mod = 1 - ((1f / (soundRadius+1)) * distance);
		}
		baseVolume = baseVolume * mod;
		
		// and then by the distance from the center of the rendered screen
		if (baseVolume > 0) {
			Vector3 renderedCenter = audioOriginator.getMap().projectToTiles(camera.position.cpy());
			distance = soundOrigin.dst(renderedCenter.x, renderedCenter.y);
			float maxDistance = Configuration.getAudioOffScreenMaxDistance() + Math.max(camera.viewportHeight, camera.viewportWidth);
			if (distance > maxDistance) {
				mod = 0;
			} else {
				mod = 1 - ((1f / (float)maxDistance) * distance);
			}
			MathUtil.freeVector2(soundOrigin);
		}
		return baseVolume * mod;
	}
	
	@Override
	public void onEvent(int type, BaseTween<?> source) {
		if (type == TweenCallback.COMPLETE) {
			if (source.equals(currentTween)) {
				currentTween.free();
				currentTween = null;
				if (isFadingOut) {
					isFadingOut = false;
					stopTrack();
				} else if (isFadingIn) {
					isFadingIn = false;
				}
			}
		}
	}
	
	public void loadFromXML(Element trackElement) {
		XMLUtil.readPrimitiveMembers(this, trackElement);
		if (s_filename != null) {
			s_filename = Configuration.addModulePath(s_filename);
		}
	}
}
