package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.story.StorySequence;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class StorySequenceLoader extends SimpleAsynchronousLoader<StorySequence, StorySequenceLoader.StorySequenceParameter> {
	
	public StorySequenceLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public StorySequenceLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public StorySequence load (AssetManager assetManager, String fileName, FileHandle file, StorySequenceParameter parameter) {
		try {
			return new StorySequence(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, StorySequenceParameter parameter) {
		return null;
	}

	static public class StorySequenceParameter extends AssetLoaderParameters<StorySequence> {
	}

}
