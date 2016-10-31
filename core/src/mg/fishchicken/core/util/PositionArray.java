package mg.fishchicken.core.util;

import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;

public class PositionArray {

	private IntArray array;
	private IntMap<Integer> hashSet;
	
	public PositionArray() {
		array = new IntArray();
		hashSet = new IntMap<Integer>();
	}
	
	public PositionArray(PositionArray arrayToCopy) {
		array = new IntArray(arrayToCopy.array);
		hashSet = new IntMap<Integer>(arrayToCopy.hashSet);
	}
	
	
	public PositionArray(int initialSize) {
		array = new IntArray(initialSize*2);
		hashSet = new IntMap<Integer>(initialSize);
	}
 	
	public int size() {
		return array.size / 2;
	}

	public boolean isEmpty() {
		return array.size == 0;
	}
	
	public boolean contains(Tile position) {
		return contains(position.getX(), position.getY());
	}

	public boolean contains(int x, int y) {
		return indexOf(x, y) != -1;
	}

	public int[] toArray() {
		return array.toArray();
	}

	public int getX(int i) {
		return array.get(i*2);
	}
	
	public int getY(int i) {
		return array.get(i*2+1);
	}
	
	public void add(int x, int y) {
		array.add(x);
		array.add(y);
		int hash = x*y;
		Integer existing = hashSet.get(hash);
		if (existing == null) {
			existing = 1;
		} else {
			++existing;
		}
		hashSet.put(hash, existing);
	}

	public void add(Tile pos) {
		add(pos.getX(), pos.getY());
	}
	
	public void removeIndex(int i) {
		int x = array.removeIndex(i*2);
		int y = array.removeIndex(i*2);
		int hash = x*y;
		Integer existing = hashSet.get(hash);
		if (existing == 1) {
			hashSet.remove(hash);
		} else {
			hashSet.put(hash, existing-1);
		}
	}
	
	public void removeValue(int x, int y) {
		int index = indexOf(x, y);
		if (index > -1) {
			removeIndex(index);
		}
	}
	
	public int indexOf(int x, int y) {
		if (!hashSet.containsKey(x*y)) {
			return -1;
		}
		for (int i = 0; i < array.size; i += 2) {
			if (x == array.get(i) && y == array.get(i+1)) {
				return i/2;
			}
		}
		return -1;
	}

	public void addAll(PositionArray array) {
		for (int i = 0; i < array.size(); ++i) {
			this.add(array.getX(i), array.getY(i));
		}
	}
	
	public void addAllNew(PositionArray array) {
		for (int i = 0; i < array.size(); ++i) {
			int x = array.getX(i);
			int y = array.getY(i);
			if (!contains(x, y)) {
				this.add(x, y);
			}
		}
	}
	
	public void clear() {
		array.clear();
		hashSet.clear();
	}	
}
