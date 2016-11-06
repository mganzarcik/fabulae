package mg.fishchicken.gamelogic.dialogue;

import java.io.IOException;

import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Dialogue implements XMLLoadable {
	
	private ObjectMap<String, Banter> banters = new ObjectMap<String, Banter>();
	private ObjectMap<String, Greeting> greetings = new ObjectMap<String, Greeting>();
	private ObjectMap<String, PCTalk> pcTalks = new ObjectMap<String, PCTalk>();
	private ObjectMap<String, NPCTalk> npcTalks = new ObjectMap<String, NPCTalk>();
	
	private GameCharacter pcAtDialogue = null;
	private GameCharacter npcAtDialogue = null;
	
	@XMLField(fieldPath="title")
	private String title;
	
	public void setTalkers(GameCharacter talkingPC, GameCharacter talkingNPC) {
		pcAtDialogue = talkingPC;
		npcAtDialogue = talkingNPC;
	}
	
	public GameCharacter getPCAtDialogue() {
		return pcAtDialogue;
	}
	
	public GameCharacter getNPCAtDialogue() {
		return npcAtDialogue;
	}
	
	public String getTitle() {
		if (title != null) {
			return title;
		}
		if (npcAtDialogue != null) {
			return npcAtDialogue.getName();
		}
		return "";
	}
	
	public Banter addBanter(Banter banter) {
		banters.put(banter.getId(), banter);
		return banter;
	}
	
	public Banter getBanter(String id) {
		return banters.get(id);
	}
	
	/**
	 * Gets the greeting that is valid 
	 * to start this Dialogue
	 * 
	 * @return
	 */
	public Greeting getStartingGreeting() {
		Array<Greeting> candidates = new Array<Greeting>();
		for (Greeting greeting : greetings.values()) {
			if (greeting.evaluateCondition()) {
				candidates.add(greeting);
			}
		}
		if (candidates.size < 1) {
			return null; 
		}
		Greeting returnValue = candidates.random();
		returnValue.executeAction();
		return returnValue;
	}
	
	public Greeting addGreeting(Greeting greeting) {
		greetings.put(greeting.getId(), greeting);
		return greeting;
	}
	
	public Greeting getGreeting(String id) {
		return greetings.get(id);
	}
	
	public PCTalk addPCTalk(PCTalk pcTalk) {
		pcTalks.put(pcTalk.getId(), pcTalk);
		return pcTalk;
	}
	
	public PCTalk getPCTalk(String id) {
		return pcTalks.get(id);
	}
	
	public NPCTalk addNPCTalk(NPCTalk npcTalk) {
		npcTalks.put(npcTalk.getId(), npcTalk);
		return npcTalk;
	}
	
	public NPCTalk getNPCTalk(String id) {
		return npcTalks.get(id);
	}
	
	/**
	 * Returns all image paths that this dialogue requires for any of its
	 * NPC talks.
	 * @return
	 */
	public ObjectSet<String> getAllImages() {
		ObjectSet<String> images = new ObjectSet<String>();
		for (NPCTalk talk : npcTalks.values()) {
			String image = talk.getImage(); 
			if (image != null) {
				images.add(image);
			}
		}
		for (NPCTalk talk : greetings.values()) {
			String image = talk.getImage(); 
			if (image != null) {
				images.add(image);
			}
		}
		return images;
	}
	
	public void loadFromXML(FileHandle file) throws IOException {
		banters.clear();
		greetings.clear();
		npcTalks.clear();
		pcTalks.clear();
		loadFromXMLNoInit(file);
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		loadFromXML(root);
	}
	
	public void loadFromXML(Element root) throws IOException {		
		XMLUtil.readPrimitiveMembers(this, root);
		
		// load banters
		Array<Element> children = root.getChildrenByName(DialogueItem.XML_ELEMENT_BANTER);
		for (Element banter : children) {
			Banter newBanter = (Banter) new Banter(this).loadFromXML(banter);
			banters.put(newBanter.getId(), newBanter);
		}
		
		// load greetings
		children = root.getChildrenByName(DialogueItem.XML_ELEMENT_GREETING);
		for (Element greeting : children) {
			Greeting newGreeting = (Greeting) new Greeting(this).loadFromXML(greeting);
			greetings.put(newGreeting.getId(), newGreeting);
		}
		
		// load NPCTalks
		children = root.getChildrenByName(DialogueItem.XML_ELEMENT_NPCTTALK);
		for (Element npcTalk : children) {
			NPCTalk newNPCTalk = new NPCTalk(this).loadFromXML(npcTalk);
			npcTalks.put(newNPCTalk.getId(), newNPCTalk);
		}
		
		// load PCSelectors
		children = root.getChildrenByName(DialogueItem.XML_ELEMENT_PCSELECTOR);
		for (Element npcTalk : children) {
			PCSelector newNPCTalk = new PCSelector(this).loadFromXML(npcTalk);
			npcTalks.put(newNPCTalk.getId(), newNPCTalk);
		}
		
		// load PCTalks
		children = root.getChildrenByName(DialogueItem.XML_ELEMENT_PCTTALK);
		for (Element pcTalk : children) {
			String id = pcTalk.getAttribute(DialogueItem.XML_ATTRIBUTE_ID);
			PCTalk newPCTalk = pcTalks.get(id);
			if (newPCTalk == null) {
				newPCTalk = new PCTalk(this);
				pcTalks.put(id, newPCTalk);
			}
			newPCTalk.loadFromXML(pcTalk);
		}
	}

	
}
