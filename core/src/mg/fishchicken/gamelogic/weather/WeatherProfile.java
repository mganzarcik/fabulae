package mg.fishchicken.gamelogic.weather;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Sound;
import mg.fishchicken.audio.StreamingSound;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.weather.WeatherManager.PrecipitationAmount;
import mg.fishchicken.gamelogic.weather.WeatherParticleEffectPool.PooledWeatherEffect;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class WeatherProfile implements XMLLoadable, ThingWithId {

	private static ObjectMap<String, String> weatherProfiles = new ObjectMap<String, String>();

	public static WeatherProfile getWeatherProfile(String id) {
		return Assets.get(weatherProfiles.get(id.toLowerCase(Locale.ENGLISH)));
	}

	/**
	 * Gathers all Races and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherWeatherProfiles() throws IOException {
		Assets.gatherAssets(Configuration.getFolderWeatherProfiles(), "xml", WeatherProfile.class, weatherProfiles);
	}
	
	public static final String MODIFIER_SUFFIX = "Modifier";
	public static final String XML_CONTINOUS = "continuous";
	public static final String XML_RANDOM = "random";
	public static final String XML_TEMPERATURE = "temperature";
	public static final String XML_PRECIPITATION = "precipitation";
	public static final String XML_SUN = "sun";
	public static final String XML_WEATHER_EFFECTS = "weatherEffects";
	public static final String XML_RAIN = "rain";
	public static final String XML_SNOW = "snow";
	public static final String XML_SOUNDS = "sounds";
	public static final String XML_TRACK = "track";
	
	private String id;
	private boolean isModifier;
	@XMLField(fieldPath="temperature.min")
	private int s_min;
	@XMLField(fieldPath="temperature.max")
	private int s_max;
	@XMLField(fieldPath="precipitation.chanceForSmall")
	private int s_chanceForSmall;
	@XMLField(fieldPath="precipitation.chanceForMedium") 
	private int s_chanceForMedium;
	@XMLField(fieldPath="precipitation.chanceForHigh")
	private int s_chanceForHigh;
	@XMLField(fieldPath="precipitation.durationMin")
	private int s_durationMin;
	@XMLField(fieldPath="precipitation.durationMax")
	private int s_durationMax;
	@XMLField(fieldPath="weatherEffects.rain.small.particleEffect")
	private ParticleEffect rainSmall;
	@XMLField(fieldPath="weatherEffects.rain.medium.particleEffect")
	private ParticleEffect rainMedium;
	@XMLField(fieldPath="weatherEffects.rain.high.particleEffect")
	private ParticleEffect rainHigh;
	@XMLField(fieldPath="weatherEffects.snow.small.particleEffect")
	private ParticleEffect snowSmall;
	@XMLField(fieldPath="weatherEffects.snow.medium.particleEffect")
	private ParticleEffect snowMedium;
	@XMLField(fieldPath="weatherEffects.snow.high.particleEffect")
	private ParticleEffect snowHigh;
	private WeatherParticleEffectPool rainSmallPool, rainMediumPool, rainHighPool, snowSmallPool, snowMediumPool, snowHighPool;
	private ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>> continuousSoundsRain;
	protected ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>> randomSoundsRain;
	private ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>> continuousSoundsSnow;
	protected ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>> randomSoundsSnow;
	
	public WeatherProfile(FileHandle file) throws IOException {
		id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		isModifier = false;
		continuousSoundsRain = new ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>>();
		randomSoundsRain = new ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>>();
		continuousSoundsSnow = new ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>>() ;
		randomSoundsSnow = new ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>>();
		loadFromXML(file);
	}
	
	/**
	 * Creates a copy of the supplied Weather Profile.
	 * 
	 * The Id of the new copy will be null.
	 * 
	 * The particle effects will not be copied, both the copy
	 * and the original will point to the same objects. The
	 * same with effect pools and sounds.
	 * 
	 * @param profileToCopy
	 */
	public WeatherProfile(WeatherProfile profileToCopy) {
		id = null;
		isModifier = profileToCopy.isModifier;
		s_min = profileToCopy.s_min;
		s_max = profileToCopy.s_max;
		s_chanceForSmall = profileToCopy.s_chanceForSmall;
		s_chanceForMedium = profileToCopy.s_chanceForMedium;		
		s_chanceForHigh = profileToCopy.s_chanceForHigh;
		s_durationMax = profileToCopy.s_durationMax;
		s_durationMin = profileToCopy.s_durationMin;
		rainSmall = profileToCopy.rainSmall;
		rainMedium = profileToCopy.rainMedium;
		rainHigh = profileToCopy.rainHigh;
		rainSmallPool = profileToCopy.rainSmallPool;
		rainMediumPool = profileToCopy.rainMediumPool;
		rainHighPool = profileToCopy.rainHighPool;
		snowSmall = profileToCopy.snowSmall;
		snowMedium = profileToCopy.snowMedium;
		snowHigh = profileToCopy.snowHigh;
		continuousSoundsRain = profileToCopy.continuousSoundsRain;
		continuousSoundsSnow = profileToCopy.continuousSoundsSnow;
		randomSoundsRain = profileToCopy.randomSoundsRain;
		randomSoundsSnow = profileToCopy.randomSoundsSnow;
	}
	
	public String getId() {
		return id;
	}
	
	public AudioTrack<?> getContinousSound(PrecipitationAmount amount, boolean isRain) {
		Array<AudioTrack<?>> tracks = null;
		if (isRain) {
			tracks = continuousSoundsRain.get(amount);
		} else {
			tracks = continuousSoundsSnow.get(amount);
		}
		return tracks == null ? null : tracks.random();
	}
	
	public boolean isModifier() {
		return isModifier;
	}

	public void addToTemperatureMin(int value) {
		s_min += value;
	}
	
	public int getTemperatureMin() {
		return s_min;
	}
	
	public int getTemperatureMax() {
		return s_max;
	}
	
	public void addToTemperatureMax(int value) {
		s_max += value;
	}
	
	/**
	 * Gets the chance that small precipitation will occur at least
	 * once during the day.
	 * 
	 * @return
	 */
	public int getChanceForSmallPrecipitation() {
		return s_chanceForSmall;
	}
	
	public void addToChanceForSmallPrecipitation(int value) {
		s_chanceForSmall += value;
	}
	
	/**
	 * Gets the chance that high precipitation will occur at least
	 * once during the day.
	 * 
	 * @return
	 */
	public int getChanceForHighPrecipitation() {
		return s_chanceForHigh;
	}
	
	public void addToChanceForHighPrecipitation(int value) {
		s_chanceForHigh += value;
	}
	

	/**
	 * Gets the minimum duration of a weather effect
	 * in game seconds.
	 * 
	 * @return
	 */
	public int getPrecipitationDurationMin() {
		return s_durationMin;
	}
	
	public void addToPrecipitationDurationMin(int value) {
		s_durationMin += value;
	}
	
	/**
	 * Gets the maximum duration of a weather effect
	 * in game seconds.
	 * 
	 * @return
	 */
	public int getPrecipitationDurationMax() {
		return s_durationMax;
	}
	
	public void addToPrecipitationDurationMax(int value) {
		s_durationMax += value;
	}
	
	
	/**
	 * Gets the chance that medium precipitation will occur at least
	 * once during the day.
	 * 
	 * @return
	 */
	public int getChanceForMediumPrecipitation() {
		return s_chanceForMedium;
	}
	
	public void addToChanceForMediumPrecipitation(int value) {
		s_chanceForMedium += value;
	}

	public PooledWeatherEffect getRainEffect(PrecipitationAmount amount) {
		switch (amount) {
		case SMALL:
			return rainSmallPool != null ? rainSmallPool.obtain() : null;
		case MEDIUM:
			return rainMediumPool != null ? rainMediumPool.obtain() : null;
		case HIGH:
			return rainHighPool != null ? rainHighPool.obtain() : null;
		default:
			return null;
		}
	}
	
	public PooledWeatherEffect getSnowEffect(PrecipitationAmount type) {
		switch (type) {
		case SMALL:
			return snowSmallPool != null ? snowSmallPool.obtain() : null;
		case MEDIUM:
			return snowMediumPool != null ? snowMediumPool.obtain() : null;
		case HIGH:
			return snowHighPool != null ? snowHighPool.obtain() : null;
		default:
			return null;
		}
	}
	
	private void readAllSounds (Element element, ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>> continuous, ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>> random) {
		if (element == null) {
			return;
		}
		for (PrecipitationAmount amount : PrecipitationAmount.values()) {
			Element amountElement = element.getChildByName(amount.toString().toLowerCase(Locale.ENGLISH));
			if (amountElement != null) {
				Element soundsElement =  amountElement.getChildByName(XML_SOUNDS);
				if (soundsElement != null) {
					readTracks(soundsElement.getChildByName(XML_CONTINOUS), continuous, amount, StreamingSound.class);
					readTracks(soundsElement.getChildByName(XML_RANDOM), random, amount, Sound.class);
				}
			}
		}
	}
	
	private <T extends AudioTrack<?>> void readTracks(Element tracksElement, ObjectMap<PrecipitationAmount, Array<AudioTrack<?>>> trackMap, PrecipitationAmount amount, Class<T> trackType) {
		if (tracksElement == null) {
			return;
		}
		Array<AudioTrack<?>> array = trackMap.get(amount);
		if (array == null) {
			array = new Array<AudioTrack<?>>();
			trackMap.put(amount, array);
		}
		
		Array<AudioTrack<?>> tracks = XMLUtil.readTracks(tracksElement, trackType);
		for (AudioTrack<?> track : tracks) {
			array.add(track);
		}
	}
	
	private void createPools() {
		if (rainSmall != null) {
			rainSmallPool = new  WeatherParticleEffectPool(rainSmall, 2, 4);
		}
		if (rainMedium != null) {
			rainMediumPool = new  WeatherParticleEffectPool(rainMedium, 2, 4);
		}
		if (rainHigh != null) {
			rainHighPool = new  WeatherParticleEffectPool(rainHigh, 2, 4);
		}
		if (snowSmall != null) {
			snowSmallPool = new  WeatherParticleEffectPool(snowSmall, 2, 4);
		}
		if (snowMedium != null) {
			snowMediumPool = new  WeatherParticleEffectPool(snowMedium, 2, 4);
		}
		if (snowHigh != null) {
			snowHighPool = new  WeatherParticleEffectPool(snowHigh, 2, 4);
		}
	}

	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		loadFromXMLNoInit(file);
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		
		isModifier = root.getName().endsWith(MODIFIER_SUFFIX);
		XMLUtil.readPrimitiveMembers(this,
				root);
		createPools();
		Element weatherEffects = root.getChildByName(XML_WEATHER_EFFECTS);
		if (weatherEffects != null) {
			readAllSounds(weatherEffects.getChildByName(XML_RAIN), continuousSoundsRain, randomSoundsRain);
			readAllSounds(weatherEffects.getChildByName(XML_SNOW), continuousSoundsSnow, randomSoundsSnow);
		}
	}

}

