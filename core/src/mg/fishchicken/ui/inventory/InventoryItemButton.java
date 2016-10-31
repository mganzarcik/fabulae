package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.ImageButtonWithSound.ImageButtonWithSoundStyle;
import mg.fishchicken.ui.tooltips.ItemTooltip;
import mg.fishchicken.ui.tooltips.ItemTooltip.ItemTooltipStyle;
import mg.fishchicken.ui.tooltips.TradingItemTooltip;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class InventoryItemButton extends ImageButton implements EventListener {
	private InventoryItem item;
	protected Image itemIcon;
	private int slot;
	private Inventory inventory;
	private float fontHeight;
	private BagType bagType;
	private ItemTooltip tooltip;
	private InventoryItemButtonStyle style;
	private GameCharacter merchant;
	
	public InventoryItemButton(InventoryItemButtonStyle style,  int slot, Inventory inventory, BagType bagType, GameCharacter merchant) {
		super(style.emptyStyle);
		this.item = null;
		this.style = style;
		this.slot = slot;
		this.inventory = inventory;
		this.bagType = bagType;
		this.merchant = merchant;
		attachSoundListener();
		fontHeight = UIManager.getStackFont().getCapHeight();
		addListener(this);
	}
	
	private void attachSoundListener() {
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				ButtonStyle style = getStyle();
				if (style instanceof ImageButtonWithSoundStyle) {
					ImageButtonWithSoundStyle castStyle = (ImageButtonWithSoundStyle) style;
					if (castStyle.clickSound != null && (getItem() != null || UIManager.getDraggedItem() != null)) {
						castStyle.clickSound.play(Configuration.getUIEffectsVolume());
					}
				}
				return false;
			}
		});
	}

	private void setItem(InventoryItem item) {
		this.item = item;
		
		if (item != null) {
			if (itemIcon == null) {
				itemIcon = new Image();
				this.add(itemIcon).fill();
			}
			itemIcon.setDrawable(new TextureRegionDrawable(Assets.getTextureRegion(item.getInventoryIconFile())));
			setStyle(style.occupiedStyle);
		} else {
			setStyle(style.emptyStyle);
			if (itemIcon != null) {
				itemIcon.setDrawable(null);
			}
		}
		if (item != null) {
			createOrUpdateTooltip(item);
		}
	}
	
	protected void createOrUpdateTooltip(InventoryItem item) {
		if (tooltip == null) {
			if (merchant == null) {
				tooltip = new ItemTooltip(item, style.tooltipStyle);
			} else {
				tooltip = new TradingItemTooltip(item, style.tooltipStyle,  merchant);
			}
		} else {
			tooltip.setItem(item);
		}
	}
	
	protected ItemTooltip getTooltip() {
		return tooltip;
	}
	
	public InventoryItem getItem() {
		return item;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public InventoryContainer getContainer() {
		return inventory.getParentContainer();
	}

	public Inventory getInventory() {
		return inventory;
	}
	
	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (item != null && (item.isInfinite() || item.getStackSize() > 1)) {
			UIManager.getStackFont().draw(batch, item.isInfinite() ? "oo" : Integer.toString(item.getStackSize()), getX()+2, getY()+fontHeight+2);
		}
	}

	public BagType getBagType() {
		return bagType;
	}

	public void setBagType(BagType bagType) {
		this.bagType = bagType;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if (inventory == null) {
			return;
		}
		
		InventoryItem newItem = inventory.getBag(getBagType()).get(getSlot());
		if (!CoreUtil.equals(newItem, item)) {
			setItem(newItem);
			if (UIManager.isThisTooltipSet(getTooltip())) {
				UIManager.hideToolTip();
			}
		}
		
		if (merchant != null) {
			// for merchant items, show whether they can be bought or not
			if (getItem() != null) {
				if (merchant.equals(getItem().getInventory().getParentContainer())) {
					int playerGold = GameState.getPlayerCharacterGroup().getGold();
					if (getItem().getTradingCost(UIManager.getDisplayedCharacter(), merchant, true) > playerGold) {
						setStyle(style.cannotBuyStyle);
						return;
					}
				}
			} 
			
			InventoryItem draggedItem = UIManager.getDraggedItem();
			if (draggedItem != null
					&& getContainer().equals(merchant)
					&& !UIManager.isDraggedFromMerchant()
					&& merchant.isOwnerOf(draggedItem) && !draggedItem.getOwner().isEmpty()) {
				setStyle(style.cannotSellStyle);
				return;
			}
		}
		
		if (getItem() == null) {
			setStyle(style.emptyStyle);
		} else {
			setStyle(style.occupiedStyle);
		}
	}
	
	public boolean handle(Event event) {
		if (item == null) {
			return false;
		}
		if (item.equals(UIManager.getDraggedItem())) {
			UIManager.hideToolTip();
			return false;
		}
		if (event instanceof InputEvent) {
			Type inputEventType = ((InputEvent) event).getType();
			if (Type.enter.equals(inputEventType)) {
				getTooltip().updateText(UIManager.getDisplayedCharacter());
				if (getTooltip().shouldDisplay()) {
					UIManager.setToolTip(getTooltip());
				}
			}
			if (Type.exit.equals(inputEventType)) {
				UIManager.hideToolTip();
			}
		}
		return false;
	}
	
	public static class InventoryItemButtonStyle {
		protected ItemTooltipStyle tooltipStyle;
		protected ImageButtonWithSoundStyle emptyStyle, occupiedStyle, cannotBuyStyle, cannotSellStyle;
	}
}
