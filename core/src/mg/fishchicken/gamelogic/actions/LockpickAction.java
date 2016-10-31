package mg.fishchicken.gamelogic.actions;

import java.io.IOException;
import java.util.HashSet;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.CharacterFilter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locks.Lockable;
import mg.fishchicken.gamelogic.traps.TrapType;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.crime.Trespass;
import mg.fishchicken.gamestate.locks.Lock;
import mg.fishchicken.gamestate.traps.Trap;
import mg.fishchicken.pathfinding.Path;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Finds the Lockable at the supplied coordinates, 
 * moves the GameCharacter to is and then tries to pick the lock
 * on it if it is locked and pickable.
 * 
 * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>x - float - the x coordinate of the Lockable
 *	<li>y - float - the y coordinate of the Lockable
 * </ol>
 *
 */
public class LockpickAction extends MoveToAction implements SkillCheckModifier {
	private GameCharacter character;
	private float targetX, targetY;
	private Lockable lockable;
	private ObjectSet<GameLocation> tempLocations = new ObjectSet<GameLocation>();
	
	public LockpickAction() {
	}
	
	public LockpickAction(GameCharacter character, Lockable lockable) {
		init(character, lockable);
	}

	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("LockpickAction only works on GameCharacter!");
		}
		targetX = -1;
		targetY = -1;
		character = (GameCharacter) ac;
		if (parameters.length > 0) {
			this.lockable = (Lockable)parameters[0];
			this.targetX = lockable.position().getX();
			this.targetY = lockable.position().getY();
			super.init(ac, lockable.findSafeDisarmPath(new Path(), character, character.getMap().getPathFinder(), this.getClass()));
			
		}
	}
	
	@Override
	public void update(float deltaTime) {
		if (lockable == null && targetX != -1 && targetY != -1) {
			GameMap map = gameState.getCurrentMap();
			this.lockable = (Lockable) map.getGameObjectAt(targetX, targetY, Lockable.class);
			if (this.lockable == null) {
				throw new GdxRuntimeException("no lockable found at "+targetX+", "+targetY+"!");
			}
			super.init(character, lockable.findSafeDisarmPath(new Path(), character, character.getMap().getPathFinder(), this.getClass()));
		}
		if (!super.isFinished()) {
			super.update(deltaTime);
		}
		if(super.isFinished()) {
			if (MathUtil.isNextToOrOnTarget(character, lockable.getGround())) {
				Lock lock = lockable.getLock();
				if (lock == null || !lock.isLocked()) {
					Log.logLocalized(STRING_TABLE, "cannotLockpickNotLocked", LogType.INFO, character.getName());
				} else if (!lock.isPickable()) {
					Log.logLocalized(STRING_TABLE, "cannotLockpickNotPickable", LogType.INFO, character.getName());
				} else {
					if (!GameState.isCombatInProgress() || character.stats().getAPAct() >= Configuration.getAPCostUseItem()) {
						
						if (character.isSneaking() 
								&& character.getAllCharactersInSightRadius(null, CharacterFilter.NOT_SAME_FACTION, CharacterFilter.AWAKE)
								&& !character.stats().rollSkillCheck(Skill.SNEAKING, this)) {
							character.setIsSneaking(false);
						}
						
						Tile position = lockable.getGround();
						tempLocations.clear();
						character.getMap().getLocationsAt(tempLocations, position.getX(), position.getY());
						tempLocations.add(character.getMap());
						HashSet<Faction> factions = new HashSet<Faction>();
						
						for (GameLocation location : tempLocations) {
							factions.add(location.getOwnerFaction());
						}
						
						boolean crimeSpotted = false;
						
						for (GameLocation location : tempLocations) {
							Faction locFaction = location.getOwnerFaction();
							if (factions.contains(locFaction) && Faction.NO_FACTION != locFaction) {
								factions.remove(locFaction);
								crimeSpotted = crimeSpotted || gameState.getCrimeManager().registerNewCrime(new Trespass(character, location));
							}
						}
						
						Trap trap = lockable.getTrap();
						if (trap != null && !trap.isDisarmed()) {
							TrapType.springTrap(trap, lockable.getOriginatorGameObject(), character, "trapSprungLockpick");
						} else if (!crimeSpotted) {
							if (lock.getLockLevel() <= character.stats().skills().getSkillRank(Skill.LOCKPICKING)) {
								lock.setLocked(false);
								Log.logLocalized(STRING_TABLE, "lockpickSuccess", LogType.INFO, character.getName());
								character.stats().giveExperience(lock.getLockLevel()*5);
							} else {
								Log.logLocalized(STRING_TABLE, "lockpickFail", LogType.INFO, character.getName());
							}
						}
						
						character.stats().addToAP(-Configuration.getAPCostUseItem());
					} 
				}
			} 
		}
	}
	
	@Override
	public boolean isFinished() {
		if (lockable.getLock() == null || !lockable.getLock().isLocked() || !lockable.getLock().isPickable()) {
			return true;
		}
		return super.isFinished();
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		targetX = actionElement.getFloatAttribute(XML_ATTRIBUTE_X, -1);
		if (targetX == -1) {
			throw new GdxRuntimeException("x must be specified!");
		}
		
		targetY = actionElement.getFloatAttribute(XML_ATTRIBUTE_Y, -1);
		if (targetY == -1) {
			throw new GdxRuntimeException("y must be specified!");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_X, targetX);
		writer.attribute(XML_ATTRIBUTE_Y, targetY);
	}

	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		if (Skill.SNEAKING == skill) {
			return Configuration.getPicLockStealthModifier() + skillUser.getMap().getSkillCheckModifier(skill, skillUser);
		}
		return skillUser.getMap().getSkillCheckModifier(skill, skillUser);
	}
}
