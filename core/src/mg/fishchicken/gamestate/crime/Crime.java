package mg.fishchicken.gamestate.crime;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.crime.CrimeManager;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.ObservableState;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public abstract class Crime<T> extends ObservableState<Crime<T>, Crime.CrimeParams<T>> implements ThingWithId {

	public static final String XML_PERPETRATOR = "perpetrator";
	public static final String XML_TARGET = "target";
	public static final String XML_WITNESSES = "witnesses";
	public static final String XML_WITNESS = "witness";
	
	protected static GameState gameState;
	
	public static void setGameState(GameState state) {
		gameState = state;
	}
	
	private String s_id;
	private GameCharacter perpetrator;
	private T crimeTarget;
	private Array<GameCharacter> witnesses;
	
	public Crime() {
		witnesses = new Array<GameCharacter>();
	}
	
	public Crime(GameCharacter perpetrator, T crimeTarget) {
		this.s_id = Integer.toString(gameState.getNextId());
		this.perpetrator = perpetrator;
		this.crimeTarget = crimeTarget;
		determineWitnesses();
	}
	
	private void determineWitnesses() {
		this.witnesses = new Array<GameCharacter>();
		GameMap map = perpetrator.getMap();
		if (map == null) {
			return;
		}
		int radius = map.isWorldMap() ? Configuration.getSightRadiusWorld() : Configuration.getSightRadiusLocal();
		// make the radius two tiles bigger to account for any rounding errors
		radius += 2;
		PositionArray circle = MathUtil.getCircle(perpetrator.position().tile().getX(), perpetrator.position().tile().getY(), radius, true);
		ObjectSet<GameCharacter> potentialWitnesses = new ObjectSet<GameCharacter>(); 
		map.getAllObjectsInArea(potentialWitnesses, circle, GameCharacter.class);
		Faction victimFaction = getVictimFaction();
		Faction perpFaction = perpetrator.getFaction();
		for (GameObject go : potentialWitnesses) {
			GameCharacter potentialWitness = (GameCharacter) go;
			Faction witnessFaction = potentialWitness.getFaction();
			if (!perpFaction.equals(witnessFaction) && !Faction.areHostile(witnessFaction, victimFaction) && potentialWitness.canSee(perpetrator)) {
				witnesses.add(potentialWitness);
			}
		}
	}
	
	/**
	 * Returns the unique identifier of this crime.
	 * @return
	 */
	public String getId() {
		return s_id;
	}
	
	/**
	 * The actual thing that was the target of the crime.
	 * 
	 * @return
	 */
	public T getCrimeTarget() {
		return crimeTarget;
	}
	
	/**
	 * Gets the crime's victim faction.
	 * @return
	 */
	public abstract Faction getVictimFaction(); 
	
	public GameCharacter getPerpetrator() {
		return perpetrator;
	}
	
	public Array<GameCharacter> getWitnesses() {
		return witnesses;
	}
	
	public String getName() {
		return Strings.getString(CrimeManager.STRING_TABLE, this.getClass().getSimpleName());
	}
	
	public abstract boolean canBePaidOff();
	
	protected abstract int getBaseFineAmount();
	
	public int getFineAmount() {
		int disposition = getVictimFaction().getDispositionTowards(getPerpetrator());
		int fine =  getBaseFineAmount();
		if (disposition > 0) {
			fine = fine - (((fine / 2) / 100) * (MathUtil.boxValue(disposition, disposition, 100))); 
		}
		return fine;
	}
	
	public abstract int getDispositionPenalty();
	
	@Override
	public void loadFromXML(Element crimeElement) throws IOException {
		XMLUtil.readPrimitiveMembers(this, crimeElement);
		readXMLContents(crimeElement);
	}
	
	@Override
	protected void writeXMLContents(XmlWriter writer) throws IOException {
		writer.element(XML_PERPETRATOR, perpetrator.getInternalId());
		
		writer.element(XML_TARGET);
		writeTargetToXml(writer);
		writer.pop();
		
		writer.element(XML_WITNESSES);
		for (GameCharacter witness : witnesses) {
			writer.element(XML_WITNESS, witness.getInternalId());
		}
		writer.pop();
	}
	
	@Override
	protected void readXMLContents(Element element) throws IOException {
		perpetrator = (GameCharacter) GameState.getGameObjectByInternalId(element.get(XML_PERPETRATOR));
		crimeTarget = readTargetFromXml(element.getChildByName(XML_TARGET));
		Element witnesses = element.getChildByName(XML_WITNESSES);
		for (int i = 0; i < witnesses.getChildCount(); ++i) {
			GameCharacter witness = (GameCharacter) GameState.getGameObjectByInternalId(witnesses.getChild(i).getText());
			this.witnesses.add(witness);
		}
	}
	
	protected abstract void writeTargetToXml(XmlWriter writer) throws IOException;
	protected abstract T readTargetFromXml(Element targetElement) throws IOException;
	
	public static Crime<?> readCrime(Element crimeElement) throws IOException {
		try {
			Class<? extends Crime<?>> crimeClass = getCrimeClassForSimpleName(crimeElement.getName());
			Crime<?> crimeInstance = crimeClass.newInstance(); 
			crimeInstance.loadFromXML(crimeElement);
			return crimeInstance;
		} catch (ClassNotFoundException e) {
			throw new GdxRuntimeException(e);
		} catch (InstantiationException e) {
			throw new GdxRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Crime<?>> getCrimeClassForSimpleName(String simpleName) throws ClassNotFoundException {
		String className = Crime.class.getPackage().getName()+"."+StringUtil.capitalizeFirstLetter(simpleName);
		return (Class<? extends Crime<?>>) Class.forName(className);
	}
	
	public static class CrimeParams<T> {
		
	}
}
