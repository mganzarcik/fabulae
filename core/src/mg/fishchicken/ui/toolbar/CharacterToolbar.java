package mg.fishchicken.ui.toolbar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.IntMap;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.tools.Tool;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.ui.TableStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.inventory.InventoryItemButton.InventoryItemButtonStyle;
import mg.fishchicken.ui.tooltips.SimpleTooltip;
import mg.fishchicken.ui.tooltips.SimpleTooltip.SimpleTooltipStyle;

public class CharacterToolbar extends Table implements EventListener {

	private SimpleTooltip tooltip;
	private CharacterToolbarStyle style;
	private GameState gameState;
	private Button attackButton, inventoryButton, characterSheetButton,
			activeEffectsButton, journalButton, formationEditorButton,
			campButton, usePerkButton, spellbookButton, stealthButton,
			lockpickButton, detectTrapsButton, disarmTrapsButton, talkToButton;
	private boolean ignoreEvent;
	private Table quickUseButtons;
	private PlayerCharacterGroup group;
	private QuickUseListener quickUseListener;
	private IntMap<InventoryItem> lastQuickUseItems;
	
	public CharacterToolbar(GameState gameState, PlayerCharacterGroup group, CharacterToolbarStyle style) {
		this.gameState = gameState;
		this.style = style;
		this.group = group;
		quickUseListener = new QuickUseListener(gameState, group.getGroupLeader(true));
		lastQuickUseItems = new IntMap<InventoryItem>();
		style.apply(this);
		loadTools();
		tooltip = new SimpleTooltip(style.tooltipStyle);
		
	}
	public void loadTools() {
		ignoreEvent = false;
		// screens
		inventoryButton = addButton(Strings.getString(UIManager.STRING_TABLE, "inventoryButton"), style.inventoryButtonStyle);
		characterSheetButton = addButton(Strings.getString(UIManager.STRING_TABLE, "characterSheetButton"), style.characterSheetButtonStyle);
		activeEffectsButton = addButton(Strings.getString(UIManager.STRING_TABLE, "activeEffectsButton"), style.activeEffectsButtonStyle);
		journalButton = addButton(Strings.getString(UIManager.STRING_TABLE, "journalButton"), style.journalButtonStyle);
		formationEditorButton = addButton(Strings.getString(UIManager.STRING_TABLE, "formationEditorButton"), style.formationEditorButtonStyle);
		// rest
		campButton = addButton(Strings.getString(UIManager.STRING_TABLE, "campButton"), style.campButtonStyle);
		// actions
		usePerkButton = addButton(Strings.getString(UIManager.STRING_TABLE, "usePerkButton"), style.usePerkButtonStyle);
		spellbookButton = addButton(Strings.getString(UIManager.STRING_TABLE, "spellbookButton"), style.spellbookButtonStyle);
		attackButton = addButton(Strings.getString(UIManager.STRING_TABLE, "attackButton"), style.attackButtonStyle);
		talkToButton = addButton(Strings.getString(UIManager.STRING_TABLE, "talkToButton"), style.talkToButtonStyle);
		stealthButton = addButton(Strings.getString(UIManager.STRING_TABLE, "stealthButton"), style.stealthButtonStyle);
		lockpickButton = addButton(Strings.getString(UIManager.STRING_TABLE, "lockpickButton"), style.lockpickButtonStyle);
		disarmTrapsButton = addButton(Strings.getString(UIManager.STRING_TABLE, "disarmButton"), style.disarmButtonStyle);
		detectTrapsButton = addButton(Strings.getString(UIManager.STRING_TABLE, "detectTrapsButton"), style.detectTrapsButtonStyle);
		
		quickUseButtons = new Table();
		if (style.inRows) {
			row();
		}
		add(quickUseButtons).space(style.quickUseGap);
		pack();
	}
	
	private void rebuildQuickUseButtons() {
		GameCharacter leader = group.getGroupLeader(true);
		if (leader != null) {
			quickUseListener.setUser(leader);
			Inventory inventory = leader.getInventory();
			IntMap<InventoryItem> quickUseItems = inventory.getBag(BagType.QUICKUSE);
			
			if (lastQuickUseItems.equals(quickUseItems)) {
				return;
			}
			
			lastQuickUseItems.clear();
			lastQuickUseItems.putAll(quickUseItems);
			
			quickUseButtons.clearChildren();
			
			for (IntMap.Entry<InventoryItem> item : quickUseItems) {
				QuickUseButton button = new QuickUseButton(
						style.quickSlotStyle, item.key, inventory,
						BagType.QUICKUSE);
				button.addListener(quickUseListener);
				quickUseButtons.add(button).fill().width(style.quickSlotWidth).height(style.quickSlotHeight).pad(style.toolGap/2);
				if (style.inRows) {
					quickUseButtons.row();
				}
			}
		} else {
			quickUseButtons.clearChildren();
		}
		
	}
	
	private TextButtonWithSound addButton(String text, TextButtonWithSoundStyle buttonStyle) {
		TextButtonWithSound button = new TextButtonWithSound(text, buttonStyle);
		button.addListener(this);
		this.add(button).pad(style.toolGap/2);
		if (style.inRows) {
			this.row();
		}
		return button;
	}
	
	private void updatePosition(int screenWidth, int screenHeight) {
		setY(screenHeight - getHeight() - style.y);
		setX(screenWidth - getWidth() - style.x);
	}
	
