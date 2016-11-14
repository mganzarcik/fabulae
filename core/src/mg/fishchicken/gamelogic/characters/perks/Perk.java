package mg.fishchicken.gamelogic.characters.perks;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import groovy.lang.Binding;
import groovy.lang.Script;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.conditions.Condition.ConditionResult;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.Targetable;
import mg.fishchicken.core.projectiles.OnProjectileHitCallback;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.projectiles.ProjectileTarget;
import mg.fishchicken.core.projectiles.ProjectileType;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.effects.EffectParameter;
import mg.fishchicken.gamelogic.effects.targets.Self;
import mg.fishchicken.gamelogic.effects.targets.Single;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.effects.targets.TargetTypeContainer;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;
import mg.fishchicken.gamestate.characters.Stats;

/**
 * Perk is a special ability a character may have.
 * <br /><br />
 * Perks are associated with requirements, modifiers and effects.
 * <br /><br />
 * Requirements must be fulfilled in order for a character
 * to be able to learn and use a perk.
 * <br /><br />
 * Modifiers are always applied to the character that learned / used
 * the perk and effects are applied (usually) to the target of the perk.
 * <br /><br />
 * Perks can be passive or active. Passive perks don't need to be activated
 * to be used. Active perks must be activated and have a limited duration
 * (can also be instanteous) and associated costs. Activated perks
 * can also be attacks, which means they follow standard attacking 
 * rules when activated.
 * 
 * @author ANNUN
 *
 */
public class Perk implements XMLLoadable, ModifierContainer, EffectContainer, TargetTypeContainer, Targetable, OnProjectileHitCallback, AssetContainer{
	
	public static final String STRING_TABLE = "perks."+Strings.RESOURCE_FILE_EXTENSION;
	public static final String XML_LEARN_REQUIREMENTS = "learnRequirements";
	public static final String XML_ACTIVATION_REQUIREMENTS = "activationRequirements";
	private static ObjectMap<String, String> perks = new ObjectMap<String, String>();

