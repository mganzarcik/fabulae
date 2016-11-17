package mg.fishchicken.gamelogic.combat;

import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamelogic.characters.Brain;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.GameObjectPosition;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.tweening.PositionedThingTweenAccessor;

public class CombatManager {
	
	public static final String STRING_TABLE = "combat."+Strings.RESOURCE_FILE_EXTENSION;
	private static Array<GameCharacter> tempArray = new Array<GameCharacter>();
	
	private enum Side { PLAYER, COMPUTER };
	
	private Side activeSide;
	private Array<GameCharacter> aiCharacterQueue = new Array<GameCharacter>();
	private GameCharacter activeAICharacter;
	private boolean combatInProgress = false;
	private Array<GameCharacter> combatants = new Array<GameCharacter>();
	private InitiativeComparator initiativeComparator = new InitiativeComparator();
	private int endTurnCounter = 0;
	private GameState gameState;
	private PositionArray occupiedTiles = new PositionArray();
	private PositionArray characterMoved = new PositionArray();
	
	public CombatManager(GameState gameState) {
		this.gameState = gameState;
	}
	
	public void startCombat() {
		if (combatInProgress) {
			return;
		}
		Log.logLocalized("combatStarted", LogType.COMBAT);
		combatInProgress = true;
		endTurnCounter = 0;
		
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		
		combatants.clear();
		
		gameState.getCurrentMap().getAllGameObjects(combatants, GameCharacter.class);

		occupiedTiles.clear();
		characterMoved.clear();
		
		for (GameCharacter character : combatants) {
			fixCharacterPosition(character);
			character.onCombatStart();
			character.onTurnStart();
		}
		
		if (pcg.getGroupLeader() != null) {
			pcg.selectOnlyMember(pcg.getGroupLeader());
		} else {
			pcg.selectOnlyMember(pcg.getPlayerCharacters().first());
		}
		
		activeSide = Side.PLAYER;
	}
	
	private void fixCharacterPosition(GameCharacter character) {
		GameMap map = character.getMap();
		GameObjectPosition position = character.position();
		int oldX = position.tile().getX();
		int oldY = position.tile().getY();
		int newX = oldX;
		int newY = oldY;
		
		// if we are the only ones standing on our tile and it is not blocked, just stay there
		// otherwise move to the closest free tile
		if (map.blocked(character, newX, newY, false, true)) {
			// first try to use our previous tile and if available, slide there
			Tile prevTile = position.prevTile();
			if (!map.blocked(character, prevTile.getX(), prevTile.getY(), false, true) && !occupiedTiles.contains(prevTile)) {
				newX = prevTile.getX();
				newY = prevTile.getY();
			} else {
				// otherwise just look for the nearest available tile
				Vector2 dest = map.getUnblockedTile(newX, newY, 3, character, true, occupiedTiles);
				if (dest != null) {
					newX = (int)dest.x;
					newY = (int)dest.y;
				}
			}
		}
		occupiedTiles.add(newX, newY);
		map.notifyGOTileChanged(character, newX, newY, oldX, oldY);
		Tween.to(character, PositionedThingTweenAccessor.XY, 0.5f).target(newX, newY).ease(TweenEquations.easeOutQuint).start(map.getTweenManager(true));
	}
	
	public void update(float deltaTime) {
		if (!combatInProgress || isPlayersTurn()) {
			return;
		}
		
		if (activeAICharacter == null) {
			if (aiCharacterQueue.size == 0) {
				switchToNextSide();
			}
			if (aiCharacterQueue.size > 0) {
				activeAICharacter = aiCharacterQueue.pop();
				activeAICharacter.resetCharacterCircleColor();
			}
		}
		
		if (activeAICharacter != null) {
			if (!activeAICharacter.isActive()) {
				activeAICharacter = null;
			} else {
				Brain brain = activeAICharacter.brain();
				brain.updateCombatAction(deltaTime);
				if (brain.finishedTurn()) {
					activeAICharacter = null;
				}
			}
		}
	}
	
	public boolean isPlayersTurn() {
		return Side.PLAYER.equals(activeSide) && combatInProgress;
	}
	
