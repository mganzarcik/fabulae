package mg.fishchicken.core.saveload;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.quests.Quest;
import mg.fishchicken.gamelogic.weather.WeatherManager;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.saveload.SaveGameDetails;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.XmlWriter;

public class GameSaver {

	public static final String XML_SAVE_GAME = "saveGame";
	public static final String XML_GLOBAL = "global";
	public static final String XML_PLAYER_CHARACTER_GROUP = "playerCharacterGroup";
	public static final String XML_CRIME = "crime";
	public static final String XML_GAME_OBJECTS = "gameObjects";
	public static final String XML_LOCATIONS = "locations";
	public static final String XML_FACTIONS = "factions";
	public static final String XML_WEATHER = "weather";
	public static final String XML_QUESTS = "quests";
	public static final String XML_MAP = "map";
	public static final String XML_ATTRIBUTE_GOID = "goId";
	public static final String XML_ATTRIBUTE_GAME_TIME = "gameTime";
	public static final String XML_ATTRIBUTE_CURRENT_MAP = "currrentMap";
	public static final String XML_ATTRIBUTE_CAMERA_POSITION_X = "cameraPositionX";
	public static final String XML_ATTRIBUTE_CAMERA_POSITION_Y = "cameraPositionY";
	
	private WeatherManager weatherManager;
	private Random random;
	private ObjectMap<String, GameLocation> locations;
	private ObjectMap<String, GameObject> unassignedLocalGameObjects;
	private GameState gameState;
	
	public GameSaver(GameState gameState, WeatherManager weatherManager, Random random, ObjectMap<String, GameLocation> locations, ObjectMap<String, GameObject> unassignedLocalGameObjects) {
		this.gameState = gameState;
		this.weatherManager = weatherManager;
		this.random = random;
		this.locations = locations;
		this.unassignedLocalGameObjects = unassignedLocalGameObjects;
	}
	
	public void saveGame(SaveGameDetails saveGameDetails) {
		FileHandle saveGameFile = GameLoader.getSaveGameFile(saveGameDetails.getId());
		FileHandle tempSaveGameFile = GameLoader.getSaveGameFile("temp"+saveGameDetails.getId());
		if (!tempSaveGameFile.exists()) {
			tempSaveGameFile.parent().mkdirs();
		}
		
		FileHandle saveGameDetailsFile = GameLoader.getSaveGameDetailsFile(saveGameDetails.getId());
		FileHandle tempDetailsFile = GameLoader.getSaveGameDetailsFile("temp"+saveGameDetails.getId());
		OutputStream detailsOutputStream = tempDetailsFile.write(false);
		Writer detailsWriter = new OutputStreamWriter(detailsOutputStream, Charset.forName("UTF-8"));
		
		OutputStream outputStream =  tempSaveGameFile.write(false);
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		ObjectOutputStream oos = null;
		Throwable exception = null;
		
		try {
			
			detailsWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			XmlWriter xml = new XmlWriter(detailsWriter);
			saveGameDetails.writeToXML(xml);
			xml.flush();
			
			ZipEntry entry = new ZipEntry("savegame");
			zipOutputStream.putNextEntry(entry);
			Writer writer = new OutputStreamWriter(zipOutputStream, Charset.forName("UTF-8"));
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			xml = new XmlWriter(writer);
			xml.element(XML_SAVE_GAME);
			
			writeGlobalToXML(gameState, xml);
			writeWeatherToXML(xml, weatherManager);
			writeFactionsToXML(xml);
			writeQuestsToXML(xml);
			writeAllLocationsToXML(xml, locations);
			writeAllGameObjectsToXML(xml);
			xml.element(XML_PLAYER_CHARACTER_GROUP);
			GameState.getPlayerCharacterGroup().writeToXML(xml);
			xml.pop();
			UIManager.writeToXML(xml);
			
			xml.element(XML_CRIME);
			gameState.getCrimeManager().writeToXML(xml);
			xml.pop();
			
			xml.pop();
			xml.flush();
			
			zipOutputStream.putNextEntry(new ZipEntry("random"));
			oos = new ObjectOutputStream(zipOutputStream);
			oos.writeObject(random);
			zipOutputStream.closeEntry();
		} catch (IOException e) {
			exception = e;
			throw new GdxRuntimeException(e);
		} catch (Throwable t) {
			exception = t;
			throw t;
		} finally {
			StreamUtils.closeQuietly(zipOutputStream);
			StreamUtils.closeQuietly(outputStream);
			StreamUtils.closeQuietly(detailsOutputStream);
			if (oos != null) {
				StreamUtils.closeQuietly(oos);
			}
			if (exception == null) {
				tempSaveGameFile.moveTo(saveGameFile);
				tempDetailsFile.moveTo(saveGameDetailsFile);
			} else {
				tempDetailsFile.delete();
				tempSaveGameFile.delete();
			}
		}
		Log.logLocalized(UIManager.STRING_TABLE, "gameSaved", LogType.INFO);
	}
	
