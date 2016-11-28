package mg.fishchicken.core.configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Locale;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.groups.Formation;
import mg.fishchicken.gamelogic.inventory.ItemPile;
import mg.fishchicken.gamelogic.inventory.items.Armor.ArmorClass;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;
import mg.fishchicken.graphics.particles.ParticleEffectDescriptor;
import mg.fishchicken.ui.configuration.LoadingScreens;

public final class Configuration implements XMLLoadable {

	public static final String FOLDER_MODULES = "modules/";
	public static final String FOLDER_USER_DATA = "userdata/";
	public static final String FOLDER_SAVEGAMES = "savegames/";
	public static final String FILE_LAST_MODULE = "module.hmm";

	public static final String XML_CHARACTER = "character";
	public static final String XML_ARMOR = "armor";
	public static final String XML_MAXRATIO = "maxRatio";
	public static final String XML_PENALTIES = "penalties";
	public static final String XML_START = "start";
	public static final String XML_GROUP = "group";
	public static final String XML_MENU = "menu";
	public static final String XML_MUSIC = "music";
	public static final String XML_MEMBERS = "members";
	public static final String XML_FORMATION = "formation";
	public static final String XML_KEY_BINDINGS = "keyBindings";
	public static final String XML_UI = "ui";
	public static final String XML_INDICATORS = "indicators";
	public static final String XML_DETECT = "detect";
	public static final String XML_SNEAK = "sneak";

	private static Configuration configuration;
	private static ModifierContainer EMPTY_MODIFIERS = new ArmorModifiers();

	// all of the below are default values that can be overridden in config.xml
	@XMLField(fieldPath = "folders.maps")
	private String folderMaps = "maps/";
	@XMLField(fieldPath = "folders.locations")
	private String folderLocations = "locations/";
	@XMLField(fieldPath = "folders.lights")
	private String folderLights = "lights/";
	@XMLField(fieldPath = "folders.characters")
	private String folderCharacters = "characters/";
	@XMLField(fieldPath = "folders.pcPortraits")
	private String folderPCPortraits = "characters/portraits/pc";
	@XMLField(fieldPath = "folders.audioProfiles")
	private String folderAudioProfiles = "characters/audioProfiles/";
	@XMLField(fieldPath = "folders.itemModels")
	private String folderItemModels = "items/models/";
	@XMLField(fieldPath = "folders.characterModels")
	private String folderCharacterModels = "characters/models/";
	@XMLField(fieldPath = "folders.groups")
	private String folderGroups = "characters/groups/";
	@XMLField(fieldPath = "folders.dialogues")
	private String folderDialogues = "dialogues/";
	@XMLField(fieldPath = "folders.chatter")
	private String folderChatter = folderCharacters + "chatter/";
	@XMLField(fieldPath = "folders.containers")
	private String folderContainers = "containers/";
	@XMLField(fieldPath = "folders.usables")
	private String folderUsables = "usables/";
	@XMLField(fieldPath = "folders.races")
	private String folderRaces = "races/";
	@XMLField(fieldPath = "folders.roles")
	private String folderRoles = "characters/roles/";
	@XMLField(fieldPath = "folders.weatherProfiles")
	private String folderWeatherProfiles = "weather/";
	@XMLField(fieldPath = "folders.quests")
	private String folderQuests = "quests/";
	@XMLField(fieldPath = "folders.perks")
	private String folderPerks = "perks/";
	@XMLField(fieldPath = "folders.spells")
	private String folderSpells = "spells/";
	@XMLField(fieldPath = "folders.traps")
	private String folderTraps = "traps/";
	@XMLField(fieldPath = "folders.aiScripts")
	private String folderAiScripts = "characters/aiscripts/";
	@XMLField(fieldPath = "compiledScripts")
	private String folderCompiledScripts = "compiledscripts/";
	@XMLField(fieldPath = "folders.factions")
	private String folderFactions = "factions/";
	@XMLField(fieldPath = "folders.projectiles")
	private String folderProjectiles = "projectiles/";
	@XMLField(fieldPath = "folders.effects")
	private String folderEffects = "effects/";
	@XMLField(fieldPath = "folders.storySequences")
	private String folderStorySequences = "storysequences/";
	@XMLField(fieldPath = "folders.itemGroups")
	private String folderItemGroups = "items/groups";
	@XMLField(fieldPath = "folders.items")
	private String folderItems = "items/";
	@XMLField(fieldPath = "folders.particles")
	private String folderParticles = "particles/";
	@XMLField(fieldPath = "folders.strings")
	private String folderStringResources = "strings/";
	@XMLField(fieldPath = "folders.ui")
	private String folderUI = "ui/";

	@XMLField(fieldPath = "files.experienceTable")
	private String fileExperienceTable = "experienceTable." + Strings.RESOURCE_FILE_EXTENSION;
	@XMLField(fieldPath = "files.calendar")
	private String fileCalendar = "calendar.xml";
	@XMLField(fieldPath = "files.survivalConfiguration")
	private String fileSurvivalConfiguration = "survival.xml";
	@XMLField(fieldPath = "files.characterCircleSprite")
	private String fileCharacterCircleSprite = "sprites/character_circle.png";
	@XMLField(fieldPath = "files.maps.isometric.gridTexture")
	private String fileIsometricMapGridTexture = "maps/grid_iso.png";
	@XMLField(fieldPath = "files.maps.isometric.transitionTexture")
	private String fileIsometricMapTransitionTexture = "maps/transition_iso.png";
	@XMLField(fieldPath = "files.maps.isometric.solidWhiteTileTexture")
	private String fileIsometricMapSolidWhiteTileTexture = "maps/solid_tile_iso.png";
	@XMLField(fieldPath = "files.maps.orthogonal.gridTexture")
	private String fileOrthogonalMapGridTexture = "maps/grid.png";
	@XMLField(fieldPath = "files.maps.orthogonal.transitionTexture")
	private String fileOrthogonalMapTransitionTexture = "maps/transition.png";
	@XMLField(fieldPath = "files.maps.orthogonal.solidWhiteTileTexture")
	private String fileOrthogonalMapSolidWhiteTileTexture = "maps/solid_tile.png";
	@XMLField(fieldPath = "files.itemPileFile")
	private String fileItemPile = "usables/ItemPile.xml";

	@XMLField(fieldPath = "audio.soundsUpdateInterval")
	private float audioSoundsUpdateInterval = 0.3f;
	@XMLField(fieldPath = "audio.musicUpdateInterval")
	private float audioMusicUpdateInterval = 2f;
	@XMLField(fieldPath = "audio.offScreenMaxDistance")
	private int audioOffScreenMaxDistance = 5;
	@XMLField(fieldPath = "audio.weather.interiorVolumeModifier")
	private float audioWeatherInteriourSoundVolumeModifier = 0.3f;
	@XMLField(fieldPath = "audio.weather.soundsUpdateInterval")
	private float audioWeatherSoundsUpdateInterval = 5f;

	@XMLField(fieldPath = "time.localTimeMultiplier")
	private int gameTimeMultiplierLocal = 60;
	@XMLField(fieldPath = "time.worldTimeMultiplier")
	private int gameTimeMultiplierWorld = 1200;
	@XMLField(fieldPath = "time.combatTurnDuration")
	private int combatDurationGameSeconds = 120;
	@XMLField(fieldPath = "time.globalGameObjectsUpdateInterval")
	private float globalGameObjectsUpdateInterval = 0.5f;
	@XMLField(fieldPath = "time.fastForwardStep")
	private float fastForwardStep = 0.2f;

