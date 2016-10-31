package mg.fishchicken.gamelogic.characters.los;

import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.core.util.TileSet;
import mg.fishchicken.gamelogic.locations.GameMap;
import box2dLight.CollisionFractionComparator;
import box2dLight.FixtureUserData;
import box2dLight.FixtureUserData.UserDataType;
import box2dLight.RayHandler;
import box2dLight.RaycastCallbackHandler;
import box2dLight.RaycastCallbackHandler.CollisionInfo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectSet;

public abstract class LineOfSight {

	final static int MIN_RAYS = 3;
	
	protected float[] mx;
	protected float[] my;
	
	protected RayHandler rayHandler;
	protected int rayNum;
	protected int distance;
	
	final Vector2 tmpPosition = new Vector2();
	final float sin[];
	final float cos[];

	final Vector2 start = new Vector2();
	final float endX[];
	final float endY[];
	protected RaycastCallbackHandler raycastCallbackHandler = null;
	
	private TileSet tileVertices;
	private PositionArray visibleTiles;
	private ObjectSet<Polygon> visibleShapePolygons;
	
	private final Vector2 tempVector = new Vector2();
	private final Vector2 projectedStart = new Vector2();
	
	private GameMap map;
	
	LineOfSight(RayHandler rayHandler, int rays,
			int distance, float x, float y, GameMap map) {
		start.x = x;
		start.y = y;
		sin = new float[rays];
		cos = new float[rays];
		endX = new float[rays];
		endY = new float[rays];
		
		visibleShapePolygons = new ObjectSet<Polygon>();
		
		this.rayHandler = rayHandler;
		setRayNum(rays);
		this.distance = distance < 1 ? 1 : distance;
		tileVertices = new TileSet((distance+1)*2);
		this.map = map;
	}
	
	private final void setRayNum(int rays) {

		if (rays < MIN_RAYS)
			rays = MIN_RAYS;

		rayNum = rays;

		mx = new float[rays + 1];
		my = new float[rays + 1];

	}

	public Vector2 getPosition() {
		tmpPosition.x = start.x;
		tmpPosition.y = start.y;
		return tmpPosition;
	}

	/**
	 * horizontal starting position of light in world coordinates.
	 */
	public float getX() {
		return start.x;
	}

	/**
	 * vertical starting position of light in world coordinates.
	 */
	public float getY() {
		return start.y;
	}
	
	public void setPosition(float x, float y) {
		start.x = x;
		start.y = y;
		update();
	}

	public void setPosition(Vector2 position) {
		start.x = position.x;
		start.y = position.y;
		update();
	}
	
	protected void update() {
		visibleShapePolygons.clear();
		tileVertices.clear();
		map.projectToTiles(projectedStart.set(start));
		projectedStart.set((int)projectedStart.x, (int)projectedStart.y);
		for (int i = 0; i < rayNum; i++) {
			tempVector.x = endX[i] + start.x;
			mx[i] = tempVector.x;
			tempVector.y = endY[i] + start.y;
			my[i] = tempVector.y;
			// for disposed maps we do no raycasting, since the box2d world is no longer valid
			if (rayHandler.world != null && !map.isDisposed()) {
				getRayCastCallback().reset();
				rayHandler.world.rayCast(getRayCastCallback(),start, tempVector);
				getRayCastCallback().collisions.sort(CollisionFractionComparator.singleton());
				boolean shouldBreak = false;
				boolean enteredGroundPolygon = false;
				boolean enteredTilePolygon = false;
				for (CollisionInfo ci : getRayCastCallback().collisions) {
					FixtureUserData userData = (FixtureUserData) ci.fixture.getUserData();
					switch (userData.type) {
						case LOS_BLOCKER_TILE: 
							shouldBreak = enteredTilePolygon;
							enteredTilePolygon = true;
							break;
						case LOS_BLOCKER_POLYGON_GROUND:
							shouldBreak = enteredGroundPolygon;
							enteredGroundPolygon = true;
							break;
						case LOS_BLOCKER_LINE:
							shouldBreak = true;
							break;
						default:
					}
					
					if (shouldBreak) {
						// we will move the collision point a little back to avoid any floating point
						// shenanigans and to make sure we hit the right tile later when we cast to int
						// 0.001 seems to work okay, its kind of magic, but it works, so what the heck
						float dist = start.dst(ci.point);
						float r = 0.001f / dist;
						
						mx[i] = r * start.x + (1 - r) * ci.point.x;
						my[i] = r * start.y + (1 - r) * ci.point.y;
						break;
					}
					
					if (userData.type == UserDataType.LOS_BLOCKER_POLYGON) {
						visibleShapePolygons.add(userData.polygon);
					}
				}
			}
			map.projectToTiles(tempVector.set(mx[i], my[i]));
			tileVertices.add((int)tempVector.x, (int)tempVector.y);
		}
		visibleTiles = fillPolygon(tileVertices.getTiles(), (int)projectedStart.x, (int)projectedStart.y);
	}
	
