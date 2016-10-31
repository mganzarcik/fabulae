package mg.fishchicken.gamelogic.traps;

import groovy.lang.Script;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.audio.Sound;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.projectiles.OnProjectileHitCallback;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.projectiles.ProjectileTarget;
import mg.fishchicken.core.projectiles.ProjectileType;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.effects.EffectParameter;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.effects.targets.TargetTypeContainer;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.traps.Trap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * A definition of a trap. Each trap has a name, level, associated effects, sounds, 
 * a projectile  and target type. 
 * 
 * TrapType itself cannot do anything, but it can be used to create a {@link Trap}
 * object.
 * 
 */
public class TrapType  implements OnProjectileHitCallback, EffectContainer, TargetTypeContainer, XMLLoadable {

	public static final String STRING_TABLE = "traps."+Strings.RESOURCE_FILE_EXTENSION;
	public static String XML_DISARMED = "disarmed";
	public static String XML_SPRUNG = "sprung";
	
	private static ObjectMap<String, String> traps = new ObjectMap<String, String>();

	public static TrapType getTrap(String id) {
		return Assets.get(traps.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns an Array of all loaded traps.
	 * 
	 * @return
	 */
	public static Array<TrapType> getAllTraps() {
		Array<TrapType> returnValue = new Array<TrapType>();
		for (String id: traps.keys()) {
			returnValue.add(getTrap(id));
		}
		return returnValue;
	}

	/**
	 * Gathers all Traps and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherTraps() throws IOException {
		Assets.gatherAssets(Configuration.getFolderTraps(), "xml", TrapType.class, traps);
	}
	
	private String s_id;
	private String s_name;
	private int s_level;
	private boolean s_automaticOnHitAnimation;
	private String s_projectile;
	private ObjectMap<Effect, Array<EffectParameter>> s_effects;
	private String targetType;
	private Script targetScript;
	private Array<Sound> disarmedSounds, sprungSounds;
	
	public TrapType(FileHandle file) throws IOException {
		s_id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		s_effects =  new OrderedMap<Effect, Array<EffectParameter>>();
		s_projectile = null;
		s_automaticOnHitAnimation = true;
		loadFromXML(file);
	}
	
	@Override
	public String getId() {
		return s_id;
	}

	public String getName() {
		return Strings.getString(s_name);
	}
	
	@Override
	public String getRawName() {
		return s_name;
	}
	
	public int getLevel() {
		return s_level;
	}

	@Override
	public void addEffect(Effect effect, Array<EffectParameter> effectParameters) {
		s_effects.put(effect, effectParameters);
	}

	@Override
	public ObjectMap<Effect, Array<EffectParameter>> getEffects() {
		return s_effects;
	}
	
	@Override
	public String toString() {
		return s_id;
	}
	
	/**
	 * Executes the effects of this Trap as if
	 * it was used by the supplied user on the
	 * supplied target.
	 * 
	 * @param user
	 * @param target
	 * @return
	 */
	private void executeEffects(GameObject originator, GameObject target) {
		ProjectileTarget effectTarget = target;
		if (targetType != null) {
			TargetType targetType = TargetType.getTargetTypeInstance(this.targetType, targetScript);
			targetType.executeTargetScript(this, null);
			Tile tile = target.position().tile();
			targetType.setTarget(tile.getX(), tile.getY(), target.getMap());
			targetType.destroyLights();
			effectTarget = targetType;
		}
		
		if (s_projectile == null || originator.getMap() == null) {
			onProjectileHit(null, originator, effectTarget);
		} else {
			new Projectile(ProjectileType.getType(s_projectile), originator, effectTarget, this);
		}
	}
	
	@Override
	public void setTargetType(String targetType, Script targetScript) {
		this.targetType = targetType;
		this.targetScript = targetScript;
	}
	
	@Override
	public void onProjectileHit(Projectile projectile, GameObject originator,
			ProjectileTarget target) {
		if (s_automaticOnHitAnimation) {
			target.onHit(projectile, originator);
		}
		for (Effect effect : s_effects.keys()) {
			effect.executeEffect(this, originator, target, s_effects.get(effect));
		}
	}
	
	public Sound getDisarmedSound() {
		return disarmedSounds.random();
	}
	
	public Sound getSprungSound() {
		return sprungSounds.random();
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
	
	public void loadFromXML(Element root) {
		XMLUtil.readPrimitiveMembers(this, root);
		XMLUtil.readEffect(this, root.getChildByName(XMLUtil.XML_EFFECTS));
		XMLUtil.readTargetType(this, root.getChildByName(XMLUtil.XML_TARGET));
		Element soundsElement = root.getChildByName(XMLUtil.XML_SOUNDS);
		if (soundsElement != null) {
			disarmedSounds = XMLUtil.readSounds(soundsElement, XML_DISARMED);
			sprungSounds = XMLUtil.readSounds(soundsElement, XML_SPRUNG);
		}
	}
	
	/**
	 * Checks whether the supplied trapable has a active trap and if it does,
	 * it will spring it on the supplied character.
	 * 
	 * @param character
	 * @param trapable
	 * @return true if the trap was sprung, false otherwise
	 */
	public static boolean checkTrap(GameCharacter character, Trapable trapable, String messageKey) {
		Trap trap = trapable.getTrap();
		if (trap != null && !trap.isDisarmed()) {
			TrapType.springTrap(trap, trapable.getOriginatorGameObject(), character, messageKey);
			return true;
		}
		return false;
	}
	
	public static void springTrap(Trap trap, GameObject originator, GameCharacter target, String messageKey) {
		TrapType type = trap.getType();
		type.executeEffects(originator, target);
		Sound sound = type.getSprungSound();
		if (sound != null) {
			sound.play(originator);
		}
		trap.setDisarmed(true);
		Log.logLocalized(STRING_TABLE, messageKey, LogType.INFO, target.getName(), originator.getName(), type.getName());
	}

}