	@XMLField(fieldPath = "weather.minimumUpdateInterval")
	private int weatherUpdateMin = 3600; // 1 hr
	@XMLField(fieldPath = "weather.maximumUpdateInterval")
	private int weatherUpdateMax = 10800; // 3 hrs
	@XMLField(fieldPath = "weather.snowTemperatureThreshold")
	private int weatherSnowTemperatureThreshold = 3;
	@XMLField(fieldPath = "weather.nightTemperatureModifier")
	private int weatherNightTemperatureModifier = -10;
	@XMLField(fieldPath = "weather.precipitationStartDuration")
	private int weatherPrecipitationStartDuration = 30;
	@XMLField(fieldPath = "weather.precipitationEndDuration")
	private int weatherPrecipitationEndDuration = 30;

	@XMLField(fieldPath = "lighting.ambientLightMax")
	private float ambientLightMax = 0.5f;
	@XMLField(fieldPath = "lighting.ambientLightMin")
	private float ambientLightMin = 0.1f;

	@XMLField(fieldPath = "ui.debugPanelEnabled")
	private boolean debugPanelEnabled = true;
	@XMLField(fieldPath = "ui.scrollAreaStartOffset")
	private int scrollAreaStartOffset = 10;
	@XMLField(fieldPath = "ui.maxMessagesInLog")
	private int maxMessagesInLog = 50;
	@XMLField(fieldPath = "ui.language")
	private Locale gameLocale = Locale.ENGLISH;
	@XMLField(fieldPath = "ui.chatterFadeTime")
	private float chatterFadeTime = 5;
	@XMLField(fieldPath = "ui.chatterWidth")
	private int chatterWidth = 300;
	@XMLField(fieldPath = "ui.selectionTolerance")
	private int selectionTolerance = 10;
	@XMLField(fieldPath = "ui.decimalFormat")
	private String decimalFormat = "#.##";
	@XMLField(fieldPath = "ui.cursors.default")
	private String cursorDefault = null;
	@XMLField(fieldPath = "ui.cursors.disarm")
	private String cursorDisarm = "ui/cursors/cursor_disarm.png";
	@XMLField(fieldPath = "ui.cursors.lockpick")
	private String cursorLockpick = "ui/cursors/cursor_lockpick.png";
	@XMLField(fieldPath = "ui.cursors.talkTo")
	private String cursorTalkTo = "ui/cursors/cursor_talk_to.png";
	@XMLField(fieldPath = "ui.cursors.attack")
	private String cursorAttack = "ui/cursors/cursor_attack.png";
	
	private ParticleEffectDescriptor detectTrapsIndicator = null;
	private ParticleEffectDescriptor sneakIndicator = null;

	@XMLField(fieldPath = "graphics.fogColor")
	private Color fogColor = new Color(0.7f, 0.7f, 0.7f, 1f);
	@XMLField(fieldPath = "graphics.selectionHighlightColor")
	private Color selectionHighlightColor = new Color(1f, 0.3f, 0.3f, 1f);
	@XMLField(fieldPath = "graphics.invisibleCharacterAlpha")
	private float invisibleCharacterAlpha = 0.5f;
	@XMLField(fieldPath = "graphics.tileSizeX")
	private float tileSizeX = 32f;
	@XMLField(fieldPath = "graphics.tileSizeY")
	private float tileSizeY = 32f;
	@XMLField(fieldPath = "graphics.mapScale")
	private float mapScale = 1f; // how much everything is magnified on the game
									// map - CURRENTLY DOES NOT WORK PROPERLY

	@XMLField(fieldPath = "character.maxCharactersInGroup")
	private int maxCharactersInGroup = 6;
	@XMLField(fieldPath = "character.localSightRadius")
	private int sightRadiusLocal = 20;
	@XMLField(fieldPath = "character.worldmapSightRadius")
	private int sightRadiusWorld = 5;
	@XMLField(fieldPath = "character.costs.openInventory")
	private int apCostInventoryOpen = 3;
	@XMLField(fieldPath = "character.costs.attack")
	private int apCostAttack = 5;
	@XMLField(fieldPath = "character.costs.useItem")
	private int apCostUseItem = 4;
	@XMLField(fieldPath = "character.costs.disarmTrap")
	private int apCostDisarmTrap = 4;
	@XMLField(fieldPath = "character.costs.open")
	private int apCostOpen = 4;
	@XMLField(fieldPath = "character.costs.pickUp")
	private int apCostPickUp = 4;
	@XMLField(fieldPath = "character.costs.move")
	private int apCostMove = 1;
	@XMLField(fieldPath = "character.maxSkillPointRank")
	private int maxBaseSkillRank = 5;
	@XMLField(fieldPath = "character.skillPointGainPerLevel")
	private float skillPointGainPerLevel = 2;
	@XMLField(fieldPath = "character.skillIncreasesPerLevel")
	private int skillIncreasesPerLevel = 1;
	@XMLField(fieldPath = "character.perkPointGainPerLevel")
	private float perkPointGainPerLevel = 0.5f;
	@XMLField(fieldPath = "character.stealth.tilesPerCheck")
	private int tilesPerStealthCheck = 10;
	@XMLField(fieldPath = "character.stealth.modifiers.darkness")
	private int darknessStealthModifier = 20;
	@XMLField(fieldPath = "character.stealth.modifiers.pickUpItem")
	private int pickUpItemStealthModifier = 0;
	@XMLField(fieldPath = "character.stealth.modifiers.pickLock")
	private int picLockStealthModifier = -15;
	@XMLField(fieldPath = "character.stealth.modifiers.disarmTrap")
	private int disarmTrapStealthModifier = -15;
	@XMLField(fieldPath = "character.stealth.modifiers.castSpell")
	private int castSpellStealthModifier = -30;
	@XMLField(fieldPath = "character.stealth.modifiers.usePerk")
	private int usePerkStealthModifier = -30;
	@XMLField(fieldPath = "character.stealth.modifiers.useObject")
	private int useObjectStealthModifier = -30;
	@XMLField(fieldPath = "character.stealth.modifiers.talkTo")
	private int talkToStealthModifier = -45;
	private ObjectMap<ArmorClass, Float> armorClassRatio = new ObjectMap<ArmorClass, Float>();
	private ObjectMap<ArmorClass, IntMap<ModifierContainer>> armorClassModifiers = new ObjectMap<ArmorClass, IntMap<ModifierContainer>>();

	@XMLField(fieldPath = "combat.automaticCombatEnd")
	private int automaticCombatEnd = 3;
	@XMLField(fieldPath = "combat.d"
			+ "efaultCombatStartedChatterId")
	private String defaultCombatStartedChatterId = "combatStarted";
	@XMLField(fieldPath = "combat.cthBonusSide")
	private int cthBonusSide = 10;
	@XMLField(fieldPath = "combat.cthBonusBack")
	private int cthBonusBack = 20;
	@XMLField(fieldPath = "combat.disengagementMovementPenalty")
	private int disengagementMovementPenalty = 2;

	@XMLField(fieldPath = "worldmap.speedModifier")
	private float woldMapSpeedModifier = 0.5f;
	@XMLField(fieldPath = "worldmap.randomEncountersCooldown")
	private int randomEncountersCooldown = 4;

	@XMLField(fieldPath = "survival.enabled")
	private boolean survivalEnabled = true;
	@XMLField(fieldPath = "survival.randomEncountersBaseChance")
	private int randomEncountersBaseChance = 10;
	@XMLField(fieldPath = "survival.randomEncountersLevelTolerance")
	private int randomEncountersLevelTolerance = 2;
	@XMLField(fieldPath = "survival.breakingCampDuration")
	private int breakingCampDuration = 1;
	@XMLField(fieldPath = "survival.packingCampDuration")
	private int packingCampDuration = 1;
	@XMLField(fieldPath = "survival.pauseOnBadChange")
	private boolean pauseOnBadChange = true;
	@XMLField(fieldPath = "survival.sleepDuration")
	private int sleepDuration = 8;

