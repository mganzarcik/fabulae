package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.CharacterFilter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.traps.TrapType;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.locks.Lock;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Moves the GameCharacter to target UsableGameObject and uses it. If the
 * target is locked, it will try to unlock it if the character has the required key. If
 * the target cannot be unlocked or is currently unusable, a message will be
 * logged. <br />
 * <br />
 * Parameters:
 * <ol>
 * <li>targetGameObject - UsableGameObject - the UsableGameObject to use
 * </ol>
 * 
 * @author Annun
 *
 */
public class UseGameObjectAction extends MoveToAction implements SkillCheckModifier {
	private AbstractGameCharacter character;
	private String targetId;
	private UsableGameObject targetGameObject;

	public UseGameObjectAction() {
	}

	public UseGameObjectAction(GameCharacter character, UsableGameObject usable) {
		init(character, usable);
	}

	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof AbstractGameCharacter)) {
			throw new GdxRuntimeException("UseGameObjectAction only works on GameCharacter!");
		}
		targetId = null;
		character = (AbstractGameCharacter) ac;
		if (parameters.length > 0) {
			this.targetGameObject = (UsableGameObject) parameters[0];
			this.targetId = targetGameObject.getInternalId();
			initSuper();
		}
	}
	
	private void initSuper() {
		Tile ground = targetGameObject.getGround();
		int targetX = ground.getX(); 
		int targetY = ground.getY();
		super.init(character, targetX, targetY, !targetGameObject.hasSprite() || character.getMap().blocked(character, targetX, targetY));
	}

	@Override
	public void update(float deltaTime) {
		if (targetGameObject == null && targetId != null) {
			this.targetGameObject = (UsableGameObject) GameState.getGameObjectById(targetId);
			initSuper();
		}
		if (!super.isFinished()) {
			super.update(deltaTime);
		}
		if (super.isFinished()) {
			if (MathUtil.isNextToOrOnTarget(character, targetGameObject.getGround())) {
				Lock lock = targetGameObject.getLock();
				GameCharacter representative = character.getRepresentative();
				if (!lock.isLocked()
						|| (lock.getKeyId() != null && representative.getInventory().getItem(lock.getKeyId()) != null)) {
					// if the usable was locked with a specific key, we unlock
					// it for good
					if (lock.isLocked() && lock.getKeyId() != null) {
						lock.setLocked(false);
						Log.logLocalized(STRING_TABLE, "unlocked", LogType.INFO, representative.getName(),
								targetGameObject.getName(), representative.getInventory().getItem(lock.getKeyId()).getName());
					}
					if (!GameState.isCombatInProgress()
							|| representative.stats().getAPAct() >= targetGameObject.getApCostToUse()) {
						if (!TrapType.checkTrap(representative, targetGameObject, "trapSprungUsable")) {
							Log.logLocalized(STRING_TABLE, targetGameObject.getActionKey(), LogType.INFO, representative.getName(),
									targetGameObject.getName());
							targetGameObject.use(representative);
							representative.stats().addToAP(-targetGameObject.getApCostToUse());
							if (representative.isSneaking()
									&& representative.getAllCharactersInSightRadius(null, CharacterFilter.NOT_SAME_FACTION, CharacterFilter.AWAKE)
									&& !representative.stats().rollSkillCheck(Skill.SNEAKING, this)) {
								representative.setIsSneaking(false);
							}
						} else {
							representative.stats().addToAP(-targetGameObject.getApCostToUse());
						}
					} else {
						Log.logLocalized("cannotUse", LogType.COMBAT, representative.getName(), targetGameObject.getName(),
								representative.stats().getGender().getPronoun().toLowerCase(),
								targetGameObject.getApCostToUse());
					}
				} else {
					if (!TrapType.checkTrap(representative, targetGameObject, "trapSprungUsable")) {
						Log.logLocalized(STRING_TABLE, "cannotUseLocked", LogType.INFO, character.getName(),
								targetGameObject.getName());
					}
				}
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
			return Configuration.getUseObjectStealthModifier()
					+ skillUser.getMap().getSkillCheckModifier(skill, skillUser);
		}
		return skillUser.getMap().getSkillCheckModifier(skill, skillUser);
	}
}
