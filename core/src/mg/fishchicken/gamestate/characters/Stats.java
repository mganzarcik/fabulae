package mg.fishchicken.gamestate.characters;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.configuration.ExperienceTable;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.Gender;
import mg.fishchicken.gamelogic.characters.Race;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.items.Armor;
import mg.fishchicken.gamelogic.inventory.items.Armor.ArmorClass;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;
import mg.fishchicken.gamestate.ObservableState;
import mg.fishchicken.gamestate.Observer;
import mg.fishchicken.gamestate.characters.Skills.SkillChange;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Stats extends ObservableState<Stats, Stats.StatChange> implements ModifierContainer {
	
	public static final String XML_SKILL_INCREASES_THIS_LEVEL = "skillIncreasesThisLevel";
	
	private int s_level;
	private int s_experience;
	private int s_experienceValue;
	private Gender s_gender;
	private Race s_race;
	private boolean s_invincible;
	private int s_hpMax;
	private float s_hpAct;
	private int s_mpMax;
	private int s_mpAct;
	private int s_spMax;
	private int s_spAct;
	private int s_apAct;
	private int s_encumbrance;
	private int s_ar;
	private float s_perkPoints;
	private float s_skillPoints;
	private ObjectMap<Skill, Integer> skillIncreasesThisLevel;
	private Array<Modifier> modifiers, armorModifiers;
	private Skills skills;
	private GameCharacter character;
	private Inventory inventory;
	
	private ObjectMap<Skill, Boolean> lastSkillChecks;
	
	public Stats(GameCharacter character, Inventory inventory) {
		s_level = 1;
		s_gender = Gender.Male; // default value
		skillIncreasesThisLevel = new ObjectMap<Skill, Integer>();
		modifiers = new Array<Modifier>();
		armorModifiers = new Array<Modifier>();
		skills = new Skills(this);
		skills.addObserver(new Observer<Skills, Skills.SkillChange>() {
			@Override
			public void hasChanged(Skills stateObject, SkillChange changes) {
				if (changes.getSkill() == Skill.ARMOR) {
					updateArmorModifiers();
				}
			}
		});
		this.character = character;
		this.inventory = inventory;
		lastSkillChecks = new ObjectMap<Skill, Boolean>();
	}
	
	@Override
	public String getName() {
		return character.getName();
	}
	
	public Skills skills() {
		return skills;
	}
	
	/**
	 * Makes sure all stat values are below or equal
	 * their maximum possible values.
	 * 
	 */
	public void boxStats() {
		s_hpAct = MathUtil.boxValue(s_hpAct, 0, getHPMax());
		s_spAct = MathUtil.boxValue(s_spAct, 0, getSPMax());
		s_mpAct = MathUtil.boxValue(s_mpAct, 0, getMPMax());
		s_apAct = MathUtil.boxValue(s_apAct, 0, getAPMax());
		changed(null);
	}
	
	public int getLevel() {
		return s_level;
	}

	public Stats setLevel(int s_level) {
		this.s_level = s_level;
		changed(null);
		return this;
	}

	public int getExperienceValue() {
		return s_experienceValue;
	}

	public Stats setExperienceValue(int experienceValue) {
		s_experienceValue = experienceValue;
		changed(null);
		return this;
	}

	public int getExperience() {
		return s_experience;
	}

	public Stats setExperience(int experience) {
		this.s_experience = experience;
		changed(null);
		return this;
	}
	
	/**
	 * Gives the supplied amount of experience to this character.
	 * 
	 * The supplied amount will be modified by the character's race experience
	 * gain multiplier before it is actually added.
	 * 
	 * @param amount
	 */
	public void giveExperience(int amount) {
		amount = (int) (amount * getRace().getExperienceGainMultiplier());
		setExperience(s_experience+amount);
		if (amount != 0) {
			Log.logLocalized("experienceAwarded", LogType.CHARACTER, getName(), amount);
		}
		ExperienceTable expTable = Configuration.getExperienceTable(); 
		int requiredExp = expTable.getRequiredExperienceTotalForLevel(getLevel()+1);
		 
		while (requiredExp <= getExperience()) {
			levelUp();
			requiredExp = expTable.getRequiredExperienceTotalForLevel(getLevel()+1);
		}
	}
	
	public void levelUp() {
		++s_level;
		if (character.isMemberOfPlayerGroup()) {
			Log.logLocalized("leveledUp", LogType.CHARACTER, getName(), s_level);
		}
		s_skillPoints += Configuration.getSkillPointGainPerLevel();
		s_perkPoints += Configuration.getPerkPointGainPerLevel();

		skillIncreasesThisLevel.clear();
		int hpGain = 1 + (getRace().getMaxHPGain() > 0 ? GameState.getRandomGenerator().nextInt(getRace().getMaxHPGain()) : 0);
		int mpGain = 1 + (getRace().getMaxMPGain() > 0 ? GameState.getRandomGenerator().nextInt(getRace().getMaxMPGain()) : 0);
		int spGain = 1 + (getRace().getMaxSPGain() > 0 ? GameState.getRandomGenerator().nextInt(getRace().getMaxSPGain()) : 0);
		setHPMax(getHPMax()+hpGain);
		setMPMax(getMPMax()+mpGain);
		setSPMax(getSPMax()+spGain);
		addToHP(hpGain);
		addToMP(mpGain);
		addToSP(spGain);
		changed(null);
	}

	public Race getRace() {
		return s_race;
	}

	public Stats setRace(Race race) {
		this.s_race = race;
		changed(null);
		return this;
	}
	
	public boolean isInvincible() {
		return s_invincible;
	}

	public Stats setInvincible(boolean invincible) {
		s_invincible = invincible;
		changed(null);
		return this;
	}

	public int getHPMax() {
		return (int)getModifiedStat(s_hpMax, ModifiableStat.HITPOINTS);
	} 

	public Stats setHPMax(int hpMax) {
		this.s_hpMax = hpMax;
		changed(null);
		return this;
	}

	public float getHPAct() {
		return s_hpAct;
	}

	/**
	 * Sets the current hit points of the character
	 * to the supplied value.
	 * @param hpAct
	 */
	public Stats setHPAct(float hpAct) {
		this.s_hpAct = hpAct;
		changed(null);
		return this;
	}
	
	/**
	 * Adds the supplied value to the character's current hit points. The value 
	 * will be boxed so that after adding, the HP never goes below zero and
	 * above HP max.
	 * 
	 * This is not considered an attack and even if the character dies as a result
	 * of this modification, the death will unattributed. If you want to record
	 * who killed the character, use {@link #dealDamage(int, GameObject)}.
	 * 
	 * @param value
	 * @return the actual number of HP added
	 */
	public float addToHP(float value) {
		float boxed = MathUtil.boxValue(s_hpAct+value, 0, getHPMax());
		float returnValue = boxed - s_hpAct;
		setHPAct(boxed);
		return returnValue;
	}
	
	public int getMPMax() {
		return (int)getModifiedStat(s_mpMax, ModifiableStat.MANA);
	}

	public Stats setMPMax(int mpMax) {
		this.s_mpMax = mpMax;
		changed(null);
		return this;
	}

	public int getMPAct() {
		return s_mpAct;
	}

	/**
	 * Adds the supplied value to the character's current mana points. The value 
	 * will be boxed so that after adding, the MP never goes below zero and
	 * above MP max.
	 * 
	 * @param value
	 * @return the actual number of MP added
	 */
	public int addToMP(int value) {
		int boxed = MathUtil.boxValue(s_mpAct+value, 0, getMPMax());
		int returnValue = boxed - s_mpAct;
		setMPAct(boxed);
		return returnValue;
	}
	
	public Stats setMPAct(int mpAct) {
		this.s_mpAct = mpAct;
		changed(null);
		return this;
	}

	public int getSPMax() {
		return (int)getModifiedStat(s_spMax, ModifiableStat.STAMINA);
	}

	public Stats setSPMax(int spMax) {
		this.s_spMax = spMax;
		changed(null);
		return this;
	}
	
	/**
	 * Adds the supplied value to the character's current stamina points. The value 
	 * will be boxed so that after adding, the SP never goes below zero and
	 * above SP max.
	 * 
	 * @param value
	 * @return the actual number of SP added
	 */
	public int addToSP(int value) {
		int boxed = MathUtil.boxValue(s_spAct+value, 0, getSPMax());
		int returnValue = boxed - s_spAct;
		setSPAct(boxed);
		return returnValue;
	}

	public int getSPAct() {
		return s_spAct;
	}

	public Stats setSPAct(int spAct) {
		this.s_spAct = spAct;
		changed(null);
		return this;
	}

	public int getAPMax() {
		return (int)getModifiedStat(getRace().getMaxAP(), ModifiableStat.ACTIONPOINTS);
	}

	public int getAPAct() {
		return s_apAct;
	}

	public Stats setAPAct(int apAct) {
		this.s_apAct = apAct;
		changed(null);
		return this;
	}

	/**
	 * Adds the supplied value to the character's current action points. The value 
	 * will be boxed so that after adding, the AP never goes below zero and
	 * above AP max.
	 * 
	 * @param value
	 * @return the actual number of AP added
	 */
	public int addToAP(int value) {
		int boxed = MathUtil.boxValue(s_apAct+value, 0, getAPMax());
		int returnValue = boxed - s_apAct;
		setAPAct(boxed);
		return returnValue;
	}
	
	public int getAR() {
		return (int)getModifiedStat(s_ar, ModifiableStat.ARMORRATING);
	}

	public Stats setAR(int ar) {
		this.s_ar = ar;
		changed(null);
		return this;
	}
	
	public int getPerkPoints() {
		return (int)s_perkPoints;
	}

	public Stats setPerkPoints(float perkPoints) {
		s_perkPoints = perkPoints;
		changed(null);
		return this;
	};
	
	public int getSkillPoints() {
		return (int)s_skillPoints;
	}

	public Stats setSkillPoints(float skillPoints) {
		s_skillPoints = skillPoints;
		changed(null);
		return this;
	}
	
	/**
	 * Modifies the current load by the 
	 * supplied number of grams and returns the 
	 * resulting load in grams.
	 * @param weight
	 * @return
	 */
	public int modifyLoad(int weight) {
		s_encumbrance += weight;
		
		int encubranceMod = s_encumbrance - getRace().getMaxEncumbrance();
		if (encubranceMod > 0) {
			encubranceMod = encubranceMod / 10000;
		} else {
			encubranceMod = 0;
		}
		if (encubranceMod != 0) {
			Modifier encumbranceModifier = new Modifier("encumberanceModifierHelloHelpIAmStuckInTheIDGenerator", Strings.getString(GameCharacter.STRING_TABLE, "Encumberance")); 
			encumbranceModifier.setMod(ModifiableStat.ACTIONPOINTS, -encubranceMod);
			addModifier(encumbranceModifier);
		} else {
			removeModifier("encumberanceModifierHelloHelpIAmStuckInTheIDGenerator");
		}
		updateArmorModifiers();
		changed(null);
		return s_encumbrance;
	}
	
	/**
	 * Returns the current load of the character in kilograms.
	 */
	public float getLoad() {
		return (float)s_encumbrance/1000f;
	}
	
	/**
	 * Returns the maximum load of the character in kilograms.
	 */
	public float getMaximumLoad() {
		return (float)getRace().getMaxEncumbrance() / 1000f;
	}
	
	/**
	 * Adds the supplied modifier to this character.
	 * 
	 * If the modifier has a non empty id, this will
	 * first remove any modifiers with the same id
	 * from the character and only then add the new one,
	 * ensuring that the character only has
	 * one active modifier with a given id.
	 * @param modifier
	 */
	public void addModifier(Modifier modifier) {
		if (!StringUtil.nullOrEmptyString(modifier.getId())) {
			removeModifier(modifier.getId());
		}
		modifiers.add(modifier);
		onModifierChange();
	}
	
	/**
	 * Removes all modifiers with the supplied id
	 * from this character.
	 * 
	 * @param modifierId
	 */
	public void removeModifier(String modifierId) {
		Iterator<Modifier> iterator = modifiers.iterator();
		while (iterator.hasNext()) {
			Modifier mod = iterator.next();
			if (modifierId.equals(mod.getId())) {
				iterator.remove();
			}
		}
		onModifierChange();
	}
	
	public void removeModifier(Modifier modifier) {
		modifiers.removeValue(modifier, false);
		onModifierChange();
	}
	
	/**
	 * Returns an iterator of all active stat modifiers on the character. 
	 * @return
	 */
	public Iterator<Modifier> getModifiers() {
		return modifiers.iterator();
	}
	
	/**
	 * Returns a list of all modifiers that modify the supplied
	 * stat. 
	 * @param mod
	 * @param includeMultipliers - if true, multiplier
	 * modifiers for this stat will be included as well
	 * @return
	 */
	public ObjectMap<ModifiableStat, Array<Modifier>> getAllModifiersForStat(ModifiableStat stat, boolean includeMultipliers) {
		ObjectMap<ModifiableStat, Array<Modifier>> returnValue = new ObjectMap<ModifiableStat, Array<Modifier>>();
		ModifiableStat multiplierStat = null;
		try {
			multiplierStat = ModifiableStat.valueOf(stat.toString()+ModifiableStat.MULTIPLIER_SUFFIX);
		} catch (IllegalArgumentException e) {
			includeMultipliers = false;
		}
		for (Modifier modifier : modifiers) {
			if (!modifier.isEmpty(stat)) {
				addModifierToReturnValue(returnValue, stat, modifier);
			} if (includeMultipliers && !modifier.isEmpty(multiplierStat)) {
				addModifierToReturnValue(returnValue, multiplierStat, modifier);
			}
		}
		return returnValue;
	}
	
	private void addModifierToReturnValue(ObjectMap<ModifiableStat, Array<Modifier>> returnValue, ModifiableStat stat, Modifier modifier) {
		Array<Modifier> modifiers;
		if (!returnValue.containsKey(stat)) {
			modifiers = new Array<Modifier>();
			returnValue.put(stat, modifiers);
		} else {
			modifiers = returnValue.get(stat);
		}
		modifiers.add(modifier);
	}

	@Override
	public void onModifierChange() {
		boxStats();
	}
	
	public float getModifiedStat(float unmodifiedValue, ModifiableStat stat) {
		float returnValue = unmodifiedValue;
		
		for (Modifier mod : modifiers) {
			if (stat.isMultiplier()) {
				returnValue *= mod.getMod(stat);
			} else {
				returnValue += mod.getMod(stat);
			}
		}
		
		if (returnValue < 0) {
			returnValue = 0;
		}
		return returnValue;
	}
	
	/**
	 * Returns the number of times this skill
	 * has been increased this level.
	 * @param skill
	 * @return
	 */
	public int getSkillIncreasesThisLevel(Skill skill) {
		if (skillIncreasesThisLevel.containsKey(skill)) {
			return skillIncreasesThisLevel.get(skill);
		}
		return 0;
	}
	
	/**
	 * Adds one to the number of times this skill
	 * has been increased this level.
	 * @param skill
	 */
	public void incrementSkillIncreasesThisLevel(Skill skill) {
		skillIncreasesThisLevel.put(skill, getSkillIncreasesThisLevel(skill)+1);
		changed(null);
	}
	
	/**
	 * Removes one from the number of times this skill
	 * has been increased this level. Cannot go below zero.
	 * 
	 * @param skill
	 */
	public void decrementSkillIncreasesThisLevel(Skill skill) {
		int currentValue = getSkillIncreasesThisLevel(skill);
		if (currentValue > 0) {
			skillIncreasesThisLevel.put(skill, currentValue-1);
		}
		changed(null);
	}
	
	public Gender getGender() {
		return s_gender;
	}

	public Stats setGender(Gender gender) {
		s_gender = gender;
		changed(null);
		return this;
	}
	
	/**
	 * Returns the amount of food this character would fine based on the hunting skill and
	 * any active modifiers.
	 * @return
	 */
	public int getAmountOfFoodFound() {
		int huntingSkill = skills.getSkillRank(Skill.HUNTING);
		int amount = MathUtils.floor(huntingSkill/2f) + GameState.getRandomGenerator().nextInt(huntingSkill*2);
		if (amount < 1) {
			amount = 1;
		}
		return (int) getModifiedStat(amount, ModifiableStat.RATIONSFOUND);
	}
	
	public int getAPCostToSpellModifier() {
		return (int) getModifiedStat(0, ModifiableStat.APCOSTTOSPELL);
	}
	
	public int getMPCostToSpellModifier() {
		return (int) getModifiedStat(0, ModifiableStat.MPCOSTTOSPELL);
	}
	
	public int getAPCostToAttack() {
		return getAPCostToAttackModified(Configuration.getAPCostAttack());
	}

	public int getAPCostToAttackModified(int baseCost) {
		InventoryItem rightEquipped = inventory.getEquipped(ItemSlot.RIGHTHAND);
		InventoryItem leftEquipped = inventory.getEquipped(ItemSlot.LEFTHAND);
		
		if (rightEquipped instanceof Weapon) {
			return getAPCostToAttackModified(baseCost, (Weapon)rightEquipped);
		}
		
		if (leftEquipped instanceof Weapon) {
			return getAPCostToAttackModified(baseCost, (Weapon)leftEquipped);
		}
		
		return getAPCostToAttackModified(baseCost, null);
	}
	
	private int getAPCostToAttackModified(int apCost, Weapon weapon) {	
		if (weapon != null) {
			int skill = skills().getSkillRank(weapon.getWeaponSkill());
			if (skill > 2) {
				apCost -= 1;
			} 
			if (skill > 4) {
				apCost -= 1;
			}
		} else {
			int skill = skills().getSkillRank(Skill.UNARMED);
			if (skill > 0) {
				apCost -= 1;
			} 
			if (skill > 2) {
				apCost -= 1;
			}
			if (skill > 4) {
				apCost -= 1;
			}
		}
		
		int returnValue = (int) getModifiedStat((int) getModifiedStat(apCost, ModifiableStat.APCOSTTOATTACK),
				ModifiableStat.APCOSTTOATTACKMULTIPLIER);
		if (returnValue < 1) {
			returnValue = 1;
		}
		
		return returnValue;
	}
	
	/**
	 * Returns the Chance to Hit for the supplied hand.
	 * 
	 * @param slot
	 * @return
	 */
	public int getChanceToHit(ItemSlot slot) {
		return getChanceToHit(slot.getSlot());
	}
	
	public int getChanceToHit(int slot) {
		float returnValue = 10;
		InventoryItem weapon = inventory.getEquipped(slot);
		if (weapon instanceof Weapon) {
			returnValue += skills.getSkillRank(((Weapon)weapon).getWeaponSkill())+((Weapon)weapon).getWeaponBonus();
		} else {
			returnValue += skills.getSkillRank(Skill.UNARMED)*1.5f;
		}
		if (isDualWielding()) {
			int dualWieldingSkill = skills.getSkillRank(Skill.DUALWIELDING);
			if (ItemSlot.RIGHTHAND.getSlot() == slot) {
				switch (dualWieldingSkill) {
					case 0: returnValue -= 4; break;
					case 1:  returnValue -= 2; break;
					case 2: case 3:  returnValue -= 1; break;
					default: break;
				}
			} else if (ItemSlot.LEFTHAND.getSlot() == slot) {
				switch (dualWieldingSkill) {
				case 0: case 1:  returnValue -= 4; break;
				case 2: returnValue -= 2; break;
				case 3: case 4: returnValue -= 1; break;
				default: break;
			}
		}
		}
		return Math.round(getModifiedStat((int)(returnValue*5), ModifiableStat.CHANCETOHIT));
	}
	
	/**
	 * Returns the Dodge or Parry chance if attacked by the supplied weapon (can be null for unarmed attacks).
	 * In case a weapon is equipped, DoPC for that weapon's hand is returned.
	 * In case of dual wielding, the main hand's DoPC is returned.
	 *  
	 * @return
	 */
	public int getDodgeOrParryChance(Weapon attackingWeapon) {
		boolean rightEquipped = inventory.getEquipped(ItemSlot.RIGHTHAND) != null;
		boolean unarmed = attackingWeapon == null;
		boolean ranged = unarmed ? false : attackingWeapon.isRanged();
		
		if (rightEquipped) {
			return getDodgeOrParryChance(ItemSlot.RIGHTHAND, ranged, unarmed);
		} 
		return getDodgeOrParryChance(ItemSlot.LEFTHAND, ranged, unarmed);
	}
	
	/**
	 * Returns the Dodge or Parry chance for the supplied hand.
	 * 
	 * @param hand
	 * @return
	 */
	public int getDodgeOrParryChance(ItemSlot hand, boolean rangedAttack, boolean unarmedAttack) {
		int returnValue = skills.getSkillRank(Skill.DODGE);
		InventoryItem weapon = inventory.getEquipped(hand);
		boolean unarmedDefense = false;
		if (weapon instanceof Weapon) {
			if (!((Weapon) weapon).isRanged() && !rangedAttack) {
				returnValue += skills.getSkillRank(((Weapon)weapon).getWeaponSkill());
			}
		} else {
			unarmedDefense = true;
			// if the defender is unarmed, he can only add his unarmed skill to his parry chance
			// if the attacker is unarmed as well
			if (unarmedAttack) {
				returnValue += skills.getSkillRank(Skill.UNARMED);
			}
		}
		returnValue = Math.round(getModifiedStat(returnValue*5, ModifiableStat.DODGEPARRY)*getModifiedStat(1, ModifiableStat.DODGEPARRYMULTIPLIER));
		// if the attacker is unarmed, the defenders dodge and parry chance will be halved if he is also not unarmed
		if (unarmedAttack && !unarmedDefense) {
			returnValue /= 2;
		}
		return returnValue;
	}
	
	/**
	 * Returns the modifier to Dodge and Parry that is caused by armor worn.
	 * This is a fraction by which DP should be multiplied before
	 * being used in calculations.
	 * 
	 * @return
	 */
	public void updateArmorModifiers() {
		for (int i = 0; i < armorModifiers.size; ++i) {
			removeModifier(armorModifiers.get(i));
		}
		armorModifiers.clear();
		
		ArmorClass ac = getWornArmorClass(inventory.getAllEquippedArmor());
		Iterator<Modifier> modifiers = Configuration.getModifiersForArmorClass(ac, skills.getSkillRank(Skill.ARMOR));
		while (modifiers.hasNext()) {
			Modifier mod = modifiers
					.next()
					.copy()
					.setName(
							Strings.getString(GameCharacter.STRING_TABLE, "WearingArmor",
									Strings.getString(InventoryItem.STRING_TABLE, ac.toString())));
			addModifier(mod);
			armorModifiers.add(mod);
		}
			
	}
	
	private ArmorClass getWornArmorClass(Array<Armor> armors) {
		int totalWeight = 0;
		for (int i = 0; i < armors.size; ++i) {
			totalWeight += armors.get(i).getWeight();
		}
		
		float ratio = totalWeight / (float)getRace().getMaxEncumbrance();
		for (ArmorClass ac : ArmorClass.values()) {
			if (ratio <= Configuration.getMaxRatioForArmorClass(ac)) {
				return ac;
			}
		}
		
		return ArmorClass.HEAVY;
	}

	/**
	 * Returns true if this character is dual wielding two weapons.
	 * @return
	 */
	private boolean isDualWielding() {
		return inventory.getEquipped(ItemSlot.RIGHTHAND) instanceof Weapon
				&& inventory.getEquipped(ItemSlot.LEFTHAND) instanceof Weapon;
	}
	
	/**
	 * Returns the supplied damage modified by any damage modifiers
	 * that this character currently has active.
	 * @param damage
	 * @return
	 */
	public float applyDamageModifiers(float damage, boolean unarmed) {
		float returnValue = getModifiedStat(damage, unarmed ? ModifiableStat.UNARMEDDAMAGE : ModifiableStat.DAMAGE);
		returnValue = getModifiedStat(returnValue, unarmed ? ModifiableStat.UNARMEDDAMAGEMULTIPLIER
				: ModifiableStat.DAMAGEMULTIPLIER);
		return returnValue;
	}
	
	/**
	 * @see GameCharacter#rollSkillCheck(String)
	 */
	public boolean rollSkillCheck(String skill) {
		return rollSkillCheck(Skill.valueOf(skill.toUpperCase(Locale.ENGLISH)));
	}
	
	/**
	 * @see GameCharacter#rollSkillCheck(Skill, SkillCheckModifier)
	 */
	public boolean rollSkillCheck(String skill, SkillCheckModifier... modifier) {
		return rollSkillCheck(Skill.valueOf(skill.toUpperCase(Locale.ENGLISH)), modifier);
	}
	
	/**
	 * Rolls a check for the given skill using the given modifier
	 * and stores the result in the last skill checks map and also
	 * returns it.
	 * 
	 * Modifier will be substracted from the chance to succeed
	 * when calculating the skill check.
	 * 
	 * This result can later be queried using passedSkillCheck(Skill).
	 * 
	 * @param skill
	 * @param modifier
	 */
	public boolean rollSkillCheck(Skill skill, SkillCheckModifier... skillCheckModifier) {
		boolean returnValue = false;
		int chance = 0;
		if (Skill.CLIMBING.equals(skill) || 
			Skill.SWIMMING.equals(skill) || 
			Skill.SNEAKING.equals(skill) || 
			Skill.PERSUASION.equals(skill) || 
			Skill.HUNTING.equals(skill) || 
			Skill.SCOUTING.equals(skill)) {
				chance = 16 * skills.getSkillRank(skill);
		} 
		
		if (skillCheckModifier != null) {
			for (SkillCheckModifier modifier : skillCheckModifier) {
				if (modifier != null) {
					chance += modifier.getSkillCheckModifier(skill, character);
				}
			}
		}
		
		if (chance > 99) {
			chance = 99;
		}
		int roll = GameState.getRandomGenerator().nextInt(100);
		returnValue =  roll < chance;
		Log.logLocalized("rolledSkillCheck", LogType.SKILLCHECK, this.getName(), skill.toUIString(), chance, roll, (Strings.getString(GameCharacter.STRING_TABLE, returnValue ? "success" : "failure")));
		
		lastSkillChecks.put(skill, returnValue);
		// award XP for successful skill use
		if (returnValue) {
			float div = Skill.PERSUASION.equals(skill) ? 5 : 10;
			int expGained = (int)((100 - chance) / div);
			if (expGained > 0) {
				giveExperience(expGained);
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns true if the last rolled skillcheck
	 * for the given skill passed.
	 * 
	 * @param skill
	 * @return
	 */
	public boolean passedSkillCheck(String skill) {
		return passedSkillCheck(Skill.valueOf(skill.toUpperCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns true if the last rolled skillcheck
	 * for the given skill passed.
	 * 
	 * @param skill
	 * @return
	 */
	public boolean passedSkillCheck(Skill skill) {
		if (lastSkillChecks.containsKey(skill)) {
			return lastSkillChecks.get(skill);
		}
		return false;
	}
	
	private void readSkillIncreasesThisLevel(Element skillIncreasesElement) {
		if (skillIncreasesElement != null) {
			for (int i = 0; i< skillIncreasesElement.getChildCount(); ++i) {
				Element variable = skillIncreasesElement.getChild(i);
				skillIncreasesThisLevel.put(Skill.valueOf(variable.getAttribute(XMLUtil.XML_ATTRIBUTE_NAME).toUpperCase(Locale.ENGLISH)),Integer.valueOf(variable.getAttribute(XMLUtil.XML_ATTRIBUTE_VALUE)));
			}
		}
	}
	
	private void writeSkillIncreasesThisLevel(XmlWriter writer) throws IOException {
		writer.element(XML_SKILL_INCREASES_THIS_LEVEL);
		for (Skill skill : Skill.values()) {
			int rank = getSkillIncreasesThisLevel(skill);
			if (rank > 0) {
				writer.element(Skills.XML_SKILL).attribute(XMLUtil.XML_ATTRIBUTE_NAME, skill.toString()).attribute(XMLUtil.XML_ATTRIBUTE_VALUE, rank).pop();
			}
		}
		writer.pop();
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		skills.loadFromXML(root);
		readSkillIncreasesThisLevel(root.getChildByName(XML_SKILL_INCREASES_THIS_LEVEL));
		XMLUtil.readModifiers(this, root.getChildByName(XMLUtil.XML_MODIFIERS));
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		skills.writeToXML(writer);
		writeSkillIncreasesThisLevel(writer);
		XMLUtil.writeModifiers(this, writer);
	}
	
	public static class StatChange {
		
	}
}
