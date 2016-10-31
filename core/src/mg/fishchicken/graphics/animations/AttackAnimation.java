package mg.fishchicken.graphics.animations;

import mg.fishchicken.audio.AudioTrack;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;

public class AttackAnimation extends Animation {
	private int hitFrame;

	public AttackAnimation(int frameWidth, int frameHeight, float frameDuration,
			ObjectMap<Integer, AudioTrack<?>> sounds, int hitFrame,
			TextureRegion... keyFrames) {
		super(frameWidth, frameHeight, frameDuration, sounds, keyFrames);
		this.hitFrame = hitFrame;
	}
	
	public boolean targetHit(float stateTime) {
		return getKeyFrameIndex (stateTime) >= hitFrame;
	}

	public int getHitFrame() {
		return hitFrame;
	}

}