	public void switchToNextSide() {
		switch (activeSide) {
			case COMPUTER: activeSide = Side.PLAYER; break;
			case PLAYER: activeSide = Side.COMPUTER; break;
		}
		
		// move the game time forward
		int passedSeconds = Configuration.getCombatTurnDurationInGameSeconds();
		gameState.updateGameTimeWithGameSeconds(passedSeconds);
		Array<GameCharacter> playerControlledCharacters = GameState.getPlayerCharacterGroup().getMembers();
		for (int i = 0; i < playerControlledCharacters.size; ++i) {
			playerControlledCharacters.get(i).updateSurvival(passedSeconds/3600f);
		}
		
		aiCharacterQueue.clear();
		if (!isPlayersTurn()) {
			Iterator<GameCharacter> iterator = combatants.iterator();
			while (iterator.hasNext()) {
				GameCharacter character = iterator.next();
				if (character.isActive() && !character.belongsToPlayerFaction()) {
					character.onTurnStart();
					aiCharacterQueue.add(character);
				}
			}

			aiCharacterQueue.sort(initiativeComparator);
		} else {
			Log.logLocalized(STRING_TABLE, "newTurnStart", LogType.INFO);
			if (GameState.getPlayerCharacterGroup().getSelectedSize() < 1) {
				GameState.getPlayerCharacterGroup().selectMember(playerControlledCharacters.first());
			}
			
			// make sure all the hostile NPCs that can see any PC at the start of their turn
			// memorize the PCs position, since the player can move them out of sight
			// before they get registered if the next tile they would move to
			// would be out of sight of all enemies
			for (int i = 0; i < playerControlledCharacters.size; ++i) {
				GameCharacter pc = playerControlledCharacters.get(i);
				pc.broadcastMyPositionToEnemiesInSight();
				pc.onTurnStart();
			}
			
			if (canEndCombat(true)) {
				++endTurnCounter;
			} else {
				endTurnCounter = 0;
			}
			
			if (endTurnCounter >= Configuration.getAutomaticCombatEnd() && canEndCombat(false)) {
				endTurnCounter = 0;
				gameState.endCombat();
				return;
			}
		}
	}

	public void endCombat() {
		if (!combatInProgress) {
			return;
		}
		Log.logLocalized("combatFinished", LogType.COMBAT);
		combatInProgress = false;
		
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		Array<GameCharacter> playerCharacters = pcg.getPlayerCharacters();
		int playerCharacterCount = 0;
		for (GameCharacter character : playerCharacters) {
			if (character.isActive()) {
				++ playerCharacterCount;
			}
		}
		
		Iterator<GameCharacter> iterator = combatants.iterator();
		
		float pcgAverageLevel = pcg.getAverageLevel(false);
		int experiencePool = 0;
		
		while (iterator.hasNext()) {
			GameCharacter character = iterator.next();
			if (!character.isActive()) {
				GameObject killer = character.getKiller();
				if ((killer instanceof GameCharacter) && pcg.containsCharacter((GameCharacter)killer)) {
					int expValue = character.stats().getExperienceValue();
					int level = character.stats().getLevel();
					int expGain = (int) (expValue + ((level - pcgAverageLevel) * (expValue / (float)level)));
					if (expGain > 0) {
						experiencePool += expGain; 
					}
				}
			}
		}
		
		int expToAward = playerCharacterCount > 0 ? experiencePool / playerCharacterCount : 0;
		
		for (GameCharacter character : playerCharacters) {
			if (character.isActive()) {
				character.stats().giveExperience(expToAward);
			}
		}
		
		for (GameCharacter character : combatants) {
			character.onCombatEnd();
		}
		Music.stopPlayingMusic();
	}
	
	public boolean canPlayerEndCombat() {
		return canEndCombat(true) && endTurnCounter > 0;
	}
	
	private boolean canEndCombat(boolean ignoreSleeping) {
		return (!GameState.getPlayerCharacterGroup().canSeeEnemy() && !gameState.getCurrentMap().isCombatMap()) || noMoreEnemies(gameState.getCurrentMap(), ignoreSleeping);
	}
	
	private static boolean noMoreEnemies(GameMap map, boolean ignoreSleeping) {
		tempArray.clear();
		map.getAllGameObjects(tempArray, GameCharacter.class);
		
		for (GameCharacter character : tempArray) {
			if (character.isHostileTowardsPlayer() && (!ignoreSleeping || !character.isAsleep())) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isCombatInProgress() {
		return combatInProgress;
	}
}
