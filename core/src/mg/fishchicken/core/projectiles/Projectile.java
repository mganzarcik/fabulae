package mg.fishchicken.core.projectiles;

import mg.fishchicken.audio.Sound;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.projectiles.ProjectileType.ScalingMethod;
import mg.fishchicken.core.util.GraphicsUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.graphics.animations.AnimatedGameObject;
import mg.fishchicken.graphics.lights.GamePointLight;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.graphics.particles.ParticleEffectManager;
import mg.fishchicken.graphics.renderers.GameMapRenderer;
import mg.fishchicken.tweening.LightTweenAccessor;
import mg.fishchicken.tweening.PositionedThingTweenAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Projectile extends AnimatedGameObject implements TweenCallback, AssetContainer {

	public static final String EXPLOSION_EMITTERS = "explosion";
	public static final String CREATION_EMITTERS = "creation";
	public static final String PROJECTILE_EMITTERS = "projectile";
	
	private GameObject originator;
	private ProjectileTarget target;
	private OnProjectileHitCallback callback;
	private Tween tween;
	private Orientation orientation;
	private float speed;
	private float moveDelay;
	private GamePointLight light;
	private ProjectileType s_type;
	private Vector2 endOffset;
	private ParticleEffect particleEffect;
	private boolean lightTweenSet;
	private float explosionDuration;
	private boolean hit;
	private Sound onHitSound, onStartSound, duringSound;
	private boolean fixedOnOrigin;
	private boolean moveStarted;

	/**
	 * Creates a new projectile of the supplied type and sends it on its way
	 * from the originator to the target. The callback is called once the target
	 * is hit.
	 * 
	 * @param type
	 * @param originator
	 * @param target
	 * @param callback
	 */
	public Projectile(ProjectileType type, GameObject originator,
			ProjectileTarget target, OnProjectileHitCallback callback) {
		super(type.getId(), type.getId());
		this.hit = false;
		this.originator = originator;
		this.target = target;
		this.callback = callback;
		this.s_type = type;
		this.speed = type.s_speed;
		lightTweenSet = false;
		this.fixedOnOrigin = type.s_fixedOnOrigin;
		moveStarted = false;
		
		Vector2 startOffset = GraphicsUtil.transformOffsets(originator.getMap(), MathUtil.getVector2().set(originator.getProjectileOriginXOffset(), originator.getProjectileOriginYOffset()));

		Position originatorPosition = originator.position();
		
		this.orientation = Orientation.calculateOrientationToTarget(
				originator.getMap().isIsometric(), originatorPosition.getX(), originatorPosition.getY(),
				target.getTargetX(), target.getTargetY());
		if (this.orientation == null) {
			this.orientation = Orientation.UP;
		}
		position().set(originatorPosition.getX()+startOffset.x, originatorPosition.getY()+startOffset.y);
		MathUtil.freeVector2(startOffset);
		setMap(originator.getMap());
	
		if (type.getParticleEffect() != null) {
			particleEffect = ParticleEffectManager.getParticleEffect(type
					.getParticleEffect());
		}
		if (type.animations != null) {
			setAnimation(type.animations.getAnimation(orientation, true));
		}

		endOffset =  GraphicsUtil.transformOffsets(originator.getMap(), new Vector2(type.s_xOffsetEnd, type.s_yOffsetEnd));
		
		Vector2 tempVector = MathUtil.getVector2().set(target.getTargetX()+endOffset.x,
				target.getTargetY()+endOffset.y);
		float distance = tempVector.dst(position.getX(), position.getY());
		MathUtil.freeVector2(tempVector);
		
		setupParticleEffect(distance, type.s_scalingMethod);
		
		if (type.onHitSounds.size > 0) {
			onHitSound = new Sound(type.onHitSounds.random());
			onHitSound.setAudioOriginator(this);
		}
		if (type.onStartSounds.size > 0) {
			onStartSound = new Sound(type.onStartSounds.random());
			onStartSound.setAudioOriginator(this);
			onStartSound.play();
		}
		if (type.duringSounds.size > 0) {
			duringSound = new Sound(type.duringSounds.random());
			duringSound.setAudioOriginator(this);
		}
		
		if (speed > 0) {
			tween = Tween
					.to(this, PositionedThingTweenAccessor.XY, distance / speed)
					.ease(TweenEquations.easeInSine)
					.target(target.getTargetX()+endOffset.x, target.getTargetY()+endOffset.y)
					.setCallback(this)
					.setCallbackTriggers(TweenCallback.COMPLETE);
			
		}
	}

	private void setupParticleEffect(float distance, ScalingMethod scalingMethod) {
		if (particleEffect == null) {
			return;
		}		
		
		Array<ParticleEmitter> creationEmitters = GraphicsUtil
				.getEmittersByName(particleEffect, CREATION_EMITTERS);
		
		moveDelay = 0;
		for (ParticleEmitter pe : creationEmitters) {
			float duration = pe.getDuration().getLowMin() / 1000f;
			if (moveDelay < duration) {
				moveDelay = duration;
			}
		}

		Array<ParticleEmitter> explosionEmitters = GraphicsUtil
				.getEmittersByName(particleEffect, EXPLOSION_EMITTERS);

		GraphicsUtil.scale(explosionEmitters, target.getSize(), getMap().isIsometric(), scalingMethod == ScalingMethod.PARTICLES);
	
		if (getMap().isIsometric()) {
			if (fixedOnOrigin) {
				if (orientation == Orientation.LEFT || orientation == Orientation.RIGHT) {
					GraphicsUtil.widen(explosionEmitters, 0.66f);
					GraphicsUtil.slow(explosionEmitters, 1.33f);
				} else if (orientation == Orientation.UP || orientation == Orientation.DOWN) {
					GraphicsUtil.widen(explosionEmitters, 1.33f);
					GraphicsUtil.slow(explosionEmitters, 0.75f);
				}  
			}
		}

		Array<ParticleEmitter> projectileEmitters = GraphicsUtil
				.getEmittersByName(particleEffect, PROJECTILE_EMITTERS);
		
		GraphicsUtil.rotateBy(projectileEmitters,
				-(orientation.getDegrees() + 90));
		 
		if (fixedOnOrigin) {
			GraphicsUtil.rotateBy(explosionEmitters,
					-(orientation.getDegrees() + 90));
			
		}
		
		int duration = speed > 0 ? (int) ((distance / speed) * 1000) : 0;
		for (ParticleEmitter pe : projectileEmitters) {
			pe.getDuration().setLow(duration);
		}

		explosionDuration = 0;
		for (ParticleEmitter pe : explosionEmitters) {
			if (duration >= 0) {
				pe.getDelay().setLow(duration+(moveDelay*1000));
			}
			if (explosionDuration < pe.getDuration().getLowMin()) {
				explosionDuration = pe.getDuration().getLowMin();
			}
		}

		particleEffect.start();
	}

	@Override
	public void setMap(GameMap map) {
		super.setMap(map);
		createNonCastingLight(s_type.s_light);
	}

	private void createNonCastingLight(LightDescriptor lightDescriptor) {
		if (lightDescriptor == null) {
			return;
		}
		light = new GamePointLight(getMap().getLightsRayHandler(), 16,
				lightDescriptor.lightColor, lightDescriptor.lightRadius, this);
		light.setXray(true);
		light.setStaticLight(false);
		light.setSoft(true);
		light.setSoftnessLenght(3);
		updateLightPosition();
	}

	public void updateLightPosition() {
		if (light != null) {
			Vector2 tempVector = MathUtil.getVector2().set(position.getX(), position.getY());
			getMap().projectFromTiles(tempVector);
			light.setPosition(tempVector.x, tempVector.y);
			MathUtil.freeVector2(tempVector);
		}
	}

	@Override
	public void draw(GameMapRenderer renderer, float deltaTime) {
		if (getAnimation() != null) {
			super.draw(renderer, deltaTime);
		} 
		if (particleEffect != null) {
			particleEffect.draw(renderer.getSpriteBatch());
		}
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (!moveStarted)  {
			moveDelay -= deltaTime;
			if (moveDelay <= 0) {
				moveStarted = true;
				if (duringSound != null) {
					duringSound.play();
				}
				if (tween != null) {
					tween.start();
				} else {
					if (!fixedOnOrigin) {
						position.set(target.getTargetX()+endOffset.x, target.getTargetY()+endOffset.y);
					}
					processHit();
				}
			}
		} else {
			// projectiles follow their target if they are moving
			// so update their position
			if (tween != null) {
				tween.target(target.getTargetX()+endOffset.x, target.getTargetY()+endOffset.y).update(deltaTime);
			} else if (!fixedOnOrigin) {
				position.set(target.getTargetX()+endOffset.x, target.getTargetY()+endOffset.y); 
			}
		}
		
		if (onHitSound != null) {
			onHitSound.update(deltaTime);
		}
		
		if (onStartSound != null) {
			onStartSound.update(deltaTime);
		}
		
		if (duringSound != null) {
			duringSound.update(deltaTime);
		}
		
		if (particleEffect != null) {
			Vector2 projectedCoordinates = MathUtil.getVector2();
			getMap().projectFromTiles(projectedCoordinates.set(position.getX(), position.getY()));
			particleEffect.setPosition(projectedCoordinates.x,
					projectedCoordinates.y);
			MathUtil.freeVector2(projectedCoordinates);
			particleEffect.update(deltaTime);
		}
		
		updateLightPosition();

		if (hit) {
			if (particleEffect != null && !particleEffect.isComplete()) {
				if (light != null && !lightTweenSet) {
					lightTweenSet = true;
					float duration = explosionDuration/1000f;
					Timeline.createSequence()
						.beginSequence()
							.push(Tween
									.to(light, LightTweenAccessor.DISTANCE, duration*(22f/20f))
									.ease(TweenEquations.easeOutSine)
									.target(target.getSize()*2))
							.push(Tween
									.to(light, LightTweenAccessor.DISTANCE, duration*(3f/20f))
									.ease(TweenEquations.easeOutSine)
									.target(0))
							.setCallback(this)
						.end().start(getMap().getTweenManager(false));
					light.setColor(Color.WHITE);
					light.setMaxIntensity(Color.WHITE);
				}
			}
			if (particleEffect == null || particleEffect.isComplete()) {
				kill();
			}
		}
	}
	
	/**
	 * Kills this Projectile, removing it from the map
	 * immediately and releasing all associated resources.
	 */
	public void kill() {
		getMap().removeGameObject(this);
		releaseResources();
	}

	public String getTypeId() {
		return s_type.getId();
	}

	/**
	 * Projectiles never contain anything.
	 */
	@Override
	public boolean contains(float x, float y) {
		return false;
	}

	@Override
	public void onHit(Projectile projectile, GameObject user) {
		return;
	}

	private void releaseResources() {
		super.setAnimation(null);
		MathUtil.freeVector2(endOffset);
		if (light != null) {
			light.remove();
			light = null;
		}
		particleEffect = null;
	}

	@Override
	public void onEvent(int type, BaseTween<?> source) {
		// the check for hit has to be here, since for some reason
		// tweens sometimes call the COMPLETE event twice 
		// (may need to investigate this in detail later)
		if (type == TweenCallback.COMPLETE && !hit) {
			processHit();
		}
	}
	
	private void processHit() {
		hit = true;
		callback.onProjectileHit(this, originator, target);
		if (duringSound != null) {
			duringSound.stopTrack();
		}
		if (onHitSound != null) {
			onHitSound.play(this);
		}
		if (tween != null) {
			tween.free();
			tween = null;
		}
	}

	@Override
	public boolean shouldModifyVolume() {
		return false;
	}
	
	/**
	 * Projectiles are not saved.
	 */
	@Override
	public boolean shouldBeSaved() {
		return false;
	}
	
	@Override
	public boolean isAlwaysInFront() {
		return true;
	}
	
	@Override
	public void clearAssetReferences() {
		light = null;
	}
	
	@Override
	public void undispose() {
		if (getMap() != null) {
			createNonCastingLight(s_type.s_light);
		}
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
	}
}
