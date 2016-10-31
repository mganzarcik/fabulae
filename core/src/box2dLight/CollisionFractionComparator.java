package box2dLight;

import java.util.Comparator;

import box2dLight.RaycastCallbackHandler.CollisionInfo;

public class CollisionFractionComparator implements Comparator<CollisionInfo>{

	private static final CollisionFractionComparator singleton = new CollisionFractionComparator();
	
	public static CollisionFractionComparator singleton() {
		return singleton;
	}
	
	@Override
	public int compare(CollisionInfo arg0, CollisionInfo arg1) {
		return Float.compare(arg0.fraction, arg1.fraction);
	}

}
