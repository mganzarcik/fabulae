package mg.fishchicken.gamelogic.characters.groups;

import java.io.IOException;

import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.Path.Step;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class CharacterGroup implements XMLSaveable, XMLLoadable, SkillCheckModifier {
	
	public static final String XML_MEMBERS = "members";
	public static final String XML_MEMBER = "member";
	public static final String XML_GROUP_LEADER = "leader";
	
	private Array<GameCharacter> groupMembers;
	private int s_level;
	private String s_name;
	private String s_description;
	private Formation formation;
	private PositionArray tempPositionArray;
	private Path tempPath;
	
	public CharacterGroup(FileHandle file) throws IOException {
		this();
		loadFromXML(file);
	}
	
	/**
	 * Creates a new character group that will be a copy of the supplied group.
	 * 
	 * All members will be created as new objects of the same type as the members
	 * of the original group.
	 * 
	 * @param groupToCopy
	 * @throws IOException
	 */
	public CharacterGroup(CharacterGroup groupToCopy) throws IOException {
		this.formation = new Formation(groupToCopy.formation);
		this.s_level = groupToCopy.s_level;
		this.s_description = groupToCopy.s_description;
		this.s_name = groupToCopy.s_name;
		tempPositionArray = new PositionArray();
		tempPath = new Path();
		groupMembers = new Array<GameCharacter>();
		for (GameCharacter member : groupToCopy.groupMembers) {
			groupMembers.add(GameCharacter.loadCharacter(member.getId(), Gdx.files
					.internal(Configuration.getFolderCharacters()
							+ member.getType() + ".xml"), member.getMap()));
		}
	}
	
	protected CharacterGroup() {
		s_level = -1;
		groupMembers = new Array<GameCharacter>();
		formation = new Formation();
		tempPositionArray = new PositionArray();
		tempPath = new Path();
	}
	
	/**
	 * Adds the supplied character to the group.
	 * @param newMember
	 * @return true if the character was added, false if it was not because it was already a member
	 * 
	 */
	public boolean addMember(GameCharacter newMember) {
		if (groupMembers.contains(newMember, false)) {
			return false;
		}
		groupMembers.add(newMember);
		return true;
	}
	
	public void removeMember(GameCharacter memberToRemove) {
		groupMembers.removeValue(memberToRemove, false);
	}
	
	public int getSize() {
		return groupMembers.size;
	}
	
	public Formation formation() {
		return formation;
	}

	/** 
	 * Sets the position of the leader to the specified
	 * position and all other members will be positioned
	 * according to the current formation.
	 * 
	 * @param position
	 * @return an array of characters for which the position could not be set because no free tile was found on the map
	 */
	public Array<GameCharacter> setPosition(Vector2 position, Orientation orientation, GameMap map) {
		Array<GameCharacter> unpositioned = new Array<GameCharacter>();
		formation.setOrientation(orientation);
		Vector2 target = MathUtil.getVector2();
		Array<GameCharacter> members = getMembers();
		PositionArray occupied = new PositionArray();
		
		for (int i = 0; i <  members.size; ++i) {
			GameCharacter member =  members.get(i);
			Tile formationPosition = formation.getOffset(groupMembers.indexOf(member, false), map.isIsometric());
			target.set(formationPosition.getX(), formationPosition.getY());
			target.add(position.x, position.y);
			tempPositionArray.clear();
			map.getUnblockedTiles((int)target.x, (int)target.y, 5, member, true, tempPositionArray);
			
			int minStepsFound = -1;
			target.set(-1, -1);
			for (int j = 0; j < tempPositionArray.size(); ++j) {
				int x = tempPositionArray.getX(j);
				int y = tempPositionArray.getY(j);
				if (occupied.contains(x, y)) {
					continue;
				}
				map.getPathFinder().findPath(member, x, y, (int)position.x, (int)position.y, tempPath);
				int length = tempPath.getLength();
				if (length > 0) {
					if (minStepsFound > length || minStepsFound < 0) {
						target.set(x, y);
						minStepsFound= length;
					}
				}
				if (minStepsFound == 1) {
					break;
				}
			}
			if (target.x < 0 || target.y < 0) {
				unpositioned.add(member);
				continue;
			} 
			occupied.add((int)target.x, (int)target.y);
			member.position().set(target);
				
			if (orientation != null) {
				member.setOrientation(orientation);
			}
		}
		MathUtil.freeVector2(target);
		return unpositioned;
	}
	
	public int getLevel() {
		return s_level;
	}

	public void setLevel(int level) {
		this.s_level = level;
	}

	/**
	 * Removes all inactive members from the group.
	 */
	public void removeInactiveMembers() {
		Array<GameCharacter> charsToRemove = new Array<GameCharacter>();
		for (GameCharacter character : getMembers()) {
				if (!character.isActive()) {
					charsToRemove.add(character);
			}
		}
		
		for (GameCharacter character : charsToRemove) {
			removeMember(character);
		}
	}
	
	/**
	 * Returns true if at least one member of the group
	 * is active.
	 * 
	 * @return
	 */
	public boolean hasActiveMembers() {
		for (GameCharacter character : getMembers()) {
			if (character.isActive()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if any member of this groupcurrently has any active (non-paused)
	 * action of any of the supplied types.
	 * 
	 * This DOES take into effect class inheritance.
	 * 
	 * @param actionClasses
	 * @return
	 */
	public boolean hasActiveAction(Array<Class<? extends Action>> actionClasses) {
		for (GameCharacter member : getMembers()) {
			if (member.hasActiveAction(actionClasses)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds the supplied action to the group leader
	 * and supplies the targetGO as a parameter to the action.
	 * 
	 * All other group members are issued a MoveTo action
	 * to a tile near the targetGO according to the current formation.
	 * 
	 * @param actionClass
	 * @param targetGO
	 */
	public void addAction(Class<? extends Action> actionClass, PositionedThing targetThing) {
		if (groupMembers.size > 0) {
			GameCharacter leader = getGroupLeader();
			if (leader != null) {
				Action action = leader.addAction(actionClass, targetThing);
				if (action instanceof MoveToAction) {
					Path leaderPath = ((MoveToAction) action).getCurrentPath();
					if (leaderPath != null && leaderPath.getLength() > 0) {
						Step step = leaderPath.getLastStep();
						moveMembersExceptLeaderTo(leader, step.getX(), step.getY(), leaderPath);
					}
				}
			}
		}
	}

	/**
	 * Moves the leader of this group to the supplied coordinate
	 * and all other members to the coordinates near it according to the current
	 * formation of the group.
	 * 
	 * @param x
	 * @param y
	 */
	public void moveTo(int x, int y) {
		if (groupMembers.size > 0) {
			GameCharacter leader =  getGroupLeader();
			MoveToAction action = leader.addAction(MoveToAction.class, x, y);
			Path leaderPath = action.getCurrentPath();
			if (leaderPath != null && leaderPath.getLength() > 0) {
				if (leader.isMemberOfPlayerGroup()) {
					leader.getAudioProfile().getTrack(AudioProfile.MOVE).playIfRollSuccessfull(leader);
				}
				Step step = leaderPath.getLastStep();
				moveMembersExceptLeaderTo(leader, step.getX(), step.getY(), leaderPath);
			}
		}
	}
	
	/**
	 * Returns all members of this group, including dead / inactive
	 * members.
	 * 
	 * @return
	 */
	public Array<GameCharacter> getMembers() {
		return groupMembers;
	}
	
	/**
	 * Gets the group leader. This is the first member of the group.
	 * @return
	 */
	public GameCharacter getGroupLeader() {
		if (groupMembers.size > 0) {
			return groupMembers.first();
		}
		return null;
	}
	
	protected int getFormationIndex(GameCharacter character) {
		return groupMembers.indexOf(character, false);
	}
	
	protected void moveMembersExceptLeaderTo(GameCharacter leader, float x, float y, Path leaderPath) {
		moveMembersExceptLeaderTo(leader, groupMembers, x, y, leaderPath);
	}
	
	protected void moveMembersExceptLeaderTo(GameCharacter leader, Array<GameCharacter> groupMembers, float x, float y, Path leaderPath) {
		if (groupMembers.size < 1) {
			return;
		}
		formation.setOrientation(Orientation.calculateOrientationToTarget(leader.getMap().isIsometric(),leader.position().getX(), leader.position().getY(), x, y));
		Vector2 target = MathUtil.getVector2(); 
		
		for (GameCharacter member : groupMembers) {

			if(member.equals(getGroupLeader())) {
				continue;
			}
			
			target.set(x,y);
			
			if (!member.getMap().isWorldMap()) {
				Tile formationPosition = formation.getOffset(getFormationIndex(member), member.getMap().isIsometric());
				if (formationPosition != null) {
					target.add(formationPosition.getX(), formationPosition.getY());
				}
			} 
			
			//  if we are already on the desired target, we do nothing
			if (member.position().tile().equals((int)target.x, (int)target.y)) {
				continue;
			}
			Path path = member.getMap().findPath(member, target.x,
					target.y);
			// if the path to the formation's destination cannot be found, 
			// but we have the leader's path, use that to determine our destination
			if (path.getLength() == 0 && leaderPath != null) {
				int stepIndex = leaderPath.getLength()-1-getFormationIndex(member);
				if (leaderPath.getLength() > stepIndex && stepIndex >= 0) {
					Step step = leaderPath.getStep(stepIndex);
					path = member.getMap().findPath(member, step.getX(),
							step.getY());
				}
			}
			
			// if there is no path (for example destination is blocked)
			// this character wont move
			if (path.getLength() == 0) {
				continue;
			}
			member.addAction(MoveToAction.class, path);
		}
		MathUtil.freeVector2(target);
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XMLUtil.XML_PROPERTIES);
		XMLUtil.writePrimitives(this, writer);
		writer.pop();
		
		writer.element(XML_MEMBERS);
		for (GameCharacter pc : groupMembers) {
			writer.element(XML_MEMBER).attribute(XMLUtil.XML_ATTRIBUTE_ID, pc.getInternalId()).attribute(XMLUtil.XML_ATTRIBUTE_TYPE, pc.getType()).pop();
		}
		writer.pop();
		
		formation.writeToXML(writer);
		
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
		XMLUtil.readPrimitiveMembers(this, root.getChildByName(XMLUtil.XML_PROPERTIES));
		
		Element membersElement = root.getChildByName(XML_MEMBERS);
		for (int i = 0; i <membersElement.getChildCount(); ++i) {
			String id = membersElement.getChild(i).getAttribute(XMLUtil.XML_ATTRIBUTE_ID, null);
			String type = membersElement.getChild(i).getAttribute(XMLUtil.XML_ATTRIBUTE_TYPE);
			groupMembers.add(GameCharacter.getCharacter(type, id));
		}
		
		formation = new Formation(root);
	}
	
	/**
	 * This will also move all members into the new map.
	 * 
	 * It will also stop any actions the members had.
	 * @param map
	 */
	public void setMap(GameMap map, boolean removeActions) {
		setMap(this, map, removeActions);
	}

	/**
	 * This will also move all members into the new map.
	 * 
	 * It will also stop any actions the members had.
	 * @param map
	 */
	public static void setMap(CharacterGroup group, GameMap map, boolean removeActions) {
		for (int i = 0; i < group.getMembers().size; ++i) {
			GameCharacter member =  group.getMembers().get(i);
			member.setMap(map);
			if (removeActions) {
				member.removeAllActions();
			}
		}
	}
	
	/**
	 * Returns the average level of memebrs of this group.
	 * 
	 * If includeInactive is false, only active members
	 * will be included in the calculation.
	 * 
	 * @param includeInactive
	 * @return
	 */
	public int getAverageLevel(boolean includeInactive) {
		float levelTotal = 0;
		float characterTotal = 0;
		for (GameCharacter character : getMembers()) {
			if (includeInactive || character.isActive()) {
				++characterTotal;
				levelTotal += character.stats().getLevel();
			}
		}
		return Math.round(levelTotal / characterTotal);
	}

	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		if (Skill.SCOUTING.equals(skill)) {
			return (2 + skillUser.stats().getLevel() - getAverageLevel(false)) * 10;
		}
		return 0;
	}
	
	public GameCharacter getMemberWithHighestSkill(Skill skill) {
		GameCharacter returnValue = null;
		
		for (GameCharacter character : getMembers()) {
			if (returnValue == null
					|| character.stats().skills().getSkillRank(skill) > returnValue
							.stats().skills().getSkillRank(skill)) {
				returnValue = character;
			}
		}
		
		return returnValue;
	}

	public String getName() {
		return Strings.getString(s_name);
	}
	public String getDescription() {
		return Strings.getString(s_description);
	}
	
	/**
	 * Sets all members of this group as sneaking or not.
	 *
	 * @param value
	 * @return
	 */
	public void setStealth(boolean value) {
		Array<GameCharacter> members = getMembers();
		for (int i = 0; i < members.size; ++i) {
			members.get(i).setIsSneaking(value);
		}
	}
	
	/** 
	 * Tells all members of the group whether to be saved or not.
	 *  
	 * @param shouldBeSaved
	 */
	public void setShouldBeSaved(boolean shouldBeSaved) {
		for (GameCharacter member : groupMembers) {
			member.setShouldBeSaved(shouldBeSaved);
		}
		
	}
	
}
