package mg.fishchicken.core.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.MainInputProcessor.EventType;
import mg.fishchicken.core.input.tools.Tool;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.Pair;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.CastSpellAction;
import mg.fishchicken.gamelogic.actions.FlickerAction;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.actions.UsePerkAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.combat.CombatPath;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.PickableGameObject;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.traps.TrapLocation;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.pathfinding.Path.Step;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.saveload.SaveGameDetails;
import mg.fishchicken.ui.tooltips.CombatTooltip;

public class PlayerCharacterController implements InputConsumer, EventListener {

	private static enum ClickConfirmationAction {
		CONFIRM, CANCEL, EXECUTE
	};

	private PlayerCharacterGroup group;
	private Rectangle selection;
	private boolean selectionInProgress;
	private MainInputProcessor mip;
	private Vector2 tempVector;
	private boolean targetSelectionInProgress;
	private TargetType targetType;
	private Targetable targetable;
	private TargetSelectionCallback targetSelectionCallback;
	private GameCharacter user;
	private CombatPath combatPath;
	private boolean waitingForClickConfirmation;
	private Vector2 lastClick;
	private CombatTooltip combatTooltip; // lazy
	private boolean overUIElement, overCharacterPortrait;
	private TrapLocation trapLocationCurrentlyHovered;
	private boolean highlightUsables;
	private boolean onlyMoveActionAllowed;
	private GameState gameState;
	private Tool activeTool;
	private Vector2 mouseTileCoordinates;
	private boolean multiSelectActive;
	private Array<GameCharacter> charactersToSelect;
	private Array<GameCharacter> charactersToDeselect;

	public PlayerCharacterController(GameState gameState, PlayerCharacterGroup group) {
		this.group = group;
		this.gameState = gameState;
		charactersToSelect = new Array<GameCharacter>();
		charactersToDeselect = new Array<GameCharacter>();
		multiSelectActive = false;
		selection = new Rectangle();
		selectionInProgress = false;
		tempVector = new Vector2();
		mouseTileCoordinates = new Vector2();
		waitingForClickConfirmation = false;
		lastClick = new Vector2();
		combatPath = new CombatPath();
		overUIElement = false;
		overCharacterPortrait = false;
		highlightUsables = false;
		onlyMoveActionAllowed = false;
		activeTool = null;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (KeyBindings.HIGHLIGHT_USABLES.is(keycode)) {
			highlightUsables = true;
		} else if (KeyBindings.MOVE_ONLY.is(keycode)) {
			onlyMoveActionAllowed = true;
			recalculateCombatPathAndTooltip();
		} else if (KeyBindings.MULTI_SELECT.is(keycode)) {
			multiSelectActive = true;
		}
		return false;
	}

	private GameCharacter determineCharacterToShow(boolean pcOnly) {
		return UIManager.getDisplayedCharacter() != null ? UIManager.getDisplayedCharacter() : group
				.getGroupLeader(pcOnly);
	}

