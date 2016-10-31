package mg.fishchicken.gamelogic.locations;

import groovy.lang.Binding;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.audio.AudioContainer;
import mg.fishchicken.audio.AudioOriginator;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Music;
import mg.fishchicken.audio.Sound;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.VariableContainer;
import mg.fishchicken.core.actions.Action;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.SaveablePolygon;
import mg.fishchicken.gamestate.Variables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class GameLocation implements VariableContainer, XMLLoadable, XMLSaveable, AssetContainer, AudioOriginator,
		AudioContainer, ThingWithId {

	public static final String XML_PROPERTIES = "properties";
	public static final String XML_ON_ENTRY = "onEntry";
	public static final String XML_ON_EXIT = "onExit";
	public static final String XML_COMBAT_MAPS = "combatMaps";
	public static final String XML_ENCOUNTERS = "encounters";
	public static final String XML_ACTIVITY_MODIFIERS = "activityModifiers";
	public static final String GROOVY_CHARACTER = "character";
	public static final String GROOVY_LOCATION = "location";

	protected static final String ON_EXIT = "onexit";
	protected static final String AMBIENT = "ambient";
	protected static final String ON_ENTRY = "onentry";
	protected static final String COMBAT = "combat";
	protected static final String CAMP = "camp";

	public enum TerrainActivity {
		HUNTING, SWIMMING, CLIMBING, WATER_SEARCHING {
			@Override
			public String xmlName() {
				return "waterSearching";
			}
		},
		SNEAKING;

		public String xmlName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	protected static GameState gameState;

	public static void setGameState(GameState gameState) {
		GameLocation.gameState = gameState;
	}

	private String s_id, s_type, s_internalId;
	private int s_dangerousness;
	private Faction s_ownerFaction;
	private boolean s_modifyVolume;
	private boolean s_stopSoundsOnExit;
	private ObjectMap<TerrainActivity, Integer> activityModifiers;
	protected SaveablePolygon s_polygon;
	private Rectangle boundingRectangle;

	private Action onEntryAction, onExitAction;
	private Condition onEntryCondition, onExitCondition;

	private Array<CharacterGroup> randomEncounterGroups;
	protected ObjectMap<String, Array<AudioTrack<?>>> soundTracks, musicTracks;
	private Variables variables;
	private Array<String> combatMaps;

	private GameMap map;
	private float audioStateTimeSounds, audioStateTimeMusic;

	/**
	 * Empty constructor for game loading.
	 */
	public GameLocation() {
		s_dangerousness = 0;
		s_modifyVolume = true;
		s_stopSoundsOnExit = true;
		audioStateTimeSounds = 0;
		audioStateTimeMusic = 0;
		onEntryAction = null;
		onEntryCondition = null;
		activityModifiers = new ObjectMap<TerrainActivity, Integer>();
		randomEncounterGroups = new Array<CharacterGroup>();
		variables = new Variables();
		combatMaps = new Array<String>();
		soundTracks = new ObjectMap<String, Array<AudioTrack<?>>>();
		musicTracks = new ObjectMap<String, Array<AudioTrack<?>>>();
		soundTracks.put(ON_EXIT, new Array<AudioTrack<?>>());
		soundTracks.put(AMBIENT, new Array<AudioTrack<?>>());
		soundTracks.put(ON_ENTRY, new Array<AudioTrack<?>>());
		soundTracks.put(COMBAT, new Array<AudioTrack<?>>());
		soundTracks.put(CAMP, new Array<AudioTrack<?>>());
		musicTracks.put(ON_EXIT, new Array<AudioTrack<?>>());
		musicTracks.put(AMBIENT, new Array<AudioTrack<?>>());
		musicTracks.put(ON_ENTRY, new Array<AudioTrack<?>>());
		musicTracks.put(COMBAT, new Array<AudioTrack<?>>());
		musicTracks.put(CAMP, new Array<AudioTrack<?>>());
	}

	public GameLocation(String id, String type, SaveablePolygon polygon) {
		this();
		if (polygon != null) {
			setPolygon(polygon);
		}
		this.s_id = id.toLowerCase(Locale.ENGLISH);
		this.s_internalId = (type + "#" + this.getClass().getSimpleName() + gameState.getNextId())
				.toLowerCase(Locale.ENGLISH);
		this.s_type = type;
		gameState.addLocation(this);
	}

	public String getId() {
		return s_id;
	}

	public String getInternalId() {
		return s_internalId;
	}

	public String getType() {
		return s_type;
	}

	public String getCombatMapId() {
		return combatMaps.random();
	}

	public GameMap getMap() {
		return map;
	}

	public void setMap(GameMap map) {
		if (this.map != null) {
			this.map.removeLocation(this);
		}
		this.map = map;
		if (map != null) {
			map.addLocation(this);
		}
	}

	public float getX() {
		return s_polygon.getX();
	}

	public float getY() {
		return s_polygon.getY();
	}

	public float getWidth() {
		return boundingRectangle.width;
	}

	public float getHeight() {
		return boundingRectangle.height;
	}

	protected void setPolygon(SaveablePolygon polygon) {
		this.s_polygon = polygon;
		this.boundingRectangle = polygon.getBoundingRectangle();
	}

	/**
	 * Returns true if this location contains the supplied map coordinates.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(float x, float y) {
		if (!boundingRectangle.contains(x, y)) {
			return false;
		}
		return s_polygon.contains(x, y);
	}

	@Override
	public Variables variables() {
		return variables;
	}

	public Array<AudioTrack<?>> getCampMusic() {
		return musicTracks.get(CAMP);
	}

	public void onEntry(AbstractGameCharacter character) {
		if (character.belongsToPlayerFaction()) {
			for (AudioTrack<?> track : soundTracks.get(ON_ENTRY)) {
				track.playIfRollSuccessfull(this);
			}
			AudioTrack<?> track = musicTracks.get(ON_ENTRY).random();
			if (track != null) {
				track.playIfRollSuccessfull(this);
			}
			if (onEntryAction != null) {
				Binding context = new Binding();
				context.setVariable(Condition.PARAM_ENTERING_CHARACTER, character);
				context.setVariable(Condition.PARAM_INITIAL_OBJECT, this);
				if (onEntryCondition == null || onEntryCondition.execute(this, context)) {
					onEntryAction.execute(this, context);
				}
			}
		}
		Faction myFaction = getOwnerFaction();
		Faction characterFaction = character.getFaction();
		if (myFaction != Faction.NO_FACTION && characterFaction.getDispositionTowards(myFaction) >= 0
				&& character.getMap() != null && character.getMap().isWorldMap()) {
			gameState.getCrimeManager().spreadCrimeInfo(characterFaction, myFaction);
		}
	}

	public void onExit(AbstractGameCharacter character) {
		if (character.belongsToPlayerFaction()) {
			for (AudioTrack<?> track : soundTracks.get(ON_EXIT)) {
				track.playIfRollSuccessfull(this);
			}
			AudioTrack<?> track = musicTracks.get(ON_EXIT).random();
			if (track != null) {
				track.playIfRollSuccessfull(this);
			}
			if (onExitAction != null) {
				Binding context = new Binding();
				context.setVariable(Condition.PARAM_ENTERING_CHARACTER, character);
				context.setVariable(Condition.PARAM_INITIAL_OBJECT, this);
				if (onExitCondition == null || onExitCondition.execute(this, context)) {
					onExitAction.execute(this, context);
				}
			}
			
			if (s_stopSoundsOnExit && !isOccupiedByPC()) {
				stopSoundsOnExit();
			}
		}
	}
	
	protected boolean isOccupiedByPC() {
		if (getMap().isWorldMap()) {
			Position pos = GameState.getPlayerCharacterGroup().getGroupGameObject().position();
			return contains(pos.getX(), pos.getY());
		} else {
			Array<GameCharacter> pcs = GameState.getPlayerCharacterGroup().getMembers();
			for (int i = 0; i < pcs.size; ++i) {
				GameCharacter pc = pcs.get(i);
				Position pos = pc.position();
				if (pc.isActive() && contains(pos.getX(), pos.getY())) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected void stopSoundsOnExit() {
		for (AudioTrack<?> track : soundTracks.get(AMBIENT)) {
			track.stop();
		}
		for (AudioTrack<?> track : soundTracks.get(ON_ENTRY)) {
			track.stop();
		}
		for (AudioTrack<?> track : soundTracks.get(COMBAT)) {
			track.stop();
		}
		for (AudioTrack<?> track : soundTracks.get(CAMP)) {
			track.stop();
		}
	}

	public void update(float delta, Camera camera) {
		if ((soundTracks.size > 0 || musicTracks.size > 0) && isOccupiedByPC()) {
			audioStateTimeSounds += delta;

			if (audioStateTimeSounds > Configuration.getAudioUpdateIntervalSounds()) {
				audioStateTimeSounds = 0;
				AudioTrack<?> track = soundTracks.get(AMBIENT).random();
				if (track != null) {
					track.playIfRollSuccessfull(this);
				}
			}
			if (!GameState.isCombatInProgress()) {
				audioStateTimeMusic += delta;
				if (audioStateTimeMusic > Configuration.getAudioUpdateIntervalMusic()) {
					audioStateTimeMusic = 0;
					if (!Music.isPlayingMusic()) {
						AudioTrack<?> track = musicTracks.get(AMBIENT).random();
						if (track != null) {
							track.playIfRollSuccessfull(this);
						}
					}
				}
			} else {
				audioStateTimeMusic = 0;
				if (!Music.isPlayingMusic()) {
					AudioTrack<?> track = musicTracks.get(COMBAT).random();
					if (track != null) {
						track.play();
					}
				}
			}
		}
		for (Array<AudioTrack<?>> sounds : soundTracks.values()) {
			for (AudioTrack<?> track : sounds) {
				track.update(delta);
			}
		}
		for (Array<AudioTrack<?>> sounds : musicTracks.values()) {
			for (AudioTrack<?> track : sounds) {
				track.update(delta);
			}
		}
	}

	public Array<CharacterGroup> getRandomEncounterGroups() {
		return randomEncounterGroups;
	}

	public int getActivityModifier(TerrainActivity activity) {
		Integer returnValue = activityModifiers.get(activity);
		return returnValue != null ? returnValue : 0;
	}

	@Override
	public void addTrack(AudioTrack<?> track, String type) {
		if (track instanceof Sound) {
			soundTracks.get(type).add((Sound) track);
		} else if (track instanceof Music) {
			musicTracks.get(type).add((Music) track);
		}
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
		for (Array<AudioTrack<?>> sounds : soundTracks.values()) {
			for (AudioTrack<?> track : sounds) {
				track.gatherAssets(assetStore);
			}
		}
		for (Array<AudioTrack<?>> sounds : musicTracks.values()) {
			for (AudioTrack<?> track : sounds) {
				track.gatherAssets(assetStore);
			}
		}
	}

	@Override
	public boolean alreadyVisited() {
		return GameState.getPlayerCharacterGroup().visitedLocation(this);
	}

	@Override
	public float getSoundRadius() {
		return Math.min(getWidth() / 2, getHeight() / 2);
	}

	@Override
	public Vector2 getSoundOrigin() {
		// we assume the sound originates in the middle of the GL
		return new Vector2((getX() + getWidth() / 2), (getY() + getHeight() / 2));
	}

	@Override
	public boolean shouldModifyVolume() {
		return s_modifyVolume;
	}

	@Override
	public float getDistanceToPlayer() {
		Vector2 origin = getSoundOrigin();
		return GameState.getPlayerCharacterGroup().getDistanceToTheNearestMember(origin.x, origin.y, getMap());
	}

	public Faction getOwnerFaction() {
		return s_ownerFaction == null ? Faction.NO_FACTION : s_ownerFaction;
	}

	public void setOwnerFaction(Faction ownerFaction) {
		s_ownerFaction = ownerFaction;
	}

	/**
	 * Gets the dangerousness (in percentage points) of this location.
	 * 
	 * This modifies the chance of a random encounter.
	 * 
	 * @return
	 */
	public int getDangerousness() {
		return s_dangerousness;
	}

	/**
	 * Sets the dangerousness (in percentage points) of this location.
	 * 
	 * This modifies the chance of a random encounter.
	 * 
	 * @return
	 */
	public void setDangerousness(int dangerousness) {
		s_dangerousness = dangerousness;
	}

	@Override
	public void clearAssetReferences() {
		for (Array<AudioTrack<?>> sounds : soundTracks.values()) {
			for (AudioTrack<?> track : sounds) {
				track.clearAssetReferences();
			}
		}
		for (Array<AudioTrack<?>> sounds : musicTracks.values()) {
			for (AudioTrack<?> track : sounds) {
				track.clearAssetReferences();
			}
		}
	}

	@Override
	public void loadFromXML(FileHandle locationFile) throws IOException {
		loadFromXMLNoInit(locationFile);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle locationFile) throws IOException {
		// if there is no xml, we just create an empty location and we are done
		if (!locationFile.exists()) {
			return;
		}
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(locationFile);
		XMLUtil.handleImports(this, locationFile, root);
		loadFromXML(root);
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		Element properties = root.getChildByName(XML_PROPERTIES);
		XMLUtil.readPrimitiveMembers(this, properties);
		if (s_polygon != null) {
			boundingRectangle = s_polygon.getBoundingRectangle();
		}

		variables.loadFromXML(root);
		XMLUtil.readAudio(this, root);

		Element triggersElement = root.getChildByName(XMLUtil.XML_TRIGGERS);

		if (triggersElement != null) {
			Element onEntryTrigger = triggersElement.getChildByName(XML_ON_ENTRY);
			if (onEntryTrigger != null) {
				if (onEntryTrigger.getChildByName(XMLUtil.XML_CONDITION) != null) {
					onEntryCondition = Condition.getCondition(onEntryTrigger.getChildByName(XMLUtil.XML_CONDITION).getChild(0));
				}
				onEntryAction = Action.getAction(onEntryTrigger.getChildByName(XMLUtil.XML_ACTION));
			}
			Element onExitTrigger = triggersElement.getChildByName(XML_ON_EXIT);
			if (onExitTrigger != null) {
				if (onExitTrigger.getChildByName(XMLUtil.XML_CONDITION) != null) {
					onExitCondition = Condition.getCondition(onEntryTrigger.getChildByName(XMLUtil.XML_CONDITION).getChild(0));
				}
				onExitAction = Action.getAction(onExitTrigger.getChildByName(XMLUtil.XML_ACTION));
			}
		}

		Element combatMapsElement = root.getChildByName(XML_COMBAT_MAPS);
		if (combatMapsElement != null) {
			String[] combatMapsStrings = combatMapsElement.getText().split(",");
			for (String mapId : combatMapsStrings) {
				combatMaps.add(mapId.trim());
			}
		}

		Element encountersElement = root.getChildByName(XML_ENCOUNTERS);
		if (encountersElement != null) {
			String[] encountersStrings = encountersElement.getText().split(",");
			for (String groupId : encountersStrings) {
				CharacterGroup encounter = new CharacterGroup(Gdx.files.internal(Configuration.getFolderGroups()
						+ groupId + ".xml"));
				encounter.setShouldBeSaved(false);
				randomEncounterGroups.add(encounter);
			}
		}

		Element modifiers = root.getChildByName(XML_ACTIVITY_MODIFIERS);
		if (modifiers != null) {
			for (TerrainActivity activity : TerrainActivity.values()) {
				activityModifiers.put(activity, modifiers.getInt(activity.xmlName(), 0));
			}
		}
	}

	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_PROPERTIES);
		XMLUtil.writePrimitives(this, writer);
		writer.pop();
		variables.writeToXML(writer);
	}
}
