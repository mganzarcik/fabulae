package box2dLight;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class RaycastCallbackHandler implements RayCastCallback {

	private static Pool<CollisionInfo> infoPool = new Pool<CollisionInfo>() {
		@Override
		protected CollisionInfo newObject () {
			return new CollisionInfo();
		}
	};
	
	public Array<CollisionInfo> collisions = new Array<CollisionInfo>();
	
	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point,
			Vector2 normal, float fraction) {
		collisions.add(infoPool.obtain().set(fixture, point, normal, fraction));
		return 1;
	}
	
	public void reset() {
		infoPool.freeAll(collisions);
		collisions.clear();
	}
	
	public static class CollisionInfo {
		public float fraction;
		public Vector2 point, normal;
		public Fixture fixture;
		
		private CollisionInfo() {
			this.point = new Vector2();
			this.normal = new Vector2();
		}
		
		public CollisionInfo set(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
			this.fixture = fixture;
			this.point.set(point);
			this.normal.set(normal);
			this.fraction = fraction;
			return this;
		}
	}
}
