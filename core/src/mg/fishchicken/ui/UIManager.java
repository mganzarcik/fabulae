package mg.fishchicken.ui;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.TargetSelectionCallback;
import mg.fishchicken.core.input.Targetable;
import mg.fishchicken.core.input.tools.Tool;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.PlayerCharacter;
import mg.fishchicken.gamelogic.dialogue.DialogueCallback;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.story.StorySequence;
import mg.fishchicken.screens.start.CharacterCreationWindow;
import mg.fishchicken.screens.start.CharacterCreationWindowLoader;
import mg.fishchicken.ui.CombatButtons.CombatButtonsStyle;
import mg.fishchicken.ui.LogPanel.LogPanelStyle;
import mg.fishchicken.ui.PlayerCharactersPanel.PlayerCharactersPanelStyle;
import mg.fishchicken.ui.TargetSelectionDialog.TargetSelectionDialogStyle;
import mg.fishchicken.ui.camp.CampPanel;
import mg.fishchicken.ui.camp.CampPanel.CampPanelStyle;
import mg.fishchicken.ui.debug.DebugConsole;
import mg.fishchicken.ui.debug.DebugConsole.DebugConsoleStyle;
import mg.fishchicken.ui.debug.DebugPanel;
import mg.fishchicken.ui.debug.DebugPanel.DebugPanelStyle;
import mg.fishchicken.ui.dialog.ConfirmationDialog;
import mg.fishchicken.ui.dialog.ConfirmationDialog.ConfirmationDialogStyle;
import mg.fishchicken.ui.dialog.MessageDialog;
import mg.fishchicken.ui.dialog.MessageDialog.MessageDialogStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.dialog.ProgressDialog;
import mg.fishchicken.ui.dialog.ProgressDialog.ProgressDialogStyle;
import mg.fishchicken.ui.dialog.ProgressDialogCallback;
import mg.fishchicken.ui.dialog.ProgressDialogSettings;
import mg.fishchicken.ui.dialogue.DialoguePanel;
import mg.fishchicken.ui.dialogue.DialoguePanel.DialoguePanelStyle;
import mg.fishchicken.ui.dialogue.DialoguePanelLoader;
import mg.fishchicken.ui.effects.ActiveEffectsPanel;
import mg.fishchicken.ui.effects.ActiveEffectsPanel.ActiveEffectsPanelStyle;
import mg.fishchicken.ui.formation.FormationEditorPanel;
import mg.fishchicken.ui.formation.FormationEditorPanel.FormationEditorPanelStyle;
import mg.fishchicken.ui.gamemenu.GameMenuPanel;
import mg.fishchicken.ui.gamemenu.GameMenuPanel.GameMenuPanelStyle;
import mg.fishchicken.ui.inventory.InventoryContainerPanel;
import mg.fishchicken.ui.inventory.InventoryContainerPanel.InventoryContainerPanelStyle;
import mg.fishchicken.ui.inventory.InventoryEventHandler;
import mg.fishchicken.ui.inventory.InventoryItemButton;
import mg.fishchicken.ui.inventory.InventoryPanel;
import mg.fishchicken.ui.inventory.InventoryPanel.InventoryPanelStyle;
import mg.fishchicken.ui.inventory.TradingEventHandler;
import mg.fishchicken.ui.inventory.TradingPanel;
import mg.fishchicken.ui.inventory.TradingPanel.TradingPanelStyle;
import mg.fishchicken.ui.journal.JournalPanel;
import mg.fishchicken.ui.journal.JournalPanel.JournalPanelStyle;
import mg.fishchicken.ui.loading.LoadingIndicator.LoadingIndicatorStyle;
import mg.fishchicken.ui.loading.LoadingWindow;
import mg.fishchicken.ui.map.MapPanel;
import mg.fishchicken.ui.map.MapPanel.MapPanelStyle;
import mg.fishchicken.ui.options.GameOptionsPanel;
import mg.fishchicken.ui.options.GameOptionsPanelCallback;
import mg.fishchicken.ui.perks.PerksPanel;
import mg.fishchicken.ui.perks.PerksPanel.PerksPanelStyle;
import mg.fishchicken.ui.perks.UsePerksPanel;
import mg.fishchicken.ui.perks.UsePerksPanel.UsePerksPanelStyle;
import mg.fishchicken.ui.saveload.LoadGamePanel;
import mg.fishchicken.ui.saveload.LoadGamePanel.LoadGamePanelStyle;
import mg.fishchicken.ui.saveload.SaveGamePanel;
import mg.fishchicken.ui.saveload.SaveGamePanel.SaveGamePanelStyle;
import mg.fishchicken.ui.saveload.SavedGameLoader;
import mg.fishchicken.ui.spells.SpellbookPanel;
import mg.fishchicken.ui.spells.SpellbookPanel.SpellbookPanelStyle;
import mg.fishchicken.ui.storysequence.StorySequencePanel;
import mg.fishchicken.ui.storysequence.StorySequencePanel.StorySequencePanelStyle;
import mg.fishchicken.ui.storysequence.StorySequencePanelLoader;
import mg.fishchicken.ui.toolbar.CharacterToolbar;
import mg.fishchicken.ui.toolbar.CharacterToolbar.CharacterToolbarStyle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class UIManager {
	
	public static final String STRING_TABLE = "generalUI."+Strings.RESOURCE_FILE_EXTENSION;
	
	public static final String XML_UI = "ui";
	
	private static GameState gameState;
	private static Stage stage;
	private static DialoguePanel dialoguePanel;
	private static MessageDialog messageDialog;
	private static ConfirmationDialog confirmationDialog;
	private static LogPanel logPanel;
	private static PlayerCharactersPanel pcPanel;
	private static CharacterToolbar characterToolbar;
	private static PerksPanel perksPanel;
	private static ActiveEffectsPanel activeEffectsPanel;
	private static FormationEditorPanel formationEditor;
	private static CampPanel campPanel;
	private static TargetSelectionDialog targetSelectionDialog;
	private static UsePerksPanel usePerksPanel;
	private static SpellbookPanel spellbookPanel;
	private static InventoryPanel inventoryPanel;
	private static JournalPanel journalPanel;
	private static InventoryContainerPanel containerPanel;
	private static TradingPanel tradingPanel;
	private static CombatButtons combatButtons;
	private static ProgressDialog progressDialog;
	private static GameCharacter displayedCharacter;
	private static GameCharacter displayedMerchant;
	private static InventoryContainer displayedContainer;
	private static InventoryItem draggedItem;
	private static InventoryItemButton firstDraggedFrom;
	private static MapPanel mapPanel;
	private static GameMenuPanel gameMenuPanel;
	private static GameOptionsPanel gameOptionsPanel;
	private static CharacterCreationWindow characterEditPanel;
	private static LoadGamePanel loadGamePanel;
	private static SaveGamePanel saveGamePanel;
	private static float movedX;
	private static float movedY;
	private static Actor toolTip;
	private static BitmapFont stackFont, combatPathFont, floatingTextFont, loadingFont;
	private static InventoryEventHandler inventoryEventHandler;
	private static TradingEventHandler tradingEventHandler;
	private static InventoryEventHandler activeEventHandler;
	private static float tooltipDelayCounter;
	private static CharacterCreationWindowLoader characterEditLoader;
	private static SavedGameLoader savedGameLoader;
	private static DialoguePanelLoader dialogueLoader;
	private static OkCancelCallback<GameCharacter> characterEditPanelCallback;
	private static LoadingWindow<?> loadingWindow;
	private static StorySequencePanel storySequencePanel;
	private static StorySequencePanelLoader storySequenceLoader;
	private static DebugPanel debugPanel;
	private static DebugConsole debugConsole;
	private static Label pausedIndicator;
	private static PausedIndicatorStyle pausedIndicatorStyle;
	private static LoadingIndicatorStyle loadingStyle;
	private static Skin skin;
	
	private static Cursor defaultCursor, lockpickCursor, disarmCursor;
	
	private static SpriteBatch batch;
		
	public static void init(final GameState gameState, Skin skin) {
		dispose();
		UIManager.skin = skin;
		loadingStyle = skin.get(LoadingIndicatorStyle.class);
		stage = new Stage(new ScreenViewport());
		stackFont = skin.getFont("stackSize");
		combatPathFont = skin.getFont("combatPath");
		floatingTextFont = skin.getFont("floatingText");
		loadingFont = skin.getFont("loading");
		batch = new SpriteBatch();
		buildCursors(Assets.getAssetManager());
		
		stage.addListener(new EventListener() {

			@Override
			public boolean handle(Event event) {
				if (event instanceof InputEvent) {
					InputEvent inputEvent = (InputEvent) event;
					Type type = inputEvent.getType();
					if (Type.mouseMoved == type || Type.touchDragged == type) {
						movedX = inputEvent.getStageX() * Configuration.getMapScale();
						movedY = inputEvent.getStageY() * Configuration.getMapScale();
					}
				}
				return false;
			}
		});
		
		stage.addListener(gameState.getPlayerCharacterController());
		UIManager.gameState = gameState;
		final AssetManager am = Assets.getAssetManager();
		inventoryEventHandler = new InventoryEventHandler(gameState);
		tradingEventHandler = new TradingEventHandler(gameState);
		dialoguePanel = new DialoguePanel(skin.get(DialoguePanelStyle.class));
		logPanel = new LogPanel(skin.get(LogPanelStyle.class));
		pcPanel =  new PlayerCharactersPanel(gameState, GameState.getPlayerCharacterGroup(), skin.get(PlayerCharactersPanelStyle.class));
		characterToolbar = new CharacterToolbar(gameState, GameState.getPlayerCharacterGroup(), skin.get(CharacterToolbarStyle.class));
		perksPanel = new PerksPanel(skin.get(PerksPanelStyle.class));
		gameMenuPanel = new GameMenuPanel(gameState, skin.get(GameMenuPanelStyle.class));
		gameMenuPanel.setModal(true);
		gameOptionsPanel = new GameOptionsPanel(skin, gameState, new GameOptionsPanelCallback() {
			@Override
			public void onClose(GameOptionsPanel options) {
				hideGameOptionsPanel();
			}
		});
		gameOptionsPanel.setModal(true);
		activeEffectsPanel = new ActiveEffectsPanel(skin.get(ActiveEffectsPanelStyle.class));
		campPanel = new CampPanel(gameState, skin.get(CampPanelStyle.class));
		campPanel.setModal(true);
		targetSelectionDialog =  new TargetSelectionDialog(skin.get(TargetSelectionDialogStyle.class));
		targetSelectionDialog.setModal(true);
		usePerksPanel = new UsePerksPanel(gameState, skin.get(UsePerksPanelStyle.class));
		spellbookPanel = new SpellbookPanel(gameState, skin.get(SpellbookPanelStyle.class));
		inventoryPanel = new InventoryPanel(gameState, skin.get(InventoryPanelStyle.class));
		journalPanel = new JournalPanel(skin.get(JournalPanelStyle.class));
		containerPanel = new InventoryContainerPanel(skin.get(InventoryContainerPanelStyle.class));
		tradingPanel = new TradingPanel(skin.get(TradingPanelStyle.class));
		combatButtons = new CombatButtons(gameState, skin.get(CombatButtonsStyle.class));
		progressDialog = new ProgressDialog(skin.get(ProgressDialogStyle.class));
		progressDialog.setModal(true);
		formationEditor = new FormationEditorPanel(skin.get(FormationEditorPanelStyle.class));
		messageDialog = new MessageDialog("", skin.get(MessageDialogStyle.class));
		messageDialog.setModal(true);
		confirmationDialog = new ConfirmationDialog("",skin.get(ConfirmationDialogStyle.class));
		confirmationDialog.setModal(true);
		storySequencePanel = new StorySequencePanel(gameState, skin.get(StorySequencePanelStyle.class));
		storySequencePanel.setModal(true);
		storySequenceLoader = new StorySequencePanelLoader();
		loadGamePanel = new LoadGamePanel(skin.get(LoadGamePanelStyle.class), gameState, new OkCancelCallback<Void>() {	
			@Override
			public void onCancel() {
				removeActor(loadGamePanel);
				savedGameLoader.unload(am);
				gameState.unpauseGame();
			}

			@Override
			public void onError(String errorMessage) {
				gameState.exitGameToMainMenu(errorMessage);
			}
		});
		loadGamePanel.setModal(true);
		
		saveGamePanel = new SaveGamePanel(skin.get(SaveGamePanelStyle.class), gameState, new OkCancelCallback<Void>() {
			@Override
			public void onOk(Void nada) {
				onCancel();
			}
			
			public void onCancel() {
				removeActor(saveGamePanel);
				savedGameLoader.unload(am);
				gameState.unpauseGame();
			}
			
			@Override
			public void onError(String errorMessage) {
				displayMessage(Strings.getString(STRING_TABLE, "error"), errorMessage);
			}
		});
		saveGamePanel.setModal(true);
		
		savedGameLoader = new SavedGameLoader();
		
		characterEditLoader = new CharacterCreationWindowLoader() {
			@Override
			public void onLoaded(AssetManager am, CharacterCreationWindow loadedWindow) {
				if (displayedCharacter.isPlayerEditable()) {
					unload(displayedCharacter, am);
				}
				loadedWindow.setCharacter(displayedCharacter);
			}
		};
		characterEditPanelCallback = new OkCancelCallback<GameCharacter>() {
			
			@Override
			public void onOk(GameCharacter character) {
				onCancel();
				character.recalculateAllItemModels();
				refreshPCPanel();
			}
			
			@Override
			public void onCancel() {
				if (displayedCharacter.isPlayerEditable()) {
					characterEditLoader.load(displayedCharacter, am);
				}
				characterEditLoader.unload(am);
				removeActor(characterEditPanel);
				gameState.unpauseGame();
			}
		};
		
		dialogueLoader = new DialoguePanelLoader();
		
		characterEditPanel = new CharacterCreationWindow(skin, "default", characterEditPanelCallback);
		characterEditPanel.setTitle(Strings.getString(UIManager.STRING_TABLE, "characterEditTitle"));
		debugPanel = new DebugPanel(gameState, skin.get(DebugPanelStyle.class));
		debugConsole = new DebugConsole(skin.get(DebugConsoleStyle.class));
		pausedIndicatorStyle = skin.get(PausedIndicatorStyle.class);
		pausedIndicator = new Label(Strings.getString(UIManager.STRING_TABLE, "paused"), pausedIndicatorStyle.labelStyle);

		stage.addActor(pausedIndicator);
		stage.addActor(characterToolbar);
		stage.addActor(logPanel);
		stage.addActor(pcPanel);
		if (Configuration.isDebugPanelEnabled()) {
			stage.addActor(debugPanel);
		}
	}
	
	public static Stage getStage() {
		return stage;
	}
	
	public static Skin getSkin() {
		return skin;
	}
	
	/**
	 * Returns true if any modal dialog is currently open. This includes
	 * the message, confirmation, target selection and progress dialogs.
	 * @return
	 */
	public static boolean isAnyModalOpen() {
		for (Actor actor : stage.getActors()) {
			if (actor.isVisible() && actor instanceof Window) {
				if (((Window)actor).isModal()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Updates the viewport of the UI Stage with the supplied dimensions.
	 * 
	 * This should be called when the game window is resized.
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 */
	public static void onResize(int screenWidth, int screenHeight) {
		if (batch != null) {
			batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
		screenWidth = (int) (screenWidth / Configuration.getMapScale());
		screenHeight = (int) (screenHeight / Configuration.getMapScale());
		stage.getViewport().update(screenWidth, screenHeight, true);
		if (pcPanel!= null) {
			pcPanel.updatePosition(screenWidth, screenHeight);
		} if (combatButtons != null) {
			combatButtons.updatePosition(screenWidth, screenHeight);
		} if (logPanel != null) {
			logPanel.updateSizeAndPosition(screenWidth, screenHeight);
		} if (gameOptionsPanel != null) {
			gameOptionsPanel.onResize();
		} if (debugPanel != null) {
			debugPanel.updatePosition(screenWidth, screenHeight);
		} if (pausedIndicator != null) {
			pausedIndicator.setPosition(
					Gdx.graphics.getWidth() / 2f - pausedIndicator.getWidth() / 2f, 
					Gdx.graphics.getHeight() - pausedIndicator.getHeight() - pausedIndicatorStyle.marginTop);
		}
	}
	
	/**
	 * Switch the UI to combat mode, which displays
	 * combat - relevant UI elements like the start / end combat buttons.
	 */
	public static void startCombatMode() {
		stage.addActor(combatButtons);
		combatButtons.setVisible(true);
	}
	
	/**
	 * Ends the combat mode of the UI.
	 * 
	 * @see #startCombatMode()
	 */
	public static void endCombatMode() {
		removeActor(combatButtons);
		hideToolTip();
	}
	
	/**
	 * Displays the debug console.
	 */
	public static void displayDebugConsole() {
		if (!isPanelOpen(debugConsole)) {
			addAndShow(debugConsole);
		}
	}
	
	/**
	 * Hides the debug console.
	 */
	public static void hideDebugConsole() {
		removeActor(debugConsole);
	}
	
	/**
	 * Displays the dialogue panel for the supplied player character
	 * and dialogue id.
	 * 
	 * @param talkingPC
	 * @param dialogueId
	 */
	public static void displayDialogue(PlayerCharacter talkingPC, String dialogueId) {
		displayDialogue(talkingPC, null, dialogueId);
	}
	
	/**
	 * Displays the dialogue panel for the supplied player character, non player character
	 * and dialogue id.
	 * 
	 * @param talkingPC
	 * @param dialogueId
	 */
	public static void displayDialogue(GameCharacter talkingPC, GameCharacter talkingNPC, String dialogueId) {
		displayDialogue(talkingPC, talkingNPC, dialogueId, null, null);
	}
	
	/**
	 * Displays the dialogue panel for the supplied player character, non player character
	 * and dialogue id. The supplied callback will be called when the dialogue ends.
	 * 
	 * @param talkingPC
	 * @param talkingNPC
	 * @param dialogueId
	 */
	public static void displayDialogue(GameCharacter talkingPC, GameCharacter talkingNPC, String dialogueId, DialogueCallback callback, ObjectMap<String, String> dialogueParameters) {
		if (dialogueId == null) {
			return;
		}
		gameState.pauseGame();
		dialogueLoader.setDetails(dialogueId, talkingPC, talkingNPC, callback, dialogueParameters);
		addAndShow(dialoguePanel);
		loadingWindow = new LoadingWindow<DialoguePanel>(dialoguePanel, loadingStyle, dialogueLoader);
	}
	
	/**
	 * Displays the story sequence panel for the supplied sequence.
	 * 
	 */
	public static void displayStorySequence(String storySequenceId) {
		displayStorySequence(StorySequence.getStorySequence(storySequenceId));
	}
	
	/**
	 * Displays the story sequence panel for the supplied sequence.
	 * 
	 */
	public static void displayStorySequence(StorySequence storySequence) {
		if (storySequence == null) {
			return;
		}
		gameState.pauseGame();
		storySequenceLoader.setStorySequence(storySequence);
		addAndShow(storySequencePanel);
		loadingWindow = new LoadingWindow<StorySequencePanel>(storySequencePanel, loadingStyle, storySequenceLoader);
	}
	
	/**
	 * Hides the currently story sequence panel, if visible.
	 */
	public static void hideStorySequence() {
		boolean unpause = removeActor(storySequencePanel);
		if (isLoadingPanel(storySequencePanel)) {
			removeActor(loadingWindow);
			unpause = true;
		}
		storySequenceLoader.unload(Assets.getAssetManager());
		if (unpause) {
			gameState.unpauseGame();
		}
	}
	
	/**
	 * Hides the currently displayed dialogue panel, if visible.
	 */
	public static void hideDialogue() {
		boolean unpause = removeActor(dialoguePanel);
		if (isLoadingPanel(dialoguePanel)) {
			removeActor(loadingWindow);
			unpause = true;
		}
		dialogueLoader.unload(Assets.getAssetManager());
		if (unpause) {
			gameState.unpauseGame();
		}
	}
	
	/**
	 * Displays a message in the game log panel.
	 * 
	 * @param text
	 */
	public static void logMessage(String text, Color color, boolean logTime) {
		if (logPanel != null) {
			logPanel.logMessage(text, color, logTime);
		}
	}
	
	/**
	 * Displays a message to the player with a single OK button.
	 * 
	 * Will pause the game until the message is closed.
	 * 
	 * @param text
	 */
	public static void displayMessage(String title, String text) {
		gameState.pauseGame();
		messageDialog.setTitle(title);
		messageDialog.setMessage(text);
		addAndShow(messageDialog);
	}
	
	public static void displayProgressDialog(ProgressDialogSettings settings, ProgressDialogCallback callback) {
		progressDialog.init(settings,callback);
		addAndShow(progressDialog);
	}
	
	public static void updateProgressDialog(float newValue) {
		if (progressDialog.isVisible()) {
			progressDialog.update(newValue);
		}
	}
	
	public static void hideProgressDialog() {
		removeActor(progressDialog);
	}
	
	/**
	 * This will display a confirmation window with the supplied text
	 * and yes and no buttons.
	 * 
	 * All other UI elements will be disabled while the confirmation window is open.
	 * 
	 * @param text
	 * @param moveMouse - if true, the mouse cursor will be automatically moved to the 
	 * 					  no option of the confirmation dialogue
	 * @param callback
	 */
	public static void displayConfirmation(String title, String text, OkCancelCallback<Void> callback) {
		confirmationDialog.setTitle(title);
		confirmationDialog.setMessage(text);
		confirmationDialog.setCallback(callback);
		addAndShow(confirmationDialog);
		hideToolTip();
		
		// move the mouse cursor to the confirmation dialogue
		if (Configuration.getMoveMouse()) {
			drawUI();
			Vector2 tempVector = MathUtil.getVector2().set(confirmationDialog.getCancelCoordinates());
			stage.stageToScreenCoordinates(tempVector);
			Gdx.input.setCursorPosition((int)tempVector.x, (int)tempVector.y);
			Gdx.input.getInputProcessor().mouseMoved((int)tempVector.x, (int)tempVector.y);
			stage.act(0f); // this will update enter and exit events on stage correctly, resetting mouseOver states correctly
			MathUtil.freeVector2(tempVector);
		}
	}
	
	/**
	 * Displays the trading panels, with he customer's inventory on one side and
	 * the merchant's inventory on the other.
	 * 
	 * This pauses the game.
	 * 
	 * @param customer
	 * @param merchant
	 */
	public static void displayTradingInventory(GameCharacter customer, GameCharacter merchant) {
		if (customer == null || isDialogueOpen() || merchant == null) {
			return;
		}
		
		if (toggleInventory(customer, tradingEventHandler, WindowPosition.QUARTER_X, merchant)) {
			displayedMerchant = merchant;
			tradingPanel.loadInventory(merchant, tradingEventHandler);
			addAndShow(tradingPanel, WindowPosition.THREE_QUARTERS_X);
		} 
	}
	
	/**
	 * Displays the container panels, with the character's inventory on one side, and the container's
	 * on the other.
	 *
	 * This pauses the game.
	 * 
	 * @param character
	 * @param container
	 */
	public static void displayContainerInventory(GameCharacter character, InventoryContainer container) {
		if (character == null || isDialogueOpen() || container == null) {
			return;
		}
		
		if (toggleInventory(character,inventoryEventHandler, WindowPosition.QUARTER_X)) {
			displayedContainer = container;
			containerPanel.loadInventory(container, inventoryEventHandler);
			addAndShow(containerPanel, WindowPosition.THREE_QUARTERS_X);
		} 
	}
	
	public static void displayCampPanel() {
		closeMutuallyExclusiveScreens();
		
		campPanel.rebuild();
		addAndShow(campPanel);
		gameState.pauseGame();
	}
	
	public static void hideCampPanel() {
		removeActor(campPanel);
		gameState.unpauseGame();
	}
	
	public static void toggleFormationEditor() {
		if (isDialogueOpen()) {
			return;
		}
		if (isFormationEditorOpen()) {
			removeActor(formationEditor);
			gameState.unpauseGame();
			return;
		}
		
		closeMutuallyExclusiveScreens(false);
		
		formationEditor.refresh();
		addAndShow(formationEditor);
		gameState.pauseGame();
	}
	
	/**
	 * Displays a dialog in which the player will be able to choose the target for the supplied targetable.
	 * 
	 * @param targetable
	 * @param targetSize
	 * @param callback
	 */
	public static void displayTargetSelectionDialog(GameCharacter user, Targetable targetable, TargetType targetType, TargetSelectionCallback callback) {
		closeMutuallyExclusiveScreens();
		hideToolTip();
		
		targetSelectionDialog.init(user, targetable, targetType, callback);
		addAndShow(targetSelectionDialog);
		gameState.pauseGame();
	}
	
	/**
	 * Hides the target selection dialog.
	 */
	public static void hideTargetSelectionDialog() {
		removeActor(targetSelectionDialog);
	}
	
	/**
	 * Displays the perks panel for the supplied character.
	 * 
	 * This pauses the game.
	 * 
	 * @param character
	 * @return
	 */
	public static void togglePerks(GameCharacter character) {
		toggleCharacterPanel(perksPanel, character);
	}
	
	/**
	 * Displays the character edit panel for the supplied character.
	 * 
	 * This pauses the game.
	 * 
	 * @param character
	 * @return
	 */
	public static void toggleCharacterEdit(GameCharacter character) {
		if (character == null
				|| isDialogueOpen()
				|| closeIfOpenForSameCharacter(character, characterEditPanel)) {
			return;
		}
		
		closeMutuallyExclusiveScreens(false);
		
		displayedCharacter = character;
		
		addAndShow(characterEditPanel);
		loadingWindow = new LoadingWindow<CharacterCreationWindow>(characterEditPanel,
				loadingStyle, characterEditLoader);
		
		gameState.pauseGame();
	}
	
	/**
	 * Toggles the active effects panel for the supplied character.
	 * 
	 * This pauses the game.
	 * 
	 * @param character
	 * @return
	 */
	public static void toggleActiveEffects(GameCharacter character) {
		toggleCharacterPanel(activeEffectsPanel, character);
	}
	
	public static void toggleSpellbook(GameCharacter character) {
		toggleCharacterPanel(spellbookPanel, character);
	}
	
	public static void toggleUsePerks(GameCharacter character) {
		toggleCharacterPanel(usePerksPanel, character);
	}
	
	private static void toggleCharacterPanel(CharacterPanel panel, GameCharacter character) {
		if (character == null || isDialogueOpen() || closeIfOpenForSameCharacter(character, panel)) {
			return;
		}
		
		closeMutuallyExclusiveScreens(false);
		
		displayedCharacter = character;
		panel.loadCharacter(character);
		
		addAndShow(panel);
		
		gameState.pauseGame();
	}
	
	/**
	 * Toggles the journal panel.
	 * 
	 * The game will be paused while the journal is displayed.
	 */
	public static void toggleJournal() {
		if (isDialogueOpen()) {
			return;
		}
		if (isJournalOpen()) {
			removeActor(journalPanel);
			gameState.unpauseGame();
			return;
		}
		
		closeMutuallyExclusiveScreens(false);
		
		journalPanel.refresh();
		addAndShow(journalPanel);
		gameState.pauseGame();
	}
	
	/**
	 * Toggles the map panel.
	 * 
	 * The game will be paused while the map is displayed.
	 */
	public static void toggleMapPanel() {
		if (isMapOpen()) {
			mapPanel.dispose();
			stage.getActors().removeValue(mapPanel, true);
			mapPanel = null;
			gameState.unpauseGame();
			return;
		}
		
		closeMutuallyExclusiveScreens(false);
		
		mapPanel = new MapPanel(gameState.getCurrentMap(), gameState, skin.get(MapPanelStyle.class));
		addAndShow(mapPanel);
		gameState.pauseGame();
	}
	
	/**
	 * Shows the game menu panel.
	 * 
	 * The game will be paused while the menu is displayed.
	 */
	public static void displayGameMenuPanel() {
		if (!isGameMenuOpen()) {
			closeMutuallyExclusiveScreens(false);
			addAndShow(gameMenuPanel);
			gameState.pauseGame();
		}
	}
	
	/**
	 * Shows the game options panel.
	 * 
	 * The game will be paused while the menu is displayed.
	 */
	public static void displayGameOptionsPanel() {
		if (!isGameOptionsOpen()) {
			closeMutuallyExclusiveScreens(false);
			gameOptionsPanel.setConfigurationValuesToFields();
			addAndShow(gameOptionsPanel);
			gameState.pauseGame();
		}
	}
	
	/**
	 * Hides the game menu panel.
	 * 
	 * The game will be also unpaused.
	 */
	public static void hideGameMenuPanel() {
		removeActor(gameMenuPanel);
		gameState.unpauseGame();
	}
	
	/**
	 * Hides the game options panel.
	 * 
	 * The game will be also unpaused.
	 */
	public static void hideGameOptionsPanel() {
		removeActor(gameOptionsPanel);
		gameState.unpauseGame();
	}
	
	/**
	 * Shows the load game panel.
	 * 
	 * The game will be paused while the menu is displayed.
	 */
	public static void displayLoadGamePanel() {
		if (!isPanelOpen(loadGamePanel) && !isLoadingPanel(loadGamePanel)) {
			closeMutuallyExclusiveScreens(false);
			
			addAndShow(loadGamePanel);
			loadingWindow = new LoadingWindow<LoadGamePanel>(loadGamePanel,
					loadingStyle, savedGameLoader);
			
			gameState.pauseGame();
		}
	}
	
	/**
	 * Shows the save game panel.
	 * 
	 * The game will be paused while the menu is displayed.
	 */
	public static void displaySaveGamePanel() {
		if (!isPanelOpen(saveGamePanel) && !isLoadingPanel(saveGamePanel)) {
			closeMutuallyExclusiveScreens(false);
			
			addAndShow(saveGamePanel);
			loadingWindow = new LoadingWindow<LoadGamePanel>(saveGamePanel,
					loadingStyle, savedGameLoader);
			
			gameState.pauseGame();
		}
	}
	
	/**
	 * Toggles the inventory panel of the supplied character.
	 * 
	 * If the panel is not displayed, it will be displayed, otherwise it will be hidden.
	 * 
	 * This pauses the game.
	 * 
	 * @param character
	 * @return true if the panel was displayed by this method, false if it was hidden
	 */
	public static boolean toggleInventory(GameCharacter character) {
		return toggleInventory(character, activeEventHandler == null ? inventoryEventHandler : activeEventHandler, WindowPosition.CENTER);
	}
	
	private static boolean toggleInventory(GameCharacter character, InventoryEventHandler eventHandler, WindowPosition position) {
		return toggleInventory(character, eventHandler, position, null);	
	}
	
	/**
	 * Toggles the inventory panel of the supplied character.
	 * 
	 * If the panel is not displayed, it will be displayed, otherwise it will be hidden.
	 * 
	 * This pauses the game.
	 * 
	 * @param character
	 * @return true if the panel was displayed by this method, false if it was hidden
	 */
	private static boolean toggleInventory(GameCharacter character, InventoryEventHandler eventHandler, WindowPosition position, GameCharacter merchant) {
		if (character == null || isDialogueOpen() || closeIfOpenForSameCharacter(character, inventoryPanel)) {
			return false;
		}
		
		closeMutuallyExclusiveScreens(true);
		
		activeEventHandler = eventHandler;

		displayedCharacter = character;
		eventHandler.setDisplayedCharacter(character);
		inventoryPanel.loadInventory(character, eventHandler, merchant);
		
		addAndShow(inventoryPanel, position);
		
		gameState.pauseGame();
		return true;
	}
	
	private static void addAndShow(WidgetGroup actor) {
		addAndShow(actor, WindowPosition.CENTER);
	}
	
	private static void addAndShow(WidgetGroup actor, WindowPosition position) {
		stage.addActor(actor);
		actor.setVisible(true);
		actor.pack();
		position.position(actor);
		moveToTop(actor);
	}
	
	private static void moveToTop(Actor actor) {
		actor.setZIndex(stage.getActors().size+1);
	}
	
	private static boolean closeIfOpenForSameCharacter(GameCharacter character, BorderedWindow panel) {
		if (character.equals(displayedCharacter) && (isPanelOpen(panel) || isLoadingPanel(panel))) {
			closeMutuallyExclusiveScreens(false);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Closes all panels that should not be open at the same time. This includes
	 * the inventory panels, the trading panels, the container panel,
	 * the perks panel, the use perks panel, the spellbook, the journal panel, the game menu panel,
	 * the save / load game panels, the character edit panel and the map panel.
	 * 
	 * Also, any dragged item will be returned to wherever it was dragged from
	 * and will stop being dragged
	 */
	public static void closeMutuallyExclusiveScreens() {
		closeMutuallyExclusiveScreens(false);
	}

	/**
	 * Closes all panels that should not be open at the same time. This includes
	 * the inventory panels, the trading panels, the container panel,
	 * the perks panel, the use perks panel, the spellbook, the journal panel, the game menu panel,
	 * the save / load game panels, the character edit panel the options panel and the map panel.
	 * 
	 * @param keedDragged - if false, any dragged item will be returned to wherever it was dragged from
	 *  and will stop being dragged
	 */
	public static void closeMutuallyExclusiveScreens(boolean keepDragged) {
		removeActor(inventoryPanel);
		removeActor(tradingPanel);
		removeActor(perksPanel);
		removeActor(activeEffectsPanel);
		removeActor(usePerksPanel);
		removeActor(spellbookPanel);
		removeActor(journalPanel);
		removeActor(formationEditor);
		removeActor(containerPanel);
		removeActor(gameMenuPanel);
		removeActor(gameOptionsPanel);
		boolean unload = false;
		AssetManager am = Assets.getAssetManager();
		unload = removeActor(loadGamePanel);
		unload = unload || isLoadingPanel(loadGamePanel);
		unload = unload || removeActor(saveGamePanel);
		unload = unload || isLoadingPanel(saveGamePanel);
		if (unload) {
			savedGameLoader.unload(am);
		}
		if (removeActor(characterEditPanel)) {
			characterEditLoader.load(displayedCharacter, am);
			characterEditLoader.unload(am);
		}
		if (isLoadingPanel(characterEditPanel)) {
			characterEditLoader.unload(am);
		}
		
		removeActor(loadingWindow);
		displayedMerchant = null;
		if (displayedContainer != null) {
			displayedContainer.getInventory().onInventoryClose();
			displayedContainer=null;
		}
		
		if (mapPanel != null) {
			stage.getActors().removeValue(mapPanel, true);
			mapPanel.dispose();
			mapPanel = null;
		}
		displayedCharacter = null;
		if (!keepDragged) {
			if (firstDraggedFrom != null && draggedItem !=null) {
				Inventory inventory = firstDraggedFrom.getInventory();
				inventory.addItem(draggedItem);
			}
			firstDraggedFrom = null;
			draggedItem = null;
		}
		
		activeEventHandler=null;
		
		gameState.unpauseGame();
	}
	
	private static boolean removeActor(Actor actor) {
		if (actor == null) {
			return false;
		}
		actor.setVisible(false);
		return actor.remove();
	}
	
	/**
	 * Switches the currently displayed panel to display the supplied character.
	 * 
	 * This includes the inventory, trading, container, perk, spellbook, active effects panels,
	 * character edit panel.
	 * 
	 * @param newCharacter
	 */
	public static void switchDisplayedCharacter(GameCharacter newCharacter) {
		if (displayedCharacter == null) {
			return;
		}
		if (isPanelOpen(tradingPanel) || isPanelOpen(containerPanel)) {
			displayedCharacter = newCharacter;
			activeEventHandler.setDisplayedCharacter(newCharacter);
			inventoryPanel.loadInventory(newCharacter, activeEventHandler, displayedMerchant);
		} else if (isPanelOpen(inventoryPanel)) {
			toggleInventory(newCharacter);
		} else if (isPanelOpen(perksPanel)) {
			togglePerks(newCharacter);
		} else if (isPanelOpen(usePerksPanel)) {
			toggleUsePerks(newCharacter);
		} else if (isPanelOpen(spellbookPanel)) {
			toggleSpellbook(newCharacter);
		} else if (isPanelOpen(activeEffectsPanel)) {
			toggleActiveEffects(newCharacter);
		} else if (isPanelOpen(characterEditPanel)) {
			toggleCharacterEdit(newCharacter);
		}
	}
	
	/**
	 * Returns the currently displayed character.
	 * @return
	 */
	public static GameCharacter getDisplayedCharacter() {
		return displayedCharacter;
	}
	
	/**
	 * Returns true if any window, panel or dialogue is currently open.
	 * 
	 * @return
	 */
	public static boolean isAnythingOpen() {
		return isCharacterScreenOpen() || isAnyModalOpen() || isCampOpen()
				|| isMapOpen() || isDialogueOpen() || isGameMenuOpen()
				|| isGameOptionsOpen() || isLoadGameOpen() || isSaveGameOpen() || isStorySequenceOpen();
	}

	/**
	 * Returns true if any of the character - related panels 
	 * is currently open.
	 * 
	 * @return
	 */
	public static boolean isCharacterScreenOpen() {
		return displayedCharacter != null || isJournalOpen() || isFormationEditorOpen();
	}
	
	public static boolean isCampOpen() {
		return isPanelOpen(campPanel);
	}
	
	public static boolean isStorySequenceOpen() {
		return isPanelOpen(storySequencePanel) || isLoadingPanel(storySequencePanel);
	}
	
	public static boolean isJournalOpen() {
		return isPanelOpen(journalPanel);
	}
	
	public static boolean isFormationEditorOpen() {
		return isPanelOpen(formationEditor);
	}
	
	public static boolean isMapOpen() {
		return isPanelOpen(mapPanel);
	}
	
	public static boolean isGameMenuOpen() {
		return isPanelOpen(gameMenuPanel);
	}
	
	public static boolean isGameOptionsOpen() {
		return isPanelOpen(gameOptionsPanel);
	}
	
	public static boolean isLoadGameOpen() {
		return isPanelOpen(loadGamePanel) || isLoadingPanel(loadGamePanel);
	}
	
	public static boolean isSaveGameOpen() {
		return isPanelOpen(saveGamePanel) || isLoadingPanel(saveGamePanel);
	}
	
	public static boolean isActiveEffectsScreenOpen() {
		return isPanelOpen(activeEffectsPanel);
	}
	
	/**
	 * Returns true if the perks panel is currently open.
	 */
	public static boolean isPerksScreenOpen() {
		return isPanelOpen(perksPanel);
	}
	

	/**
	 * Returns true if the use perks panel is currently open.
	 */
	public static boolean isUsePerksScreenOpen() {
		return isPanelOpen(usePerksPanel);
	}
	
	/**
	 * Returns true if the use spellbook panel is currently open.
	 */
	public static boolean isSpellbookScreenOpen() {
		return isPanelOpen(spellbookPanel);
	}
	
	/**
	 * Returns true if a dialogue panel is open.
	 * 
	 * @return
	 */
	public static boolean isDialogueOpen() {
		return isPanelOpen(dialoguePanel) || isLoadingPanel(dialoguePanel);
	}
	
	/**
	 * Returns true if the inventory panel is currently open.
	 * 
	 * This also include the trading and container panels.
	 * @return
	 */
	public static boolean isInventoryScreenOpen() {
		return isPanelOpen(inventoryPanel);
	}
	
	private static boolean isPanelOpen(Actor actor) {
		if (actor == null) {
			return false;
		}
		return  actor.isVisible() && stage.getActors().contains(actor, true);
	}
	
	private static boolean isLoadingPanel(BorderedWindow actor) {
		if (actor == null || loadingWindow == null) {
			return false;
		}
		return loadingWindow.isLoading(actor);
	}
	
	/**
	 * Set the supplied tooltip as active. This will
	 * not immediately show the tooltip, just let the manager know
	 * that this is the tooltip to display once the tooltip delay is up.
	 * 
	 * @param toolTip
	 * @see Configuration#getTooltipDelay()
	 */
	public static void setToolTip(Actor toolTip) {
		if (!CoreUtil.equals(UIManager.toolTip, toolTip)) {
			tooltipDelayCounter = 0;
			UIManager.toolTip = toolTip;
			UIManager.toolTip.setVisible(false);
		}
	}
	
	/**
	 * Returns true if the supplied tooltip is the currently active tooltip.
	 * @param tooltip
	 * @return
	 */
	public static boolean isThisTooltipSet(Actor tooltip) {
		if (tooltip == null) {
			return false;
		}
		return CoreUtil.equals(tooltip, UIManager.toolTip);
	}
	
	private static void showTooltipIfNecessarry(float delta) {
		if (toolTip != null) {
			tooltipDelayCounter += delta;
			if (tooltipDelayCounter > Configuration.getTooltipDelay()) {
				toolTip.setVisible(true);
			}
		}
	}
	
	/**
	 * Hides any displayed tooltip and prevents it from appearing again.
	 */
	public static void hideToolTip() {
		toolTip = null;
	}
	
	/**
	 * Updates all UI elements.
	 * 
	 * @param deltaTime
	 */
	public static void updateUI(float deltaTime) {
		stage.act(Math.min(deltaTime, 1 / 30f));
		showTooltipIfNecessarry(deltaTime);
		pausedIndicator.setVisible(GameState.isPaused());
	}

	/**
	 * Draws all visible UI elements.
	 */
	public static void drawUI() {
		if (!pcPanel.isVisible()) {
			pcPanel.setVisible(true);
			onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
		stage.draw();
		drawActorsStuckToCursor();
	}
	
	/**
	 * Refreshes the PC panel. Should be called if members of the 
	 * player character group change.
	 */
	public static void refreshPCPanel() {
		if (pcPanel != null) {
			pcPanel.loadPCPanels(gameState);
		}
	}
	
	/**
	 * Returns true if the supplied actor is part of of PlayerCharacterPortrait,
	 * or the portrait itself.
	 * 
	 * @param actor
	 * @return
	 */
	public static boolean isPCPortrait(Actor actor) {
		if (pcPanel != null) {
			return pcPanel.isPCPortrait(actor);
		}
		return false;
	}
	
	/**
	 * This will draw all UI elements that are currently
	 * following the cursor - tooltips, dragged items, etc.
	 * 
	 * @param batch
	 */
	private static void drawActorsStuckToCursor() {

		if (draggedItem == null && (toolTip == null || !toolTip.isVisible())) {
			return;
		}
		
		batch.begin();
		if (draggedItem != null) {
			TextureRegion draggedIcon = Assets.getTextureRegion(draggedItem.getInventoryIconFile());
			batch.draw(draggedIcon, movedX, movedY-32,draggedIcon.getRegionWidth()*Configuration.getMapScale(),draggedIcon.getRegionHeight()*Configuration.getMapScale());
			if (draggedItem.getStackSize() > 1) {
				getStackFont().draw(batch, Integer.toString(draggedItem.getStackSize()), movedX+2, movedY-32+UIManager.getStackFont().getCapHeight()+2);
			}
		} if (toolTip != null && toolTip.isVisible()) {
			updateToolTipPosition(batch);
			toolTip.draw(batch, 1f);
		}
		batch.end();
	}
	
	/**
	 * Sets the position at which hovered items should be rendered.
	 * This position will be used until the next time a mouse is moved,
	 * after that it will be reset to the cursor's position.
	 * 
	 * The supplied coordinates must be in the stage coordinate system.
	 * @param x
	 * @param y
	 */
	public static void setHoveredItemsPosition(float x, float y) {
		movedX = x;
		movedY = y;
	}
	
	/**
	 * Updates the tooltip's position making sure no part of it is outside of the screen.
	 * 
	 * @param batch
	 */
	private static void updateToolTipPosition(SpriteBatch batch) {
		float x = movedX;
		float y = movedY;
		Vector2 tempVector = MathUtil.getVector2().set(x, y);
		
		stage.stageToScreenCoordinates(tempVector);//, batch.getTransformMatrix());
		
		boolean needTranslatingX = false;
		boolean needTranslatingY = false;
		if (tempVector.x < 0) {
			tempVector.x = 0;
			needTranslatingX = true;
		} else if (tempVector.x + toolTip.getWidth()> Gdx.graphics.getWidth()) {
			tempVector.x = Gdx.graphics.getWidth() - toolTip.getWidth();
			needTranslatingX = true;
		}
		
		if (tempVector.y < 0) {
			tempVector.y = 0;
			needTranslatingY = true;
		} else if (((Gdx.graphics.getHeight() - tempVector.y) + toolTip.getHeight())> Gdx.graphics.getHeight()) {
			tempVector.y = 0 + toolTip.getHeight();
			needTranslatingY = true;
		}
		
		if (needTranslatingX || needTranslatingY) {
			stage.screenToStageCoordinates(tempVector);
			if (needTranslatingX) {
				x = tempVector.x;
			}
			if (needTranslatingY) {
				y = tempVector.y;
			}
		}
		
		MathUtil.freeVector2(tempVector);
		toolTip.setX(x);
		toolTip.setY(y);
	}

	/**
	 * Sets the dragged item to the supplied one.
	 * @param item
	 */
	public static void setDraggedItem(InventoryItem item) {
		draggedItem = item;
	}
	
	/**
	 * Sets the dragged item to the supplied one, remembering where it was dragged from.
	 * 
	 * @param item
	 * @param firstDraggedFrom
	 */
	public static void setDraggedItem(InventoryItem item, InventoryItemButton firstDraggedFrom) {
		draggedItem = item;
		UIManager.firstDraggedFrom = firstDraggedFrom;
	}
	
	/**
	 * Returns the slot from which the currently dragged item 
	 * was picked up.
	 * 
	 * @return
	 */
	public static InventoryItemButton getDraggedFrom() {
		if (activeEventHandler == null || draggedItem == null) {
			return null;
		}
		return activeEventHandler.getDraggedFrom();
	}
	
	/**
	 * Returns true if the currently dragged item is dragged from a merchant
	 * in the trading panel.
	 * @return
	 */
	public static boolean isDraggedFromMerchant() {
		if (tradingPanel != null && tradingPanel.isVisible()) {
			return tradingEventHandler.isDraggedFromMerchant();
		}
		return false;
	}
	
	/**
	 * Returns the currently displayed merchant in the trading panel.
	 * 
	 * @return
	 */
	public static GameCharacter getDisplayedMerchant() {
		return displayedMerchant;
	}
	
	/**
	 * Returns the currently dragged item.
	 * 
	 * @return
	 */
	public static InventoryItem getDraggedItem() {
		return draggedItem;
	}
	
	/**
	 * Returns the font used to render the stack size of inventory items.
	 * @return
	 */
	public static BitmapFont getStackFont() {
		return stackFont;
	}
	
	/**
	 * Returns the font used to render cost of actions during combat.
	 * @return
	 */
	public static BitmapFont getCombatPathFont() {
		return combatPathFont;
	}
	
	/**
	 * Returns the font used to render text floating above game object.
	 * @return
	 */
	public static BitmapFont getFloatingTextFont() {
		return floatingTextFont;
	}
	
	/**
	 * Returns the font used to render text on the loading screens.
	 * @return
	 */
	public static BitmapFont getLoadingFont() {
		return loadingFont;
	}
	
	/**
	 * Gathers all assets that should be loaded by the AssetManager
	 * prior to displaying UI.
	 * 
	 * @param assetStore
	 */
	public static void loadUIAssets() {
		Assets.getAssetManager().load(Configuration.getFolderUI()+"uiStyle.json", Skin.class);
	}
	
	public static void setCursorForTool(Tool tool) {
		Cursor cursor = defaultCursor;
		if (tool == Tool.LOCKPICK) {
			cursor = lockpickCursor;
		} else if (tool == Tool.DISARM) {
			cursor = disarmCursor;
		}
		Gdx.graphics.setCursor(cursor);
	}
	
	private static void buildCursors(AssetManager am) {
		defaultCursor = Gdx.graphics.newCursor(getCursorPixmapForPath(am, Configuration.getDefaultCursorPath()), 0, 0);
		disarmCursor = Gdx.graphics.newCursor(getCursorPixmapForPath(am, Configuration.getDisarmCursorPath()), 0, 0);
		lockpickCursor = Gdx.graphics.newCursor(getCursorPixmapForPath(am, Configuration.getLockpickCursorPath()), 0, 0);
	}
	
	private static Pixmap getCursorPixmapForPath(AssetManager am, String texturePath) {
		if (texturePath != null) {
			Texture cursorTexture = am.get(texturePath);
			TextureData data = cursorTexture.getTextureData();
			if (!data.isPrepared()) {
				data.prepare();
			}
			return data.consumePixmap();
		}
		return null;
	}
	
	public static void resetUI() {
		closeMutuallyExclusiveScreens();
		hideCampPanel();
		hideProgressDialog();
		hideTargetSelectionDialog();
		hideToolTip();
		hideDialogue();
		hideStorySequence();
		endCombatMode();
		if (logPanel != null) {
			logPanel.clearMessages();
		}
	}
	
	public static void dispose() {
		if (stage != null) {
			stage.dispose();
		}
		if (stackFont != null) {
			stackFont.dispose();
		}
		if (batch != null) {
			batch.dispose();
		}
		if (defaultCursor != null) {
			defaultCursor.dispose();
			defaultCursor = null;
		}
		if (disarmCursor != null) {
			disarmCursor.dispose();
			disarmCursor = null;
		}
		if (lockpickCursor != null) {
			lockpickCursor.dispose();
			lockpickCursor = null;
		}
	}
	
	public static void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_UI);
		if (campPanel != null) {
			campPanel.writeToXML(writer);
		}
		writer.pop();
	}
	
	public static void loadFromXML(Element element) {
		Element uiElement = element.getChildByName(XML_UI);
		if (uiElement != null) {
			campPanel.loadFromXML(uiElement);
		}
	}
	
}
