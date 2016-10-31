package mg.fishchicken.gamelogic.characters;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.RaceInventory;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamestate.characters.Skills;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Represents a character race in the game.
 * 
 * Contains skill modifiers and basic game logic information like the number of
 * HP, SP and MP dices and maximum encumberance.
 * 
 * @author Annun
 * 
 */
public class Race implements XMLLoadable, InventoryContainer, ThingWithId {

	private static ObjectMap<String, String> races = new ObjectMap<String, String>();

	public static Race getRace(String id) {
		return Assets.get(races.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns an array of all races that can be selected by the player
	 * during character creation.
	 */
	public static Array<Race> getAllPlayableRaces() {
		Array<Race> returnValue = new Array<Race>();
		for (String racePath : races.values()) {
			Race race = Assets.get(racePath);
			if (race.isPlayable()) {
				returnValue.add(race);
			}
		}
		return returnValue;
	}

	/**
	 * Gathers all Races and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherRaces() throws IOException {
		Assets.gatherAssets(Configuration.getFolderRaces(), "xml", Race.class, races);
	}
	
	private String s_id;
	private String s_name;
	private String s_description;
	private float s_walkSpeed;
	private float s_sneakingSpeed;
	private float s_detectTrapsSpeed;
	private int s_maxHPGain;
	private int s_maxSPGain;
	private int s_maxMPGain;
	private int s_maxEncumbrance;
	private int s_maxAP;
	private float s_experienceGainMultiplier;
	private int s_thirstPeriod = 2;
	private int s_thirstRecovery = 6;
	private float s_amountDrank = 0.5f;
	private int s_hungerPeriod = 4;
	private float s_amountEaten = 1;
	private int s_hungerRecovery = 2;
	private int s_sleepPeriod = 18;
	private float s_moreTiredAfter = 2;
	private float s_maxRations = 10;
	private float s_maxWater = 6;
	private boolean s_isPlayable;
	private int s_HPRecovery, s_SPRecovery, s_MPRecovery;
	private Skills inherentSkills;
	private RaceInventory startingInventory;

	public Race(FileHandle file) throws IOException {
		s_id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		inherentSkills = new Skills();
		s_isPlayable = false;
		startingInventory = new RaceInventory(this);
		loadFromXML(file);
	}
	
	@Override
	public Inventory getInventory() {
		return startingInventory;
	}

	@Override
	public boolean isOwnerOf(InventoryItem item) {
		return false;
	}
	
	public String getName() {
		return Strings.getString(s_name);
	}
	
	public String getDescription() {
		return Strings.getString(s_description);
	}

	public int getMaxHPGain() {
		return s_maxHPGain;
	}

	public int getMaxSPGain() {
		return s_maxSPGain;
	}

	public int getMaxMPGain() {
		return s_maxMPGain;
	}

	public int getMaxAP() {
		return s_maxAP;
	}

	public Skills getInherentSkills() {
		return inherentSkills;
	}

	public String getId() {
		return s_id;
	}
	
	/**
	 * Whether or not the player can select this Race
	 * when creating a new character.
	 * 
	 * Has no impact on whether or not a character
	 * of this race that was not created by the player 
	 * can join the player character group.
	 * 
	 * Default is false.
	 * 
	 * @return
	 */
	public boolean isPlayable() {
		return s_isPlayable;
	}

	/**
	 * Returns the maximum load of the race members in grams.
	 * 
	 * @return
	 */
	public int getMaxEncumbrance() {
		return s_maxEncumbrance;
	}

	public float getExperienceGainMultiplier() {
		return s_experienceGainMultiplier;
	}

	/**
	 * Gets the number of game hours after which the members of this race need
	 * to drink or will become more thirsty. A negative number means a character
	 * does not need to drink at all.
	 * @return
	 */
	public int getThirstPeriod() {
		return s_thirstPeriod;
	}
	
	/**
	 * Returns the liters of water members of this race will drink
	 * each time they are thirsty.
	 * @return
	 */
	public float getAmountDrank() {
		return s_amountDrank;
	}

	/**
	 * Gets the number of thirst stages that are removed
	 * each time a character drinks when thirsty.
	 * @return
	 */
	public int getThirstRecovery() {
		return s_thirstRecovery;
	}
	
	/**
	 * Gets the number of game hours after which the members of this race need
	 * to eat or will become more hungry. A negative number means a character
	 * does not need to eat at all.
	 * @return
	 */
	public int getHungerPeriod() {
		return s_hungerPeriod;
	}
	
	/**
	 * Returns the number of rations the members of this race will eat
	 * each time they are hungry.
	 * @return
	 */
	public float getAmountEaten() {
		return s_amountEaten;
	}
	
	/**
	 * Gets the number of hunger stages that are removed
	 * each time a character eats when thirsty.
	 * @return
	 */
	public int getHungerRecovery() {
		return s_hungerRecovery;
	}

	/**
	 * Gets the number of game hours after which the members of this race need
	 * to sleep or will become more tired. A negative number means a character
	 * does not need to sleep at all.
	 * @return
	 */
	public int getSleepPeriod() {
		return s_sleepPeriod;
	}

	/**
	 * Gets the number of game hours after which the member of this race gets more tired
	 * after it has not slept for longer than the sleep period.
	 * 
	 * @return
	 */
	public float getMoreTiredAfter() {
		return s_moreTiredAfter;
	}
	/**
	 * Gets the maximum number of rations members of this race
	 * can carry.
	 * @return
	 */
	public float getMaxRations() {
		return s_maxRations;
	}

	/**
	 * Gets the maximum liters of water members of this race
	 * can carry.
	 * @return
	 */
	public float getMaxWater() {
		return s_maxWater;
	}

	/**
	 * Gets the rate of recovery of HP during sleep.
	 * 
	 * The number returned is the percentage of max HP
	 * that is healed every time a member of this race
	 * sleeps for the full duration.
	 * 
	 * @return
	 */
	public int getHPRecovery() {
		return s_HPRecovery;
	}

	/**
	 * Gets the rate of recovery of SP during sleep.
	 * 
	 * The number returned is the percentage of max SP
	 * that is healed every time a member of this race
	 * sleeps for the full duration.
	 * 
	 * @return
	 */
	public int getSPRecovery() {
		return s_SPRecovery;
	}

	/**
	 * Gets the rate of recovery of MP during sleep.
	 * 
	 * The number returned is the percentage of max MP
	 * that is healed every time a member of this race
	 * sleeps for the full duration.
	 * 
	 * @return
	 */
	public int getMPRecovery() {
		return s_MPRecovery;
	}
	
	/**
	 * Returns the speed (in tiles per second) of walking for
	 * members of this race.
	 * 
	 * @return
	 */
	public float getWalkSpeed() {
		return s_walkSpeed;
	}

	/**
	 * Returns the base speed (in tiles per second) of sneaking for
	 * members of this race.
	 * 
	 * @return
	 */
	public float getSneakingSpeed() {
		return s_sneakingSpeed;
	}
	
	
	/**
	 * Returns the base walk speed (in tiles per second) while detecting traps for
	 * members of this race.
	 * 
	 * @return
	 */
	public float getDetectingTrapsSpeed() {
		return s_detectTrapsSpeed;
	}
	
	@Override
	public String toString() {
		return s_id;
	}

	@Override
	public void loadFromXML(FileHandle raceFile) throws IOException {
		loadFromXMLNoInit(raceFile);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		XMLUtil.readPrimitiveMembers(this, root);
		startingInventory.loadFromXML(root);
		inherentSkills.loadFromXML(root);
	}
}
