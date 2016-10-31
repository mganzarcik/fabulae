package mg.fishchicken.graphics.renderers;

import java.util.Comparator;

import mg.fishchicken.graphics.Drawable;

public class OrthogonalDrawOrderComparator implements Comparator<Drawable> {
	
	private static final int BEHIND = -1;
	private static final int IN_FRONT = 1;

	public int compare(Drawable o1, Drawable o2) {
		if (o1.isAlwaysBehind() && o2.isAlwaysBehind()) {
			return 0;
		} else if (o1.isAlwaysBehind()) {
			return BEHIND;
		} else if (o2.isAlwaysBehind()) {
			return IN_FRONT;
		}

		if (o1.isAlwaysInFront() && o2.isAlwaysInFront()) {
			return 0;
		} else if (o1.isAlwaysInFront()) {
			return IN_FRONT;
		} else if (o2.isAlwaysInFront()) {
			return BEHIND;
		}
		
		return Float.compare(o1.getZIndex(), o2.getZIndex());
	}
}
