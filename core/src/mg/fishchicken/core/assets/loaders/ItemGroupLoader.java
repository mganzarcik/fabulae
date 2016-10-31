package mg.fishchicken.core.assets.loaders;

import mg.fishchicken.gamelogic.inventory.items.ItemGroup;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class ItemGroupLoader extends SimpleAsynchronousLoader<ItemGroup, ItemGroupLoader.ItemGroupParameter> {
	
	public ItemGroupLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public ItemGroupLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ItemGroup load (AssetManager assetManager, String fileName, FileHandle file, ItemGroupParameter parameter) {
		/*try {
			return new ItemGroup(resolve(fileName));
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}*/
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ItemGroupParameter parameter) {
		return null;
	}

	static public class ItemGroupParameter extends AssetLoaderParameters<ItemGroup> {
	}

}
