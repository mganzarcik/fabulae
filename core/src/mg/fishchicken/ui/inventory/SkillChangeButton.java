package mg.fishchicken.ui.inventory;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamestate.characters.Stats;
import mg.fishchicken.ui.button.ImageButtonWithSound;
import mg.fishchicken.ui.button.ImageButtonWithSound.ImageButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class SkillChangeButton extends Table implements EventListener{

	private Skill skill;
	private Stats stats;
	private Cell<?> decreaseButtonCell;
	private Cell<?> increaseButtonCell;
	private ImageButtonWithSound decreaseButton;
	private ImageButtonWithSound increaseButton;
	private int increases;
	private Label padInc;
	private Label padDec;
	
	public SkillChangeButton(Stats stats, SkillChangeButtonStyle style, StatLabel statLabel) {
		super();
		this.skill = statLabel.getStat().toSkill();
		this.stats = stats;
		increases = 0;
		
		// a very hacky way to determine the min width of the label with the skill value
		// this is important so that buttons don't jump around as the value changes
		float minWidth = new Label(""+(Configuration.getMaxBaseSkillRank() > 9 ? Configuration.getMaxBaseSkillRank() : 0), statLabel.getStyle()).getPrefWidth();
		
		padInc = new Label("", style.skillIncreaseStyle);
		padDec = new Label("", style.skillDecreaseStyle);
		decreaseButton = new ImageButtonWithSound(style.decreaseButtonStyle);
		decreaseButton.add(new Label("-", style.skillDecreaseStyle)).padLeft(style.buttonPaddingLeft).padRight(style.buttonPaddingRight);
		decreaseButton.addListener(this);
		increaseButton = new ImageButtonWithSound(style.increaseButtonStyle);
		increaseButton.add(new Label("+", style.skillIncreaseStyle)).padLeft(style.buttonPaddingLeft).padRight(style.buttonPaddingRight);
		increaseButton.addListener(this);
		
		decreaseButtonCell = add(decreaseButton);
		decreaseButtonCell.minWidth(decreaseButtonCell.getPrefWidth()).fill();
		add(statLabel).padLeft(style.textPaddingLeft).padRight(style.textPaddingRight).minWidth(minWidth);
		increaseButtonCell = add(increaseButton);
		increaseButtonCell.minWidth(increaseButtonCell.getPrefWidth()).fill();
		recombuteButtonVisibility();
		pack();
	}

	@Override
	public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
			Actor target = event.getTarget();
			boolean isDecrease = target.equals(decreaseButton);
			int skillIncreases = stats.getSkillIncreasesThisLevel(skill);
			
			if (isDecrease && increases > 0) {
				--increases;
				stats.decrementSkillIncreasesThisLevel(skill);
				stats.setSkillPoints(stats.getSkillPoints()+1);
				stats.skills().decreaseSkillRank(skill);
				return true;
			} else if (!isDecrease && skillIncreases < Configuration.getSkillIncreasesPerLevel()) {
				++increases;
				stats.incrementSkillIncreasesThisLevel(skill);
				stats.setSkillPoints(stats.getSkillPoints()-1);
				stats.skills().increaseSkillRank(skill);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		recombuteButtonVisibility();
	}
	
	public void resetIncreases() {
		increases = 0;
	}
	
	private void recombuteButtonVisibility() {
		int skillIncreases = stats.getSkillIncreasesThisLevel(skill);
		if (increases <= 0) {
			decreaseButtonCell.setActor(padDec);
		} else {
			decreaseButtonCell.setActor(decreaseButton);
		}
		if (stats.getSkillPoints() < 1 || skillIncreases >= Configuration.getSkillIncreasesPerLevel()
				|| stats.skills().getSkillRank(skill) >= Configuration.getMaxBaseSkillRank()) {
			increaseButtonCell.setActor(padInc);
		} else {
			increaseButtonCell.setActor(increaseButton);
		}
	}
	
	public static class SkillChangeButtonStyle {
		protected int textPaddingRight, textPaddingLeft, buttonPaddingLeft, buttonPaddingRight;
		protected LabelStyle skillIncreaseStyle, skillDecreaseStyle;
		private ImageButtonWithSoundStyle increaseButtonStyle, decreaseButtonStyle;
	}
}
