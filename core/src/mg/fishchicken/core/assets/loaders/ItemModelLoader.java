package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.graphics.models.ItemModel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ItemModelLoader extends SimpleAsynchronousLoader<ItemModel, ItemModelLoader.ModelParameter> {
	
	public ItemModelLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public ItemModelLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ItemModel load (AssetManager assetManager, String fileName, FileHandle file, ModelParameter parameter) {
		try {
			return new ItemModel(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ModelParameter parameter) {
		return null;
	}

	static public class ModelParameter extends AssetLoaderParameters<ItemModel> {
	}

}