	public static Perk getPerk(String id) {
		return Assets.get(perks.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns an Array of all loaded perks.
	 * 
	 * @return
	 */
	public static Array<Perk> getAllPerks() {
		Array<Perk> returnValue = new Array<Perk>();
		for (String id: perks.keys()) {
			returnValue.add(getPerk(id));
		}
		return returnValue;
	}

	/**
	 * Gathers all Perks and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherPerks() throws IOException {
		Assets.gatherAssets(Configuration.getFolderPerks(), "xml", Perk.class, perks);
	}
	
	/**
	 * Gathers all Perks and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherPerkImages() throws IOException {
		AssetMap assetStore = new AssetMap(); 
		for (String id: perks.keys()) {
			getPerk(id).gatherAssets(assetStore);
		}
		for(Entry<String, Class<?>> entry : assetStore) {
			Assets.getAssetManager().load(entry.key, entry.value);
		}
	}
		
	private String s_id;
	private String s_name, s_description;
	private int s_level, s_ap, s_mp, s_sp, s_hp; // requirements and costs for the perk
	private Condition s_learnRequirements, s_activationRequirements;
	private boolean s_isAttack, s_isActivated, s_combatOnly, s_automaticOnHitAnimation;
	private String s_characterSound;
	private Array<Modifier> s_modifiers;
	private OrderedMap<Effect, Array<EffectParameter>> s_effects;
	private String targetType;
	private Script targetScript;
	private TargetType target;
	private String projectile;
	private String s_animationState;
	private String s_iconFile;
	private int s_rank;
	
	public Perk(FileHandle file) throws IOException {
		s_id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		s_modifiers = new Array<Modifier>();
		s_effects =  new OrderedMap<Effect, Array<EffectParameter>>();
		s_isAttack = false;
		s_isActivated = false;
		s_automaticOnHitAnimation = true;
		projectile = null;
		loadFromXML(file);
	}
		
	@Override
	public String getRawName() {
		return s_name;
	}
	
	/**
	 * Returns the user friendly, localized name of this Perk.
	 */
	@Override
	public String getName() {
		return Strings.getString(s_name);
	}

	@Override
	public void onModifierChange() {
	}
	
	@Override
	public void addModifier(Modifier modifier) {
		s_modifiers.add(modifier);
	}

	@Override
	public Iterator<Modifier> getModifiers() {
		return s_modifiers.iterator();
	}

	public String getId() {
		return s_id;
	}

	public void setName(String name) {
		s_name = name;
	}

	public String getDescription() {
		return Strings.getString(s_description);
	}

	public void setDescription(String description) {
		s_description = description;
	}

	public int getLevelRequirement() {
		return s_level;
	}

	public void setLevelRequirement(int level) {
		s_level = level;
	}

	@Override
	public void addEffect(Effect effect, Array<EffectParameter> effectParameters) {
		s_effects.put(effect, effectParameters);
	}
	
	@Override
	public ObjectMap<Effect, Array<EffectParameter>> getEffects() {
		return s_effects;
	}

	/**
	 * Returns the AP cost it would cost the supplied character
	 * to use this perk.
	 * 
	 * @param character
	 * @return
	 */
	public int getApCost(GameCharacter character) {
		if (!isAttack()) {
			return s_ap;
		}
		return character.stats().getAPCostToAttackModified(s_ap);
	}

	public void setApCost(int ap) {
		s_ap = ap;
	}

	public int getMpCost() {
		return s_mp;
	}

	public void setMpCost(int mp) {
		s_mp = mp;
	}

	public int getSpCost() {
		return s_sp;
	}

	public void setSpCost(int sp) {
		s_sp = sp;
	}

	public int getHpCost() {
		return s_hp;
	}
	
	public void setHpCost(int hp) {
		s_hp = hp;
	}

	/**
	 * Returns true if this is an attack perk.
	 * 
	 * Attack perks needs to use standard attacking logic to determine whether
	 * they hit or not and also damage based on the equipped weapon.
	 * 
	 * If an attack perk misses, no effect is executed.
	 * 
	 * @return
	 */
	public boolean isAttack() {
		return s_isAttack;
	}

	/**
	 * Returns true if this is an activated perk,
	 * false if it is passive (i.e. always active).
	 * 
	 * @return
	 */
	public boolean isActivated() {
		return s_isActivated;
	}

	/**
	 * Returns true if this perk can only be used during combat.
	 * 
	 * Using such a perk outside of combat will start
	 * combat automatically.
	 * 
	 * @return
	 */
	public boolean isCombatOnly() {
		return s_combatOnly;
	}

	/**
	 * Returns true if this Perk requires
	 * the player to choose a target.
	 * 
	 * @return
	 */
	public boolean requiresTargeting() {
		return target.requiresTargeting();
	}
	
	/**
	 * Gets the target type for this perk.
	 * 
	 * In case the Perk has no effects associated,
	 * null is returned.
	 * @return
	 */
	public TargetType getTargetTypeInstance(GameCharacter user) {
		if (targetType != null) {
			TargetType target = TargetType.getTargetTypeInstance(targetType, targetScript);
			target.executeTargetScript(user, null);
			return target;
		}
		
		// if we have no effects, we are self targeted if we are not an attack
		if (!isAttack()) {
			return new Self();
		} else {
			return new Single();
		}
	}
	
	/**
	 * Executes the effects of this Perk as if
	 * it was used by the supplied user on the
	 * supplied target.
	 * 
	 * @param user
	 * @param target
	 * @return
	 */
	public void executeEffects(AbstractGameCharacter user, TargetType target) {
		if (s_characterSound != null) {
			user.getTrack(s_characterSound).play(user);
		}
		if (!target.requiresTargeting() || projectile == null || user.getMap() == null) {
			onProjectileHit(null, user, target);
		} else {
			new Projectile(ProjectileType.getType(projectile), user, target, this);
		}
	}
	
	/**
	 * Returns true if the supplied character fulfills all
	 * requirements in order to learn this perk.
	 * 
	 * @param character
	 * @return
	 */
	public boolean canBeLearned(GameCharacter character) {
		if (character.stats().getLevel() < getLevelRequirement()) {
			return false;
		}
		
		if (s_learnRequirements != null) {
			return s_learnRequirements.execute(character, new Binding());
		}
		
		return true;
	}
	
	public String getLearnRequirementsAsString() {
		if (s_learnRequirements != null) {
			return s_learnRequirements.toUIString();
		}
		return null;
	}

	/**
	 * Evaluates the learn requirements for this perk for the supplied character
	 * and returns an array that will contain the result for each condition
	 * associated with the perk.
	 * 
	 * Does not contain the level requirement.
	 * 
	 * @return null if there are no requirements, the array instead
	 */
	public Array<ConditionResult> evaluateLearnRequirements(GameCharacter character) {
		if (s_learnRequirements != null) {
			return s_learnRequirements.evaluateWithDetails(character, new Binding());
		}
		return null;
	}
	
	/**
	 * Evaluates the activation requirements for this perk for the supplied character
	 * and returns an array that will contain the result for each condition
	 * associated with the perk.
	 * 
	 * Does not contain the level requirement.
	 * 
	 * @return null if there are no requirements, the array instead
	 */
	public Array<ConditionResult> evaluateActivationRequirements(GameCharacter character) {
		if (s_activationRequirements != null) {
			return s_activationRequirements.evaluateWithDetails(character, new Binding());
		}
		return null;
	}
	
	/**
	 * Returns true if the supplied character fulfills all
	 * requirements in order to activate this perk. Please note
	 * that in order to be able to activate, the character
	 * must also fulfill all the requirements required to learn 
	 * the perk.
	 * 
	 * @param character
	 * @return
	 */
	public boolean canBeActivated(GameCharacter character) {
		return canBeActivated(character, false);
		
	}
	
	/**
	 * Returns true if the supplied character fulfills all
	 * requirements in order to activate this perk. Please note
	 * that in order to be able to activate, the character
	 * must also fulfill all the requirements required to learn 
	 * the perk.
	 * 
	 * @param character
	 * @param noApCheck - if true, actions points of the character are not considered
	 * @return
	 */
	public boolean canBeActivated(GameCharacter character, boolean noApCheck) {
		if (!isActivated() || !canBeLearned(character)) {
			return false;
		}
		
		if ((isCombatOnly() || isAttack()) && (character.getMap() == null || character.getMap().isWorldMap())) { 
			return false;
		}
		
		Stats characterStats = character.stats();

		if (getSpCost() > 0 && characterStats.getSPAct() < getSpCost()) {
			return false;
		}
		
		if (!noApCheck && GameState.isCombatInProgress() && getApCost(character) > 0 && characterStats.getAPAct() < getApCost(character)) {
			return false;
		}
		
		if (getMpCost() > 0 && characterStats.getMPAct() < getMpCost()) {
			return false;
		}
		
		if (s_activationRequirements != null) {
			return s_activationRequirements.execute(character, new Binding());
		}
		
		return true;
	}

	@Override
	public void setTargetType(String targetType, Script targetScript) {
		this.targetType = targetType;
		this.targetScript = targetScript;
	}

	@Override
	public void onProjectileHit(Projectile projectile, GameObject user, ProjectileTarget target) {
		if (s_automaticOnHitAnimation) {
			target.onHit(projectile, user);
		}
		for (Effect effect : s_effects.keys()) {
			effect.executeEffect(this, user, target, s_effects.get(effect));
		}
	}
	
	public String getAnimationState() {
		return s_animationState;
	}

	public String getIconFile() {
		return s_iconFile;
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		if (s_iconFile != null) {
			assetStore.put(s_iconFile, Texture.class);
		}
	}
	
	@Override
	public void clearAssetReferences() {
	}

	public int getRank() {
		return s_rank;
	}

	public void setRank(int rank) {
		s_rank = rank;
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
		if (root.getName().toLowerCase(Locale.ENGLISH).startsWith("activated")) {
			s_isActivated = true;
		}
		
		XMLUtil.readPrimitiveMembers(this, root);
		if (s_iconFile != null) {
			s_iconFile = Configuration.addModulePath(s_iconFile);
		}
		XMLUtil.readModifiers(this, root.getChildByName(XMLUtil.XML_MODIFIERS));
		XMLUtil.readEffect(this, root.getChildByName(XMLUtil.XML_EFFECTS));
		XMLUtil.readTargetType(this, root.getChildByName(XMLUtil.XML_TARGET));
		String projectileValue = root.get(XMLUtil.XML_PROJECTILE,null);
		if (projectileValue != null) {
			projectile = projectileValue;
		}
		
		if (root.getChildByName(XML_LEARN_REQUIREMENTS) != null) { 
			s_learnRequirements = Condition.getCondition(root.getChildByName(XML_LEARN_REQUIREMENTS).getChild(0));
		}
		if (root.getChildByName(XML_ACTIVATION_REQUIREMENTS) != null) {
			s_activationRequirements = Condition.getCondition(root.getChildByName(XML_ACTIVATION_REQUIREMENTS).getChild(0));
		}
	}
}
