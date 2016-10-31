package mg.fishchicken.core.logging;

import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.graphics.Color;

public class GameLogLogger implements Logger {

	private Color color;
	private static GameLogLogger logger;
	
	public static GameLogLogger get(Color color) {
		if (logger == null) {
			logger = new GameLogLogger();
		}
		logger.setColor(color);
		return logger;
	}
	
	private GameLogLogger() {
		
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	@Override
	public void logMessage(String message) {
		UIManager.logMessage(message, color, true);
	}

}
