package mg.fishchicken.core.saveload;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.quests.Quest;
import mg.fishchicken.gamelogic.weather.WeatherManager;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.dialog.OkCancelCallback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class GameLoader {
	
	private GameState gameState;
	private WeatherManager weatherManager;
	private ObjectMap<String, GameMap> mapsById;
	private ObjectMap<GameMap, Array<GameObject>> gosToMaps;
	private boolean isLoadingGame;
	private FishchickenGame game;
	
	public GameLoader(GameState global, FishchickenGame game,
			WeatherManager weatherManager,
			ObjectMap<String, GameMap> locations) {
		this.weatherManager = weatherManager;
		this.mapsById = locations;
		this.game = game;
		this.gameState = global;
		this.gosToMaps = new ObjectMap<GameMap, Array<GameObject>>();
	}
	
	public static FileHandle getSaveGameFile(String slot) {
		return Gdx.files.local(Configuration.getFolderSaveGames()+slot+".sav");
	}
	public static FileHandle getSaveGameDetailsFile(String slot) {
		return Gdx.files.local(Configuration.getFolderSaveGames()+slot+".xml");
	}
	
	/**
	 * Loads the game from the supplied slot. Returns true if the game was loaded successfully,
	 * false otherwise.
	 * 
	 * @param slot
	 * @return
	 */
	public void loadGame(final String slot, final OkCancelCallback<GameMap> callback) {
		
		isLoadingGame = true;
		gosToMaps.clear(); 
		FileHandle saveGameFile = getSaveGameFile(slot);
		
		InputStream is = saveGameFile.read();
		ZipInputStream zis = new ZipInputStream(is);
		try {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				if ("random".equals(zipEntry.getName())) {
					gameState.readRandomGenerator(new ObjectInputStream(zis));
				} else if ("savegame".equals(zipEntry.getName())) {
					final Element root = XMLUtil.parseNonCLosing(new XmlReader(), zis);
					if (root == null || !GameSaver.XML_SAVE_GAME.equals(root.getName())) {
						break;
					}
					unloadGame();
					readFactionsFromXML(root);
					readQuestsFromXML(root);
					readAllLocationsFromXML(root);
					readGlobalFromXLM(root);
					readWeatherFromXML(root);
					
					UIManager.loadFromXML(root);
					
					game.switchToMapAfterLoading(gameState.getCurrentMap().getId(), new LoadedCallback() {

						@Override
						public void finishedLoading(AssetManager assetManager, String fileName, @SuppressWarnings("rawtypes") Class type) {
							try {
								readAllGameObjectsFromXML(root);
								GameState.getPlayerCharacterGroup().loadFromXML(root.getChildByName(GameSaver.XML_PLAYER_CHARACTER_GROUP));
								setMapOnGameObjects();
								gameState.getCrimeManager().loadFromXML(root.getChildByName(GameSaver.XML_CRIME));
								if (callback != null) {
									callback.onOk(gameState.getCurrentMap());
								}
							} catch(Throwable e) {
								error(e, slot, callback);
							} finally {
								isLoadingGame = false;
							}	
						}
					});
					zis.closeEntry();
				}
				
				zipEntry = zis.getNextEntry();
			}
		} catch(Throwable e) {
			error(e, slot, callback);
		} finally {
			StreamUtils.closeQuietly(zis);
		}
	}
	
	private void error(Throwable e, String slot, OkCancelCallback<GameMap> callback) {
		String errorMessage = "Error loading game "+slot+": "+e.getMessage();
		Log.log(errorMessage, LogType.ERROR);
		e.printStackTrace();
		if (callback != null) {
			callback.onError(errorMessage);
		}
	}
	
	/**
	 * Returns true if loading game from a savegame is currently in progress.
	 * 
	 * @return
	 */
	public boolean isLoadingGame() {
		return isLoadingGame;
	}
	
	public void unloadGame() {
		gameState.endCombat();
		gameState.setCurrentMap(null);
		gameState.clearGameObjects();
		weatherManager.stop();
		
		for (GameMap map : mapsById.values()) {
			map.unload();
		}
		
		gameState.clearLocations();
		
		gameState.getPlayerCharacterController().reset();
		GameState.getPlayerCharacterGroup().unload();
		
		Quest.resetAllQuests();
		Faction.resetAllFactions();
		
		UIManager.resetUI();
		Music.stopPlayingMusic();
		gameState.getCrimeManager().reset();
		gameState.resetGameDate();
		gameState.variables().clear();
	}
	
	private void setMapOnGameObjects() {
		for (Entry<GameMap, Array<GameObject>> entry : gosToMaps.entries()) {
			GameMap map = entry.key;
			for (int i = 0; i < entry.value.size; ++i) {
				entry.value.get(i).setMap(map);
			}
		}
	}
	
	private void readAllGameObjectsFromXML(Element root) {
		Array<Element> gosElements = root.getChildrenByName(GameSaver.XML_GAME_OBJECTS);
		for (Element gosElement : gosElements) {
			String mapId = gosElement.getAttribute(GameSaver.XML_MAP, null);
			GameMap map = mapId == null ? null : mapsById.get(mapId);
			Array<GameObject> gosForMap = new Array<GameObject>();
			if (map != null) {
				gosToMaps.put(map, gosForMap);
			}
			for (int i = 0; i < gosElement.getChildCount(); ++i) {
				Element goElement = gosElement.getChild(i);
				GameObject go = (GameObject) createFromXML(gameState, goElement);
				gosForMap.add(go);
			}
		}
	}
	
	private static void readQuestsFromXML(Element root) throws IOException {
		Element questsElement = root.getChildByName(GameSaver.XML_QUESTS);
		for (int i = 0; i < questsElement.getChildCount(); ++i) {
			Element questElement = questsElement.getChild(i);
			Quest quest = Quest.getQuest(questElement.getName());
			quest.loadFromXML(questElement);
		}
	}
	
	private void readWeatherFromXML(Element root) throws IOException {
		Element weatherElement = root.getChildByName(GameSaver.XML_WEATHER);
		if (weatherElement != null) {
			weatherManager.loadFromXML(weatherElement);
		}
	}

	private static void readFactionsFromXML(Element root) throws IOException {
		Element factionsElement = root.getChildByName(GameSaver.XML_FACTIONS);
		for (int i = 0; i < factionsElement.getChildCount(); ++i) {
			Element factionElement = factionsElement.getChild(i);
			Faction faction = Faction.getFaction(factionElement.getName());
			faction.loadFromXML(factionElement);
		}
	}

	private void readAllLocationsFromXML(Element root) throws IOException {
		Array<Element> locationsElements = root.getChildrenByName(GameSaver.XML_LOCATIONS);
		for (Element locationsElement : locationsElements) {
			String mapId = locationsElement.getAttribute(GameSaver.XML_MAP, null);
			if (mapId == null) {
				continue;
			}
			GameMap map = null;
			for (int i = 0; i < locationsElement.getChildCount(); ++i) {
				Element locElement = locationsElement.getChild(i);
				GameLocation loc = (GameLocation) createFromXML(gameState, locElement);
				// the first location is the map itself, the others are those that belong to it
				// its okay if this dies on a class cast, since if this is not true, then everything is terrible
				if (i == 0) {
					map = (GameMap) loc;
				} else {
					loc.setMap(map);
					// load any "master data" from the xml file
					loc.loadFromXML(Gdx.files.internal(Configuration
							.getFolderLocations() + loc.getType() + ".xml"));
					// and then reload it from the savegame to override any changes
					// TODO this currently means the savegame element is read twice, this should be optimized
					loc.loadFromXML(locElement);
				}
				gameState.addLocation(loc);
			}
		}
	}
	
	private void readGlobalFromXLM(Element root) throws IOException {
		Element globalElement = root.getChildByName(GameSaver.XML_GLOBAL);
		Element propsElement = globalElement.getChildByName(XMLUtil.XML_PROPERTIES);
		
		gameState.setCurrentMap(mapsById.get(propsElement.getAttribute(GameSaver.XML_ATTRIBUTE_CURRENT_MAP)));
		gameState.setIdCounter(propsElement.getInt(GameSaver.XML_ATTRIBUTE_GOID));
		
		GameState.getCurrentGameDate().readFromXML(globalElement.getChildByName(GameSaver.XML_ATTRIBUTE_GAME_TIME));
		gameState.getCurrentMap().setStartCoordinates(
				propsElement.getFloat(GameSaver.XML_ATTRIBUTE_CAMERA_POSITION_X),
				propsElement.getFloat(GameSaver.XML_ATTRIBUTE_CAMERA_POSITION_Y));
		
		gameState.variables().loadFromXML(globalElement);
	}
	
	@SuppressWarnings("unchecked")
	private static XMLSaveable createFromXML(GameState gameState, Element saveableElement) {
		XMLSaveable returnValue = null;
		
		String implementationClassName = saveableElement.getName();
		try {
			Class<? extends XMLSaveable> saveableClass = (Class<? extends XMLSaveable>) Class.forName(implementationClassName);
			returnValue = saveableClass.newInstance();
			returnValue.loadFromXML(saveableElement);
			
		} catch (ClassNotFoundException e) {
			throw new GdxRuntimeException(e);
		} catch (InstantiationException e) {
			throw new GdxRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new GdxRuntimeException(e);
		} catch (SecurityException e) {
			throw new GdxRuntimeException(e);
		}

		return returnValue;
	}
}
