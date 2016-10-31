package mg.fishchicken.gamelogic.dialogue;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;


public class PCTalk extends DialogueItem {

	public static final String ROLE_DEFAULT = "default";
	
	private Array<NPCTalk> npcTalks = new Array<NPCTalk>();
	private ObjectMap<String, String> roleSpecificTexts = new ObjectMap<String, String>();
	private boolean conversationEnd;
	private boolean isYes = false;

	public PCTalk(Dialogue dialogue) {
		super(dialogue);
	}
	
	public Array<NPCTalk> getNpcTalks() {
		return npcTalks;
	}
	
	/**
	 * Executes the action of the NPC talk that fulfills the condition
	 * to be displayed to the player next and returns NPC talk. 
	 * @return
	 */
	public NPCTalk executeNextValidNPCTalk() {
		Array<NPCTalk> candidates = new Array<NPCTalk>();
		for (NPCTalk talk : npcTalks) {
			if (talk.evaluateCondition()) {
				candidates.add(talk);
			}
		}
		NPCTalk returnValue = candidates.random();
		if (returnValue != null) {
			returnValue.executeAction();
		}
		return returnValue;
	}
	
	@Override
	public DialogueItem setText(String text) {
		roleSpecificTexts.put(ROLE_DEFAULT, text);
		return this;
	}
	
	public String getText(ObjectMap<String, String> parameters) {
		String text = null;
		
		if (getDialogue().getPCAtDialogue() != null) {
			text = roleSpecificTexts.get(getDialogue().getPCAtDialogue().getRole().getId());
		}
		if (text == null) {
			text = roleSpecificTexts.get(ROLE_DEFAULT);
		}

		if (text == null) {
			text = "";
		}
		
		super.setText(text);
		
		return super.getText(parameters);
	}

	public boolean isConversationEnd() {
		return conversationEnd;
	}
	
	public boolean isYes() {
		return isYes;
	}

	public void setConversationEnd(boolean conversationEnd) {
		this.conversationEnd = conversationEnd;
	}
	
	public PCTalk loadFromXML(Element element) {
		super.loadFromXML(element);
		
		Array<Element> texts = element.getChildrenByName(XML_ELEMENT_TEXT);
		roleSpecificTexts = new ObjectMap<String, String>();
		
		for (Element textElement : texts) {
			String role = textElement.getAttribute(XML_ATTRIBUTE_ROLE, ROLE_DEFAULT);
			roleSpecificTexts.put(role, StringUtil.clearString(textElement.getText()));
		}
		
		conversationEnd = Boolean.valueOf(element.getAttribute(XML_ATTRIBUTE_CONVERSATION_END, "false"));
		isYes = Boolean.valueOf(element.getAttribute(XML_ATTRIBUTE_IS_YES, "false"));
		
		Array<Element> children = element.getChildrenByName(XML_ELEMENT_NPCTTALK);
		for (Element npcTalk  : children) {
			String id = npcTalk.getAttribute(XML_ATTRIBUTE_ID);
			NPCTalk talk = getDialogue().getNPCTalk(id);
			if (talk == null) {
				talk = new NPCTalk(getDialogue());
				talk.setId(id);
			}
			this.npcTalks.add(talk);
			getDialogue().addNPCTalk(talk);
		}
		
		return this;
	}

	@Override
	public GameObject getConditionObject() {
		return getDialogue().getPCAtDialogue();
	}
}
