package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.GradientColorValue;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.Particle;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.RangedNumericValue;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ScaledNumericValue;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnShapeValue;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpriteMode;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;

public class WeatherParticleEmitter {
	static private final int UPDATE_SCALE = 1 << 0;
	static private final int UPDATE_ANGLE = 1 << 1;
	static private final int UPDATE_ROTATION = 1 << 2;
	static private final int UPDATE_VELOCITY = 1 << 3;
	static private final int UPDATE_WIND = 1 << 4;
	static private final int UPDATE_GRAVITY = 1 << 5;
	static private final int UPDATE_TINT = 1 << 6;
	static private final int UPDATE_SPRITE = 1 << 7;

	private RangedNumericValue delayValue = new RangedNumericValue();
	private ScaledNumericValue lifeOffsetValue = new ScaledNumericValue();
	private RangedNumericValue durationValue = new RangedNumericValue();
	private ScaledNumericValue lifeValue = new ScaledNumericValue();
	private ScaledNumericValue emissionValue = new ScaledNumericValue();
	private ScaledNumericValue xScaleValue = new ScaledNumericValue();
	private ScaledNumericValue yScaleValue = new ScaledNumericValue();
	private ScaledNumericValue rotationValue = new ScaledNumericValue();
	private ScaledNumericValue velocityValue = new ScaledNumericValue();
	private ScaledNumericValue angleValue = new ScaledNumericValue();
	private ScaledNumericValue windValue = new ScaledNumericValue();
	private ScaledNumericValue gravityValue = new ScaledNumericValue();
	private ScaledNumericValue transparencyValue = new ScaledNumericValue();
	private GradientColorValue tintValue = new GradientColorValue();
	private RangedNumericValue xOffsetValue = new ScaledNumericValue();
	private RangedNumericValue yOffsetValue = new ScaledNumericValue();
	private ScaledNumericValue spawnWidthValue = new ScaledNumericValue();
	private ScaledNumericValue spawnHeightValue = new ScaledNumericValue();
	private SpawnShapeValue spawnShapeValue = new SpawnShapeValue();
	
	private RangedNumericValue[] xSizeValues;
	private RangedNumericValue[] ySizeValues;
	private RangedNumericValue[] motionValues;

	private float accumulator;
	private Array<Sprite> sprites;
	private SpriteMode spriteMode = SpriteMode.single;
	private Particle[] particles;
	private int minParticleCount, maxParticleCount = 4;
	private float x, y;
	private Array<String> imagePaths;
	private int activeCount;
	private boolean[] active;
	private boolean firstUpdate;
	private boolean flipX, flipY;
	private int updateFlags;

	private int emission, emissionDiff, emissionDelta;
	private int lifeOffset, lifeOffsetDiff;
	private int life, lifeDiff;
	private float spawnWidth, spawnWidthDiff;
	private float spawnHeight, spawnHeightDiff;
	private float duration = 1, durationTimer;
	private float delay, delayTimer;

	private boolean attached;
	private boolean aligned;
	private boolean additive = true;
	private Vector2 tempVector = MathUtil.getVector2();

	public WeatherParticleEmitter(ParticleEmitter emitter) {
		setFrom(emitter);
	}

	public void setFrom(ParticleEmitter emitter) {
		sprites = new Array<Sprite>(emitter.getSprites());
		imagePaths = new Array<String>(emitter.getImagePaths());
		setMaxParticleCount(emitter.getMaxParticleCount());
		minParticleCount = emitter.getMinParticleCount();
		delayValue.load(emitter.getDelay());
		durationValue.load(emitter.getDuration());
		emissionValue.load(emitter.getEmission());
		lifeValue.load(emitter.getLife());
		lifeOffsetValue.load(emitter.getLifeOffset());
		xScaleValue.load(emitter.getXScale());
		yScaleValue.load(emitter.getYScale());
		rotationValue.load(emitter.getRotation());
		velocityValue.load(emitter.getVelocity());
		angleValue.load(emitter.getAngle());
		windValue.load(emitter.getWind());
		gravityValue.load(emitter.getGravity());
		transparencyValue.load(emitter.getTransparency());
		tintValue.load(emitter.getTint());
		xOffsetValue.load(emitter.getXOffsetValue());
		yOffsetValue.load(emitter.getYOffsetValue());
		spawnWidthValue.load(emitter.getSpawnWidth());
		spawnHeightValue.load(emitter.getSpawnHeight());
		spawnShapeValue.load(emitter.getSpawnShape());
		attached = emitter.isAttached();
		aligned = emitter.isAligned();
		additive = emitter.isAdditive();
		spriteMode = emitter.getSpriteMode();
	}

