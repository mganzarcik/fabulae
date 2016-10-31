package mg.fishchicken.ui.toolbar;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.conditions.Condition.ConditionResult;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.input.TargetSelectionCallback;
import mg.fishchicken.core.input.Targetable;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.actions.UseInventoryItemAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.UsableItem;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.inventory.InventoryItemButton;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class QuickUseListener extends ChangeListener implements TargetSelectionCallback {

	private GameState gameState;
	private GameCharacter user;
	
	public QuickUseListener(GameState gameState, GameCharacter user) {
		this.gameState = gameState;
		this.user = user;
	}
	
	public void setUser(GameCharacter user) {
		this.user = user;
	}
	
	@Override
	public void changed(ChangeEvent event, Actor actor) {
		InventoryItem item = null;
		if (actor instanceof InventoryItemButton) {
			item = ((InventoryItemButton) actor).getItem();
		}
		if (item == null) {
			item = UIManager.getDraggedItem();
		}
		if (item != null) {
			useItem(item, user);
		}	
	}
	
	private void useItem(InventoryItem item, GameCharacter user) {
		Array<ConditionResult> results = item.canBeUsedBy(user);
		if (Condition.areResultsOk(results)) {
			calculateTargetAndUseItem((UsableItem)item);
		} else {
			StringBuilder fsb = StringUtil.getFSB();
			for (ConditionResult result : results) {
				if (!result.passed) {
					fsb.append(result.conditionName);
					fsb.append(", ");
				}
			}
			String failedConditions = fsb.substring(0, fsb.lastIndexOf(", "));
			StringUtil.freeFSB(fsb);
			Log.logLocalized("cannotUseItem", LogType.INVENTORY, user.getName(),
					item.getName(), failedConditions);
		}
	}
	
	private void calculateTargetAndUseItem(UsableItem usable) {
		if (usable.isCombatOnly() && !GameState.isCombatInProgress()) {
			gameState.startCombat();
		}
		
		TargetType target = usable.getTargetTypeInstance(user);
		target.setApCost(Configuration.getAPCostUseItem());
		gameState.getPlayerCharacterController().startTargetSelection(
				user, usable, target, this);
	}
	
	@Override
	public void targetSelectionCompleted(Targetable targetable,
			TargetType effectTarget) {
		user.addAction(UseInventoryItemAction.class, targetable, effectTarget);
	}

	@Override
	public void targetSelectionCancelled(Targetable targetable,
			TargetType effectTarget) {
	}

}
