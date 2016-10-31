package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.AIScript;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AIScriptsLoader extends SimpleAsynchronousLoader<AIScript, AIScriptsLoader.ScriptParameter> {
	
	public AIScriptsLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public AIScriptsLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public AIScript load (AssetManager assetManager, String fileName, FileHandle file, ScriptParameter parameter) {
		try {
			return new AIScript(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ScriptParameter parameter) {
		return null;
	}

	static public class ScriptParameter extends AssetLoaderParameters<AIScript> {
	}
}
