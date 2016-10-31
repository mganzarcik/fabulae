package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.gamestate.characters.Stats;

import com.badlogic.gdx.utils.XmlWriter;

public class CastSpellAction extends UsePerkAction {
	
	public CastSpellAction() {
		super();
	}
	
	public CastSpellAction(GameCharacter user, Spell spell, TargetType target) {
		super(user, spell, target);
	}
	
	@Override
	protected void logCannotUseActivatedError() {
		Log.logLocalized("cannotUseActivatedSpell",LogType.COMBAT, perk.getName(), user.getName(),user.stats().getGender().getPronoun().toLowerCase());
	}
	
	/**
	 * Returns the id of the spell currently cast by this action.
	 * @return
	 */
	public String getSpellId() {
		return getPerkId();
	}
	
	@Override
	protected Perk getPerk(String id) {
		return Spell.getSpell(id);
	}
	
	protected void applyCost() {
		Stats stats = user.stats();
		if (!useForNoAP) {
			stats.addToAP(-(perk.getApCost(user) - user.stats().getAPCostToSpellModifier()));
		}
		stats.addToHP(-perk.getHpCost());
		stats.addToSP(-perk.getSpCost());
		stats.addToMP(-(perk.getMpCost() - user.stats().getMPCostToSpellModifier()));
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_SPELL, perkId);
	}
	
	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		if (Skill.SNEAKING == skill) {
			return Configuration.getCastSpellStealthModifier() + skillUser.getMap().getSkillCheckModifier(skill, skillUser);
		}
		return skillUser.getMap().getSkillCheckModifier(skill, skillUser);
	}
}
