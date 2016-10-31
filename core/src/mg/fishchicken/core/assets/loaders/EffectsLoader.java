package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.effects.Effect;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class EffectsLoader extends SimpleAsynchronousLoader<Effect, EffectsLoader.EffectParameter> {
	
	public EffectsLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public EffectsLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Effect load (AssetManager assetManager, String fileName, FileHandle file, EffectParameter parameter) {
		try {
			return new Effect(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, EffectParameter parameter) {
		return null;
	}

	static public class EffectParameter extends AssetLoaderParameters<Effect> {
	}

}
