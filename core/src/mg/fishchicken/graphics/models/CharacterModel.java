package mg.fishchicken.graphics.models;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class CharacterModel extends Model {

	private static ObjectMap<String, String> models = new ObjectMap<String, String>();

	public static CharacterModel getModel(String id) {
		return Assets.get(models.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns an array of all models that can be selected by the player
	 * during character creation.
	 */
	public static Array<CharacterModel> getAllSelectableModels() {
		Array<CharacterModel> returnValue = new Array<CharacterModel>();
		for (String modelPath : models.values()) {
			CharacterModel model = Assets.get(modelPath);
			if (model.isSelectable()) {
				returnValue.add(model);
			}
		}
		return returnValue;
	}

	/**
	 * Gathers all Models and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherModels() throws IOException {
		Assets.gatherAssets(Configuration.getFolderCharacterModels(), "xml", CharacterModel.class, models);
	}
	
	private float s_projectileOriginXOffset;
	private float s_projectileOriginYOffset;
	private boolean s_selectable;
	private boolean s_paperdoll;
	private String s_itemModelPrefix;

	public CharacterModel(FileHandle file) throws IOException {
		super(file);
	}

	public float getProjectileOriginXOffset() {
		return s_projectileOriginXOffset;
	}

	public float getProjectileOriginYOffset() {
		return s_projectileOriginYOffset;
	}

	public boolean isSelectable() {
		return s_selectable;
	}
	
	/**
	 * Returns the prefix that any item models rendered on this model when using paperdolls should have.
	 * 
	 * For example, if the prefix is "human" and the model id of the item itself is "sword", the resulting
	 * item model ID will be "humansword". "humansword" will then be the model that will be used
	 * to try to render the sword.
	 * @return
	 */
	public String getItemModelIdPrefix() {
		return s_itemModelPrefix != null ? s_itemModelPrefix : "";
	}
	
	/**
	 * Whether or not this model supports paperdoll features, where worn items are actually rendered on top of it
	 * if they have their own models.
	 * @return
	 */
	public boolean isPaperdoll() {
		return s_paperdoll;
	}
}
