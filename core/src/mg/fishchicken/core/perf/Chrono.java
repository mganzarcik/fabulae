package mg.fishchicken.core.perf;

public class Chrono {

	private long start;
	
	public Chrono() {
	}
	
	public long start() {
		start = System.currentTimeMillis();
		return start;
	}
	
	public long getTimeElapsed() {
		return System.currentTimeMillis() - start;
	}
	
	public long getTimeElapsedAndRestart() {
		long returnValue = getTimeElapsed();
		start();
		return returnValue;
	}
	
	public void printTimeElapsed(String message) {
		System.out.println(message+getTimeElapsed());
	}
	
	public void printTimeElapsedAndRestart(String message) {
		System.out.println(message+getTimeElapsedAndRestart());
	}
}
