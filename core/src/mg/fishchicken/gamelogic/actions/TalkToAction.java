package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.CharacterFilter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.dialogue.DialogueCallback;
import mg.fishchicken.gamelogic.dialogue.PCTalk;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Moves the AbstractGameCharacter to target AbstractGameCharacter
 * and initializes dialogue.
 * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>targetNpc - AbstractGameCharacter - the character to talk to
 * </ol>
 * @author Annun
 *
 */
public class TalkToAction extends MoveToAction  implements SkillCheckModifier  {
	
	private AbstractGameCharacter character;
	private String targetId;
	private AbstractGameCharacter targetNpc;
	
	public TalkToAction() {
	}
	
	public TalkToAction(AbstractGameCharacter character, AbstractGameCharacter target) {
		init(character, target);
	}

	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof AbstractGameCharacter)) {
			throw new GdxRuntimeException("TalkToAction only works on AbstractGameCharacter!");
		}
		character = (AbstractGameCharacter) ac;
		if (parameters.length > 0) {
			this.targetNpc = (AbstractGameCharacter)parameters[0];
			targetId = targetNpc.getInternalId();
			targetNpc.pauseAllActions();
			super.init(ac, targetNpc.position().tile(), false);
		}
	}

	@Override
	public void onRemove(ActionsContainer ac) {
		if (targetNpc != null) {
			targetNpc.resumeAllActions();
		}
	}
	
	@Override
	public void update(float deltaTime) {
		if (targetNpc == null && targetId != null) {
			this.targetNpc = (AbstractGameCharacter)GameState.getGameObjectById(targetId);
			targetNpc.pauseAllActions();
			super.init(character,targetNpc.position().tile());
		}
		
		super.update(deltaTime);
		if(isFinished()) {
			GameCharacter talker = character.getRepresentative();
			UIManager.displayDialogue(talker, targetNpc.getRepresentative(), targetNpc.getDialogueId(), new DialogueCallback() {
				@Override
				public void onDialogueEnd(PCTalk dialogueStopper) {
					targetNpc.getRepresentative().setMetNPCBefore(true);
					
				}
			}, null);
			if (talker.isSneaking() 
					&& talker.getAllCharactersInSightRadius(null, CharacterFilter.NOT_SAME_FACTION, CharacterFilter.AWAKE)
					&& !talker.stats().rollSkillCheck(Skill.SNEAKING, this)) {
				talker.setIsSneaking(false);
			}
			targetNpc.resumeAllActions();
		}
		
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		targetId = actionElement.getAttribute(XML_ATTRIBUTE_TARGET, null);
		if (targetId == null) {
			throw new GdxRuntimeException("target must be specified!");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_TARGET, targetId);
	}
	
	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		if (Skill.SNEAKING == skill) {
			return Configuration.getTalkToStealthModifier() + skillUser.getMap().getSkillCheckModifier(skill, skillUser);
		}
		return skillUser.getMap().getSkillCheckModifier(skill, skillUser);
	}
}
