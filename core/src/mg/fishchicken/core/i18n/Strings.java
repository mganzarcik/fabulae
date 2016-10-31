package mg.fishchicken.core.i18n;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * A Util class for i8n.
 * 
 * Contains most of the localization logic.
 * 
 * @author ANNUN
 *
 */
public class Strings {

	public static final String RESOURCE_FILE_EXTENSION= "json";
	public static final String RESOURCE_STRING_KEY_SEPARATOR = "#";
	private static final String RESOURCE_STRING_SEPARATOR = "."+RESOURCE_FILE_EXTENSION+RESOURCE_STRING_KEY_SEPARATOR;
	private static final int RESOURCE_STRING_SEPARATOR_LENGTH = RESOURCE_STRING_SEPARATOR.length();
	public static final String REPLACEMENT_STRING = "%s";
	/**
	 * Gathers all Strings and registers them in the AssetManager
	 * so that they can be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherStringResources() throws IOException {
		Assets.gatherAssets(Configuration.getFolderStringResources(), RESOURCE_FILE_EXTENSION, StringTable.class, null);
	}
	
	/**
	 * Returns a localized version of the supplied key.
	 * @param key
	 * @return
	 */
	public static String getString(String string, Object... parameters) {
		if (string == null) {
			return "";
		}
		if (string.contains(RESOURCE_STRING_SEPARATOR)) {
			String resourceFilePath = string.substring(0, string.lastIndexOf(RESOURCE_STRING_SEPARATOR))+"."+RESOURCE_FILE_EXTENSION;
			String resourceKey = string.substring(string.lastIndexOf(RESOURCE_STRING_SEPARATOR)+RESOURCE_STRING_SEPARATOR_LENGTH, string.length());
			return getString(resourceFilePath, resourceKey, parameters);
		}
		return formatString(string, parameters);
	}
	
	public static String getString(String stringTable, String resourceKey, Object... parameters) {
		if (stringTable == null) {
			return getString(resourceKey, parameters);
		}
		if (resourceKey == null) {
			return "";
		}
		resourceKey = resourceKey.toLowerCase(Locale.ENGLISH);
		StringTable resources = null;
		try {
			resources = Assets.get(Configuration.getFolderStringResources()+stringTable);
		} catch (GdxRuntimeException e) {
			
		}
		if (resources == null) {
			try {
				resources = Assets.get(stringTable);
			} catch (GdxRuntimeException e) {
			}
		}
		if (resources == null || !resources.resources.containsKey(resourceKey)) {
			return stringTable+RESOURCE_STRING_KEY_SEPARATOR+resourceKey;
		}
		return resources.resources.get(resourceKey).format(parameters).replaceAll("\\\\n", "\n");
	}
	
	/**
	 * Formats the supplied string using the parameters using
	 * MessageFormat logic.
	 * 
	 * It also replaces "\n" strings with new lines.
	 * 
	 * For example calling 
	 * <pre>formatString("Object a: {1}, object b: {0}, object c: {2}", 10, "apple", 13.5);</pre>
	 * will return 
	 * <pre>Object a: apple, object b: 10, object c: 13.5</pre>
	 * 
	 * @param control
	 * @param parameters
	 * @return
	 */
	public static String formatString(String resource, Object... parameters) {
		String returnValue = parameters.length > 0 ? new MessageFormat(resource).format(parameters) : resource;
		return returnValue.replaceAll("\\\\n", "\n");
	}
	
	public static class StringTable {
		ObjectMap<String, MessageFormat> resources;
		
		public StringTable() {
			this.resources = new ObjectMap<String, MessageFormat>();
		}
		
		public void add(String resourceKey, String resourceString) {
			// using regex here to escape all single quotes with another single quote, 
			// since these would otherwise break the MessageFormat - see 
			// http://stackoverflow.com/questions/17544794/escaping-single-quotes-for-java-messageformat
			resources.put(resourceKey, new MessageFormat(resourceString.replaceAll("(?<!')'(?!')", "''")));
		}
	}
}
