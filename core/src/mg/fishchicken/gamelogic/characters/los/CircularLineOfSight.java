package mg.fishchicken.gamelogic.characters.los;

import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;
import box2dLight.RayHandler;

import com.badlogic.gdx.math.MathUtils;

public class CircularLineOfSight extends LineOfSight {

	private float distanceX, distanceY;

	/**
	 * @param rayHandler
	 * @param rays
	 * @param color
	 * @param radius
	 * @param x
	 * @param y
	 */
	public CircularLineOfSight(RayHandler rayHandler, int rays, int radius,
			float x, float y, GameMap map) {
		super(rayHandler, rays,
				(map.isIsometric() ? (int) (radius * MathUtil.SQRT_TWO)
						: radius), x, y, map);
		if (map.isIsometric()) {
			distanceX = distance;
			distanceY = distanceX / 2;
		} else {
			// for orthogonal maps, make sure the radius is actually a little
			// less
			// to correct for the integer rounding that would make the resulting
			// circle uneven otherwise
			distanceX = distance - 0.1f;
			distanceY = distance - 0.1f;
		}
		setEndPoints();
		update();
	}

	private final void setEndPoints() {
		float angleNum = 360f / (rayNum - 1);
		for (int i = 0; i < rayNum; i++) {
			final float angle = angleNum * i;
			sin[i] = MathUtils.sinDeg(angle);
			cos[i] = MathUtils.cosDeg(angle);
			endX[i] = distanceX * cos[i];
			endY[i] = distanceY * sin[i];
		}
	}

	/**
	 * Returns true if this LOS contains the supplied point in the camera
	 * coordinate system within its radius.
	 * 
	 */
	public boolean containsInRadius(float x, float y) {
		final float x_d = start.x - x;
		final float y_d = start.y - y;
		return ((x_d * x_d) / (distanceX * distanceX))
				+ ((y_d * y_d) / (distanceY * distanceY)) <= 1;
	}
}
