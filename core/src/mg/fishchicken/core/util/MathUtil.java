package mg.fishchicken.core.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.SaveablePolygon;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class MathUtil {

	public static final double SQRT_TWO = Math.sqrt(2);
	public static final float FLOAT_EQUALITY_TOLERANCE = 0.0001f;
	private static Pool<Vector2> vector2Pool = Pools.get(Vector2.class);
	private static Pool<Vector3> vector3Pool = Pools.get(Vector3.class);
	private static DecimalFormat decimalFormat;
	
	/**
	 * Returns a string representation of the supplied number,
	 * rounded to the configured number of decimals.
	 * 
	 * @see Configuration#getDecimalFormat()
	 */
	public static String toUIString(float number) {
		if (decimalFormat == null) {
			decimalFormat = new DecimalFormat(Configuration.getDecimalFormat(),  new DecimalFormatSymbols(Locale.ENGLISH));
		}
		return decimalFormat.format(number);
	}
	
	/**
	 * Gets a pooled Vector2 instance. The instance must
	 * be released by calling {@link MathUtil#freeVector2(Vector2)} 
	 * once it is no longer used.
	 *  
	 */
	public static Vector2 getVector2() {
		return vector2Pool.obtain();
	}
	
	/**
	 * Checks whether two floats are almost equal.
	 * 
	 * @param f1
	 * @param f2
	 * @return
	 */
	public static boolean equals(float f1, float f2) {
		return (Math.abs(f1 - f2) < FLOAT_EQUALITY_TOLERANCE);
	}
	
	/**
	 * Frees a previously pooled Vector2 instance.
	 */
	public static void freeVector2(Vector2 toFree) {
		vector2Pool.free(toFree);
	}
	
	/**
	 * Gets a pooled Vector3 instance. The instance must
	 * be released by calling {@link MathUtil#freeVector3(Vector3)} 
	 * once it is no longer used.
	 *  
	 */
	public static Vector3 getVector3() {
		return vector3Pool.obtain();
	}
	
	/**
	 * Frees a previously pooled Vector3 instance.
	 */
	public static void freeVector3(Vector3 toFree) {
		vector3Pool.free(toFree);
	}
	
	/**
	 * Fills the supplied area.
	 * 
	 * This expects an PositionArray that contains coordinates of 
	 * all tiles (pixels, whatever) that form the boundary of the area 
	 * to fill and the coordinates of a point that must be within the area.
	 * 
	 * The method then adds into the supplied array all the other points that
	 * are within it.
	 * 
	 * @param startX
	 * @param startY
	 * @param area
	 */
	public static void fillArea(int startX, int startY, PositionArray area) {
		PositionArray queue = new PositionArray();
		queue.add(startX, startY);
		while (!queue.isEmpty()) {
			int lastIndex = queue.size()-1;
			int x = queue.getX(lastIndex);
			int y = queue.getY(lastIndex);
			queue.removeIndex(lastIndex);
			if (area.contains(x, y)) {
				continue;
			}
			int w = x-1;
			int e = x+1;
			while (!area.contains(w, y)) {
				--w;
			}
			++w;
			while (!area.contains(e, y)) {
				++e;
			}
			--e;
			for (int i = w; i <= e; ++i) {
				area.add(i, y);
				if (!area.contains(i, y+1)) {
					queue.add(i, y+1);
				}
				if (!area.contains(i, y-1)) {
					queue.add(i, y-1);
				}
			}
		}
	}
	
	/**
	 * If the rectangle has width or height negative, it
	 * recalculates it to have only positive values without 
	 * changing its size or location.
	 * 
	 * @param rect
	 */
	public static void normalizeRectangle(Rectangle rect) {
		if (rect.width < 0) {
			rect.setX(rect.x + rect.width);
			rect.setWidth(-1*rect.width);
		}
		
		if (rect.height < 0) {
			rect.setY(rect.y + rect.height);
			rect.setHeight(-1*rect.height);
		}
	}
	
	/**
	 * Makes sure the supplied angle degrees are between 0 and 360.
	 * @param degrees
	 * @return
	 */
	public static float normalizeDegrees(final float degrees) {
		float returnValue = degrees % 360;
		if (returnValue < 0) {
			returnValue += 360;
		}
		
		return returnValue;
	}
	
	public static float roundTo(float value, int decimals) {
		for (int i = 0; i < decimals; ++i) {
			value *= 10;
		}
		value = Math.round(value);
		for (int i = 0; i < decimals; ++i) {
			value /= (float)10;
		}
		return value;
	}
	
	/**
	 * Makes sure the supplied value is not less than the supplied minimum
	 * and not more than the supplied maximum.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static int boxValue(int value, int min, int max) {
		return (int) boxValue((float)value, min, max);
	}
	
	/**
	 * Makes sure the supplied value is not less than the supplied minimum
	 * and not more than the supplied maximum.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static float boxValue(float value, float min, float max) {
		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}
		return value;
	}
	
	/**
	 * Returns the distance between the two supplied points.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float distance(float x1, float y1, float x2, float y2){
		Vector2 tempVector = getVector2();
		float returnValue = tempVector.set(x1, y1).dst(x2, y2);
		freeVector2(tempVector);
		return returnValue;
	}
	
	/**
	 * Returns the distance between the two supplied points.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float distance(Position p1, Position p2){
		return distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}
	
	/**
	 * Returns the distance between the two PositionedThings.
	 * 
	 * @param pt1
	 * @param pt2
	 * @return
	 */
	public static float distance(PositionedThing pt1, PositionedThing pt2){
		return distance(pt1.position(), pt2.position());
	}
	
	/**
	 * Gets the coordinates of points representing a line originating in
	 * (x0,y0) and running to (x1, y1).
	 * 
	 * Implements the Bresenhams line algorithm.
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return	
	 */
	public static PositionArray getLine(int x0, int y0, int x1, int y1) {
		return getLine(x0, y0, x1, y1, -1);
	}
	
	/**
	 * Gets the coordinates of points representing a line originating in
	 * (x0,y0) and running to (x1, y1) with the supplied length.
	 * 
	 * Implements the Bresenhams line algorithm.
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return	
	 */
	public static PositionArray getLine(int x0, int y0, int x1, int y1, int length) {
		PositionArray returnValue = new PositionArray();
		int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
		int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
		int err = (dx > dy ? dx : -dy) / 2, e2;
		int counter = 0;
		
		while(length < 0 || counter < length) {
			returnValue.add(x0, y0);
			if (x0 == x1 && y0 == y1) {
				break;
			}
			e2 = err;
			if (e2 > -dx) {
				err -= dy;
				x0 += sx;
			}
			if (e2 < dy) {
				err += dx;
				y0 += sy;
			}
			++counter;
		}
		return returnValue;
	}
	
	/**
	 * Returns a cone starting at sx, sy, with the width defined by the supplied degrees and of the supplied length,
	 * pointing from the start coordinate in the supplied orientation.
	 * 
	 * @param sx
	 * @param sy
	 * @param degrees
	 * @param length
	 * @param orientation
	 * @return
	 */
	public static PositionArray getCone(int sx, int sy, int degrees, int length, Orientation orientation) {
		float myAngle = orientation.getDegrees();
		
		int x1end = Math.round(sx + length*MathUtils.sinDeg(myAngle - degrees/2f));
		int y1end = Math.round(sy + length*MathUtils.cosDeg(myAngle - degrees/2f));
		
		int x2end = Math.round(sx + length*MathUtils.sinDeg(myAngle + degrees/2f));
		int y2end = Math.round(sy + length*MathUtils.cosDeg(myAngle + degrees/2f));
		
		int targetX = Math.round(sx + length*MathUtils.sinDeg(myAngle));
		int targetY = Math.round(sy + length*MathUtils.cosDeg(myAngle));
		
		PositionArray returnValue =  MathUtil.getLine(sx, sy, x1end, y1end);
		returnValue.addAllNew(MathUtil.getLine(sx, sy, x2end, y2end));
		returnValue.addAllNew(MathUtil.getLine(x1end, y1end, targetX, targetY));
		returnValue.addAllNew(MathUtil.getLine(x2end, y2end, targetX, targetY));
		
		PositionArray lineTo = MathUtil.getLine(sx, sy, targetX, targetY, 2);
		if (lineTo.size() == 2) {
			MathUtil.fillArea(lineTo.getX(1), lineTo.getY(1), returnValue);
		} 	
		
		return returnValue;
	}
	
	/**
	 * Gets the coordinates of points representing a circle originating in
	 * (x0,y0) with the supplied radius. The circle can be either filled,
	 * or just the border is returned.
	 * 
	 * Implements the Bresenhams (Midpoint) algorithm.
	 * 
	 * @param x0
	 * @param y0
	 * @param radius
	 * @return	
	 */
	public static PositionArray getCircle(int cx, int cy, int radius, boolean filled)
	{
		PositionArray returnValue = new PositionArray();
		int error = -radius;
	    int x = radius;
	    int y = 0;

	    if (radius == 0) {
	    	returnValue.add(cx, cy);
	    	return returnValue;
	    } else if (radius == 1) {
	    	returnValue.add(cx, cy);
	    	returnValue.add(cx-1, cy);
	    	returnValue.add(cx, cy-1);
	    	returnValue.add(cx+1, cy);
	    	returnValue.add(cx, cy+1);
	    	return returnValue;
	    }
	    
	    while (x >= y)
	    {
	        int lastY = y;

	        error += y;
	        ++y;
	        error += y;

	        plot4points(returnValue, cx, cy, x, lastY, filled);

	        if (error >= 0)
	        {
	            if (x != lastY)
	                plot4points(returnValue, cx, cy, lastY, x, filled);

	            error -= x;
	            --x;
	            error -= x;
	        }
	    }
	    return returnValue;
	}

	private static void plot4points(PositionArray returnValue, int cx, int cy, int x, int y, boolean filled)
	{
		if (filled) {
			horizontalLine(returnValue, cx - x, cy + y, cx + x);
		} else {
			returnValue.add(cx - x, cy + y);
			returnValue.add(cx + x, cy + y);
		}
	    if (x != 0 && y != 0) {
	    	if (filled) {
	    		horizontalLine(returnValue, cx - x, cy - y, cx + x);
	    	} else {
	    		returnValue.add(cx - x, cy - y);
				returnValue.add(cx + x, cy - y);
	    	}
	    }
	}    
	
	private static void horizontalLine(PositionArray returnValue, int x0, int y0, int x1)
    {
		for (int x = x0; x <= x1; ++x) {
        	returnValue.add(x, y0); 
		}	
	}
	
	public static boolean isNextToOrOnTarget(AbstractGameCharacter character, PositionedThing target) {
		Position pos = target.position();
		return isNextToOrOnTarget(character, (int)pos.getX(), (int)pos.getY());
	}
	
	/**
	 * Returns true if the supplied character is on or standing next to
	 * the supplied tile.
	 * @param character
	 * @param target 
	 * @return
	 */
	public static boolean isNextToOrOnTarget(AbstractGameCharacter character, Tile target) {
		return isNextToOrOnTarget(character, target.getX(), target.getY());
	}
	
	/**
	 * Returns true if the supplied character is on or standing next to
	 * the supplied tile.
	 * @param character
	 * @param target 
	 * @return
	 */
	public static boolean isNextToOrOnTarget(AbstractGameCharacter character, int x, int y) {
		Tile charPos = character.position().tile();
		return Vector2.dst(charPos.getX(), charPos.getY(), x, y) < 2;
	}
	
	/**
	 * Prolongs the line defined by the start and end points by
	 * the supplied length.
	 * 
	 * The line will be prolonged in the direction of the endpoint.
	 * 
	 * @param startPoint
	 * @param endPoint
	 * @param lengthenBy
	 */
	public static void lengthenLine(Vector2 startPoint, Vector2 endPoint, float lengthenBy)
	{
	  if (startPoint.equals(endPoint))
	    return; // not a line

	  double dx = endPoint.x - startPoint.x;
	  double dy = endPoint.y - startPoint.y;
	  if (dx == 0)
	  {
	    // vertical line:
	    if (endPoint.y < startPoint.y)
	      endPoint.y -= lengthenBy;
	    else
	      endPoint.y += lengthenBy;
	  }
	  else if (dy == 0)
	  {
	    // horizontal line:
	    if (endPoint.x < startPoint.x)
	      endPoint.x -= lengthenBy;
	    else
	      endPoint.x += lengthenBy;
	  }
	  else
	  {
	    // non-horizontal, non-vertical line:
	    double length = Math.sqrt(dx * dx + dy * dy);
	    double scale = (length + lengthenBy) / length;
	    dx *= scale;
	    dy *= scale;
	    endPoint.x = startPoint.x + (float)dx;
	    endPoint.y = startPoint.y + (float)dy;
	  }
	}

	/**
	 * Returns true if the supplied rectangle contains any vertex of the
	 * supplied polygon.
	 * 
	 * @param polygon
	 * @param rectangle
	 * @return
	 */
	public static final boolean containsAnyVertex(Rectangle rectangle, Polygon polygon) {
		float[] vertices = polygon.getTransformedVertices();
		
		for (int i = 0; i < vertices.length; i += 2) {
			if (rectangle.contains(vertices[i], vertices[i+1])) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a new SaveablePolygon instance from the supplied Rectangle.
	 * @param rec
	 * @return
	 */
	public static final SaveablePolygon polygonFromRectangle(Rectangle rec) {
		SaveablePolygon polygon = new SaveablePolygon(new float[] {0, 0,  rec.getWidth(), 0,  rec.getWidth(),  rec.getHeight(), 0,  rec.getHeight(), 0, 0});
		polygon.setPosition(rec.x, rec.y);
		return polygon;
	}
	
	public static final float[] transformVerticesFromTileToScreen(float[] vertices, GameMap map) {
		float [] returnValue = Arrays.copyOf(vertices, vertices.length);
		if (map.isIsometric()) {
			Vector2 tempVector = MathUtil.getVector2();
			for (int i = 0; i < returnValue.length; i += 2) {
				tempVector.set(returnValue[i], returnValue[i+1]);
				map.projectFromTiles(tempVector);
				returnValue[i] = tempVector.x;
				returnValue[i+1] = tempVector.y;
			}
			MathUtil.freeVector2(tempVector);
		}
		return returnValue;
	}

	/** Calculates and returns the vertices of the polygon after scaling, rotation, and positional translations have been applied,
	 * as they are position within the world.
	 * 
	 * @return vertices scaled, rotated, and offset by the polygon position. */
	public static final float[] scaleAndRotateVertices(float[] vertices, float scaleX, float scaleY, float rotation) {
		float[] returnValue = new float[vertices.length];
		final boolean scale = scaleX != 1 || scaleY != 1;
		final float cos = MathUtils.cosDeg(rotation);
		final float sin = MathUtils.sinDeg(rotation);

		for (int i = 0, n = vertices.length; i < n; i += 2) {
			float x = vertices[i];
			float y = vertices[i + 1];

			if (scale) {
				x *= scaleX;
				y *= scaleY;
			}

			if (rotation != 0) {
				float oldX = x;
				x = cos * x - sin * y;
				y = sin * oldX + cos * y;
			}

			returnValue[i] = x;
			returnValue[i + 1] = y;
		}
		return returnValue;
	}
}

