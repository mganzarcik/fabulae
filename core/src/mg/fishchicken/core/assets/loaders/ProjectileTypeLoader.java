package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.projectiles.ProjectileType;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ProjectileTypeLoader extends SimpleAsynchronousLoader<ProjectileType, ProjectileTypeLoader.ProjectileTypeParameter> {
	
	public ProjectileTypeLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public ProjectileTypeLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ProjectileType load (AssetManager assetManager, String fileName, FileHandle file, ProjectileTypeParameter parameter) {
		try {
			return new ProjectileType(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, ProjectileTypeParameter parameter) {
		XmlReader xmlReader = new XmlReader();
		try {
			Array<AssetDescriptor>  returnValue = new Array<AssetDescriptor>();
			Element root = xmlReader.parse(file);
			LoaderUtil.handleImports(this, parameter, returnValue, file, root);
			String animationFile = root.get(ProjectileType.XML_ANIMATION_FILE, null);
			if (animationFile != null) {
				returnValue.add(new AssetDescriptor<Texture>(Configuration.addModulePath(animationFile), Texture.class));
			}
			Element soundsElement = root.getChildByName(XMLUtil.XML_SOUNDS);
			if (soundsElement != null) {
				addSoundDependency(soundsElement, ProjectileType.XML_ON_START, returnValue);
				addSoundDependency(soundsElement, ProjectileType.XML_ON_HIT, returnValue);
				addSoundDependency(soundsElement, ProjectileType.XML_DURING, returnValue);
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
	
	static public class ProjectileTypeParameter extends AssetLoaderParameters<ProjectileType> {
	}
}