	private static void writeGlobalToXML(GameState gameState, XmlWriter writer) throws IOException {
		writer.element(XML_GLOBAL);
		writer.element(XMLUtil.XML_PROPERTIES);
		GameMap currentMap = gameState.getCurrentMap();
		
		if (currentMap != null) {
			writer.attribute(XML_ATTRIBUTE_CURRENT_MAP, currentMap.getId());
			Vector2 tempVector = MathUtil.getVector2().set(currentMap.getCamera().position.x, currentMap.getCamera().position.y);
			currentMap.projectToTiles(tempVector);
			writer.attribute(XML_ATTRIBUTE_CAMERA_POSITION_X, tempVector.x);
			writer.attribute(XML_ATTRIBUTE_CAMERA_POSITION_Y, tempVector.y);
			MathUtil.freeVector2(tempVector);
		} 
		
		writer.attribute(XML_ATTRIBUTE_GOID, gameState.getCurrentId());
		writer.pop();
		
		writer.element(XML_ATTRIBUTE_GAME_TIME);
		GameState.getCurrentGameDate().writeToXML(writer);
		writer.pop();
		
		gameState.variables().writeToXML(writer);
		writer.pop();
	}

	
	private static void writeAllLocationsToXML(XmlWriter writer, ObjectMap<String, GameLocation> locations) throws IOException {
		for (GameLocation loc : locations.values()) {
			if (loc instanceof GameMap) {
				writer.element(XML_LOCATIONS).attribute(XML_MAP, loc.getId());
				writer.element(loc.getClass().getName());
				loc.writeToXML(writer);
				writer.pop();
				((GameMap) loc).writeAllLocationsToXML(writer);
				writer.pop();
			}
		}
	}
	
	
	private static void writeFactionsToXML(XmlWriter writer) throws IOException {
		writer.element(XML_FACTIONS);
		Faction.writeAllModifiedFactions(writer);
		writer.pop();
	}
	


	private static void writeWeatherToXML(XmlWriter writer, WeatherManager weatherManager) throws IOException {
		writer.element(XML_WEATHER);
		weatherManager.writeToXML(writer);;
		writer.pop();
	}
	

	
	private static void writeQuestsToXML(XmlWriter writer) throws IOException {
		writer.element(XML_QUESTS);
		Quest.writeAllStartedQuests(writer);
		writer.pop();
	}
	

	
	private void writeAllGameObjectsToXML(XmlWriter writer) throws IOException {
		for (GameLocation loc : locations.values()) {
			if (loc instanceof GameMap) {
				writer.element(XML_GAME_OBJECTS).attribute(XML_MAP, loc.getId());
				((GameMap) loc).writeAllGameObjectsToXML(writer);
				writer.pop();
			}
		}
		if (unassignedLocalGameObjects.size > 0) {
			writer.element(XML_GAME_OBJECTS);
			for (GameObject go : unassignedLocalGameObjects.values()) {
				if (!go.shouldBeSaved()) {
					 continue;
				 }
				 writer.element(go.getClass().getName());
				 go.writeToXML(writer);
				 writer.pop();
			}
			writer.pop();
		}
	}
}
