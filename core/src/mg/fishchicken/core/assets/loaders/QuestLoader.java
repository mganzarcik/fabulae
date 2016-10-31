package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.quests.Quest;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class QuestLoader extends SimpleAsynchronousLoader<Quest, QuestLoader.QuestParameter> {
	
	public QuestLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public QuestLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Quest load (AssetManager assetManager, String fileName, FileHandle file, QuestParameter parameter) {
		try {
			return new Quest(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, QuestParameter parameter) {
		return null;
	}

	static public class QuestParameter extends AssetLoaderParameters<Quest> {
	}

}
