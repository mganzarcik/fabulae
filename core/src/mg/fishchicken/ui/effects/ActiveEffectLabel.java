package mg.fishchicken.ui.effects;

import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.effects.ActiveEffectsComponent.EffectDescription;
import mg.fishchicken.ui.tooltips.SimpleTooltip;
import mg.fishchicken.ui.tooltips.SimpleTooltip.SimpleTooltipStyle;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class ActiveEffectLabel extends Table implements EventListener {

	private SimpleTooltip tooltip;
	
	public ActiveEffectLabel(String text, EffectDescription ed, ActiveEffectLabelStyle style, boolean wrap) {
		super();
		Label label = new Label(text, style);
		label.setWrap(wrap);
		this.add(label).expandX().fill();
		this.row();
		this.add(new Label("", style)).expand().fill();
		tooltip = new SimpleTooltip(style.tooltipStyle);
		tooltip.setText(ed.description);
		addListener(this);
		
	}
	
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.enter.equals(inputEvent.getType())) {
				UIManager.setToolTip(tooltip);
			}
			if (Type.exit.equals(inputEvent.getType())) {
				UIManager.hideToolTip();
			}
		}
		return false;
	}
	
	public static class ActiveEffectLabelStyle extends LabelStyle {
		private SimpleTooltipStyle tooltipStyle;
	}
}
