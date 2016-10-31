package mg.fishchicken.gamelogic.characters;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Represents a player character role in the game.
 * 
 * This can be used to develop a story around a character more effectively.
 * 
 * @author Annun
 * 
 */
public class Role implements XMLLoadable, ThingWithId {

	private static ObjectMap<String, String> roles = new ObjectMap<String, String>();

	public static Role getRole(String id) {
		return Assets.get(roles.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns an array of all roles that can be selected by the player
	 * during character creation.
	 */
	public static Array<Role> getAllSelectableRoles() {
		Array<Role> returnValue = new Array<Role>();
		for (String rolePath : roles.values()) {
			Role role = Assets.get(rolePath);
			if (role.isSelectable()) {
				returnValue.add(role);
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
	public static void gatherRoles() throws IOException {
		Assets.gatherAssets(Configuration.getFolderRoles(), "xml", Role.class, roles);
	}
	
	public static final String XML_RACES = "races";
	public static final String XML_GENDERS = "genders";
	
	private String s_id;
	private String s_name;
	private String s_description;
	private String s_dialogueId;
	private boolean s_mandatory;
	private boolean s_memberAtStart;
	private boolean s_selectable;
	private Array<Race> races;
	private Array<Gender> genders;

	public Role(FileHandle file) throws IOException {
		s_id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		races = new Array<Race>(Race.class);
		genders = new Array<Gender>(Gender.class);
		loadFromXML(file);
	}
		
	public String getName() {
		return Strings.getString(s_name);
	}
	
	public String getDescription() {
		return Strings.getString(s_description);
	}
	
	public String getDialogueId() {
		return s_dialogueId;
	}
	
	/**
	 * Returns true if this role is mandatory, meaning a character with this role
	 * must be created by the player before a game can be started.
	 */
	public boolean isMandatory() {
		return s_mandatory;
	}
	
	/**
	 * Returns true if this role can be selected by the player during character creation.
	 */
	public boolean isSelectable() {
		return s_selectable;
	}

	
	/**
	 * Returns true if this role should be a member of the player character group
	 * from the very start, or if it will only join later. If it is the latter, 
	 * it is up to the game designer to place it on a map and assign a dialogue id 
	 * to it so that it can join the player. 
	 */
	public boolean isMemberAtStart() {
		return s_memberAtStart;
	}

	public String getId() {
		return s_id;
	}	
	
	@Override
	public String toString() {
		return s_id;
	}
	
	public Array<Gender> getGenders() {
		return new Array<Gender>(genders);
	}
	
	public Array<Race> getRaces() {
		return new Array<Race>(races);
	}

	@Override
	public void loadFromXML(FileHandle raceFile) throws IOException {
		this.races.clear();
		this.genders.clear();
		s_selectable = true;
		loadFromXMLNoInit(raceFile);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		XMLUtil.readPrimitiveMembers(this, root);
		
		Element racesElement = root.getChildByName(XML_RACES);
		if (racesElement != null) {
			String[] races = racesElement.getText().split(",");
			for (String race : races) {
				this.races.add(Race.getRace(race.trim()));
			}
		}
		
		Element gendersElement = root.getChildByName(XML_GENDERS);
		if (gendersElement != null) {
			String[] genders = gendersElement.getText().split(",");
			for (String gender: genders) {
				gender = gender.trim();
				for (Gender g : Gender.values()) {
					if (!this.genders.contains(g, true)
							&& g.name().toLowerCase(Locale.ENGLISH)
									.equals(gender)) {
						this.genders.add(g);
					}
				}
			}
		}
	}
}
