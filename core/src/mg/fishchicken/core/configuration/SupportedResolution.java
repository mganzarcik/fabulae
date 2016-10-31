package mg.fishchicken.core.configuration;

import mg.fishchicken.graphics.Resolution;

public enum SupportedResolution implements Resolution {

	a43w800(800, 600, "4:3"),
	a43w1024(1024, 768, "4:3"),
	a43w1152(1152, 864, "4:3"),
	a43w1280(1280, 969, "4:3"),
	a43w1400(1400, 1050, "4:3"),
	a43w1600(1600, 1200, "4:3"),
	
	a169w1280(1280, 720, "16:9"),
	a169w1360(1360, 768, "16:9"),
	a169w1366(1366, 768, "16:9"),
	a169w1600(1600, 900, "16:9"),
	a169w1920(1920, 1080, "16:9"),
	a169w2048(2048, 1152, "16:9"),
	a169w2560(2560, 1440, "16:9"),
	
	a1610w1366(1280, 800, "16:10"),
	a1610w1600(1440, 900, "16:10"),
	a1610w1920(1680, 1050, "16:10"),
	a1610w2048(1920, 1200, "16:10"),
	a1610w2560(2560, 1600, "16:10");
	
	
	private int width, height;
	private String aspect;
	
	private SupportedResolution(int width, int height, String aspect) {
		this.width = width;
		this.height = height;
		this.aspect = aspect;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getAspectRatio() { 
		return aspect;
	}
}
