package mg.fishchicken.gamelogic.characters;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import groovy.lang.Script;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;

/**
 * Brain is responsible for managing the AIScript of any GameObject it is attached to.
 * 
 * It will determine what actions the GO needs to take and assign them as required.
 * 
 * It is able to differentiate between combat and non combat modes.
 * 
 * @author Annun
 *
 */
public class Brain implements XMLSaveable {
	public static final String XML_CURRENT_AI_ACTION = "currentAIAction";
	public static final String XML_AI_BACKUP = "aiBackup";
	public static final String XML_BRAIN = "brain";
	
	private boolean s_disabled;
	
	private AIScriptPackage aiScript;
	private AIScriptPackage aiScriptBackUp;
	
	private Action currentTurnAction;
	private Action currentAIAction;
	
	private GameObject go;
	
	public Brain(GameObject go) {
		this.go = go;
	}
	
	/**
	 * Updates the brain, determining the next action that should 
	 * be undertaken.
	 */
	public void update(float deltaTime) {
		if (!s_disabled && aiScript != null && currentAIAction == null && !GameState.isCombatInProgress()) {
			currentAIAction  = aiScript.run(go);
			if (currentAIAction != null) {
				go.addAction(currentAIAction, false);
			}
		}
	}
	
	/**
	 * Updates the currently active turn action and determines
	 * the next one if this one finished.
	 * @param deltaTime
	 */
	public void updateCombatAction(float deltaTime) {
		if (currentTurnAction != null) {
			currentTurnAction.update(deltaTime);
			if (currentTurnAction.isFinished()) {
				currentTurnAction.onRemove(go);
				currentTurnAction = null;
			}
		} else {
			setNextTurnAction();
		}
	}
	
	/**
	 * Disables the Brain, immediately
	 * stopping any AI action currently in progress.
	 */
	public void disable() {
		s_disabled = true;
		if (currentAIAction != null) {
			go.removeAction(currentAIAction);
			
		}
	}
	
	/**
	 * Enabled the Brain.
	 * 
	 * @see #disable()
	 */
	public void enable() {
		s_disabled = false;
	}

	/**
	 * Returns true if this character currently has a combat action in progress
	 * that should block any other action from being assigned to him.
	 * 
	 * @return
	 */
	public boolean blockingTurnActionInProgress() {
		return (currentTurnAction != null && !currentTurnAction.isBlockingInCombat()) || go.hasAnyBlockingAction();
	}
	
	private boolean hasNextTurnAction() {
		if (aiScript == null) {
			return false;
		}
		Action action = aiScript.run(go);
		return action != null && !action.isFinished() && go.canPerformAction(action.getClass());
	}
	
	private void setNextTurnAction() {
		if (aiScript == null) {
			currentTurnAction = null;
			return;
		}
		currentTurnAction= aiScript.run(go);
		if (currentTurnAction != null && !go.canPerformAction(currentTurnAction.getClass())) {
			currentTurnAction = null;
		}
	}
	
	public boolean finishedTurn() {
		if (currentTurnAction == null && !hasNextTurnAction()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Remove all actions managed exclusively by the Brain
	 */
	protected void removeActions() {
		if (currentTurnAction != null) {
			currentTurnAction.onRemove(go);
			currentTurnAction = null;
		}
	}
	
	protected void removeAction(Action a) {
		if (CoreUtil.equals(a, currentAIAction)) {
			currentAIAction = null;
		}
	}
	
	/**
	 * Gets the current AIScript of this brain.
	 * 
	 */
	public AIScriptPackage getAIScript() {
		return aiScript;
	}
	
	/**
	 * This replaces the brain's defined AIScript with a new one.
	 * This action is irreversible and the old script will not be restorable.
	 * If you want to temporarily replace an AI script, call setOverrideAIScript()
	 * {@link #setOverrideAIScript()}.
	 * 
	 * @param newAIScript
	 */
	public void setAIScript(AIScriptPackage newAIScript) {
		aiScript = newAIScript;
		aiScriptBackUp = newAIScript;
	}
	
	/**
	 * This replaces the brains's defined AIScript with a new one.
	 * If you wish to restore the original AI script the brain was
	 * loaded with, just call restore.
	 * {@link #restore()}.
	 * 
	 * @param newAIScript
	 */
	public void override(AIScriptPackage newAIScript) {
		aiScript = newAIScript;
	}
	
	/**
	 * Restores the original, XML master data defined AI Script 
	 * of this brain in case it was replaced by calling 
	 * {@link #override(Script)}.
	 */
	public void restore() {
		aiScript = aiScriptBackUp;
	}
	
	
	public void loadFromXML(Element root) throws IOException {
		if (root.getChildByName(AIScriptPackage.XML_AI) != null) {
			aiScript = new AIScriptPackage(root);
		}
		Element brainElement = root.getChildByName(XML_BRAIN);
		if (brainElement != null) {
			readBrainFromXML(brainElement);
		}
	}
	
	protected void readBrainFromXML(Element brainElement) throws IOException {
		XMLUtil.readPrimitiveMembers(this, brainElement);
		Element currentAIActionElement = brainElement.getChildByName(XML_CURRENT_AI_ACTION);
		if (currentAIActionElement != null && currentAIActionElement.getChildCount() > 0) {
			currentAIAction = Action.readFromXML(currentAIActionElement.getChild(0), go);
			go.addAction(currentAIAction, false);
		}
		
		Element aiBackup = brainElement.getChildByName(XML_AI_BACKUP);
		if (aiBackup != null) {
			aiScriptBackUp = new AIScriptPackage(aiBackup);
		} else {
			aiScriptBackUp = aiScript;
		}
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		if (aiScript != null) {
			aiScript.writeToXML(writer);
		}
		
		writer.element(XML_BRAIN);
		writeBrainToXML(writer);
		writer.pop();
	}
	
	protected void writeBrainToXML(XmlWriter writer) throws IOException {
		XMLUtil.writePrimitives(this, writer, true);
		if (currentAIAction != null) {
			writer.element(XML_CURRENT_AI_ACTION);
			currentAIAction.writeToXML(writer);
			writer.pop();
		}	
		
		if (aiScriptBackUp != null) {
			writer.element(XML_AI_BACKUP);
			aiScriptBackUp.writeToXML(writer);
			writer.pop();
		}
	}
}
