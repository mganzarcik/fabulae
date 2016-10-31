package mg.fishchicken.graphics.animations;

import mg.fishchicken.audio.AudioOriginator;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.core.util.GraphicsUtil;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Extension of the LibGDX Animation class that unfortunately could not extend
 * it directly, since some of the properties were final.
 * 
 **/
public class Animation {
	public static final int NORMAL = 0;
	public static final int REVERSED = 1;
	public static final int LOOP = 2;
	public static final int LOOP_REVERSED = 3;
	public static final int LOOP_PINGPONG = 4;
	public static final int LOOP_RANDOM = 5;

	final TextureRegion[] keyFrames;
	public float animationDuration;

	private float frameDuration;
	private float frameWidth, frameHeight;
	private int playMode;
	private int lastFrameSoundPlayedFor = -1;
	private ObjectMap<Integer, Rectangle> bounds = null;
	ObjectMap<Integer, AudioTrack<?>> sounds;
	
	public Animation(int frameWidth,
			int frameHeight, float frameDuration, ObjectMap<Integer, AudioTrack<?>> sounds, TextureRegion... keyFrames) {
		this(frameDuration, frameWidth, frameHeight, keyFrames);
		this.sounds = sounds;
		playMode = Animation.LOOP;
		setPlayMode(playMode);
	}
	
	public Animation(Texture animationTexture, int frameWidth,
			int frameHeight, float frameDuration, ObjectMap<Integer, AudioTrack<?>> sounds) {
		this(frameDuration, frameWidth, frameHeight, GraphicsUtil.flattenFrames(TextureRegion.split(
				animationTexture, frameWidth, frameHeight)));
		this.sounds = sounds;
		playMode = Animation.LOOP;
		setPlayMode(playMode);
	}
	

	/** Constructor, storing the frame duration and key frames.
	 * 
	 * @param frameDuration the time between frames in seconds.
	 * @param keyFrames the {@link TextureRegion}s representing the frames. */
	public Animation (float frameDuration, int frameWidth, int frameHeight, TextureRegion... keyFrames) {
		this.frameDuration = frameDuration;
		this.animationDuration = keyFrames.length * frameDuration;
		this.keyFrames = keyFrames;
		this.playMode = NORMAL;
		this.sounds = new ObjectMap<Integer, AudioTrack<?>>();
		this.frameHeight = frameHeight;
		this.frameWidth = frameWidth;
	}
	
	/**
	 * Sets the bounding rectangles of the animation. The supplied map should contain
	 * frame indexes as keys and rectangles in pixels as values.
	 * 
	 * @return
	 */
	public void setBounds(ObjectMap<Integer, Rectangle> bounds) {
		this.bounds = bounds;
	}
	
	/**
	 * Gets the current bounding rectangle of the animation, if defined for the current frame.
	 * 
	 * Can be null.
	 * @return
	 */
	public Rectangle getBounds(float stateTime) {
		if (bounds == null) {
			return null;
		}
		return bounds.get(getKeyFrameIndex(stateTime));
	}
	
	public int getNumberOfFrames() {
		return (int)(animationDuration / frameDuration);
	}
	
	public void setFrameDuration(float frameDuration) {
		this.frameDuration = frameDuration;
		this.animationDuration = keyFrames.length * frameDuration;
	}
	
	public float getFrameDuration() {
		return frameDuration;
	}
	
	public float getFrameWidth() {
		return frameWidth;
	}
	public float getFrameHeight() {
		return frameHeight;
	}
	
	public int getPlayMode() {
		return playMode;
	}
	public void setPlayMode(int playMode) {
		this.playMode = playMode;
	}
	
	/**
	 * This will play a sound if the animation has a sound defined for the
	 * current frame.
	 * 
	 * This should be called whenever the animation is rendered to ensure any
	 * associated sounds are be played as well.
	 * 
	 * @param deltaTime
	 */
	public void playSounds(float stateTime, AudioOriginator ao) {
		if (sounds.size == 0) {
			return;
		}
		
		int frameIndex = getKeyFrameIndex(stateTime);
		if (sounds.get(frameIndex) != null) {
			if (lastFrameSoundPlayedFor != frameIndex) {
				lastFrameSoundPlayedFor = frameIndex;
				sounds.get(frameIndex).play(ao);
			}
		} else {
			lastFrameSoundPlayedFor = -1;
		}
	}
	
	public void resetSounds() {
		lastFrameSoundPlayedFor = -1;
	}
	
	/** Returns a {@link TextureRegion} based on the so called state time. This is the amount of seconds an object has spent in the
	 * state this Animation instance represents, e.g. running, jumping and so on using the mode specified by
	 * {@link #setPlayMode(int)} method.
	 * 
	 * @param stateTime
	 * @return the TextureRegion representing the frame of animation for the given state time. */
	public TextureRegion getKeyFrame (float stateTime) {
		int frameNumber = getKeyFrameIndex (stateTime);
		return keyFrames[frameNumber];
	}
	
	/** Returns the current frame number.
	 * @param stateTime
	 * @return current frame number */
	public int getKeyFrameIndex (float stateTime) {
		int frameNumber = (int)(stateTime / frameDuration);

		if(keyFrames.length == 1)
         return 0;
		
		switch (playMode) {
		case NORMAL:
			frameNumber = Math.min(keyFrames.length - 1, frameNumber);
			break;
		case LOOP:
			frameNumber = frameNumber % keyFrames.length;
			break;
		case LOOP_PINGPONG:
			frameNumber = frameNumber % ((keyFrames.length * 2) - 2);
         if (frameNumber >= keyFrames.length)
            frameNumber = keyFrames.length - 2 - (frameNumber - keyFrames.length);
         break;
		case LOOP_RANDOM:
			frameNumber = MathUtils.random(keyFrames.length - 1);
			break;
		case REVERSED:
			frameNumber = Math.max(keyFrames.length - frameNumber - 1, 0);
			break;
		case LOOP_REVERSED:
			frameNumber = frameNumber % keyFrames.length;
			frameNumber = keyFrames.length - frameNumber - 1;
			break;

		default:
			// play normal otherwise
			frameNumber = Math.min(keyFrames.length - 1, frameNumber);
			break;
		}
		
		return frameNumber;
	}
	
	/** Whether the animation would be finished if played without looping (PlayMode Animation#NORMAL), given the state time.
	 * For lopped animations, it always returns false.
	 * @param stateTime
	 * @return whether the animation is finished. */
	public boolean isAnimationFinished (float stateTime) {
		if (playMode > REVERSED) {
			return false;
		}
		int frameNumber = (int)(stateTime / frameDuration);
		return keyFrames.length - 1 < frameNumber;
	}
}
