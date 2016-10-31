package mg.fishchicken.ui.perks;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.TargetSelectionCallback;
import mg.fishchicken.core.input.Targetable;
import mg.fishchicken.gamelogic.actions.AttackAction;
import mg.fishchicken.gamelogic.actions.UsePerkAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
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

public class UsePerksPanel extends CharacterPanel implements EventListener, TargetSelectionCallback {

	private GameState gameState;
	private GameCharacter displayedCharacter;
	private Perk perkBeingUsed;
	private TargetType perkTarget;
	private UsePerksPanelStyle style;
	
	public UsePerksPanel(GameState gameState, UsePerksPanelStyle style) {
		super(style);
		this.style = style;
		this.gameState = gameState;
	}
	
	public void loadCharacter(GameCharacter character) {
		displayedCharacter = character;
		
		setTitle(Strings.getString(UIManager.STRING_TABLE, "usePerkHeading", character.getName()));
		clearChildren();
		add(new PerksComponent(character, style.perksStyle, this, true, true, false)).fill();
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
		if (actor instanceof PerkButton) {
			PerkButton clickedButton = (PerkButton)actor;
			perkBeingUsed = clickedButton.getPerk();
			if (!perkBeingUsed.canBeActivated(displayedCharacter)) {
				return;
			}
			UIManager.closeMutuallyExclusiveScreens();
			if (perkBeingUsed.isCombatOnly() && !GameState.isCombatInProgress()) {
				gameState.startCombat();
			}
			
			perkTarget = perkBeingUsed.getTargetTypeInstance(displayedCharacter);
			perkTarget.setApCost(perkBeingUsed.getApCost(displayedCharacter));
			// for melee attacks, we will also render the move to path during target selection
			// just use a bogus target tile coordinate here, anything more than one tile away will do
			// to determine whether the attacking weapon will be ranged
			if (perkBeingUsed.isAttack() && !AttackAction.isRangedAttack(displayedCharacter, (int) displayedCharacter.position().getX(),  (int)displayedCharacter.position().getY()+2)) {
				perkTarget.setRenderMoveToPath(true);
			}
			gameState.getPlayerCharacterController()
					.startTargetSelection(displayedCharacter,
							perkBeingUsed, perkTarget,
							this);
		}
	}

	@Override
	public void targetSelectionCompleted(Targetable targetable,
			TargetType effectTarget) {
		displayedCharacter.addAction(UsePerkAction.class, perkBeingUsed, perkTarget);
	}

	@Override
	public void targetSelectionCancelled(Targetable targetable,
			TargetType effectTarget) {		
	}
	
	public static class UsePerksPanelStyle extends BorderedWindowStyle {
		private PerksComponentStyle perksStyle;
	}
}
