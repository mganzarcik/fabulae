package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.dialogue.DialogueCallback;
import mg.fishchicken.gamelogic.dialogue.PCTalk;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Displays the dialogue with the supplied id. If the id is not specified,
 * the action will try to display a dialogue with the same id as the object
 * this action was initiated on. This object is always the actual initiator 
 * of the action, regardless of the targetObject.<br />
 * <br />
 * Parameter targetObject need to be specified to tell the action which player
 * character is doing the talking. <br />
 * <br />
 * Optional parameter npc can be specified to tell action which NPC is doing the
 * talking. If the supplied value is one of the special keywords 
 * (starting with "__", like "__enteringCharacter") the id of the object this
 * keyword represents will be used to get the NPC. 
 * <br />
 * If completely omitted, the dialogue will be displayed without an NPC talker. <br />
 * <br />
 * Example:
 * 
 * <pre>
 * 	&lt;displayDialogue targetObject="__enteringCharacter" npc="npcId" id="DialogueId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class DisplayDialogue extends Action {

	public static final String XML_DIALOGUE_ID = "id";
	public static final String XML_DIALOGUE_NPC = "npc";

	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof AbstractGameCharacter) {
			String talkerId = getParameter(XML_DIALOGUE_NPC);
			String dialogueId = getParameter(XML_DIALOGUE_ID);
			if (dialogueId == null) {
				Object initialObject = parameters.getVariable(PARAM_INITIAL_OBJECT);
				if (initialObject instanceof ThingWithId) {
					dialogueId = ((ThingWithId)initialObject).getId();
				}
			}
			if (dialogueId == null) {
				Log.log("No dialogue id found for object .", LogType.ERROR, object);
				return;
			}
			if (talkerId != null && talkerId.startsWith("__")) {
				Object talkerIdObject = parameters.getVariable(talkerId);
				if (talkerIdObject instanceof ThingWithId) {
					talkerId = ((ThingWithId)talkerIdObject).getId();
				}
			}
			final GameCharacter talker = talkerId != null ? (GameCharacter) GameState.getGameObjectById(talkerId, GameCharacter.class)
					: null;
			UIManager.displayDialogue(((AbstractGameCharacter) object).getRepresentative(), talker, dialogueId,
					new DialogueCallback() {
						@Override
						public void onDialogueEnd(PCTalk dialogueStopper) {
							if (talker != null) {
								talker.setMetNPCBefore(true);
							}

						}
					}, null);
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

}
