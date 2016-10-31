package mg.fishchicken.ui;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.combat.CombatManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class CombatButtons extends Table implements EventListener {
	private TextButtonWithSound endTurnButton;
	private TextButtonWithSound endCombatButton;
	private boolean ignoreEvent;
	private GameState gameState;
	
	public CombatButtons(GameState gameState, CombatButtonsStyle style) {
		this.gameState = gameState;
		style.apply(this);
		endTurnButton = new TextButtonWithSound(Strings.getString(
				CombatManager.STRING_TABLE, "endTurn"),
				style.endTurnButtonStyle);
		endCombatButton = new TextButtonWithSound(Strings.getString(
				CombatManager.STRING_TABLE, "endCombat"),
				style.endCombatButtonStyle);
		
		this.add(endCombatButton).fill().width(style.endCombatButtonWidth).height(style.endCombatButtonHeight);
		this.add().space(0).width(10);
		this.add(endTurnButton).fill().width(style.endTurnButtonWidth).height(style.endTurnButtonHeight);
		
		endTurnButton.addListener(this);
		endCombatButton.addListener(this);
		
		ignoreEvent = false;
		
		setX(Gdx.graphics.getWidth() - 145);
		setY(45);	
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			ignoreEvent = true;
			endCombatButton.setChecked(false);
			ignoreEvent = false;
			endCombatButton.setVisible(GameState.canPlayerEndCombat());
		}
	}
	public void updatePosition(int screenWidth, int screenHeight) {
		setX(screenWidth - 145);
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		endCombatButton.setVisible(GameState.isPlayersTurn() && GameState.canPlayerEndCombat());
		if (GameState.isPlayersTurn()) {
			ignoreEvent = true;
			endTurnButton.setChecked(false);
			endTurnButton.setDisabled(false);
			ignoreEvent = false;
		} else if (GameState.isCombatInProgress() && !GameState.isPlayersTurn() && !endTurnButton.isDisabled()) {
			ignoreEvent = true;
			endTurnButton.setDisabled(true);
			endTurnButton.setChecked(true);
			ignoreEvent = false;
		}
		
	}

	@Override
	public boolean handle(Event event) {
		if (!(event instanceof ChangeEvent)) {
			return false;
		}
		if (ignoreEvent) {
			return false;
		}
		return changed((ChangeEvent)event, event.getTarget());
	}
	
	public boolean changed (ChangeEvent event, Actor actor) {
		if (endTurnButton.equals(actor) && GameState.isPlayersTurn()) {
			UIManager.hideToolTip();
			gameState.unpauseGame();
			gameState.switchToNextSide();
			endTurnButton.setDisabled(true);
			ignoreEvent = true;
			endTurnButton.setChecked(true);
			ignoreEvent = false;
			return true;
		} else if (endCombatButton.equals(actor)) {
			if (GameState.canPlayerEndCombat()) {
				gameState.endCombat();
			}
		}
		return false;		
	}
	
	public static class CombatButtonsStyle extends TableStyle {
		private TextButtonWithSoundStyle endTurnButtonStyle, endCombatButtonStyle;
		private int endTurnButtonWidth=130, endTurnButtonHeight=70, endCombatButtonWidth=130, endCombatButtonHeight=70;
	}
}
