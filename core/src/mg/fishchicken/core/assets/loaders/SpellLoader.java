package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.magic.Spell;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SpellLoader extends SimpleAsynchronousLoader<Spell, SpellLoader.SpellParameter> {
	
	public SpellLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public SpellLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Spell load (AssetManager assetManager, String fileName, FileHandle file, SpellParameter parameter) {
		try {
			return new Spell(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, SpellParameter parameter) {
		return null;
	}

	static public class SpellParameter extends AssetLoaderParameters<Spell> {
	}

}
