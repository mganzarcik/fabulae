package mg.fishchicken.ui.saveload;

import java.time.Instant;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.GameLoader;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.InputDialog;
import mg.fishchicken.ui.dialog.InputDialog.InputDialogStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.saveload.DeletableSavedGameButton.DeletableSavedGameButtonStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SaveGamePanel extends LoadGamePanel {

	private InputDialog inputDialog;
	private boolean isSaving;

	public SaveGamePanel(SaveGamePanelStyle style, GameState gameState,
			OkCancelCallback<Void> callback) {
		super(style, gameState, callback);
		setTitle(Strings.getString(UIManager.STRING_TABLE, "saveGame"));
		inputDialog = new InputDialog(Strings.getString(UIManager.STRING_TABLE, "newSaveTitle"), 
				Strings.getString(UIManager.STRING_TABLE, "newSaveMessage"), style.inputDialogStyle, new InputDialogCallback());
		inputDialog.setModal(true);
		isSaving = false;
	}
	
	@Override
	protected SavedGameButton createSavedGameButton(final SaveGameDetails game,
			LoadGamePanelStyle style) {
		SaveGamePanelStyle castStyle = (SaveGamePanelStyle)style;
		
		return new DeletableSavedGameButton(game, castStyle.slotStyle, new OkCancelCallback<Void>() {
			@Override
			public void onOk(Void result) {
				deleteGame(game);
			}
		});
	}
	
	private void deleteGame(SaveGameDetails game) {
		games.removeValue(game, false);
		setGames(games);
		GameLoader.getSaveGameFile(game.getId()).delete();
		GameLoader.getSaveGameDetailsFile(game.getId()).delete();
	}

	@Override
	protected ClickListener createListener() {
		return new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				Actor actor = event.getListenerActor();
				if (isSaving || !(actor instanceof SavedGameButton) || event.isStopped() || event.isCancelled()) {
					return;
				}
				final SaveGameDetails game = ((SavedGameButton) actor)
						.getGame();
				UIManager.displayConfirmation(
						Strings.getString(UIManager.STRING_TABLE, "overwriteSaveQuestion"), 
						Strings.getString(UIManager.STRING_TABLE,"overwriteSaveConfirmation", game.getName()),
						new OkCancelCallback<Void>() {
							@Override
							public void onOk(Void nada) {
								isSaving = true;
								performSave(game.getId(), game.getName());
								isSaving = false;
							}
						}
				);
			}
		};
	}

	private void performSave(String slot, String name) {
		try {
			gameState.saveGame(slot, name);
			callback.onOk(null);
		} catch (GdxRuntimeException e) {
			callback.onError(Strings.getString(UIManager.STRING_TABLE,
					"errorSavingGame", name));
		}
	}

	@Override
	protected Table createButtons(LoadGamePanelStyle style) {
		Table buttonsTable = super.createButtons(style);
		SaveGamePanelStyle castStyle = (SaveGamePanelStyle) style;
		TextButtonWithSound newSaveButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "createNewSave"),
				castStyle.newGameButtonStyle);
		newSaveButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				inputDialog.clearInput();
				inputDialog.setVisible(true);
				getStage().addActor(inputDialog);
				inputDialog.focusOnInput();
				WindowPosition.CENTER.position(inputDialog);
			}
		});
		buttonsTable.add(newSaveButton)
				.padLeft(castStyle.newGameButtonMarginLeft)
				.width(castStyle.newGameButtonWidth)
				.height(castStyle.newGameButtonHeight).fill();

		return buttonsTable;
	}
	
	private class InputDialogCallback extends OkCancelCallback<String> {
		@Override
		public void onOk(String result) {
			performSave(Long.toString(Instant.now().getEpochSecond()), result);
		}
	}

	public static class SaveGamePanelStyle extends LoadGamePanelStyle {
		private TextButtonWithSoundStyle newGameButtonStyle;
		private InputDialogStyle inputDialogStyle;
		protected DeletableSavedGameButtonStyle slotStyle;
		private int newGameButtonWidth, newGameButtonHeight,
				newGameButtonMarginLeft;
	}
}
