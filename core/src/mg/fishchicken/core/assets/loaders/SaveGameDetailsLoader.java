package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.ui.saveload.SaveGameDetails;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SaveGameDetailsLoader extends SimpleAsynchronousLoader<SaveGameDetails, SaveGameDetailsLoader.SaveGameDetailsParameter> {
	
	public SaveGameDetailsLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public SaveGameDetailsLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public SaveGameDetails load (AssetManager assetManager, String fileName, FileHandle file, SaveGameDetailsParameter parameter) {
		try {
			return new SaveGameDetails(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, SaveGameDetailsParameter parameter) {
		return null;
	}

	static public class SaveGameDetailsParameter extends AssetLoaderParameters<SaveGameDetails> {
	}
}
