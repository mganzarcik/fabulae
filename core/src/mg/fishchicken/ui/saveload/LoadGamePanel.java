package mg.fishchicken.ui.saveload;

import java.util.Comparator;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.ConfirmCancelKeyboardListener;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.saveload.SavedGameButton.SavedGameButtonStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ForcedScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

public class LoadGamePanel extends BorderedWindow {

	protected GameState gameState;
	protected Array<SaveGameDetails> games;
	protected OkCancelCallback<Void> callback;
	private Table scrollPaneTable;
	private boolean isLoading;

	public LoadGamePanel(LoadGamePanelStyle style, final GameState gameState,
			final OkCancelCallback<Void> callback) {
		super(style);
		this.games = new Array<SaveGameDetails>();
		this.gameState = gameState;
		this.callback = callback;
		setTitle(Strings.getString(UIManager.STRING_TABLE, "loadGame"));

		scrollPaneTable = new Table();
		add(new ForcedScrollPane(scrollPaneTable, style.scrollPaneStyle))
				.fill().width(style.scrollPaneWidth)
				.height(style.scrollPaneHeight);
		row();
		if (callback != null) {
			add(createButtons(style)).center().padTop(style.cancelButtonMarginTop);
		}
		pack();
	}

	protected Table createButtons(LoadGamePanelStyle style) {
		Table buttonTable = new Table();
		TextButtonWithSound cancelButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "cancel"), style.cancelButtonStyle);
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				callback.onCancel();
			}
		});
		addListener(new ConfirmCancelKeyboardListener(null, cancelButton));
		buttonTable.add(cancelButton).width(style.cancelButtonWidth)
		.height(style.cancelButtonHeight).fill();
		return buttonTable;
	}

	public void setGames(Array<SaveGameDetails> games) {
		scrollPaneTable.clearChildren();
		isLoading = false;

		games.sort(new Comparator<SaveGameDetails>() {
			@Override
			public int compare(SaveGameDetails arg0, SaveGameDetails arg1) {
				return arg1.getSavedAt().compareTo(arg0.getSavedAt());
			}
		});
		
		this.games = new Array<SaveGameDetails>(games);

		LoadGamePanelStyle style = getStyle();

		ClickListener buttonsListener = createListener();
		for (SaveGameDetails game : games) {
			SavedGameButton button = createSavedGameButton(game, style);
			button.addListener(buttonsListener);
			scrollPaneTable.add(button).fill().expandX()
					.spaceBottom(style.slotSpacing);
			scrollPaneTable.row();
		}
		
		scrollPaneTable.add().expandY();
	}
	
	protected SavedGameButton createSavedGameButton(final SaveGameDetails game, LoadGamePanelStyle style) {
		return new SavedGameButton(game, style.slotStyle);
	}
	
	protected ClickListener createListener() {
		return new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				final Actor actor = event.getListenerActor();
				if (isLoading) {
					return;
				}
				isLoading = true;
				gameState.loadGame(((SavedGameButton) actor).getGame().getId(), new OkCancelCallback<GameMap>() {
					public void onOk(GameMap result) {
						callback.onOk(null);
					};

					@Override
					public void onError(String errorMessage) {
						callback.onError(Strings.getString(UIManager.STRING_TABLE, "errorLoadingGame",
								((SavedGameButton) actor).getGame().getName()));
					}
				});				
				isLoading = false;
			}

		};
	}

	@Override
	public LoadGamePanelStyle getStyle() {
		return (LoadGamePanelStyle) super.getStyle();
	}

	public static class LoadGamePanelStyle extends BorderedWindowStyle {
		protected SavedGameButtonStyle slotStyle;
		private TextButtonWithSoundStyle cancelButtonStyle;
		private ScrollPaneStyle scrollPaneStyle;
		private int cancelButtonWidth, cancelButtonHeight,
				cancelButtonMarginTop, slotSpacing, scrollPaneWidth,
				scrollPaneHeight;
	}
}
