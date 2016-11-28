package mg.fishchicken.gamelogic.characters;

import mg.fishchicken.gamelogic.factions.Faction;

public enum CharacterFilter {
	/**
	 * Sleeping characters will be excluded.
	 */
	AWAKE {
		@Override
		public boolean shouldFilter(AbstractGameCharacter me,
				AbstractGameCharacter character) {
			return character.isAsleep();
		}
	},
	/**
	 * Characters not visible to me (sneaking or invisible) will be excluded.
	 */
	VISIBLE {
		@Override
		public boolean shouldFilter(AbstractGameCharacter me,
				AbstractGameCharacter character) {
			return character.isInvisible() || character.isSneaking();
		}
	},
	/**
	 * Characters not hostile to me or me to them will be excluded.
	 */
	HOSTILE {
		@Override
		public boolean shouldFilter(AbstractGameCharacter me,
				AbstractGameCharacter character) {
			return !Faction.areHostile(me, character);
		}
	},
	/**
	 * Characters without a faction, or those not allied with me, will be
	 * excluded.
	 */
	ALLIED {
		@Override
		public boolean shouldFilter(AbstractGameCharacter me,
				AbstractGameCharacter character) {
			return me.getFaction() == null
					|| (!me.getFaction().isAlliedWith(character));
		}
	},

	/**
	 * Characters of the same faction as mine will be excluded.
	 */
	NOT_SAME_FACTION {
		@Override
		public boolean shouldFilter(AbstractGameCharacter me,
				AbstractGameCharacter character) {
			return character.getFaction().equals(me.getFaction());
		}
	},
	
	/**
	 * Characters that do not have line of sight will be excluded.
	 */
	HAS_SIGHT {
		@Override
		public boolean shouldFilter(AbstractGameCharacter me,
				AbstractGameCharacter character) {
			return character.getLineOfSight() == null;
		}
	},
	/**
	 * Characters not in the Player faction will be excluded.
	 */
	PLAYER_FACTION {
		@Override
		public boolean shouldFilter(AbstractGameCharacter me,
				AbstractGameCharacter character) {
			return character.getFaction() != Faction.PLAYER_FACTION;
		}
	};

	/**
	 * Returns true if the supplied character should be filtered out when
	 * queried for by me.
	 * 
	 * @param me
	 * @param character
	 * @return
	 */
	public abstract boolean shouldFilter(AbstractGameCharacter me,
			AbstractGameCharacter character);
}
