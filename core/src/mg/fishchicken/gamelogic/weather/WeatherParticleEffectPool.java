/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package mg.fishchicken.gamelogic.weather;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.WeatherParticleEffect;
import com.badlogic.gdx.utils.Pool;

import mg.fishchicken.gamelogic.weather.WeatherParticleEffectPool.PooledWeatherEffect;

public class WeatherParticleEffectPool extends Pool<PooledWeatherEffect> {
	private final ParticleEffect effect;

	public WeatherParticleEffectPool (ParticleEffect effect, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.effect = effect;
	}

	protected PooledWeatherEffect newObject () {
		return new PooledWeatherEffect(effect);
	}

	public PooledWeatherEffect obtain () {
		PooledWeatherEffect effect = super.obtain();
		effect.reset();
		return effect;
	}

	public class PooledWeatherEffect extends WeatherParticleEffect {
		ParticleEffect blueprint;
		PooledWeatherEffect (ParticleEffect effect) {
			super(effect);
			blueprint = effect;
		}
		
		@Override
		public void reset () {
			super.reset();
		}

		public void free () {
			WeatherParticleEffectPool.this.free(this);
			setEmittersFrom(blueprint);
		}
	}
}
