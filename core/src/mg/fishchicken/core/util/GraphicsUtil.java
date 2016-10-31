package mg.fishchicken.core.util;

import java.util.Locale;

import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnShape;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

public class GraphicsUtil {
	
	private GraphicsUtil() {
	}

	/**
	 * Transforms the supplied offsets supplied in camera coordinate system
	 * into corresponding offsets in tile coordinate system of the map.
	 * 
	 * @param map
	 * @param offsets
	 * @return
	 */
	public static Vector2 transformOffsets(GameMap map, Vector2 offsets) {
		map.projectToTiles(offsets);
		if (map.isIsometric()) {
			offsets.add(0.25f, 0.25f);
		}
		return offsets;
	}
	
	/**
	 * Resizes the supplied particle emitters by setting
	 * their width and height to the new value.
	 * 
	 * This will also increase the spawn rates and maximum 
	 * number of particles to compensate for the larger area.
	 * 
	 * Only works correctly for square shaped emitters.
	 * 
	 * All the supplied emitters should be of the same size.
	 * 
	 * @param emitters
	 * @param newSize
	 * @return returns the multiplier by which each property
	 * of the emitters was modified
	 * 
	 */
	public static float resize(Array<ParticleEmitter> emitters, float newSize) {
		float scale = 0;
		for (ParticleEmitter e : emitters) {
			scale = newSize / e.getSpawnWidth().getHighMax();
			scale(e, scale, false, false);
		}
		return scale;
	}
	
	/**
	 * Scales the supplied particle emmiters by multiplying the 
	 * velocity and scale settings of all its emitters
	 * by the supplied value.
	 * 
	 * @param pe
	 * @param scale
	 */
	public static void scale(Array<ParticleEmitter> emitters, float scale, boolean isometric, boolean scaleParticles) {
		for (ParticleEmitter e : emitters) {
			scale(e, scale, isometric, scaleParticles);
		}
	}

	/**
	 * Scales the supplied particle emmiters by multiplying the velocity and
	 * scale settings of all its emitters by the supplied value.
	 * 
	 * @param pe
	 * @param scale
	 * @param scaleParticles
	 *            if true, the particles themselves are scaled up to create a
	 *            larger effect. If false, the particles remain the same size,
	 *            but the emission rate and min / max particle counts are
	 *            increased instead
	 */
	public static void scale(ParticleEmitter emitter, float scale, boolean isometric, boolean scaleParticles) {
		float scaling;
		
		if (scaleParticles) {
			scaling = emitter.getScale().getHighMax();
		    emitter.getScale().setHighMax(scaling * scale);
		    
		    scaling = emitter.getScale().getHighMin();
		    emitter.getScale().setHighMin(scaling * scale);
	
		    scaling = emitter.getScale().getLowMax();
		    emitter.getScale().setLowMax(scaling * scale);
	
		    scaling = emitter.getScale().getLowMin();
		    emitter.getScale().setLowMin(scaling * scale);
		} else {
			scaling = emitter.getMinParticleCount();
			emitter.setMinParticleCount((int)(scaling*scale));
			
			scaling = emitter.getMaxParticleCount();
			emitter.setMaxParticleCount((int)(scaling*scale));
			
			scaling = emitter.getEmission().getHighMax();
			emitter.getEmission().setHighMax(scaling*scale);
			
			scaling = emitter.getEmission().getHighMin();
			emitter.getEmission().setHighMin(scaling*scale);
			
			scaling = emitter.getEmission().getLowMax();
			emitter.getEmission().setLowMax(scaling*scale);
			 
			scaling = emitter.getEmission().getLowMin();
			emitter.getEmission().setLowMin(scaling*scale);
		}
	    if (scaleParticles || SpawnShape.point.equals(emitter.getSpawnShape().getShape()) ) {
	    	scaling = emitter.getVelocity().getHighMax();
		    emitter.getVelocity().setHighMax(scaling * scale);

		    scaling = emitter.getVelocity().getHighMin();
		    emitter.getVelocity().setHighMin(scaling * scale);

		    scaling = emitter.getVelocity().getLowMax();
		    emitter.getVelocity().setLowMax(scaling * scale); 
		    
		    scaling = emitter.getVelocity().getLowMin();
		    emitter.getVelocity().setLowMin(scaling * scale);
	    }
		
	    scaling = emitter.getSpawnHeight().getHighMax();
	    emitter.getSpawnHeight().setHighMax(scaling * scale * (isometric ? 0.66f : 1f)); 
	    
	    scaling = emitter.getSpawnHeight().getHighMin();
	    emitter.getSpawnHeight().setHighMin(scaling * scale * (isometric ? 0.66f : 1f)); 
	    
	    scaling = emitter.getSpawnHeight().getLowMax();
	    emitter.getSpawnHeight().setLowMax(scaling * scale * (isometric ? 0.66f : 1f)); 
	    
	    scaling = emitter.getSpawnHeight().getLowMin();
	    emitter.getSpawnHeight().setLowMin(scaling * scale * (isometric ? 0.66f : 1f)); 
	    
	    scaling = emitter.getSpawnWidth().getHighMax();
	    emitter.getSpawnWidth().setHighMax(scaling * scale * (isometric ? 1.33f : 1f)); 
	    
	    scaling = emitter.getSpawnWidth().getHighMin();
	    emitter.getSpawnWidth().setHighMin(scaling * scale * (isometric ? 1.33f : 1f)); 
	    
	    scaling = emitter.getSpawnWidth().getLowMax();
	    emitter.getSpawnWidth().setLowMax(scaling * scale * (isometric ? 1.33f : 1f)); 
	    
	    scaling = emitter.getSpawnWidth().getLowMin();
	    emitter.getSpawnWidth().setLowMin(scaling * scale * (isometric ? 1.33f : 1f)); 
	}
	