	@XMLField(fieldPath = "crime.theftFineMultiplier")
	private float theftFineMultiplier = 1.5f;
	@XMLField(fieldPath = "crime.theftDispositionPenalty")
	private int theftDispositionPenalty = 30;
	@XMLField(fieldPath = "crime.assaultBaseFine")
	private int assaultBaseFine = 500;
	@XMLField(fieldPath = "crime.assaultDispositionPenalty")
	private int assaultDispositionPenalty = 50;
	@XMLField(fieldPath = "crime.trespassBaseFine")
	private int trespassBaseFine = 200;
	@XMLField(fieldPath = "crime.trespassDispositionPenalty")
	private int trespassDispositionPenalty = 15;
	@XMLField(fieldPath = "crime.murderDispositionPenalty")
	private int murderDispositionPenalty = 150;
	@XMLField(fieldPath = "crime.defaultFineDialogueId")
	private String defaultFineDialogueId = "fineDialogue";
	@XMLField(fieldPath = "crime.defaultLawEnfoncerDialogueId")
	private String defaultLawEnfoncerDialogueId = "lawEnfoncerDialogue";
	@XMLField(fieldPath = "crime.defaultCrimeSpottedChatterId")
	private String defaultCrimeSpottedChatterId = "crimeSpotted";
	@XMLField(fieldPath = "crime.hostilityDuration")
	private int hostilityDuration = 8;

	@XMLField(fieldPath = "start.map")
	private String startMap;
	@XMLField(fieldPath = "start.group.numberOfCharactersToCreate")
	private int numberOfCharactersToCreate = 1;
	private Array<String> startGroupMembers = new Array<String>();
	private Formation startFormation;
	private Array<AudioTrack<?>> startMenuMusic;

	private boolean renderLOSDebug, renderLightsDebug;
	private AssetMap globalAssets = null;
	private String moduleFolder;
	private String moduleName;
	private GameOptions options;
	private LoadingScreens loadingScreensConfiguration;
	private ExperienceTable experienceTable;

	private Configuration(Files files) {
		configuration = this;
		loadOptions(files);
	}

	private Configuration(final Files files, final String moduleName) {
		if (configuration != null) {
			this.options = configuration.options;
		} else {
			loadOptions(files);
		}
		configuration = this;
		this.moduleName = moduleName;
		this.moduleFolder = FOLDER_MODULES + moduleName + "/";
		String file = moduleFolder + "config.xml";
		try {
			loadFromXML(files.internal(file));
			loadingScreensConfiguration = new LoadingScreens(files.internal(Configuration.getFolderUI()
					+ "loadingScreens.xml"));
		} catch (final IOException e) {
			throw new GdxRuntimeException("Cannot read configuration file " + file + ", aborting.", e);
		}
	}

	private void loadOptions(Files files) {
		String file = FOLDER_USER_DATA + "config.xml";
		try {
			FileHandle userOptions = files.local(file);
			options = userOptions.exists() ? new GameOptions(userOptions) : new GameOptions();
		} catch (final IOException e) {
			throw new GdxRuntimeException("Cannot read configuration file " + file + ", aborting.", e);
		}
	}

	public static void writeOptions(Files files) {
		if (configuration == null || configuration.options == null) {
			return;
		}
		String file = FOLDER_USER_DATA + "config.xml";
		FileHandle userOptions = files.local(file);
		OutputStream outputStream = userOptions.write(false);
		Writer writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
		try {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			XmlWriter xml = new XmlWriter(writer);
			xml.element("gameOptions");
			XMLUtil.writePrimitives(configuration.options, xml, true);
			xml.element(XML_KEY_BINDINGS);
			for (KeyBindings binding : KeyBindings.values()) {
				xml.element(binding.name().toLowerCase(Locale.ENGLISH));
				xml.text(binding.getKeys().toString(","));
				xml.pop();
			}
			xml.pop();
			xml.pop();
			xml.flush();
		} catch (final IOException e) {
			throw new GdxRuntimeException("Cannot write configuration file " + file + ", aborting.", e);
		} finally {
			StreamUtils.closeQuietly(writer);
		}
	}

	/**
	 * This will create a new configuration with only the user options loaded.
	 * 
	 * Everything else will have default values.
	 */
	public static void createConfiguration(Files files) {
		new Configuration(files);
	}

	/**
	 * This will load the specified module into the configuration. Any values
	 * not specified in the module will revert to defaults. Any user options
	 * will be kept.
	 * 
	 * @param moduleName
	 */
	public static void loadModule(Files files, String moduleName) {
		new Configuration(files, moduleName);
	}

	/**
	 * Returns the name of the currently loaded module.
	 * 
	 * @return
	 */
	public static String getCurrentModuleName() {
		return configuration.moduleName;
	}

	public static String addModulePath(String path) {
		if (path == null) {
			return null;
		}
		return path.startsWith(configuration.moduleFolder) ? path : configuration.moduleFolder + path;
	}

	/**
	 * Returns true if the game should be fullscreen.
	 */
	public static boolean isFullscreen() {
		return configuration.options.s_fullscreen;
	}

	/**
	 * Sets whether the game should be fullscreen. Does not actually change the
	 * mode, just update the config!
	 */
	public static void setFullscreen(boolean value) {
		configuration.options.s_fullscreen = value;
	}

	/**
	 * Width of the game screen in pixels (x resolution)
	 * 
	 * @return
	 */
	public static int getScreenWidth() {
		return configuration.options.s_screenWidth;
	}

	/**
	 * Sets the screen width. This will only update the configuration, it will
	 * not automatically change the resolution as well!
	 * 
	 * @param value
	 */
	public static void setScreenWidth(int value) {
		configuration.options.s_screenWidth = value;
	}

	/**
	 * Height of the game screen in pixels (y resolution)
	 * 
	 * @return
	 */
	public static int getScreenHeight() {
		return configuration.options.s_screenHeight;
	}

	/**
	 * Sets the screen height. This will only update the configuration, it will
	 * not automatically change the resolution as well!
	 * 
	 * @param value
	 */
	public static void setScreenHeight(int value) {
		configuration.options.s_screenHeight = value;
	}

	/**
	 * Gets the alpha value invisible characters should have.
	 * 
	 * @return
	 */
	public static float getInvisibleCharacterAlpha() {
		return configuration.invisibleCharacterAlpha;
	}

	/**
	 * Returns the ratio by which all interior weather sounds are quieter.
	 * 
	 * @return
	 */
	public static float getInteriorSoundVolumeModifier() {
		return configuration.audioWeatherInteriourSoundVolumeModifier;
	}

	/**
	 * Gets the number of seconds it takes rain / snow to start falling fully.
	 *
	 * @return
	 */
	public static int getPrecipitationStartLenght() {
		return configuration.weatherPrecipitationStartDuration;
	}

	/**
	 * Gets the number of seconds it takes rain / snow to stop falling.
	 *
	 * @return
	 */
	public static int getPrecipitationEndLenght() {
		return configuration.weatherPrecipitationEndDuration;
	}

	/**
	 * Gets the temperature modifier that should be applied to all temperatures
	 * during night time.
	 * 
	 * @return
	 */
	public static int getNightTemperatureModifier() {
		return configuration.weatherNightTemperatureModifier;
	}

	/**
	 * Gets the temperature under which snow should occur instead of rain.
	 * 
	 * @return
	 */
	public static int getSnowTemperatureThreshold() {
		return configuration.weatherSnowTemperatureThreshold;
	}

	/**
	 * Gets the minimum number in seconds during which a weather update should
	 * occur. This is game-time seconds, not real-world seconds.
	 * 
	 * @return
	 */
	public static int getWeatherUpdateMin() {
		return configuration.weatherUpdateMin;
	}

	/**
	 * Gets the maximum number in seconds during which a weather update should
	 * occur. This is game-time seconds, not real-world seconds.
	 * 
	 * @return
	 */
	public static int getWeatherUpdateMax() {
		return configuration.weatherUpdateMax;
	}

