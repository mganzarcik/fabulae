package mg.fishchicken.core.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;


public class CoreUtil {

	public static <K, V> void addToArrayMap(ObjectMap<K, Array<V>> map, K key, V value) {
		Array<V> array = map.get(key);
		if (array == null) {
			array = new Array<V>();
			map.put(key, array);
		}
		array.add(value);
	}
	
	public static <K, V> void removeFromArrayMap(ObjectMap<K, Array<V>> map, K key, V value) {
		Array<V> array = map.get(key);
		if (array == null) {
			return;
		}
		array.removeValue(value, false);
	}
	
	/**
	 * Null-safe equals.
	 */
	public static boolean equals(Object a, Object b) {
		if (a == null && b == null) {
			return true;
		}
		
		if (a != null) {
			return a.equals(b);
		}
		
		return false;
	}
	
}
