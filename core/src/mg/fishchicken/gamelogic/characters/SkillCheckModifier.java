package mg.fishchicken.gamelogic.characters;

import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;

public interface SkillCheckModifier {
	
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser);

}
