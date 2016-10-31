package mg.fishchicken.screens;

import mg.fishchicken.ui.SkinWithTrueTypeFonts;
import mg.fishchicken.ui.WindowPosition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class ErrorScreen extends BasicStageScreen {

	private Skin skin;
	private Button optionalButton; 
	
	public ErrorScreen(String errorMessage) {
		this(errorMessage, null);
	}
	
	public ErrorScreen(String errorMessage, String optionalButtonText) {
		this.skin = new SkinWithTrueTypeFonts(Gdx.files.internal(ModuleSelectionScreen.SKIN_PATH));
		
		TextButton exitButton = new TextButton("Exit", skin);
		exitButton.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event instanceof ChangeEvent) {
					dispose();
					Gdx.app.exit();
				}
				return false;
			}
		});
		
		Window window = new Window("Error", skin);
		window.pad(30, 10, 10, 10);
		window.setModal(true);
		window.setMovable(false);
		
		window.add(new Label(errorMessage, skin)).padBottom(20).padTop(10);
		window.row();
		Table buttonsTable = new Table();
		if (optionalButtonText != null) {
			optionalButton = new TextButton(optionalButtonText, skin);
			buttonsTable.add(optionalButton).fill().minWidth(100).height(40).padRight(30);
		}
		buttonsTable.add(exitButton).fill().minWidth(100).height(40);
		window.add(buttonsTable).center();
		window.pack();
		stage.addActor(window);
		WindowPosition.CENTER.position(window);
	}
	
	public Button getOptionalButton() {
		return optionalButton;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		skin.dispose();
	}
}
