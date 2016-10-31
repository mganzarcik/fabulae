package mg.fishchicken.ui.inventory;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.tooltips.StatTooltip;
import mg.fishchicken.ui.tooltips.StatTooltip.StatTooltipStyle;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class StatLabel extends Label implements EventListener{

	private ModifiableStat stat;
	private StatTooltip tooltip;
	private GameCharacter character;
	
	public StatLabel(GameCharacter character, CharSequence text, StatLabelStyle style, ModifiableStat stat) {
		super(text, style);
		this.stat = stat;
		this.character = character;
		tooltip = new StatTooltip(stat, style.tooltipStyle);
		addListener(this);
	}

	public ModifiableStat getStat() {
		return stat;
	}
	
	public StatTooltip getTooltip() {
		return tooltip;
	}

	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.enter.equals(inputEvent.getType())) {
				tooltip.updateText(character);
				if (tooltip.shouldDisplay()) {
					UIManager.setToolTip(tooltip);
				}
			}
			if (Type.exit.equals(inputEvent.getType())) {
				UIManager.hideToolTip();
			}
		}
		return false;
	}

	public static class StatLabelStyle extends LabelStyle {
		protected StatTooltipStyle tooltipStyle;
	}

}
