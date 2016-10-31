package com.badlogic.gdx.graphics.g2d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public class WeatherParticleEffect extends ParticleEffect {

	public WeatherParticleEffect (ParticleEffect effect) {
		super();
		Array<ParticleEmitter> myEmitters = getEmitters();
		Array<ParticleEmitter> hisEmitters = effect.getEmitters();
		for (int i = 0, n = hisEmitters.size; i < n; i++)
			myEmitters.add(new WeatherParticleEmitter(hisEmitters.get(i)));
	}
	
	public void draw (GameMap map, Batch spriteBatch) {
		Array<ParticleEmitter> myEmitters = getEmitters();
		for (int i = 0, n = myEmitters.size; i < n; i++)
			((WeatherParticleEmitter)myEmitters.get(i)).draw(map, spriteBatch);
	}

	public void draw (GameMap map, Batch spriteBatch, float delta) {
		Array<ParticleEmitter> myEmitters = getEmitters();
		for (int i = 0, n = myEmitters.size; i < n; i++)
			((WeatherParticleEmitter)myEmitters.get(i)).draw(map, spriteBatch, delta);
	}
	
	public void setEmittersFrom(ParticleEffect effect) {
		Array<ParticleEmitter> myEmitters = getEmitters();
		Array<ParticleEmitter> hisEmitters = effect.getEmitters();
		for (int i = 0, n = hisEmitters.size, m = myEmitters.size; i < n && i < m; i++) {
			((WeatherParticleEmitter)myEmitters.get(i)).setFrom(hisEmitters.get(i));
		}
	}
	
	public void loadEmitters (FileHandle effectFile) {
		InputStream input = effectFile.read();
		getEmitters().clear();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")), 512);
			while (true) {
				WeatherParticleEmitter emitter = new WeatherParticleEmitter(reader);
				reader.readLine();
				emitter.setImagePath(reader.readLine());
				getEmitters().add(emitter);
				if (reader.readLine() == null) break;
				if (reader.readLine() == null) break;
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error loading effect: " + effectFile, ex);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}
}
