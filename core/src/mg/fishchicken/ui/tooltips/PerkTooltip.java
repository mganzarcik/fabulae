package mg.fishchicken.ui.tooltips;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.conditions.Condition.ConditionResult;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamelogic.modifiers.Modifier;

import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Simple tooltip that contains multiple labels, 
 * allowing for different formatting per label.
 * @author ANNUN
 *
 */
public class PerkTooltip extends CompositeTooltip {
	
	protected PerkTooltipStyle style;
	private Perk perk;
	
	public PerkTooltip(Perk perk, PerkTooltipStyle style) {
		super(style);
		this.style = style;
		this.perk = perk;
	}

	public void updateText(GameCharacter character, boolean includeLearnRequirements) {
		clear();
		
		StringBuilder fsb = StringUtil.getFSB();
		
		buildName(fsb);
		buildDescription(fsb);
		
		if (includeLearnRequirements) {
			buildLearnRequirements(character, fsb);
		}
		
		if (perk.isActivated()) {
			buildActivationRequirements(character, fsb);
			buildTarget(character, fsb);
		}
		
		buildEffectsAndModifiers(character, fsb);
		
		buildSynergiesDescription(fsb);
		
		StringUtil.freeFSB(fsb);
	}
	
	protected void buildName(StringBuilder fsb) {
		fsb.append(perk.getName());
		if (!perk.isActivated()) {
			fsb.append(" (");
			fsb.append(Strings.getString(Perk.STRING_TABLE, "passive"));
			fsb.append(")");
		}
		addLine(fsb.toString(), style.headingStyle);
		fsb.setLength(0);
	}
	
	protected void buildDescription(StringBuilder fsb) {
		addLine(perk.getDescription());
	}
	
	protected void buildSynergiesDescription(StringBuilder fsb) {
		String description =  perk.getSynergiesDescription();
		if (!StringUtil.nullOrEmptyString(description)) {
			addLine();
			addLine(Strings.getString(Perk.STRING_TABLE, "synergiesDescription"), style.subheadingStyle);
			addLine(description);
		}
	}
	
	protected void buildEffectsAndModifiers(GameCharacter character, StringBuilder fsb) {
		String modifiers = Modifier.getModifiersAsString(perk, ", ", false);
		String effects = Effect.getEffectsAsString(perk, character);
		if (!modifiers.isEmpty() || effects != null) {
			addLine();
			addLine(Strings.getString(Perk.STRING_TABLE, "effects"), style.subheadingStyle);
		}
		
		if (!modifiers.isEmpty()) {
			addLine(modifiers);
		}
		
		if (effects != null) {
			addLine(effects);
		}
	}
	
	protected void buildLearnRequirements(GameCharacter character, StringBuilder fsb) {
		addLine();
		addLine(Strings.getString(Perk.STRING_TABLE, "learnRequirements"), style.subheadingStyle);
		
		fsb.append(Strings.getString(ModifiableStat.STRING_TABLE, "level"));
		fsb.append(": ");
		fsb.append(Integer.toString(perk.getLevelRequirement()));
		if (perk.getLevelRequirement() > character.stats().getLevel()) {
			addLine(fsb.toString(), style.reqsNotReachedStyle);
		} else {
			addLine(fsb.toString(), style.reqsReachedStyle);
		}
		fsb.setLength(0);
	
		Array<ConditionResult> learnReqs = perk.evaluateLearnRequirements(character);
		if (learnReqs != null) {
			for (ConditionResult result : learnReqs) {
				addLine(result.conditionName, result.passed ? style.reqsReachedStyle : style.reqsNotReachedStyle);
			}
		}
	}
	
	protected void buildActivationRequirements(GameCharacter character, StringBuilder fsb) {
		Array<ConditionResult> activationReqs = perk.evaluateActivationRequirements(character);
		if (activationReqs != null || perk.isCombatOnly()) {
			addLine();
			addLine(Strings.getString(Perk.STRING_TABLE, "useRequirements"), style.subheadingStyle);
			if (perk.isCombatOnly()) {
				addLine(Strings.getString(Perk.STRING_TABLE, "combatOnly"), GameState.isCombatInProgress() ? style.reqsReachedStyle : style.reqsNotReachedStyle);
			}
			if (activationReqs != null) {
				for (ConditionResult result : activationReqs) {
					addLine(result.conditionName, result.passed ? style.reqsReachedStyle : style.reqsNotReachedStyle);
				}
			}
		}
		
		addLine();
		addLine(Strings.getString(Perk.STRING_TABLE, "cost"), style.subheadingStyle);
		
		fsb.append(Strings.getString(ModifiableStat.STRING_TABLE, "actionPoints"));
		fsb.append(": ");
		fsb.append(Integer.toString(perk.getApCost(character)));
		addLine(fsb.toString(), !GameState.isCombatInProgress() || (character.stats().getAPAct() >= perk.getApCost(character)) ? style.reqsReachedStyle : style.reqsNotReachedStyle);
		fsb.setLength(0);
		
		if (perk.getHpCost() != 0) {
			fsb.append(Strings.getString(ModifiableStat.STRING_TABLE, "hitPoints"));
			fsb.append(": ");
			fsb.append(Integer.toString(perk.getHpCost()));
			addLine(fsb.toString(), character.stats().getHPAct() >= perk.getHpCost() ? style.reqsReachedStyle : style.reqsNotReachedStyle);
			fsb.setLength(0);
		}
		
		if (perk.getSpCost() != 0) {
			fsb.append(Strings.getString(ModifiableStat.STRING_TABLE, "stamina"));
			fsb.append(": ");
			fsb.append(Integer.toString(perk.getSpCost()));
			addLine(fsb.toString(), character.stats().getSPAct() >= perk.getSpCost() ? style.reqsReachedStyle : style.reqsNotReachedStyle);
			fsb.setLength(0);
		}
		
		if (perk.getMpCost() != 0) {
			fsb.append(Strings.getString(ModifiableStat.STRING_TABLE, "mana"));
			fsb.append(": ");
			fsb.append(Integer.toString(perk.getMpCost()));
			addLine(fsb.toString(), character.stats().getMPAct() >= perk.getMpCost() ? style.reqsReachedStyle : style.reqsNotReachedStyle);
			fsb.setLength(0);
		}
		
	}
	
	protected void buildTarget(GameCharacter character, StringBuilder fsb) {
		addLine();
		addLine(Strings.getString(Perk.STRING_TABLE, "targetType"), style.subheadingStyle);
		addLine(perk.getTargetTypeInstance(character).getUIString());
	}
	
	public static class PerkTooltipStyle extends SimpleTooltipStyle {
		protected LabelStyle reqsReachedStyle, reqsNotReachedStyle, headingStyle, subheadingStyle;
	}
}