	/**
	 * Returns the maximum number in messages that get displayed in the game
	 * message log.
	 *
	 * @return
	 */
	public static int getMaxMessagesInLog() {
		return configuration.maxMessagesInLog;
	}

	/**
	 * Returns the number of pixels the cursor needs to be dragged in order for
	 * the game to start character selection and stop performing normal touch up
	 * events.
	 */
	public static int getSelectionTolerance() {
		return configuration.selectionTolerance;
	}

	/**
	 * Returns the format to be used when displaying decimal numbers.
	 * 
	 * See {@link DecimalFormat} for pattern syntax.
	 * 
	 * @return
	 */
	public static String getDecimalFormat() {
		return configuration.decimalFormat;
	}

	/**
	 * Gets the path to the file that contains the image of the default mouse
	 * cursor. If null or empty, the system cursor will be used.
	 * 
	 * @return
	 */
	public static String getDefaultCursorPath() {
		return StringUtil.nullOrEmptyString(configuration.cursorDefault) ? null : configuration.moduleFolder
				+ configuration.cursorDefault;
	}

	/**
	 * Gets the path to the file that contains the image of the lockpick mouse
	 * cursor. If null or empty, the system cursor will be used.
	 * 
	 * @return
	 */
	public static String getLockpickCursorPath() {
		return StringUtil.nullOrEmptyString(configuration.cursorLockpick) ? null : configuration.moduleFolder
				+ configuration.cursorLockpick;
	}

	/**
	 * Gets the path to the file that contains the image of the disarm traps
	 * mouse cursor. If null or empty, the system cursor will be used.
	 * 
	 * @return
	 */
	public static String getDisarmCursorPath() {
		return StringUtil.nullOrEmptyString(configuration.cursorDisarm) ? null : configuration.moduleFolder
				+ configuration.cursorDisarm;
	}
	
	/**
	 * Gets the path to the file that contains the image of the talk to
	 * mouse cursor. If null or empty, the system cursor will be used.
	 * 
	 * @return
	 */
	public static String getTalkToCursorPath() {
		return StringUtil.nullOrEmptyString(configuration.cursorTalkTo) ? null : configuration.moduleFolder
				+ configuration.cursorTalkTo;
	}
	
	/**
	 * Gets the path to the file that contains the image of the attack
	 * mouse cursor. If null or empty, the system cursor will be used.
	 * 
	 * @return
	 */
	public static String getAttackCursorPath() {
		return StringUtil.nullOrEmptyString(configuration.cursorAttack) ? null : configuration.moduleFolder
				+ configuration.cursorAttack;
	}
	
	/**
	 * Returns the particle effect that should be used when a character is detecting traps.
	 * @return
	 */
	public static ParticleEffectDescriptor getDetectTrapsParticleEffect() {
		return configuration.detectTrapsIndicator;
	}
	
	/**
	 * Returns the particle effect that should be used when a character is sneaking.
	 * @return
	 */
	public static ParticleEffectDescriptor getSneakParticleEffect() {
		return configuration.sneakIndicator;
	}

	/**
	 * Returns the maximum base rank for a skill.
	 *
	 * @return
	 */
	public static int getMaxBaseSkillRank() {
		return configuration.maxBaseSkillRank;
	}

	/**
	 * Returns the number of times a skill can be increased each level
	 *
	 * @return
	 */
	public static int getSkillIncreasesPerLevel() {
		return configuration.skillIncreasesPerLevel;
	}

	/**
	 * Returns the number of perk points a character gains each level.
	 *
	 * @return
	 */
	public static float getPerkPointGainPerLevel() {
		return configuration.perkPointGainPerLevel;
	}

	/**
	 * Returns the number of tiles in sight radius a character can cross before
	 * he needs to make another stealth check.
	 *
	 * @return
	 */
	public static int getTilesPerStealthCheck() {
		return configuration.tilesPerStealthCheck;
	}

	/**
	 * Returns the modifier to stealth all characters receive when sneaking in
	 * darkness.
	 */
	public static int getDarknessStealthModifier() {
		return configuration.darknessStealthModifier;
	}

	/**
	 * Returns the modifier to stealth all characters receive when rolling a
	 * stealth check for picking up an item.
	 */
	public static int getPickUpItemStealthModifier() {
		return configuration.pickUpItemStealthModifier;
	}

	/**
	 * Returns the modifier to stealth all characters receive when rolling a
	 * stealth check for trying to pick a lock.
	 */
	public static int getPicLockStealthModifier() {
		return configuration.picLockStealthModifier;
	}

	/**
	 * Returns the modifier to stealth all characters receive when rolling a
	 * stealth check for trying to disarm a trap.
	 */
	public static int getDisarmTrapStealthModifier() {
		return configuration.disarmTrapStealthModifier;
	}

	/**
	 * Returns the modifier to stealth all characters receive when rolling a
	 * stealth check for casting a spell.
	 */
	public static int getCastSpellStealthModifier() {
		return configuration.castSpellStealthModifier;
	}

	/**
	 * Returns the modifier to stealth all characters receive when rolling a
	 * stealth check for using a perk.
	 */
	public static int getUsePerkStealthModifier() {
		return configuration.usePerkStealthModifier;
	}

	/**
	 * Returns the modifier to stealth all characters receive when rolling a
	 * stealth check for using an object on the map.
	 */
	public static int getUseObjectStealthModifier() {
		return configuration.useObjectStealthModifier;
	}

	/**
	 * Returns the modifier to stealth all characters receive when rolling a
	 * stealth check for talking to another character.
	 */
	public static int getTalkToStealthModifier() {
		return configuration.talkToStealthModifier;
	}

	/**
	 * Returns the number of skill points a character gains each level;
	 *
	 * @return
	 */
	public static float getSkillPointGainPerLevel() {
		return configuration.skillPointGainPerLevel;
	}

	/**
	 * Returns the number of seconds by which game time is advanced each turn.
	 *
	 * @return
	 */
	public static int getCombatTurnDurationInGameSeconds() {
		return configuration.combatDurationGameSeconds;
	}

	public static int getAPCostMove() {
		return configuration.apCostMove;
	}

	public static int getAPCostPickUp() {
		return configuration.apCostPickUp;
	}

	public static int getAPCostOpen() {
		return configuration.apCostOpen;
	}

	public static int getAPCostUseItem() {
		return configuration.apCostUseItem;
	}

	public static int getAPCostDisarmTrap() {
		return configuration.apCostDisarmTrap;
	}

	public static int getAPCostAttack() {
		return configuration.apCostAttack;
	}

	public static int getAPCostInventoryOpen() {
		return configuration.apCostInventoryOpen;
	}

	public static float getWorldMapSpeedMultiplier() {
		return configuration.woldMapSpeedModifier;
	}

	/**
	 * Gets the combat move speed multiplier. Speed of all character movement is
	 * multiplied by this number.
	 * 
	 * @return
	 */
	public static float getCombatSpeedMultiplier() {
		return configuration.options.s_combatMoveSpeed;
	}

	/**
	 * Sets the combat move speed multiplier. Speed of all character movement is
	 * multiplied by this number.
	 *
	 * @return
	 */
	public static void setCombatSpeedMultiplier(float value) {
		configuration.options.s_combatMoveSpeed = value;
	}

	/**
	 * Returns the maximum number of characters in the player's group.
	 */
	public static int getMaxCharactersInGroup() {
		return configuration.maxCharactersInGroup;
	}

	public static int getSightRadiusLocal() {
		return configuration.sightRadiusLocal;
	}

	public static int getSightRadiusWorld() {
		return configuration.sightRadiusWorld;
	}