	/**
	 * Scales the supplied particle effect by multiplying the 
	 * velocity and scale settings of all its emitters
	 * by the supplied value.
	 * 
	 * @param pe
	 * @param scale
	 */
	public static void scale(ParticleEffect pe, float scale, boolean isometric, boolean scaleParticles) {
		scale(pe.getEmitters(), scale, isometric, scaleParticles);
	}
	
	
	/**
	 * Returns all emitters from the supplied effect with a name 
	 * that starts with the supplied prefix.
	 * 
	 * Case insensitive.
	 * 
	 * @param pe
	 * @param prefix
	 * @return
	 */
	public static Array<ParticleEmitter> getEmittersByName(ParticleEffect pe, String prefix) {
		prefix = prefix.toLowerCase(Locale.ENGLISH);
		Array<ParticleEmitter> returnValue = new  Array<ParticleEmitter>();
		for (ParticleEmitter p : pe.getEmitters()) {
			if (p.getName().toLowerCase(Locale.ENGLISH).startsWith(prefix)) {
				returnValue.add(p);
			}
		}
		return returnValue;
	}
	
	/**
	 * Rotates the supplied emitters by the supplied
	 * angle by adding it to the emission
	 * angleemitters.
	 * 
	 * @param pe
	 * @param angle the angle to rotate by, in degrees
	 */
	public static void rotateBy(Array<ParticleEmitter> emitters, float angle) {
		for (ParticleEmitter e : emitters) {
			float firstHighMin = e.getAngle().getHighMin();
			float firstHighMax = e.getAngle().getHighMax();
			float firstLowMax = e.getAngle().getLowMax();
			float firstLowMin = e.getAngle().getLowMin();

			e.getAngle().setHighMax(angle + firstHighMax);
			e.getAngle().setHighMin(angle + firstHighMin);
			e.getAngle().setLowMax(angle + firstLowMax);
			e.getAngle().setLowMin(angle + firstLowMin);
		}
	}
	
	/**
	 * Rotates the supplied particle effect by setting
	 * the angle value of all emitters to the supplied value.
	 * 
	 * The first emitter is rotated to the supplied value
	 * and the others are then rotated relatively to the first one.
	 * 
	 * @param pe
	 * @param angle the new angle, in degrees
	 */
	public static void rotate(ParticleEffect pe, float angle) {
		boolean first = true;
		float firstHighMax = 0, firstHighMin = 0, firstLowMax = 0, firstLowMin = 0;
		for (ParticleEmitter e : pe.getEmitters()) {
			if (first) {
				firstHighMin = e.getAngle().getHighMin();
				firstHighMax = e.getAngle().getHighMax();
				firstLowMax = e.getAngle().getLowMax();
				firstLowMin = e.getAngle().getLowMin();
				
				e.getAngle().setHighMax(angle); 
			    e.getAngle().setHighMin(angle - (firstHighMax-firstHighMin)); 
			    e.getAngle().setLowMax(angle - (firstHighMax-firstLowMax));
			    e.getAngle().setLowMin(angle - (firstHighMax-firstLowMin));
			   
			    first = false;
			} else {
				float oldAngle = e.getAngle().getHighMax();
				e.getAngle().setHighMax(angle - (firstHighMax-oldAngle)); 
				oldAngle = e.getAngle().getHighMin();
			    e.getAngle().setHighMin(angle - (firstHighMin-oldAngle));
			    oldAngle = e.getAngle().getLowMax();
			    e.getAngle().setLowMax(angle - (firstLowMax-oldAngle));
			    oldAngle = e.getAngle().getLowMin();
			    e.getAngle().setLowMin(angle - (firstLowMin-oldAngle));
			}
		}
	}
	
	
	/**
	 * Widens the emission angle of the supplied emitters
	 * by the supplied coefficient. 
	 * 
	 * @param pe
	 * @param coef
	 */
	public static void widen(Array<ParticleEmitter> emitters, float coef) {
		for (ParticleEmitter e : emitters) {
			float highMin = e.getAngle().getHighMin();
			float highMax = e.getAngle().getHighMax();
			
			float oldDist = (highMax - highMin);
			float newDist =  oldDist * coef;
			float distDif = newDist - oldDist;
			
			e.getAngle().setHighMax(highMax + distDif / 2); 
		    e.getAngle().setHighMin(highMin - distDif / 2); 
			
			float lowMin = e.getAngle().getLowMin();
			float lowMax = e.getAngle().getLowMax();
			
			oldDist = (lowMax - lowMin);
			newDist =  oldDist * coef;
			distDif = newDist - oldDist;
		    
		    e.getAngle().setLowMax(lowMax + distDif / 2);
		    e.getAngle().setLowMin(lowMin - distDif / 2);
		}
	}
	
