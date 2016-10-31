package box2dLight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class PositionalLight extends Light {

	private Body body;
	private float bodyOffsetX;
	private float bodyOffsetY;
	protected final float sin[];
	protected final float cos[];

	final Vector2 start = new Vector2();
	protected final float endX[];
	protected final float endY[];

	/** attach positional light to automatically follow body. Position is fixed to given offset. */
	@Override
	public void attachToBody (Body body, float offsetX, float offSetY) {
		this.body = body;
		bodyOffsetX = offsetX;
		bodyOffsetY = offSetY;
		dirty = true;
	}

	@Override
	public Vector2 getPosition () {
		tmpPosition.x = start.x;
		tmpPosition.y = start.y;
		return tmpPosition;
	}

	public Body getBody () {
		return body;
	}

	/** horizontal starting position of light in world coordinates. */
	@Override
	public float getX () {
		return start.x;
	}

	/** vertical starting position of light in world coordinates. */
	@Override
	public float getY () {
		return start.y;
	}

	private final Vector2 tmpEnd = new Vector2();

	@Override
	public void setPosition (float x, float y) {
		start.x = x;
		start.y = y;
		dirty = true;
	}
	
	void staticUpdate() {
		boolean tmp = rayHandler.culling;
		staticLight = !staticLight;
		rayHandler.culling = false;
		update();
		rayHandler.culling = tmp;
		staticLight = !staticLight;
	}

	@Override
	public void update () {
		boolean isUnchangedStatic = staticLight && !dirty;
		dirty = false;
		if (body != null && !isUnchangedStatic) {
			final Vector2 vec = body.getPosition();
			float angle = body.getAngle();
			final float cos = MathUtils.cos(angle);
			final float sin = MathUtils.sin(angle);
			final float dX = bodyOffsetX * cos - bodyOffsetY * sin;
			final float dY = bodyOffsetX * sin + bodyOffsetY * cos;
			start.x = vec.x + dX;
			start.y = vec.y + dY;
			setDirection(angle * MathUtils.radiansToDegrees);
		}

		if (rayHandler.culling && !isUnchangedStatic) {
			culled = ((!rayHandler.intersect(start.x, start.y, distance + softShadowLenght)));
			if (culled) return;
		}

		if (isUnchangedStatic) {
			return;
		}

		for (int i = 0; i < rayNum; i++) {
			m_index = i;
			f[i] = 1f;
			tmpEnd.x = endX[i] + start.x;
			mx[i] = tmpEnd.x;
			tmpEnd.y = endY[i] + start.y;
			my[i] = tmpEnd.y;
			if (rayHandler.world != null && !xray) {
				rayHandler.world.rayCast(getRayCastCallback(), start, tmpEnd);
			}
		}
		setMesh();
	}

	protected void setMesh () {
		// ray starting point
		int nonSoftIndex = 0;
		int softIndex = 0;

		nonSoftSegments[nonSoftIndex++] = start.x;
		nonSoftSegments[nonSoftIndex++] = start.y;
		nonSoftSegments[nonSoftIndex++] = colorF;
		nonSoftSegments[nonSoftIndex++] = 1;
		// rays ending points.
		for (int i = 0; i < rayNum; i++) {
			final float s = (1 - f[i]);
			nonSoftSegments[nonSoftIndex++] = mx[i];
			nonSoftSegments[nonSoftIndex++] = my[i];
			nonSoftSegments[nonSoftIndex++] = colorF;
			nonSoftSegments[nonSoftIndex++] = s;
			if (soft && !xray) {
				softSegments[softIndex++] = mx[i];
				softSegments[softIndex++] = my[i];
				softSegments[softIndex++] = colorF;
				softSegments[softIndex++] = s;
				softSegments[softIndex++] = mx[i] + s * softShadowLenght * cos[i];
				softSegments[softIndex++] = my[i] + s * softShadowLenght * sin[i];
				softSegments[softIndex++] = zero;
				softSegments[softIndex++] = 0f;
			}
		}
		lightMesh.setVertices(nonSoftSegments, 0, nonSoftIndex);

		if (soft && !xray) {
			softShadowMesh.setVertices(softSegments, 0, softIndex);
		}
	}

	@Override
	public void render () {
		if (rayHandler.culling && culled) return;

		rayHandler.lightRenderedLastFrame++;
		lightMesh.render(rayHandler.lightShader, GL20.GL_TRIANGLE_FAN, 0, vertexNum);
		if (soft && !xray) {
			softShadowMesh.render(rayHandler.lightShader, GL20.GL_TRIANGLE_STRIP, 0, (vertexNum - 1) * 2);
		}
	}

	public PositionalLight (RayHandler rayHandler, int rays, Color color, float distance, float x, float y, float directionDegree) {
		super(rayHandler, rays, color, directionDegree, distance);
		start.x = x;
		start.y = y;
		sin = new float[rays];
		cos = new float[rays];
		endX = new float[rays];
		endY = new float[rays];

		lightMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum, 0, new VertexAttribute(Usage.Position, 2,
			"vertex_positions"), new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
			new VertexAttribute(Usage.Generic, 1, "s"));
		softShadowMesh = new Mesh(VertexDataType.VertexArray, false, vertexNum * 2, 0, new VertexAttribute(Usage.Position, 2,
			"vertex_positions"), new VertexAttribute(Usage.ColorPacked, 4, "quad_colors"),
			new VertexAttribute(Usage.Generic, 1, "s"));
		setMesh();
	}

	@Override
	public boolean contains (float x, float y) {

		// fast fail
		final float x_d = start.x - x;
		final float y_d = start.y - y;
		final float dst2 = x_d * x_d + y_d * y_d;
		if (distance * distance <= dst2) return false;

		// actual check

		boolean oddNodes = false;
		float x2 = mx[rayNum] = start.x;
		float y2 = my[rayNum] = start.y;
		float x1, y1;
		for (int i = 0; i <= rayNum; x2 = x1, y2 = y1, ++i) {
			x1 = mx[i];
			y1 = my[i];
			if (((y1 < y) && (y2 >= y)) || (y1 >= y) && (y2 < y)) {
				if ((y - y1) / (y2 - y1) * (x2 - x1) < (x - x1)) oddNodes = !oddNodes;
			}
		}
		return oddNodes;

	}
}
