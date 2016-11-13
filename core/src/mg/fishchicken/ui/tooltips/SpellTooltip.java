package mg.fishchicken.ui.tooltips;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.magic.Spell;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.StringBuilder;

public class SpellTooltip extends PerkTooltip {

	private Spell spell;
	
	public SpellTooltip(Spell spell, PerkTooltipStyle style) {
		super(spell, style);
		this.spell = spell;
	}

	
	@Override
	protected void buildActivationRequirements(GameCharacter character,
			StringBuilder fsb) {
		super.buildActivationRequirements(character, fsb);
		ObjectMap<String, Boolean> foci = spell.getFoci();
		
		if (foci.size > 0) {
			addLine();
			addLine(Strings.getString(Spell.STRING_TABLE, "foci"), style.headingStyle);
			
			Inventory inventory = character.getInventory();
			for (Entry<String, Boolean> entry : foci.entries()) {
				fsb.append(InventoryItem.getItemPrototype(entry.key).getName());
				if (entry.value) {
					fsb.append(" (");
					fsb.append(Strings.getString(Spell.STRING_TABLE, "consumed"));
					fsb.append(")");
				}
				addLine(fsb.toString(),
						inventory.getItem(entry.key) != null ? style.reqsReachedStyle
								: style.reqsNotReachedStyle);
				fsb.setLength(0);
			}
		}
	}
}
