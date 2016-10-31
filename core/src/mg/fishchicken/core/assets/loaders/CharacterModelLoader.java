package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.graphics.models.CharacterModel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class CharacterModelLoader extends SimpleAsynchronousLoader<CharacterModel, CharacterModelLoader.ModelParameter> {
	
	public CharacterModelLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public CharacterModelLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public CharacterModel load (AssetManager assetManager, String fileName, FileHandle file, ModelParameter parameter) {
		try {
			return new CharacterModel(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ModelParameter parameter) {
		return null;
	}

	static public class ModelParameter extends AssetLoaderParameters<CharacterModel> {
	}

}