	public void setMaxParticleCount(int maxParticleCount) {
		this.maxParticleCount = maxParticleCount;
		if (active == null || maxParticleCount > active.length) {
			active = new boolean[maxParticleCount];
			activeCount = 0;
			particles = new Particle[maxParticleCount];
		}
	}

	public void addParticle() {
		int activeCount = this.activeCount;
		if (activeCount == maxParticleCount)
			return;
		boolean[] active = this.active;
		for (int i = 0, n = maxParticleCount; i < n; i++) {
			if (!active[i]) {
				activateParticle(i);
				active[i] = true;
				this.activeCount = activeCount + 1;
				break;
			}
		}
	}

	public void addParticles(int count) {
		count = Math.min(count, maxParticleCount - activeCount);
		if (count == 0)
			return;
		boolean[] active = this.active;
		int index = 0, n = maxParticleCount;
		outer: for (int i = 0; i < count; i++) {
			for (; index < n; index++) {
				if (!active[index]) {
					activateParticle(index);
					active[index++] = true;
					continue outer;
				}
			}
			break;
		}
		this.activeCount += count;
	}

	public void update(float delta) {
		accumulator += delta * 1000;
		if (accumulator < 1)
			return;
		int deltaMillis = (int) accumulator;
		accumulator -= deltaMillis;

		if (delayTimer < delay) {
			delayTimer += deltaMillis;
		} else {
			if (firstUpdate) {
				firstUpdate = false;
				addParticle();
			}

			if (durationTimer < duration)
				durationTimer += deltaMillis;
			else {
				restart();
			}

			emissionDelta += deltaMillis;
			float emissionTime = emission + emissionDiff * emissionValue.getScale(durationTimer / (float) duration);
			if (emissionTime > 0) {
				emissionTime = 1000 / emissionTime;
				if (emissionDelta >= emissionTime) {
					int emitCount = (int) (emissionDelta / emissionTime);
					emitCount = Math.min(emitCount, maxParticleCount - activeCount);
					emissionDelta -= emitCount * emissionTime;
					emissionDelta %= emissionTime;
					addParticles(emitCount);
				}
			}
			if (activeCount < minParticleCount)
				addParticles(minParticleCount - activeCount);
		}

		boolean[] active = this.active;
		int activeCount = this.activeCount;
		Particle[] particles = this.particles;
		for (int i = 0, n = maxParticleCount; i < n; i++) {
			if (active[i] && !updateParticle(particles[i], delta, deltaMillis)) {
				active[i] = false;
				activeCount--;
			}
		}
		this.activeCount = activeCount;
	}

