package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.ui.inventory.InventoryItemButton.InventoryItemButtonStyle;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class InventoryComponent extends Table {
	
	private InventoryContainer ic;
	private EventListener listener;
	private String label;
	private InventoryComponentStyle style;
	private GameCharacter merchant;
	
	public InventoryComponent(InventoryContainer ic, InventoryComponentStyle style, EventListener listener) {
		this(ic ,null, style, listener, null);
	}
	
	public InventoryComponent(InventoryContainer ic, GameCharacter merchant,  InventoryComponentStyle style, EventListener listener, String label) {
		super();
		this.ic = ic;
		this.label = label;
		this.listener = listener;
		this.merchant = merchant;
		this.style =  style;
		buildComponent();
	}
	
	private void buildComponent() {
		Inventory inventory = ic.getInventory();
		Table inventoryTable = new Table();
		ScrollPane srollPane = new ScrollPane(inventoryTable, style.scrollPaneStyle);
		srollPane.setFadeScrollBars(false);
		srollPane.setOverscroll(false, false);
		int slotCounter = 0;
		
		int numberOfAllRows = (int) (50f*(10f / style.cols));
		
		for (int i = 0; i < numberOfAllRows; ++i) {
			for (int j = 0; j < style.cols; ++j, ++slotCounter) {
				InventoryItemButton button = new InventoryItemButton(
						style.inventorySlotStyle,
						slotCounter,
						inventory,
						CoreUtil.equals(inventory.getParentContainer(), merchant) ? BagType.MERCHANT : BagType.BACKPACK,
						merchant);
				button.addListener(listener);
				inventoryTable.add(button).width(style.inventorySlotWidth).height(style.inventorySlotHeight).space(style.inventorySlotSpacing);
			}
			inventoryTable.row();
		}
		
		if (label != null) {
			add(new Label(" "+label+":", style.headingStyle)).prefWidth(style.cols*(style.inventorySlotWidth+style.inventorySlotSpacing)).fill();
			add(new Label("", style.borderStyle)).prefWidth(style.borderWidth).fill();
		}
		row();
		
		add(srollPane).prefHeight(style.rows*(style.inventorySlotHeight+style.inventorySlotSpacing)).prefWidth(style.cols*(style.inventorySlotWidth+style.inventorySlotSpacing)+style.borderWidth).colspan(2);
		row();
	}
	
	static public class InventoryComponentStyle {
		public int inventorySlotWidth, inventorySlotHeight, inventorySlotSpacing, borderWidth, rows, cols;
		public LabelStyle borderStyle, headingStyle;
		public ScrollPaneStyle scrollPaneStyle;
		public InventoryItemButtonStyle inventorySlotStyle;
	}
}
