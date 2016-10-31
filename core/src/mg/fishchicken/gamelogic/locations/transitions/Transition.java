package mg.fishchicken.gamelogic.locations.transitions;

import java.util.Arrays;
import java.util.Locale;

import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class Transition {

	private String targetMap;
	private int targetX;
	private int targetY;
	protected Polygon polygon;
	private float[] cameraVertices;

	public Transition(GameMap parentMap, String targetMap, int targetX, int targetY, Polygon polygon) {
		this.targetMap = targetMap.toLowerCase(Locale.ENGLISH);
		this.targetX = targetX;
		this.targetY = targetY;
		this.polygon = polygon;
		cameraVertices = Arrays.copyOf(polygon.getTransformedVertices(), polygon.getTransformedVertices().length);
		if (parentMap.isIsometric()) {
			Vector2 tempVector = MathUtil.getVector2();
			for (int i = 0; i < cameraVertices.length; i += 2) {
				tempVector.set(cameraVertices[i], cameraVertices[i+1]);
				parentMap.projectFromTiles(tempVector);
				cameraVertices[i] = tempVector.x;
				cameraVertices[i+1] = tempVector.y;
			}
			MathUtil.freeVector2(tempVector);
		}
	}

	public String getTargetMap() {
		return targetMap;
	}


	public int getTargetX() {
		return targetX;
	}


	public int getTargetY() {
		return targetY;
	}
	
	/**
	 * Returns the vertices of the polygon defining this Transition in
	 * the tile coordinate system.
	 * @return
	 */
	public float[] getVertices() {
		return polygon.getTransformedVertices();
	}
	
	/**
	 * Returns the vertices of the polygon defining this Transition in
	 * the camera coordinate system.
	 * 
	 * @return
	 */
	public float[] getCameraVertices() {
		return cameraVertices;
	}
	
	public boolean contains(float x, float y) {
		return polygon.contains(x, y);
	}
}
