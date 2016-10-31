package mg.fishchicken.screens.start;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.Race;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamestate.characters.Skills;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class RaceInfoPanel extends Table {

	private Race race;
	private Label hp, mp, sp, ap, exp, maxEncumberance, speed, sleep, thirst, hunger, skills, inventory;
	
	public RaceInfoPanel(RaceInfoPanelStyle style, boolean hideInventory) {
		super();
		
		Table col1 = new Table();
		Table col2 = new Table();
		add(col1).fillX().align(Align.top).prefWidth(style.colWidth).padRight(style.colMargin);
		add(col2).fillX().align(Align.top).prefWidth(style.colWidth);
		
		
		col1.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "stats"), style.headingStyle)).fill().expandX();
		col1.row();
		
		col1.add(new Label(Strings.getString(ModifiableStat.STRING_TABLE, "hitPoints"), style.subheadingStyle)).fill().padBottom(style.subHeadingMarginBottom);
		col1.row();
		hp = new Label("", style.valueStyle);
		col1.add(hp).fill();
		col1.row();
		
		col1.add(new Label(Strings.getString(ModifiableStat.STRING_TABLE, "mana"), style.subheadingStyle)).fill().padTop(style.subHeadingMarginTop).padBottom(style.subHeadingMarginBottom);
		col1.row();
		mp = new Label("", style.valueStyle);
		col1.add(mp).fill();
		col1.row();
		
		col1.add(new Label(Strings.getString(ModifiableStat.STRING_TABLE, "stamina"), style.subheadingStyle)).fill().padTop(style.subHeadingMarginTop).padBottom(style.subHeadingMarginBottom);
		col1.row();
		sp = new Label("", style.valueStyle);
		col1.add(sp).fill();
		col1.row();
		
		col1.add(new Label(Strings.getString(ModifiableStat.STRING_TABLE, "actionPoints"), style.subheadingStyle)).fill().padTop(style.subHeadingMarginTop).padBottom(style.subHeadingMarginBottom);
		col1.row();
		ap = new Label("", style.valueStyle);
		col1.add(ap).fill();
		col1.row();
		
		col1.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "experienceMultiplier"), style.subheadingStyle)).fill().padTop(style.subHeadingMarginTop).padBottom(style.subHeadingMarginBottom);
		col1.row();
		exp = new Label("", style.valueStyle);
		col1.add(exp).fill();
		col1.row();
		
		col1.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "encumberance"), style.headingStyle)).fill().padTop(style.headingMarginTop).padBottom(style.headingMarginBottom);
		col1.row();
		maxEncumberance = new Label("", style.valueStyle);
		col1.add(maxEncumberance).fill();
		col1.row();
		
		col1.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "speed"), style.headingStyle)).fill().padTop(style.headingMarginTop).padBottom(style.headingMarginBottom);
		col1.row();
		speed = new Label("", style.valueStyle);
		col1.add(speed).fill();
		col1.row();
		
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "survival"), style.headingStyle)).fill().expandX().padBottom(style.headingMarginBottom);
		col2.row();
		
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "sleep"), style.subheadingStyle)).fill().padBottom(style.subHeadingMarginBottom);
		col2.row();
		sleep = new Label("", style.valueStyle);
		sleep.setWrap(true);
		col2.add(sleep).fill();
		col2.row();
		
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "hunger"), style.subheadingStyle)).fill().padTop(style.subHeadingMarginTop).padBottom(style.subHeadingMarginBottom);
		col2.row();
		hunger = new Label("", style.valueStyle);
		hunger.setWrap(true);
		col2.add(hunger).fill();
		col2.row();
		
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "thirst"), style.subheadingStyle)).fill().padTop(style.subHeadingMarginTop).padBottom(style.subHeadingMarginBottom);
		col2.row();
		thirst= new Label("", style.valueStyle);
		thirst.setWrap(true);
		col2.add(thirst).fill();
		col2.row();
		
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "startingSkills"), style.headingStyle)).fill().padTop(style.headingMarginTop).padBottom(style.headingMarginBottom);
		col2.row();
		skills = new Label("", style.valueStyle);
		skills.setWrap(true);
		col2.add(skills).fill();
		
		if (!hideInventory) {
			col2.row();
			col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "inventory"), style.headingStyle)).fill().padTop(style.headingMarginTop).padBottom(style.headingMarginBottom);
			col2.row();
			inventory = new Label("", style.valueStyle);
			inventory.setWrap(true);
			col2.add(inventory).fill();
			col2.row();
		}
	}
	
	public void setRace(Race race) {
		this.race = race;
		recalculateInfo();
	}
	
	private void recalculateInfo() {
		
		// STATS
		// hp
		hp.setText(Strings.getString(GameCharacter.STRING_TABLE, "hpMaxAndRecovery", race.getMaxHPGain(), race.getHPRecovery()));
		// mp
		mp.setText(Strings.getString(GameCharacter.STRING_TABLE, "mpMaxAndRecovery", race.getMaxMPGain(), race.getMPRecovery()));
		// sp
		sp.setText(Strings.getString(GameCharacter.STRING_TABLE, "spMaxAndRecovery", race.getMaxSPGain(), race.getSPRecovery()));
		// ap
		ap.setText(Integer.toString(race.getMaxAP()));
		// exp
		exp.setText(MathUtil.toUIString(race.getExperienceGainMultiplier()));
		
		// ENCUMBERANCE
		maxEncumberance.setText(Strings.getString(GameCharacter.STRING_TABLE,
				"encumberanceValues", MathUtil.toUIString(race.getMaxEncumbrance() / 1000f),
				race.getMaxRations(), race.getMaxWater()));
		
		// SPEED
		speed.setText(Strings.getString(GameCharacter.STRING_TABLE,
				"speedValues", race.getWalkSpeed(),
				race.getDetectingTrapsSpeed(), race.getSneakingSpeed()));
		
		// SURVIVAL
		
		// sleep
		sleep.setText(race.getSleepPeriod() > 0 ? Strings.getString(GameCharacter.STRING_TABLE, "sleepValues",
				race.getSleepPeriod(), race.getMoreTiredAfter()) : Strings.getString(GameCharacter.STRING_TABLE,
				"noNeedToSleep"));

		// hunger
		hunger.setText(race.getHungerPeriod() > 0 ? Strings.getString(GameCharacter.STRING_TABLE, "hungerValues",
				race.getAmountEaten(), race.getHungerPeriod(), race.getHungerRecovery()) : Strings.getString(
				GameCharacter.STRING_TABLE, "noNeedToEat"));

		// thirst
		thirst.setText(race.getThirstPeriod() > 0 ? Strings.getString(GameCharacter.STRING_TABLE, "thirstValues",
				race.getAmountDrank(), race.getThirstPeriod(), race.getThirstRecovery()) : Strings.getString(
				GameCharacter.STRING_TABLE, "noNeedToDrink"));
		
		// SKILLS
		StringBuilder fsb = StringUtil.getFSB();
		Array<String> skills = new Array<String>();
		Skills inherentSkills = race.getInherentSkills();
		for (Skill skill : Skill.values()) {
			int rank = inherentSkills.getSkillRank(skill);
			if (rank != 0) {
				skills.add(skill.toUIString()+" "+rank);
			}
		}
		fsb.append(skills.toString(", "));
		
		this.skills.setText(fsb.toString());

		fsb.setLength(0);
		
		// INVENTORY
		if (inventory != null) {
			Array<InventoryItem> items = race.getInventory().getAllItems();
			Array<String> itemNames = new Array<String>();
			for (InventoryItem item : items) {
				itemNames.add(item.getName());
			}
			
			fsb.append(itemNames.toString(", "));
		this.inventory.setText(fsb.toString());
		}
		StringUtil.freeFSB(fsb);
	}
	
	public static class RaceInfoPanelStyle {
		public LabelStyle headingStyle, subheadingStyle, valueStyle;
		public int 	headingMarginTop = 10, 
				   	headingMarginBottom = 0,
				   	subHeadingMarginTop = 5,
				   	subHeadingMarginBottom = 0,
				   	colMargin = 10,
				   	colWidth = 200;
	}
}
