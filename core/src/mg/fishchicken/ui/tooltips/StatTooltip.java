package mg.fishchicken.ui.tooltips;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

public class StatTooltip extends CompositeTooltip {
	
	ModifiableStat stat;
	private boolean shouldDisplay;
	private StringBuilder stringBuilder;
	
	public StatTooltip(ModifiableStat stat, StatTooltipStyle style) {
		super(style);
		this.stat = stat;
		stringBuilder = new StringBuilder();
	}
	
	public void updateText(GameCharacter character) { 
		StatTooltipStyle style = (StatTooltipStyle) this.style;
		clear();
		ObjectMap<ModifiableStat, Array<Modifier>> modifiers = ((GameCharacter) character)
				.stats().getAllModifiersForStat(stat, true);
		
		addLine(stat.getDescription(character));
		
		if (modifiers.size > 0) {
			if (getRows() > 0) {
				addLine();
			}
			addLine(Strings.getString(UIManager.STRING_TABLE, "activeEffects"), style.headingStyle);
		}
		
		for (ModifiableStat stat : modifiers.keys()) {
			Array<Modifier> modifiersForStat =  modifiers.get(stat);
			for (Modifier modifier : modifiersForStat) {
				stringBuilder.setLength(0);
				stringBuilder.append(modifier.getName());
				stringBuilder.append(": ");
				stringBuilder.append(modifier.getModAsString(stat));
				addLine(stringBuilder.toString());
			}
		}
		shouldDisplay = getRows() > 0;
	}
	
	public boolean shouldDisplay() {
		return shouldDisplay;
	}
	
	public static class StatTooltipStyle extends SimpleTooltipStyle {
		private LabelStyle headingStyle;
	}

}
