package mg.fishchicken.ui.saveload;

import java.io.IOException;
import java.util.Date;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class SaveGameDetails implements XMLSaveable, ThingWithId {
	
	public static final String QUICK_SAVE = "quick";

	public static final String XML_DETAILS =  "saveGameDetails";
	public static final String XML_SAVED_AT = "savedAt";
	public static final String XML_GAME_DATE = "gameDate";
	public static final String XML_CHARACTERS = "characters";
	public static final String XML_CHARACTER = "character";
	
	private Date savedAt;
	private String s_gameDate;
	private String s_name;
	private String s_currentMap;
	private Array<String> groupMembers;
	private String id;
	
	public SaveGameDetails(String id, String name, GameState gameState) {
		this.s_name = name;
		this.id = id;
		s_currentMap = gameState.getCurrentMap() != null ? gameState.getCurrentMap().getId() : null;
		savedAt = new Date();
		s_gameDate = GameState.getCurrentGameDate().toString(true);
		groupMembers = new Array<String>();
		for (GameCharacter member : GameState.getPlayerCharacterGroup().getMembers()) {
			groupMembers.add(member.getName());
		}
	}
	
	public SaveGameDetails(FileHandle file) throws IOException {
		id = file.nameWithoutExtension();
		loadFromXML(new XmlReader().parse(file));
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return s_name;
	}
	
	public String getCurrentMapName() {
		return s_currentMap != null ? Strings.getString(GameMap.STRING_TABLE, s_currentMap) : "";
	}
	
	public Date getSavedAt() {
		return savedAt;
	}
	
	public String getGameDate() {
		return s_gameDate;
	}
	
	public Array<String> getCharacters() {
		return groupMembers;
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_DETAILS);
		XMLUtil.writePrimitives(this, writer);
		writer.element(XML_SAVED_AT, savedAt.getTime());
		writer.element(XML_CHARACTERS);
		for (String charName : groupMembers) {
			writer.element(XML_CHARACTER, charName);
		}
		writer.pop();
		writer.pop();
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
		savedAt = new Date(Long.parseLong(root.get(XML_SAVED_AT)));
		groupMembers = new Array<String>();
		Element characters = root.getChildByName(XML_CHARACTERS);
		for (int i = 0; i < characters.getChildCount(); ++i) {
			groupMembers.add(characters.getChild(i).getText());
		}
	}
	
	

}
