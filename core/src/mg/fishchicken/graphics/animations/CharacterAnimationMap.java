package mg.fishchicken.graphics.animations;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.EmptyTrack;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.util.GraphicsUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.Pair;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter.State;
import mg.fishchicken.graphics.models.Model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

public class CharacterAnimationMap extends AnimationMap<Pair<String, Orientation>> {

	private static final Array<String> WALK_STATES = new Array<String>(new String[]{State.WALK, State.SNEAKING});
	
	private Pool<Pair<String, Orientation>> keyPairPool = new Pool<Pair<String, Orientation>>() {
		@Override
		protected Pair<String, Orientation> newObject () {
			return new Pair<String, Orientation>();
		}
	};
	
	private float characterSpeed; 
	private AudioProfile audioProfile;
	
	public CharacterAnimationMap(Model model, AudioProfile audioProfile, float speed) throws IOException {
		super(model.getAnimationTextureFile(), Gdx.files.internal(model.getAnimationInfoFile()));
		characterSpeed = speed;
		this.audioProfile = audioProfile;
	}
	
	public CharacterAnimationMap(CharacterAnimationMap toCopy) {
		super(toCopy);
		this.characterSpeed = toCopy.characterSpeed;
		this.audioProfile = toCopy.audioProfile;
	}
	
	@Override
	protected AudioTrack<?> getTrack(String identifier) {
		return audioProfile != null ? audioProfile.getTrack(identifier) : EmptyTrack.INSTANCE;
	}
	
	@Override
	protected Animation createAnimationFromInfo(
			int frameIndex,
			TextureRegion[][] animationFrames,
			ObjectMap<Integer, AudioTrack<?>> sounds,
			AnimationMap<Pair<String, Orientation>>.AnimationDescription animationInfo) {
		CharacterAnimationDescription  myAnimationInfo = (CharacterAnimationDescription) animationInfo;
		Animation animation = null;
		String state = myAnimationInfo.key.getLeft();
		if (WALK_STATES.contains(state, true)) {
			float duration = (float) (myAnimationInfo.numberOfSteps * myAnimationInfo.tilesTravelledPerStep)
					/ (myAnimationInfo.framesPerStep * characterSpeed);
			animation = new WalkAnimation(frameWidth, frameHeight,
					duration, sounds,
					myAnimationInfo.numberOfSteps,
					myAnimationInfo.framesPerStep,
					myAnimationInfo.tilesTravelledPerStep,
					GraphicsUtil.getFrames(animationFrames[frameIndex],
							animationInfo.startingFrame, 
							animationInfo.numberOfFrames));
		} else if (State.ATTACKMELEE.equals(state) || State.ATTACKRANGED.equals(state)) {
			animation = new AttackAnimation(frameWidth,
					frameHeight, frameDuration, sounds,
					myAnimationInfo.hitFrame, GraphicsUtil.getFrames(
							animationFrames[frameIndex],
							animationInfo.startingFrame,
							animationInfo.numberOfFrames));
		}else {
			animation = new Animation(frameWidth, frameHeight,
					frameDuration, sounds, GraphicsUtil.getFrames(
							animationFrames[frameIndex],
							animationInfo.startingFrame,
							animationInfo.numberOfFrames));
		}
		animation.setPlayMode(animationInfo.playMode);
		animation.setBounds(animationInfo.bounds);
		return animation;
	}
	
	/**
	 * Sets the duration of all walk animations
	 * according to the supplied speed.
	 * 
	 * @param speed
	 */
	public void setSpeed(float speed) {
		characterSpeed = speed;
		if (animationMap == null) {
			return;
		}
		Pair<String, Orientation> key = keyPairPool.obtain();
		
		for (Orientation orientation : Orientation.values()) {
			for (String walkState : WALK_STATES) {
				key.setLeft(walkState);
				key.setRight(orientation);
				Array<Animation> animations = animationMap.get(key);
				if (animations == null) {
					continue;
				}
				for (Animation animation : animations) {
					if (animation instanceof WalkAnimation) {
						((WalkAnimation)animation).recomputeDurationBasedOnSpeed(speed);
					}
				}
			}
		}
		
		keyPairPool.free(key);
	}
	
	/**
	 * Returns the Animation belonging to the given state and orientation. If more
	 * than one animation is defined for this pair, a random one is returned.
	 * 
	 * @param state
	 * @param orientation
	 * @param loadIfRequired - if true, the animations will be loaded from the
	 * asset manager if they are not already present in the map. If false, null
	 * will be returned if the animations are not already loaded, of if they were
	 * disposed.
	 * @return
	 */
	public Animation getAnimation(String state, Orientation orientation, boolean loadIfRequired) {
		Pair<String, Orientation> key = keyPairPool.obtain();
		key.setLeft(state.toLowerCase(Locale.ENGLISH));
		key.setRight(orientation);
		Animation animation = super.getAnimation(key, loadIfRequired);
		keyPairPool.free(key);
		return animation;
	}
	
	/**
	 * Returns the MiddleOffset belonging to the given state and orientation.
	 * 
	 * @param state
	 * @param orientation
	 * @return
	 */
	public Vector2 getMiddleOffset(String state, Orientation orientation) {
		Pair<String, Orientation> key = keyPairPool.obtain();
		key.setLeft(state.toLowerCase(Locale.ENGLISH));
		key.setRight(orientation);
		Vector2 offset = super.getMiddleOffset(key);
		keyPairPool.free(key);
		return offset;
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		assetStore.put(animationTexturePath, Texture.class);
	}
	
	@Override
	protected AnimationMap<Pair<String, Orientation>>.AnimationDescription readAnimationDescription(
			JsonValue line) {
		return new CharacterAnimationDescription( 
			new Pair<String, Orientation>(
					line.getString("state").trim().toLowerCase(Locale.ENGLISH),
					Orientation.valueOf(line.getString("orientation").trim().toUpperCase(Locale.ENGLISH))
			), 
			line
		);
	}
	
	class CharacterAnimationDescription extends AnimationMap<Pair<String, Orientation>>.AnimationDescription {
		final int numberOfSteps;
		final int framesPerStep;
		final int hitFrame;
		final float tilesTravelledPerStep;

		public CharacterAnimationDescription(Pair<String, Orientation> key, JsonValue line) {
			super(key, line);
			this.numberOfSteps = line.getInt("numberOfSteps", 0);
			this.framesPerStep = line.getInt("framesPerStep", 0);
			this.tilesTravelledPerStep = line.getFloat("tilesTravelledPerStep", 0);
			this.hitFrame = line.getInt("hitFrame", 0);
		}
	}

	
}