	/**
	 * Changes the particle velocity of the supplied emitters
	 * by multiplying it by the supplied coefficient. 
	 * 
	 * @param pe
	 * @param coef
	 */
	public static void slow(Array<ParticleEmitter> emitters, float coef) {
		for (ParticleEmitter e : emitters) {
			float velocityHighMin = e.getVelocity().getHighMin();
			float velocityHighMax = e.getVelocity().getHighMax();
			float velocityLowMin = e.getVelocity().getLowMin();
			float velocityLowMax = e.getVelocity().getLowMax();
			
			e.getVelocity().setHighMax(velocityHighMax * coef); 
		    e.getVelocity().setHighMin(velocityHighMin * coef); 
			e.getVelocity().setLowMax(velocityLowMax * coef);
		    e.getVelocity().setLowMin(velocityLowMin * coef);
		}
	}
	
	/**
	 * Returns only the specified number of TextureRegions from the supplied
	 * array of TextureRegions.
	 * 
	 * For example, supplying an array of 10 frames and parameters 3 for a
	 * starting frame and 4 for number of frames will return an array containing
	 * frames 3, 4, 5, 6.
	 * 
	 * @param animationFrames
	 *            frames from which to get the TextureRegions
	 * @param startingFrame
	 *            the frame from which the return value should start (indexed
	 *            from 0)
	 * @param numberOfFrames
	 *            number of frames we should return
	 * @return
	 */
	public static TextureRegion[] getFrames(TextureRegion[] animationFrames, int startingFrame, int numberOfFrames) {
		if (numberOfFrames < 1) {
			return null;
		}
		if (numberOfFrames+startingFrame > animationFrames.length) {
			numberOfFrames = animationFrames.length-startingFrame;
		}
		TextureRegion[] returnValue = new TextureRegion[numberOfFrames];
		for (int i = 0; i < numberOfFrames; ++i) {
			returnValue[i] = animationFrames[i+startingFrame];
		}
		return returnValue;
	}
	
	/**
	 * Takes a table of frames and transforms it into a single row of frames.
	 * 
	 * Assumes the table contains the frames in the standard left to right, top
	 * to bottom order.
	 * 
	 * @param animationFrames
	 * @return
	 */
	public static TextureRegion[] flattenFrames(TextureRegion[][] animationFrames) {
		int numberOfFrames = animationFrames.length;
		if (numberOfFrames < 1) {
			throw new GdxRuntimeException(
					"No frames found in supplied texture.");
		}
		if (numberOfFrames > 0) {
			numberOfFrames = numberOfFrames * animationFrames[0].length;
		}
		TextureRegion[] flattenedFrames = new TextureRegion[numberOfFrames];
		for (int i = 0, k = 0; i < animationFrames.length; ++i) {
			TextureRegion[] animationFramesColumns = animationFrames[i];
			for (int j = 0; j < animationFramesColumns.length; ++j, ++k) {
				flattenedFrames[k] = animationFramesColumns[j];
			}
		}
		return flattenedFrames;
	}
	
	
	public static int renderLoadingIndicator(float stateTime, int numberOfDots, SpriteBatch batch, BitmapFont font, String text) {
		if (stateTime > 0.3f) {
			++numberOfDots;
			if (numberOfDots > 3) {
				numberOfDots = 0; 
			}
		}
		
		StringBuilder fsb = StringUtil.getFSB();
		fsb.append(text);
		for (int i=0; i <numberOfDots; ++i) {
			fsb.append(".");
		}
		
		batch.begin();
		font.draw(batch, fsb.toString(), Gdx.graphics.getWidth()/2-50,  Gdx.graphics.getHeight()/2+30); 
		batch.end();
		StringUtil.freeFSB(fsb);
		return numberOfDots;
	}
}
