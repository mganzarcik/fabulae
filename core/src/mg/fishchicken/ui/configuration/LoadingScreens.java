package mg.fishchicken.ui.configuration;

import java.io.IOException;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Basic configuration for loading screens. Different types of loading screens
 * (for menus, for the module, for game maps) can have different settings.
 * 
 * The settings dictate what images will be used for the screens, whether
 * they will be shown randomly or at a sequence and at what interval (in seconds)
 * they should change when loading.
 * 
 * @author ANNUN
 *
 */
public class LoadingScreens implements XMLLoadable {
	public static final String XML_SCREEN = "screen";
	public static final String XML_IMAGE = "image";
	
	private ObjectMap<String, LoadingScreensImagesList> screensConfiguration;
	
	public LoadingScreens(FileHandle file) throws IOException {
		screensConfiguration = new ObjectMap<String, LoadingScreensImagesList>();
		loadFromXML(file);
	}
	
	/**
	 * Gets a configuration for the supplied loading screen type or null
	 * if no such configuration exists.
	 * 
	 * @param type
	 * @return
	 */
	public LoadingScreensImagesList getConfigurationForScreenType(String type) {
		return screensConfiguration.get(type);
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		loadFromXML(root);
	}
	
	private void loadFromXML(Element root) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			Element typeElement = root.getChild(i);
			screensConfiguration.put(typeElement.getName(), new LoadingScreensImagesList(typeElement));
		}
	}
	
	public static class LoadingScreensImagesList {
		private int s_changeInterval;
		private boolean s_random;
		private ObjectMap<String, Array<String>> images;
		
		private LoadingScreensImagesList(Element element) {
			s_changeInterval = -1;
			s_random = true;
			images = new ObjectMap<String, Array<String>>();
			loadFromXML(element);
		}
	
		/**
		 * Gets the time interval in seconds in which background images
		 * on the loading screen should change.
		 * @return
		 */
		public int getChangeInterval() {
			return s_changeInterval;
		}
		
		/**
		 * Returns true if the loading screen should pick its background randomly. 
		 * @return
		 */
		public boolean isRandom() {
			return s_random;
		}
		
		private void addImage(String imagePath) {
			addImage("default", imagePath);
		}
		
		private void addImage(String type, String imagePath) {
			if (!images.containsKey(type)) {
				images.put(type, new Array<String>());
			}
			images.get(type).add(imagePath);
		}
		
		/**
		 * Loads all default images into the supplied
		 * asset manager.
		 * 
		 * @param type
		 * @param am
		 */
		public void loadImages(AssetManager am) {
			loadImages("default", am);
		}
		
		/**
		 * Loads all images of the specified type (or default images
		 * if no type specific ones are found) into the supplied
		 * asset manager.
		 * 
		 * @param type
		 * @param am
		 */
		public void loadImages(String type, AssetManager am) {
			for (String image : getImages(type)) {
				am.load(image, Texture.class);
			}
		}
		
		/**
		 * Unloads all default images from the supplied
		 * asset manager.
		 * 
		 * @param type
		 * @param am
		 */
		public void unloadImages(AssetManager am) {
			unloadImages("default", am);
		}
		
		/**
		 * Unloads all images of the specified type (or default images
		 * if no type specific ones are found) from the supplied
		 * asset manager.
		 * 
		 * @param type
		 * @param am
		 */
		public void unloadImages(String type, AssetManager am) {
			for (String image : getImages(type)) {
				am.unload(image);
			}
		}

		/**
		 * Gets the default background images for the loading screen.
		 * 
		 * @return
		 */
		public Array<String> getImages() {
			return images.get("default");
		}
		
		/**
		 * Gets background images of a specified type for the loading screen,
		 * or the default images if no specific ones can be found.
		 * 
		 * @param type
		 * @return
		 */
		public Array<String> getImages(String type) {
			return images.containsKey(type) ? images.get(type) : getImages();
		}
		
		private void loadFromXML(Element screensElement) {
			XMLUtil.readPrimitiveMembers(this, screensElement);
			for (int i = 0; i < screensElement.getChildCount(); ++i) {
				Element child = screensElement.getChild(i);
				String childName = child.getName();
				if (XML_SCREEN.equals(childName)) {
					addImage(Configuration.addModulePath(child.get(XML_IMAGE)));
				} else {
					for (int j = 0; j < child.getChildCount(); ++j) {
						addImage(childName, Configuration.addModulePath(child.getChild(j).get(XML_IMAGE)));
					}
				}
			}
		}
	}

}
