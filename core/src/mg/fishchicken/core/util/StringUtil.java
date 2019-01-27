package mg.fishchicken.core.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;

public class StringUtil {

	private static final String SPECIAL_CHARACTERNAME = "characterName";
	private static final Pattern CHARACTER_NAME_PATTERN = Pattern.compile("\\<"+SPECIAL_CHARACTERNAME+"\\((\\w+)\\)\\>");
	
	public static final Pool<StringBuilder> stringBufferPool = new Pool<StringBuilder>() {
		@Override
		protected StringBuilder newObject() {
			return new StringBuilder();
		}
	};

	private StringUtil() {
		
	}
	
	public static final Array<String> arrayFromDelimitedString(String string, String delimiter) {
		String[] splits = string.split(delimiter);
		Array<String> returnValue = new Array<String>();
		for (String split : splits) {
			returnValue.add(split.trim());
		}
		return returnValue;
	}
	
	public static String replaceParameters(String string, ObjectMap<String, String> parameters) {
		for (Entry<String, String> entry : parameters.entries()) {
			string = replaceWithCapitalisation(string, "\\<"+entry.key+"\\>", entry.value);
		}
		return string;
	}
	
	public static boolean nullOrEmptyString(String s) {
		return s == null || s.isEmpty();
	}
	
	public static String splitCamelCase(String s) {
		return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])","(?<=[^A-Z])(?=[A-Z])","(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
	}
	
	public static String capitalizeFirstLetter(String text) {
		return text.substring(0,1).toUpperCase(Locale.ENGLISH)+text.substring(1);
	}
	
	public static String lowercaseFirstLetter(String text) {
		return text.substring(0,1).toLowerCase(Locale.ENGLISH)+text.substring(1);
	}
	
	public static String clearString(String stringToClear) {
		return stringToClear.replace("\n", " ").replace("\t", "");
	}

	public static StringBuilder getFSB() {
		return stringBufferPool.obtain();
	}

	public static void freeFSB(StringBuilder fsb) {
		fsb.setLength(0);
		stringBufferPool.free(fsb);
	}
	
	/**
	 * Replaces all occurrences of <characterName(CHARACTER_ID)> with the actual
	 * names of characters with those IDs in the String.
	 * 
	 * The character must have already been loaded prior to this. 
	 * @param string
	 * @return
	 */
	public static String replaceCharacterNames(String string) {
		Matcher matcher = CHARACTER_NAME_PATTERN.matcher(string);
		String returnValue = string;
		while(matcher.find()) {
			GameObject go = GameState.getGameObjectById(matcher.group(1));
			returnValue = returnValue.replace("<"+SPECIAL_CHARACTERNAME+"("+matcher.group(1)+")>", go != null ? go.getName() : "!CHARACTER "+matcher.group(1)+"NOT FOUND!");
        }
		return returnValue;
	}
	
	private static String replaceWithCapitalisation(String textToProcess, String toReplace, String replacement) {
		textToProcess = textToProcess.replaceAll("\\.[ ]*"+toReplace, ". "+capitalizeFirstLetter(replacement));
		textToProcess = textToProcess.replaceAll("\\n[ ]*"+toReplace, "\n"+capitalizeFirstLetter(replacement));
		textToProcess = textToProcess.replaceAll(toReplace, replacement);
		return textToProcess;
	}
}
