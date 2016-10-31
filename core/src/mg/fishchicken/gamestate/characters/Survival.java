package mg.fishchicken.gamestate.characters;

import java.util.Locale;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.survival.FoodAndWaterSource;
import mg.fishchicken.gamelogic.survival.SurvivalManager;
import mg.fishchicken.gamelogic.survival.SurvivalManager.SurvivalHazard;
import mg.fishchicken.gamestate.ObservableState;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Survival extends ObservableState<Survival, Survival.SurvivalChange> {

	private GameState gameState;
	private FoodAndWaterSource source;
	private Stats stats;
	private SurvivalChange params;
	private float s_hoursNotEaten;
	private float s_hoursNotDrank;
	private float s_hoursNotSlept;
	private int s_stageOfHunger;
	private int s_stageOfThirst;
	private int s_stageOfSleepDeprivation;
	
	public Survival(GameState state, FoodAndWaterSource source, Stats stats) {
		this.gameState = state;
		this.stats = stats;
		this.source = source;
		params = new SurvivalChange();
		s_hoursNotEaten = 0;
		s_hoursNotDrank = 0;
		s_hoursNotSlept = 0;
		s_stageOfHunger = 0;
		s_stageOfThirst= 0;
		s_stageOfSleepDeprivation = 0;
	}
	
	/**
	 * Updates the survival status of this character.
	 * 
	 * Asleep characters do not update their survival states.
	 * 
	 * @param deltaGameHours - time passed since last calling this method, in game hours
	 */
	public void update(float deltaGameHours, boolean skipHunger, boolean skipThirst) {
		update(deltaGameHours, false, skipHunger, skipThirst);
	}
	
	
	/**
	 * Updates the survival status of this character.
	 * 
	 * Asleep characters do not update their survival states.
	 * 
	 * @param deltaGameHours - time passed since last calling this method, in game hours
	 * @param skipSleep - whether we should not update sleeping status
	 */
	private void update(float deltaGameHours, boolean skipSleep, boolean skipHunger, boolean skipThirst) {
		if (!Configuration.isSurvivalEnabled()) {
			return;
		}
		params.updateFinished = false;
		if (!skipSleep && stats.getRace().getSleepPeriod() > 0) {
			updateSleep(deltaGameHours);
		}
		if (!skipThirst && stats.getRace().getThirstPeriod() > 0) {
			updateThirst(deltaGameHours);
		}
		if (!skipHunger && stats.getRace().getHungerPeriod() > 0) {
			updateHunger(deltaGameHours);
		}
		params.updateFinished = true;
		params.messageKey = null;
		changed(params);
	}
	
	/**
	 * Removes all survival-related modifiers.
	 */
	public void removeAllModifiers() {
		SurvivalManager.removeModidifiers(stats, SurvivalHazard.HUNGER);
		SurvivalManager.removeModidifiers(stats, SurvivalHazard.THIRST);
		SurvivalManager.removeModidifiers(stats, SurvivalHazard.SLEEPDEPRIVATION);
	}
	
	/**
	 * Tells the character that he has slept for the supplied
	 * number of game hours and should now wake up and feel refreshed and amazing.
	 * 
	 * This logs a message, updates survival states (making the character eat and drink
	 * based on how long he slept and removes any negative sleep modifiers) and it resets
	 * the sleep counter.
	 * 
	 * It also actually sets the character state as "not asleep".
	 * @param duration
	 */
	public void hasSlept(float duration, boolean skipHunger, boolean skipThirst) {
		update(duration / 2f, true, skipHunger, skipThirst);
		recoverHP(duration);
		recoverMP(duration);
		recoverSP(duration);
		// only remove sleep modifier if the character has slept long enough
		if (duration >= Configuration.getSleepDuration()) {
			SurvivalManager.removeModidifiers(stats, SurvivalHazard.SLEEPDEPRIVATION);
			s_hoursNotSlept = 0;
			s_stageOfSleepDeprivation = 0;
		}
		params.messageKey = "hasSlept";
		params.setMessageParams(Math.round(duration));
		changed(params);
	}
	
	private void recoverHP(float sleepDuration) {
		float maxAmount = stats.getHPMax() * (stats.getRace().getHPRecovery() / 100f);
		stats.addToHP(MathUtils.round((sleepDuration / (float)Configuration.getSleepDuration()) * maxAmount));
	}
	
	private void recoverMP(float sleepDuration) {
		float maxAmount = stats.getMPMax() * (stats.getRace().getMPRecovery() / 100f);
		stats.addToMP(MathUtils.round((sleepDuration / (float)Configuration.getSleepDuration()) * maxAmount));
	}
	
	private void recoverSP(float sleepDuration) {
		float maxAmount = stats.getSPMax() * (stats.getRace().getSPRecovery() / 100f);
		stats.addToSP(MathUtils.round((sleepDuration / (float)Configuration.getSleepDuration()) * maxAmount));
	}
	
	private void updateSleep(float deltaGameHours) {
		s_hoursNotSlept += deltaGameHours;
		if (s_hoursNotSlept - stats.getRace().getSleepPeriod() < 0) {
			return;
		}
		int newSleepStage = 1 + (int) ((s_hoursNotSlept- stats.getRace().getSleepPeriod()) / stats.getRace().getMoreTiredAfter());
		if (newSleepStage > s_stageOfSleepDeprivation) {
			for (int i = 1; i <= newSleepStage - s_stageOfSleepDeprivation; ++i) {
				gameState.getSurvivalManager().addModifiers(stats, SurvivalHazard.SLEEPDEPRIVATION, s_stageOfSleepDeprivation+i);
			}
			s_stageOfSleepDeprivation = newSleepStage;
			params.messageKey = "needsToSleep";
			params.setMessageParams(gameState.getSurvivalManager().getStageName(SurvivalHazard.SLEEPDEPRIVATION, newSleepStage).toLowerCase(Locale.ENGLISH));
			changed(params);
			if (Configuration.getPauseOnBadChange() && !GameState.isCombatInProgress()) {
				gameState.pauseGame();
			}
		}
	}
	
	private void updateHunger(float deltaGameHours) {
		s_hoursNotEaten += deltaGameHours;
		int newHungerStage = (int) (s_hoursNotEaten / stats.getRace().getHungerPeriod());
		if (newHungerStage > s_stageOfHunger) {
			tryToEat();
		}
	}
	
	private void updateThirst(float deltaGameHours) {
		s_hoursNotDrank += deltaGameHours;
		int newThirstStage= (int) (s_hoursNotDrank / stats.getRace().getThirstPeriod());
		if (newThirstStage > s_stageOfThirst) {
			tryToDrink();
		}
	}
	
	private boolean tryToEat() {
		int newStage = (int) (s_hoursNotEaten / stats.getRace().getHungerPeriod());
		if (source.getFood() > 0 && newStage > s_stageOfHunger) {
			source.addFood(-stats.getRace().getAmountEaten()*(newStage - s_stageOfHunger));
			params.messageKey = "hasEaten";
			params.setMessageParams();
			changed(params);
			for (int i = 0; i < stats.getRace().getHungerRecovery() && s_stageOfHunger > 0; ++i) {
				gameState.getSurvivalManager().removeModidifiers(stats, SurvivalHazard.HUNGER, s_stageOfHunger);
				--s_stageOfHunger;
			}
			s_hoursNotEaten = s_stageOfHunger * stats.getRace().getHungerPeriod();
			return true;
		} else {
			params.messageKey = "couldNotEat";
			params.setMessageParams(gameState.getSurvivalManager().getStageName(SurvivalHazard.HUNGER, newStage).toLowerCase(Locale.ENGLISH));
			changed(params);
			if (newStage > s_stageOfHunger) {
				for (int i = 1; i <= newStage - s_stageOfHunger; ++i) {
					gameState.getSurvivalManager().addModifiers(stats, SurvivalHazard.HUNGER, s_stageOfHunger+i);
				}
				s_stageOfHunger = newStage;
			}
			if (Configuration.getPauseOnBadChange() && !GameState.isCombatInProgress()) {
				gameState.pauseGame();
			}
			return false;
		}
	}
	
	private boolean tryToDrink() {
		int newStage = (int) (s_hoursNotDrank / stats.getRace().getThirstPeriod());
		if (source.getWater() > 0  && newStage > s_stageOfThirst) {
			source.addWater(-stats.getRace().getAmountDrank()*(newStage - s_stageOfThirst));
			params.messageKey = "hasDrank";
			params.setMessageParams();
			changed(params);
			for (int i = 0; i < stats.getRace().getThirstRecovery() && s_stageOfThirst > 0; ++i) {
				gameState.getSurvivalManager().removeModidifiers(stats, SurvivalHazard.THIRST, s_stageOfThirst);
				--s_stageOfThirst;
			}
			s_hoursNotDrank = s_stageOfThirst * stats.getRace().getThirstPeriod();
			return true;
		} else {
			params.messageKey = "couldNotDrink";
			params.setMessageParams(gameState.getSurvivalManager().getStageName(SurvivalHazard.THIRST, newStage).toLowerCase(Locale.ENGLISH));
			changed(params);
			if (newStage > s_stageOfThirst) {
				for (int i = 1; i <= newStage - s_stageOfThirst; ++i) {
					gameState.getSurvivalManager().addModifiers(stats, SurvivalHazard.THIRST, s_stageOfThirst+i);
				}
				s_stageOfThirst = newStage;
			}
			if (Configuration.getPauseOnBadChange() && !GameState.isCombatInProgress()) {
				gameState.pauseGame();
			}
			return false;
		}
	}
	
	public static class SurvivalChange {
		public String messageKey;
		public Array<Object> messageParams = new Array<Object>();
		public boolean updateFinished;
		
		private void setMessageParams(Object... params) {
			messageParams.clear();
			for (Object object : params) {
				messageParams.add(object);
			}
		}
		
	}
}