	@Override
	public void act(float delta) {
		GameMap map = gameState.getCurrentMap();
		if (map == null) {
			return;
		}
		boolean isLocalMap = !map.isWorldMap();
		
		attackButton.setVisible(isLocalMap);
		talkToButton.setVisible(isLocalMap);
		stealthButton.setVisible(isLocalMap);
		lockpickButton.setVisible(isLocalMap);
		detectTrapsButton.setVisible(isLocalMap);
		disarmTrapsButton.setVisible(isLocalMap);
		quickUseButtons.setVisible(isLocalMap);
		
		ignoreEvent = true;
		inventoryButton.setChecked(UIManager.isInventoryScreenOpen());
		characterSheetButton.setChecked(UIManager.isPerksScreenOpen());
		activeEffectsButton.setChecked(UIManager.isActiveEffectsScreenOpen());
		journalButton.setChecked(UIManager.isJournalOpen());
	    formationEditorButton.setChecked(UIManager.isFormationEditorOpen());
		campButton.setChecked(UIManager.isCampOpen());
		usePerkButton.setChecked(UIManager.isUsePerksScreenOpen());
		spellbookButton.setChecked(UIManager.isSpellbookScreenOpen());
		stealthButton.setChecked(GameState.isAnySelectedPCSneaking());
		detectTrapsButton.setChecked(group.isAnySelectedPCDetectingTraps());
		Tool activeTool = gameState.getPlayerCharacterController().getActiveTool();
		lockpickButton.setChecked(Tool.LOCKPICK == activeTool);
		disarmTrapsButton.setChecked(Tool.DISARM == activeTool);
		attackButton.setChecked(Tool.ATTACK == activeTool);
		talkToButton.setChecked(Tool.TALKTO == activeTool);
		rebuildQuickUseButtons();
		ignoreEvent = false;
		
		pack();
		updatePosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		super.act(delta);
	}

	@Override
	public boolean handle(Event event) {
		if (group.getGroupLeader() != null) {
			if ((event instanceof ChangeEvent) && !ignoreEvent) {
				return changed((ChangeEvent)event, event.getTarget());
			}
		}
		
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.enter.equals(inputEvent.getType())) {
				Actor target = inputEvent.getTarget();
				if (inventoryButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "inventoryTooltip"));
				} else if (characterSheetButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "charsheetTooltip"));
				} else if (usePerkButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "usePerkTooltip"));
				} else if (journalButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "displayJournalTooltip"));
				} else if (spellbookButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "displaySpellbookTooltip"));
				} else if (activeEffectsButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "activeEffectsTooltip"));
				} else if (campButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, gameState.getCurrentMap().isWorldMap() ? "breakCampTooltip" : "restTooltip"));
				} else if (stealthButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "stealthTooltip"));
				} else if (lockpickButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "lockpickTooltip"));
				} else if (disarmTrapsButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "disarmTooltip"));
				} else if (detectTrapsButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "detectTrapsTooltip"));
				} else if (formationEditorButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "formationEditorTooltip"));
				} else if (attackButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "attackTooltip"));
				} else if (talkToButton.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "talkToTooltip"));
				}
				UIManager.setToolTip(tooltip);
			} else if (Type.exit.equals(inputEvent.getType())) {
				UIManager.hideToolTip();
			}
		}
		
		return true;
	}
	
	public boolean changed (ChangeEvent event, Actor actor) {
		if (inventoryButton == actor) {
			UIManager.toggleInventory(group.getGroupLeader());
		} else if (characterSheetButton == actor) {
			UIManager.togglePerks(group.getGroupLeader());
		} else if (usePerkButton == actor) {
			UIManager.toggleUsePerks(group.getGroupLeader());
		} else if (journalButton == actor) {
			UIManager.toggleJournal();
		} else if (spellbookButton == actor) {
			UIManager.toggleSpellbook(group.getGroupLeader());
		} else if (activeEffectsButton == actor) {
			UIManager.toggleActiveEffects(group.getGroupLeader());
		} else if (campButton == actor && !UIManager.isCampOpen()) {
			group.sleepOrCamp();
		} else if (stealthButton == actor) {
			gameState.toggleStealth();
		} else if (lockpickButton == actor) {
			gameState.getPlayerCharacterController().toggleTool(Tool.LOCKPICK);
		} else if (detectTrapsButton == actor) {
			group.toggleDetectTraps();
		} else if (disarmTrapsButton == actor) {
			gameState.getPlayerCharacterController().toggleTool(Tool.DISARM);
		} else if (formationEditorButton == actor) {
			UIManager.toggleFormationEditor();
		} else if (attackButton == actor) {
			gameState.getPlayerCharacterController().toggleTool(Tool.ATTACK);
		}else if (talkToButton == actor) {
			gameState.getPlayerCharacterController().toggleTool(Tool.TALKTO);
		}
		return true;		
	}

	public static class CharacterToolbarStyle extends TableStyle {
		private float x, y, toolGap;
		private boolean inRows;
		private SimpleTooltipStyle tooltipStyle;
		private TextButtonWithSoundStyle attackButtonStyle,
				inventoryButtonStyle, characterSheetButtonStyle,
				activeEffectsButtonStyle, journalButtonStyle,
				formationEditorButtonStyle, campButtonStyle,
				usePerkButtonStyle, spellbookButtonStyle, stealthButtonStyle,
				lockpickButtonStyle, disarmButtonStyle, detectTrapsButtonStyle, talkToButtonStyle;
		private InventoryItemButtonStyle quickSlotStyle;
		private int quickSlotWidth, quickSlotHeight, quickUseGap;
	}
}
