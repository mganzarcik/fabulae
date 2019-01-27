package mg.fishchicken.gamelogic.characters;

import static mg.fishchicken.gamelogic.characters.AbstractGameCharacter.STRING_TABLE;
import mg.fishchicken.core.i18n.Strings;

public enum Gender {
	MALE {
		@Override
		public String getPronoun() {
			return Strings.getString(STRING_TABLE,"MalePronoun");
		}

		@Override
		public String getObjectPronoun() {
			return Strings.getString(STRING_TABLE,"MaleObjectPronoun");
		}
		
		@Override
		public String getPossesivePronoun() {
			return Strings.getString(STRING_TABLE,"MalePossesivePronoun");
		}
	},
	FEMALE {
		@Override
		public String getPronoun() {
			return Strings.getString(STRING_TABLE,"FemalePronoun");
		}
		@Override
		public String getObjectPronoun() {
			return Strings.getString(STRING_TABLE,"FemaleObjectPronoun");
		}
		@Override
		public String getPossesivePronoun() {
			return Strings.getString(STRING_TABLE,"FemalePossesivePronoun");
		}
	},
	NONE {
		@Override
		public String getPronoun() {
			return Strings.getString(STRING_TABLE,"NeutralPronoun");
		}
		@Override
		public String getObjectPronoun() {
			return Strings.getString(STRING_TABLE,"NeutralObjectPronoun");
		}
		@Override
		public String getPossesivePronoun() {
			return Strings.getString(STRING_TABLE,"NeutralPossesivePronoun");
		}
	};

	public abstract String getPronoun();
	public abstract String getObjectPronoun();
	public abstract String getPossesivePronoun();
}
