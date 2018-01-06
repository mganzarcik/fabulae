package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import mg.fishchicken.gamelogic.locations.GameMap;

public class WeatherParticleEffect implements Disposable {

	private final Array<WeatherParticleEmitter> emitters;
	private boolean ownsTexture;
	protected float xSizeScale = 1f;
	protected float ySizeScale = 1f;
	protected float motionScale = 1f;
	
	public WeatherParticleEffect (ParticleEffect effect) {
		emitters = new Array<>(8);
		setEmittersFrom(effect);
	}

	public void setEmittersFrom(ParticleEffect effect) {
		Array<ParticleEmitter> hisEmitters = effect.getEmitters();
		for (int i = 0, n = hisEmitters.size; i < n; i++)
			emitters.add(new WeatherParticleEmitter(hisEmitters.get(i)));
	}
	
	public void start () {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).start();
	}

	/** Resets the effect so it can be started again like a new effect. Any changes to 
	 * scale are reverted. See {@link #reset(boolean)}.*/
	public void reset () {
		reset(true);
	}
	
	/** Resets the effect so it can be started again like a new effect.
	 * @param resetScaling Whether to restore the original size and motion parameters if they were scaled. Repeated scaling
	 * and resetting may introduce error. */
	public void reset (boolean resetScaling){
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).reset();
		if (resetScaling && (xSizeScale != 1f || ySizeScale != 1f || motionScale != 1f)){
			scaleEffect(1f / xSizeScale, 1f / ySizeScale, 1f / motionScale);
			xSizeScale = ySizeScale = motionScale = 1f;
		}
	}

	public void update (float delta) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).update(delta);
	}
	
	public void draw (GameMap map, Batch spriteBatch) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).draw(map, spriteBatch);
	}

	public void draw (GameMap map, Batch spriteBatch, float delta) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).draw(map, spriteBatch, delta);
	}
	
	public void setPosition (float x, float y) {
		for (int i = 0, n = emitters.size; i < n; i++)
			emitters.get(i).setPosition(x, y);
	}
	
	public Array<WeatherParticleEmitter> getEmitters () {
		return emitters;
	}

	/** Disposes the texture for each sprite for each ParticleEmitter. */
	public void dispose () {
		if (!ownsTexture) return;
		for (int i = 0, n = emitters.size; i < n; i++) {
			WeatherParticleEmitter emitter = emitters.get(i);
			for (Sprite sprite : emitter.getSprites()) {
				sprite.getTexture().dispose();
			}
		}
	}
	
	/** Permanently scales all the size and motion parameters of all the emitters in this effect. If this effect originated from a
	 * {@link ParticleEffectPool}, the scale will be reset when it is returned to the pool. */
	public void scaleEffect (float xSizeScaleFactor, float ySizeScaleFactor, float motionScaleFactor) {
		xSizeScale *= xSizeScaleFactor;
		ySizeScale *= ySizeScaleFactor;
		motionScale *= motionScaleFactor;
		for (WeatherParticleEmitter particleEmitter : emitters) {
			particleEmitter.scaleSize(xSizeScaleFactor, ySizeScaleFactor);
			particleEmitter.scaleMotion(motionScaleFactor);
		}
	}
}
