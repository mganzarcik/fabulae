package mg.fishchicken.gamestate;

import mg.fishchicken.core.util.MathUtil;

import com.badlogic.gdx.math.Vector2;


public class Position extends ObservableState<Position, Position.PositionChange> {
	private float s_x = 0, s_y = 0;
	private PositionChange changes = new PositionChange();
	
	public float getX() {
		return s_x;
	}

	public void setX(float x) {
		set(x, s_y);
	}

	public float getY() {
		return s_y;
	}

	public void setY(float y) {
		set(s_x, y);
	}
	
	public void set(float x, float y) {
		changes.oldX = s_x;
		changes.oldY = s_y;
		s_x = x;
		s_y = y;
		changed(changes);
	}
	
	public void set(Vector2 position) {
		if (position != null) {
			set(position.x, position.y);
		}
	}
	
	public void set(Tile tile) {
		if (tile != null) {
			set(tile.getX(), tile.getY());
		}
	}
	
	public void set(Position position) {
		if (position != null) {
			set(position.getX(), position.getY());
		}
	}
	
	/**
	 * Sets the supplied vector to the same values as this Position 
	 * and returns it.
	 * @param vectorToSet
	 * @return
	 */
	public Vector2 setVector2(Vector2 vectorToSet) {
		vectorToSet.x = getX();
		vectorToSet.y = getY();
		return vectorToSet;
	}
	
	/**
	 * The distance between this and the other vector. 
	 * @param vector
	 * @return
	 */
	public float dst(Vector2 vector) {
		return vector.dst(getX(), getY());
	}
	
	/**
	 * The distance between this and the other position. 
	 * @param vector
	 * @return
	 */
	public float dst(Position position) {
		return Vector2.dst(getX(), getY(), position.getX(), position.getY());
	}
	
	/**
	 * The distance between this and the other position. 
	 * @param vector
	 * @return
	 */
	public float dst(float x, float y) {
		return Vector2.dst(getX(), getY(), x, y);
	}
	
	/**
	 * Returns true if coordinages are exactly the same
	 * as the supplied ones.
	 * 
	 * WARNING: this ignores floating point rounding errors,
	 * so it it should only be used in situations
	 * where the caller is certain that at some point, 
	 * the positions coordinates will actually be exactly the same
	 * as the supplied values.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean equals(int x, int y) {
		return getX() == x && getY() == y;
	}
	
	/**
	 * Returns true if coordinates are the same
	 * as the supplied ones. Uses {@link MathUtil#FLOAT_EQUALITY_TOLERANCE} 
	 * to get get around floating point equivalence issues.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean floatEquals(float x, float y) {
		return dst(x, y) < MathUtil.FLOAT_EQUALITY_TOLERANCE;
	}
	
	@Override
	public int hashCode() {
		return (int)(s_x*s_y);
	}
	
	public static class PositionChange{
		private float oldX, oldY;
		
		public float getOldX() {
			return oldX;
		}
		
		public float getOldY() {
			return oldY;
		}
	}
}
