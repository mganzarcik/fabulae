package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.conditions.Condition.ConditionResult;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter.State;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.UsableItem;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the supplied GameCharacter use the supplied InventoryItem.
 * <br><br>
 * XML Parameters:
 * <ol>
 * 		<li> id - the id of the InventoryItem to use
 * 		<li> x - x-coordinate of the target
 * 		<li> y - y-coordinate of the target
 * </ol>
 * Code parameters:
 * <ol>
 * 		<li> item - mg.fishchicken.gamelogic.inventory.items.UsableItem - InventoryItem to use
 * 		<li> target - mg.fishchicken.gamelogic.effects.targets.TargetType - the target
 * </ol>
 * @author ANNUN
 *
 */
public class UseInventoryItemAction extends MoveToAction {
	protected String itemId;
	protected UsableItem usable;
	protected GameCharacter user;
	private boolean isFinished;
	private TargetType effectTarget;
	private boolean itemUseInProgress;
	private float targetX, targetY;
	private boolean moveToInitiated;
	
	public UseInventoryItemAction() {
	}

	public UseInventoryItemAction(GameCharacter user, UsableItem perk, TargetType target) {
		init(user, perk);
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("UseInventoryItemAction only works on GameCharacter!");
		}
		itemId = null;
		usable = null;
		effectTarget = null;
		itemUseInProgress = false;
		moveToInitiated = false;
		user = (GameCharacter) ac;
		isFinished = false;
		if (parameters.length >= 2) {
			usable = (UsableItem) parameters[0];
			itemId = usable.getId();
			effectTarget = (TargetType) parameters[1];
		}
		// if the item can be used instantly, just do it right now
		if (canBeUsedInstantly()) {
			update(0);
			isFinished = true;
		}
	}
	
	private boolean canBeUsedInstantly() {
		return (usable != null
				&& (user.getMap() == null ||  ((!effectTarget.requiresTargeting() || (effectTarget.requiresTargeting() && user.canSeeTile((int) effectTarget.getTargetX(),
						(int) effectTarget.getTargetY()))) && UIManager
						.isInventoryScreenOpen()))
				&& user.belongsToPlayerFaction());
	}
	
	@Override
	public void update(float deltaTime) {
		if (usable == null && itemId != null) {
			// TODO: this probably needs fixing for world maps?
			usable = (UsableItem) InventoryItem.getItem(itemId);
			effectTarget = usable.getTargetTypeInstance(user);
			effectTarget.setTarget((int)targetX, (int)targetY, user.getMap());
		}
		if (!isFinished) {
			if (!itemUseInProgress) {
				Array<ConditionResult> results = usable.canBeUsedBy(user);
				if (!Condition.areResultsOk(results)) {
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
							usable.getName(), failedConditions);
					isFinished = true;
					return;
				}
				if (user.getMap() == null || user.canSeeTile((int)effectTarget.getTargetX(), (int)effectTarget.getTargetY())) {
					useItem();
				} else {
					moveTo(deltaTime);
				}
			} else {
				if (user.isAnimationFinished()) {
					user.setState(State.IDLE);
					isFinished = true;
				}
			}
		}
	}
	
	private void moveTo(float deltaTime) {
		if (!moveToInitiated) {
			super.init(user, (int)effectTarget.getTargetX(), (int)effectTarget.getTargetY());
			moveToInitiated = true;
		} else {
			if (!super.isFinished()) {
				super.update(deltaTime);
			} else {
				isFinished = true;
			}
		}
	}
	
	@Override
	public boolean noMoreSteps() {
		if (user.canSeeTile((int)effectTarget.getTargetX(), (int)effectTarget.getTargetY())) {
			return true;
		} else {
			return super.noMoreSteps();
		}
	}
	
	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	private void useItem() {
		Log.logLocalized("usedItem", Log.LogType.INVENTORY, user.getName(), usable.getName());
		if (!canBeUsedInstantly()) {
			if (!user.position().floatEquals(effectTarget.getTargetX(), effectTarget.getTargetY())) {
				user.setOrientation(Orientation
					.calculateOrientationToTarget(user.getMap().isIsometric(),
							user.position().getX(), user.position().getY(),
							effectTarget.getTargetX(), effectTarget.getTargetY()));
			}
			user.setState(State.CAST);
		}
		
		boolean stacked = false;
		UsableItem usableToUse = usable;
		if (usable.getStackSize() > 1) {
			usableToUse = (UsableItem) usable.removeFromStack();
			stacked = true;
		} 
		
		if (usableToUse.use(user, effectTarget)) {
			if (!stacked) {
				// if the item was dragged when it was used, destroy it
				if (UIManager.getDraggedItem() != null) {
					UIManager.setDraggedItem(null, null);
				} else {
					// otherwise remove it from the owner's inventory
					if (usableToUse.getInventory() != null) {
						usableToUse.getInventory().removeFromBag(usableToUse.getInventoryBag(), usableToUse,
								false);
					}
				}
			}
		} else if (stacked) {
			usable.addToStack(usableToUse);
		}
		
		user.stats().addToAP(-Configuration.getAPCostUseItem());
		
		itemUseInProgress = true;
	}
	
	@Override
	public void onRemove(ActionsContainer ac) {
		super.onRemove(ac);
		isFinished = true;
	}
	
	@Override
	public void reset() {
		init(user, effectTarget);
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		itemId = actionElement.getAttribute(XMLUtil.XML_ATTRIBUTE_ID, null);
		if (itemId == null) {
			throw new GdxRuntimeException("id must be specified!");
		}
		try {
			targetX = actionElement.getFloatAttribute(XML_ATTRIBUTE_X);
		} catch (GdxRuntimeException e) {
			throw new GdxRuntimeException("x must be specified!");
		}
		try {
			targetY = actionElement.getFloatAttribute(XML_ATTRIBUTE_Y);
		} catch (GdxRuntimeException e) {
			throw new GdxRuntimeException("x must be specified!");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XMLUtil.XML_ATTRIBUTE_ID, itemId);
		if (effectTarget != null) {
			writer.attribute(XML_ATTRIBUTE_X, effectTarget.getTargetX());
			writer.attribute(XML_ATTRIBUTE_Y, effectTarget.getTargetY());
		}
	}
}
