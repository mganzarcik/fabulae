package mg.fishchicken.ui.spells;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.ui.perks.PerkButton;
import mg.fishchicken.ui.tooltips.PerkTooltip;
import mg.fishchicken.ui.tooltips.SpellTooltip;

public class SpellButton extends PerkButton {

	private Spell spell;
	
	public SpellButton(GameCharacter character, Spell spell, PerkButtonStyle style) {
		super(character, spell, style, true);
		this.spell = spell;
	}
	
	public Spell getSpell() {
		return spell;
	}
	
	@Override
	protected PerkTooltip createTooltip() {
		return new SpellTooltip((Spell)getPerk(), style.tooltipStyle);
	}
	
	@Override
	public void recomputeColor(GameCharacter character) {
		if (perkImage == null) {
			return;
		}
		if (spell.canBeCast() && character.hasSpell(spell) && spell.canBeActivated(character)) {
			perkImage.setColor(style.knownIconColor);
			perkLabel.setColor(style.knownTextColor);
		} else {
			perkImage.setColor(style.forbiddenIconColor);
			perkLabel.setColor(style.forbiddenTextColor);
		}
	}

}
