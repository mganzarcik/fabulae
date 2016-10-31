package mg.fishchicken.gamelogic.dialogue;

import java.util.Locale;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLField;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class NPCTalk extends DialogueItem  {
	private Array<PCTalk> pcTalks = new Array<PCTalk>();
	private Array<Banter> banters = new Array<Banter>();
	@XMLField(fieldPath="image")
	private String image;
	
	public NPCTalk(Dialogue dialogue) {
		super(dialogue);
	}
	
	public PCTalk addPCTalk(PCTalk talk) {
		pcTalks.add(talk);
		return talk;
	}
	
	public Array<PCTalk> getPCTalks() {
		Array<PCTalk> candidates = new Array<PCTalk>();
		for (PCTalk talk : pcTalks) {
			if (talk.evaluateCondition()) {
				candidates.add(talk);
			}
		}
		return candidates;
	}
	
	public Array<Banter> getRelevantBanters() {
		Array<Banter> returnValue = new Array<Banter>();
		
		for (Banter banter : banters) {
			if (!getDialogue().getPCAtDialogue().getInternalId()
					.equals(banter.getCharacterId())
					&& GameState.isMemberOfPlayerGroup(banter.getCharacterId())) {
				returnValue.add(banter);
			}
		}
		
		return returnValue;
	}
	
	public NPCTalk loadFromXML(Element element) {
		super.loadFromXML(element);
		
		if (image != null) {
			image  = Configuration.addModulePath(image);
		}
		
		Array<Element> children = element.getChildrenByName(XML_ELEMENT_PCTTALK);
		for (Element pcTalk  : children) {
			String id = pcTalk.getAttribute(XML_ATTRIBUTE_ID);
			PCTalk talk = getDialogue().getPCTalk(id);
			if (talk == null) {
				talk = new PCTalk(getDialogue());
				talk.setId(id);
			}
			this.pcTalks.add(talk);
			getDialogue().addPCTalk(talk);
		}
		
		children = element.getChildrenByName(XML_ELEMENT_BANTER);
		for (Element banter  : children) {
			String id = banter.getAttribute(XML_ATTRIBUTE_ID);
			Banter talk = getDialogue().getBanter(id);
			if (talk == null) {
				talk = new Banter(getDialogue());
				talk.setId(id);
			}
			talk.setCharacterId(banter.getAttribute(XML_ATTRIBUTE_NPC).toLowerCase(Locale.ENGLISH));
			this.banters.add(talk);
			getDialogue().addBanter(talk);
		}
		
		return this;
	}

	@Override
	public GameObject getConditionObject() {
		return getDialogue().getNPCAtDialogue();
	}
	
	public String getImage() {
		return image;
	}

}
