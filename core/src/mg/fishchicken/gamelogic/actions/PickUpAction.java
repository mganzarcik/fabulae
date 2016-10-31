package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.CharacterFilter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.inventory.PickableGameObject;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.crime.Theft;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Moves the PlayerCharacter to target PickableGameObject
 * and picks it up.
 * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>targetItem - PickableGameObject - the GO to pick up
 * </ol>
 * @author Annun
 *
 */
public class PickUpAction extends MoveToAction implements SkillCheckModifier {
	private GameCharacter character;
	private String targetId;
	private PickableGameObject targetItem;
	
	public PickUpAction() {
	}
	
	public PickUpAction(GameCharacter character, PickableGameObject pickable) {
		init(character, pickable);
	}

	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("PickUpAction only works on GameCharacter!");
		}
		character = (GameCharacter) ac;
		if (parameters.length > 0) {
			this.targetItem = (PickableGameObject)parameters[0];
			targetId = targetItem.getInternalId();
			Tile pos = targetItem.position().tile();
			super.init(ac, pos.getX(), pos.getY());
		}
	}
	
	@Override
	public void update(float deltaTime) {
		if (targetItem == null && targetId != null) {
			this.targetItem = (PickableGameObject)GameState.getGameObjectById(targetId);
			Tile pos = targetItem.position().tile();
			super.init(character,pos.getX(), pos.getY());
		}
		
		super.update(deltaTime);
		if(isFinished() && MathUtil.isNextToOrOnTarget(character, targetItem)) {
			int apCost = Configuration.getAPCostPickUp();
			if (GameState.isCombatInProgress() && character.stats().getAPAct() < apCost) {
				return;
			}
			character.stats().addToAP(-apCost);
			if (character.isSneaking() 
					&& character.getAllCharactersInSightRadius(null, CharacterFilter.NOT_SAME_FACTION, CharacterFilter.AWAKE)
					&& !character.stats().rollSkillCheck(Skill.SNEAKING, this)) {
				character.setIsSneaking(false);
			}
			boolean canPickUp = true;
			if(!targetItem.getOwner().includes(character)) {
				canPickUp = !gameState.getCrimeManager().registerNewCrime(new Theft(character, targetItem.getInventoryItem()));
			}
			if (canPickUp && targetItem.pickUp(character)) {	
				Log.logLocalized("itemPickedUp", LogType.INVENTORY, targetItem.getName(), character.getName());
			}
		}
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		targetId = actionElement.getAttribute(XML_ATTRIBUTE_TARGET, null);
		if (targetId == null) {
			throw new GdxRuntimeException("target must be specified!");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_TARGET, targetId);
	}

	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		if (Skill.SNEAKING == skill) {
			return Configuration.getPickUpItemStealthModifier() + skillUser.getMap().getSkillCheckModifier(skill, skillUser);
		}
		return skillUser.getMap().getSkillCheckModifier(skill, skillUser);
	}
}
