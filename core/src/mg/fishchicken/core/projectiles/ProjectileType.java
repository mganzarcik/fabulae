package mg.fishchicken.core.projectiles;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.audio.Sound;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.graphics.animations.OrientationAnimationMap;
import mg.fishchicken.graphics.lights.LightDescriptor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ProjectileType implements XMLLoadable, ThingWithId {
	
	public static String XML_ANIMATION_FILE = "animationFile";
	public static String XML_ON_HIT = "onHit";
	public static String XML_ON_START = "onStart";
	public static String XML_DURING = "during";
	public static String XML_LIGHT = "light";
	public static String XML_SCALING_METHOD = "scalingMethod";
		
	public static enum ScalingMethod { PARTICLES, EMISSION };
	
	private static ObjectMap<String, String> types = new ObjectMap<String, String>(); 
	
	public static ProjectileType getType(String id) {
		return Assets.get(types.get(id.toLowerCase(Locale.ENGLISH)), ProjectileType.class);
	}
	
	/**
	 * Gathers all ProjectileTypes and registers them in the AssetManager
	 * so that they can be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherProjectileTypes() throws IOException {
		Assets.gatherAssets(Configuration.getFolderProjectiles(), "xml", ProjectileType.class, types);
	}
	
	private String id;
	private String s_particleEffect;
	private String s_animationFile;
	private String s_animationInfoFile;
	// offsets are always specified as if for orthogonal maps - they will be automatically transformed
	// for isometric maps to work the same
	public float s_xOffsetEnd, s_yOffsetEnd;
	public OrientationAnimationMap animations;
	public LightDescriptor s_light;
	public float s_speed;
	public float s_moveDelay;
	public boolean s_fixedOnOrigin;
	public ScalingMethod s_scalingMethod;
	public Array<Sound> onStartSounds, onHitSounds, duringSounds;
	
	public ProjectileType(FileHandle file) throws IOException {
		id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		s_moveDelay = 0;
		s_scalingMethod = ScalingMethod.PARTICLES;
		loadFromXML(file);
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return id;
	}

	public String getParticleEffect() {
		return s_particleEffect;
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
		loadFromXML(root);
	}
	
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
		
		Element soundsElement = root.getChildByName(XMLUtil.XML_SOUNDS);
		if (soundsElement != null) {
			onStartSounds = XMLUtil.readSounds(soundsElement, ProjectileType.XML_ON_START);
			onHitSounds = XMLUtil.readSounds(soundsElement, ProjectileType.XML_ON_HIT);
			duringSounds = XMLUtil.readSounds(soundsElement, ProjectileType.XML_DURING);
		} else {
			onStartSounds = new Array<Sound>();
			onHitSounds = new Array<Sound>();
			duringSounds = new Array<Sound>();
		}
		
		try {
			if (s_animationFile != null && s_animationInfoFile != null) {
				s_animationFile = Configuration.addModulePath(s_animationFile);
				s_animationInfoFile = Configuration.addModulePath(s_animationInfoFile);
				animations = new OrientationAnimationMap(s_animationFile,
						Gdx.files.internal(s_animationInfoFile));
			}
		} catch (IOException e) {
			throw new GdxRuntimeException("Problem loading animation for projectile type "+getId(),e);
		}
	}
	
}
