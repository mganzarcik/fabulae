package mg.fishchicken.core.logging;

public class ConsoleLogger implements Logger {

	private static ConsoleLogger logger;
	
	public static ConsoleLogger get() {
		if (logger == null) {
			logger = new ConsoleLogger();
		}
		return logger;
	}
	
	private ConsoleLogger() {
	}
	
	@Override
	public void logMessage(String message) {
		System.out.println(message);
	}

}
