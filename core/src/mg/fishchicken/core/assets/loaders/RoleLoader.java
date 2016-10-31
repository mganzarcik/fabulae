package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.Role;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class RoleLoader extends SimpleAsynchronousLoader<Role, RoleLoader.RoleParameter> {
	
	public RoleLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public RoleLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Role load (AssetManager assetManager, String fileName, FileHandle file, RoleParameter parameter) {
		try {
			return new Role(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, RoleParameter parameter) {
		return null;
	}

	static public class RoleParameter extends AssetLoaderParameters<Role> {
	}

}
