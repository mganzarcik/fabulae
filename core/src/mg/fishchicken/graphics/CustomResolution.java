package mg.fishchicken.graphics;

public class CustomResolution implements Resolution {

	private int width, height;
	
	public CustomResolution(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

}
