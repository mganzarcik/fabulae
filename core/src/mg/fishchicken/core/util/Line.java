package mg.fishchicken.core.util;

import com.badlogic.gdx.math.Vector2;


public class Line {

	public float startX, startY, endX, endY;
	
	public Line(float startX, float startY, float endX, float endY) {
		setPoints(startX, startY, endX, endY);
	}
	
	public void setPoints(float startX, float startY, float endX, float endY) {
		float dstStart = Vector2.Zero.dst(startX, startY);
		float dstEnd = Vector2.Zero.dst(endX, endY);
		if (dstStart > dstEnd) {
			this.startX = endX;
			this.startY = endY;
			this.endX = startX;
			this.endY = startY;
		} else {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
		}
	}

	/**
	 * Returns true if this line can combine with the supplied
	 * line to form a longer line.
	 * 
	 * This means they must share at least one point
	 * and be parallel.
	 * 
	 * @param line
	 * @return
	 */
	public boolean canCombine(Line line) {
		if (!shareAPoint(line)) {
			return false;
		}
		Vector2 myVector = MathUtil.getVector2().set(endX - startX, endY - startY).nor();
		Vector2 itsVector = MathUtil.getVector2().set(line.endX - line.startX, line.endY - line.startY).nor();
		boolean returnValue = 	MathUtil.equals(myVector.x, itsVector.x) &&  
							  	MathUtil.equals(myVector.y, itsVector.y);
		MathUtil.freeVector2(myVector);
		MathUtil.freeVector2(itsVector);
		return returnValue;
	}
	
	/**
	 * Whether this line shares at least one end point
	 * with the supplied line.
	 * @param line
	 * @return
	 */
	private boolean shareAPoint(Line line) {	
		if (MathUtil.equals(startX, line.startX) && MathUtil.equals(startY, line.startY)) {
			return true;
		} else if (MathUtil.equals(startX, line.endX) && MathUtil.equals(startY, line.endY)) {
			return true;
		} else if (MathUtil.equals(endX, line.startX) && MathUtil.equals(endY, line.startY)) {
			return true;
		} else if (MathUtil.equals(endX, line.endX) && MathUtil.equals(endY, line.endY)) {
			return true;
		}
		return false;
	} 
	
	/**
	 * Combines this line with the supplied line to 
	 * form a longer line. This only works if the
	 * lines share a point.
	 *
	 * @param line
	 */
	public void combine(Line line) {
		if (MathUtil.equals(startX, line.startX) && MathUtil.equals(startY, line.startY)) {
			if (Vector2.Zero.dst(endX, endY) < Vector2.Zero.dst(line.endX, line.endY)) {
				setPoints(startX, startY, line.endX, line.endY); 
			}
		} else if (MathUtil.equals(startX, line.endX) && MathUtil.equals(startY, line.endY)) {
			setPoints(endX, endY, line.startX, line.startY);
		} else if (MathUtil.equals(endX, line.startX) && MathUtil.equals(endY, line.startY)) {
			setPoints(startX, startY, line.endX, line.endY);
		} else if (MathUtil.equals(endX, line.endX) && MathUtil.equals(endY, line.endY)) {
			if (Vector2.Zero.dst(startX, startY) > Vector2.Zero.dst(line.startX, line.startY)) {
				setPoints(line.startX, line.startY, endX, endY);
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Line) {
			Line second = (Line) obj;
			return 	(MathUtil.equals(startX, second.startX) && 
					MathUtil.equals(startY, second.startY) &&
					MathUtil.equals(endX, second.endX) &&
					MathUtil.equals(endY, second.endY));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (Float.toString(MathUtil.roundTo(startX, 4))+MathUtil.roundTo(startY, 4)+MathUtil.roundTo(endX, 4)+MathUtil.roundTo(endY, 4)).hashCode();
	}
	
}
