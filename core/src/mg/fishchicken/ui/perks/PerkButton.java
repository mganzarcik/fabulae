package mg.fishchicken.ui.perks;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.tooltips.PerkTooltip;
import mg.fishchicken.ui.tooltips.PerkTooltip.PerkTooltipStyle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class PerkButton extends Table implements EventListener {

	private GameCharacter character;
	private Perk perk;
	protected Image perkImage;
	protected PerkButtonStyle style;
	protected Label perkLabel;
	private PerkTooltip tooltip;
	private boolean onlyKnownPerks;
	
	public PerkButton(GameCharacter character, Perk perk, PerkButtonStyle style, boolean onlyKnownPerks) {
		super();
		this.onlyKnownPerks = onlyKnownPerks;
		this.character = character;
		this.perk = perk;
		this.style = style;
		
		if (perk.getIconFile() != null) {
			TextureRegion tr = Assets.getTextureRegion(perk.getIconFile());
			if (tr != null) {
				perkImage = new Image(tr);
				add(perkImage).width(style.iconWidth).height(style.iconHeight);
				row();
			}
		}
		perkLabel = new Label(perk.getName(), style.headingStyle);
		perkLabel.setWrap(true);
		perkLabel.setAlignment(Align.center);
		perkLabel.setWidth(style.iconWidth);
		add(perkLabel).fill().expand().center();
		
		tooltip = createTooltip();
		
		addListener(this);
		
	}
	
	public void recomputeColor(GameCharacter character) {
		if (perkImage == null) {
			return;
		}
		if (character.hasPerk(perk) && (!onlyKnownPerks || perk.canBeActivated(character))) {
			perkImage.setColor(style.knownIconColor);
			perkLabel.setColor(style.knownTextColor);
		} else if (!onlyKnownPerks && perk.canBeLearned(character)) {
			perkImage.setColor(style.unknownIconColor);
			perkLabel.setColor(style.unknownTextColor);
		} else {
			perkImage.setColor(style.forbiddenIconColor);
			perkLabel.setColor(style.forbiddenTextColor);
		}
	}
	
	protected PerkTooltip createTooltip() {
		return new PerkTooltip(perk, style.tooltipStyle);
	}
	
	public Perk getPerk() {
		return perk;
	}
	
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.enter.equals(inputEvent.getType())) {
				tooltip.updateText(character, !onlyKnownPerks);
				UIManager.setToolTip(tooltip);
			}
			if (Type.exit.equals(inputEvent.getType())) {
				UIManager.hideToolTip();
			}
		}
		return false;
	}
	
	public static class PerkButtonStyle {
		public int iconWidth, iconHeight;
		public PerkTooltipStyle tooltipStyle;
		public LabelStyle headingStyle;
		public Color forbiddenIconColor, unknownIconColor, knownIconColor,
				forbiddenTextColor, unknownTextColor, knownTextColor;
	}
}
