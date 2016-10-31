package mg.fishchicken.ui.storysequence;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.gamelogic.story.StoryPage;
import mg.fishchicken.gamelogic.story.StorySequence;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.loading.LoadingWindow.Loader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class StorySequencePanelLoader extends Loader<StorySequencePanel> {

	private AssetMap loadedAssets = new AssetMap();
	private StorySequence storySequence;
	
	public void setStorySequence(StorySequence storySequence) {
		this.storySequence = storySequence;
		this.loadedAssets.clear();
	}
	
	@Override
	public void load(AssetManager am) {
		unload(am);
		if (storySequence != null) {
			for (AudioTrack<?> music : storySequence.getMusic()) {
				music.gatherAssets(loadedAssets);
			}
			for (StoryPage page : storySequence.getPages()) {
				if (page.isApplicable()) {
					loadedAssets.put(page.getImage(), Texture.class);
				}
			}
		}
		
		for (Entry<String, Class<?>> entry : loadedAssets) {
			am.load(entry.key, entry.value);
		}
	}
	
	@Override
	public void onLoaded(AssetManager am, StorySequencePanel loadedWindow) {
		super.onLoaded(am, loadedWindow);
		loadedWindow.setStorySequence(storySequence);
		WindowPosition.CENTER.position(loadedWindow);
	}
	
	public void unload(AssetManager am) {
		for (Entry<String, Class<?>> entry : loadedAssets) {
			am.unload(entry.key);
		}
		loadedAssets.clear();
	}
}