	@Override
	public boolean keyUp(int keycode) {
		if (UIManager.isAnyModalOpen()) {
			return false;
		}
		if (KeyBindings.END_TURN.is(keycode)) {
			if (GameState.isPlayersTurn()) {
				gameState.unpauseGame();
				gameState.switchToNextSide();
				UIManager.hideToolTip();
				return true;
			}
		} else if (KeyBindings.CANCEL.is(keycode)) {
			if (targetSelectionInProgress) {
				stopTargetSelection(false);
				return true;
			}
			if (UIManager.isAnythingOpen()) {
				UIManager.closeMutuallyExclusiveScreens();
			} else {
				UIManager.displayGameMenuPanel();
			}
		} else if (KeyBindings.QUICK_SAVE.is(keycode)) {
			gameState.saveGame(SaveGameDetails.QUICK_SAVE, "Quicksave");
			return true;
		} else if (KeyBindings.QUICK_LOAD.is(keycode)) {
			gameState.loadGame(SaveGameDetails.QUICK_SAVE, new OkCancelCallback<GameMap>() {
				@Override
				public void onError(String errorMessage) {
					gameState.exitGameToMainMenu(Strings.getString(UIManager.STRING_TABLE, "errorLoadingGame",
							"Quicksave"));
				}
			});
			return true;
		} else if (KeyBindings.MOVE_ONLY.is(keycode)) {
			onlyMoveActionAllowed = false;
			recalculateCombatPathAndTooltip();
		} else if (KeyBindings.HIGHLIGHT_USABLES.is(keycode)) {
			highlightUsables = false;
		} else if (KeyBindings.MULTI_SELECT.is(keycode)) {
			multiSelectActive = false;
		} else if (KeyBindings.PAUSE.is(keycode)) {
			if (GameState.isPaused() && !UIManager.isCharacterScreenOpen() && !UIManager.isMapOpen()) {
				gameState.unpauseGame(true);
			} else {
				gameState.pauseGame(true);
			}
			;
			return true;
		} else if (KeyBindings.TOGGLE_GRID.is(keycode)) {
			gameState.getCurrentMap().toggleGrid();
			return true;
		} else if (KeyBindings.DISPLAY_INVENTORY.is(keycode)) {
			GameCharacter leader = determineCharacterToShow(true);
			if (leader != null) {
				leader.displayInventory();
			}
			return true;
		} else if (KeyBindings.DISPLAY_PERKS.is(keycode)) {
			UIManager.togglePerks(determineCharacterToShow(true));
			return true;
		} else if (KeyBindings.DISPLAY_ACTIVE_EFFECTS.is(keycode)) {
			UIManager.toggleActiveEffects(determineCharacterToShow(true));
			return true;
		} else if (KeyBindings.DISPLAY_USE_PERK.is(keycode)) {
			GameCharacter leader = determineCharacterToShow(false);
			if (leader != null && leader.canPerformAction(UsePerkAction.class)) {
				UIManager.toggleUsePerks(leader);
				return true;
			}
		} else if (KeyBindings.DISPLAY_JOURNAL.is(keycode)) {
			UIManager.toggleJournal();
			return true;
		} else if (KeyBindings.DISPLAY_CHARACTER_EDIT.is(keycode)) {
			UIManager.toggleCharacterEdit(determineCharacterToShow(true));
			return true;
		} else if (KeyBindings.DISPLAY_SPELLBOOK.is(keycode)) {
			GameCharacter leader = determineCharacterToShow(false);
			if (leader != null && leader.canPerformAction(CastSpellAction.class)) {
				UIManager.toggleSpellbook(leader);
				return true;
			}
		} else if (KeyBindings.DISPLAY_FORMATION_EDITOR.is(keycode)) {
			UIManager.toggleFormationEditor();
			return true;
		} else if (KeyBindings.REST.is(keycode)) {
			GameState.getPlayerCharacterGroup().sleepOrCamp();
			return true;
		} else if (KeyBindings.DISPLAY_MAP.is(keycode)) {
			UIManager.toggleMapPanel();
			return true;
		} else if (KeyBindings.SNEAK.is(keycode)) {
			gameState.toggleStealth();
			return true;
		} else if (KeyBindings.DISARM.is(keycode)) {
			toggleTool(Tool.DISARM);
			return true;
		} else if (KeyBindings.DETECT.is(keycode)) {
			group.toggleDetectTraps();
			return true;
		} else if (KeyBindings.LOCKPICK.is(keycode)) {
			toggleTool(Tool.LOCKPICK);
			return true;
		} else if (KeyBindings.TALKTO.is(keycode)) {
			toggleTool(Tool.TALKTO);
			return true;
		} else if (KeyBindings.ATTACK.is(keycode)) {
			toggleTool(Tool.ATTACK);
			return true;
		} else if (KeyBindings.SELECT_ALL.is(keycode)) {
			GameCharacter oldLeader = group.getGroupLeader(true);
			group.selectAll();
			GameCharacter leader = group.getGroupLeader(true);
			if (oldLeader != leader) { // identity check ok
				leader.getAudioProfile().playCharacterBark(leader);
			}
			return true;
		}

		int numberPressed = KeyBindings.getNumberPressed(keycode);
		if (numberPressed > 0 && numberPressed <= group.getPlayerCharacters().size) {
			GameCharacter selectedCharacter = group.getPlayerCharacters().get(numberPressed - 1);
			GameCharacter displayedCharacter = UIManager.getDisplayedCharacter();
			if (displayedCharacter != null && !selectedCharacter.equals(displayedCharacter)) {
				UIManager.switchDisplayedCharacter(selectedCharacter);
			} else {
				if (!multiSelectActive) {
					boolean wasSelected = selectedCharacter.isSelected();
					group.selectOnlyMember(selectedCharacter);
					if (!wasSelected) {
						selectedCharacter.getAudioProfile().playCharacterBark(selectedCharacter);
					}
				} else {
					group.selectMember(selectedCharacter);
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(float screenX, float screenY, int pointer, int button) {
		selection.setX(screenX);
		selection.setY(Gdx.graphics.getHeight() - screenY);
		selection.setWidth(0);
		selection.setHeight(0);
		return true;
	}

	@Override
	public boolean touchUp(float screenX, float screenY, int pointer, int button) {
		if (UIManager.isAnyModalOpen() || gameState.getCurrentMap() == null) {
			return false;
		}

		UIManager.hideToolTip();

		if (shouldIgnoreTouchUp()) {
			return false;
		}
		
		if (activeTool != null && Buttons.RIGHT == button) {
			toggleTool(activeTool);
			return true;
		}

		if (UIManager.isCharacterScreenOpen()) {
			if (UIManager.getDraggedItem() != null) {
				if (!gameState.getCurrentMap().isWorldMap()) {
					dropDraggedItemToTheGround();
				}
			} else {
				UIManager.closeMutuallyExclusiveScreens();
			}
			return true;
		}

		if (UIManager.isMapOpen()) {
			UIManager.toggleMapPanel();
			return true;
		}

		if (UIManager.isAnythingOpen()) {
			return true;
		}

		gameState.getCurrentMap().projectToTiles(tempVector.set(screenX, screenY));

		if (handleTargetSelection(button, tempVector)) {
			return true;
		}
		GameObject clickedGameObject = determineClickedGameObject(tempVector);
		if (handleCharacterSelection(clickedGameObject)) {
			return true;
		}
		if (handleCombatPath(screenX, screenY, tempVector)) {
			return true;
		}
		if (handleGroupActions(clickedGameObject, tempVector)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean touchDragged(float screenX, float screenY, int pointer) {
		if (!gameState.getCurrentMap().isWorldMap() && !UIManager.isAnythingOpen() && !GameState.isCombatInProgress()
				&& !targetSelectionInProgress) {
			selectionInProgress = true;
			selection.setWidth(screenX - selection.x);
			selection.setHeight(-screenY + (Gdx.graphics.getHeight() - selection.y));
			return true;
		}
		return false;
	}

	@Override
	public boolean touchDragFinished(float screenX, float screenY, int pointer, int button) {
		if (selectionInProgress) {
			selectionInProgress = false;
			MathUtil.normalizeRectangle(selection);
			if (selection.getHeight() > Configuration.getSelectionTolerance()
					&& selection.getWidth() > Configuration.getSelectionTolerance()) {
				charactersToSelect.clear();
				charactersToDeselect.clear();
				GameCharacter oldLeader = group.getGroupLeader(true);
				for (GameCharacter pc : group.getMembers()) {
					tempVector.set(pc.position().getX(), pc.position().getY());
					gameState.getCurrentMap().projectFromTiles(tempVector);
					mip.translateFromCameraToWindow(tempVector);
					if (selection.contains(tempVector.x + pc.getCircleOffsetX(), tempVector.y + pc.getCircleOffsetY())) {
						charactersToSelect.add(pc);
					} else if (!isMultiSelectActive()) {
						charactersToDeselect.add(pc);
					}
				}

				for (GameCharacter pc : charactersToSelect) {
					group.selectMember(pc);
				}

				if (!isMultiSelectActive() && charactersToSelect.size > 0) {
					GameCharacter leader = group.getGroupLeader(true);

					if (oldLeader != leader) { // identity check ok
						leader.getAudioProfile().playCharacterBark(leader);
					}

					for (GameCharacter pc : charactersToDeselect) {
						group.deselectMember(pc);
					}
				}

			} else {
				touchUp(screenX, screenY, pointer, button);
			}
		}

		return false;
	}

	@Override
	public boolean mouseMoved(float screenX, float screenY) {
		if (gameState.getCurrentMap() != null) {
			Vector3 projectedCoordinates = MathUtil.getVector3();
			mip.translateFromWindowToCamera(projectedCoordinates.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			gameState.getCurrentMap().projectToTiles(projectedCoordinates);
			mouseTileCoordinates.set(projectedCoordinates.x, projectedCoordinates.y);
			MathUtil.freeVector3(projectedCoordinates);
		}
		if (UIManager.isAnythingOpen()) {
			return false;
		}
		updateTargetSelection(screenX, screenY, false);
		updateCombatPath(screenX, screenY);
		determineHoveredUsableGameObject();
		return false;
	}

	/**
	 * Returns true if mutliple character selection is currently active, meaning
	 * selecting one character will not automatically deselect the others
	 * 
	 * @return
	 */
	public boolean isMultiSelectActive() {
		return multiSelectActive && !GameState.isCombatInProgress();
	}

	/**
	 * Returns true if target selection is currently in progress.
	 * 
	 * @return
	 */
	public boolean isTargetSelectionInProgress() {
		return targetSelectionInProgress;
	}

	/**
	 * Returns the current x coordinate of the mouse cursor in the tile
	 * coordinate system.
	 * 
	 * @return
	 */
	public float getMouseTileX() {
		return mouseTileCoordinates.x;
	}

	/**
	 * Returns the current y coordinate of the mouse cursor in the tile
	 * coordinate system.
	 * 
	 * @return
	 */
	public float getMouseTileY() {
		return mouseTileCoordinates.y;
	}

	@Override
	public boolean scrolled(float amount) {
		return false;
	}

	@Override
	public boolean handle(Event event) {
		// disable the combat path in case any UI element is being hovered
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.enter.equals(inputEvent.getType())) {
				overUIElement = true;
				if (UIManager.isThisTooltipSet(combatTooltip)) {
					UIManager.hideToolTip();
				}
			}
			if (Type.exit.equals(inputEvent.getType())) {
				overUIElement = false;
				overCharacterPortrait = false;
			}
			if (Type.mouseMoved.equals(inputEvent.getType())) {
				Actor hitActor = UIManager.getStage().hit(inputEvent.getStageX(), inputEvent.getStageY(), true);
				overUIElement = hitActor != null;
				overCharacterPortrait = UIManager.isPCPortrait(hitActor);
			}
		}
		return false;
	}

	/**
	 * Sets the supplied tool as the currently active tool for the party.
	 * 
	 * @param tool
	 */
	public void toggleTool(Tool tool) {
		if (activeTool == tool) {
			activeTool = null;
		} else {
			activeTool = tool;
		}
		UIManager.setCursorForTool(activeTool);
	}

	/**
	 * Returns the tool that's currently active for the party.
	 * 
	 * @return
	 */
	public Tool getActiveTool() {
		return activeTool;
	}

	/**
	 * Returns the rectangle the player dragged while selecting characters if a
	 * selection is in progress, or null otherwise.
	 * 
	 * @return
	 */
	public Rectangle getSelectionRectangle() {
		return selectionInProgress ? selection : null;
	}

	/**
	 * Returns true if the player indicated all usables should be highlighted.
	 * 
	 * @return
	 */
	public boolean shouldHighlightUsables() {
		return highlightUsables;
	}

	/**
	 * Returns true if the player indicated only Move actions should be
	 * performed by player characters.
	 * 
	 * @return
	 */
	public boolean onlyMoveActionAllowed() {
		return onlyMoveActionAllowed;
	}

	/**
	 * Returns the trap location the player is currently hovering the mouse
	 * cursor over.
	 * 
	 * @return
	 */
	public TrapLocation getTrapLocationCurrentlyHovered() {
		return trapLocationCurrentlyHovered;
	}

	private boolean shouldIgnoreTouchUp() {
		return GameState.isCombatInProgress()
				&& (!GameState.isPlayersTurn() || (group.getGroupLeader() != null && group.getGroupLeader()
						.brain().blockingTurnActionInProgress()));
	}

	private boolean handleGroupActions(GameObject clickedGameObject, Vector2 tileCoordinates) {
		if (group.getSelectedSize() > 0 && group.hasActiveMembers()) {

			Class<? extends Action> actionClass = null;
			PositionedThing actionTarget = clickedGameObject;
			GameMap currentMap = gameState.getCurrentMap();

			if (activeTool != null) {
				Pair<Class<? extends Action>, PositionedThing> pair = activeTool.getActionForObject(this,
						group.getGroupLeader(), currentMap, clickedGameObject);
				if (pair != null) {
					actionClass = pair.getLeft();
					actionTarget = pair.getRight();
				} else {
					return false;
				}
			}

			if (actionClass == null) {
				actionClass = currentMap.getActionForTarget(group.getGroupLeader(), clickedGameObject);
			}

			if (actionClass != null) {
				if (actionTarget instanceof GameCharacter) {
					((GameCharacter) actionTarget).getCharacterCircle().addAction(FlickerAction.class);
				}

				if (MoveToAction.class.equals(actionClass)) {
					group.moveTo((int) tileCoordinates.x, (int) tileCoordinates.y);
				} else {
					group.addAction(actionClass, actionTarget);
				}
				if (activeTool != null) {
					toggleTool(activeTool);
				}
				if (currentMap.isWorldMap() && Action.VERB_ACTIONS.contains(actionClass, true)) {
					gameState.unpauseGame();
				}
			}
			return true;
		}
		return false;
	}

	private boolean handleCombatPath(float screenX, float screenY, Vector2 tileCoordinates) {
		if (GameState.isPlayersTurn()) {
			ClickConfirmationAction action = handleClickConfirmation(tileCoordinates, combatPath);
			if (action == ClickConfirmationAction.CONFIRM) {
				GameCharacter leader = group.getGroupLeader();
				if (leader != null) {
					leader.setOrientation(Orientation.calculateOrientationToTarget(leader.getMap().isIsometric(),
							leader.position().getX(), leader.position().getY(), tileCoordinates.x, tileCoordinates.y));
				}
				return true;
			} else if (action == ClickConfirmationAction.CANCEL) {
				destroyCombatPath();
				mouseMoved(screenX, screenY);
				return true;
			}
		}

		destroyCombatPath();
		UIManager.hideToolTip();
		return false;
	}

	private boolean handleCharacterSelection(GameObject clickedGameObject) {
		if (clickedGameObject instanceof GameCharacter && group.containsCharacter((GameCharacter) clickedGameObject) && activeTool == null) {
			GameCharacter pc = (GameCharacter) clickedGameObject;
			if (!isMultiSelectActive()) {
				GameCharacter leader = group.getGroupLeader(true);
				group.selectOnlyMember(pc);
				if (leader != pc) { // identity check ok
					pc.getAudioProfile().playCharacterBark(pc);
				}
			} else {
				group.toggleMemberSelection(pc);
			}
			destroyCombatPath();
			return true;
		}
		return false;
	}

	private GameObject determineClickedGameObject(Vector2 tileCoordinates) {
		ObjectSet<GameObject> clickedGameObjects = gameState.getCurrentMap().getAllGameObjectsAt(tileCoordinates.x,
				tileCoordinates.y);

		GameObject clickedGameObject = null;
		// we first determine if any characters were clicked on - if so, they
		// have priority
		for (GameObject go : clickedGameObjects) {
			if (go instanceof GameCharacter) {
				clickedGameObject = go;
				break;
			}
		}
		// if no characters were found, just take a random pick
		if (clickedGameObject == null && clickedGameObjects.size > 0) {
			clickedGameObject = clickedGameObjects.iterator().next();
		}
		return clickedGameObject;
	}

	public boolean handleTargetSelection(int button, Vector2 tileCoordinates) {
		if (targetSelectionInProgress) {
			if (!group.getGroupLeader().canSeeTile((int) tileCoordinates.x, (int) tileCoordinates.y)) {
				return true;
			}
			if (Buttons.RIGHT == button) {
				if (!waitingForClickConfirmation) {
					stopTargetSelection(false);
				} else {
					waitingForClickConfirmation = false;
				}
				return true;
			}

			if (!waitingForClickConfirmation) {
				targetType.setTarget((int) tileCoordinates.x, (int) tileCoordinates.y, gameState.getCurrentMap());
			}
			boolean validTarget = targetType.isValidTarget();
			ClickConfirmationAction action = handleClickConfirmation(tileCoordinates, targetType);
			if (action == ClickConfirmationAction.CONFIRM) {
				if (!validTarget) {
					waitingForClickConfirmation = false;
				}
				return true;
			} else if (action == ClickConfirmationAction.CANCEL) {
				stopTargetSelection(false);
				return true;
			}
			if (validTarget) {
				stopTargetSelection(true);
			}
			return true;
		}
		return false;
	}

	private ClickConfirmationAction handleClickConfirmation(Vector2 clickedTileCoordinates, CombatPath pathToCheck) {
		if (!waitingForClickConfirmation && pathToCheck.getLength() > 0) {
			lastClick.set(clickedTileCoordinates);
			waitingForClickConfirmation = true;
			return ClickConfirmationAction.CONFIRM;
		}
		if (waitingForClickConfirmation && pathToCheck.getLength() < 1) {
			waitingForClickConfirmation = false;
		}
		if (waitingForClickConfirmation) {
			if (((int) lastClick.x != (int) clickedTileCoordinates.x || (int) lastClick.y != (int) clickedTileCoordinates.y)
					&& !pathToCheck.contains((int) clickedTileCoordinates.x, (int) clickedTileCoordinates.y)) {
				waitingForClickConfirmation = false;
				return ClickConfirmationAction.CANCEL;
			}
		}
		waitingForClickConfirmation = false;
		return ClickConfirmationAction.EXECUTE;
	}

	/**
	 * Returns true if we are currently waiting for click confirmation.
	 * 
	 * @return
	 */
	public boolean getWaitingForClickConfirmation() {
		return waitingForClickConfirmation;
	}

	/**
	 * Tells the controller not to wait for the click confirmation anymore.
	 */
	public void resetWaitingForClickConfirmation() {
		waitingForClickConfirmation = false;
	}

	private void dropDraggedItemToTheGround() {
		InventoryItem draggedItem = UIManager.getDraggedItem();
		if (UIManager.getDraggedFrom() == null) {
			Log.log("Cannot determine dragedFrom for item {0},  not allowing to drop the item.", LogType.ERROR,
					draggedItem.getId());
			return;
		}
		InventoryContainer ic = UIManager.getDraggedFrom().getContainer();

		// only drop it if it belongs to the PC
		if (ic instanceof PlayerCharacterGroup
				|| (ic instanceof GameCharacter && ((GameCharacter) ic).isMemberOfPlayerGroup())) {
			Tile characterPosition = UIManager.getDisplayedCharacter().position().tile();
			new PickableGameObject(draggedItem, characterPosition.getX(), characterPosition.getY(),
					gameState.getCurrentMap());
			UIManager.setDraggedItem(null, null);
			Log.logLocalized("itemDropped", LogType.INVENTORY, draggedItem.getName(), UIManager.getDisplayedCharacter()
					.getName());
		}
	}

	private void determineHoveredUsableGameObject() {
		if (!targetSelectionInProgress) {
			GameMap map = gameState.getCurrentMap();
			if (map != null && mouseTileCoordinates.x >= 0 && mouseTileCoordinates.y >= 0
					&& mouseTileCoordinates.x < map.getMapWidth() && mouseTileCoordinates.y <= map.getMapHeight()) {
				if (map.shouldRenderTile((int) mouseTileCoordinates.x, (int) mouseTileCoordinates.y)) {			
					trapLocationCurrentlyHovered = map
							.getTrapLocationAt(mouseTileCoordinates.x, mouseTileCoordinates.y);
				}
			}
		}
	}

	/**
	 * If target selection is is progress, this will update the target selection
	 * indicator using the supplied tile coordinates.
	 * 
	 * @param screenX
	 * @param screenY
	 */
	public void updateTargetSelection(Tile tile) {
		updateTargetSelection(tile.getX(), tile.getY(), true);
	}

	private void updateTargetSelection(float screenX, float screenY, boolean isTileCoordinates) {
		if (targetSelectionInProgress) {
			if (!waitingForClickConfirmation) {
				Vector3 tempVector3 = MathUtil.getVector3().set(screenX, screenY, 1);
				if (!isTileCoordinates) {
					gameState.getCurrentMap().projectToTiles(tempVector3);
				}
				if (user.canSeeTile((int) tempVector3.x, (int) tempVector3.y)) {
					targetType.setTarget((int) tempVector3.x, (int) tempVector3.y, gameState.getCurrentMap());
				}
				MathUtil.freeVector3(tempVector3);
			}
			UIManager.hideToolTip();
		}
	}

	private void updateCombatPath(float screenX, float screenY) {
		// do not update the path if it should be disabled, or if we are
		// confirming a click
		if (isCombatPathDisabled() || waitingForClickConfirmation) {
			return;
		}
		Vector3 tempVector3 = MathUtil.getVector3().set(screenX, screenY, 1);
		gameState.getCurrentMap().projectToTiles(tempVector3);

		boolean needToRecalculate = combatPath.getLength() < 1;
		if (!needToRecalculate) {
			Step step = combatPath.getStep(combatPath.getLength() - 1);
			needToRecalculate = (step.getX() != (int) tempVector3.x || step.getY() != (int) tempVector3.y);
		}
		if (needToRecalculate) {
			recalculateCombatPath((int) tempVector3.x, (int) tempVector3.y);
			refreshTooltip(tempVector3);
		}

		MathUtil.freeVector3(tempVector3);
	}

	private void refreshTooltip() {
		refreshTooltip(null);
	}

	private void refreshTooltip(Vector3 mouseCoordinates) {
		Step lastStep = combatPath.getLastStep();
		if (lastStep == null) {
			UIManager.hideToolTip();
			return;
		}
		// use the supplied coordinates only if the path has no last step
		int x = lastStep != null ? lastStep.getX() : (int) mouseCoordinates.x;
		int y = lastStep != null ? lastStep.getY() : (int) mouseCoordinates.y;

		if (group.getGroupLeader().position().tile().equals(x, y)) {
			UIManager.hideToolTip();
			return;
		}

		if (combatPath.getAction() != null || targetSelectionInProgress) {
			if (combatTooltip == null) {
				combatTooltip = new CombatTooltip();
			}
			combatTooltip.update(
					targetSelectionInProgress ? targetable.getName()
							: Action.getUINameForAction(combatPath.getAction()), combatPath);
			UIManager.setToolTip(combatTooltip);
		} else {
			UIManager.hideToolTip();
		}
	}

	private void recalculateCombatPathAndTooltip() {
		if (isCombatPathDisabled() || combatPath.getLength() < 1) {
			return;
		}
		Vector3 tempVector = MathUtil.getVector3().set(Gdx.input.getX(), Gdx.input.getY(), 0);
		gameState.getCurrentMap().getCamera().unproject(tempVector);
		gameState.getCurrentMap().projectToTiles(tempVector);
		int x = (int) tempVector.x;
		int y = (int) tempVector.y;
		MathUtil.freeVector3(tempVector);
		recalculateCombatPath(x, y);
		refreshTooltip();
	}

	private void recalculateCombatPath(int x, int y) {
		combatPath.clear();
		GameCharacter leader = group.getGroupLeader();
		if (!targetSelectionInProgress) {
			combatPath.setTarget(leader, x, y, gameState.getCurrentMap());
		}
	}

	private void destroyCombatPath() {
		if (combatPath.getLength() > 0) {
			combatPath.clear();
		}
		if (!targetSelectionInProgress) {
			resetWaitingForClickConfirmation();
		}
	}

	public CombatPath getCombatPath() {
		if (isCombatPathDisabled()) {
			destroyCombatPath();
		}

		return combatPath.getLength() > 0 ? combatPath : null;
	}

	private boolean isEffectTargetDisabled() {
		return (overUIElement && !overCharacterPortrait) || !targetSelectionInProgress
				|| UIManager.isCharacterScreenOpen() || group.getGroupLeader() == null
				|| !group.getGroupLeader().isActive() || group.getGroupLeader().brain().blockingTurnActionInProgress();
	}

	private boolean isCombatPathDisabled() {
		return overUIElement || !GameState.isPlayersTurn() || UIManager.isAnythingOpen()
				|| group.getGroupLeader() == null || !group.getGroupLeader().isActive()
				|| group.getGroupLeader().brain().blockingTurnActionInProgress();
	}

	public TargetType getEffectTarget() {
		return !isEffectTargetDisabled() ? targetType : null;
	}

	/**
	 * Asks the player to select a valid target of the supplied type for the
	 * supplied targetable.
	 * 
	 * The callback methods are then called based on whether the selection was
	 * completed or cancelled.
	 * 
	 * @param user
	 * @param targetable
	 * @param targetType
	 * @param callback
	 */
	public void startTargetSelection(GameCharacter user, Targetable targetable, TargetType targetType,
			TargetSelectionCallback callback) {
		if (this.targetType != null) {
			this.targetType.clear();
		}

		targetType.setUser(user);
		if (targetType.requiresTargeting()) {
			UIManager.closeMutuallyExclusiveScreens();
			if (!gameState.getCurrentMap().isWorldMap()) {
				targetSelectionInProgress = true;
				this.targetType = targetType;
				this.targetable = targetable;
				this.targetSelectionCallback = callback;
				this.user = user;
				if (mip != null) {
					mip.mouseMoved(Gdx.input.getX(), Gdx.input.getY());
				} else {
					mouseMoved(Gdx.input.getX(), Gdx.input.getY());
				}
			} else {
				UIManager.displayTargetSelectionDialog(user, targetable, targetType, callback);
			}
		} else {
			callback.targetSelectionCompleted(targetable, targetType);
		}
	}

	public void stopTargetSelection(boolean targetSelected) {
		if (!targetSelectionInProgress) {
			return;
		}
		targetSelectionInProgress = false;
		if (targetSelected) {
			targetSelectionCallback.targetSelectionCompleted(targetable, targetType);
		} else {
			targetSelectionCallback.targetSelectionCancelled(targetable, targetType);
		}
		targetType.clear();
		this.targetType = null;
		this.targetable = null;
		this.user = null;
		this.targetSelectionCallback = null;
	}

	@Override
	public boolean needsInputTranslated(EventType eventType) {
		if (EventType.TouchUp.equals(eventType)) {
			return true;
		} else if (EventType.MouseMoved.equals(eventType)) {
			return true;
		}
		return false;
	}

	@Override
	public void setMainInputProcessor(MainInputProcessor mip) {
		this.mip = mip;
	}

	/**
	 * Resets the state of the PCGC to it's initial state.
	 */
	public void reset() {
		selectionInProgress = false;
		stopTargetSelection(false);
		destroyCombatPath();
		resetWaitingForClickConfirmation();
	}
}
