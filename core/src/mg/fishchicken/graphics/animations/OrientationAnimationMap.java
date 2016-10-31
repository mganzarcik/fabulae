package mg.fishchicken.graphics.animations;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.util.Orientation;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

public class OrientationAnimationMap extends AnimationMap<Orientation> {

	public OrientationAnimationMap(String animationTexturePath, FileHandle textureDescriptor) throws IOException {
		super(animationTexturePath, textureDescriptor);
	}

	@Override
	protected AnimationDescription readAnimationDescription(JsonValue line) {
		return new AnimationDescription(
			Orientation.valueOf(line.getString("orientation").trim().toUpperCase(Locale.ENGLISH)), 
			line
		);
	}
}
