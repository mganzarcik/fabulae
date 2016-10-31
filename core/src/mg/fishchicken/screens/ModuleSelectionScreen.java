package mg.fishchicken.screens;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.ui.SkinWithTrueTypeFonts;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.selectbox.SelectOption;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;

public class ModuleSelectionScreen extends BasicStageScreen implements EventListener {

	public static final String SKIN_PATH = "uiStyle.json";
	private FishchickenGame game;
	private Skin skin;
	private SelectBox<SelectOption<String>> moduleSelectBox;
	
	public ModuleSelectionScreen(FishchickenGame game) {
		super();
		this.skin = new SkinWithTrueTypeFonts(Gdx.files.internal(ModuleSelectionScreen.SKIN_PATH));
		this.game = game;
		Array<FileHandle> modules = getModules();
		
		Window window = new Window("Module selection", skin);
		window.pad(30, 10, 10, 10);
		window.setModal(true);
		window.setMovable(false);
		
		TextButton exitButton = new TextButton("Exit", skin);
		exitButton.addListener(this);
		exitButton.setName("EXIT");
		
		if (modules == null) {
			window.add(new Label("No modules found.", skin)).padBottom(20).padTop(10);
			window.row();
			window.add(exitButton).fill().width(100).height(40).center();
		} else {
			moduleSelectBox = new SelectBox<SelectOption<String>>(skin.get(SelectBoxStyle.class));
			Array<SelectOption<String>> options = new Array<SelectOption<String>>();
			for (FileHandle moduleDir : modules) {
				SelectOption<String> option = new SelectOption<String>(" "+moduleDir.name(), moduleDir.name());
				options.add(option);
			}
			moduleSelectBox.setItems(options);
			
			TextButton okButton = new TextButton("Ok", skin);
			okButton.addListener(this);
			okButton.setName("OK");
			
			window.add(new Label("Please choose a module:", skin)).padBottom(10).padTop(10).left();
			window.row();
			window.add(moduleSelectBox).padBottom(20).center().minWidth(170);
			window.row();
			Table buttonsTable = new Table();
			buttonsTable.add(exitButton).fill().width(100).height(40);
			buttonsTable.add(okButton).fill().width(100).height(40).padLeft(30);
			window.add(buttonsTable).center();
		}
		window.pack();
		stage.addActor(window);
		WindowPosition.CENTER.position(window);
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
			if ("EXIT".equals(event.getTarget().getName())) {
				dispose();
				Gdx.app.exit();
			} else if ("OK".equals(event.getTarget().getName())) {
				SelectOption<String> selected = moduleSelectBox.getSelected(); 
				if (selected != null) {
					FileHandle currentModuleFile = Gdx.files.local(Configuration.FILE_LAST_MODULE);
					currentModuleFile.writeString(selected.value, false);
					dispose();
					game.reloadGame();
				}
			}
			return true;
		}
		return false;
	}
	
	public static final Array<FileHandle> getModules() {
		FileHandle modulesFolder = Gdx.files.internal(Configuration.FOLDER_MODULES);
		if (!modulesFolder.isDirectory()) {
			modulesFolder = Gdx.files.internal(Assets.BIN_FOLDER+Configuration.FOLDER_MODULES);
		} 
		Array<FileHandle> returnValue = new Array<FileHandle>();
		if (!modulesFolder.isDirectory()) {
			return null;
		} else {
			for (FileHandle dir : modulesFolder.list()) {
				if (dir.isDirectory()) {
					returnValue.add(dir);
				}
			}
		}
		return returnValue;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		skin.dispose();
	}

}
