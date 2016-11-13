package mg.fishchicken.core.util;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.gamestate.Position;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public enum Orientation {

	UP {
		@Override
		public Orientation getClockwise() {
			return UPRIGHT;
		}

		@Override
		public Orientation getAntiClockwise() {
			return UPLEFT;
		}

		@Override
		public int getDegrees() {
			return 0;
		}
	}, UPRIGHT{
		@Override
		public Orientation getClockwise() {
			return RIGHT;
		}

		@Override
		public Orientation getAntiClockwise() {
			return UP;
		}

		@Override
		public int getDegrees() {
			return 45;
		}
	}, RIGHT{
		@Override
		public Orientation getClockwise() {
			return DOWNRIGHT;
		}

		@Override
		public Orientation getAntiClockwise() {
			return UPRIGHT;
		}

		@Override
		public int getDegrees() {
			return 90;
		}
	}, DOWNRIGHT{
		@Override
		public Orientation getClockwise() {
			return DOWN;
		}

		@Override
		public Orientation getAntiClockwise() {
			return RIGHT;
		}
		@Override
		public int getDegrees() {
			return 135;
		}
	}, DOWN{
		@Override
		public Orientation getClockwise() {
			return DOWNLEFT;
		}

		@Override
		public Orientation getAntiClockwise() {
			return DOWNRIGHT;
		}
		
		@Override
		public int getDegrees() {
			return 180;
		}
	}, DOWNLEFT{
		@Override
		public Orientation getClockwise() {
			return LEFT;
		}

		@Override
		public Orientation getAntiClockwise() {
			return DOWN;
		}
		
		@Override
		public int getDegrees() {
			return 225;
		}
	}, LEFT{
		@Override
		public Orientation getClockwise() {
			return UPLEFT;
		}

		@Override
		public Orientation getAntiClockwise() {
			return DOWNLEFT;
		}
		
		@Override
		public int getDegrees() {
			return 270;
		}
	}, UPLEFT{
		@Override
		public Orientation getClockwise() {
			return UP;
		}

		@Override
		public Orientation getAntiClockwise() {
			return LEFT;
		}
		
		@Override
		public int getDegrees() {
			return 315;
		}
	};
	
	public Orientation getOpposite() {
		return getClockwise().getClockwise().getClockwise().getClockwise();
	}
	
	/**
	 * Gets the Orientation thats next to this one clockwise.
	 * @return
	 */
	public abstract Orientation getClockwise();
	
	/**
	 * Gets the Orientation thats next to this one anti clockwise.
	 * @return
	 */
	public abstract Orientation getAntiClockwise();
	
	public abstract int getDegrees();
	
	/**
	 * Returns true if at least one direction of the supplied Orientation matches
	 * with ours. Or in other words, if the Orientations are no more than 45 degrees apart.
	 * 
	 * For example, this would return true for UpLeft and DownLeft, or for
	 * UpRight and Up, but not for Down and UpLeft, or UpRight and DownLeft.
	 * 
	 * @param o
	 * @return
	 */
	public boolean atLeastOneDirectionMatches(Orientation o) {
		if (o == null) {
			return false;
		}
		
		return (this.equals(o) || getAntiClockwise() == o || getClockwise() == o);
	}
	
	/**
	 * Returns true if at least one direction of the supplied Orientation matches
	 * with ours. Or in other words, if the Orientations are no more than 45 degrees apart.
	 * 
	 * For example, this would return true for UpLeft and DownLeft, or for
	 * UpRight and Up, but not for Down and UpLeft, or UpRight and DownLeft.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean atLeastOneDirectionMatches(Orientation o1, Orientation o2) {
		if (o1 == null) {
			return false;
		}
		return o1.atLeastOneDirectionMatches(o2);
	}
	
	/**
	 * Sets the supplied values according to this orientation
	 * 
	 * This sets the offset to the relative coordinates of tile that the 
	 * OrientationUtil is "looking at", so DownLeft would be -1, -1 or orthogonal
	 * and 0, -1 for isometric.
	 * @param o
	 * @param offset
	 */
	public void setOffsetByOrientation( Vector2 offset,
			boolean isometric) {
		offset.set(0, 0);
		switch (isometric ? this : getClockwise()) {
		case DOWNRIGHT:
			offset.x = 1;
			break;
		case DOWN:
			offset.x = -1;
			offset.y = -1;
			break;
		case DOWNLEFT:
			offset.y = -1;
			break;
		case LEFT:
			offset.x = -1;
			offset.y = -1;
			break;
		case UPLEFT:
			offset.x = -1;
			break;
		case UP:
			offset.x = -1;
			offset.y = 1;
			break;
		case UPRIGHT:
			offset.y = 1;
			break;
		case RIGHT:
			offset.x = 1;
			offset.y = 1;
			break;
		}
	}
	
	/**
	 * Calculates the OrientationUtil the mover needs to have
	 * in order to move xDif, yDif points on the map.
	 *  
	 * @param isometric - if true, the orientation will be calculated in the orthogonal system, otherwise isometric will be used
	 * @param xDif number of points we need to move in x direction
	 * @param yDif number of points we need to move in y direction
	 * @return
	 */
	public static Orientation calculateOrientationToTarget(boolean isometric, float xDif, float yDif) {
		
		double angle = Math.round(MathUtils.radiansToDegrees * Math.atan(Math.abs(xDif / yDif)));
		
		if (xDif >= 0 && yDif < 0) {
			angle = 180 - angle;
		} else if (xDif < 0 && yDif < 0) {
			angle = 180 + angle;
		} else if (xDif < 0 && yDif >= 0) {
			angle = 360 - angle;
		}
		
		float half = 22.5f;
		
		Orientation returnValue = Orientation.UP;
	
		for (float i = half; i <  337.5 && angle > i && angle < 337.5; i += half*2) {
			returnValue = returnValue.getClockwise();
		}
		
		if (isometric) {
			returnValue = returnValue.getClockwise();
		}
		
		return returnValue;
	}
	
	/**
	 * Calculates the facing OrientationUtil if looking from point [fromX, fromY]
	 * at point [toX, toY] on the map.
	 *  
	 * @param isometric - if true, the orientation will be calculated in the orthogonal system, otherwise isometric system will be used
	 * @param map
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @return
	 */
	public static Orientation calculateOrientationToTarget(boolean isometric, float fromX, float fromY, float toX, float toY) {
		return calculateOrientationToTarget(isometric, toX - fromX, toY - fromY);
	}
	
	public static Orientation calculateOrientationToTarget(boolean isometric, Position from, Position to) {
		return calculateOrientationToTarget(isometric, from.getX(), from.getY(), to.getX(), to.getY());
	}
	
	/**
	 * Calculates the OrientationUtil the origin GO needs to have in order to face the target GO.
	 *  
	 * @param origin
	 * @param target
	 * @return
	 */
	public static Orientation calculateOrientationToTarget(GameObject origin, PositionedThing target) {
		return calculateOrientationToTarget(origin.getMap().isIsometric(), origin.position(), target.position());
	}
	
	public Vector2 setNextTileInDirection(int sx, int sy, Vector2 nextTile) {
		switch(this) {
			case LEFT: nextTile.set(sx-1, sy); break;
			case DOWN: nextTile.set(sx, sy-1); break;
			case DOWNLEFT: nextTile.set(sx-1, sy-1); break;
			case DOWNRIGHT: nextTile.set(sx+1, sy-1); break;
			case RIGHT: nextTile.set(sx+1, sy); break;
			case UP: nextTile.set(sx, sy+1); break;
			case UPLEFT: nextTile.set(sx-1, sy+1); break;
			case UPRIGHT:nextTile.set(sx+1, sy+1); break;
		}
		return nextTile;
	}
	
	public static Orientation getCombinedOrientation(Orientation x, Orientation y) {
		if (y == null) {
			return x;
		}
		
		if (x == null) {
			return y;
		}
		
		return valueOf(y.name()+x.name());
	}
}
