package mg.fishchicken.gamelogic.locations;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.locations.transitions.Transition;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class CombatGameMap extends GameMap {

	private static final String LOCATION_COMPUTER_START = "ComputerStart";
	private static final String LOCATION_PLAYER_START = "PlayerStart";
	private static final String LOCATION_ESCAPE_ZONE = "EscapeZone";
	
	private Vector2 playerStartCoordinates = null; // lazy init
	private Vector2 aiStartCoordinates = null; // lazy init
	private CharacterGroup enemyGroup;
	private String fromMap;
	private int fromX, fromY, escapeX, escapeY;
	private Transition escapeTransition;
	
	public CombatGameMap() {
		super();
	}
	
	public CombatGameMap(String id) {
		super(id);
	}
	
	@Override
	public boolean isCombatMap() {
		return true;
	}
	
	@Override
	public boolean isWorldMap() {
		return false;
	}
	
	public void init(CombatMapInitializationData data) {
		this.enemyGroup = data.enemyGroup;
		this.fromMap = data.fromMapId;
		this.fromX = data.fromX;
		this.fromY = data.fromY;
		this.escapeX = data.escapeX;
		this.escapeY = data.escapeY;
	}
	
	@Override
	public Vector2 getStartCoordinates() {
		if (playerStartCoordinates == null) {
			playerStartCoordinates = getRandomCoordinatesFromLocation(LOCATION_PLAYER_START);
		}
		return playerStartCoordinates;
	}
	
	private Vector2 getAICoordinates() {
		if (aiStartCoordinates == null) {
			aiStartCoordinates = getRandomCoordinatesFromLocation(LOCATION_COMPUTER_START);
		}
		return aiStartCoordinates;
	}
	
	private Vector2 getRandomCoordinatesFromLocation(String locationId) {
		GameLocation desiredLocation = findLocation(locationId);
		
		int x = (int) MathUtils.random(desiredLocation.getX(), desiredLocation.getX()+desiredLocation.getWidth());
		int y = (int) MathUtils.random(desiredLocation.getY(), desiredLocation.getY()+desiredLocation.getHeight());
		return new Vector2(x, y);
	}
	
	private GameLocation findLocation(String locationId) {
		GameLocation desiredLocation = null;
		for (GameLocation location : locations) {
			if (locationId.equalsIgnoreCase(location.getId())) {
				desiredLocation = location;
				break;
			}
		}
		if (desiredLocation == null) {
			throw new GdxRuntimeException(locationId+" location not found for combat map "+getId() +".");
		}
		return desiredLocation;
	}
	
	@Override
	public Orientation getStartOrientation() {
		Vector2 startCoordinates = getStartCoordinates();
		Vector2 aiCoordinates = getAICoordinates();
		return Orientation.calculateOrientationToTarget(isIsometric(), startCoordinates.x, startCoordinates.y, aiCoordinates.x, aiCoordinates.y);
	}
	
	private void determineEscapeZone() {
		GameLocation escapeZone = findLocation(LOCATION_ESCAPE_ZONE);
		Polygon polygon = new Polygon(new float[] {0, 0, escapeZone.getWidth(), 0, escapeZone.getWidth(), escapeZone.getHeight(), 0, escapeZone.getHeight()});
		polygon.setPosition(escapeZone.getX(), escapeZone.getY());
		Transition victoryTransition = new Transition(this, fromMap, fromX, fromY, polygon);
		escapeTransition = new Transition(this, fromMap, escapeX, escapeY, polygon);
		for (int x = (int)escapeZone.getX(); x < escapeZone.getX()+escapeZone.getWidth(); ++x) {
			for (int y = (int)escapeZone.getY(); y < escapeZone.getY()+escapeZone.getHeight(); ++y) {
				transitionTiles.put(new Vector2(x, y), victoryTransition);
			}
		}
	}
	
	@Override
	public Transition getTransitionAt(int x, int y) {
		Transition transition = super.getTransitionAt(x, y);
		if (transition != null && enemyGroup.hasActiveMembers()) {
			return escapeTransition;
		}
		return transition;
	}
	
	@Override
	public void onLoad() {
		if (enemyGroup == null) {
			throw new GdxRuntimeException("Enemy group not defined for combat map "+getId() +".");
		}
		enemyGroup.setMap(this, true);
		Array<GameCharacter> unpositionedChars = enemyGroup.setPosition(getAICoordinates(), getStartOrientation().getOpposite(), this);
		for (GameCharacter character : unpositionedChars) {
			Log.log("Character {0} could not be positioned, killing silently.", LogType.ERROR, character.getInternalId());
			character.setActive(false);
			character.setMap(null);
		}
		determineEscapeZone();
		super.onLoad();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		playerStartCoordinates = null;
		aiStartCoordinates = null;
	}
	
	@Override
	public void currentMapWillChange() {
		super.currentMapWillChange();
		// remove surviving enemies from the map
		if (enemyGroup != null) {
			CharacterGroup.setMap(enemyGroup, null, true);
			enemyGroup.removeInactiveMembers();
		}
		
		// mark everything left on the map as not saveable and remove it
		// except for player characters
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		Array<GameObject> gameObjectCopy = new Array<GameObject>(this.gameObjects);
		for (GameObject go : gameObjectCopy) {
			if (go instanceof GameCharacter && pcg.containsCharacter((GameCharacter)go)) {
				continue;
			}
			go.setShouldBeSaved(false);
			removeGameObject(go);
		}
		
		// removing everything from the map
		//removeEverything();
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		for (GameCharacter character : enemyGroup.getMembers()) {
			character.gatherAssets(assetStore);
		}
		
		super.gatherAssets(assetStore);
	}
	
	public static class CombatMapInitializationData {
		public String fromMapId;
		public CharacterGroup enemyGroup;
		public int fromX, fromY, escapeX, escapeY;
		
		public CombatMapInitializationData(String fromMapId, CharacterGroup enemyGroup, Tile from, int escapeX, int escapeY) {
			this.enemyGroup = enemyGroup;
			this.fromMapId = fromMapId;
			this.fromX = from.getX();
			this.fromY = from.getY();
			this.escapeX = escapeX;
			this.escapeY = escapeY;
		}
		
		public CombatMapInitializationData(String fromMapId, CharacterGroup enemyGroup, Tile from, Tile escape) {
			this(fromMapId, enemyGroup, from, escape.getX(), escape.getY());
		}
	}
}
