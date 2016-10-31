package mg.fishchicken.ui.perks;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.ui.CharacterPanel;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.inventory.StatsComponent;
import mg.fishchicken.ui.inventory.StatsComponent.StatsComponentStyle;
import mg.fishchicken.ui.perks.PerksComponent.PerksComponentStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class PerksPanel extends CharacterPanel implements EventListener {

	private GameCharacter displayedCharacter;
	private PerkButton clickedButton;
	private PerksComponent perksComponent;
	private StatsComponent statsComponent;
	private DialogueCallback callback;
	private PerksPanelStyle style;
	
	public PerksPanel(PerksPanelStyle style) {
		super(style);
		this.style = style;
		callback = new DialogueCallback();
	}
	
	public void loadCharacter(GameCharacter character) {
		this.displayedCharacter = character;
		this.clearChildren();
		
		setTitle(character.getName());
		
		perksComponent = new PerksComponent(character, style.perksStyle, this);
		statsComponent = new StatsComponent(displayedCharacter, style.statsStyle, this, (int)perksComponent.getPrefHeight(), true);
		this.add(statsComponent).top().fill();
		this.add(perksComponent).fill();
	}
	
	@Override
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.touchDown.equals(inputEvent.getType())) {
				clicked(inputEvent, inputEvent.getTarget());
			}
		}
		if (event instanceof ChangeEvent) {
			ChangeEvent inputEvent = (ChangeEvent) event;
			changed(inputEvent.getTarget());
		}
		return false;
	}
	
	private void changed(Actor actor) {
		if (statsComponent.isAscendantOf(actor)) {
			perksComponent.recomputePerks();
		}
	}
	
	private void clicked(InputEvent event, Actor actor) {
		if (actor instanceof Image || actor instanceof Label) {
			actor = actor.getParent();
		}
		if (displayedCharacter.stats().getPerkPoints() < 1) {
			return;
		}
		if (actor instanceof PerkButton) {
			clickedButton = (PerkButton)actor;
			if (displayedCharacter.hasPerk(clickedButton.getPerk()) || !clickedButton.getPerk().canBeLearned(displayedCharacter)) {
				clickedButton = null;
				return;
			}
			UIManager.displayConfirmation(
					Strings.getString(UIManager.STRING_TABLE, "perkSelectionQuestion", clickedButton.getPerk().getName()), 
					Strings.getString(UIManager.STRING_TABLE, "perkSelectionConfirmation", displayedCharacter.getName(), clickedButton.getPerk().getName()), 
					callback);
		}
	}
	
	private class DialogueCallback extends OkCancelCallback<Void> {
		@Override
		public void onOk(Void nada) {
			displayedCharacter.addPerk(clickedButton.getPerk());
			displayedCharacter.stats().setPerkPoints(displayedCharacter.stats().getPerkPoints()-1);
			Log.logLocalized("perkLearned", Log.LogType.CHARACTER, displayedCharacter.getName(), clickedButton.getPerk().getName());
			perksComponent.recomputePerks();
			statsComponent.resetIncreases();
		}
	}
	
	public static class PerksPanelStyle extends BorderedWindowStyle {
		private StatsComponentStyle statsStyle;
		private PerksComponentStyle perksStyle;
	}

}