	/**
	 * Returns the interval in seconds in which every global game object should
	 * be updated.
	 *
	 * @return
	 */
	public static float getGlobalGameObjectsUpdateInterval() {
		return configuration.globalGameObjectsUpdateInterval;
	}

	/**
	 * Returns the number of real world seconds the game world will by updated
	 * by each frame when fast forwarding time (like during sleep). The value
	 * should not be too high (not more than half a second), or weird bugs can
	 * start occurring.
	 *
	 * @return
	 */
	public static float getFastForwardStep() {
		return configuration.fastForwardStep;
	}

	public static Color getSelectionHighlightColor() {
		return configuration.selectionHighlightColor;
	}

	public static Color getFogColor() {
		return configuration.fogColor;
	}

	public static float getAmbientLightMin() {
		return configuration.ambientLightMin;
	}

	public static float getAmbientLightMax() {
		return configuration.ambientLightMax;
	}

	/**
	 * Returns how many game seconds is one real - world second when on world
	 * maps.
	 *
	 * @return
	 */
	public static int getWorldGameTimeMultiplier() {
		return configuration.gameTimeMultiplierWorld;
	}

	/**
	 * Returns how many game seconds is one real - world second when on local
	 * maps.
	 *
	 * @return
	 */
	public static int getLocalGameTimeMultiplier() {
		return configuration.gameTimeMultiplierLocal;
	}

	/**
	 * Returns the width of the chatter floating text in pixels.
	 *
	 * @return
	 */
	public static int getChatterWidth() {
		return configuration.chatterWidth;
	}

	/**
	 * Returns the number of seconds the chatter text stays on screen before it
	 * starts to fade.
	 *
	 * @return
	 */
	public static float getChatterFadeTime() {
		return configuration.chatterFadeTime;
	}

	/**
	 * Returns the distance in tiles that is the maximum an off-screen audio
	 * originator can be from the center of the rendered screen in order for any
	 * of its audio to be audible.
	 *
	 * @return
	 */
	public static int getAudioOffScreenMaxDistance() {
		return configuration.audioOffScreenMaxDistance;
	}

	/**
	 * Gets the interval in seconds in which each ambient track should check if
	 * it should play.
	 * 
	 * @return
	 */
	public static float getAudioUpdateIntervalSounds() {
		return configuration.audioSoundsUpdateInterval;
	}

	/**
	 * Gets the interval in seconds in which each ambient track should check if
	 * it should play.
	 * 
	 * @return
	 */
	public static float getAudioUpdateIntervalMusic() {
		return configuration.audioMusicUpdateInterval;
	}

	/**
	 * Gets the interval in seconds in which weather random tracks should check
	 * if they should play. This usually means thunder.
	 * 
	 * @return
	 */
	public static float getAudioUpdateIntervalWeather() {
		return configuration.audioWeatherSoundsUpdateInterval;
	}

	/**
	 * Gets the path to the maps folder.
	 *
	 * @return
	 */
	public static String getFolderMaps() {
		return configuration.moduleFolder + configuration.folderMaps;
	}

	/**
	 * Gets the path to the locations folder.
	 *
	 * @return
	 */
	public static String getFolderLocations() {
		return configuration.moduleFolder + configuration.folderLocations;
	}

	/**
	 * Gets the path to the lights folder.
	 *
	 * @return
	 */
	public static String getFolderLights() {
		return configuration.moduleFolder + configuration.folderLights;
	}

	/**
	 * Gets the path to the characters folder.
	 *
	 * @return
	 */
	public static String getFolderCharacters() {
		return configuration.moduleFolder + configuration.folderCharacters;
	}

	/**
	 * Gets the path to the player character portraits folder.
	 *
	 * @return
	 */
	public static String getFolderPCPortraits() {
		return configuration.moduleFolder + configuration.folderPCPortraits;
	}

	/**
	 * Gets the path to the audio profiles folder.
	 * 
	 * @return
	 */
	public static String getFolderAudioProfiles() {
		return configuration.moduleFolder + configuration.folderAudioProfiles;
	}

	/**
	 * Gets the path to the item models folder.
	 * 
	 * @return
	 */
	public static String getFolderItemModels() {
		return configuration.moduleFolder + configuration.folderItemModels;
	}
	
	/**
	 * Gets the path to the character models folder.
	 * 
	 * @return
	 */
	public static String getFolderCharacterModels() {
		return configuration.moduleFolder + configuration.folderCharacterModels;
	}

	/**
	 * Gets the path to the characters folder.
	 *
	 * @return
	 */
	public static String getFolderGroups() {
		return configuration.moduleFolder + configuration.folderGroups;
	}

	/**
	 * Gets the path to the dialogues folder.
	 *
	 * @return
	 */
	public static String getFolderDialogues() {
		return configuration.moduleFolder + configuration.folderDialogues;
	}

	public static String getFolderContainers() {
		return configuration.moduleFolder + configuration.folderContainers;
	}

	/**
	 * Gets the path to the usables folder.
	 * 
	 * @return
	 */
	public static String getFolderUsables() {
		return configuration.moduleFolder + configuration.folderUsables;
	}

	/**
	 * Gets the path to the calendar file.
	 *
	 * @return
	 */
	public static String getFileCalendar() {
		return configuration.moduleFolder + configuration.fileCalendar;
	}

	/**
	 * Gets the path to the survival configuration file.
	 *
	 * @return
	 */
	public static String getFileSurvivalConfiguration() {
		return configuration.moduleFolder + configuration.fileSurvivalConfiguration;
	}

	/**
	 * Gets the path to the projectiles folder.
	 *
	 * @return
	 */
	public static String getFolderProjectiles() {
		return configuration.moduleFolder + configuration.folderProjectiles;
	}

	/**
	 * Gets the path to the factions folder.
	 *
	 * @return
	 */
	public static String getFolderFactions() {
		return configuration.moduleFolder + configuration.folderFactions;
	}

	/**
	 * Gets the path to the compiled scripts folder.
	 *
	 * @return
	 */
	public static String getFolderCompiledScripts() {
		return configuration.moduleFolder + configuration.folderCompiledScripts;
	}
	
	/**
	 * Gets the path to the AI scripts folder.
	 *
	 * @return
	 */
	public static String getFolderAIScripts() {
		return configuration.moduleFolder + configuration.folderAiScripts;
	}

	/**
	 *
	 * Gets the path to the weather profiles folder.
	 *
	 * @return
	 */
	public static String getFolderWeatherProfiles() {
		return configuration.moduleFolder + configuration.folderWeatherProfiles;
	}

	/**
	 * Gets the path to the races folder.
	 *
	 * @return
	 */
	public static String getFolderRaces() {
		return configuration.moduleFolder + configuration.folderRaces;
	}

	/**
	 * Gets the path to the roles folder.
	 *
	 * @return
	 */
	public static String getFolderRoles() {
		return configuration.moduleFolder + configuration.folderRoles;
	}

	/**
	 * Gets the path to the quests folder.
	 *
	 * @return
	 */
	public static String getFolderQuests() {
		return configuration.moduleFolder + configuration.folderQuests;
	}

	/**
	 * Gets the path to the perks folder.
	 *
	 * @return
	 */
	public static String getFolderPerks() {
		return configuration.moduleFolder + configuration.folderPerks;
	}

	/**
	 * Gets the path to the traps folder.
	 *
	 * @return
	 */
	public static String getFolderTraps() {
		return configuration.moduleFolder + configuration.folderTraps;
	}

	/**
	 * Gets the path to the spells folder.
	 *
	 * @return
	 */
	public static String getFolderSpells() {
		return configuration.moduleFolder + configuration.folderSpells;
	}

	/**
	 * Gets the path to the effects folder.
	 *
	 * @return
	 */
	public static String getFolderEffects() {
		return configuration.moduleFolder + configuration.folderEffects;
	}

