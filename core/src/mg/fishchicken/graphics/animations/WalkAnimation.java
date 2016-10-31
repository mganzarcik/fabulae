package mg.fishchicken.graphics.animations;

import mg.fishchicken.audio.AudioTrack;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;

public class WalkAnimation extends Animation {
	private int numberOfSteps;
	private int framesPerStep;
	private float tilesTravelledPerStep;

	public WalkAnimation(int frameWidth, int frameHeight, float frameDuration,
			ObjectMap<Integer, AudioTrack<?>> sounds, int numberOfSteps,
			int framesPerStep, float tilesTravelledPerStep,
			TextureRegion... keyFrames) {
		super(frameWidth, frameHeight, frameDuration, sounds, keyFrames);
		this.numberOfSteps = numberOfSteps;
		this.framesPerStep = framesPerStep;
		this.tilesTravelledPerStep = tilesTravelledPerStep;
	}


	public int getNumberOfSteps() {
		return numberOfSteps;
	}


	public int getFramesPerStep() {
		return framesPerStep;
	}


	public float getTilesTravelledPerStep() {
		return tilesTravelledPerStep;
	}
	
	/**
	 * Recomputes the frame duration of this walk animation
	 * based on the supplied character speed.
	 * 
	 * @param speed
	 */
	public void recomputeDurationBasedOnSpeed(float speed) {
		setFrameDuration((float) (numberOfSteps * tilesTravelledPerStep)
				/ (framesPerStep * speed));
	}

}
