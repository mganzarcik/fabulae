package mg.fishchicken.graphics.models;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.graphics.animations.ItemAnimationMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class ItemModel extends Model {

	private static ObjectMap<String, String> models = new ObjectMap<String, String>();

	public static ItemModel getModel(String id) {
		String filename = models.get(id.toLowerCase(Locale.ENGLISH));
		return filename != null ? Assets.get(filename, ItemModel.class) : null;
	}
	
	/**
	 * Gathers all Models and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherModels() throws IOException {
		Assets.gatherAssets(Configuration.getFolderItemModels(), "xml", ItemModel.class, models);
	}
	
	private ItemAnimationMap animationMap;
	private AssetMap assetStore = new AssetMap();
	
	public ItemModel(FileHandle file) throws IOException {
		super(file);
		// create the animation map already
		// speed will be recalculated once the item is equipped
		animationMap = new ItemAnimationMap(this, null, 1f);
		animationMap.gatherAssets(assetStore);
	}
	
	/**
	 * Returns a new instance of the animation map for this item.
	 * @return
	 */
	public ItemAnimationMap getAnimationMapInstance() {
		return new ItemAnimationMap(animationMap);
	}
	
	public void loadAssets() {
		AssetManager am = Assets.getAssetManager();
		for (Entry<String, Class<?>> asset : assetStore) {
			am.load(asset.key, asset.value);
		}
	}

	public void unloadAssets() {
		AssetManager am = Assets.getAssetManager();
		for (Entry<String, Class<?>> asset : assetStore) {
			am.unload(asset.key);
		}
	}

	
}
