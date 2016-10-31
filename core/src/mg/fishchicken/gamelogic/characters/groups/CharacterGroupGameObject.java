package mg.fishchicken.gamelogic.characters.groups;

import java.io.IOException;

import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class CharacterGroupGameObject extends AbstractGameCharacter implements XMLLoadable {
	
	private CharacterGroup group;

	public CharacterGroupGameObject() {
		super();
	}
	
	public CharacterGroupGameObject(String id, FileHandle file, GameMap map) throws IOException {
		super(id, file.nameWithoutExtension());
		group = new CharacterGroup(file);
		loadFromXML(file);
		setMap(map);
	}
	
	public CharacterGroup getGroup() {
		return group;
	}

	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		loadFromXML(root);
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		if (group == null) {
			group = new CharacterGroup();
			group.loadFromXML(root);
		}
		// reset the speed since group was null at the time of loading and therefore
		// speed was set to 0 initially
		animations.setSpeed(getSpeed());
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		if (group != null) {
			group.writeToXML(writer);
		}
	}

	@Override
	public String getDialogueId() {
		return group.getGroupLeader().getDialogueId();
	}

	@Override
	public GameCharacter getRepresentative() {
		return group.getGroupLeader();
	}

	@Override
	public boolean isActive() {
		if (group != null) {
			return group.hasActiveMembers();
		}
		return false;
	}
	
	@Override
	public boolean isAsleep() {
		Array<GameCharacter> members = group.getMembers();
		for (int i = 0; i < members.size; ++i) {
			if (!members.get(0).isAsleep()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean shouldBeSaved() {
		return isActive();
	}
	
	@Override
	public boolean shouldDraw(Rectangle cullingRectangle) {
		return super.shouldDraw(cullingRectangle) && isActive();
	}
	
	@Override
	public boolean shouldRenderDestination() {
		return false;
	}

	@Override
	public float getSpeed() {
		if (group == null || group.getSize() < 1) {
			return 0;
		}
		return group.getGroupLeader().getSpeed();
	}

}