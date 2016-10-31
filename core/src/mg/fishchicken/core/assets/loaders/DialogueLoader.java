package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.gamelogic.dialogue.Dialogue;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DialogueLoader extends SimpleAsynchronousLoader<Dialogue, DialogueLoader.DialogueParameter> {
	
	private Dialogue dialogue;
	
	public DialogueLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public DialogueLoader (FileHandleResolver resolver) {
		super(resolver);
	}
	
	@Override
	public Dialogue load(AssetManager manager, String fileName,
			FileHandle file, DialogueParameter parameter) {
		this.dialogue = new Dialogue();
		try {
			dialogue.loadFromXML(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
		return dialogue;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, DialogueParameter parameter) {
		return null;
	}

	static public class DialogueParameter extends AssetLoaderParameters<Dialogue> {
	}

}
