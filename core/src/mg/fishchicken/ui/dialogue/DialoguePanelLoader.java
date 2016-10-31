package mg.fishchicken.ui.dialogue;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.dialogue.Dialogue;
import mg.fishchicken.gamelogic.dialogue.DialogueCallback;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.loading.LoadingWindow.Loader;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

public class DialoguePanelLoader extends Loader<DialoguePanel> {

	private String dialoguePath;
	private GameCharacter talkingPC; 
	private GameCharacter talkingNPC; 
	private DialogueCallback callback; 
	private ObjectMap<String, String> dialogueParameters;
	
	public void setDetails(String dialogueId, GameCharacter talkingPC, GameCharacter talkingNPC, DialogueCallback callback, ObjectMap<String, String> dialogueParameters) {
		this.dialoguePath = Configuration.getFolderDialogues()+dialogueId+".xml";
		this.talkingNPC = talkingNPC;
		this.talkingPC = talkingPC;
		this.callback = callback;
		this.dialogueParameters = dialogueParameters;
	}
	
	@Override
	public void load(AssetManager am) {
		if (dialoguePath != null) {
			am.load(dialoguePath, Dialogue.class);
		}
	}
	
	@Override
	protected void loadDependencies(AssetManager am) {
		ObjectSet<String> images = am.get(dialoguePath, Dialogue.class).getAllImages();
		for (String image : images) {
			am.load(image, Texture.class);
		}
	}
	
	@Override
	public void onLoaded(AssetManager am, DialoguePanel loadedWindow) {
		super.onLoaded(am, loadedWindow);
		loadedWindow.setDialogue(am.get(dialoguePath, Dialogue.class), talkingPC, talkingNPC, callback, dialogueParameters);
		WindowPosition.CENTER.position(loadedWindow);
	}
	
	public void unload(AssetManager am) {
		if (dialoguePath != null) {
			ObjectSet<String> images = am.get(dialoguePath, Dialogue.class).getAllImages();
			for (String image : images) {
				am.unload(image);
			}
			am.unload(dialoguePath);
			dialoguePath = null;
		}
	}
}