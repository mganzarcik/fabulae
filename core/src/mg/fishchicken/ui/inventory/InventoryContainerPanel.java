package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.inventory.InventoryComponent.InventoryComponentStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class InventoryContainerPanel extends BorderedWindow {

	private InventoryContainerPanelStyle style;
	
	public InventoryContainerPanel(InventoryContainerPanelStyle style) {
		super(style);
		this.style = style;
	}
	
	public void loadInventory(final InventoryContainer container, final InventoryEventHandler eventHandler) {
		clearChildren();
		setTitle(container.getName());
		add(new InventoryComponent(container, style.inventoryStyle, eventHandler)).fill();
		row();
		TextButtonWithSound takeAllButton = new TextButtonWithSound(Strings.getString(Inventory.STRING_TABLE, "takeAll"), style.takeAllButtonStyle);
		takeAllButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Array<InventoryItem> allItems = container.getInventory().getAllItems();
				PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
				Inventory groupBag = pcg.getInventory();
				for (InventoryItem item : allItems) {
					int amount = item.canBeAddedTo(pcg);
					for (int i = 0; i < amount; ++i) {
						groupBag.addItem(item.removeFromStack());
					}
				}
				UIManager.closeMutuallyExclusiveScreens();
			}
			
		});
		add(takeAllButton).fill().padRight(style.inventoryStyle.scrollPaneStyle.hScroll.getMinWidth()).padTop(style.takeAllButtonMarginTop);
	}
	
	static public class InventoryContainerPanelStyle extends BorderedWindowStyle {
		private InventoryComponentStyle inventoryStyle;
		private int takeAllButtonMarginTop;
		private TextButtonWithSoundStyle takeAllButtonStyle;
	}
	
}
