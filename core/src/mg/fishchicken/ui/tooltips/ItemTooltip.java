package mg.fishchicken.ui.tooltips;

import mg.fishchicken.core.conditions.Condition.ConditionResult;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.inventory.items.Armor;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemOwner;
import mg.fishchicken.gamelogic.inventory.items.UsableItem;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamelogic.modifiers.Modifier;

import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class ItemTooltip extends CompositeTooltip {
	
	protected InventoryItem item;
	protected ItemTooltipStyle style;
	
	public ItemTooltip(InventoryItem item, ItemTooltipStyle style) {
		super(style);
		this.item = item;
		this.style = style;
	}
	
	public void setItem(InventoryItem item) {
		this.item = item;
	}
	
	public void updateText(GameCharacter ic) {
		clear();
		
		StringBuilder fsb = StringUtil.getFSB();
		
		buildName(ic, fsb);
		buildDescription(fsb);
		buildOwner(ic, fsb);
		buildArmorInfo(fsb);
		buildWeaponInfo(fsb);
		buildUsesLeft(fsb);
		buildUseRequirements(ic, fsb);
		buildEquipRequirements(ic, fsb);
		buildEffectsAndModifiers(ic, fsb);
		
		StringUtil.freeFSB(fsb);
	}
	

	protected void buildName(GameCharacter ic, StringBuilder fsb) {
		addLine(item.getName(), style.headingStyle);
	}
	
	
	protected void buildWeaponInfo(StringBuilder fsb) {
		if (item instanceof Weapon) {
			Weapon weapon = (Weapon)item;
			addLine();
			addLine(Strings.getString(InventoryItem.STRING_TABLE, "Damage")+": " + weapon.getWeaponDamageMin()+ " - "+weapon.getWeaponDamageMax());
			addLine(Strings.getString(InventoryItem.STRING_TABLE, "Bonus")+": +"+weapon.getWeaponBonus());
			addLine(Strings.getString(InventoryItem.STRING_TABLE,"Skill")+": "+weapon.getWeaponSkill().toString().toLowerCase());
		}
	}
	
	protected void buildArmorInfo(StringBuilder fsb) {
		if (item instanceof Armor) {
			addLine();
			addLine(Strings.getString(InventoryItem.STRING_TABLE,"weight")+": " + MathUtil.toUIString(item.getWeight() / 1000f) + " kg");
		}
	}
	
	protected void buildDescription(StringBuilder fsb) {
		addLine(item.getDescription());
	}
	
	protected void buildOwner(GameCharacter ic, StringBuilder fsb) {
		ItemOwner owner = item.getOwner();
		if (!owner.isEmpty()) {
			addLine();
			if (ic.isOwnerOf(item)) {
				addLine(Strings.getString(InventoryItem.STRING_TABLE,"Owner")+": " +item.getOwner().toUIString());
			} else if(item.getInventory() != ic.getInventory()) { // identity check ok
				addLine(Strings.getString(InventoryItem.STRING_TABLE,"Owner")+": " +item.getOwner().toUIString(), style.stolenStyle);
			} else {
				addLine(Strings.getString(InventoryItem.STRING_TABLE,"Owner")+" ("+Strings.getString(InventoryItem.STRING_TABLE,"stolen")+"): " +item.getOwner().toUIString(), style.stolenStyle);
			}
		}
	}
	
	protected void buildUsesLeft(StringBuilder fsb) {
		if (item instanceof UsableItem) {
			addLine();
			addLine(Strings.getString(UsableItem.STRING_TABLE,"Uses")+": " +  ((UsableItem)item).getUsesLeft() + " / " + ((UsableItem)item).getMaxUses());
		}
	}
	
	protected void buildEffectsAndModifiers(GameCharacter ic, StringBuilder fsb) {
		String modifiers = Modifier.getModifiersAsString(item, ", ", false);
		String effects = null;
		if (item instanceof EffectContainer) {
			effects = Effect.getEffectsAsString((EffectContainer)item, ic);
		}
		if (!modifiers.isEmpty() || effects != null) {
			addLine();
			addLine(Strings.getString(InventoryItem.STRING_TABLE, "effects"), style.subheadingStyle);
		}
		
		if (!modifiers.isEmpty()) {
			addLine(modifiers);
		}
		
		if (effects != null) {
			addLine(effects);
		}
	}
	
	protected void buildUseRequirements(GameCharacter ic,
			StringBuilder fsb) {
		if (item instanceof UsableItem) {
			addConditionResults(
					Strings.getString(InventoryItem.STRING_TABLE,
							"useRequirements"),
					((UsableItem) item).evaluateUseRequirements(ic));
		}
	}

	protected void buildEquipRequirements(GameCharacter ic,
			StringBuilder fsb) {
		addConditionResults(
				Strings.getString(InventoryItem.STRING_TABLE,
						"equipRequirements"),
				item.evaluateEquipRequirements(ic));
	}
	
	protected void addConditionResults(String heading, Array<ConditionResult> userReqs) {
		if (userReqs != null) {
			addLine();
			addLine(heading, style.subheadingStyle);
			for (ConditionResult result : userReqs) {
				addLine(result.conditionName,
						result.passed ? style.reqsReachedStyle
								: style.reqsNotReachedStyle);
			}
		}
	}
	
	public static class ItemTooltipStyle extends SimpleTooltipStyle {
		protected LabelStyle headingStyle, subheadingStyle, stolenStyle, reqsReachedStyle, reqsNotReachedStyle;
	}

}
