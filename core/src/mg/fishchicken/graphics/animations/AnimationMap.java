package mg.fishchicken.graphics.animations;

import java.io.IOException;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Sound;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.GraphicsUtil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public abstract class AnimationMap<T> implements AssetContainer {

	private ObjectMap<Integer, Array<AnimationDescription>> animationInfoMap;
	protected ObjectMap<T, Array<Animation>> animationMap;
	protected ObjectMap<T, Vector2> middleOffsets;
	private Vector2 middleOffset;
	protected int frameWidth;
	protected int frameHeight;
	protected float frameDuration;
	protected String animationTexturePath;
	private int objectWidth;
	private int objectHeight;

	public AnimationMap(String animationTexturePath, FileHandle textureDescriptor) throws IOException {
		animationInfoMap = readTextureDescriptor(textureDescriptor);
		animationMap = null;
		this.animationTexturePath = animationTexturePath;
	}
	
	public AnimationMap(AnimationMap<T> toCopy) {
		this.animationInfoMap = new ObjectMap<Integer, Array<AnimationDescription>>();
		
		for (Entry<Integer, Array<AnimationDescription>> entry : toCopy.animationInfoMap.entries()) {
			Array<AnimationDescription> descriptions = new Array<AnimationDescription>();
			animationInfoMap.put(entry.key, descriptions);
			for (AnimationDescription description : entry.value) {
				descriptions.add(description);
			}
		}
		
		frameWidth = toCopy.frameWidth;
		frameHeight = toCopy.frameHeight;
		frameDuration = toCopy.frameDuration;
		animationTexturePath = toCopy.animationTexturePath;
		middleOffsets = new ObjectMap<T, Vector2>(toCopy.middleOffsets);
		middleOffset = toCopy.middleOffset;
		objectWidth = toCopy.objectWidth;
		objectHeight = toCopy.objectHeight;
	}
	
	/**
	 * Whether this AnimationMap is fail fast. Fail fast animation maps will throw a runtime exception
	 * in case the required texture region is not available in the asset manager when the animations
	 * should be loaded.
	 * 
	 *  A non fail fast AnimationMap will just return null in that case.
	 * 
	 * @return
	 */
	protected boolean isFailFast() {
		return true;
	}

	private void loadAnimations() {
		
		if (!isFailFast() && !Assets.isLoaded(animationTexturePath)) {
			return;
		}
		
		TextureRegion[][] animationFrames = Assets.getTextureRegion(animationTexturePath)
				.split(frameWidth, frameHeight);

		for (int i = 0; i < animationFrames.length; ++i) {

			Array<AnimationDescription> animationInfos = animationInfoMap.get(i);
			if (animationInfos == null) {
				continue;
			}

			for (AnimationDescription animationInfo : animationInfos) {
				ObjectMap<Integer, AudioTrack<?>> loadedSounds = new ObjectMap<Integer, AudioTrack<?>>();
				if (animationInfo.sounds.size > 0) {
					for (Integer soundIndex : animationInfo.sounds.keys()) {
						loadedSounds.put(soundIndex, getTrack(animationInfo.sounds.get(soundIndex)));
					}
				}

				Animation animation = createAnimationFromInfo(i, animationFrames, loadedSounds, animationInfo);

				if (animationMap == null) {
					animationMap = new ObjectMap<T, Array<Animation>>();
				}

				Array<Animation> animationList = null;
				if (animationMap.containsKey(animationInfo.key)) {
					animationList = animationMap.get(animationInfo.key);
				} else {
					animationList = new Array<Animation>();
					animationMap.put(animationInfo.key, animationList);
				}
				animationList.add(animation);
			}
		}
	}

	/**
	 * Gets a new instance of the AudioTrack with the supplied identifier.
	 * 
	 * Default identifier assumes the identifier is a file path.
	 * 
	 * @param identifier
	 * @return
	 */
	protected AudioTrack<?> getTrack(String identifier) {
		return new Sound(Configuration.addModulePath(identifier));
	}

	protected Animation createAnimationFromInfo(int frameIndex, TextureRegion[][] animationFrames,
			ObjectMap<Integer, AudioTrack<?>> sounds, AnimationDescription animationInfo) {
		Animation animation = new Animation(frameWidth, frameHeight, frameDuration, sounds, GraphicsUtil.getFrames(
				animationFrames[frameIndex], animationInfo.startingFrame, animationInfo.numberOfFrames));
		animation.setPlayMode(animationInfo.playMode);
		animation.setBounds(animationInfo.bounds);
		return animation;
	}

	public Vector2 getMiddleOffset(T key) {
		Vector2 offset = middleOffsets.get(key);
		return offset != null ? offset : middleOffset;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public int getObjectWidth() {
		return objectWidth;
	}

	public int getObjectHeight() {
		return objectHeight;
	}

	/**
	 * Returns the Animation belonging to the given key. If more
	 * than one animation is defined for this pair, a random one is returned.
	 * 
	 * @param orientation
	 * @param loadIfRequired
	 *            - if true, the animations will be loaded from the asset
	 *            manager if they are not already present in the map. If false,
	 *            null will be returned if the animations are not already
	 *            loaded, of if they were disposed.
	 * @return
	 */
	public Animation getAnimation(T key, boolean loadIfRequired) {
		if (animationMap == null && loadIfRequired) {
			loadAnimations();
		} 
		
		if (animationMap == null) {
			return null;
		}

		Array<Animation> animations = animationMap.get(key);
		if (animations == null) {
			return null;
		}
		return animations.random();
	}

	public ObjectMap<Integer, Array<AnimationDescription>> readTextureDescriptor(FileHandle textureDescriptor)
			throws IOException {

		JsonValue descriptor = new JsonReader().parse(textureDescriptor);

		JsonValue frame = descriptor.get("frame");
		frameWidth = frame.getInt("width");
		frameHeight = frame.getInt("height");
		frameDuration = 1 / frame.getFloat("fps");

		JsonValue offset = descriptor.get("middleOffset");
		middleOffset = new Vector2(offset.getInt("x", 0), offset.getInt("y", 0));
		middleOffsets = new ObjectMap<T, Vector2>();

		JsonValue object = descriptor.get("object");
		objectWidth = object.getInt("width");
		objectHeight = object.getInt("height");

		ObjectMap<Integer, Array<AnimationDescription>> returnValue = new ObjectMap<Integer, Array<AnimationDescription>>();
		for (JsonValue animation : descriptor.get("animations")) {
			readAnimation(animation, returnValue);
		}
		return returnValue;
	}

	private void readAnimation(JsonValue line, ObjectMap<Integer, Array<AnimationDescription>> animationInfoMap) {
		int animationLineNumber = line.getInt("line");

		AnimationDescription ad = readAnimationDescription(line);

		Array<AnimationDescription> lineAnimations = animationInfoMap.get(animationLineNumber);
		if (lineAnimations == null) {
			lineAnimations = new Array<AnimationDescription>();
			animationInfoMap.put(animationLineNumber, lineAnimations);
		}

		lineAnimations.add(ad);
	}

	protected abstract AnimationDescription readAnimationDescription(JsonValue line);

	protected ObjectMap<Integer, String> readSoundInfo(JsonValue soundInfo) {
		ObjectMap<Integer, String> mapToStore = new ObjectMap<Integer, String>();

		if (soundInfo == null) {
			return mapToStore;
		}

		for (JsonValue sound : soundInfo) {
			mapToStore.put(Integer.valueOf(sound.name().trim()), sound.asString());
		}

		return mapToStore;
	}

	protected Array<String> split(String orig, char delimiter, char startingBracket, char endBracket) {
		Array<String> splitted = new Array<String>();
		int skipCommas = 0;
		String s = "";
		for (char c : orig.toCharArray()) {
			if (c == delimiter && skipCommas == 0) {
				splitted.add(s);
				s = "";
			} else {
				if (c == startingBracket)
					skipCommas++;
				if (c == endBracket)
					skipCommas--;
				s += c;
			}
		}
		splitted.add(s);
		return splitted;
	}

	class AnimationDescription {
		final T key;
		final int startingFrame;
		final int numberOfFrames;
		final int playMode;
		final ObjectMap<Integer, String> sounds;
		final ObjectMap<Integer, Rectangle> bounds = new ObjectMap<Integer, Rectangle>();

		public AnimationDescription(T key, JsonValue line) {
			this.key = key;
			this.startingFrame = line.getInt("start");
			this.numberOfFrames = line.getInt("count");
			this.playMode = line.getInt("mode");
			this.sounds = readSoundInfo(line.get("sounds"));
			JsonValue offset = line.get("middleOffset");
			if (offset != null) {
				middleOffsets.put(key, new Vector2(offset.getInt("x"), offset.getInt("y")));
			}
			JsonValue bounds = line.get("bounds");
			if (bounds != null) {
				for (JsonValue rectangle : bounds) {
					this.bounds.put(Integer.valueOf(rectangle.name().trim()), new Rectangle(rectangle.getInt("x"),
							rectangle.getInt("y"), rectangle.getInt("width"), rectangle.getInt("height")));
				}
			}
		}
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
		assetStore.put(animationTexturePath, Texture.class);
		for (Array<AnimationDescription> animationInfos : animationInfoMap.values()) {
			for (AnimationDescription animationInfo : animationInfos) {
				if (animationInfo.sounds != null) {
					for (String sound : animationInfo.sounds.values()) {
						assetStore.put(Configuration.addModulePath(sound), com.badlogic.gdx.audio.Sound.class);
					}
				}
			}
		}
	}

	@Override
	public void clearAssetReferences() {
		animationMap = null;
	}
}
