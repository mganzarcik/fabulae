package mg.fishchicken.ui.gamemenu;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class GameMenuPanel extends BorderedWindow {
	
	public GameMenuPanel(final GameState gameState, GameMenuPanelStyle style) {
		super(Strings.getString(UIManager.STRING_TABLE, "gameMenuTitle"),
				style);
		
		final TextButtonWithSound backButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "back"),
				style.backButtonStyle);
		backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.hideGameMenuPanel();
			}
		});
		
		this.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (KeyBindings.CANCEL.is(keycode)) {
					backButton.toggle();
				}
				
				return false;
			}
		});

		TextButtonWithSound loadGameButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "loadGame"),
				style.loadGameButtonStyle);
		loadGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.displayLoadGamePanel();
			}
		});
		
		TextButtonWithSound saveGameButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "saveGame"),
				style.saveGameButtonStyle);
		saveGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.displaySaveGamePanel();
			}
		});
		
		TextButtonWithSound optionsButton = new TextButtonWithSound(
				Strings.getString(UIManager.STRING_TABLE, "options"),
				style.optionsButtonStyle);
		optionsButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.hideGameMenuPanel();
				UIManager.displayGameOptionsPanel();
			}
		});
	
		TextButtonWithSound exitButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "exit"),
				style.exitButtonStyle);
		exitButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.displayConfirmation(Strings.getString(
						UIManager.STRING_TABLE, "exitGameQuestion"),
						Strings.getString(
						UIManager.STRING_TABLE, "exitGameConfirmation"),
						new OkCancelCallback<Void>() {
					
					@Override
					public void onOk(Void nada) {
						gameState.exitGameToMainMenu();
					}
				});
			}
		});
		if (style.headerImage != null) {
			add(new Image(style.headerImage));
			row();
		}
		add(backButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padTop(style.buttonsMarginTop)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		row();
		add(loadGameButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		row();
		add(saveGameButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		row();
		add(optionsButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight).padBottom(style.buttonSpacing)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		row();
		add(exitButton).prefWidth(style.buttonWidth)
				.prefHeight(style.buttonHeight)
				.padBottom(style.buttonsMarginBottom)
				.padLeft(style.buttonsMarginLeft)
				.padRight(style.buttonsMarginRight);
		pack();

	}
	
	public static class GameMenuPanelStyle extends BorderedWindowStyle {
		int buttonWidth, buttonHeight, buttonSpacing, 
			buttonsMarginTop=0, buttonsMarginBottom=0, buttonsMarginLeft=0, buttonsMarginRight=0;
		Drawable headerImage;
		TextButtonWithSoundStyle exitButtonStyle, optionsButtonStyle, saveGameButtonStyle, loadGameButtonStyle, backButtonStyle;
	}

}
