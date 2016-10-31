package mg.fishchicken.gamelogic.factions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * A faction is a group of characters that have opinions about other factions.
 * 
 * They can own items and locations and depending on their disposition (opinion)
 * about the other factions, they can be aggressive or friendly.
 * 
 * Technically faction is considered to be a "modifiable master data". This means
 * that all Factions are defined as master data in xml files and loaded into memory 
 * when the game starts, however, their attributes can be modified during the course 
 * of the game. If that happens, they are flagged as shouldBeSaved = true which then
 * includes these factions in the savegame file.
 * 
 * When the file is loaded, the factions in it are reloaded as well so that the data
 * from the savegame file overwrite the loaded masterdata.
 * 
 * @author ANNUN
 *
 */
public class Faction implements XMLSaveable, ThingWithId {
	
	public static final Faction NO_FACTION = new NoFaction();
	public static final Faction PLAYER_FACTION = new PlayerFaction();
	
	public static final String STRING_TABLE = "factions."+Strings.RESOURCE_FILE_EXTENSION;
	public static final String XML_DISPOSITION = "disposition";
	private static ObjectMap<String, String> factions = new ObjectMap<String, String>();
	
	private ObjectMap<String, Integer> disposition = new ObjectMap<String, Integer>();
	private boolean s_shouldBeSaved;
	private String s_id;
	private HashSet<AbstractGameCharacter> members = new HashSet<AbstractGameCharacter>();
	
