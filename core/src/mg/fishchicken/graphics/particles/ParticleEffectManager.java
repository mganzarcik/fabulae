package mg.fishchicken.graphics.particles;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.GraphicsUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.graphics.Drawable;
import mg.fishchicken.graphics.renderers.GameMapRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class ParticleEffectManager {
	
	private static ObjectMap<String, String> particleEffects = new ObjectMap<String, String>();
	
	private Array<ParticleEffectDrawable> keysToRemove = new Array<ParticleEffectDrawable>();
	private Array<ParticleEffectDrawable> activeEffects = new Array<ParticleEffectDrawable>();
	
	/**
	 * Returns a new, fresh copy of a particle effect with the specified id.
	 * 
	 * @param id
	 * @return
	 */
	public static ParticleEffect getParticleEffect(String id) {
		ParticleEffect effect = Assets.get(particleEffects.get(id.toLowerCase(Locale.ENGLISH)), ParticleEffect.class);
		return new ParticleEffect(effect);
	}
	
	/**
	 * Gathers all ParticleEffects and registers them in the AssetManager
	 * so that they can be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherParticleEffects() throws IOException {
		Assets.gatherAssets(Configuration.getFolderParticles(), "p", ParticleEffect.class, particleEffects);
	}
	
	/**
	 * Creates a new particle effect instance for the specified particle effect descriptor, attaches it
	 * to the supplied game object and starts it (after delay, if any is specified in the descriptor). The
	 * effect will update its position according to the GOs position. 
	 * 
	 * If the supplied GO is a Drawable, the effect will only be drawn if the GO
	 * can be drawn.
	 *  
	 * @param go
	 * @param pe
	 * @param delay
	 *            - the delay in seconds
	 * @param xOffset
	 * @param yOffset
	 * @return
	 */
	public ParticleEffect attachParticleEffect(GameObject go, ParticleEffectDescriptor pe) {
		if (pe == null) {
			return null;
		}
		ParticleEffect effect = getParticleEffect(pe.getEffectId());
		new ParticleEffectDrawable(pe.getEffectId(), effect, go, GraphicsUtil.transformOffsets(go.getMap(), new Vector2(pe.getXOffset(), pe.getYOffset())), pe.getDelay());
		return effect;
	}
	
	
	/**
	 * Updates the positions of all managed effects according
	 * to the GOs they are attached to.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime) {
		keysToRemove.clear();
		
		for (ParticleEffectDrawable info : activeEffects) {
			if (info.update(deltaTime)) {
				keysToRemove.add(info);
			}
		}
		
		for (ParticleEffectDrawable info : keysToRemove) {
			info.remove();
		}
	}

	/**
	 * Returns the number of active particle effects with the supplied id
	 * that the supplied GO has attached;
	 * @param go
	 * @param effectId
	 */
	public int getCount(GameObject go, String effectId) {
		int returnValue = 0;
		for (ParticleEffectDrawable info : activeEffects) {
			if (info.effectId.equals(effectId) && info.gameObject.equals(go)) {
				++returnValue;
			}
		}
		return returnValue;
	}
	
	/**
	 * Kills and removes all effects with the supplied id on the supplied GO.
	 * 
	 * @param pe
	 */
	public void kill(GameObject go, String effectId) {
		keysToRemove.clear();
		for (ParticleEffectDrawable info : activeEffects) {
			if (info.effectId.equals(effectId) && info.gameObject.equals(go)) {
				keysToRemove.add(info);
			}
		}
		
		for (ParticleEffectDrawable info : keysToRemove) {
			info.remove();
		}
	}
	
	/**
	 * Kills and removes all currently managed particle effects.
	 * 
	 */
	public void killAll() {
		keysToRemove.clear();
		keysToRemove.addAll(activeEffects);
		for (ParticleEffectDrawable info : keysToRemove) {
			info.remove();
		}
	}
	
	private class ParticleEffectDrawable implements Drawable  {
		private String effectId;
		private GameObject gameObject;
		private ParticleEffect effect;
		private Vector2 offset;
		private float delay;
		private boolean isStarted;
		private GameMap map;
		
		private ParticleEffectDrawable(String id, ParticleEffect effect, GameObject target, Vector2 offset, float delay) {
			effectId = id;
			this.effect = effect;
			this.gameObject = target;
			this.offset = offset;
			isStarted = false;
			this.delay = delay;
			this.map = gameObject.getMap();
			map.addDrawable(this);
			activeEffects.add(this);
			updatePosition();
		}
		
		private void remove() {
			activeEffects.removeValue(this, false);
			map.removeDrawable(this);
		}
		
		/**
		 * Updates this effect drawable. Returns true if it is finished now and
		 * should be removed.
		 * @param deltaTime
		 * @return
		 */
		private boolean update(float deltaTime) {
			if (!isStarted) {
				delay -= deltaTime;
				if (delay <= 0) {
					effect.start();
					isStarted = true;
				}
			}
			if (isStarted) {
				updatePosition();
				if (effect.isComplete() || !gameObject.isActive()) {
					return true;
				}
			}
			return false;
		}
		
		private void updatePosition() {
			Vector2 projectedCoordinates = gameObject.position().setVector2(MathUtil.getVector2());
			map.projectFromTiles(projectedCoordinates.add(offset.x, offset.y));
			effect.setPosition(projectedCoordinates.x ,
					projectedCoordinates.y);
			MathUtil.freeVector2(projectedCoordinates);
		}
		
		@Override
		public void draw(GameMapRenderer renderer, float deltaTime) {
			if (!GameState.isPaused()) {
				effect.draw(renderer.getSpriteBatch(), deltaTime);
			} else {
				effect.draw(renderer.getSpriteBatch());
			}
		}

		@Override
		public float getZIndex() {
			return -gameObject.getYCamera()+0.001f; // make sure we are always drawn on top of our GO
		}

		@Override
		public boolean shouldDraw(Rectangle cullingRectangle) {
			if (!isStarted
					|| (gameObject instanceof Drawable && !((Drawable) gameObject)
							.shouldDraw(cullingRectangle))) {
				return false;
			}
			return true;
		}

		@Override
		public boolean isAlwaysBehind() {
			return false;
		}

		@Override
		public boolean isAlwaysInFront() {
			return false;
		}

		@Override
		public Color getColor() {
			return Color.WHITE;
		}
		
		@Override
		public Color getHighlightColor(float x, float y) {
			return null;
		}
		
		@Override
		public int getHighlightAmount(float x, float y) {
			return 0;
		}

		@Override
		public Position position() {
			return gameObject.position();
		}

		@Override
		public GameMap getMap() {
			return map;
		}

		@Override
		public float getWidth() {
			return gameObject.getWidth();
		}

		@Override
		public float getHeight() {
			return gameObject.getHeight();
		}
	}
}
