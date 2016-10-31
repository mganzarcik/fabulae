package mg.fishchicken.gamelogic.crime;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.CharacterFilter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.dialogue.Chatter;
import mg.fishchicken.gamelogic.dialogue.DialogueCallback;
import mg.fishchicken.gamelogic.dialogue.PCTalk;
import mg.fishchicken.gamelogic.dialogue.Chatter.ChatterType;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamestate.crime.Crime;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class CrimeManager implements XMLSaveable {

	public static final String STRING_TABLE = "crime."+Strings.RESOURCE_FILE_EXTENSION;
	public static final String XML_CRIMES = "crimes";
	public static final String XML_CRIME = "crime";
	public static final String XML_FACTIONS = "factions";
	
	private GameState gameState;
	private ObjectMap<Faction, Array<Crime<?>>> trackedCrimes;
	private ObjectSet<AbstractGameCharacter> tempCharactersSet;
	
	public CrimeManager(GameState gameState) {
		this.gameState = gameState;
		trackedCrimes = new ObjectMap<Faction, Array<Crime<?>>>();
		tempCharactersSet = new  ObjectSet<AbstractGameCharacter>();
	}
	
	/**
	 * Registers a new crime with the crime manager if the crime is relevant. 
	 * 
	 * Returns true if the crime was considered relevant and handled, or false if 
	 * it was ignored.
	 * 
	 * @param crime
	 * @return
	 */
	public boolean registerNewCrime(final Crime<?> crime) {
		final GameCharacter perp = crime.getPerpetrator();
		Faction victimFaction = crime.getVictimFaction();
		
		// crime committed by computer controlled character are ignored
		if (!perp.isMemberOfPlayerGroup()) {
			return false;
		}
		
		// ignore crimes committed between hostile factions and crimes with no witnesses
		if (Faction.areHostile(victimFaction, perp.getFaction()) || crime.getWitnesses().size < 1) {
			return false;
		}
		
		Log.logLocalized("crimeCommitted", LogType.CRIME, perp.getName(), crime.getName());
		
		victimFaction.modifyDisposition(perp.getFaction(), -crime.getDispositionPenalty());
		UIManager.closeMutuallyExclusiveScreens();
		if (!victimFaction.isHostileTowards(perp)) {
			final int fineAmount = crime.getFineAmount();
			if (crime.canBePaidOff() && perp.getGold() >= fineAmount && !GameState.isCombatInProgress()) {
				GameCharacter witness = crime.getWitnesses().random();
				ObjectMap<String, String> dialogueParameters = new ObjectMap<String, String>();
				dialogueParameters.put("fine", Integer.toString(crime.getFineAmount()));
				UIManager.displayDialogue(perp, witness, witness.getFineDialogueId(), new DialogueCallback() {
					@Override
					public void onDialogueEnd(PCTalk dialogueStopper) {
						if (dialogueStopper.isYes()) {
							perp.addGold(-fineAmount);
						} else {
							handleNoFinePaid(crime);
						}
					}
				}, dialogueParameters);
			} else {
				handleNoFinePaid(crime);
			}
		} else {
			handleNoFinePaid(crime);
		}
		return true;
	}
	
	/**
	 * This will spread information about crimes known to fromFaction but
	 * unknown to toFaction from the one to the other.
	 * 
	 * @param fromFaction
	 * @param toFaction
	 */
	public void spreadCrimeInfo(Faction fromFaction, Faction toFaction) {
		Array<Crime<?>> crimesKnownToFromFaction = trackedCrimes.get(fromFaction);
		
		if (crimesKnownToFromFaction == null) {
			return;
		}
		
		ObjectMap<Faction, Integer> penalties = new ObjectMap<Faction, Integer>();
		for (Crime<?> crime : crimesKnownToFromFaction) {
			if (addCrime(crime, toFaction)) {
				Faction perpFaction = crime.getPerpetrator().getFaction(); 
				int penalty = crime.getDispositionPenalty()/2;
				if (penalties.containsKey(perpFaction)) {
					penalties.put(perpFaction, penalties.get(perpFaction)+penalty);
				} else {
					penalties.put(perpFaction, penalty);
				}
			}
		}
		for (Entry<Faction, Integer> entry : penalties.entries()) { 
			toFaction.modifyDisposition(entry.key, -entry.value);
		}
	}
	
	/**
	 * Will make the supplied law enfoncer character check for any known criminals in the visible area.
	 * 
	 * If any are found, the same process will be applied as if the law enfoncer just became a witness
	 * of the crime.
	 * 
	 * @param lawEnfoncer
	 */
	public void checkForCriminals(final GameCharacter lawEnfoncer) {
		if (!lawEnfoncer.isLawEnfoncer() || lawEnfoncer.getFaction() == Faction.NO_FACTION || lawEnfoncer.getMap().isWorldMap()) {
			return;
		}
		tempCharactersSet.clear();
		lawEnfoncer.getAllCharactersInViewCone(tempCharactersSet, CharacterFilter.PLAYER_FACTION);
		for (AbstractGameCharacter character : tempCharactersSet) {
			if (!(character instanceof GameCharacter)) {
				continue;
			}
			final GameCharacter perp = (GameCharacter)character;
			final Array<Crime<?>> knownCrimes = getCrimesTrackedFor(lawEnfoncer.getFaction(), (GameCharacter)perp);
			if (knownCrimes.size > 0) {
				int fineAmount = 0;
				boolean canBePaidOff = true;
				for (Crime<?> crime : knownCrimes) {
					fineAmount += crime.getFineAmount() * getNumberOfFactionsAwareOfCrime(crime);
					canBePaidOff = canBePaidOff && crime.canBePaidOff();
				}
				final int finalFineAmount = fineAmount;
				if (canBePaidOff && perp.getGold() >= fineAmount && !GameState.isCombatInProgress()) {
					ObjectMap<String, String> dialogueParameters = new ObjectMap<String, String>();
					dialogueParameters.put("fine", Integer.toString(finalFineAmount));
					UIManager.displayDialogue(perp, lawEnfoncer, lawEnfoncer.getLawEnfoncerDialogueId(), new DialogueCallback() {
						@Override
						public void onDialogueEnd(PCTalk dialogueStopper) {
							if (dialogueStopper.isYes()) {
								perp.addGold(-finalFineAmount);
								removeCrimes(knownCrimes);
							} else {
								lawEnfoncer.makeTemporarilyHostileTowards(perp.getFaction(), Configuration.getHostilityDuration());
								gameState.startCombat();
							}
						}
					}, dialogueParameters);
				} else {
					lawEnfoncer.makeTemporarilyHostileTowards(perp.getFaction(), Configuration.getHostilityDuration());
					gameState.startCombat();
				}
			}
		}
	}
	
	/**
	 * Returns all tracked crimes that were committed by the supplied character
	 * against the supplied faction.
	 * 
	 * @param victimFactiom
	 * @param committedBy
	 * @return
	 */
	private Array<Crime<?>> getCrimesTrackedFor(Faction victimFactiom, GameCharacter committedBy) {
		Array<Crime<?>> returnValue = new Array<Crime<?>>();
		Array<Crime<?>> crimesKnownToFaction = trackedCrimes.get(victimFactiom);
		if (crimesKnownToFaction == null) {
			return returnValue;
		}
		for (Crime<?> crime : crimesKnownToFaction) {
			if (crime.getPerpetrator() == committedBy) {
				returnValue.add(crime);
			}
		}
		return returnValue;
	}
	
	private void handleNoFinePaid(Crime<?> crime) {
		addCrime(crime);
		Array<GameCharacter> witnesses = crime.getWitnesses();
		Faction perpFaction = crime.getPerpetrator().getFaction();
		int dispositionPenalty = crime.getDispositionPenalty() / 2;
		Array<Faction> alreadyProcessedFactions = new Array<Faction>();
		alreadyProcessedFactions.add(crime.getVictimFaction());
		for (GameCharacter witness : witnesses) {
			Faction witnessFaction = witness.getFaction();
			addCrime(crime, witnessFaction);
			if (!alreadyProcessedFactions.contains(witnessFaction, true)) {
				witnessFaction.modifyDisposition(perpFaction, -dispositionPenalty);
				alreadyProcessedFactions.add(witnessFaction);
			}
			witness.makeTemporarilyHostileTowards(perpFaction, Configuration.getHostilityDuration());
			witness.shout(witness.getChatter().getTexts(ChatterType.CRIME_SPOTTED, witness.getCurrentLocations()).random());
		}
		gameState.startCombat();
	}
	
	private int getNumberOfFactionsAwareOfCrime(Crime<?> crime) {
		int returnValue = 0;
		for (Array<Crime<?>> crimes : trackedCrimes.values()) {
			if (crimes.contains(crime, false)) {
				++ returnValue;
			}
		}
		return returnValue;
	}
	
	/**
	 * Adds the supplied crime to the list of tracked crimes for the faction of the victim.
	 * 
	 * @param crime
	 * @param faction
	 */
	private void addCrime(Crime<?> crime) {
		addCrime(crime, crime.getVictimFaction());
	}
	
	/**
	 * Adds the supplied crime to the list of tracked crimes for the supplied faction.
	 * 
	 * This will do nothing if the supplied faction is a NoFaction.
	 * 
	 * @param crime
	 * @param faction
	 * @return true if the crime was added, false otherwise
	 */
	private boolean addCrime(Crime<?> crime, Faction faction) {
		if (Faction.NO_FACTION.equals(faction)) {
			return false;
		}
		Array<Crime<?>> crimes = trackedCrimes.get(faction);
		if (crimes == null) {
			crimes = new Array<Crime<?>>();
			trackedCrimes.put(faction, crimes);
		}
		if (!crimes.contains(crime, false)) {
			crimes.add(crime);
			return true;
		}
		return false;
	}
	
	private void removeCrimes(Array<Crime<?>> crimes) {
		for (Crime<?> crime : crimes) {
			removeCrime(crime);
		}
	}
	
	private void removeCrime(Crime<?> crime) {
		for (Array<Crime<?>> crimes : trackedCrimes.values()) {
			crimes.removeValue(crime, false);
		}
	}
	
	/**
	 * Removes all tracked crimes and resets the state of the crime manager.
	 */
	public void reset() {
		trackedCrimes.clear();
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		Set<Crime<?>> crimesToSave = new HashSet<Crime<?>>();
		writer.element(XML_FACTIONS);
		for (Entry<Faction, Array<Crime<?>>> entry : trackedCrimes.entries()) {
			writer.element(entry.key.getId());
			for (Crime<?> crime : entry.value) {
				crimesToSave.add(crime);
				writer.element(XML_CRIME, crime.getId());
			}
			writer.pop();
		}
		writer.pop();
		
		writer.element(XML_CRIMES);
		for (Crime<?> crime : crimesToSave) {
			crime.writeToXML(writer);
		}
		writer.pop();
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		ObjectMap<String, Crime<?>> savedCrimes = new ObjectMap<String, Crime<?>>();
		Element crimesElement = root.getChildByName(XML_CRIMES);
		for (int i = 0; i < crimesElement.getChildCount(); ++i) {
			Crime<?> crime = Crime.readCrime(crimesElement.getChild(i));
			savedCrimes.put(crime.getId(), crime);
		}
		
		Element factionsElement = root.getChildByName(XML_FACTIONS);
		for (int i = 0; i < factionsElement.getChildCount(); ++i) {
			Element factionElement = factionsElement.getChild(i);
			Faction faction = Faction.getFaction(factionElement.getName());
			for (int j = 0; j < factionElement.getChildCount(); ++j) {
				addCrime(savedCrimes.get(factionElement.getChild(j).getText()), faction);
			}
		}
	}
}
