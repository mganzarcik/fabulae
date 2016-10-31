package mg.fishchicken.core.util;

import com.badlogic.gdx.Input.Keys;

public class InputUtil {

	private InputUtil() {
		
	}
	
	
	public static boolean isArrowKey(int keycode) {
		return Keys.LEFT == keycode || Keys.RIGHT == keycode || Keys.UP == keycode
				|| Keys.DOWN == keycode;
	}
}