	/**
	 * Gets the path to the endings folder.
	 *
	 * @return
	 */
	public static String getFolderStorySequences() {
		return configuration.moduleFolder + configuration.folderStorySequences;
	}

	/**
	 * Gets the path to the races folder.
	 *
	 * @return
	 */
	public static String getFolderChatter() {
		return configuration.moduleFolder + configuration.folderChatter;
	}

	/**
	 * Gets the path to the Item Groups folder.
	 *
	 * @return
	 */
	public static String getFolderItemGroups() {
		return configuration.moduleFolder + configuration.folderItemGroups;
	}

	/**
	 * Gets the path to the items folder.
	 *
	 * @return
	 */
	public static String getFolderItems() {
		return configuration.moduleFolder + configuration.folderItems;
	}

	/**
	 * Gets the path to the string resources folder.
	 *
	 * @return
	 */
	public static String getFolderStringResources() {
		return configuration.moduleFolder + configuration.folderStringResources
				+ configuration.gameLocale.getLanguage() + "/";
	}

	/**
	 * Gets the path to the particles folder.
	 *
	 * @return
	 */
	public static String getFolderParticles() {
		return configuration.moduleFolder + configuration.folderParticles;
	}

	/**
	 * Gets the path to the UI folder.
	 *
	 * @return
	 */
	public static String getFolderUI() {
		return configuration.moduleFolder + configuration.folderUI;
	}

	public static String getItemPileFile() {
		return configuration.moduleFolder + configuration.fileItemPile;
	}

	/**
	 * Returns the path to the texture used to render character circles.
	 *
	 * @return
	 */
	public static String getFileCharacterCircleSprite() {
		return configuration.moduleFolder + configuration.fileCharacterCircleSprite;
	}

	/**
	 * Returns the path to the texture used to render the tile grid on isometric
	 * maps
	 *
	 * @return
	 */
	public static String getFileIsometricMapGridTexture() {
		return configuration.moduleFolder + configuration.fileIsometricMapGridTexture;
	}

	/**
	 * Returns the path to the texture used to render the tile grid on
	 * orthogonal maps
	 *
	 * @return
	 */
	public static String getFileOrthogonalMapGridTexture() {
		return configuration.moduleFolder + configuration.fileOrthogonalMapGridTexture;
	}

	/**
	 * Returns the path to the texture used to render the tile transition on
	 * isometric maps
	 *
	 * @return
	 */
	public static String getFileIsometricMapTransitionTexture() {
		return configuration.moduleFolder + configuration.fileIsometricMapTransitionTexture;
	}

	/**
	 * Returns the path to the texture used to render the tile transition on
	 * orthogonal maps
	 *
	 * @return
	 */
	public static String getFileOrthogonalMapTransitionTexture() {
		return configuration.moduleFolder + configuration.fileOrthogonalMapTransitionTexture;
	}

	/**
	 * Returns the path to the texture used to render a solid white tile on
	 * isometric maps
	 *
	 * @return
	 */
	public static String getFileIsometricMapSolidWhiteTileTexture() {
		return configuration.moduleFolder + configuration.fileIsometricMapSolidWhiteTileTexture;
	}

	/**
	 * Returns the path to the texture used to render a solid white tile on
	 * orthogonal maps
	 *
	 * @return
	 */
	public static String getFileOrthogonalMapSolidWhiteTileTexture() {
		return configuration.moduleFolder + configuration.fileOrthogonalMapSolidWhiteTileTexture;
	}

	/**
	 * Volume of music. Value between 0 and 1.
	 * 
	 * @return
	 */
	public static float getMusicVolume() {
		return configuration.options.s_audioMusicEffectsVolume;
	}

	/**
	 * Sets the current music volume.
	 * 
	 * @param volume
	 */
	public static void setMusicVolume(float volume) {
		configuration.options.s_audioMusicEffectsVolume = MathUtils.clamp(volume, 0, 1);
		Music.updatePlayingMusicVolume();
	}

	/**
	 * Volume of sound effects. Value between 0 and 1.
	 * 
	 * @return
	 */
	public static float getSoundEffectsVolume() {
		return configuration.options.s_audioSoundEffectsVolume;
	}

	/**
	 * Sets the current sound effects volume.
	 * 
	 * @param volume
	 */
	public static void setSoundEffectsVolume(float volume) {
		configuration.options.s_audioSoundEffectsVolume = MathUtils.clamp(volume, 0, 1);
	}

	/**
	 * Gets whether character barks are enabled.
	 * 
	 * @param volume
	 */
	public static boolean areCharacterBarksEnabled() {
		return configuration.options.s_characterBarks;
	}

	/**
	 * Sets the character barks to enabled or disabled.
	 * 
	 * @param volume
	 */
	public static void setCharacterBarksEnabled(boolean value) {
		configuration.options.s_characterBarks = value;
	}

	/**
	 * Volume of UI sound effects. Value between 0 and 1.
	 * 
	 * @return
	 */
	public static float getUIEffectsVolume() {
		return configuration.options.s_audioUIEffectsVolume;
	}

	/**
	 * Sets the current UI sound effects volume.
	 * 
	 * @param volume
	 */
	public static void setUIEffectsVolume(float volume) {
		configuration.options.s_audioUIEffectsVolume = MathUtils.clamp(volume, 0, 1);
	}

	/**
	 * Returns true if the debug panel is enabled.
	 * 
	 * @return
	 */
	public static boolean isDebugPanelEnabled() {
		return configuration.debugPanelEnabled;
	}

	/**
	 * How many pixels from the screen edges scrolling will occur
	 */
	public static int getScrollAreaStartOffset() {
		return configuration.scrollAreaStartOffset;
	}

	/**
	 * Number of tiles scrolled per second.
	 */
	public static float getScrollSpeed() {
		return configuration.options.s_scrollSpeed;
	}

	/**
	 * Set the number of tiles scrolled per second.
	 */
	public static void setScrollSpeed(float value) {
		configuration.options.s_scrollSpeed = value;
	}

	private void buildGlobalAssets() {
		globalAssets = new AssetMap();
		try {
			final ItemPile pile = new ItemPile("asset");
			pile.setShouldBeSaved(false);
			pile.gatherAssets(globalAssets);
		} catch (final IOException e) {
			throw new GdxRuntimeException(e);
		}

		globalAssets.put(getFileIsometricMapGridTexture(), Texture.class);
		globalAssets.put(getFileIsometricMapTransitionTexture(), Texture.class);
		globalAssets.put(getFileIsometricMapSolidWhiteTileTexture(), Texture.class);
		globalAssets.put(getFileOrthogonalMapGridTexture(), Texture.class);
		globalAssets.put(getFileOrthogonalMapTransitionTexture(), Texture.class);
		globalAssets.put(getFileOrthogonalMapSolidWhiteTileTexture(), Texture.class);
		if (getDefaultCursorPath() != null) {
			globalAssets.put(getDefaultCursorPath(), Texture.class);
		}
		if (getLockpickCursorPath() != null) {
			globalAssets.put(getLockpickCursorPath(), Texture.class);
		}
		if (getDisarmCursorPath() != null) {
			globalAssets.put(getDisarmCursorPath(), Texture.class);
		}
		if (getAttackCursorPath() != null) {
			globalAssets.put(getAttackCursorPath(), Texture.class);
		}
		if (getTalkToCursorPath() != null) {
			globalAssets.put(getTalkToCursorPath(), Texture.class);
		}
		

		for (AudioTrack<?> music : startMenuMusic) {
			music.gatherAssets(globalAssets);
		}
	}

	public static void gatherGlobalAssets(final AssetMap assets) {
		configuration.buildGlobalAssets();
		assets.putAll(configuration.globalAssets);
	}

	public static boolean isGlobalAsset(final String filepath) {
		return configuration.globalAssets.containsFile(filepath);
	}

