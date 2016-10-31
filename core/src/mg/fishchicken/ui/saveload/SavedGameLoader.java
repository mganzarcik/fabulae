package mg.fishchicken.ui.saveload;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.ui.loading.LoadingWindow.Loader;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class SavedGameLoader extends Loader<LoadGamePanel> {

	private ObjectMap<String, String> loadedGames = new ObjectMap<String, String>();
	
	public void unload(AssetManager am) {
		for (String fileName : loadedGames.values()) {
			am.unload(fileName);
		}
		loadedGames.clear();
	}
	
	@Override
	public void onLoaded(AssetManager am, LoadGamePanel loadedWindow) {
		Array<SaveGameDetails> games = new Array<SaveGameDetails>();
		am.getAll(SaveGameDetails.class, games);
		loadedWindow.setGames(games);
	}
	
	@Override
	public void load(AssetManager am) {
		unload(am);
		Assets.gatherAssets(Configuration.getFolderSaveGames(), "xml", SaveGameDetails.class, loadedGames, FileType.Local);
	}

}
