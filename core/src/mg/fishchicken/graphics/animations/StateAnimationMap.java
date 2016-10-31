package mg.fishchicken.graphics.animations;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.Pair;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;

public class StateAnimationMap extends AnimationMap<Pair<String, Orientation>> {

	private Pool<Pair<String, Orientation>> keyPairPool = new Pool<Pair<String, Orientation>>() {
		@Override
		protected Pair<String, Orientation> newObject() {
			return new Pair<String, Orientation>();
		}
	};

	public StateAnimationMap(String animationTexturePath, FileHandle textureDescriptor) throws IOException {
		super(animationTexturePath, textureDescriptor);
	}

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
	protected AnimationDescription readAnimationDescription(JsonValue line) {
		return new AnimationDescription( 
			new Pair<String, Orientation>(
				line.getString("state").trim(),
				Orientation.valueOf(line.getString("orientation").trim().toUpperCase(Locale.ENGLISH))
			), 
			line
		);
	}
}
