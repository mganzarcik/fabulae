package mg.fishchicken.gamelogic.dialogue;

import groovy.lang.Binding;

import java.util.Locale;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.actions.Action;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class DialogueItem implements ThingWithId {

	public static final String SPECIAL_PCNAME = "pcName";
	public static final String SPECIAL_PC_PRONOUN = "pcPronoun";
	public static final String SPECIAL_PC_POSSESIVE_PRONOUN = "pcPossesivePronoun";
	public static final String SPECIAL_PC_OBJECT_PRONOUN = "pcObjectPronoun";
	public static final String SPECIAL_NPCNAME = "npcName";
	public static final String SPECIAL_NPC_PRONOUN = "npcPronoun";
	public static final String SPECIAL_NPC_POSSESIVE_PRONOUN = "npcPossesivePronoun";
	public static final String SPECIAL_NPC_OBJECT_PRONOUN = "npcObjectPronoun";
	
	public static final String XML_ELEMENT_GREETING = "greeting";
	public static final String XML_ELEMENT_BANTER = "banter";
	public static final String XML_ELEMENT_PCTTALK = "pcTalk";
	public static final String XML_ELEMENT_NPCTTALK = "npcTalk";
	public static final String XML_ELEMENT_TEXT = "text";
	public static final String XML_ATTRIBUTE_ID = "id";
	public static final String XML_ATTRIBUTE_CONVERSATION_END = "endOfConversation";
	public static final String XML_ATTRIBUTE_IS_YES = "isYes";
	public static final String XML_ATTRIBUTE_NPC = "npc";
	public static final String XML_ATTRIBUTE_ROLE = "role";
	
	private Dialogue dialogue;
	@XMLField(fieldPath="id")
	private String id;
	private Action action;
	protected Condition condition;
	private String text;
	
	public DialogueItem(Dialogue dialogue) {
		this.dialogue = dialogue;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public abstract GameObject getConditionObject();
	
	public boolean evaluateCondition() {
		if (condition == null) {
			return true;
		}
		Binding conditionParams = new Binding();
		conditionParams.setVariable(Condition.PARAM_NPC_AT_DIALOGUE, dialogue.getNPCAtDialogue());
		conditionParams.setVariable(Condition.PARAM_PC_AT_DIALOGUE, dialogue.getPCAtDialogue());
		return condition.execute(getConditionObject(), conditionParams);
	}
	
	public void executeAction() {
		if (action == null) {
			return;
		}
		Binding actionParams = new Binding();
		actionParams.setVariable(Condition.PARAM_NPC_AT_DIALOGUE, dialogue.getNPCAtDialogue());
		actionParams.setVariable(Condition.PARAM_PC_AT_DIALOGUE, dialogue.getPCAtDialogue());
		actionParams.setVariable(Condition.PARAM_INITIAL_OBJECT, dialogue.getNPCAtDialogue());
		action.execute(getConditionObject(), actionParams);
	}
	
	public String getText(ObjectMap<String, String> parameters) {
		if (parameters == null) {
			parameters = new ObjectMap<String, String>();
		}
		
		if (dialogue.getPCAtDialogue() != null) {
			parameters.put(SPECIAL_PCNAME, dialogue.getPCAtDialogue().getName());
			parameters.put(SPECIAL_PC_PRONOUN, dialogue.getPCAtDialogue().stats().getGender().getPronoun().toLowerCase(Locale.ENGLISH));
			parameters.put(SPECIAL_PC_POSSESIVE_PRONOUN, dialogue.getPCAtDialogue().stats().getGender().getPossesivePronoun().toLowerCase(Locale.ENGLISH));
			parameters.put(SPECIAL_PC_OBJECT_PRONOUN, dialogue.getPCAtDialogue().stats().getGender().getObjectPronoun().toLowerCase(Locale.ENGLISH));
		}
		
		if (dialogue.getNPCAtDialogue() != null) {
			parameters.put(SPECIAL_NPCNAME, dialogue.getNPCAtDialogue().getName());
			parameters.put(SPECIAL_NPC_PRONOUN, dialogue.getNPCAtDialogue().stats().getGender().getPronoun().toLowerCase(Locale.ENGLISH));
			parameters.put(SPECIAL_NPC_POSSESIVE_PRONOUN, dialogue.getNPCAtDialogue().stats().getGender().getPossesivePronoun().toLowerCase(Locale.ENGLISH));
			parameters.put(SPECIAL_NPC_OBJECT_PRONOUN, dialogue.getNPCAtDialogue().stats().getGender().getObjectPronoun().toLowerCase(Locale.ENGLISH));
		}
		
		String returnValue = StringUtil.replaceParameters(Strings.getString(text), parameters);
		returnValue = StringUtil.replaceCharacterNames(returnValue);
		return returnValue;
	}
	
	public DialogueItem setText(String text) {
		this.text = text;
		return this;
	}

	public Dialogue getDialogue() {
		return dialogue;
	}

	public DialogueItem loadFromXML(Element element) {
		XMLUtil.readPrimitiveMembers(this, element);
		Element child = element.getChildByName(XML_ELEMENT_TEXT);
		text = child != null ? StringUtil.clearString(child.getText()) : "";
		
		action = Action.getAction(element.getChildByName(XMLUtil.XML_ACTION));
		condition = Condition.getCondition(element.getChildByName(XMLUtil.XML_CONDITION));
		
		return this;
	}
}
