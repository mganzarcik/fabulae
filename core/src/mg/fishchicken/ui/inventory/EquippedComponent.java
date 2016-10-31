package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.ui.TableStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.ImageButtonWithSound;
import mg.fishchicken.ui.button.ImageButtonWithSound.ImageButtonWithSoundStyle;
import mg.fishchicken.ui.inventory.EquipButton.EquipButtonStyle;
import mg.fishchicken.ui.toolbar.QuickUseListener;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class EquippedComponent extends Table  {

	private GameCharacter displayedCharacter;
	private ImageButtonWithSound useButton;
	private EquippedComponentStyle style;
	
	public EquippedComponent(GameState gameState, GameCharacter character, GameCharacter merchant, EquippedComponentStyle style, InventoryEventHandler listener) {
		super();
		this.style = style;
		style.apply(this);
		
		displayedCharacter = character;
		
		Inventory inventory = character.getInventory();
		Table equippedArea = new Table();
		if (style.equippedStyle != null) {
			style.equippedStyle.apply(equippedArea);
		}
		Table labels = new Table();
		Table slots = new Table();
		if (style.equippedLabelsStyle != null) {
			style.equippedLabelsStyle.apply(labels);
		}
		for (ItemSlot slot : ItemSlot.values()) {
			InventoryItemButton button = new EquipButton(style.equipSlotStyle, slot.getSlot(), inventory, merchant);
			button.addListener(listener);
			slots.add(button).prefWidth(style.equippedSlotWidth).prefHeight(style.equippedSlotHeight).space(style.equippedSlotSpacing).spaceTop(0);
			labels.add(new Label(slot.getUIString()+": ", style.subheadingStyle)).fill().expand().prefHeight(style.equippedSlotHeight+style.equippedSlotSpacing/2f);
			labels.row();
			slots.row();
		}
		
		labels.add(new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"UseItem"), style.subheadingStyle)).fill().expand().prefHeight(style.equippedSlotHeight+style.equippedSlotSpacing/2f);
		useButton = new ImageButtonWithSound(style.canUseStyle);
		useButton.addListener(new QuickUseListener(gameState, character));
		slots.add(useButton).prefWidth(style.equippedSlotWidth).prefHeight(style.equippedSlotHeight).space(style.equippedSlotSpacing).spaceTop(0);
		
		equippedArea.add(labels).expand().fill().padRight(style.borderWidth);
		equippedArea.add(slots);
		equippedArea.row();
		
		Table quickUseSlots = new Table();
		style.quickUseStyle.apply(quickUseSlots);
		for (int i = 0; i < style.quickUseSlots; ++i) {
			Button button =  new EquipButton(style.equipSlotStyle, i, inventory, BagType.QUICKUSE, merchant);
			button.addListener(listener);
			quickUseSlots.add(button).prefWidth(style.equippedSlotWidth).prefHeight(style.equippedSlotHeight).space(style.equippedSlotSpacing).spaceLeft(0);
		}
		
		add(new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"Equipped")+":", style.headingStyle)).fillX().top().expandX();
		row();
		add(equippedArea).fill().top().padRight(style.borderWidth);
		row();
		add(new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"QuickSlots")+":", style.headingStyle)).fill().top().padRight(style.borderWidth);
		row();
		add(quickUseSlots).align(Align.left).fill().top().padRight(style.borderWidth);
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		InventoryItem draggedItem = UIManager.getDraggedItem();
		
		if (draggedItem == null || Condition.areResultsOk(draggedItem.canBeUsedBy(displayedCharacter))) {
			useButton.setStyle(style.canUseStyle);
		} else {
			useButton.setStyle(style.cannotUseStyle);
		}
	}
	
	static public class EquippedComponentStyle extends TableStyle {
		public TableStyle equippedStyle, equippedLabelsStyle, quickUseStyle;
		public int borderWidth, equippedSlotWidth, equippedSlotHeight, equippedSlotSpacing, quickUseSlots;
		public LabelStyle headingStyle, subheadingStyle;
		public ImageButtonWithSoundStyle canUseStyle, cannotUseStyle;
		public EquipButtonStyle equipSlotStyle;
	}
}
