package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.audio.Sound;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.gamelogic.characters.CharacterFilter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.traps.Trap;
import mg.fishchicken.pathfinding.Path;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Finds the Trapable at the supplied coordinates, 
 * moves the GameCharacter to is and then tries to disarme the trap
 * on it if it is trapped and detected.
 * 
 * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>x - float - the x coordinate of the Trapable
 *	<li>y - float - the y coordinate of the Trapable
 * </ol>
 *
 */
public class DisarmTrapAction extends MoveToAction implements SkillCheckModifier {
	private GameCharacter character;
	private float targetX, targetY;
	private Trapable trapable;
	private ObjectSet<GameLocation> tempLocations = new ObjectSet<GameLocation>();
	
	public DisarmTrapAction() {
	}
	
	public DisarmTrapAction(GameCharacter character, Trapable trapable) {
		init(character, trapable);
	}

	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("DisarmTrapAction only works on GameCharacter!");
		}
		targetX = -1;
		targetY = -1;
		character = (GameCharacter) ac;
		if (parameters.length > 0) {
			this.trapable = (Trapable)parameters[0];
			this.targetX = trapable.position().getX();
			this.targetY = trapable.position().getY();
			super.init(ac, trapable.findSafeDisarmPath(new Path(), character, character.getMap().getPathFinder(), this.getClass()));
		}
	}
	
	@Override
	public void update(float deltaTime) {
		if (trapable == null && targetX != -1 && targetY != -1) {
			GameMap map = gameState.getCurrentMap();
			this.trapable = (Trapable) map.getGameObjectAt(targetX, targetY, Trapable.class);
			if (this.trapable == null) {
				tempLocations.clear();
				map.getLocationsAt(tempLocations, (int)targetX, (int)targetY);
				for (GameLocation location : tempLocations) {
					if (location instanceof Trapable) {
						this.trapable = (Trapable) location;
						break;
					}
				}
			}
			if (this.trapable == null) {
				throw new GdxRuntimeException("no trapable found at "+targetX+", "+targetY+"!");
			}
			super.init(character, trapable.findSafeDisarmPath(new Path(), character, character.getMap().getPathFinder(), this.getClass()));
		}
		if (!super.isFinished()) {
			super.update(deltaTime);
		}
		if(super.isFinished()) {
			Trap trap = trapable.getTrap();
			if (trap == null || trap.isDisarmed() || !trap.isDetected()) {
				Log.logLocalized(STRING_TABLE, "cannotDisarmNotTrapped", LogType.INFO, character.getName());
			} else {
				if (!GameState.isCombatInProgress() || character.stats().getAPAct() >= Configuration.getAPCostDisarmTrap()) {
					
					if (character.isSneaking() 
							&& character.getAllCharactersInSightRadius(null, CharacterFilter.NOT_SAME_FACTION, CharacterFilter.AWAKE)
							&& !character.stats().rollSkillCheck(Skill.SNEAKING, this)) {
						character.setIsSneaking(false);
					}
					
					if (trap.getType().getLevel() <= character.stats().skills().getSkillRank(Skill.TRAPS)) {
						trap.setDisarmed(true);
						Sound sound = trap.getType().getDisarmedSound();
						if (sound != null) {
							sound.play(trapable.getOriginatorGameObject());
						}
						Log.logLocalized(STRING_TABLE, "trapDisarmSuccess", LogType.INFO, character.getName(), trap.getType().getName());
						character.stats().giveExperience(trap.getType().getLevel()*3);
					} else {
						Log.logLocalized(STRING_TABLE, "trapDisarmFail", LogType.INFO, character.getName(), trap.getType().getName());
					}
					
					character.stats().addToAP(-Configuration.getAPCostDisarmTrap());
				} 
			}
		}
	}
	
	@Override
	public boolean isFinished() {
		if (trapable.getTrap() == null || trapable.getTrap().isDisarmed() || !trapable.getTrap().isDetected()) {
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
			return Configuration.getDisarmTrapStealthModifier() + skillUser.getMap().getSkillCheckModifier(skill, skillUser);
		}
		return skillUser.getMap().getSkillCheckModifier(skill, skillUser);
	}
}