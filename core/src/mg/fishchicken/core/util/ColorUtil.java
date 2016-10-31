package mg.fishchicken.core.util;

import com.badlogic.gdx.graphics.Color;

public class ColorUtil {
	public static final Color WHITE_FIFTY = new Color(1, 1, 1, 0.5f);
	public static final Color BLACK_FIFTY = new Color(0, 0, 0, 0.5f);
	public static final Color RED_FIFTY = new Color(1, 0, 0, 0.5f);
	public static final Color GREEN_FIFTY = new Color(0, 1, 0, 0.5f);
	public static final Color BLUE_FIFTY = new Color(0, 0, 1, 0.5f);
	public static final Color LIGHT_GRAY_FIFTY = new Color(0.75f, 0.75f, 0.75f, 0.5f);
	public static final Color GRAY_FIFTY = new Color(0.5f, 0.5f, 0.5f, 0.5f);
	public static final Color DARK_GRAY_FIFTY = new Color(0.25f, 0.25f, 0.25f, 0.5f);
	public static final Color PINK_FIFTY = new Color(1, 0.68f, 0.68f, 0.5f);
	public static final Color ORANGE_FIFTY = new Color(1, 0.78f, 0, 0.5f);
	public static final Color YELLOW_FIFTY = new Color(1, 1, 0, 0.5f);
	public static final Color MAGENTA_FIFTY = new Color(1, 0, 1, 0.5f);
	public static final Color CYAN_FIFTY = new Color(0, 1, 1, 0.5f);
	
	public static final Color WHITE_SEVENFIVE = new Color(1, 1, 1, 0.75f);
	public static final Color BLACK_SEVENFIVE = new Color(0, 0, 0, 0.75f);
	public static final Color RED_SEVENFIVE = new Color(1, 0, 0, 0.75f);
	public static final Color GREEN_SEVENFIVE = new Color(0, 1, 0, 0.75f);
	public static final Color BLUE_SEVENFIVE = new Color(0, 0, 1, 0.75f);
	public static final Color LIGHT_GRAY_SEVENFIVE = new Color(0.75f, 0.75f, 0.75f, 0.75f);
	public static final Color GRAY_SEVENFIVE = new Color(0.5f, 0.5f, 0.5f, 0.75f);
	public static final Color DARK_SEVENFIVE = new Color(0.25f, 0.25f, 0.25f, 0.75f);
	public static final Color PINK_SEVENFIVE = new Color(1, 0.68f, 0.68f, 0.75f);
	public static final Color ORANGE_SEVENFIVE = new Color(1, 0.78f, 0, 0.75f);
	public static final Color YELLOW_SEVENFIVE = new Color(1, 1, 0, 0.75f);
	public static final Color MAGENTA_SEVENFIVE = new Color(1, 0, 1, 0.75f);
	public static final Color CYAN_SEVENFIVE = new Color(0, 1, 1, 0.75f);
	
	private ColorUtil() {
		
	}
	
	public static boolean isLess(Color c1, float value) {
		return c1.r <= value && c1.g <= value && c1.b <= value;
	}
	
	public static boolean isMore(Color c1, float value) {
		return c1.r >= value && c1.g >= value && c1.b >= value;
	}
}
