package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.Race;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class RaceLoader extends SimpleAsynchronousLoader<Race, RaceLoader.RaceParameter> {
	
	public RaceLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public RaceLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Race load (AssetManager assetManager, String fileName, FileHandle file, RaceParameter parameter) {
		try {
			return new Race(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, RaceParameter parameter) {
		return null;
	}

	static public class RaceParameter extends AssetLoaderParameters<Race> {
	}

}
