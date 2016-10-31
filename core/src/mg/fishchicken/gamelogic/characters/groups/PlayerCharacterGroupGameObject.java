package mg.fishchicken.gamelogic.characters.groups;

import java.io.IOException;
import java.util.Iterator;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.PlayerCharacter;
import mg.fishchicken.gamelogic.combat.CombatManager;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.locations.CombatGameMap.CombatMapInitializationData;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.dialog.OkCancelCallback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class PlayerCharacterGroupGameObject extends AbstractGameCharacter implements XMLLoadable {
	
	private PlayerCharacterGroup group;
	private ObjectMap<GameCharacter, Array<Action>> pcActionsToUpdate = new ObjectMap<GameCharacter, Array<Action>>();
	
	private int s_tilesFromLastRandomEncounter = 0;
	private float s_speed;
	
	public PlayerCharacterGroupGameObject() {
		super();
	}
	
	public PlayerCharacterGroupGameObject(String id, String type) throws IOException {
		super(id, type);
		loadFromXML(Gdx.files.internal(Configuration.getFolderGroups()+"PlayerGroup.xml"));
		group = GameState.getPlayerCharacterGroup();
	}
	
	@Override
	public void addVisitedLocation(String locationId) {
		super.addVisitedLocation(locationId);
		Array<GameCharacter> members = group.getMembers();
		for (int i = 0; i < members.size; ++i) {
			members.get(i).addVisitedLocation(locationId);
		}
	}
	
	/**
	 * Called whenever an action has been added to a PC that's a member
	 * of the player character group.
	 * 
	 */
	public void actionAdded(PlayerCharacter pc, Action action) {
		if (getMap() != null && getMap().isWorldMap()) {
			// lets check if the action can be finished instantaneously
			action.update(0);
			if (!action.isFinished()) {
				Array<Action> actions = pcActionsToUpdate.get(pc);
				if (actions == null) {
					actions = new Array<Action>();
					pcActionsToUpdate.put(pc, actions);
				}
				actions.add(action);
			} else {
				pc.removeAction(action);
			}
		}
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (getMap() != null) {
			float gameSeconds = deltaTime * getMap().getGameTimeMultiplier();
			float effectDurationDelta = gameSeconds / Configuration.getCombatTurnDurationInGameSeconds();
			Array<GameCharacter> pcs = group.getPlayerCharacters();
			for (int i = 0; i < pcs.size; ++i) {
				GameCharacter character = pcs.get(i);
				if (character.isActive()) {
					character.updateSurvival(gameSeconds);
				}
				
				character.updatePersistentEffects(effectDurationDelta);
			}
			
			Entries<GameCharacter, Array<Action>> entries = pcActionsToUpdate.entries();
			
			for (Entry<GameCharacter, Array<Action>> entry : entries) {
				Iterator<Action> actions = entry.value.iterator();
				while (actions.hasNext()) {
					Action action = actions.next();
					if (action.isPaused()) {
						continue;
					}
					action.update(deltaTime);
					if (action.isFinished()) {
						actions.remove();
						entry.key.removeAction(action);
					}
				}
				if (entry.value.size < 1) {
					entries.remove();
				}
			}
			
		}
	}
	
	@Override
	protected void changedTile() {
		super.changedTile();
		if (s_tilesFromLastRandomEncounter != -1) {
			++s_tilesFromLastRandomEncounter;
			if (s_tilesFromLastRandomEncounter > Configuration.getRandomEncountersCooldown()) {
				s_tilesFromLastRandomEncounter = -1;
			}
		}
		if (s_tilesFromLastRandomEncounter == -1) {
			final CharacterGroup randomEncounter = getMap().getRandomEncounter(position.tile(), group.getAverageLevel(true));
			if (randomEncounter != null) {
				
				s_tilesFromLastRandomEncounter = 0;
				
				GameCharacter bestScout = group.getMemberWithHighestSkill(Skill.SCOUTING);
				
				if (bestScout.stats().skills().getSkillRank(Skill.SCOUTING) > 0) {
					if (!bestScout.stats().rollSkillCheck(Skill.SCOUTING, randomEncounter)) {
						startRandomEncounter(randomEncounter, true);
					}
					else {
						gameState.pauseGame();
						UIManager.displayConfirmation(
								Strings.getString(CombatManager.STRING_TABLE, "ambushFightQuestion"),
								Strings.getString(CombatManager.STRING_TABLE, "ambushCanBeAvoided", bestScout.getName(), randomEncounter.getName(), randomEncounter.getDescription()), 
								new OkCancelCallback<Void>() {
									@Override
									public void onOk(Void nada) {
										startRandomEncounter(randomEncounter, false);
									}
									@Override
									public void onCancel() {
										Log.logLocalized("ambushAvoided", LogType.COMBAT);
										gameState.unpauseGame();
									}
								}
						);
					}
				} else {
					startRandomEncounter(randomEncounter, true);
				}
			}
		}
	}
	
	public void startRandomEncounter(CharacterGroup randomEncounter, boolean logAmbush) {
		CombatMapInitializationData data = new CombatMapInitializationData(getMap().getId(), randomEncounter, position.tile(), position.tile());
		if (logAmbush) {
			Log.logLocalized("ambushed", LogType.COMBAT);
		}
		removeAllVerbActions();
		gameState.switchToCombatMap(data);
	}
	
	public CharacterGroup getGroup() {
		return group;
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		loadFromXML(root);
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		if (group == null) {
			group = GameState.getPlayerCharacterGroup();
		}
		super.loadFromXML(root);
	}

	@Override
	public String getDialogueId() {
		return group.getGroupLeader().getDialogueId();
	}

	@Override
	public GameCharacter getRepresentative() {
		return group.getGroupLeader();
	}
	
	@Override
	public Faction getFaction() {
		return Faction.PLAYER_FACTION;
	}
	
	@Override
	public boolean isActive() {
		return true;
	}
	
	@Override
	public boolean shouldRenderDestination() {
		return true;
	}
	
	@Override
	public boolean isAsleep() {
		Array<GameCharacter> members = group.getMembers();
		for (int i = 0; i < members.size; ++i) {
			if (!members.get(0).isAsleep()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public float getSpeed() {
		return s_speed;
	}
}