package mg.fishchicken.core.assets;

import java.util.Iterator;

import mg.fishchicken.core.util.Pair;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectSet;

public class AssetMap implements Iterable<Entry<String, Class<?>>>{

	private Array<Pair<String, Class<?>>> assets;
	private ObjectSet<String> fileNames;
	
	public AssetMap() {
		assets = new Array<Pair<String,Class<?>>>();
		fileNames = new ObjectSet<String>();
	}
	
	public boolean containsFile(String fileName) {
		return fileNames.contains(fileName);
	}
	
	public void putAll(AssetMap otherMap) {
		assets.addAll(otherMap.assets);
		fileNames.addAll(otherMap.fileNames);
	}
	
	public void clear() {
		assets.clear();
		fileNames.clear();
	}
	
	public void put(String fileName, Class<?> assetClass) {
		assets.add(new Pair<String, Class<?>>(fileName, assetClass));
		fileNames.add(fileName);
	}

	@Override
	public Iterator<Entry<String, Class<?>>> iterator() {
		return new Iterator<ObjectMap.Entry<String,Class<?>>>() {

			private Entry<String, Class<?>> entry = new Entry<String, Class<?>>();
			private int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < assets.size;
			}

			@Override
			public Entry<String, Class<?>> next() {
				Pair<String, Class<?>> value = assets.get(index++);
				entry.key = value.getLeft();
				entry.value = value.getRight();
				return entry;
			}
		};
	}
	
}
