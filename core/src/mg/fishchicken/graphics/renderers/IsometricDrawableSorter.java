package mg.fishchicken.graphics.renderers;

import java.util.Comparator;

import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.graphics.Drawable;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class IsometricDrawableSorter  {
	Comparator<Drawable> comparator = new OrthogonalDrawOrderComparator();
	
	public Array<Drawable> sort(Array<Drawable> drawables) {
		drawables.sort(comparator);
		return drawables;
	}
	
	// TODO: this is a very experimental "better" sorting algorithm
	// it is currently super slow and has cases where it fails with very bad results
	// (in the dream dungeon, go to the secret room to reproduce)
	// so in fact it is not better at all, it sucks
	// see below for some alternatives
	// https://github.com/shaunlebron/IsometricBlocks (will not help fully, has no support for overlaps)
	// https://mazebert.com/2013/04/18/isometric-depth-sorting/
	// https://github.com/as3isolib/as3isolib.v1 (DefaultSceneLayoutRenderer)
	public Array<Drawable> sort2(Array<Drawable> drawables, Rectangle cullingRectangle) {
		Array<Drawable> drawablesToDraw = new Array<Drawable>();
		
		for (Drawable d : drawables) {
			if (d.shouldDraw(cullingRectangle)) {
				drawablesToDraw.add(d);
			}
		}
		return sort2Internal(drawablesToDraw);
	}
	
	public Array<Drawable> sort2Internal(Array<Drawable> drawables) {
		int i, j, numBlocks = drawables.size;
		
		ObjectMap<Drawable, Array<Drawable>> behind = new ObjectMap<Drawable, Array<Drawable>>();
		ObjectMap<Drawable, Array<Drawable>> inFront = new ObjectMap<Drawable, Array<Drawable>>(); 
		
		// For each pair of blocks, determine which is in front and behind.
		Drawable a,b,frontDrawable;
		for (i=0; i<numBlocks; i++) {
			a = drawables.get(i);
			for (j=i+1; j<numBlocks; j++) {
				b = drawables.get(j);
				frontDrawable = getFrontDrawable(a,b);
				if (frontDrawable != null) {
					if (a == frontDrawable) {
						CoreUtil.addToArrayMap(behind, a, b);
						CoreUtil.addToArrayMap(inFront, b, a);
					}
					else {
						CoreUtil.addToArrayMap(behind, b, a);
						CoreUtil.addToArrayMap(inFront, a, b);
					}
				}
			}
		}

		// Get list of blocks we can safely draw right now.
		// These are the blocks with nothing behind them.
		Array<Drawable> drawablesToDraw = new Array<Drawable>();
		for (i=0; i<numBlocks; i++) {
			Drawable drawable = drawables.get(i);
			if (behind.get(drawable) == null || behind.get(drawable).size < 1) {
				drawablesToDraw.add(drawable);
			}
		}

		// While there are still blocks we can draw...
		Array<Drawable> blocksDrawn = new Array<Drawable>();
		while (drawablesToDraw.size > 0) {

			// Draw block by removing one from "to draw" and adding
			// it to the end of our "drawn" list.
			Drawable drawable = drawablesToDraw.pop();
			blocksDrawn.add(drawable);

			// Tell blocks in front of the one we just drew
			// that they can stop waiting on it.
			Array<Drawable> inFrontOfMe = inFront.get(drawable);
			int inFrontSize =  inFrontOfMe != null ? inFrontOfMe.size : 0;
			for (j=0; j<inFrontSize; j++) {
				Drawable drawableInFrontOfMe = inFrontOfMe.get(j);

				// Add this front block to our "to draw" list if there's
				// nothing else behind it waiting to be drawn.
				CoreUtil.removeFromArrayMap(behind, drawableInFrontOfMe, drawable);
				Array<Drawable> drawablesBehind = behind.get(drawableInFrontOfMe);
				if (drawablesBehind == null || drawablesBehind.size == 0) {
					drawablesToDraw.add(drawableInFrontOfMe);
				}
			}
		}

		return blocksDrawn;
	}
	
	private Drawable getFrontDrawable(Drawable a, Drawable b) {
		float ax = a.position().getX();
		float ay = a.position().getY();
		float aw = a.getWidth()-1;
		float ah = a.getHeight()-1;
		
		float bx = b.position().getX();
		float by = b.position().getY();
		float bw = b.getWidth()-1;
		float bh = b.getHeight()-1;
		
		
		/*if (bw <= aw && bh <= ah && bx >= ax && by>= ay) {
			return a; 
		} else if (aw <= bw && ah <= bh && ax >= bx && ay>= by) {
			return b;
		} else if (a.intersects(b)) {
			return Float.compare(a.getZIndex(), b.getZIndex());
		}*/
		
		if ((by+bh < ay && bx+bw >= ax) || (bx > ax+aw && by >= ay+ah)) {
			return b;
		}
		
		return a;
	}
	
}
