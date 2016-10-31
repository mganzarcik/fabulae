package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.audio.AudioProfile;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AudioProfileLoader extends SimpleAsynchronousLoader<AudioProfile, AudioProfileLoader.AudioProfileParameter> {
	
	public AudioProfileLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public AudioProfileLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public AudioProfile load (AssetManager assetManager, String fileName, FileHandle file, AudioProfileParameter parameter) {
		try {
			return new AudioProfile(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, AudioProfileParameter parameter) {
		return null;
	}

	static public class AudioProfileParameter extends AssetLoaderParameters<AudioProfile> {
	}

}
