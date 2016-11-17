package mg.fishchicken.gamelogic.characters;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import groovy.lang.Script;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.ChainAction;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.actions.WanderAction;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.Tile;

/**
 * Brain is responsible for managing the AIScript of any GameCharacter it is attached to.
 * 
 * It will determine what actions the GameCharacter needs to take and assign them as required.
 * 
 * It is able to differentiate between combat and non combat modes.
 * 
 * @author Annun
 *
 */
public class GameCharacterBrain extends Brain {

	public static final String XML_COMBAT_END_ACTION = "combatEndAction";
	
	private GameCharacter character;
	private Action combatEndAction;
	
	private boolean s_shouldReturnAfterCombat, s_shouldSearchAfterCombat;
	private float s_combatStartX, s_combatStartY;

	
	public GameCharacterBrain(GameCharacter character) {
		super(character);
		this.character = character;
	}
	
	@Override
	public void update(float deltaTime) {
		if (!character.isAsleep() && !character.belongsToPlayerFaction() && combatEndAction == null) {
			super.update(deltaTime);
		}
		
		if (combatEndAction != null) {
			combatEndAction.update(deltaTime);
			// the null check is important here, since the update to the search action
			// might have initiated combat, which would zero out the search action
			if (combatEndAction!= null && combatEndAction.isFinished()) {
				combatEndAction = null;
				character.clearLastKnownEnemyPosition();
			}
		}
	}
	
	@Override
	public void updateCombatAction(float deltaTime) {
		if (!character.isAsleep()) {
			super.updateCombatAction(deltaTime);
		}
	}
	
	@Override
	public boolean finishedTurn() {
		if (character.isAsleep() || character.stats().getAPAct() <= 0) {
			removeActions();
			return true;
		}
		return super.finishedTurn();
	}
	
	/**
	 * This replaces the character's defined AIScript with a new one.
	 * If you wish to restore the original AI script the character was
	 * loaded with, just call restoreAIScript.
	 * {@link #restore()}.
	 * 
	 * If the character is currently controlled by the player,
	 * this will also take the control away.
	 * 
	 * @param newAIScript
	 */
	public void override(AIScriptPackage newAIScript) {
		super.override(newAIScript);
		GameState.getPlayerCharacterGroup().temporarilyRemoveMember(character);
	}
	
	/**
	 * Restores the original, XML master data defined AI Script 
	 * of this character in case it was replaced by calling 
	 * {@link #override(Script)}.
	 * 
	 * If the character was controlled by the player at the time of the override,
	 * this will restore the control.
	 * 
	 */
	public void restore() {
		super.restore();
		GameState.getPlayerCharacterGroup().restoreTemporarilyRemovedMember(character);
	}
	
	protected void onCombatStart() {
		removeActions();
		
		Position position = character.position();
		s_combatStartX = position.getX();
		s_combatStartY = position.getY();
	}
	
	protected void onCombatEnd() {
		combatEndAction = null;
		Tile enemyPos = character.getLastKnownEnemyPosition();
		if (s_shouldReturnAfterCombat || (enemyPos != null && s_shouldSearchAfterCombat)) {
			ChainAction chain = new ChainAction(character);
			if (enemyPos != null && s_shouldSearchAfterCombat) {
				chain.addAction(new WanderAction(character, enemyPos, 5, 60, 30));
			}
			if (s_shouldReturnAfterCombat) {
				chain.addAction(new MoveToAction(character, (int)s_combatStartX, (int)s_combatStartY));
			}
			if (chain.size() > 0) {
				combatEndAction = chain;
			}
		} 
		
	}
	
	@Override
	protected void removeActions() {
		super.removeActions();
		if (combatEndAction != null) {
			combatEndAction.onRemove(character);
			combatEndAction = null;
		}
	}
	
	@Override
	protected void removeAction(Action a) {
		super.removeAction(a);
		if (CoreUtil.equals(a, combatEndAction)) {
			combatEndAction = null;
		}
	}
	
	@Override
	public void readBrainFromXML(Element brainElement) throws IOException {
		super.readBrainFromXML(brainElement);
		Element combatEndSearchActionElement = brainElement.getChildByName(XML_COMBAT_END_ACTION);
		if (combatEndSearchActionElement != null && combatEndSearchActionElement.getChildCount() > 0) {
			combatEndAction = Action.readFromXML(combatEndSearchActionElement.getChild(0), character);
		}
	}
	
	@Override
	public void writeBrainToXML(XmlWriter writer) throws IOException {
		super.writeBrainToXML(writer);
		if (combatEndAction != null) {
			writer.element(XML_COMBAT_END_ACTION);
			combatEndAction.writeToXML(writer);
			writer.pop();
		}
	}

}
