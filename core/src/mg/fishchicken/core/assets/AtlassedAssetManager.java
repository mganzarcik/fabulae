package mg.fishchicken.core.assets;

import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A special manager that can work with texture atlasses 
 * more seamlessly.
 * 
 * You can load assets with filenames like this:
 * <pre>myAtlas.atlas#regionName</pre>
 * This will load the whole myAtlas.atlas.
 * 
 * Asking for a resource with a name like
 *  <pre>myAtlas.atlas#regionName</pre>
 *  will return a TextureRegion named regionName
 *  from myAtlas.atlas.
 * 
 * @author ANNUN
 *
 */
public class AtlassedAssetManager extends com.badlogic.gdx.assets.AssetManager {
	
	public static final String TEXTURE_REGION_SEPARATOR = ".atlas#";
	private static final int TEXTURE_REGION_SEPARATOR_LENGTH = TEXTURE_REGION_SEPARATOR.length();
	
	@Override
	public synchronized <T> void load(String fileName, Class<T> type,
			AssetLoaderParameters<T> parameter) {
		if (fileName.contains(TEXTURE_REGION_SEPARATOR)) {
			fileName = fileName.substring(0, fileName.lastIndexOf(TEXTURE_REGION_SEPARATOR)+TEXTURE_REGION_SEPARATOR_LENGTH-1);
			super.load(fileName, TextureAtlas.class, null);
		} else {
			super.load(fileName, type, parameter);
		}
	}
	
	@Override
	public synchronized void unload(String fileName) {
		// do not propagate errors if we attempt to unload something already unloaded
		// just log it and go on
		//try {
			super.unload(fileName);
		/*} catch (GdxRuntimeException e) {
			Log.log("Error when unloading {0}: {1}", LogType.ERROR, fileName, e.getMessage());
			return;
		}*/
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T get(String fileName) {
		if (fileName.contains(TEXTURE_REGION_SEPARATOR)) {
			return (T) getTextureRegion(fileName);
		}
		return super.get(fileName);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T get(String fileName, Class<T> type) {
		if (TextureRegion.class.isAssignableFrom(type) || fileName.contains(TEXTURE_REGION_SEPARATOR)) {
			return (T) getTextureRegion(fileName);
		}
		return super.get(fileName, type);
	}

	
	public synchronized TextureRegion getTextureRegion(String fileName) {
		if (fileName.contains(TEXTURE_REGION_SEPARATOR)) {
			String textureRegion = fileName.substring(fileName.lastIndexOf(TEXTURE_REGION_SEPARATOR)+TEXTURE_REGION_SEPARATOR_LENGTH, fileName.length());
			fileName = fileName.substring(0, fileName.lastIndexOf(TEXTURE_REGION_SEPARATOR)+TEXTURE_REGION_SEPARATOR_LENGTH-1);
			return super.get(fileName, TextureAtlas.class).findRegion(textureRegion);
		}
		return  new TextureRegion(super.get(fileName, Texture.class));
	}
}
