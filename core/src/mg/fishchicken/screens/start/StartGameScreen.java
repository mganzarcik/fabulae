package mg.fishchicken.screens.start;

import java.io.IOException;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.Formation;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.screens.ErrorScreen;
import mg.fishchicken.screens.ModuleSelectionScreen;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.BorderedWindow.BorderedWindowStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.MessageDialog;
import mg.fishchicken.ui.dialog.MessageDialog.MessageDialogStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.loading.LoadingIndicator.LoadingIndicatorStyle;
import mg.fishchicken.ui.loading.LoadingWindow;
import mg.fishchicken.ui.options.GameOptionsPanel;
import mg.fishchicken.ui.options.GameOptionsPanelCallback;
import mg.fishchicken.ui.saveload.LoadGamePanel;
import mg.fishchicken.ui.saveload.LoadGamePanel.LoadGamePanelStyle;
import mg.fishchicken.ui.saveload.SavedGameLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

public class StartGameScreen extends SelfLoadingScreen {

	private static final String STYLE_NAME = "startGameMenuStyle";
	
	private GameState gameState;
	private FishchickenGame game;
	private BorderedWindow menu;
	private GameOptionsPanel options;
	private LoadGamePanel loadGamePanel;
	private SavedGameLoader loadableGameLoader;
	private MessageDialog errorDialog;
	private String initialError;
	
	public StartGameScreen(FishchickenGame game, GameState gameState) {
		this(game, gameState, null);
	}
	
	public StartGameScreen(FishchickenGame game, GameState gameState, String initialError) {
		super();
		this.gameState = gameState;
		this.game = game;
		this.initialError = initialError;
	}

	protected void build() {
		StartMenuWindowStyle style = skin.get("startMenu", StartMenuWindowStyle.class);
		
		options = new GameOptionsPanel(skin, gameState, new GameOptionsPanelCallback() {
			@Override
			public void onClose(GameOptionsPanel options) {
				options.setVisible(false);
				menu.setVisible(true);
			}
		});
		options.setMovable(false);
		
		errorDialog = new MessageDialog(Strings.getString(UIManager.STRING_TABLE, "error"), skin.get(MessageDialogStyle.class));
		errorDialog.setMovable(false);
		errorDialog.setModal(true);
		
		loadGamePanel = new LoadGamePanel(skin.get(LoadGamePanelStyle.class), gameState, new OkCancelCallback<Void>() {	
				
			@Override
			public void onCancel() {
				onOk(null);
				menu.setVisible(true);
			}

			@Override
			public void onOk(Void nada) {
				loadGamePanel.setVisible(false);
				loadableGameLoader.unload(Assets.getAssetManager());
			}
			
			@Override
			public void onError(String errorMessage) {
				showError(errorMessage);
			}
		});
		loadGamePanel.setMovable(false);
		loadableGameLoader = new SavedGameLoader();
		
		menu = new BorderedWindow(Strings.getString(UIManager.STRING_TABLE, "startMenuTitle"), style);
		menu.setMovable(false);
		
		TextButtonWithSound startGameButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "startGame"),
				style.startGameButtonStyle);
		startGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				startNewGame();
			}
		});

		TextButtonWithSound loadGameButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "loadGame"),
				style.loadGameButtonStyle);
		loadGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				menu.setVisible(false);
				new LoadingWindow<LoadGamePanel>(loadGamePanel,
						UIManager.getSkin().get(LoadingIndicatorStyle.class), loadableGameLoader);
			}
		});
		
		TextButtonWithSound optionsButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "options"),
				style.optionsButtonStyle);
		optionsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				menu.setVisible(false);
				options.setVisible(true);
				options.setConfigurationValuesToFields();
			}
		});
		
		TextButtonWithSound changeModuleButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "changeModule"),
				style.changeModuleButtonStyle);
		changeModuleButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.setScreen(new ModuleSelectionScreen(game));
			}
		});
	
		TextButtonWithSound exitGameButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "exit"), style.exitButtonStyle);
		exitGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.setScreen(null); // this will dispose of us by calling our hide()
				Gdx.app.exit();
			}
		});
		if (style.headerImage != null) {
			menu.add(new Image(style.headerImage));
			menu.row();
		}
		menu.add(startGameButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padTop(style.buttonsMarginTop)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		menu.row();
		menu.add(loadGameButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		menu.row();
		menu.add(optionsButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		menu.row();
		menu.add(changeModuleButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		menu.row();
		menu.add(exitGameButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight)
				.padBottom(style.buttonsMarginBottom)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		menu.pack();
		options.pack();
		stage.addActor(menu);
		stage.addActor(options);
		stage.addActor(loadGamePanel);
		WindowPosition.CENTER.position(menu);
		WindowPosition.CENTER.position(options);
		WindowPosition.CENTER.position(loadGamePanel);
		options.setVisible(false);
		loadGamePanel.setVisible(false);
		if (initialError != null) {
			showError(initialError);
			initialError = null;
		}
	}
	
	private void showError(String errorMessage) {
		errorDialog.setMessage(errorMessage);
		errorDialog.setVisible(true);
		errorDialog.pack();
		stage.addActor(errorDialog);
		WindowPosition.CENTER.position(errorDialog);
	}
	
	protected void playMusic() {
		AudioTrack<?> music = Configuration.getStartMenuMusic().random();
		if (music != null) {
			music.play();
		}
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (menu != null) {
			WindowPosition.CENTER.position(menu);
		}
		if (options != null) {
			WindowPosition.CENTER.position(options);
		}
	}
	
	private void startNewGame() {
		
		Array<String> startMembers = Configuration.getStartGroupMembers();
		
		if (startMembers.size > 0) {
			try {
				PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
				for (String type : startMembers) {
					pcg.addMember(GameCharacter.loadCharacter(type));
				}
				Formation startFormation = Configuration.getStartFormation();
				if (startFormation != null) {
					pcg.formation().setFormation(startFormation.getFormation());
				}
				pcg.selectAll();
				game.switchToMap(Configuration.getStartMap());
				Music.stopPlayingMusic();
				//game.switchToMap("PerfTest");
				//game.switchToMap("WorldMap1");
				//game.switchToMap("DesertCombat1");
				//game.switchToMap("PerfTest");
				//game.switchToMap("svetlanasHouse");
			} catch (final IOException e) {
				e.printStackTrace();
				game.setScreen(new ErrorScreen("Error loading character: "+e.getMessage()));
			} 
		} else {
			game.setScreen(new PartyCreationScreen(game, gameState));
		}
		
	}
	
	public static class StartMenuWindowStyle extends BorderedWindowStyle {
		protected int buttonWidth, buttonHeight, buttonSpacing, 
			buttonsMarginTop=0, buttonsMarginBottom=0, buttonsMarginLeft=0, buttonsMarginRight=0;
		protected Drawable headerImage;
		private TextButtonWithSoundStyle startGameButtonStyle, loadGameButtonStyle,
				optionsButtonStyle, changeModuleButtonStyle, exitButtonStyle;
	}

	@Override
	protected String getStyleName() {
		return STYLE_NAME;
	}

	@Override
	protected String getLoadingScreenSubtype() {
		return "mainMenu";
	}
}
