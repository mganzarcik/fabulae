package mg.fishchicken.gamelogic.time;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.ColorUtil;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.time.GameCalendar.Month;
import mg.fishchicken.graphics.lights.ManagedLight;
import mg.fishchicken.tweening.ColorTweenAccessor;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import box2dLight.Light;
import box2dLight.RayHandler;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class Sun {
	
	protected Color sunIntensity;
	private RayHandler sunHandler;
	private Color availableLight, newLightColor;
	private Array<Light> lightsToDeactivate;
	private Array<Light> lightsToActivate;
	private GameMap map;
		
	public Sun(GameMap map, RayHandler rayHandler) {
		sunHandler = rayHandler;
		sunIntensity = new Color();
		lightsToActivate = new Array<Light>();
		lightsToDeactivate = new Array<Light>();
		availableLight = new Color();
		newLightColor = new Color();
		this.map = map;
	}
	
	public void reset(RayHandler rayHandler) {
		sunHandler = rayHandler;
	}

	/**
	 * Updates the sun's intensity and color based on the time of day.
	 * 
	 * @param doDayNightCycle - if true, light intensity will change based on the time
	 *                  of day. If false, light intensity will not change and
	 *                  will be equal to the maximum daylight.
	 */
	public void update(boolean doDayNightCycle) {
		GameCalendarDate currentDate = GameState.getCurrentGameDate();
		Month monthInfo = currentDate.getMonthInfo();
		float dayIntensity = Configuration.getAmbientLightMax() * monthInfo.getSunIntensity();
		
		int timeMultiplier =map.getGameTimeMultiplier();
		
		if (doDayNightCycle) {
			GameCalendar calendar = currentDate.getCalendar();
			
			int distanceToDawn = currentDate.substractTimeOnly(monthInfo.dawnTime);
			int distanceToDusk = currentDate.substractTimeOnly(monthInfo.duskTime);
			
			boolean isDay = distanceToDawn > 0 && distanceToDusk <= 0;
			
			float nightIntensity = Configuration.getAmbientLightMin(); 
			
			if (isDay && distanceToDawn > 0 && !ColorUtil.isMore(sunIntensity, dayIntensity)) {
				float dawnDuskDuration = (float)calendar.getDawnDuskDuration()/timeMultiplier;
				float distanceToDawnReal = (float)distanceToDawn/timeMultiplier;
				
				sunIntensity.set(nightIntensity, nightIntensity, nightIntensity, 1f);
				Timeline currentTimeline = createDawnTimeline(dawnDuskDuration, dayIntensity).start();
				currentTimeline.update(distanceToDawnReal);
				if (ColorUtil.isMore(sunIntensity, dayIntensity)) {
					Log.logLocalized("sunRise", LogType.WEATHER);
				}
			} else if (!isDay && distanceToDusk > 0 && !ColorUtil.isLess(sunIntensity, nightIntensity)) {
				float dawnDuskDuration = (float)calendar.getDawnDuskDuration()/timeMultiplier;
				float distanceToDuskReal = (float)distanceToDusk/timeMultiplier;

				sunIntensity.set(dayIntensity, dayIntensity, dayIntensity, 1f);
				Timeline currentTimeline = createDuskTimeline(dawnDuskDuration, nightIntensity).start();
				currentTimeline.update(distanceToDuskReal);
				if (ColorUtil.isLess(sunIntensity, nightIntensity)) {
					Log.logLocalized("sunSet", LogType.WEATHER);
				}
			} else {
				float intensity = isDay ? dayIntensity : nightIntensity;
				sunIntensity.set(intensity, intensity, intensity, 1f);
			}
		} else {
			sunIntensity.set(dayIntensity, dayIntensity, dayIntensity, 1f);
		} 
		float mul = map.getSunlightMultiplier();
		sunHandler.ambientLight.set(sunIntensity).mul(mul, mul, mul, 1f);
		
		updateLights();
	}
	
	/**
	 * Returns true if the sun is set. This will check the current sun intensity and is not
	 * dependent of actual time of day. Use GameCalendar for that.
	 * 
	 * @return
	 */
	public boolean isSet() {
		float nightIntensity = Configuration.getAmbientLightMin();
		return sunIntensity.b == nightIntensity && sunIntensity.g == nightIntensity && sunIntensity.r == nightIntensity;
	}
	
	private Timeline createDuskTimeline(float duration, float target) {
		Timeline timeline = Timeline.createParallel();
		timeline
			.beginParallel()
				.push(Tween.to(sunIntensity, ColorTweenAccessor.RED, duration).target(target).ease(TweenEquations.easeInSine))
				.push(Tween.to(sunIntensity, ColorTweenAccessor.GREEN, duration).target(target).ease(TweenEquations.easeOutQuad))
				.push(Tween.to(sunIntensity, ColorTweenAccessor.BLUE, duration).target(target).ease(TweenEquations.easeOutQuad))
			.end();
		return timeline;
	}
	
	private Timeline createDawnTimeline(float duration, float target) {
		Timeline timeline = Timeline.createParallel();
		timeline
			.beginParallel()
				.push(Tween.to(sunIntensity, ColorTweenAccessor.RED, duration).target(target).ease(TweenEquations.easeInOutSine))
				.push(Tween.to(sunIntensity, ColorTweenAccessor.GREEN, duration).target(target).ease(TweenEquations.easeInQuad))
				.push(Tween.to(sunIntensity, ColorTweenAccessor.BLUE, duration).target(target).ease(TweenEquations.easeInQuad))
			.end();
		return timeline;
	}


	private void updateLights() {
		float mul = map.getSunlightMultiplier();
		availableLight.set(0.5f-(sunIntensity.r*mul), 0.5f-(sunIntensity.g*mul), 0.5f-(sunIntensity.b*mul), 1f);
		
		lightsToDeactivate.clear();
		lightsToActivate.clear();
		
		for (Light light : sunHandler.lightList) {
			updateLight(light, availableLight);
		}
		
		for (Light light : sunHandler.disabledLights) {
			updateLight(light, availableLight);
		}
		
		for (Light light : lightsToDeactivate) {
			light.setActive(false);
		}
		
		for (Light light : lightsToActivate) {
			light.setActive(true);
		}
	}
	
	private void updateLight(Light light, Color availableLight) {
		if (!(light instanceof ManagedLight)) {
			return;
		}
		Color maxLightIntensity = ((ManagedLight)light).getMaxIntensity();
		newLightColor.set(light.getColor());
		// for lights that represent sunlight coming in from the outside
		// their intensity is higher the higher the sun's intensity
		if (((ManagedLight)light).isInteriorSunlight()) {
			newLightColor.set(sunIntensity);
		} else {
			newLightColor.set(maxLightIntensity.r * (availableLight.r/0.5f),
					maxLightIntensity.g * (availableLight.g/0.5f), maxLightIntensity.b
					* (availableLight.b/0.5f), maxLightIntensity.a);
		}
		if (((ManagedLight)light).isInteriorSunlight()) {
			float min = Configuration.getAmbientLightMin();
			newLightColor.add((new Color(newLightColor)).sub(min, min, min, newLightColor.a));
		}
		if (newLightColor.r <= 0 && newLightColor.g <= 0 && newLightColor.b <= 0) {
			lightsToDeactivate.add(light);
		} else if (!light.isActive()) {
			lightsToActivate.add(light);
		}
		
		light.setColor(newLightColor);
	}

}
