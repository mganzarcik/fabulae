package mg.fishchicken.ui;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.TargetSelectionCallback;
import mg.fishchicken.core.input.Targetable;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.dialog.ConfirmationDialog.ConfirmationDialogStyle;
import mg.fishchicken.ui.selectbox.SelectOption;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound.SelectBoxWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class TargetSelectionDialog extends BorderedWindow {

	private ModalEnabledSelectBox<GameCharacter> targetSelect;
	private boolean selectVisible; 
	
	private Targetable targetable;
	
	public TargetSelectionDialog(TargetSelectionDialogStyle style) {
		super(Strings.getString(UIManager.STRING_TABLE,
				"targetSelectionHeading"), style);
		targetSelect = new ModalEnabledSelectBox<GameCharacter>(((TargetSelectionDialogStyle)getStyle()).targetSelectStyle);
	}
	
	public void init(final GameCharacter user, final Targetable targetable, final TargetType targetType, final TargetSelectionCallback callback) {
		this.targetable = targetable;
		this.selectVisible = targetType.getSize() < 2;
		
		setTitle(targetable.getName());
		
		TargetSelectionDialogStyle style = (TargetSelectionDialogStyle) getStyle();
		
		clearChildren();
		add(buildSelects(user, style)).fill().width(style.messageWidth);
		
		Table buttonsTable = new Table();
		
		TextButtonWithSound cancelButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "no"), style.cancelButtonStyle);
		buttonsTable.add(cancelButton).width(style.cancelButtonWidth)
				.height(style.cancelButtonHeight).fill()
				.padRight(style.cancelButtonMarginRight);
		
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.hideToolTip();
				callback.targetSelectionCancelled(targetable, targetType);
				UIManager.hideTargetSelectionDialog();
			}
		});
		
		TextButtonWithSound okButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "ok"), style.okButtonStyle);
		buttonsTable.add(okButton).width(style.okButtonWidth).height(style.okButtonHeight).fill();
		
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.hideToolTip();
				Array<GameCharacter> characters = GameState.getPlayerCharacterGroup().getMembers();
				if (!selectVisible) {
					for (int i = 0; i < characters.size; ++i) {
						GameCharacter character = characters.get(i);
						if (character.isActive()) {						
							targetType.addTarget(character);
						}
					}
				} else {
					targetType.addTarget(targetSelect.getSelected().value);
				}
				
				callback.targetSelectionCompleted(targetable, targetType);
				UIManager.hideTargetSelectionDialog();
			}
		});
		row();
		add(buttonsTable).center().padTop(style.buttonsMarginTop);
	}
	
	private Actor buildSelects(GameCharacter user, TargetSelectionDialogStyle style) {
		Table table = new Table();
		table.add(
				new Label(Strings.getString(UIManager.STRING_TABLE,
						selectVisible ? "chooseTarget" : "targetEveryone",
						user.getName(), targetable.getName()), style.messageStyle)).fill().padRight(10);
		if (selectVisible) {
			Array<SelectOption<GameCharacter>> options = buildPCItems();
			targetSelect.setItems(options);
			table.add(targetSelect).fill().expand();
		} 
		
		return table;
	}
	
	private Array<SelectOption<GameCharacter>> buildPCItems() {
		Array<SelectOption<GameCharacter>> items = new Array<SelectOption<GameCharacter>>();
		
		for (GameCharacter pc : GameState.getPlayerCharacterGroup().getPlayerCharacters()) {
			if (pc.isActive()) {
				items.add(new SelectOption<GameCharacter>(" "+pc.getName(), pc));
			}
		}
		
		return items;
	}
	
	public static class TargetSelectionDialogStyle extends ConfirmationDialogStyle {
		private SelectBoxWithSoundStyle targetSelectStyle;
	}
}
