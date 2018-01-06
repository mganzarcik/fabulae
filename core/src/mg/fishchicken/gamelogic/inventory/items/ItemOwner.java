package mg.fishchicken.gamelogic.inventory.items;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamestate.ObservableState;

public class ItemOwner extends ObservableState<ItemOwner, ItemOwner.ItemOwnerParam> {
	
	public static final String XML_OWNER = "owner";
	
	private Faction s_ownerFaction;
	private String s_ownerCharacterId;
	private boolean s_isFixed;
	private ItemOwnerParam changeParam;
	private String ownerCharacterName; // lazy
	
	public ItemOwner() {
		changeParam = new ItemOwnerParam();
	}
	
	public ItemOwner(ItemOwner ownerToCopy) {
		this();
		set(ownerToCopy);
	}
	
	public boolean isFixed() {
		return s_isFixed;
	}
	
	public void clear() {
		set(null, null, false);
	}
	
	public void set(ItemOwner owner) {
		set(owner.s_ownerCharacterId, owner.s_ownerFaction, owner.s_isFixed);
	}
	
	public void set(String characterId, Faction faction, boolean isFixed) {
		if (characterId != null) {
			characterId = characterId.toLowerCase(Locale.ENGLISH);
		}
		changeParam.prevOwnerCharacterId = s_ownerCharacterId;
		changeParam.prevOwnerFaction = s_ownerFaction;
		changeParam.prevFixed = s_isFixed;
		if (!CoreUtil.equals(s_ownerCharacterId, characterId)) {
			ownerCharacterName = null;
		}
		s_ownerCharacterId = characterId;
		s_ownerFaction = faction;
		s_isFixed = isFixed;
		changed(changeParam);
	}
	
	public String getOwnerCharacterId() {
		return s_ownerCharacterId;
	}
	
	public void setOwnerCharacterId(String id) {
		set(id, s_ownerFaction, s_isFixed);
	}

	public Faction getOwnerFaction() {
		return s_ownerFaction;
	}

	public void setOwnerFaction(Faction ownerFaction) {
		set(s_ownerCharacterId, ownerFaction, s_isFixed);
	}
	
	/**
	 * Returns true if this item owner includes the supplied character.
	 *  
	 * @param character
	 * @return
	 */
	public boolean includes(GameCharacter character) {
		if (isEmpty()) {
			return true;
		}
		if (s_ownerCharacterId != null && s_ownerCharacterId.equalsIgnoreCase(character.getId())) {
			return true;
		}
		return CoreUtil.equals(s_ownerFaction,
				character.getFaction());
	}
	
	/**
	 * Returns true if this item owner includes the supplied faction.
	 *  
	 * @param character
	 * @return
	 */
	public boolean includes(Faction faction) {
		if (s_ownerCharacterId != null) {
			return false;
		} else if (isEmpty()) {
			return true;
		}
		return CoreUtil.equals(s_ownerFaction,
				faction);
	}
	
	/**
	 * Owners are considered equal if their their character ids are both not null and equal,
	 * or if they are both null and their factions are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ItemOwner) {
			ItemOwner secondItem = (ItemOwner) obj;
			if (s_ownerCharacterId != null || secondItem.s_ownerCharacterId != null) {
				return CoreUtil.equals(s_ownerCharacterId,
						((ItemOwner) obj).s_ownerCharacterId);
			}
			return CoreUtil.equals(s_ownerFaction,
							((ItemOwner) obj).getOwnerFaction());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (s_ownerCharacterId != null) {
			return s_ownerCharacterId.hashCode();
		}
		if (s_ownerFaction != null) {
			return s_ownerFaction.getId().hashCode();
		}
		return super.hashCode();
	}
	
	private String getOwnerCharacterName() {
		if (ownerCharacterName == null && s_ownerCharacterId != null) {
			try {
				GameObject go = GameState.getGameObjectById(s_ownerCharacterId);
				if (go instanceof GameCharacter) {
					ownerCharacterName =  ((GameCharacter) go).getName();
				} else {
					XmlReader xmlReader = new XmlReader();
					Element root = xmlReader.parse(Gdx.files
							.internal(Configuration.getFolderCharacters()
									+ s_ownerCharacterId + ".xml"));
					ownerCharacterName = root.getChildByName(
							XMLUtil.XML_PROPERTIES).get(
							XMLUtil.XML_ATTRIBUTE_NAME);
				}
			} catch (SerializationException e) {
				throw new GdxRuntimeException("Could not determine the owner with type "+s_ownerCharacterId, e);
			}
			if (ownerCharacterName == null) {
				throw new GdxRuntimeException("Could not determine the owner with type "+s_ownerCharacterId);
			}
		}
		return Strings.getString(ownerCharacterName);
	}
	
	public boolean isEmpty() {
		return s_ownerCharacterId == null && s_ownerFaction == null;
	}
	
	public String toUIString() {
		if (s_ownerCharacterId != null) {
			return getOwnerCharacterName();
		}
		if (s_ownerFaction != null) {
			return s_ownerFaction.getName();
		}
		return "";
	}

	public static class ItemOwnerParam {
		private String prevOwnerCharacterId;
		private Faction prevOwnerFaction;
		private boolean prevFixed;
		
		public String getPrevOwnerCharacterId() {
			return prevOwnerCharacterId;
		}
		
		public Faction getPrevOwnerFaction() {
			return prevOwnerFaction;
		}
		
		public boolean getPrevFixed() {
			return prevFixed;
		}
	}

}
