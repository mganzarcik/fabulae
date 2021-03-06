package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.dialogue.Chatter;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ChatterLoader extends SimpleAsynchronousLoader<Chatter, ChatterLoader.ChatterParameter> {
	
	public ChatterLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public ChatterLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Chatter load (AssetManager assetManager, String fileName, FileHandle file, ChatterParameter parameter) {
		try {
			return new Chatter(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ChatterParameter parameter) {
		return null;
	}

	static public class ChatterParameter extends AssetLoaderParameters<Chatter> {
	}

}
