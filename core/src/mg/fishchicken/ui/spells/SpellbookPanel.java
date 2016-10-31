package mg.fishchicken.ui.spells;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.TargetSelectionCallback;
import mg.fishchicken.core.input.Targetable;
import mg.fishchicken.gamelogic.actions.CastSpellAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.ui.CharacterPanel;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.perks.PerksComponent.PerksComponentStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class SpellbookPanel extends CharacterPanel implements EventListener, TargetSelectionCallback  {

	private SpellbookPanelStyle style;
	private GameState gameState;
	private GameCharacter displayedCharacter;
	private TargetType spellTarget;
	private Spell spellBeingCast;
	
	public SpellbookPanel(GameState gameState, SpellbookPanelStyle style) {
		super("", style);
		this.gameState = gameState;
		this.style = style;
	}
	
	public void loadCharacter(GameCharacter character) {
		displayedCharacter = character;
		setTitle(Strings.getString(UIManager.STRING_TABLE, "spellbookHeading", character.getName()));
		
		clearChildren();
		add(new SpellsComponent(character, style.spellsStyle, this)).fill();
	}
	
	/**
	 * Overridden to display
	 * the item tool-tip if we need to and to recompute
	 * the total load.
	 * 
	 */
	@Override
	public void act(float delta) {
		super.act(delta);
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.touchDown.equals(inputEvent.getType())) {
				clicked(inputEvent, inputEvent.getTarget());
			}
		}
		return false;
	}
	
	private void clicked(InputEvent event, Actor actor) {
		if (actor instanceof Image || actor instanceof Label) {
			actor = actor.getParent();
		}
		if (actor instanceof SpellButton) {
			SpellButton clickedButton = (SpellButton)actor;
			spellBeingCast = clickedButton.getSpell();
			if (!spellBeingCast.canBeCast() || !spellBeingCast.canBeActivated(displayedCharacter)) {
				return;
			}
			UIManager.closeMutuallyExclusiveScreens();
			if (spellBeingCast.isCombatOnly() && !GameState.isCombatInProgress()) {
				gameState.startCombat();
			}
			spellTarget = spellBeingCast.getTargetTypeInstance(displayedCharacter);
			spellTarget.setApCost(spellBeingCast.getApCost(displayedCharacter));
			gameState.getPlayerCharacterController().startTargetSelection(
					displayedCharacter, spellBeingCast, spellTarget, this);
		}
	}

	@Override
	public void targetSelectionCompleted(Targetable targetable,
			TargetType effectTarget) {
		displayedCharacter.addAction(CastSpellAction.class, spellBeingCast, spellTarget);
	}

	@Override
	public void targetSelectionCancelled(Targetable targetable,
			TargetType effectTarget) {
	}
	
	public static class SpellbookPanelStyle extends BorderedWindowStyle {
		private PerksComponentStyle spellsStyle; 
	}
}
