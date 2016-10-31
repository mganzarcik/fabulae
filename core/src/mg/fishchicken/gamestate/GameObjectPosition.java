package mg.fishchicken.gamestate;

import java.io.IOException;

import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.utils.XmlReader.Element;


public class GameObjectPosition extends Position {
	
	private final Tile tile;
	private final Tile prevTile;
	private boolean changedPosition, changedTile;
	private float prevX, prevY;
	
	public GameObjectPosition() {
		super();
		tile = new MyTile();
		prevTile = new Tile(-1, -1);
		prevX = prevY = -1;
	}
	
	/**
	 * Returns the tile this position represents.
	 * @return
	 */
	public Tile tile() {
		return tile;
	}
	
	/**
	 * Returns the tile this position represented
	 * before changing to the current tile.
	 * @return
	 */
	public Tile prevTile() {
		return prevTile;
	}
	
	@Override
	public void set(float x, float y) {
		float currX = getX();
		float currY = getY();
		if (prevX < 0 && prevY < 0) {
			prevX = currX;
			prevY = currY;
		}
		
		if (!changedPosition && (currX != x || currY != y)) {
			changedPosition = true;
			prevX = currX;
			prevY = currY;
		}
		super.set(x, y);
		
		if (changedPosition && !changedTile && (Math.floor(currX) != Math.floor(x) || Math.floor(currY) !=  Math.floor(y))) {
			changedTile = true; 
			prevTile.set((int)prevX, (int)prevY);
		}
	}
	
	/**
	 * Gets the x coordinate before the last 
	 * time this position has changed.
	 * @return
	 */
	public float getPrevX() {
		return prevX;
	}
	
	/**
	 * Gets the y coordinate before the last 
	 * time this position has changed.
	 * 
	 * @return
	 */
	public float getPrevY() {
		return prevY;
	}
	
	/**
	 * Returns true if this position has changed 
	 * since the last call to {@link #resetChanged()}
	 * @return
	 */
	public boolean hasChanged() {
		return changedPosition;
	}
	
	/**
	 * Resets the change status of this position
	 * to not changed.
	 */
	public void resetChanged() {
		changedPosition = false;
	}
	
	/**
	 * Returns true if this position is perfectly equal to the corresponding tile.
	 * 
	 * For example, if the position is 1.5, 1.7, this will return false, but if it is 1, 1, it will return true.
	 * 
	 * This uses tolerance to get around floating point equivalence errors. See {@link MathUtil#FLOAT_EQUALITY_TOLERANCE}
	 * @return
	 */
	public boolean isExactlyOnTile() {
		Tile tile = tile();
		return dst(tile.getX(), tile.getY()) < MathUtil.FLOAT_EQUALITY_TOLERANCE;
	}
	
	/**
	 * Returns true if this position's tile has changed 
	 * since the last call to {@link #resetChangedTile()}
	 * @return
	 */
	public boolean hasChangedTile() {
		return changedTile;
	}
	
	/**
	 * Resets the change status of this position's tile
	 * to not changed.
	 */
	public void resetChangedTile() {
		changedTile = false;
	}
	
	/**
	 * Marks this position as changed. Also resets the previous tile.
	 */
	public void markAsChanged() {
		changedPosition = true;
		changedTile = true;
		prevTile.set(-1, -1);
		prevX = -1;
		prevY = -1;
	}
	
	@Override
	public String getXMLElementName() {
		return StringUtil.lowercaseFirstLetter(Position.class.getSimpleName());
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		markAsChanged();
	}
	
	private class MyTile extends Tile {

		public MyTile() {
			super();
		}
		
		@Override
		public void set(int x, int y) {
			GameObjectPosition.this.set(x, y);
		}
		
		@Override
		public int getX() {
			return (int)GameObjectPosition.this.getX();
		}
		@Override
		public int getY() {
			return (int)GameObjectPosition.this.getY();
		}
		
	}

}
