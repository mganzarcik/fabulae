package mg.fishchicken.ui.inventory;

import java.util.Locale;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamestate.characters.Stats;
import mg.fishchicken.ui.TableStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.inventory.SkillChangeButton.SkillChangeButtonStyle;
import mg.fishchicken.ui.inventory.StatLabel.StatLabelStyle;
import mg.fishchicken.ui.tooltips.StatTooltip;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.StatBar;
import com.badlogic.gdx.scenes.scene2d.ui.StatBar.StatBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;

public class StatsComponent extends Table {

	private GameCharacter character;
	private Stats stats;
	private ObjectMap<ModifiableStat, StatLabel> statHeadings;
	private OrderedMap<ModifiableStat, StatLabel> statValues;
	private OrderedMap<ModifiableStat, StatLabel> skillValues;
	private boolean showSkillPoints;
	private Label skillPointsLabel;
	private Array<SkillChangeButton> skillChangeButtons;
	
	public StatsComponent(GameCharacter character, StatsComponentStyle style, int height) {
		this(character, style, null, height, false);
	}
	
	public StatsComponent(GameCharacter character, StatsComponentStyle style, EventListener listener, int height) {
		this(character, style, listener, height, false);
	}
	
	public StatsComponent(final GameCharacter character, StatsComponentStyle style, EventListener listener, int height, boolean showSkillPoints) {
		super();
		style.apply(this);
		this.character = character;
		this.stats = character.stats();
		this.showSkillPoints = showSkillPoints;
		this.skillChangeButtons = new Array<SkillChangeButton>();
		
		int remainingHeight = height;
		
		Table heading = new Table();
		if (style.headingTableStyle != null) {
			style.headingTableStyle.apply(heading);
		}
		Label label = new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"stats")+":", style.headingStyle);
		heading.add(label).fill().expandX().left();
		remainingHeight -= label.getPrefHeight();
		
		statHeadings = new ObjectMap<ModifiableStat, StatLabel>();
		statValues = new OrderedMap<ModifiableStat, StatLabel>();
		skillValues = new OrderedMap<ModifiableStat, StatLabel>();
		
		for (ModifiableStat stat : ModifiableStat.values()) {
			if (stat.isMultiplier()) {
				continue;
			}
			StatLabel statLabel = new StatLabel(character, "", stat.isSkill() ? style.skillNameStyle : style.statNameStyle, stat);
			if (listener != null) {
				statLabel.addListener(listener);
			}
			if (stat.isSkill()) {
				skillValues.put(stat, statLabel);
			} else if (stat.isStat()) {
				statValues.put(stat, statLabel);
			} else {
				continue;
			}
			statLabel = new StatLabel(character, stat.toUIString() + ": ", stat.isSkill() ? style.skillNameStyle : style.statNameStyle, stat);
			if (listener != null) { 
				statLabel.addListener(listener);
			}
			statHeadings.put(stat, statLabel);
		}
		
		Table statsArea = new Table();
		style.statsTableStyle.apply(statsArea);
		ScrollPane backScrollPane = new ScrollPane(statsArea, style.statsPaneStyle);
		
		for (Entry<ModifiableStat, StatLabel> entry : statValues.entries()) {
			statsArea.add(statHeadings.get(entry.key))
					.fill().top().left().expandX();
			remainingHeight -= entry.value.getPrefHeight();
			statsArea.add(entry.value).fill().top().right();
			statsArea.row();
			if (entry.key == ModifiableStat.LEVEL) {
				final StatTooltip tooltip = entry.value.getTooltip();
				StatBar levelProgress = new StatBar(character, false, style.experienceBarStyle) {
					
					@Override
					protected float getStatMax(GameCharacter character) {
						return Configuration.getExperienceTable().getRequiredExperienceTotalForLevel(character.stats().getLevel()+1);
					}
					
					@Override
					protected float getStatCurr(GameCharacter character) {
						return character.stats().getExperience();
					}
				};
				levelProgress.addListener(new InputListener() {
					@Override
					public void enter(InputEvent event, float x, float y,
							int pointer, Actor fromActor) {
						tooltip.updateText(character);
						if (tooltip.shouldDisplay()) {
							UIManager.setToolTip(tooltip);
						}
					}
					@Override
					public void exit(InputEvent event, float x, float y,
							int pointer, Actor toActor) {
						UIManager.hideToolTip();
					}
				});
				remainingHeight -= levelProgress.getPrefHeight()+style.experienceBarMarginTop+style.experienceBarMarginBottom;
				statsArea.add(levelProgress).colspan(2).fill().padTop(style.experienceBarMarginTop).padBottom(style.experienceBarMarginBottom);
				statsArea.row();
			}
		}
		
		add(heading).colspan(2).fill().expandX();
		row();
		add(backScrollPane).fill().top().colspan(2).padRight(style.borderWidth);
		row();
		
		heading = new Table(); 
		if (style.headingTableStyle != null) {
			style.headingTableStyle.apply(heading);
		}
		heading.add(new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"skills")+":", style.headingStyle)).expandX().fill().left();
		if (showSkillPoints) {
			skillPointsLabel = new Label(Integer.toString(stats.getSkillPoints()), style.headingStyle);
			heading.add(new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"skillPoints")+": ", style.headingStyle)).right();
			heading.add(skillPointsLabel).right();
		}
		remainingHeight -= heading.getPrefHeight();
		
		Table skillsArea = new Table();
		style.skillsTableStyle.apply(skillsArea);
		backScrollPane = new ScrollPane(skillsArea, style.skillsPaneStyle);
		backScrollPane.setFadeScrollBars(false);
		backScrollPane.setOverscroll(false, false);
		boolean showButtons = showSkillPoints && stats.getSkillPoints() > 0;
		for (Entry<ModifiableStat, StatLabel> entry : skillValues.entries()) {
			skillsArea.add(statHeadings.get(entry.key))
					.fill().top().left().expandX();
			if (showButtons) {
				SkillChangeButton button = new SkillChangeButton(character.stats(), style.statChangeButtonStyle, entry.value);
				skillChangeButtons.add(button);
				skillsArea.add(button).fill().top().right();
				button.addListener(listener);
			} else {
				skillsArea.add(entry.value).fill().top().right();
			}
			skillsArea.row();
		}
		
		add(heading).colspan(2).fill().expandX().padRight(style.borderWidth);
		row();
		add(backScrollPane).fill().top().colspan(2).prefHeight(remainingHeight);
		
		recomputeStats();
	}
	
	public void resetIncreases() {
		for(SkillChangeButton button : skillChangeButtons) {
			button.resetIncreases();
		}
	}
	
	/**
	 * Tells this component to recompute all displayed stats.
	 * 
	 */
	private void recomputeStats() {
		if (showSkillPoints) {
			skillPointsLabel.setText(Integer.toString(stats.getSkillPoints()));
		}
		statValues.get(ModifiableStat.LEVEL).setText(Integer.toString(stats.getLevel()));
		statValues.get(ModifiableStat.HITPOINTS).setText((int)Math.ceil(stats.getHPAct()) + "/" + stats.getHPMax());
		statValues.get(ModifiableStat.MANA).setText(stats.getMPAct() + "/" + stats.getMPMax());
		statValues.get(ModifiableStat.STAMINA).setText(stats.getSPAct() + "/" + stats.getSPMax());
		statValues.get(ModifiableStat.ACTIONPOINTS).setText(stats.getAPAct() + "/" + stats.getAPMax());
		
		String damageRight = null;
		String damageLeft = null;
		int dpSkillRight = stats.getDodgeOrParryChance(ItemSlot.RIGHTHAND, false, false);
		int dpSkillLeft = stats.getDodgeOrParryChance(ItemSlot.LEFTHAND, false, false);
		int cthSkillRight = stats.getChanceToHit(ItemSlot.RIGHTHAND);
		int cthSkillLeft = stats.getChanceToHit(ItemSlot.LEFTHAND);
		InventoryItem weapon = character.getInventory().getEquipped(ItemSlot.RIGHTHAND);

		if (weapon instanceof Weapon) {
			damageRight = ((Weapon)weapon).getWeaponDamageAsString(character);
		}
		weapon = character.getInventory().getEquipped(ItemSlot.LEFTHAND);
		if (weapon instanceof Weapon) {
			damageLeft = ((Weapon)weapon).getWeaponDamageAsString(character);
		}

		if (damageLeft == null && damageRight == null) {
			int minDamage = Math.round(character.stats().applyDamageModifiers(1+character.stats().skills().getSkillRank(Skill.UNARMED)/2, true));
			int maxDamage = Math.round(character.stats().applyDamageModifiers(character.stats().skills().getSkillRank(Skill.UNARMED)+1, true));
			if (minDamage < 1) {
				minDamage = 1;
			}
			if (maxDamage < minDamage) {
				maxDamage = minDamage;
			}
			damageRight = minDamage == maxDamage ? Integer.toString(minDamage) : minDamage + " - "+maxDamage;
		}
		
		String cth = damageRight != null && damageLeft != null ? cthSkillRight + "% / " + cthSkillLeft : (damageRight != null ? Integer.toString(cthSkillRight) : Integer.toString(cthSkillLeft));
		statValues.get(ModifiableStat.CHANCETOHIT).setText(cth + "% ");
		
		String dp = damageRight != null && damageLeft != null ? dpSkillRight + "% / " + dpSkillLeft : (damageRight != null ? Integer.toString(dpSkillRight) : Integer.toString(dpSkillLeft));
		statValues.get(ModifiableStat.DODGEPARRY).setText(dp + "% ");
		
		String damage = damageRight != null && damageLeft != null ? damageRight + " / " + damageLeft : (damageRight != null ? damageRight : (damageLeft != null ? damageLeft : ""));
		statValues.get(ModifiableStat.DAMAGE).setText(damage);
		
		statValues.get(ModifiableStat.ARMORRATING).setText(stats.getAR() + "% ");
		
		statValues.get(ModifiableStat.APCOSTTOATTACK).setText(Integer.toString(stats.getAPCostToAttack()));
		
		for (Entry<ModifiableStat, StatLabel> entry : skillValues.entries()) {
			entry.value.setText(Integer.toString(stats.skills().getSkillRank(Skill.valueOf(entry.key.toString().toUpperCase(Locale.ENGLISH)))));
		}
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		recomputeStats();
	}
	
	static public class StatsComponentStyle extends TableStyle {
		private int borderWidth, experienceBarMarginTop, experienceBarMarginBottom;
		private TableStyle statsTableStyle, skillsTableStyle;
		private StatLabelStyle skillNameStyle, statNameStyle;
		private LabelStyle headingStyle;
		private TableStyle headingTableStyle;
		private ScrollPaneStyle statsPaneStyle, skillsPaneStyle;
		private SkillChangeButtonStyle statChangeButtonStyle;
		private StatBarStyle experienceBarStyle;
	}
}
