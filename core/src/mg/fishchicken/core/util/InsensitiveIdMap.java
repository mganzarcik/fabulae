package mg.fishchicken.core.util;

import java.util.Locale;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * Simple map between IDs and objects where
 * all the operations are case insensitive about the IDs.
 * 
 * Because of specific issues with case conversions,
 * the IDs should be just simple alphanumeric strings
 * without special characters.
 * 
 * @author ANNUN
 *
 * @param <T>
 * 
 * @see ObjectMap
 */
public class InsensitiveIdMap<T> extends ObjectMap<String, T>{
	@Override
	public boolean containsKey(String key) {
		return super.containsKey(key.toLowerCase(Locale.ENGLISH));
	}
	
	@Override
	public T put(String key, T value) {
		return super.put(key.toLowerCase(Locale.ENGLISH), value);
	}
	
	@Override
	public T get(String key) {
		return super.get(key.toLowerCase(Locale.ENGLISH));
	}
	
	@Override
	public T get(String key, T defaultValue) {
		return super.get(key.toLowerCase(Locale.ENGLISH), defaultValue);
	}
	
	@Override
	public T remove(String key) {
		return super.remove(key.toLowerCase(Locale.ENGLISH));
	}
	
}
