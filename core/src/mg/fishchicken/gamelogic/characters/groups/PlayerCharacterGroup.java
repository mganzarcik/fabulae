package mg.fishchicken.gamelogic.characters.groups;

import java.io.IOException;
import java.util.HashSet;

import mg.fishchicken.core.BasicCallback;
import mg.fishchicken.core.FastForwardCallback;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.VariableContainer;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.DisarmTrapAction;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.Role;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.PlayerCharacterGroupInventory;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemOwner;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.survival.FoodAndWaterSource;
import mg.fishchicken.gamelogic.survival.SurvivalFastForwardCallback;
import mg.fishchicken.gamelogic.survival.SurvivalManager;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.Variables;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.Path.Step;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.dialog.ProgressDialogSettings;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class PlayerCharacterGroup extends CharacterGroup implements FoodAndWaterSource, InventoryContainer, VariableContainer, AssetContainer, XMLSaveable  {

	public static final String XML_SELECTED = "selected";
	public static final String XML_PC_MEMBERS = "pcMembers";
	public static final String XML_NPC_MEMBERS = "npcMembers";
	public static final String XML_TEMP_REMOVED = "tempRemoved";
	public static final String XML_CREATED = "created";
	public static final String PLAYER_CHARACTER_GROUP_OBJECT_ID = "__playerCharacterGroupGameObject";
	
	private static final String INTERNAL_FORBIDDER_ID = "__internalNPCJoinedPCHGForbidder";
	
	/** 
	 * This stores all PCs regardless of whether they are currently selected.
	 * The order they are stored in is important.
	 */
	private Array<GameCharacter> playerCharacters;
	
	/** 
	 * NPCs here are members who are not "full" members - they do not show on the PC panel, you cannot see their inventory,
	 * they do not reveal fog of war, they cannot initiate dialogue and enter transitions.
	 */
	private Array<GameCharacter> nonPlayerCharacters; 
	private Array<GameCharacter> selectedCharacters;
	/**
	 * Characters that were created during the start game party creation.
	 */
	private Array<GameCharacter> createdCharacters;
	private HashSet<GameCharacter> tempRemoved;
	private Inventory junkBag;
	private Variables variables;
	private PlayerCharacterGroupGameObject characterGroupGameObject;
	private int s_gold;
	private float s_food;
	private float s_water;
	private GameState gameState;
	
	public PlayerCharacterGroup(GameState gameState) {
		super();
		this.gameState = gameState;
		playerCharacters = new Array<GameCharacter>(true, 6, GameCharacter.class);
		nonPlayerCharacters = new Array<GameCharacter>(true, 3, GameCharacter.class);
		selectedCharacters = new Array<GameCharacter>(true, 9, GameCharacter.class);
		createdCharacters = new Array<GameCharacter>(true, 6, GameCharacter.class);
		tempRemoved = new HashSet<GameCharacter>();
		junkBag = new PlayerCharacterGroupInventory(this);
		variables = new Variables();
	}
	
	public PlayerCharacterGroupGameObject getGroupGameObject() {
		if (characterGroupGameObject == null) {
			try {
				characterGroupGameObject = (PlayerCharacterGroupGameObject)GameState.getGameObjectById(PlayerCharacterGroup.PLAYER_CHARACTER_GROUP_OBJECT_ID);
				if (characterGroupGameObject == null) {
					characterGroupGameObject = new PlayerCharacterGroupGameObject(PLAYER_CHARACTER_GROUP_OBJECT_ID, PLAYER_CHARACTER_GROUP_OBJECT_ID);
				}
			} catch (IOException e) {
				throw new GdxRuntimeException(e);
			}
		}
		return characterGroupGameObject;
	}
	
	/**
	 * Returns true if any member of the group visited the supplied
	 * location during the course of the game.
	 * 
	 * @param location
	 * @return
	 */
	public boolean visitedLocation(String locationId) {
		for (GameCharacter character : playerCharacters.items) {
			if (character != null && character.visitedLocation(locationId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if any member of the group visited the supplied
	 * location during the course of the game.
	 * 
	 * @param location
	 * @return
	 */
	public boolean visitedLocation(GameLocation location) {
		return visitedLocation(location.getId());
	}
	
	/**
	 * Returns the number of currently selected characters
	 * in the group.
	 * @return
	 */
	public int getSelectedSize() {
		return selectedCharacters.size;
	}
	
	/**
	 * This will return the player characters
	 * that are members of this group.
	 * 
	 * Modifying this array will modify the group,
	 * it is NOT a copy!
	 * 
	 */
	public Array<GameCharacter> getPlayerCharacters() {
		return playerCharacters;
	}
	
	/**
	 * Returns currrently selected members, both PC and NPC.
	 * 
	 * Modifying this array will modify the group,
	 * it is NOT a copy!
	 * 
	 * @return
	 */
	public Array<GameCharacter> getSelected() {
		return selectedCharacters;
	}
	
	/**
	 * This will return the non player characters
	 * that are members of this group.
	 * 
	 * Modifying this array will modify the group,
	 * it is NOT a copy!
	 * 
	 */
	public Array<GameCharacter> getNonPlayerCharacters() {
		return nonPlayerCharacters;
	}
	
	public void removeAllNonPlayerCharacters() {
		for (GameCharacter npc : nonPlayerCharacters) {
			deselectMember(npc);
			Faction.PLAYER_FACTION.removeMember(npc);
			npc.allowAllActions(INTERNAL_FORBIDDER_ID);
			super.removeMember(npc);
			npc.resetCharacterCircleColor();
		}
		nonPlayerCharacters.clear();
	}
	
	public void removeNonPlayerCharacter(GameCharacter memberToRemove) {
		deselectMember(memberToRemove);
		Faction.PLAYER_FACTION.removeMember(memberToRemove);
		if (nonPlayerCharacters.removeValue(memberToRemove, false)) {
			memberToRemove.allowAllActions(INTERNAL_FORBIDDER_ID);
			super.removeMember(memberToRemove);
		}
		memberToRemove.resetCharacterCircleColor();
	}
	
	@Override
	public void removeMember(GameCharacter memberToRemove) {
		deselectMember(memberToRemove);
		super.removeMember(memberToRemove);
		Faction.PLAYER_FACTION.removeMember(memberToRemove);
		if (nonPlayerCharacters.removeValue(memberToRemove, false)) {
			memberToRemove.allowAllActions(INTERNAL_FORBIDDER_ID);
		}
		if (playerCharacters.removeValue(memberToRemove, false)) {
			Log.logLocalized("characterLeft", LogType.CHARACTER, memberToRemove.getName());
			// refresh the PC panel in case we are on a loaded map
			if (gameState.getCurrentMap() != null && gameState.getCurrentMap().isMapLoaded()) {
				UIManager.refreshPCPanel();
			}
		}
		memberToRemove.resetCharacterCircleColor();
	}
	
	/**
	 * This will add a new member as a player character.
	 * 
	 * @return true if the character was added, or false if it was not because the upper limit of
	 * group members has been reached
	 * 
	 */
	@Override
	public boolean addMember(GameCharacter newMember) {
		return addMember(newMember, true, false);
	}
	
	/**
	 * This will add a new member as a non player character.
	 * 
	 */
	public void addNonPlayerCharacter(GameCharacter newMember) {
		addMember(newMember, false, false);
	}
	
	/**
	 * Adds new PC to the group. The PC will become a member but it will not
	 * become automatically selected!
	 * 
	 * Call selectMember(PlayerCharacter) for that.
	 * 
	 * @param newMember
	 *            - the PC to add to the group
	 * @param updateFogOfWar
	 *            - whether or not to update the maps fog of war with the line
	 *            of sight of the new member
	 * @return true if the character was added, or false if it was not because the upper limit of
	 * group members has been reached
	 */
	public boolean addMember(GameCharacter newMember, boolean asPC, boolean updateFogOfWar) {
		Array<GameCharacter> characterArray = asPC ? playerCharacters : nonPlayerCharacters;
		
		if (asPC && playerCharacters.size >= Configuration.getMaxCharactersInGroup()) {
			return false;
		}
		
		if (!characterArray.contains(newMember, false)) {
			Faction.PLAYER_FACTION.addMember(newMember);
			super.addMember(newMember);
			characterArray.add(newMember);
			if(newMember.isSelected()) {
				selectedCharacters.add(newMember);
			}
			
			if (asPC) {
				if (updateFogOfWar && newMember.getMap() != null) {
					newMember.updateLightsPosition(true);
					newMember.updateVisibleArea();
				}
				// refresh the PC panel in case we are on a loaded map
				if (gameState.getCurrentMap() != null && gameState.getCurrentMap().isMapLoaded()) {
					UIManager.refreshPCPanel();
				}
				Log.logLocalized("characterJoined", LogType.CHARACTER, newMember.getName());
			} else {
				newMember.forbidActions(INTERNAL_FORBIDDER_ID, Action.PC_ONLY_VERBS);
			}
		}
		
		newMember.resetCharacterCircleColor();
		return true;
	}
	
	/**
	 * Temporarily removes the supplied character from the group.
	 * 
	 * This will not actually remove the character (as in, he will still have his
	 * portrait displayed, etc), but the player will be unable to control him.
	 * 
	 * @param character
	 */
	public void temporarilyRemoveMember(GameCharacter character) {
		if (playerCharacters.contains(character, false)) {
			tempRemoved.add(character);
			deselectMember(character);
		}
	}

	/**
	 * Restores the "full membership" of the temporarily removed character.
	 * 
	 * @param character
	 */
	public void restoreTemporarilyRemovedMember(GameCharacter character) {
		tempRemoved.remove(character);
	}
	
	/**
	 * Sets the supplied member as selected and 
	 * deselects all the other members;
	 * @param newMember
	 */
	public void selectOnlyMember(GameCharacter newMember) {
		if (!newMember.isActive()) {
			return;
		}
		for (GameCharacter pc : getMembers()) {
			deselectMember(pc);
		}
		selectMember(newMember);
	}
	
	/**
	 * Adds the supplied member among the 
	 * selected members.
	 *  
	 * @param newMember
	 */
	public void selectMember(GameCharacter newMember) {
		if (!newMember.isActive() || tempRemoved.contains(newMember)) {
			return;
		}
		if (!selectedCharacters.contains(newMember, false)) {
			selectedCharacters.add(newMember);
			newMember.setSelected(true);
		}
	}
	
	/**
	 * Selects all members of the group.
	 * 
	 */
	public void selectAll() {
		Array<GameCharacter> members = getMembers();
		for (int i = 0; i < members.size; ++i) {
			selectMember(members.get(i));
		}
	}
	
	/**
	 * Removes the supplied member from the
	 * selected members (without removing him
	 * from the group itself).
	 * 
	 * Deselection is not permitted
	 * on the worldMap.
	 * 
	 * @param member
	 */
	public void deselectMember(GameCharacter member) {
		if (gameState.getCurrentMap().isWorldMap()) {
			return;
		}
		selectedCharacters.removeValue(member, false);
		member.setSelected(false);
		gameState.getPlayerCharacterController().resetWaitingForClickConfirmation();
	}
		
	/**
	 * Either adds or removes the supplied member
	 * from the selected members based on his current 
	 * selection status.
	 * @param member
	 */
	public void toggleMemberSelection(GameCharacter member) {
		if (member.isSelected()) {
			deselectMember(member);
		} else {
			selectMember(member);
		}
	}
	
	private int getCharacterIndex(GameCharacter pc) {
		int index = playerCharacters.indexOf(pc, false);
		if (index < 0) {
			index = playerCharacters.size + nonPlayerCharacters.indexOf(pc, false);
		}
		return index;
	}
	
	public boolean containsPlayerCharacter(GameCharacter pc) {
		if (tempRemoved.contains(pc)) {
			return false;
		}
		return playerCharacters.contains(pc, false);
	}
	
	public boolean containsCharacter(GameCharacter pc) {
		if (tempRemoved.contains(pc)) {
			return false;
		}
		return getMembers().contains(pc, false);
	}
	
	public boolean containsPlayerCharacter(String pcId) {
		for (GameCharacter pc : playerCharacters) {
			if (pcId.equalsIgnoreCase(pc.getId()) && !tempRemoved.contains(pc)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Array<GameCharacter> setPosition(Vector2 position, Orientation orientation,
			GameMap map) {
		if (map.isWorldMap()) {
			getGroupGameObject().position().set(position);
			getGroupGameObject().setOrientation(orientation);
			return new Array<GameCharacter>();
		}
		return super.setPosition(position, orientation, map);
	}
	
	/**
	 * Gets the current group leader.  This is the first active, selected character of the group.
	 */
	@Override
	public GameCharacter getGroupLeader() {
		return getGroupLeader(false);
	}
	
	/**
	 * Gets the current group leader.  This is the first selected character of the group.
	 * @param pCOnly - if true, only player characters will be considered. If false, even temporary NPC members will be considered.
	 * @return
	 */
	public GameCharacter getGroupLeader(boolean pCOnly) {
		for (int i = 0; i < playerCharacters.size; ++i) {
			GameCharacter character = playerCharacters.get(i);
			if (character.isActive() && getSelected().contains(character, false)) {
				return character;
			}
		}
		
		if (!pCOnly) {
			for (int i = 0; i < nonPlayerCharacters.size; ++i) {
				GameCharacter character = nonPlayerCharacters.get(i);
				if (character.isActive() && getSelected().contains(character, false)) {
					return character;
				}
			}
		}
		return null;
	}
	
	@Override
	protected int getFormationIndex(GameCharacter character) {		
		return getCharacterIndex((GameCharacter)character) - getCharacterIndex((GameCharacter)getGroupLeader());
	}
	
	/**
	 * Returns the distance from the supplied point to the member that is
	 * nearest to the point.
	 * 
	 * Returns null if the group has no members.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Float getDistanceToTheNearestMember(float x, float y, GameMap map) {
		Vector2 position = MathUtil.getVector2().set(x, y);
		Float returnValue = null;
		
		if(map.isWorldMap()) {
			Position ggoPosition = getGroupGameObject().position();
			returnValue = position.dst(ggoPosition.getX(), ggoPosition.getY());
			
		} else {
			for (GameCharacter pc : playerCharacters) {
				Position pcPos = pc.position();
				if (returnValue == null) {
					returnValue = position.dst(pcPos.getX(), pcPos.getY());
				} else {
					float distance = position.dst(pcPos.getX(), pcPos.getY());
					if (distance < returnValue) {
						returnValue = distance;
					}
				}
			}
		}
		MathUtil.freeVector2(position);
		return returnValue;
	}
	
	/**
	 * Returns true if any player character member of this group that is not invisible
	 * can see any enemy
	 * @return
	 */
	public boolean canSeeEnemy() {
		for (GameCharacter pc : playerCharacters) {
			if (!pc.isInvisible() && pc.hasEnemiesInSight()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Variables variables() {
		return variables;
	}
	
	@Override
	public Inventory getInventory() {
		return junkBag;
	}
	
	@Override
	public boolean isOwnerOf(InventoryItem item) {
		Array<GameCharacter> members = getMembers();
		ItemOwner owner = item.getOwner();
		for (int i = 0; i < members.size; ++i) {
			if (owner.includes(members.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "Party";
	}

	/**
	 * Gets the current amount of gold in the group's gold pool.
	 * @return
	 */
	public int getGold() {
		return s_gold;
	}

	/**
	 * Adds the supplied amount of gold to the group's gold pool.
	 * @param gold
	 */
	public void addGold(int gold) {
		s_gold += gold;
	}
	
	/**
	 * Gets the current amount of food in the group's food pool.
	 * @return
	 */
	public float getFood() {
		s_food = MathUtil.boxValue(s_food, 0, getMaxFood());
		return s_food;
	}

	/**
	 * Adds the supplied amount of food to the group's food pool.
	 * @param water
	 */
	public void addFood(float food) {
		s_food += food;
		if (s_food < 0) {
			s_food = 0;
		} else if (s_food > getMaxFood()) {
			s_food = getMaxFood();
		}
	}
	
	/**
	 * Returns the maximum amount of food this group can carry.
	 * 
	 * @return
	 */
	public float getMaxFood() {
		float returnValue = 0;
		for (int i = 0; i < playerCharacters.size; ++i) {
			GameCharacter character = playerCharacters.get(i);
			if (character.isActive()) {
				returnValue += character.stats().getRace().getMaxRations();
			}
		}
		return returnValue;
	}
	
	/**
	 * Gets the current amount of water in the group's water pool.
	 * @return
	 */
	public float getWater() {
		s_water = MathUtil.boxValue(s_water, 0, getMaxWater());
		return s_water;
	}

	/**
	 * Adds the supplied amount of water to the group's water pool.
	 * @param water
	 */
	public void addWater(float water) {
		s_water += water;
		if (s_water < 0) {
			s_water = 0;
		} else if (s_water > getMaxWater()) {
			s_water = getMaxWater();
		}
	}
	
	/**
	 * Returns the maximum amount of water this group can carry.
	 * 
	 * @return
	 */
	public float getMaxWater() {
		float returnValue = 0;
		for (int i = 0; i < playerCharacters.size; ++i) {
			GameCharacter character = playerCharacters.get(i);
			if (character.isActive()) {
				returnValue += character.stats().getRace().getMaxWater();
			}
		}
		return returnValue;
	}

	/**
	 * Returns the average level of members of this group.
	 * 
	 * If includeInactive is false, only active members
	 * will be included in the calculation.
	 * 
	 * @param includeInactive
	 * @return
	 */
	@Override
	public int getAverageLevel(boolean includeInactive) {
		float levelTotal = 0;
		float characterTotal = 0;
		for (GameCharacter character : playerCharacters) {
			if (includeInactive || character.isActive()) {
				++characterTotal;
				levelTotal += character.stats().getLevel();
			}
		}
		return Math.round(levelTotal / characterTotal);
	}
	
	/**
	 * Returns true if any selected member of the group can perform the supplied
	 * action.
	 * 
	 * @param actionClass
	 * @return
	 */
	public boolean canPerformAction(Class<? extends Action> actionClass) {
		for (int i = 0; i < getSelected().size; ++i) {
			if (!getSelected().get(i).canPerformAction(actionClass)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void moveTo(int x, int y) {
		if (gameState.getCurrentMap().isWorldMap()) {
			AbstractGameCharacter groupGO = getGroupGameObject();
			Path path = groupGO.getMap().findPath(groupGO, x, y);
			if (path.getLength() == 0) {
				return;
			}
			groupGO.addAction(MoveToAction.class, path);
			return;
		}
		super.moveTo(x, y);
	}
	
	@Override
	public boolean hasActiveAction(Array<Class<? extends Action>> actionClasses) {
		if (gameState.getCurrentMap().isWorldMap()) {
			return getGroupGameObject().hasActiveAction(actionClasses);
		}
		return super.hasActiveAction(actionClasses);
	}
	
	/**
	 * Adds the supplied action to the group leader, or, 
	 * if the leader cannot perform it, to the first selected member who can,
	 * and supplies the targetGO as a parameter to the action.
	 * 
	 * All other selected group members are issued a MoveTo action
	 * to a tile near the targetThing according to the current formation.
	 * 
	 * @param actionClass
	 * @param targetThing
	 */
	@Override
	public void addAction(Class<? extends Action> actionClass, PositionedThing targetThing) {
		if (gameState.getCurrentMap().isWorldMap()) {
			getGroupGameObject().addAction(actionClass, targetThing);
			return;
		}
		if (getSelected().size > 0) {
			GameCharacter leader = getGroupLeader();
			
			if (!leader.canPerformAction(actionClass)) {
				leader = null;
				for (int i = 0; i < getSelected().size; ++i) {
					if (getSelected().get(i).canPerformAction(actionClass)) {
						leader = getSelected().get(i);
						break;
					}
				}
			}
			if (leader != null) {
				Action action = leader.addAction(actionClass, targetThing);
				// for disarm trap actions, nobody but the leader will move, since it can be dangerous
				if (action instanceof MoveToAction && !(action instanceof DisarmTrapAction)) {
					Path leaderPath = ((MoveToAction) action).getCurrentPath();
					if (leaderPath != null && leaderPath.getLength() > 0) {
						Step step = leaderPath.getLastStep();
						moveMembersExceptLeaderTo(leader, step.getX(), step.getY(), leaderPath);
					}
				}
			}
		}
	}
	
	protected void moveMembersExceptLeaderTo(GameCharacter leader, float x, float y, Path leaderPath) {
		moveMembersExceptLeaderTo(leader, selectedCharacters, x, y, leaderPath);
	}
	
	public void toggleDetectTraps() {
		Array<GameCharacter> selected = getSelected();
		boolean newState = !isAnySelectedPCDetectingTraps();
		
		for (int i = 0; i < selected.size; ++i) {
			selected.get(i).setIsDetectingTraps(newState);
		}
	}
	
	/**
	 * Depending on the current map will either break camp
	 * or send all members of the player character group to sleep.
	 * 
	 */
	public void sleepOrCamp() {
		UIManager.closeMutuallyExclusiveScreens();
		if (gameState.getCurrentMap().isWorldMap() && Configuration.isSurvivalEnabled()) {
			askAndDisplayCamp();
		} else {
			askAndSleep();
		}
	}
	
	private void askAndSleep() {
		final int sleepDuration = Configuration.getSleepDuration();
		if (sleepDuration <= 0) {
			return;
		}
		
		if (gameState.getCurrentMap().isCombatMap() || GameState.isCombatInProgress()) {
			Log.logLocalized("cannotSleepDuringCombat", LogType.SURVIVAL);
			return;
		}
		
		if (GameState.isAnySelectedPCSneaking()) {
			Log.logLocalized("cannotSleepDuringStealth", LogType.SURVIVAL);
			return;
		}
		
		gameState.pauseGame();
		UIManager.displayConfirmation(
				Strings.getString(SurvivalManager.STRING_TABLE, "sleepQuestion"), 
				Strings.getString(SurvivalManager.STRING_TABLE, "sleepConfirmation", sleepDuration),
				new OkCancelCallback<Void>() {
					@Override
					public void onOk(Void nada) {
						sleep(sleepDuration);
					}
					@Override
					public void onCancel() { 
						gameState.unpauseGame(); 
					}
				}
		);
	}
	
	/**
	 * Will send all members of the group to sleep for the supplied duration,
	 * fast forwarding the game time.
	 * 
	 * Ambushes will have a possibility of occurring.
	 * 
	 * @param sleepDuration
	 */
	public void sleep(final float sleepDuration) {
		sleep(sleepDuration, null);
	}
	
	/**
	 * Will send all members of the group to sleep for the supplied duration,
	 * fast forwarding the game time.
	 * 
	 * Ambushes will have a possibility of occurring.
	 * 
	 * The callback, if supplied, will be called if the sleep ends in any way.
	 * 
	 * @param sleepDuration
	 * @param callback
	 */
	public void sleep(final float sleepDuration, final BasicCallback callback) {
		Array<GameCharacter> characters = getPlayerCharacters();
		for (int i = 0; i < characters.size; ++i) {
			GameCharacter character = characters.get(i);
			character.removeAllVerbActions();
			character.setIsAsleep(true);
		}
		ProgressDialogSettings settings = new ProgressDialogSettings(Strings.getString(SurvivalManager.STRING_TABLE, "Sleeping"), sleepDuration, true);
		gameState.fastForwardTimeBy(settings, new FastForwardCallback() {
			@Override
			public void onFinished() {
				slept(sleepDuration);
			}
			@Override
			public void onCancelled(float timePassed) {
				slept(timePassed);
			}
			@Override
			public boolean onInterrupted(InterruptReason reason, float timePassed) {
				slept(timePassed);
				if (reason == InterruptReason.AMBUSH) {
					UIManager.hideCampPanel();
				}
				return true;
			}
			private void slept(float duration) {
				Array<GameCharacter> characters= getPlayerCharacters();
				for (int i = 0; i <  characters.size; ++i) {
					characters.get(i).hasSlept(duration);	
				}
				if (!UIManager.isCampOpen()) {
					gameState.unpauseGame();
				}
				if (callback != null) {
					callback.callback();
				}
			}
		}, true);
	}
	
	private void askAndDisplayCamp() {
		gameState.pauseGame();
		UIManager.displayConfirmation(
				Strings.getString(SurvivalManager.STRING_TABLE, "breakCampQuestion"),
				Strings.getString(SurvivalManager.STRING_TABLE, "breakCampConfirmation", Configuration.getBreakingCampDuration()), 
				new OkCancelCallback<Void>() {
					@Override
					public void onOk(Void nada) {
						getGroupGameObject().removeAllVerbActions();
						ProgressDialogSettings settings = new ProgressDialogSettings(Strings.getString(SurvivalManager.STRING_TABLE, "BreakingCamp"), Configuration.getBreakingCampDuration(), false);
						gameState.fastForwardTimeBy(settings, new SurvivalFastForwardCallback() {
							@Override
							public void onFinished() {
								UIManager.displayCampPanel();
							}
						}, false); 
						
					}
					@Override
					public void onCancel() { 
						gameState.unpauseGame(); 
					}
				}
		);
	}
	
	/**
	 * Sets selected members of this group as sneaking or not.
	 *
	 * @param value
	 * @return
	 */
	public void setStealth(boolean value) {
		for (int i = 0; i < selectedCharacters.size; ++i) {
			selectedCharacters.get(i).setIsSneaking(value);
		}
	}
	
	/**
	 * Returns true if any currently selected player character is sneaking.
	 * @return
	 */
	public boolean isAnySelectedPCSneaking() {  
		for (int i = 0; i < selectedCharacters.size; ++i) {
			if (selectedCharacters.get(i).isSneaking()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if any currently selected player character is detecting traps.
	 * @return
	 */
	public boolean isAnySelectedPCDetectingTraps() {  
		for (int i = 0; i < selectedCharacters.size; ++i) {
			if (selectedCharacters.get(i).isDetectingTraps()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets a character created during the start game process
	 * that has the specified role. Will return null if no such 
	 * character can be found.
	 */
	public GameCharacter getCreatedCharacter(Role role) {
		for (GameCharacter character : createdCharacters) {
			if (CoreUtil.equals(role, character.getRole())) {
				return character;
			}
		}
		return null;
	}
	
	/**
	 * Adds the supplied character into the list of characters 
	 * that were created during the start game process.
	 * 
	 * @param character
	 */
	public void addCreatedCharacter(GameCharacter character) {
		createdCharacters.add(character);
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		for (GameCharacter pc : playerCharacters) {
			pc.gatherAssets(assetStore);
		}
		junkBag.gatherAssets(assetStore);
	}
	
	/**
	 * Unloads this player character group, unloading all assets and
	 * clearing all members.
	 * 
	 * This should be called when a game is loaded.
	 */
	public void unload() {
		clearAssetReferences();
		playerCharacters.clear();
		getMembers().clear();
		nonPlayerCharacters.clear();
		selectedCharacters.clear();
		createdCharacters.clear();
		tempRemoved.clear();
		characterGroupGameObject = null;
		junkBag.clear();
	}
	
	@Override
	public void clearAssetReferences() {
		for (GameCharacter pc : getMembers()) {
			pc.clearAssetReferences();
		}
	}

	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		junkBag.writeToXML(writer);
		variables.writeToXML(writer);
		writeMembers(writer, XML_SELECTED, selectedCharacters);
		writeMembers(writer, XML_PC_MEMBERS, playerCharacters);
		writeMembers(writer, XML_NPC_MEMBERS, nonPlayerCharacters);
		writeMembers(writer, XML_TEMP_REMOVED, tempRemoved);
		writeMembers(writer, XML_CREATED, createdCharacters);
	}
	
	private void writeMembers(XmlWriter writer, String elementName, Iterable<GameCharacter> iterable) throws IOException {
		writer.element(elementName);
		for (GameCharacter pc : iterable) {
			writer.element(XML_MEMBER).attribute(XMLUtil.XML_ATTRIBUTE_ID, pc.getInternalId()).pop();
		}
		writer.pop();
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		
		junkBag = new PlayerCharacterGroupInventory(this);
		junkBag.loadFromXML(root);
		
		variables.clear();
		variables.loadFromXML(root);
		
		Element membersElement = root.getChildByName(XML_TEMP_REMOVED);
		for (int i = 0; i <membersElement.getChildCount(); ++i) {
			String id = membersElement.getChild(i).getAttribute(XMLUtil.XML_ATTRIBUTE_ID);
			tempRemoved.add((GameCharacter)GameState.getGameObjectByInternalId(id));
		}
		loadCharactersIntoArray(root.getChildByName(XML_PC_MEMBERS), playerCharacters);
		loadCharactersIntoArray(root.getChildByName(XML_NPC_MEMBERS), nonPlayerCharacters);
		loadCharactersIntoArray(root.getChildByName(XML_SELECTED), selectedCharacters);
		loadCharactersIntoArray(root.getChildByName(XML_CREATED), createdCharacters);
		
		for (GameCharacter selectedMember : selectedCharacters) {
			((GameCharacter)selectedMember).setSelected(true);
		}
		
		for (int i = 0; i < getMembers().size; ++i) {
			getMembers().get(i).resetCharacterCircleColor();
		}
	}
	
	private void loadCharactersIntoArray(Element membersElement, Array<GameCharacter> array) {
		for (int i = 0; i <membersElement.getChildCount(); ++i) {
			String id = membersElement.getChild(i).getAttribute(XMLUtil.XML_ATTRIBUTE_ID);
			array.add((GameCharacter)GameState.getGameObjectByInternalId(id));
		}
	}
	
	@Override
	public int getLevel() {
		return getAverageLevel(false);
	}
}