	private PositionArray fillPolygon(PositionArray vertices, int centerX, int centerY) {
		PositionArray returnValue = new PositionArray(vertices);
		int numberOfVertices = vertices.size();
		int nodes, nodeY, i, j, swap;
		int[] nodeX = new int[numberOfVertices];

		int xMin = centerX - distance;
		int xMax = centerX + distance;
		int yMin = centerY - distance;
		int yMax = centerY + distance;
		
		for (nodeY = yMin; nodeY < yMax; nodeY++) {

			// Build a list of nodes.
			nodes = 0;
			j = numberOfVertices - 1;
			for (i = 0; i < numberOfVertices; i++) {
				int polyXI = vertices.getX(i);
				int polyYI = vertices.getY(i);
				int polyXJ = vertices.getX(j);
				int polyYJ = vertices.getY(j);

				if (polyYI < nodeY && polyYJ >= nodeY
						|| polyYJ < nodeY
						&& polyYI >= nodeY) {
					nodeX[nodes++] =  Math.round((polyXI + (nodeY - polyYI)
							/ (float)(polyYJ - polyYI) * (polyXJ - polyXI)));
				}
				j = i;
			}

			// Sort the nodes, via a simple Bubble sort.
			i = 0;
			while (i < nodes - 1) {
				if (nodeX[i] > nodeX[i + 1]) {
					swap = nodeX[i];
					nodeX[i] = nodeX[i + 1];
					nodeX[i + 1] = swap;
					if (i > 0)
						i--;
				} else {
					i++;
				}
			}

			// Fill the tiles between node pairs.
			for (i = 0; i < nodes; i += 2) {
				if (nodeX[i] >= xMax)
					break;
				if (nodeX[i + 1] > xMin) {
					if (nodeX[i] < xMin)
						nodeX[i] = xMin;
					if (nodeX[i + 1] > xMax)
						nodeX[i + 1] = xMax;
					for (j = nodeX[i]+1; j < nodeX[i + 1]; j++) {
						returnValue.add(j, nodeY);
					}
				}
			}
		}
		return returnValue;
	}
	
	public PositionArray getVisibleTiles() {
		return visibleTiles;
	}
	
	/**
	 * Renders debug information for this LOS using the supplied renderer.
	 * 
	 * This is not optimized and probably a little slow, so don't call this 
	 * for anything other than debug.
	 * 
	 * @param renderer
	 */
	public void renderDebug(ShapeRenderer renderer) {
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.PINK);
		float lastX = 0f, lastY = 0f;
		for (int i = 0; i < rayNum; ++i) {
			renderer.rect(mx[i], my[i], 0.15f, 0.15f);
			if (i > 0) {
				renderer.line(lastX, lastY, mx[i], my[i]);
			}
			lastX = mx[i];
			lastY = my[i];
		}
		renderer.setColor(Color.CYAN);
		Vector2 tempVector = MathUtil.getVector2();
		PositionArray tiles = tileVertices.getTiles();
		for (int i = 0; i < tiles.size(); ++i) {
			tempVector.set(tiles.getX(i), tiles.getY(i));
			map.projectFromTiles(tempVector);
			renderer.rect(tempVector.x, tempVector.y, 0.2f, 0.2f);
			if (i > 0) {
				renderer.line(lastX, lastY, tempVector.x, tempVector.y);
			}
			lastX = tempVector.x;
			lastY = tempVector.y;
		}
		tempVector.set(tiles.getX(0), tiles.getY(0));
		map.projectFromTiles(tempVector);
		renderer.line(lastX, lastY, tempVector.x, tempVector.y);
		/*
		renderer.setColor(Color.RED);
		for (int i = 0; i < visibleTiles.size(); ++i) {
			tempVector.set(visibleTiles.getX(i)+0.3f, visibleTiles.getY(i)+0.3f);
			map.projectFromTiles(tempVector);
			renderer.rect(tempVector.x, tempVector.y, 0.4f, 0.4f);
		}*/
		MathUtil.freeVector2(tempVector);
		renderer.end();
	}
	
	/**
	 * Returns true if this LOS contains the supplied
	 * point in the camera coordinate system within its radius. 
	 * 
	 */
	public abstract boolean containsInRadius(float x, float y);
	
	/**
	 * Returns true of this LOS contains the supplied coordinates
	 * in any visible shape polygons it currently contains.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isContainedInVisibleShapePolygon(int x, int y) {
		float xf = x * map.getTileSizeX(); 
		float yf = y * map.getTileSizeY();
		for (Polygon polygon : visibleShapePolygons) {
			if (polygon.contains(xf, yf)) {
				return true;
			}
		}
		return false;
		
	}
	
	protected RaycastCallbackHandler getRayCastCallback() {
		if (raycastCallbackHandler == null) {
			raycastCallbackHandler = new RaycastCallbackHandler();
		}
		return raycastCallbackHandler;
	}
}
