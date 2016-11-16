package mg.fishchicken.gamelogic.weather;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.weather.Weather.WeatherRenderer;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class WeatherManager {

	public static final String XML_NEXT_WEATHER_UPDATE = "nextWeatherUpdate";
	public static final String XML_CURRENT_WEATHER = "currentWeather";
	public static final String XML_CURRENT_TEMPERATURE = "currentTemperature";

	public static enum PrecipitationAmount {
		NONE, SMALL, MEDIUM, HIGH
	};

	public static final String STRING_TABLE = "weather."+Strings.RESOURCE_FILE_EXTENSION;

	private float nextPossibleWeatherChange;
	private Weather currentWeather;
	private Integer currentTemperature = null;
	private TweenManager weatherTweenManager;
	GameState gameState;

	public WeatherManager(GameState global) {
		this.gameState = global;
	}

	public TweenManager getTweenManager() {
		if (weatherTweenManager == null) {
			weatherTweenManager = new TweenManager();
		}
		return weatherTweenManager;
	}

	public int getCurrentTemperature() {
		return currentTemperature != null ? currentTemperature : 0;
	}

	/**
	 * Returns the renderer that can draw the current weather.
	 * 
	 * @return
	 */
	public WeatherRenderer getCurrentWeatherRenderer() {
		if (currentWeather != null) {
			return currentWeather.getRenderer();
		}
		return null;
	}

	public void update(float deltaTime) {
		GameMap map = gameState.getCurrentMap();
		if (map == null || GameState.isPaused()) {
			return;
		}

		if (weatherTweenManager != null) {
			weatherTweenManager.update(deltaTime);
		}

		// update interior weather here, because we don't draw it
		if (map.isInterior()) {
			if (currentWeather != null) {
				currentWeather.update(deltaTime, map);
			}
		}

		nextPossibleWeatherChange -= (deltaTime * map.getGameTimeMultiplier());

		if (currentWeather != null) {
			if (currentWeather.isStopped()) {
				currentWeather.free();
				currentWeather = null;
			} else if (currentWeather.isStarting()
					|| currentWeather.isStopping()) {
				return;
			}
		}

		if (nextPossibleWeatherChange > 0) {
			return;
		}

		nextPossibleWeatherChange = MathUtils.random(
				Configuration.getWeatherUpdateMin(),
				Configuration.getWeatherUpdateMax());

		WeatherProfile currentProfile = determineCurrentWeatherProfile();
		determineTemperature(currentProfile);
		handleWeather(currentProfile);
	}

	public void stop() {
		if (currentWeather != null) {
			currentWeather.stopImmediately();
			currentWeather.free();
			currentWeather = null;
		}
	}
	
	public void pause() {
		if (currentWeather != null) {
			currentWeather.pause();
		}
	}

	public void resume() {
		if (currentWeather != null) {
			currentWeather.resume();
		}
	}

	private void handleWeather(WeatherProfile currentProfile) {
		PrecipitationAmount precipitation = determinePrecipitation(currentProfile);
		if (precipitation != PrecipitationAmount.NONE) {
			startNewWeather(new Weather(this, currentTemperature,
					precipitation, MathUtils.random(
							currentProfile.getPrecipitationDurationMin(),
							currentProfile.getPrecipitationDurationMax()),
					currentProfile));
		}
	}

	/**
	 * Starts to rain immediately.
	 * 
	 * @param amount
	 */
	public void startRain(PrecipitationAmount amount) {
		startNewWeather(new Weather(this,
				Configuration.getSnowTemperatureThreshold() + 1, amount, GameState
						.getCurrentGameDate().getWeatherProfile()));
	}

	/**
	 * Starts to snow immediately.
	 * 
	 * @param amount
	 */
	public void startSnow(PrecipitationAmount amount) {
		startNewWeather(new Weather(this,
				Configuration.getSnowTemperatureThreshold() - 1, amount, GameState
						.getCurrentGameDate().getWeatherProfile()));
	}

	private void startNewWeather(final Weather newWeather) {
		cameraMoved(newWeather);
		if (currentWeather == null) {
			currentWeather = newWeather;
			currentWeather.startGently();
		} else {
			if (!newWeather.isSameType(currentWeather)
					|| !newWeather.isSameAmount(currentWeather)) {
				currentWeather.stopGently(new TweenCallback() {
					@Override
					public void onEvent(int type, BaseTween<?> source) {
						if (type == TweenCallback.COMPLETE) {
							newWeather.startGently();
							currentWeather = newWeather;
						}
					}
				});
			}
		}
	}

	private PrecipitationAmount determinePrecipitation(
			WeatherProfile currentWeatherProfile) {
		float random = MathUtils.random(1f, 100f);
		float averageUpdateTime = (Configuration.getWeatherUpdateMax() + Configuration
				.getWeatherUpdateMin()) / 2f;
		float updatesPerDay = 86400 / averageUpdateTime;

		// we divide the chance by the average number of times an update occurs
		// during a day
		float small = currentWeatherProfile.getChanceForSmallPrecipitation()
				/ updatesPerDay;
		float medium = small
				+ currentWeatherProfile.getChanceForMediumPrecipitation()
				/ updatesPerDay;
		float high = medium
				+ currentWeatherProfile.getChanceForHighPrecipitation()
				/ updatesPerDay;

		if (random < small) {
			return PrecipitationAmount.SMALL;
		} else if (random < medium) {
			return PrecipitationAmount.MEDIUM;
		} else if (random < high) {
			return PrecipitationAmount.HIGH;
		}
		return PrecipitationAmount.NONE;
	}

	private void determineTemperature(WeatherProfile currentWeatherProfile) {
		int tempMin = currentWeatherProfile.getTemperatureMin();
		int tempMax = currentWeatherProfile.getTemperatureMax();

		if (GameState.getCurrentGameDate().isNight()) {
			tempMin += Configuration.getNightTemperatureModifier();
			tempMax += Configuration.getNightTemperatureModifier();
		}

		// Log.log("Temp min: {0}, temp max: {1}", LogType.ERROR, tempMin,
		// tempMax);

		if (currentTemperature == null) {
			currentTemperature = MathUtils.random(tempMin, tempMax);
		} else {
			if (currentTemperature >= tempMin && currentTemperature <= tempMax) {
				if (MathUtils.randomBoolean()) {
					int increaseDeterminer = MathUtils.random(tempMin, tempMax);
					if (increaseDeterminer < currentTemperature) {
						--currentTemperature;
					} else {
						++currentTemperature;
					}
					MathUtils.clamp(currentTemperature, tempMin, tempMax);
				}
			} else {
				if (currentTemperature < tempMin) {
					++currentTemperature;
				} else {
					--currentTemperature;
				}
			}
		}
	}

	WeatherProfile determineCurrentWeatherProfile() {
		WeatherProfile currentProfile = GameState.getCurrentGameDate()
				.getWeatherProfile();

		if (GameState.getCurrentGameDate().getWeatherProfile() == null) {
			return null;
		}

		currentProfile = new WeatherProfile(currentProfile);

		Array<WeatherProfile> modifiers = gameState.getCurrentMap()
				.getWeatherModifiers();

		for (WeatherProfile modifier : modifiers) {
			currentProfile.addToTemperatureMin(modifier.getTemperatureMin());
			currentProfile.addToTemperatureMax(modifier.getTemperatureMax());
			currentProfile.addToChanceForHighPrecipitation(modifier
					.getChanceForHighPrecipitation());
			currentProfile.addToChanceForMediumPrecipitation(modifier
					.getChanceForMediumPrecipitation());
			currentProfile.addToChanceForSmallPrecipitation(modifier
					.getChanceForSmallPrecipitation());
		}
		return currentProfile;
	}

	public void resized() {
		if (currentWeather != null) {
			currentWeather.resizeFromCamera();
			cameraMoved();
		}
	}

	public void cameraMoved() {
		cameraMoved(currentWeather);
	}

	private void cameraMoved(Weather weather) {	
		if (weather != null && gameState.getCurrentMap() != null) {
			Camera camera = gameState.getCurrentMap().getCamera();
			if (camera != null) {
				weather.setPosition(camera.position.x, camera.position.y);
			}
		}
	}

	public void mapChanged(GameMap newMap) {
		if (currentWeather != null) {
			currentWeather.mapChanged(newMap);
			if (newMap != null) {
				cameraMoved(currentWeather);
			}
		}
		resume();
	}

	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_CURRENT_TEMPERATURE).text(currentTemperature).pop();
		writer.element(XML_NEXT_WEATHER_UPDATE).text(nextPossibleWeatherChange)
				.pop();
		if (currentWeather != null) {
			writer.element(XML_CURRENT_WEATHER);
			currentWeather.writeToXML(writer);
			writer.pop();
		}
	}

	public void loadFromXML(Element weatherElement) throws IOException {
		currentTemperature = weatherElement.getInt(XML_CURRENT_TEMPERATURE);
		nextPossibleWeatherChange = weatherElement
				.getFloat(XML_NEXT_WEATHER_UPDATE);
		Element element = weatherElement.getChildByName(XML_CURRENT_WEATHER);
		if (element != null) {
			currentWeather = new Weather(element, this);
		} else {
			currentWeather = null;
		}
	}
}
