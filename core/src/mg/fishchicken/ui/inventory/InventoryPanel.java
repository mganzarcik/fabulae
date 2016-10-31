package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.inventory.EquippedComponent.EquippedComponentStyle;
import mg.fishchicken.ui.inventory.InventoryComponent.InventoryComponentStyle;
import mg.fishchicken.ui.inventory.StatsComponent.StatsComponentStyle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class InventoryPanel extends BorderedWindow {

	private GameCharacter displayedCharacter;
	private Label totalLoad;
	private Label totalGold;
	private Label food;
	private Label water;
	private InventoryEventHandler eventHandler;
	private float oldLoad;
	private float oldMaxLoad;
	private InventoryPanelStyle style;
	private GameState gameState;
	
	public InventoryPanel(GameState gameState, InventoryPanelStyle style) {
		super(style);
		this.style = style;
		this.gameState = gameState;
		totalLoad = new Label("", style.totalLoadLabelStyle);
		totalGold = new Label("", style.totalGoldLabelStyle);
		food = new Label("", style.foodLabelStyle);
		water = new Label("", style.waterLabelStyle);
	}
	
	public void loadInventory(GameCharacter character, InventoryEventHandler eventHandler, GameCharacter merchant) {
		this.displayedCharacter = character;
		this.eventHandler = eventHandler;
		setTitle(character.getName());
		clearChildren();
		recomputeTotalLoad();
		
		add(food).fill();
		add(water).fill();
		
		Table groupInfo = new Table();
		groupInfo.add(totalGold).fill().expandX();
		groupInfo.add(totalLoad).fill().expandX();
		add(groupInfo).fill().left();
		
		row();
		
		Table inventoryComponent = buildInventoryPane(merchant);
		
		StatsComponent stats = new StatsComponent(displayedCharacter, style.statsStyle, eventHandler, (int)inventoryComponent.getPrefHeight());
		add(stats).top();
		add(new EquippedComponent(gameState, displayedCharacter, merchant, style.equippedStyle, eventHandler)).top();
		add(inventoryComponent).top();
		pack();
	}
	
	private Table buildInventoryPane(GameCharacter merchant) {
		Table rightPane = new Table();
	
		rightPane.add(new InventoryComponent(GameState.getPlayerCharacterGroup(), merchant, style.junkBagStyle, eventHandler, Strings.getString(AbstractGameCharacter.STRING_TABLE,"JunkBag")));
		rightPane.row();
		rightPane.add(new InventoryComponent(displayedCharacter, merchant, style.inventoryStyle, eventHandler, Strings.getString(AbstractGameCharacter.STRING_TABLE,"Inventory")));
		
		return rightPane;
	}
	
	private void recomputeTotalLoad() {
		if (oldLoad != displayedCharacter.stats().getLoad() || oldMaxLoad !=  displayedCharacter.stats().getMaximumLoad() ) {
			oldLoad = displayedCharacter.stats().getLoad();
			oldMaxLoad =  displayedCharacter.stats().getMaximumLoad();
			totalLoad.setText(" "+Strings.getString(AbstractGameCharacter.STRING_TABLE,"load")+": "+MathUtil.toUIString(displayedCharacter.stats().getLoad()) + " / " + MathUtil.toUIString(displayedCharacter.stats().getMaximumLoad()) + " kg");
			float dif = displayedCharacter.stats().getLoad() - displayedCharacter.stats().getMaximumLoad();
			if (dif > 0) {
				totalLoad.setColor(style.encumberedColor);
				if (dif > 100) {
					totalLoad.setColor(style.cannotMoveColor);
				}
			} else {
				totalLoad.setColor(style.notEncuberedColor);
			}
		}
		PlayerCharacterGroup group = GameState.getPlayerCharacterGroup();
		totalGold.setText(" "+Strings.getString(AbstractGameCharacter.STRING_TABLE,"gold")+": "+group.getGold());
		if (Configuration.isSurvivalEnabled()) {
			food.setText(" "+Strings.getString(AbstractGameCharacter.STRING_TABLE,"food")+": "+MathUtil.toUIString(group.getFood()) +" / "+MathUtil.toUIString(group.getMaxFood()));
			water.setText(" "+Strings.getString(AbstractGameCharacter.STRING_TABLE,"water")+": "+MathUtil.toUIString(group.getWater()) +" / "+MathUtil.toUIString(group.getMaxWater()));
		}
	}
	
	public InventoryContainer getParentContainer() {
		return displayedCharacter;
	}
	
	/**
	 * Overridden to display
	 * the item tool-tip if we need to and to recompute
	 * the total load.
	 * 
	 */
	@Override
	public void act(float delta) {
		super.act(delta);
		if (isVisible()) {
			recomputeTotalLoad();
		}
	}
	
	public static class InventoryPanelStyle extends BorderedWindowStyle {
		private InventoryComponentStyle junkBagStyle, inventoryStyle;
		private StatsComponentStyle statsStyle;
		private EquippedComponentStyle equippedStyle;
		private Color notEncuberedColor, encumberedColor, cannotMoveColor;
		private LabelStyle totalLoadLabelStyle, totalGoldLabelStyle, foodLabelStyle, waterLabelStyle; 
	}
	
}
