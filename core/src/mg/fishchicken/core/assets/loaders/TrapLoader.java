package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.traps.TrapType;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class TrapLoader extends SimpleAsynchronousLoader<TrapType, TrapLoader.TrapParameter> {
	
	public TrapLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public TrapLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public TrapType load (AssetManager assetManager, String fileName, FileHandle file, TrapParameter parameter) {
		try {
			return new TrapType(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, TrapParameter parameter) {
		XmlReader xmlReader = new XmlReader();
		try {
			Array<AssetDescriptor>  returnValue = new Array<AssetDescriptor>();
			Element root = xmlReader.parse(file);
			LoaderUtil.handleImports(this, parameter, returnValue, file, root);
			Element soundsElement = root.getChildByName(XMLUtil.XML_SOUNDS);
			if (soundsElement != null) {
				addSoundDependency(soundsElement, TrapType.XML_DISARMED, returnValue);
				addSoundDependency(soundsElement, TrapType.XML_SPRUNG, returnValue);
			}
			if (returnValue.size > 0) {
				return returnValue;
			}
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private void addSoundDependency(Element soundsElement, String soundElementName, Array<AssetDescriptor> dependencies) {
		Element soundElement = soundsElement.getChildByName(soundElementName);
		if (soundElement != null && soundElement.getChildCount() > 0) {
			for (int i = 0; i < soundElement.getChildCount(); ++i) {
				dependencies.add(new AssetDescriptor<Sound>(Configuration.addModulePath(soundElement.getChild(i).get(XMLUtil.XML_FILENAME)), Sound.class));
			}
		}
	}

	static public class TrapParameter extends AssetLoaderParameters<TrapType> {
	}

}
