package mg.fishchicken.core.configuration;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

public enum KeyBindings {
	
	MULTI_SELECT,
	SELECT_ALL,
	MOVE_ONLY, 
	HIGHLIGHT_USABLES, 
	QUICK_SAVE, 
	QUICK_LOAD, 
	PAUSE, 
	END_TURN,
	CONFIRM,
	CANCEL, 
	MOVE_TO_JUNK,
	MOVE_TO_INVENTORY,
	TOGGLE_GRID, 
	DISPLAY_INVENTORY, 
	DISPLAY_PERKS, 
	DISPLAY_ACTIVE_EFFECTS, 
	DISPLAY_USE_PERK, 
	DISPLAY_JOURNAL,
	DISPLAY_CHARACTER_EDIT, 
	DISPLAY_SPELLBOOK, 
	DISPLAY_FORMATION_EDITOR,
	REST,
	DISPLAY_MAP,
	SNEAK,
	DISARM,
	DETECT,
	LOCKPICK,
	ATTACK,
	TALKTO;
	
	public static final String STRING_TABLE = "keyBindigs."+Strings.RESOURCE_FILE_EXTENSION;
	
	private static final ObjectMap<KeyBindings, Array<Integer>> keys = new ObjectMap<KeyBindings, Array<Integer>>();
	
	static {
		keys.put(MULTI_SELECT, new Array<Integer>(
				new Integer[] {Input.Keys.CONTROL_RIGHT, Input.Keys.CONTROL_LEFT }));
		keys.put(SELECT_ALL, new Array<Integer>(
				new Integer[] { Input.Keys.EQUALS }));
		keys.put(MOVE_TO_JUNK, new Array<Integer>(
				new Integer[] {Input.Keys.CONTROL_RIGHT, Input.Keys.CONTROL_LEFT }));
		keys.put(MOVE_TO_INVENTORY, new Array<Integer>(
				new Integer[] {Input.Keys.SHIFT_RIGHT, Input.Keys.SHIFT_LEFT }));
		keys.put(HIGHLIGHT_USABLES, new Array<Integer>(
				new Integer[] { Input.Keys.TAB }));
		keys.put(MOVE_ONLY, new Array<Integer>(
				new Integer[] { Input.Keys.SHIFT_LEFT }));
		keys.put(QUICK_SAVE,
				new Array<Integer>(new Integer[] { Input.Keys.F5 }));
		keys.put(QUICK_LOAD,
				new Array<Integer>(new Integer[] { Input.Keys.F9 }));
		keys.put(PAUSE, 
				new Array<Integer>(new Integer[] { Input.Keys.SPACE }));
		keys.put(END_TURN, new Array<Integer>(
				new Integer[] { Input.Keys.ENTER }));
		keys.put(CONFIRM,
				new Array<Integer>(new Integer[] { Input.Keys.ENTER }));
		keys.put(CANCEL,
				new Array<Integer>(new Integer[] { Input.Keys.ESCAPE }));
		keys.put(TOGGLE_GRID,
				new Array<Integer>(new Integer[] { Input.Keys.G }));
		keys.put(DISPLAY_INVENTORY, new Array<Integer>(
				new Integer[] { Input.Keys.I }));
		keys.put(DISPLAY_PERKS, new Array<Integer>(
				new Integer[] { Input.Keys.C }));
		keys.put(DISPLAY_ACTIVE_EFFECTS, new Array<Integer>(
				new Integer[] { Input.Keys.E }));
		keys.put(DISPLAY_USE_PERK, new Array<Integer>(
				new Integer[] { Input.Keys.P }));
		keys.put(DISPLAY_JOURNAL, new Array<Integer>(
				new Integer[] { Input.Keys.J }));
		keys.put(DISPLAY_CHARACTER_EDIT, new Array<Integer>(
				new Integer[] { Input.Keys.V }));
		keys.put(DISPLAY_SPELLBOOK, new Array<Integer>(
				new Integer[] { Input.Keys.S }));
		keys.put(REST, new Array<Integer>(
				new Integer[] { Input.Keys.R }));
		keys.put(DISPLAY_FORMATION_EDITOR, new Array<Integer>(
				new Integer[] { Input.Keys.F }));
		keys.put(DISPLAY_MAP, new Array<Integer>(
				new Integer[] { Input.Keys.M }));
		keys.put(SNEAK, new Array<Integer>(
				new Integer[] { Input.Keys.H }));
		keys.put(DISARM, new Array<Integer>(
				new Integer[] { Input.Keys.D }));
		keys.put(DETECT, new Array<Integer>(
				new Integer[] { Input.Keys.T }));
		keys.put(LOCKPICK, new Array<Integer>(
				new Integer[] { Input.Keys.L }));
		keys.put(ATTACK, new Array<Integer>(
				new Integer[] { Input.Keys.A }));
		keys.put(TALKTO, new Array<Integer>(
				new Integer[] { Input.Keys.Q }));
	}
	
	public Array<Integer> getKeys() {
		return keys.get(this);
	}
	
	public boolean is(int keyCode) {
		return getKeys().contains(keyCode, false);
	}
	
	/**
	 * Returns true if any key for this binding is currently pressed.
	 * @return
	 */
	public boolean isPressed() {
		for (int key : keys.get(this)) {
			if (Gdx.input.isKeyPressed(key)) {
				return true;
			}
		}
		return false;
	}
	
	public String getUIName() {
		return Strings.getString(STRING_TABLE, this.name());
	}
	
	public String keysToString() {
		Array<Integer> keys = getKeys();
		StringBuilder fsb = StringUtil.getFSB();
		int i = 0;
		for (Integer key : keys) {
			fsb.append(Input.Keys.toString(key));
			if (++i < keys.size) {
				fsb.append(", ");
			}
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue;
	}
	
	/**
	 * Returns the number that the supplied keycode represents.
	 * Will return 0-9 for the 0-9 keys (both normal and on the numpad)
	 * and -1 for any other key.
	 * @param keycode
	 * @return
	 */
	public static int getNumberPressed(int keycode) {
		switch (keycode) {
			case Keys.NUM_0:
			case Keys.NUMPAD_0: return 0;
			case Keys.NUM_1:
			case Keys.NUMPAD_1: return 1;
			case Keys.NUM_2:
			case Keys.NUMPAD_2: return 2;
			case Keys.NUM_3:
			case Keys.NUMPAD_3: return 3;
			case Keys.NUM_4:
			case Keys.NUMPAD_4: return 4;
			case Keys.NUM_5:
			case Keys.NUMPAD_5: return 5;
			case Keys.NUM_6:
			case Keys.NUMPAD_6: return 6;
			case Keys.NUM_7:
			case Keys.NUMPAD_7: return 7;
			case Keys.NUM_8:
			case Keys.NUMPAD_8: return 8;
			case Keys.NUM_9:
			case Keys.NUMPAD_9: return 9;
			default: return -1;
		}
	}
}
