package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.weather.WeatherProfile;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class WeatherProfileLoader extends SimpleAsynchronousLoader<WeatherProfile, WeatherProfileLoader.WeatherProfileParameter> {
	
	public WeatherProfileLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public WeatherProfileLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public WeatherProfile load (AssetManager assetManager, String fileName, FileHandle file, WeatherProfileParameter parameter) {
		try {
			return new WeatherProfile(file);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, WeatherProfileParameter parameter) {
		Array<AssetDescriptor> returnValue = new Array<AssetDescriptor>();
		try {
			XmlReader xmlReader = new XmlReader();
			Element root = xmlReader.parse(file);
			LoaderUtil.handleImports(this, parameter, returnValue, file, root);
			Array<Element> trackElements = root.getChildrenByNameRecursively(WeatherProfile.XML_TRACK);
			for (Element trackElement : trackElements) {
				String trackFileName = Configuration.addModulePath(trackElement.get(XMLUtil.XML_ATTRIBUTE_FILENAME));
				returnValue.add(new AssetDescriptor(trackFileName, WeatherProfile.XML_CONTINOUS.equalsIgnoreCase(trackElement.getParent().getName()) ? Music.class : Sound.class)); 
			}
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
		
		return returnValue;
	}

	static public class WeatherProfileParameter extends AssetLoaderParameters<WeatherProfile> {
	}

}
