package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * ChainedAction is a collection of Actions that are executed in sequence. This
 * action is considered to be finished when all the actions it contains have
 * been executed <br />
 * <br />
 * Parameters:
 * <ul>
 * <li>target - String - If supplied, the actions will be executed
 * on a GameObject with this ID instead of the owner of the ChainAction itself.
 * This allows you to basically hand over "control" from one GO to another,
 * making the first GO wait until actions are executed on the second one and
 * then getting the control back to the first GO.
 * </ul>
 * Child actions should be added using the supplied addAction methods, or, when
 * loaded from XML, defined as child elements of the ChainAction element.
 * 
 * @author Annun
 *
 */
public class ChainAction extends BasicAction {

	private BasicActionsContainer actions;
	private Action currentAction;
	private boolean isFinished;
	private String targetId;
	private Element actionElement;
	
	/**
	 * Empty contructor for loading from XML.
	 */
	public ChainAction() {
		targetId = null;
		actionElement = null;
	}
	
	public ChainAction(ActionsContainer ac) {
		this();
		actions = new BasicActionsContainer(ac);
	}
	
	public ChainAction(ActionsContainer ac, Action... actions) {
		this(ac);
		for (Action action : actions) {
			addAction(action);
		}
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		targetId = null;
		if (parameters.length > 0) {
			targetId = (String) parameters[0];
		}
		if (targetId != null) {
			actions = new BasicActionsContainer(GameState.getGameObjectById(targetId));
		} else {
			actions = new BasicActionsContainer(ac);
		}
		currentAction = null;
		isFinished = false;
	}

	public void addAction(Action a) {
		actions.addAction(a);
	}
	
	public int size() {
		return actions.size();
	}
		
	@Override
	public void update(float deltaTime) {
		// initialize ourselves if it was not done during loading from XML
		if (actions == null && actionElement != null && targetId != null) {
			actions = new BasicActionsContainer(GameState.getGameObjectById(targetId));
			XMLUtil.readActions(actions, actionElement);
			actionElement = null;
		}
		if (currentAction == null) {
			if (actions.size() < 1) {
				isFinished = true;
				return;
			}
			currentAction = actions.removeFirst();
		}
		
		if (currentAction != null) {
			if (currentAction.isFinished()) {
				currentAction = null;
			}
			else {
				currentAction.update(deltaTime);
			}
		}
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	@Override
	public boolean isBlockingInCombat() {
		if (currentAction != null) {
			return currentAction.isBlockingInCombat();
		}
		return false;
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		targetId = actionElement.getAttribute(XML_ATTRIBUTE_TARGET, null);
		if (targetId != null) {
			GameObject target = GameState.getGameObjectById(targetId);
			if (target == null) {
				actions = null;
				this.actionElement = actionElement; 
				return;
			}
			actions = new BasicActionsContainer(target);
		} 
		XMLUtil.readActions(actions, actionElement);
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		if (targetId != null) {
			writer.attribute(XML_ATTRIBUTE_TARGET, targetId);
		}
		if (currentAction != null) {
			currentAction.writeToXML(writer);
		}
		if (actions.size() > 0) {
			for (Action action : actions) {
				action.writeToXML(writer);
			}
		}
	}

	@Override
	public void reset() {
		for (Action a : actions) {
			a.reset();
		}
	}
	
	@Override
	public boolean isVerbAction() {
		boolean returnValue = true;
		if (currentAction != null) {
			returnValue = returnValue && currentAction.isVerbAction();
		}
		for (Action a : actions) {
			returnValue = returnValue && a.isVerbAction();
		}
		
		return returnValue;
	}
	
	@Override
	public void onRemove(ActionsContainer ac) {
		if (currentAction != null) {
			currentAction.onRemove(ac);
		}
	}

}
