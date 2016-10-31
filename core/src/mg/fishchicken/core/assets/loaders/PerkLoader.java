package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.perks.Perk;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PerkLoader extends SimpleAsynchronousLoader<Perk, PerkLoader.PerkParameter> {
	
	public PerkLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public PerkLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Perk load (AssetManager assetManager, String fileName, FileHandle file, PerkParameter parameter) {
		try {
			return new Perk(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, PerkParameter parameter) {
		return null;
	}

	static public class PerkParameter extends AssetLoaderParameters<Perk> {
	}

}
