package mg.fishchicken.core.logging;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.combat.CombatManager;
import mg.fishchicken.gamelogic.crime.CrimeManager;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.quests.Quest;
import mg.fishchicken.gamelogic.survival.SurvivalManager;
import mg.fishchicken.gamelogic.weather.WeatherManager;

import com.badlogic.gdx.graphics.Color;

public class Log {

	public static enum LogType {
		CRIME {
			@Override
			public String getStringTable() {
				return CrimeManager.STRING_TABLE;
			}
			
			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.ORANGE);
			}
		},
		FACTION {
			@Override
			public String getStringTable() {
				return Faction.STRING_TABLE;
			}
			
			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.TEAL);
			}
		},
		COMBAT {
			@Override
			public String getStringTable() {
				return CombatManager.STRING_TABLE;
			}
			
			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.RED);
			}
		},
		SKILLCHECK {
			@Override
			public String getStringTable() {
				return AbstractGameCharacter.STRING_TABLE;
			}

			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.CYAN);
			}
		},
		CHARACTER {
			@Override
			public String getStringTable() {
				return AbstractGameCharacter.STRING_TABLE;
			}

			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.GREEN);
			}
			
		},
		SURVIVAL {
			@Override
			public String getStringTable() {
				return SurvivalManager.STRING_TABLE;
			}

			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.OLIVE);
			}
			
		},
		INVENTORY {
			@Override
			public String getStringTable() {
				return Inventory.STRING_TABLE;
			}

			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.YELLOW);
			}
			
		},
		INFO {
			@Override
			public String getStringTable() {
				return null;
			}
			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.WHITE);
			}
		},
		ERROR
		{
			@Override
			public String getStringTable() {
				return null;
			}
			@Override
			public Logger getLogger() {
				return ConsoleLogger.get();
			}
		}, 
		DEBUG
		{
			@Override
			public String getStringTable() {
				return null;
			}
			@Override
			public Logger getLogger() {
				return ConsoleLogger.get();
			}
		}, 
		CONDITION
		{
			@Override
			public String getStringTable() {
				return null;
			}
			@Override
			public Logger getLogger() {
				return null;
				//return ConsoleLogger.get();
			}
		}, 
		STATE_MACHINE
		{
			@Override
			public String getStringTable() {
				return null;
			}
			@Override
			public Logger getLogger() {
				//return ConsoleLogger.get();
				return null;
			}
		},
		JOURNAL
		{
			@Override
			public String getStringTable() {
				return Quest.STRING_TABLE;
			}
			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.WHITE);
			}
		},
		WEATHER
		{
			@Override
			public String getStringTable() {
				return WeatherManager.STRING_TABLE;
			}
			@Override
			public Logger getLogger() {
				return GameLogLogger.get(Color.WHITE);
			}
		};
		public abstract String getStringTable();
		public abstract Logger getLogger();
	};

	public static void log(String message, LogType type, Object... parameters) {
		log(Strings.formatString(message, parameters), type.getLogger());
	}
	
	private static void log(String message, Logger logger) {
		if (logger != null) {
			logger.logMessage(message);
		}
	}
	
	public static void logLocalized(String message, LogType type, Object... parameters) {
		logLocalized(type.getStringTable(), message, type, parameters);
	}
	
	public static void logLocalized(String stringTable, String message, LogType type, Object... parameters) {
		log(Strings.getString(stringTable, message, parameters), type.getLogger());
	}
}
