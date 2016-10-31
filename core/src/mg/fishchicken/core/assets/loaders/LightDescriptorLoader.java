package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.graphics.lights.LightDescriptor;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class LightDescriptorLoader extends SimpleAsynchronousLoader<LightDescriptor, LightDescriptorLoader.LightDescriptorParameter> {
	
	public LightDescriptorLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public LightDescriptorLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public LightDescriptor load (AssetManager assetManager, String fileName, FileHandle file, LightDescriptorParameter parameter) {
		try {
			return new LightDescriptor(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, LightDescriptorParameter parameter) {
		return null;
	}

	static public class LightDescriptorParameter extends AssetLoaderParameters<LightDescriptor> {
	}
}