	/**
	 * Returns whether the UI is allowed to sometimes move the mouse cursor
	 * automatically.
	 * 
	 * @return
	 */
	public static boolean getMoveMouse() {
		return configuration.options.s_moveMouse;
	}

	/**
	 * Sets whether the UI is allowed to sometimes move the mouse cursor
	 * automatically.
	 * 
	 * @return
	 */
	public static void setMoveMouse(boolean value) {
		configuration.options.s_moveMouse = value;
	}

	/**
	 * Returns the number of seconds the player must hover over a UI element
	 * before a tooltip is shown.
	 *
	 * @return
	 */
	public static float getTooltipDelay() {
		return configuration.options.s_tooltipDelay;
	}

	/**
	 * Sets the number of seconds the player must hover over a UI element before
	 * a tooltip is shown.
	 *
	 * @return
	 */
	public static void setTooltipDelay(float value) {
		configuration.options.s_tooltipDelay = value;
	}

	/**
	 * Will determine whether Lighting related debug should be drawn or not.
	 * This is not persisted anywhere.
	 * 
	 * @param value
	 */
	public static boolean shouldRenderLightsDebug() {
		return configuration.renderLightsDebug;
	}

	/**
	 * Will determine whether Lighting related debug should be drawn or not.
	 * This is not persisted anywhere.
	 * 
	 * @param value
	 */
	public static void setRenderLightsDebug(boolean value) {
		configuration.renderLightsDebug = value;
	}

	/**
	 * Will determine whether Line of Sight debug should be drawn or not. This
	 * is not persisted anywhere.
	 * 
	 * @param value
	 */
	public static boolean shouldRenderLOSDebug() {
		return configuration.renderLOSDebug;
	}

	/**
	 * Will determine whether Line of Sight debug should be drawn or not. This
	 * is not persisted anywhere.
	 * 
	 * @param value
	 */
	public static void setRenderLOSDebug(boolean value) {
		configuration.renderLOSDebug = value;
	}

	/**
	 * Gets the folder where savegames are stored.
	 * 
	 * @return
	 */
	public static String getFolderSaveGames() {
		return FOLDER_USER_DATA + configuration.moduleName + "/" + FOLDER_SAVEGAMES;
	}

	/**
	 * Returns the number of turns that must pass without any enemy appearing in
	 * combat before the combat ends automatically.
	 */
	public static int getAutomaticCombatEnd() {
		return configuration.automaticCombatEnd;
	}

	/**
	 * Returns the ID of the chatter that should be used to generate shouts for
	 * characters when they start combat.
	 */
	public static String getDefaultCombatStartedChatterId() {
		return configuration.defaultCombatStartedChatterId;
	}

	/**
	 * Returns a Chance to Hit bonus an attacker receives when attacking from
	 * the side with melee attacks.
	 * 
	 * @return
	 */
	public static int getCtHBonusSide() {
		return configuration.cthBonusSide;
	}

	/**
	 * Returns a Chance to Hit bonus an attacker receives when attacking from
	 * the back with melee attacks.
	 * 
	 * @return
	 */
	public static int getCtHBonusBack() {
		return configuration.cthBonusBack;
	}

	/**
	 * Returns an ap cost penalty for moving when standing on a tile next to an
	 * enemy during combat.
	 * 
	 * @return
	 */
	public static int getDisengageMovementPenalty() {
		return configuration.disengagementMovementPenalty;
	}

	public static float getMapScale() {
		return configuration.mapScale;
	}

	/**
	 * Gets the number of hours of game time it takes to break camp.
	 *
	 * @return
	 */
	public static int getBreakingCampDuration() {
		return configuration.breakingCampDuration;
	}

	/**
	 * Gets the number of hours of game time it takes to pack a camp up.
	 *
	 * @return
	 */
	public static int getPackingCampDuration() {
		return configuration.packingCampDuration;
	}

	/**
	 * Returns whether or not the game should pause whenever a survival state of
	 * a player character changes for the worse (she becomes more hungry for
	 * example).
	 *
	 * @return
	 */
	public static boolean getPauseOnBadChange() {
		return configuration.pauseOnBadChange;
	}

	/**
	 * Returns how long does sleep take in game hours.
	 *
	 * @return
	 */
	public static int getSleepDuration() {
		return configuration.sleepDuration;
	}

	/**
	 * Returns the amount by which the stolen item's cost should be multiplied
	 * in order to determine the fine for the item's theft.
	 * 
	 * @return
	 */
	public static float getTheftFineMultiplier() {
		return configuration.theftFineMultiplier;
	}

	/**
	 * Returns the disposition penalty for theft.
	 */
	public static int getTheftDispositionPenalty() {
		return configuration.theftDispositionPenalty;
	}

	/**
	 * Gets the base fine for assault. This might be further modified by the
	 * disposition of the victim towards the assaulter.
	 */
	public static int getAssaultBaseFine() {
		return configuration.assaultBaseFine;
	}

	/**
	 * Returns the disposition penalty for assault.
	 */
	public static int getAssaultDispositionPenalty() {
		return configuration.assaultDispositionPenalty;
	}

	/**
	 * Gets the base fine for trespass. This might be further modified by the
	 * disposition of the location's faction towards the assaulter.
	 * 
	 * @return
	 */
	public static int getTrespassBaseFine() {
		return configuration.trespassBaseFine;
	}

	/**
	 * Returns the disposition penalty for trespass.
	 */
	public static int getTrespassDispositionPenalty() {
		return configuration.trespassDispositionPenalty;
	}

	/**
	 * Returns the disposition penalty for murder.
	 */
	public static int getMurderDispositionPenalty() {
		return configuration.murderDispositionPenalty;
	}

	/**
	 * Returns the ID of the dialogue that should be initiated by witnesses if
	 * they see a crime and want to demand a fine. This can be defined per
	 * character, but if not defined, this dialogue will be used instead.
	 * 
	 * @return
	 */
	public static String getDefaultFineDialogueId() {
		return configuration.defaultFineDialogueId;
	}

	/**
	 * Returns the ID of the dialogue that should be initiated by law enfoncers
	 * if they see a known criminal. This can be defined per character, but if
	 * not defined, this dialogue will be used instead.
	 * 
	 * @return
	 */
	public static String getDefaultLawEnfoncerDialogueId() {
		return configuration.defaultLawEnfoncerDialogueId;
	}

	/**
	 * Returns the ID of the chatter that should be used to generate shouts for
	 * characters when they spot a crime and become hostile.
	 * 
	 * @return
	 */
	public static String getDefaultCrimeSpottedChatterId() {
		return configuration.defaultCrimeSpottedChatterId;
	}

	/**
	 * Returns the number of game hours a game character will remain hostile
	 * towards the player if he witnessed a crime and the player refused or
	 * could not pay the fine.
	 * 
	 * This only applies if the character faction is not actually hostile to the
	 * player.
	 * 
	 * @return
	 */
	public static int getHostilityDuration() {
		return configuration.hostilityDuration;
	}

	/**
	 * Returns true if the survival subsystem is enabled.
	 * 
	 * @return
	 */
	public static boolean isSurvivalEnabled() {
		return configuration.survivalEnabled;
	}

	/**
	 * Gets the number of tiles a group has to go over on the overland map in
	 * order to be able to trigger a random encounter again.
	 *
	 * @return
	 */
	public static int getRandomEncountersCooldown() {
		return configuration.randomEncountersCooldown;
	}

	/**
	 * Returns the base chance of a random encounter happening, in percentage
	 * points.
	 *
	 * @return
	 */
	public static int getRandomEncountersBaseChance() {
		return configuration.randomEncountersBaseChance;
	}

	/**
	 * Returns the level tolerance for random encounters. This dictates how many
	 * levels above or below the player's level an enemy group can be and still
	 * be considered for a random encounter.
	 *
	 * @return
	 */
	public static int getRandomEncountersLevelTolerance() {
		return configuration.randomEncountersLevelTolerance;
	}

