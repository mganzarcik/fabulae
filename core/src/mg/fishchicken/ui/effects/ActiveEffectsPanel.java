package mg.fishchicken.ui.effects;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.ui.CharacterPanel;
import mg.fishchicken.ui.effects.ActiveEffectsComponent.ActiveEffectsComponentStyle;
import mg.fishchicken.ui.inventory.StatsComponent;
import mg.fishchicken.ui.inventory.StatsComponent.StatsComponentStyle;

public class ActiveEffectsPanel extends CharacterPanel {

	private ActiveEffectsPanelStyle style;
	
	public ActiveEffectsPanel(ActiveEffectsPanelStyle style) {
		super(style);
		this.style = style;
	}
	
	public void loadCharacter(GameCharacter character) {
		setTitle(character.getName());
		
		clearChildren();
		add(new StatsComponent(character, style.statsStyle, style.height)).top().fill();
		add(new ActiveEffectsComponent(character, style.activeEffectsStyle, style.height)).fill();
	}
	
	static public class ActiveEffectsPanelStyle extends BorderedWindowStyle {
		private int height;
		private StatsComponentStyle statsStyle;
		private ActiveEffectsComponentStyle activeEffectsStyle;
	}

}

