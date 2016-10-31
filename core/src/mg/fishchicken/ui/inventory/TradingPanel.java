package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.inventory.InventoryComponent.InventoryComponentStyle;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;

public class TradingPanel extends BorderedWindow implements EventListener {

	private GameCharacter displayedMerchant;
	private Label totalCost;
	private DialogueCallback callback;
	private TradingPanelStyle style;
	
	public TradingPanel(TradingPanelStyle style) {
		super(style);
		this.style = style;
		callback = new DialogueCallback();
	}
	
	public void loadInventory(GameCharacter trader, TradingEventHandler eventHandler) {
		eventHandler.setTradingPanel(this);
		displayedMerchant = trader;
		setTitle(Strings.getString(UIManager.STRING_TABLE,  "tradingWith", trader.getName()));
		clearChildren();
		totalCost = new Label("0", style.totalCostLabelStyle);
		totalCost.setAlignment(Align.center);
		
		TextButtonWithSound sellAllButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "sellAll"), style.sellAllButtonStyle);
		sellAllButton.getLabelCell().fill();
		sellAllButton.addListener(this);
		
		add(new InventoryComponent(displayedMerchant, displayedMerchant, style.inventoryStyle, eventHandler, null)).fill().colspan(2);	
		row();
		add(sellAllButton).fill().expand().padRight(style.borderLeft).padTop(style.borderBottom);
	}
	
	public GameCharacter getDisplayedCustomer() {
		return UIManager.getDisplayedCharacter();
	}

	public GameCharacter getDisplayedMerchant() {
		return displayedMerchant;
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.touchDown.equals(inputEvent.getType())) {
				Inventory junkBag = GameState.getPlayerCharacterGroup().getInventory();
				if (junkBag.getTotalNumberOfItems() > 0) {
					int totalCost = junkBag.getTotalTradingCost(BagType.BACKPACK, getDisplayedCustomer(), displayedMerchant, false);
					UIManager.displayConfirmation(
							Strings.getString(UIManager.STRING_TABLE, "sellAllQuestion"),
							Strings.getString(UIManager.STRING_TABLE, "sellAllConfirmation", totalCost), 
							callback);
				}
				return true;
			}
		}
		return false;
	}
	
	private class DialogueCallback extends OkCancelCallback<Void> {
		@Override
		public void onOk(Void nada) {
			Inventory junkBag = GameState.getPlayerCharacterGroup().getInventory();
			int totalCost = junkBag.getTotalTradingCost(BagType.BACKPACK, getDisplayedCustomer(), displayedMerchant, false);
			junkBag.moveAllItems(BagType.BACKPACK, BagType.BACKPACK, displayedMerchant.getInventory());
			GameState.getPlayerCharacterGroup().addGold(totalCost);
			
		}
	}
	
	static public class TradingPanelStyle extends BorderedWindowStyle {
		public InventoryComponentStyle inventoryStyle;
		public LabelStyle totalCostLabelStyle;
		public TextButtonWithSoundStyle sellAllButtonStyle;
	}
}