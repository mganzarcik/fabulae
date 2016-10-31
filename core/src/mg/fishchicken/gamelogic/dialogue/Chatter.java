package mg.fishchicken.gamelogic.dialogue;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.locations.GameLocation;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Represents a set of idle texts that can be said by an NPC
 * as a floating text.
 * 
 * @author Annun
 *
 */
public class Chatter implements XMLLoadable {
	
	private static final String NO_LOCATION_ID = "__nolocation";
	
	public static final String XML_LOCATION = "location";
	public static final String XML_CHECK_FREQUENCY = "checkFrequency";
	public static final String XML_CHANCE_TO_SAY = "chanceToSay";
	
	public static enum ChatterType {
		GENERAL,
		CRIME_SPOTTED {
			@Override
			public String getXmlName() {
				return "crimeSpotted";
			}
		},
		COMBAT_STARTED {
			@Override
			public String getXmlName() {
				return "combatStarted";
			}
		};
		
		public String getXmlName() {
			return this.name().toLowerCase(Locale.ENGLISH);
		}
	}
	
	public static final String XML_TEXT = "text";
	private static ObjectMap<String, String> chatters = new ObjectMap<String, String>();
	
	private ObjectMap<ChatterType, ObjectMap<String, Float>> s_checkFrequency;
	private ObjectMap<ChatterType, ObjectMap<String, Integer>> s_chanceToSay;
	private ObjectMap<ChatterType, ObjectMap<String, Array<String>>> texts;
	private String id;
	
	/**
	 * Gets a Chatter with the supplied ID. 
	 * @param id
	 * @return
	 */
	public static Chatter getChatter(String id) {
		if (StringUtil.nullOrEmptyString(id)) {
			return new Chatter();
		}
		return Assets.get(chatters.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Gathers all Chatters and registers them in the AssetManager
	 * so that they can be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherChatters() throws IOException {
		Assets.gatherAssets(Configuration.getFolderChatter(), "xml", Chatter.class, chatters);
	}

	public Chatter(FileHandle file) throws IOException {
		id = null;
		id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		loadFromXML(file);
	}
	
	public Chatter() {
		init();
	}
	
	private void init() {
		texts = new ObjectMap<ChatterType, ObjectMap<String, Array<String>>>();
		s_checkFrequency = new ObjectMap<ChatterType, ObjectMap<String, Float>>();
		s_chanceToSay = new ObjectMap<ChatterType, ObjectMap<String, Integer>>();
		for (ChatterType type : ChatterType.values()) {
			ObjectMap<String, Array<String>> locTexts = new ObjectMap<String, Array<String>>();
			locTexts.put(NO_LOCATION_ID, new Array<String>());
			texts.put(type, locTexts);
			
			ObjectMap<String, Float> locFrequency = new ObjectMap<String, Float>();
			locFrequency.put(NO_LOCATION_ID, 0f);
			s_checkFrequency.put(type, locFrequency);
			
			ObjectMap<String, Integer> locChance = new ObjectMap<String, Integer>();
			locChance.put(NO_LOCATION_ID, 0);
			s_chanceToSay.put(type, locChance);	
		}
	}

	/**
	 * Returns how many seconds must pass before the system checks again if a
	 * chatter should be displayed.
	 * 
	 * @param type
	 * @param locations
	 * @return
	 */
	public float getCheckFrequency(ChatterType type, ObjectSet<GameLocation> locations) {
		ObjectMap<String, Float> locationFrequencies = s_checkFrequency.get(type);

		if (locations != null && locationFrequencies.size > 1) {
			for (GameLocation location : locations) {
				Float returnValue = locationFrequencies.get(location.getId());
				if (returnValue != null) {
					return returnValue;
				}
			}
		}
		
		return locationFrequencies.get(NO_LOCATION_ID);
	}

	
	public int getChanceToSay(ChatterType type, ObjectSet<GameLocation> locations) {
		ObjectMap<String, Integer> locationChances = s_chanceToSay.get(type);

		if (locations != null && locationChances.size > 1) {
			for (GameLocation location : locations) {
				Integer returnValue = locationChances.get(location.getId());
				if (returnValue != null) {
					return returnValue;
				}
			}
		}
		
		return locationChances.get(NO_LOCATION_ID);
	}

	public Array<String> getTexts(ChatterType type, ObjectSet<GameLocation> locations) {
		ObjectMap<String, Array<String>> locationTexts = texts.get(type);

		if (locations != null && locationTexts.size > 1) {
			for (GameLocation location : locations) {
				Array<String> returnValue = locationTexts.get(location.getId());
				if (returnValue != null) {
					return returnValue;
				}
			}
		}
		
		return locationTexts.get(NO_LOCATION_ID);
	}

	@Override
	public String toString() {
		return id != null ? id : "";
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		init();
		loadFromXMLNoInit(file);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		for (ChatterType type : ChatterType.values()) {
			Array<Element> typeElements = root.getChildrenByName(type.getXmlName());
			
			for (Element typeElement : typeElements) {
				ObjectMap<String, Array<String>> locationTexts = texts.get(type);
				ObjectMap<String, Integer> locationChances = s_chanceToSay.get(type);
				ObjectMap<String, Float> locationFrequencies = s_checkFrequency.get(type);
				
				String location = typeElement.get(XML_LOCATION, NO_LOCATION_ID).toLowerCase(Locale.ENGLISH);
				locationChances.put(location, typeElement.getInt(XML_CHANCE_TO_SAY, 0));
				locationFrequencies.put(location, typeElement.getFloat(XML_CHECK_FREQUENCY, 0f));
				Array<String> textArray = locationTexts.get(location);
				if (textArray == null) {
					textArray = new Array<String>();
					locationTexts.put(location, textArray);
				}
				
				Array<Element> textElements = typeElement.getChildrenByName(XML_TEXT);
				for (Element textElement : textElements) {
					textArray.add(textElement.getText());
				}
			}
		}
	}

}
