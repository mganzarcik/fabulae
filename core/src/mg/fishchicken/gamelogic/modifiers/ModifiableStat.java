package mg.fishchicken.gamelogic.modifiers;

import java.util.Locale;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamestate.characters.Stats;

public enum ModifiableStat {
	
	LEVEL {
		@Override
		public String getDescription(GameCharacter character) {
			Stats stats = character.stats();
			return Strings
					.getString(
							STRING_TABLE,
							toString() + "Description",
							stats.getExperience(),
							Configuration.getExperienceTable()
									.getRequiredExperienceTotalForLevel(
											stats.getLevel() + 1));
		}
	},
	HITPOINTS, STAMINA, MANA, ACTIONPOINTS, ARMORRATING, DODGEPARRY, DODGEPARRYMULTIPLIER, DAMAGE, DAMAGEMULTIPLIER, 
	UNARMEDDAMAGEMULTIPLIER, CHANCETOHIT, APCOSTTOATTACK, APCOSTTOATTACKMULTIPLIER, ARMOR, SWORD, 
	DAGGER, AXE, STAFF, BOW, UNARMED, THROWN, DUALWIELDING, DODGE, CLIMBING, SWIMMING, HUNTING, SCOUTING, SNEAKING, 
	PERSUASION, TRAPS, LOCKPICKING, SOMATIC, ACOUSTIC, MIND, FOCUS,
	UNARMEDDAMAGE {
		@Override
		public boolean isStat() {
			return false;
		}
	},
	APCOSTTOSPELL {
		@Override
		public boolean isStat() {
			return false;
		}
	},
	MPCOSTTOSPELL {
		@Override
		public boolean isStat() {
			return false;
		}
	},
	SPELLDURATION {
		@Override
		public boolean isStat() {
			return false;
		}
	},
	RATIONSFOUND {
		@Override
		public boolean isStat() {
			return false;
		}
	};
	
	public static final String STRING_TABLE = "stats."+Strings.RESOURCE_FILE_EXTENSION;

	public static ModifiableStat valueOf(Skill skill) {
		return ModifiableStat.valueOf(skill.toString().toUpperCase(Locale.ENGLISH));
	}
	
	public Skill toSkill() {
		Skill skill = null;
		try {
			skill = Skill.valueOf(this.toString());
		} catch (Exception e) {

		}
		return skill;
	}

	public boolean isMultiplier() {
		return this.toString().endsWith(MULTIPLIER_SUFFIX);
	}

	public boolean isSkill() {
		return toSkill() != null;
	}
	
	public boolean isStat() {
		return !isSkill();
	}

	public String getSign(float value) {
		if (isMultiplier()) {
			return "x";
		} else if (value > 0) {
			return "+";
		}
		return "";
	}

	public String toUIString() {
		return Strings.getString(STRING_TABLE, toString());
	}
	
	public String getDescription(GameCharacter character) {
		return Strings.getString(STRING_TABLE, toString()+"Description");
	}
	
	public static String MULTIPLIER_SUFFIX = "MULTIPLIER";
}