	public void draw(GameMap map, Batch batch) {
		if (map == null) {
			return;
		}

		if (additive)
			batch.setBlendFunction(GL30.GL_SRC_ALPHA, GL30.GL_ONE);

		Particle[] particles = this.particles;
		boolean[] active = this.active;
		Vector2 tempVector = MathUtil.getVector2();

		for (int i = 0, n = maxParticleCount; i < n; i++)
			if (active[i]) {
				tempVector.set(particles[i].getX() + particles[i].getOriginX(),
						particles[i].getY() + particles[i].getOriginY());
				map.projectToTiles(tempVector);
				if (map.shouldRenderTile((int) tempVector.x, (int) tempVector.y)) {
					particles[i].draw(batch);
				}

			}
		MathUtil.freeVector2(tempVector);
		if (additive)
			batch.setBlendFunction(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
	}

	/**
	 * Updates and draws the particles. This is slightly more efficient than
	 * calling {@link #update(float)} and {@link #draw(Batch)} separately.
	 */
	public void draw(GameMap map, Batch batch, float delta) {
		accumulator += delta * 1000;
		if (accumulator < 1) {
			draw(map, batch);
			return;
		}
		int deltaMillis = (int) accumulator;
		accumulator -= deltaMillis;

		if (additive)
			batch.setBlendFunction(GL30.GL_SRC_ALPHA, GL30.GL_ONE);

		Particle[] particles = this.particles;
		boolean[] active = this.active;
		int activeCount = this.activeCount;
		for (int i = 0, n = maxParticleCount; i < n; i++) {
			if (active[i]) {
				Particle particle = particles[i];
				if (updateParticle(particle, delta, deltaMillis)) {
					tempVector.set(particle.getX() + particle.getOriginX(), particle.getY() + particle.getOriginY());
					map.projectToTiles(tempVector);
					if (map.shouldRenderTile((int) tempVector.x, (int) tempVector.y)) {
						particle.draw(batch);
					}
				} else {
					active[i] = false;
					activeCount--;
				}
			}
		}
		this.activeCount = activeCount;

		if (additive)
			batch.setBlendFunction(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

		if (delayTimer < delay) {
			delayTimer += deltaMillis;
			return;
		}

		if (firstUpdate) {
			firstUpdate = false;
			addParticle();
		}

		if (durationTimer < duration)
			durationTimer += deltaMillis;
		else {
			restart();
		}

		emissionDelta += deltaMillis;
		float emissionTime = emission + emissionDiff * emissionValue.getScale(durationTimer / (float) duration);
		if (emissionTime > 0) {
			emissionTime = 1000 / emissionTime;
			if (emissionDelta >= emissionTime) {
				int emitCount = (int) (emissionDelta / emissionTime);
				emitCount = Math.min(emitCount, maxParticleCount - activeCount);
				emissionDelta -= emitCount * emissionTime;
				emissionDelta %= emissionTime;
				addParticles(emitCount);
			}
		}
		if (activeCount < minParticleCount)
			addParticles(minParticleCount - activeCount);
	}

	public void start() {
		firstUpdate = true;
		restart();
	}
	
	public void reset () {
		emissionDelta = 0;
		durationTimer = duration;
		boolean[] active = this.active;
		for (int i = 0, n = active.length; i < n; i++)
			active[i] = false;
		activeCount = 0;
		start();
	}

	private void restart() {
		delay = delayValue.active ? delayValue.newLowValue() : 0;
		delayTimer = 0;

		durationTimer -= duration;
		duration = durationValue.newLowValue();

		emission = (int) emissionValue.newLowValue();
		emissionDiff = (int) emissionValue.newHighValue();
		if (!emissionValue.isRelative())
			emissionDiff -= emission;

		life = (int) lifeValue.newLowValue();
		lifeDiff = (int) lifeValue.newHighValue();
		if (!lifeValue.isRelative())
			lifeDiff -= life;

		lifeOffset = lifeOffsetValue.active ? (int) lifeOffsetValue.newLowValue() : 0;
		lifeOffsetDiff = (int) lifeOffsetValue.newHighValue();
		if (!lifeOffsetValue.isRelative())
			lifeOffsetDiff -= lifeOffset;

		spawnWidth = spawnWidthValue.newLowValue();
		spawnWidthDiff = spawnWidthValue.newHighValue();
		if (!spawnWidthValue.isRelative())
			spawnWidthDiff -= spawnWidth;

		spawnHeight = spawnHeightValue.newLowValue();
		spawnHeightDiff = spawnHeightValue.newHighValue();
		if (!spawnHeightValue.isRelative())
			spawnHeightDiff -= spawnHeight;

		updateFlags = 0;
		if (angleValue.active && angleValue.timeline.length > 1)
			updateFlags |= UPDATE_ANGLE;
		if (velocityValue.active)
			updateFlags |= UPDATE_VELOCITY;
		if (xScaleValue.timeline.length > 1)
			updateFlags |= UPDATE_SCALE;
		if (yScaleValue.active && yScaleValue.timeline.length > 1)
			updateFlags |= UPDATE_SCALE;
		if (rotationValue.active && rotationValue.timeline.length > 1)
			updateFlags |= UPDATE_ROTATION;
		if (windValue.active)
			updateFlags |= UPDATE_WIND;
		if (gravityValue.active)
			updateFlags |= UPDATE_GRAVITY;
		if (tintValue.timeline.length > 1)
			updateFlags |= UPDATE_TINT;
		if (spriteMode == SpriteMode.animated)
			updateFlags |= UPDATE_SPRITE;
	}

	protected Particle newParticle(Sprite sprite) {
		return new Particle(sprite);
	}

	private void activateParticle(int index) {
		Sprite sprite = null;
		switch (spriteMode) {
		case single:
		case animated:
			sprite = sprites.first();
			break;
		case random:
			sprite = sprites.random();
			break;
		}

		Particle particle = particles[index];
		if (particle == null) {
			particles[index] = particle = newParticle(sprite);
			particle.flip(flipX, flipY);
		} else {
			particle.set(sprite);
		}

		float percent = durationTimer / (float) duration;
		int updateFlags = this.updateFlags;

		particle.currentLife = particle.life = life + (int) (lifeDiff * lifeValue.getScale(percent));

		if (velocityValue.active) {
			particle.velocity = velocityValue.newLowValue();
			particle.velocityDiff = velocityValue.newHighValue();
			if (!velocityValue.isRelative())
				particle.velocityDiff -= particle.velocity;
		}

		particle.angle = angleValue.newLowValue();
		particle.angleDiff = angleValue.newHighValue();
		if (!angleValue.isRelative())
			particle.angleDiff -= particle.angle;
		float angle = 0;
		if ((updateFlags & UPDATE_ANGLE) == 0) {
			angle = particle.angle + particle.angleDiff * angleValue.getScale(0);
			particle.angle = angle;
			particle.angleCos = MathUtils.cosDeg(angle);
			particle.angleSin = MathUtils.sinDeg(angle);
		}

		float spriteWidth = sprite.getWidth();
		float spriteHeight = sprite.getHeight();

		particle.xScale = xScaleValue.newLowValue() / spriteWidth;
		particle.xScaleDiff = xScaleValue.newHighValue() / spriteWidth;
		if (!xScaleValue.isRelative())
			particle.xScaleDiff -= particle.xScale;

		if (yScaleValue.active) {
			particle.yScale = yScaleValue.newLowValue() / spriteHeight;
			particle.yScaleDiff = yScaleValue.newHighValue() / spriteHeight;
			if (!yScaleValue.isRelative())
				particle.yScaleDiff -= particle.yScale;
			particle.setScale(particle.xScale + particle.xScaleDiff * xScaleValue.getScale(0),
					particle.yScale + particle.yScaleDiff * yScaleValue.getScale(0));
		} else {
			particle.setScale(particle.xScale + particle.xScaleDiff * xScaleValue.getScale(0));
		}

		if (rotationValue.active) {
			particle.rotation = rotationValue.newLowValue();
			particle.rotationDiff = rotationValue.newHighValue();
			if (!rotationValue.isRelative())
				particle.rotationDiff -= particle.rotation;
			float rotation = particle.rotation + particle.rotationDiff * rotationValue.getScale(0);
			if (aligned)
				rotation += angle;
			particle.setRotation(rotation);
		}

		if (windValue.active) {
			particle.wind = windValue.newLowValue();
			particle.windDiff = windValue.newHighValue();
			if (!windValue.isRelative())
				particle.windDiff -= particle.wind;
		}

		if (gravityValue.active) {
			particle.gravity = gravityValue.newLowValue();
			particle.gravityDiff = gravityValue.newHighValue();
			if (!gravityValue.isRelative())
				particle.gravityDiff -= particle.gravity;
		}

		float[] color = particle.tint;
		if (color == null)
			particle.tint = color = new float[3];
		float[] temp = tintValue.getColor(0);
		color[0] = temp[0];
		color[1] = temp[1];
		color[2] = temp[2];

		particle.transparency = transparencyValue.newLowValue();
		particle.transparencyDiff = transparencyValue.newHighValue() - particle.transparency;

		// Spawn.
		float x = this.x;
		if (xOffsetValue.active)
			x += xOffsetValue.newLowValue();
		float y = this.y;
		if (yOffsetValue.active)
			y += yOffsetValue.newLowValue();
		switch (spawnShapeValue.shape) {
		case square: {
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			x += MathUtils.random(width) - width / 2;
			y += MathUtils.random(height) - height / 2;
			break;
		}
		case ellipse: {
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			float radiusX = width / 2;
			float radiusY = height / 2;
			if (radiusX == 0 || radiusY == 0)
				break;
			float scaleY = radiusX / (float) radiusY;
			if (spawnShapeValue.edges) {
				float spawnAngle;
				switch (spawnShapeValue.side) {
				case top:
					spawnAngle = -MathUtils.random(179f);
					break;
				case bottom:
					spawnAngle = MathUtils.random(179f);
					break;
				default:
					spawnAngle = MathUtils.random(360f);
					break;
				}
				float cosDeg = MathUtils.cosDeg(spawnAngle);
				float sinDeg = MathUtils.sinDeg(spawnAngle);
				x += cosDeg * radiusX;
				y += sinDeg * radiusX / scaleY;
				if ((updateFlags & UPDATE_ANGLE) == 0) {
					particle.angle = spawnAngle;
					particle.angleCos = cosDeg;
					particle.angleSin = sinDeg;
				}
			} else {
				float radius2 = radiusX * radiusX;
				while (true) {
					float px = MathUtils.random(width) - radiusX;
					float py = MathUtils.random(height) - radiusY;
					if (px * px + py * py <= radius2) {
						x += px;
						y += py / scaleY;
						break;
					}
				}
			}
			break;
		}
		case line: {
			float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
			float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
			if (width != 0) {
				float lineX = width * MathUtils.random();
				x += lineX;
				y += lineX * (height / (float) width);
			} else
				y += height * MathUtils.random();
			break;
		}
		}

		particle.setBounds(x - spriteWidth / 2, y - spriteHeight / 2, spriteWidth, spriteHeight);

		int offsetTime = (int) (lifeOffset + lifeOffsetDiff * lifeOffsetValue.getScale(percent));
		if (offsetTime > 0) {
			if (offsetTime >= particle.currentLife)
				offsetTime = particle.currentLife - 1;
			updateParticle(particle, offsetTime / 1000f, offsetTime);
		}
	}

	private boolean updateParticle(Particle particle, float delta, int deltaMillis) {
		int life = particle.currentLife - deltaMillis;
		if (life <= 0)
			return false;
		particle.currentLife = life;

		float percent = 1 - particle.currentLife / (float) particle.life;
		int updateFlags = this.updateFlags;

		if ((updateFlags & UPDATE_SCALE) != 0) {
			if (yScaleValue.active) {
				particle.setScale(particle.xScale + particle.xScaleDiff * xScaleValue.getScale(percent),
						particle.yScale + particle.yScaleDiff * yScaleValue.getScale(percent));
			} else {
				particle.setScale(particle.xScale + particle.xScaleDiff * xScaleValue.getScale(percent));
			}
		}

		if ((updateFlags & UPDATE_VELOCITY) != 0) {
			float velocity = (particle.velocity + particle.velocityDiff * velocityValue.getScale(percent)) * delta;

			float velocityX, velocityY;
			if ((updateFlags & UPDATE_ANGLE) != 0) {
				float angle = particle.angle + particle.angleDiff * angleValue.getScale(percent);
				velocityX = velocity * MathUtils.cosDeg(angle);
				velocityY = velocity * MathUtils.sinDeg(angle);
				if ((updateFlags & UPDATE_ROTATION) != 0) {
					float rotation = particle.rotation + particle.rotationDiff * rotationValue.getScale(percent);
					if (aligned)
						rotation += angle;
					particle.setRotation(rotation);
				}
			} else {
				velocityX = velocity * particle.angleCos;
				velocityY = velocity * particle.angleSin;
				if (aligned || (updateFlags & UPDATE_ROTATION) != 0) {
					float rotation = particle.rotation + particle.rotationDiff * rotationValue.getScale(percent);
					if (aligned)
						rotation += particle.angle;
					particle.setRotation(rotation);
				}
			}

			if ((updateFlags & UPDATE_WIND) != 0)
				velocityX += (particle.wind + particle.windDiff * windValue.getScale(percent)) * delta;

			if ((updateFlags & UPDATE_GRAVITY) != 0)
				velocityY += (particle.gravity + particle.gravityDiff * gravityValue.getScale(percent)) * delta;

			particle.translate(velocityX, velocityY);
		} else {
			if ((updateFlags & UPDATE_ROTATION) != 0)
				particle.setRotation(particle.rotation + particle.rotationDiff * rotationValue.getScale(percent));
		}

		float[] color;
		if ((updateFlags & UPDATE_TINT) != 0)
			color = tintValue.getColor(percent);
		else
			color = particle.tint;
		particle.setColor(color[0], color[1], color[2],
				particle.transparency + particle.transparencyDiff * transparencyValue.getScale(percent));

		if ((updateFlags & UPDATE_SPRITE) != 0) {
			int frame = Math.min((int) (percent * sprites.size), sprites.size - 1);
			if (particle.frame != frame) {
				Sprite sprite = sprites.get(frame);
				float prevSpriteWidth = particle.getWidth();
				float prevSpriteHeight = particle.getHeight();
				particle.setRegion(sprite);
				particle.setSize(sprite.getWidth(), sprite.getHeight());
				particle.setOrigin(sprite.getOriginX(), sprite.getOriginY());
				particle.translate((prevSpriteWidth - sprite.getWidth()) / 2,
						(prevSpriteHeight - sprite.getHeight()) / 2);
				particle.frame = frame;
			}
		}

		return true;
	}
	
	public void setPosition (float x, float y) {
		if (attached) {
			float xAmount = x - this.x;
			float yAmount = y - this.y;
			boolean[] active = this.active;
			for (int i = 0, n = active.length; i < n; i++)
				if (active[i]) particles[i].translate(xAmount, yAmount);
		}
		this.x = x;
		this.y = y;
	}
	
	public Array<Sprite> getSprites () {
		return sprites;
	}
	
	public void setSprites (Array<Sprite> sprites) {
		this.sprites = sprites;
		if (sprites.size == 0) return;
		for (int i = 0, n = particles.length; i < n; i++) {
			Particle particle = particles[i];
			if (particle == null) break;
			Sprite sprite = null;
			switch (spriteMode) {
			case single:
				sprite = sprites.first();
				break;
			case random:
				sprite = sprites.random();
				break;
			case animated:
				float percent = 1 - particle.currentLife / (float)particle.life;
				particle.frame = Math.min((int)(percent * sprites.size), sprites.size - 1);
				sprite = sprites.get(particle.frame);
				break;
			}
			particle.setRegion(sprite);
			particle.setOrigin(sprite.getOriginX(), sprite.getOriginY());
		}
	}
	
	public Array<String> getImagePaths () {
		return imagePaths;
	}

	public int getMinParticleCount () {
		return minParticleCount;
	}

	public void setMinParticleCount (int minParticleCount) {
		this.minParticleCount = minParticleCount;
	}

	public int getMaxParticleCount () {
		return maxParticleCount;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
	
	public ScaledNumericValue getXScale () {
		return xScaleValue;
	}
	
	public ScaledNumericValue getYScale () {
		return yScaleValue;
	}
	
	public ScaledNumericValue getVelocity () {
		return velocityValue;
	}
	
	public ScaledNumericValue getEmission () {
		return emissionValue;
	}
	
	public ScaledNumericValue getSpawnWidth () {
		return spawnWidthValue;
	}

	public ScaledNumericValue getSpawnHeight () {
		return spawnHeightValue;
	}
	
	public SpawnShapeValue getSpawnShape () {
		return spawnShapeValue;
	}

	public void setAttached (boolean attached) {
		this.attached = attached;
	}
	
	protected RangedNumericValue[] getXSizeValues (){
		if (xSizeValues == null){
			xSizeValues = new RangedNumericValue[3];
			xSizeValues[0] = xScaleValue;
			xSizeValues[1] = spawnWidthValue;
			xSizeValues[2] = xOffsetValue;
		}
		return xSizeValues;
	}
	
	protected RangedNumericValue[] getYSizeValues (){
		if (ySizeValues == null){
			ySizeValues = new RangedNumericValue[3];
			ySizeValues[0] = yScaleValue;
			ySizeValues[1] = spawnHeightValue;
			ySizeValues[2] = yOffsetValue;
		}
		return ySizeValues;
	}
	
	protected RangedNumericValue[] getMotionValues (){
		if (motionValues == null){
			motionValues = new RangedNumericValue[3];
			motionValues[0] = velocityValue;
			motionValues[1] = windValue;
			motionValues[2] = gravityValue;
		}
		return motionValues;
	}
	
	/** Permanently scales the size of the emitter by scaling all its ranged values related to size. */
	public void scaleSize (float scale){
		if (scale == 1f) return;
		scaleSize(scale, scale);
	}
	
	/** Permanently scales the size of the emitter by scaling all its ranged values related to size. */
	public void scaleSize (float scaleX, float scaleY){
		if (scaleX == 1f && scaleY == 1f) return;
		for (RangedNumericValue value : getXSizeValues()) value.scale(scaleX);
		for (RangedNumericValue value : getYSizeValues()) value.scale(scaleY);
	}
	
	/** Permanently scales the speed of the emitter by scaling all its ranged values related to motion. */
	public void scaleMotion (float scale){
		if (scale == 1f) return;
		for (RangedNumericValue value : getMotionValues()) value.scale(scale);
	}
}