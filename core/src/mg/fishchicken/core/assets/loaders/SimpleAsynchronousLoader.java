package mg.fishchicken.core.assets.loaders;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public abstract class SimpleAsynchronousLoader<TYPE, PARAMETER extends AssetLoaderParameters<TYPE>>
		extends AsynchronousAssetLoader<TYPE, PARAMETER> {

	private TYPE loadedObject;
	
	public SimpleAsynchronousLoader(FileHandleResolver resolver) {
		super(resolver);
	}
	
	@Override
	public void loadAsync(AssetManager manager, String fileName,
			FileHandle file, PARAMETER parameter) {
		loadedObject = load(manager, fileName, file, parameter);
	}
	
	@Override
	public TYPE loadSync(AssetManager manager, String fileName,
			FileHandle file, PARAMETER parameter) {
		TYPE returnValue = loadedObject;
		loadedObject = null;
		return returnValue;
	}
	
	public abstract TYPE load (AssetManager assetManager, String fileName, FileHandle file, PARAMETER parameter);

}