	public static Faction getFaction(String id) {
		if (id == null) {
			return null;
		}
		if (PLAYER_FACTION.s_id.equals(id)) {
			return PLAYER_FACTION;
		} else if (NO_FACTION.s_id.equals(id)) {
			return NO_FACTION;
		}
		return Assets.get(factions.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Gathers all Factions and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherFactions() throws IOException {
		Assets.gatherAssets(Configuration.getFolderFactions(), "xml", Faction.class, factions);
	}
	
	public static void writeAllModifiedFactions(XmlWriter writer) throws IOException {
		for (String factionFile : factions.values()) {
			Faction faction = Assets.get(factionFile);
			if (faction.shouldBeSaved()) {
				writer.element(faction.s_id);
				faction.writeToXML(writer);
				writer.pop();
			}
		}
	}

	protected Faction(String id) {
		s_id = id;
	}
	
	public Faction(FileHandle file) throws IOException {
		loadFromXML(file);
		s_id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
	}
	
	public String getId() {
		return s_id;
	}
	
	/**
	 * Returns a human readable name of this Faction.
	 * @return
	 */
	public String getName() {
		return Strings.getString(STRING_TABLE, s_id);
	}
	
	protected boolean shouldBeSaved() {
		return s_shouldBeSaved;
	}
	
	/**
	 * Adds the supplied character to this Faction.
	 * 
	 * @param member
	 */
	public void addMember(AbstractGameCharacter member) {
		members.add(member);
	}
	
	/**
	 * Removes the supplied character from this faction.
	 */
	public void removeMember(AbstractGameCharacter member) {
		members.remove(member);
	}
	
	/**
	 * Sets the disposition of this Faction towards the supplied
	 * Faction to the supplied value. 
	 * 
	 * @param faction
	 * @param disposition
	 */
	public void setDisposition(Faction faction, int disposition) {
		setDisposition(faction.s_id, disposition);
	}
	
	/**
	 * Sets the disposition of this Faction towards the supplied
	 * Faction to the supplied value. 
	 * 
	 * @param faction
	 * @param disposition
	 */
	public void setDisposition(String factionId, int disposition) {
		if (Faction.NO_FACTION.getId().equals(factionId)) {
			return;
		}
		s_shouldBeSaved = true;
		if (Faction.PLAYER_FACTION.getId().equals(factionId)) {
			int currentDisposition = getDisposition(factionId);
			if (disposition < currentDisposition) {
				Log.logLocalized("dispositionLowered", LogType.FACTION, getName(), currentDisposition - disposition);
			} else if (disposition > currentDisposition) {
				Log.logLocalized("dispositionIncreased", LogType.FACTION, getName(), disposition - currentDisposition);
			}		
		}
		this.disposition.put(factionId, disposition);
	}
	
	/**
	 * Modifies the disposition of this Faction towards the supplied
	 * Faction by the supplied value. 
	 * 
	 * @param factionId
	 * @param disposition
	 */
	public void modifyDisposition(String factionId, int modifier) {
		setDisposition(factionId, getDisposition(factionId)+modifier);
	}
	
	/**
	 * Modifies the disposition of this Faction towards the supplied
	 * Faction by the supplied value. 
	 * 
	 * @param faction
	 * @param disposition
	 */
	public void modifyDisposition(Faction faction, int modifier) {
		modifyDisposition(faction.s_id, modifier);
	}
	
	/**
	 * Returns the disposition of this Faction towards the supplied
	 * Faction. 
	 * 
	 * @param faction
	 */
	public int getDispositionTowards(Faction faction) {
		return getDisposition(faction.s_id);
	}
	
	/**
	 * Returns the disposition of this Faction towards the supplied
	 * Faction. 
	 * 
	 * @param factionId
	 */
	public int getDisposition(String factionId) {
		if (getId().equals(factionId)) {
			return 100;
		}
		Integer currentDisposition = disposition.get(factionId.toLowerCase(Locale.ENGLISH));
		if (currentDisposition == null) {
			currentDisposition = 0;
		}
		return currentDisposition;
	}
	
	/**
	 * Returns the disposition of this Faction towards the Player
	 * Faction. 
	 * 
	 * @param faction
	 */
	public int getDispositionTowardsPlayer() {
		return getDispositionTowards(PLAYER_FACTION);
	}
	
	/**
	 * Returns the disposition of this Faction towards the Player
	 * Faction as a human readable string. 
	 * 
	 * @param faction
	 */
	public String getDispositionTowardsPlayerAsString() {
		int dis = getDispositionTowards(PLAYER_FACTION);
		if (dis >= 100) {
			return Strings.getString(Faction.STRING_TABLE, "Loves");
		} else if (dis >= 75) {
			return Strings.getString(Faction.STRING_TABLE, "Admires");
		} else if (dis >= 50) {
			return Strings.getString(Faction.STRING_TABLE, "Likes");
		} else if (dis >= 25) {
			return Strings.getString(Faction.STRING_TABLE, "Knows");
		} else if (dis > -25) {
			return Strings.getString(Faction.STRING_TABLE, "Neutral");
		} else if (dis > -50) {
			return Strings.getString(Faction.STRING_TABLE, "Suspicious");
		} else if (dis > -75) {
			return Strings.getString(Faction.STRING_TABLE, "Dislikes");
		} else if (dis > -100) {
			return Strings.getString(Faction.STRING_TABLE, "Reviles");
		}
		return Strings.getString(Faction.STRING_TABLE, "Hostile");
	}

	/**
	 * Modifies the disposition of this Faction towards the Player Faction
	 * by the supplied value.
	 * 
	 * @param modifier
	 */
	public void modifyDispositionTowardsPlayer(int modifier) {
		modifyDisposition(PLAYER_FACTION, modifier);
	}
	
	/**
	 * Returns the disposition of this Faction towards the supplied character.
	 * 
	 * If the character does not belong to a Faction, 0 is always returned.
	 * @param character
	 * @return
	 */
	public int getDispositionTowards(FactionContainer character) {
		return getDispositionTowards(character.getFaction());
	}
	
	/**
	 * Returns true if this Faction is allied with the supplied
	 * character. 
	 * 
	 * @param character
	 */
	public boolean isAlliedWith(FactionContainer character) {
		return getDispositionTowards(character) >= 100; 
	}
	
	/**
	 * Returns true if this Faction is hostile towards the supplied
	 * character. 
	 * 
	 * @param character
	 */
	public boolean isHostileTowards(FactionContainer character) {
		return isHostileTowards(character.getFaction()); 
	}
	
	/**
	 * Returns true if this Faction is hostile towards the supplied
	 * faction. 
	 * 
	 * @param faction
	 */
	public boolean isHostileTowards(Faction faction) {
		return getDispositionTowards(faction) <= -100; 
	}
	
	/**
	 * Returns true if this Faction is hostile towards the Player Faction. 
	 * 
	 * @param character
	 */
	public boolean isHostileTowardsPlayer() {
		return getDispositionTowardsPlayer() <= -100;
	}
	
	public void loadFromXML(FileHandle factionFile) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(factionFile);
		loadFromXML(root);
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XMLUtil.XML_PROPERTIES);
		XMLUtil.writePrimitives(this, writer);
		writer.pop();
		
		writer.element(XML_DISPOSITION);
		for (String factionId : disposition.keys()) {
			writer.element(factionId, disposition.get(factionId));
		}
		writer.pop();
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this,
				root.getChildByName(XMLUtil.XML_PROPERTIES));
		
		disposition.clear();
		Element dispositionElement = root.getChildByName(XML_DISPOSITION);
		for (int i = 0; i < dispositionElement.getChildCount(); ++i) {
			Element factionElement = dispositionElement.getChild(i);
			disposition.put(factionElement.getName().toLowerCase(Locale.ENGLISH), Integer.parseInt(factionElement.getText()));
		}
	}
	
	@Override
	public String toString() {
		return s_id;
	}
	
	/**
	 * Returns true if either of the supplied faction containers
	 * is hostile to the other one.
	 * @param f1
	 * @param f2
	 * @return
	 */
	public static boolean areHostile(FactionContainer f1, FactionContainer f2) {
		return f1.isHostileTowards(f2.getFaction()) || f2.isHostileTowards(f1.getFaction());
	}
	
	/**
	 * Returns true if either of the supplied factions
	 * is hostile to the other one.
	 * @param f1
	 * @param f2
	 * @return
	 */
	public static boolean areHostile(Faction f1, Faction f2) {
		return f1.isHostileTowards(f2) || f2.isHostileTowards(f1);
	}

}
