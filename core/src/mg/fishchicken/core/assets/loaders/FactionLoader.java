package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.factions.Faction;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class FactionLoader extends SimpleAsynchronousLoader<Faction, FactionLoader.FactionParameter> {
	
	public FactionLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public FactionLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Faction load (AssetManager assetManager, String fileName, FileHandle file, FactionParameter parameter) {
		try {
			return new Faction(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, FactionParameter parameter) {
		return null;
	}

	static public class FactionParameter extends AssetLoaderParameters<Faction> {
	}

}
