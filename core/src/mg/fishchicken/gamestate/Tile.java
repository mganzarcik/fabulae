package mg.fishchicken.gamestate;

public class Tile {
	
	private int x, y;
	
	public Tile() {
	}
	
	public Tile(Tile tile) {
		this(tile.x, tile.y);
	}
	
	public Tile(int x, int y) {
		set(x, y);
	};
	
	public void setX(int x) {
		set(x, y);
	}
	
	public void setY(int y) {
		set(x, y);
	}
	
	public void set(Tile tile) {
		set(tile.getX(), tile.getY());
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	/**
	 * Rotate this tile 90 degrees clockwise around 0,0 origin.
	 */
	public void rotate90CW() {
		int x = getX();
		setX(y);
		setY(-x);
	}
	
	@Override
	public int hashCode() {
		return getX()*getY();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			Tile other = (Tile) obj;
			return equals(other.getX(), other.getY());
		}
		return false;
	}
	
	public boolean equals(int x, int y) {
		return getX() == x && getY() == y;
	}
	
}