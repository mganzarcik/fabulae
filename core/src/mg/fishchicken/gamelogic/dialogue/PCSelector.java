package mg.fishchicken.gamelogic.dialogue;

import java.util.Locale;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader.Element;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.actions.Action;
import mg.fishchicken.core.actions.And;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamestate.characters.Skills;
import mg.fishchicken.ui.UIManager;

public class PCSelector extends NPCTalk {
	
	private Array<NPCTalk> npcTalks = new Array<NPCTalk>();
	@XMLField(fieldPath="skills")
	private String skillsNames;
	private Array<Skill> skills;
	
	public PCSelector(Dialogue dialogue) {
		super(dialogue);
	}

	@Override
	public Array<PCTalk> getPCTalks() {
		Array<PCTalk> talks = super.getPCTalks();
		
		Array<GameCharacter> members = GameState.getPlayerCharacterGroup().getMembers();
		for (int i = 0; i < members.size; ++i) {
			GameCharacter member = members.get(i);
			if (member.isActive()) {
				talks.insert(0, getPcTalkForCharacter(member));
			}
		}
		
		return talks;
	}
	
	private PCTalk getPcTalkForCharacter(GameCharacter character) {
		PCTalk talk = new PCTalk(getDialogue());
		talk.setId(getId() + character.getId());
		talk.setText(skills.size < 1 ? character.getName()
				: Strings.getString(UIManager.STRING_TABLE, "pcSelectorItem", character.getName(),
						buildSkillInfo(character.stats().skills())));
		talk.getNpcTalks().addAll(npcTalks);
		talk.action = new And(new SetActiveDialogueCharacter(character), new RollSkillCheck(character));
		getDialogue().addPCTalk(talk);
		return talk;
	}
	
	private String buildSkillInfo(Skills characterSkills) {
		StringBuilder fsb = StringUtil.getFSB();
		for (int i = 0; i < skills.size; ++i) {
			Skill skill = skills.get(i);
			fsb.append(skill.toUIString());
			fsb.append(": ");
			fsb.append(characterSkills.getSkillRank(skill));
			if (i < skills.size -1) {
				fsb.append(", ");
			}
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue;
	}
	
	@Override
	public PCSelector loadFromXML(Element element) {
		super.loadFromXML(element);
		skills = new Array<Skill>();
		
		if (skillsNames != null) {
			Array<String> splitSkillNames = StringUtil.arrayFromDelimitedString(skillsNames, ",");
			for (String skill : splitSkillNames) {
				skills.add(Skill.valueOf(skill.toUpperCase(Locale.ENGLISH)));
			}
		}
		
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
	
	private final class RollSkillCheck extends Action {

		private GameCharacter character;
		
		public RollSkillCheck(GameCharacter character) {
			this.character = character;
		}
		
		@Override
		protected void run(Object object, Binding parameters) {
			for (Skill skill : skills) {
				character.stats().rollSkillCheck(skill, character.getMap(), getDialogue().getNPCAtDialogue());
			}
		}

		@Override
		public void validateAndLoadFromXML(Element conditionElement) {
		}
		
	}
	
	private final class SetActiveDialogueCharacter extends Action {

		private GameCharacter character;
		
		public SetActiveDialogueCharacter(GameCharacter character) {
			this.character = character;
		}
		
		@Override
		protected void run(Object object, Binding parameters) {
			getDialogue().setTalkers(character, getDialogue().getNPCAtDialogue());
		}

		@Override
		public void validateAndLoadFromXML(Element conditionElement) {
		}
		
	}
}
