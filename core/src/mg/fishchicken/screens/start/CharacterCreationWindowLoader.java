package mg.fishchicken.screens.start;

import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.graphics.models.CharacterModel;
import mg.fishchicken.ui.loading.LoadingWindow.Loader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class CharacterCreationWindowLoader extends Loader<CharacterCreationWindow> {

	private ObjectMap<String, String> portraits = new ObjectMap<String, String>();
	private Array<CharacterModel> models = new Array<CharacterModel>();
	private AssetMap audioProfiles = new AssetMap();
	
	/**
	 * This will unload everything loaded by this loader.
	 * 
	 * @param am
	 */
	public void unload(AssetManager am) {
		for (String path : portraits.values()) {
			am.unload(path);
		}
		portraits.clear();
		
		for (CharacterModel model : models) {
			am.unload(model.getAnimationTextureFile());
		}
		models.clear();
		
		unloadFromStore(am, audioProfiles);
		audioProfiles.clear();
	}
	
	@Override
	public void load(AssetManager am) {
		unload(am);
		portraits.clear();
		Assets.gatherAssets(Configuration.getFolderPCPortraits(), "png", Texture.class, portraits);
		models = CharacterModel.getAllSelectableModels();
		for (CharacterModel model : models) {
			am.load(model.getAnimationTextureFile(), Texture.class);
		}
		audioProfiles.clear();
		for (AudioProfile audioProfile : AudioProfile.getAllSelectableAudioProfiles()) {
			audioProfile.gatherAssets(audioProfiles);
		}
		loadFromStore(am, audioProfiles);
	}

	/**
	 * Loads all character creation screen - relevant assets of the supplied
	 * character into the asset manager.
	 * 
	 * @param character
	 * @param am
	 */
	public void load(GameCharacter character, AssetManager am) {
		am.load(character.getPortraitFile(), Texture.class);
		if (character.getModel().isSelectable()) {
			am.load(character.getModel().getAnimationTextureFile(), Texture.class);
		}
		
		AssetMap store = new AssetMap();
		character.getAudioProfile().gatherAssets(store);
		loadFromStore(am, store);
	}
	
	/**
	 * Unloads all character creation screen - relevant assets of the supplied
	 * character from the asset manager.
	 * 
	 * @param character
	 * @param am
	 */
	public void unload(GameCharacter character, AssetManager am) {
		am.unload(character.getPortraitFile());
		if (character.getModel().isSelectable()) {
			am.unload(character.getModel().getAnimationTextureFile());
		}
		
		AssetMap store = new AssetMap();
		character.getAudioProfile().gatherAssets(store);
		unloadFromStore(am, store);
	}
	
	private void loadFromStore(AssetManager am, AssetMap store) {
		for (Entry<String, Class<?>> entry : store) {
			am.load(entry.key, entry.value);
		}
	}
	
	private void unloadFromStore(AssetManager am, AssetMap store) {
		for (Entry<String, Class<?>> entry : store) {
			am.unload(entry.key);
		}
	}

}