	/**
	 * Returns the id of the map on which the game should start.
	 * 
	 * @return
	 */
	public static String getStartMap() {
		return configuration.startMap;
	}

	/**
	 * Returns an array of IDs of characters that should be members of the
	 * player group at the start of the game.
	 * 
	 * If this is empty, the player will be presented with a character creation
	 * screen before the game starts.
	 * 
	 * Changing the returning array will have no effect.
	 * 
	 * @return
	 */
	public static Array<String> getStartGroupMembers() {
		return new Array<String>(configuration.startGroupMembers);
	}

	/**
	 * Returns the formation to use at the start of the game. If this is empty,
	 * the simple line formation will be used instead.
	 * 
	 * @return
	 */
	public static Formation getStartFormation() {
		return configuration.startFormation;
	}

	/**
	 * Returns the array containing music tracks that should be played when the
	 * game starts.
	 * 
	 * @return
	 */
	public static Array<AudioTrack<?>> getStartMenuMusic() {
		return configuration.startMenuMusic;
	}

	/**
	 * Returns the number of characters it should be possible to create at the
	 * start of the game.
	 * 
	 * @return
	 */
	public static int getNumberOfCharactersToCreate() {
		return configuration.numberOfCharactersToCreate;
	}

	/**
	 * Returns configuration for loading screens.
	 * 
	 * @return
	 */
	public static LoadingScreens getLoadingScreensConfiguration() {
		return configuration.loadingScreensConfiguration;
	}
	
	/**
	 * Returns the max encumberance ratio for the supplied armor class. This is 
	 * the max ratio of the total_worn_armor_weight / race_max_encumberance of
	 * the given character for the armor worn to be still considered this armor class.
	 * 
	 * For example, light armor class max ratio can be defined as 0.10. This means
	 * that if the total weight of all armor worn by a character is one tenth or less
	 * of the total weight the character can carry, the character is considered wearing light
	 * armor.
	 * 
	 * @param ac
	 * @param skillLevel
	 * @return
	 */
	public static float getMaxRatioForArmorClass(ArmorClass ac) {
		Float returnValue = configuration.armorClassRatio.get(ac);
		return returnValue != null ? returnValue : 0f;
	}
	
	/**
	 * Returns the modifiers that should be used for the given armor class
	 * if the wearer of that armor has the supplied skill level in Armor.
	 * 
	 * @param ac
	 * @param skillLevel
	 * @return
	 */
	public static Iterator<Modifier> getModifiersForArmorClass(ArmorClass ac, int skillLevel) {
		IntMap<ModifierContainer> modifiers = configuration.armorClassModifiers.get(ac);
		if (modifiers == null) {
			return EMPTY_MODIFIERS.getModifiers();
		}
		
		ModifierContainer returnValue = modifiers.get(skillLevel);
		return (returnValue != null ? returnValue : EMPTY_MODIFIERS).getModifiers();
	}
	
	public static ExperienceTable getExperienceTable() {
		return configuration.experienceTable;
	}

	@Override
	public void loadFromXML(final FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
	}

	@Override
	public void loadFromXMLNoInit(final FileHandle file) throws IOException {
		final XmlReader xmlReader = new XmlReader();
		final Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		XMLUtil.readPrimitiveMembers(this, root);
		if (startMap == null) {
			throw new GdxRuntimeException("Start map must be specified!");
		}
		Element startElement = root.getChildByName(XML_START);
		Element groupElement = startElement.getChildByName(XML_GROUP);
		if (groupElement != null) {
			Element membersElement = groupElement.getChildByName(XML_MEMBERS);
			if (membersElement != null) {
				for (int i = 0; i < membersElement.getChildCount(); ++i) {
					startGroupMembers.add(membersElement.getChild(i).getText());
				}
			}
			if (groupElement.getChildByName(XML_FORMATION) != null) {
				startFormation = new Formation(groupElement);
			}
		}
		
		Element uiElement = root.getChildByName(XML_UI);
		if (uiElement != null) {
			uiElement = uiElement.getChildByName(XML_INDICATORS);
			if (uiElement != null) {
				Element indicator = uiElement.getChildByName(XML_DETECT);
				if (indicator != null) {
					detectTrapsIndicator = new ParticleEffectDescriptor(indicator);
				}
				indicator = uiElement.getChildByName(XML_SNEAK);
				if (indicator != null) {
					sneakIndicator = new ParticleEffectDescriptor(indicator);
				}
			}
		}

		Element menuElement = startElement.getChildByName(XML_MENU);
		if (menuElement != null) {
			Element musicElement = menuElement.getChildByName(XML_MUSIC);
			if (musicElement != null) {
				startMenuMusic = XMLUtil.readTracks(musicElement, Music.class);
			}
		}
		
		Element characterElement = root.getChildByName(XML_CHARACTER);
		if (characterElement != null) {
			Element armorElement = characterElement.getChildByName(XML_ARMOR);
			for (int i = 0; armorElement != null && i < armorElement.getChildCount(); ++i) {
				Element armorClassElement = armorElement.getChild(i);
				ArmorClass ac = ArmorClass.valueOf(armorClassElement.getName().toUpperCase(Locale.ENGLISH));
				armorClassRatio.put(ac, Float.parseFloat(armorClassElement.get(XML_MAXRATIO, "-1")));
				IntMap<ModifierContainer> modifiers = new IntMap<ModifierContainer>();
				armorClassModifiers.put(ac, modifiers);
				Element penaltiesElement = armorClassElement.getChildByName(XML_PENALTIES);
				for (int j = 0; penaltiesElement != null && j < penaltiesElement.getChildCount(); ++j) {
					Element armorSkillElement = penaltiesElement.getChild(j);
					int skillValue = armorSkillElement.getIntAttribute(XMLUtil.XML_ATTRIBUTE_VALUE);
					ModifierContainer armorClassModifiers = new ArmorModifiers();
					XMLUtil.readModifiers(armorClassModifiers, armorSkillElement.getChildByName(XMLUtil.XML_MODIFIERS));
					modifiers.put(skillValue, armorClassModifiers);
				}
			}
		}
		experienceTable = new ExperienceTable(Gdx.files.internal(configuration.moduleFolder
				+ configuration.fileExperienceTable));
	}

	private static class GameOptions {
		private int s_screenWidth = 1600;
		private int s_screenHeight = 900;
		private boolean s_fullscreen = false;
		private float s_scrollSpeed = 20;
		private float s_tooltipDelay = 0.5f;
		private float s_audioSoundEffectsVolume = 0.3f;
		private float s_audioMusicEffectsVolume = 0.3f;
		private float s_audioUIEffectsVolume = 0.2f;
		private boolean s_characterBarks = true;
		private float s_combatMoveSpeed = 1f;
		private boolean s_moveMouse = true;

		private GameOptions() {
		}

		private GameOptions(final FileHandle file) throws IOException {
			final XmlReader xmlReader = new XmlReader();
			final Element root = xmlReader.parse(file);
			XMLUtil.readPrimitiveMembers(this, root);
			Element keyBindingsElement = root.getChildByName(XML_KEY_BINDINGS);
			if (keyBindingsElement != null) {
				for (int i = 0; i < keyBindingsElement.getChildCount(); ++i) {
					Element bindingElement = keyBindingsElement.getChild(i);
					KeyBindings binding = KeyBindings.valueOf(bindingElement.getName().toUpperCase(Locale.ENGLISH));
					binding.getKeys().clear();
					Array<Integer> newKeys = new Array<Integer>();
					if (bindingElement.getText() != null) {
						for (String key : bindingElement.getText().split(",")) {
							newKeys.add(Integer.valueOf(key));
						}
					}
					binding.getKeys().addAll(newKeys);
				}
			}
		}
	}
}