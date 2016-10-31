package mg.fishchicken.gamelogic.inventory.items;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.conditions.Condition.ConditionResult;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.Targetable;
import mg.fishchicken.core.projectiles.OnProjectileHitCallback;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.projectiles.ProjectileTarget;
import mg.fishchicken.core.projectiles.ProjectileType;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.effects.EffectParameter;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.effects.targets.TargetTypeContainer;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.XmlReader.Element;

public class UsableItem extends InventoryItem implements EffectContainer, TargetTypeContainer, Targetable, OnProjectileHitCallback {

	private int s_maxUses;
	private int usesLeft;
	private OrderedMap<Effect, Array<EffectParameter>> effects;
	private String targetType;
	private Script targetScript;
	private String s_projectile;
	private Condition useCondition;
	private boolean s_combatOnly;

	public UsableItem() {
		super();
	}
	
	public UsableItem(FileHandle file) throws IOException {
		super(file);
	}
	
	@Override
	protected Array<ConditionResult> canBeAddedToQuickUse(
			InventoryContainer container) {
		return null;
	}
	
	@Override
	public Array<ConditionResult> canBeUsedBy(InventoryContainer ic) {
		Array<ConditionResult> result = new Array<ConditionResult>();
		if (useCondition != null) {
			result.addAll(useCondition.evaluateWithDetails(ic,
					new Binding()));
		}
		if (ic instanceof GameCharacter) {
			GameCharacter character = (GameCharacter) ic;
			result.add(new ConditionResult(Strings.getString(
					Condition.STRING_TABLE, "combatOnly"), !isCombatOnly()
					|| (character.getMap() != null && !character.getMap()
							.isWorldMap())));
			result.add(new ConditionResult(Strings.getString(
					Condition.STRING_TABLE, "apRequired",
					Configuration.getAPCostUseItem()), !GameState
					.isCombatInProgress()
					|| character.stats().getAPAct() >= Configuration
							.getAPCostUseItem()));
		}
		return result;
	}

	/**
	 * Evaluates the use requirements for this item for the supplied character
	 * and returns an array that will contain the result for each condition
	 * associated with the item.
	 * 
	 * @return null if there are no requirements, the array instead
	 */
	public Array<ConditionResult> evaluateUseRequirements(GameCharacter user) {
		if (useCondition != null) {
			return useCondition.evaluateWithDetails(user, new Binding());
		}
		return null;
	}
	
	/**
	 * Uses this item.
	 * 
	 * Returns true if the item ran out of all uses
	 * and should be destroyed.
	 * 
	 * @param user
	 * @param target
	 * @return
	 */
	public boolean use(GameObject user, TargetType target) {
		if (usesLeft > 0) {
			--usesLeft;
			if (!target.requiresTargeting() || s_projectile == null || user.getMap() == null) {
				onProjectileHit(null, user, target);
			} else {
				new Projectile(ProjectileType.getType(s_projectile), user, target, this);
			}
		}
		if (usesLeft < 1) {
			return true;
		}
		return false;
	}
	
	public TargetType getTargetTypeInstance(GameCharacter user) {
		if (targetType != null) {
			TargetType target = TargetType.getTargetTypeInstance(targetType, targetScript);
			target.executeTargetScript(user, null);
			return target;
		}
		throw new GdxRuntimeException("Could not determine EffectTarget for usable "+getId());
	}
	
	@Override
	public void addEffect(Effect effect,
			Array<EffectParameter> effectParameters) {
		effects.put(effect, effectParameters);
	}
	
	@Override
	public  ObjectMap<Effect, Array<EffectParameter>> getEffects() {
		return effects;
	}
	
	public int getUsesLeft() {
		return usesLeft;
	}
	
	public int getMaxUses() {
		return s_maxUses;
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		effects = new OrderedMap<Effect, Array<EffectParameter>>();
		super.loadFromXML(file);
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		if (s_maxUses == 0) {
			s_maxUses = 1;
		}
		usesLeft = s_maxUses;
		String projectileValue =root.get(XMLUtil.XML_PROJECTILE,null); 
		if (projectileValue != null) {
			s_projectile = projectileValue;
		}
		Element conditionElement = root.getChildByName(XMLUtil.XML_CONDITION);
		if (conditionElement != null && conditionElement.getChildCount() > 0) {
			useCondition = Condition.getCondition(conditionElement.getChild(0));
		}
		XMLUtil.readEffect(this, root.getChildByName(XMLUtil.XML_EFFECTS));
		XMLUtil.readTargetType(this, root.getChildByName(XMLUtil.XML_TARGET));
	}
	
	@Override
	public InventoryItem createNewInstance() {
		UsableItem returnValue = (UsableItem) super.createNewInstance();
		returnValue.usesLeft = s_maxUses;
		returnValue.useCondition = useCondition;
		returnValue.targetType = targetType;
		returnValue.targetScript = targetScript;
		returnValue.effects = new OrderedMap<Effect, Array<EffectParameter>>();
		for (Effect key : effects.keys()) {
			Array<EffectParameter> originalParams = effects.get(key);
			Array<EffectParameter> newCopyParam = new Array<EffectParameter>();
			for (EffectParameter param : originalParams) {
				newCopyParam.add(param);
			}
			returnValue.effects.put(key, newCopyParam);
		}
	return returnValue;
	}

	@Override
	public void setTargetType(String targetType, Script targetScript) {
		this.targetType = targetType;
		this.targetScript = targetScript;
	}
	
	@Override
	public void onProjectileHit(Projectile projectile, GameObject user, ProjectileTarget target) {
		target.onHit(projectile, user);
		for (Effect effect : effects.keys()) {
			effect.executeEffect(this, user, target, effects.get(effect));
			
		}
	}

	public boolean isCombatOnly() {
		return s_combatOnly;
	}

	public void setCombatOnly(boolean s_combatOnly) {
		this.s_combatOnly = s_combatOnly;
	}
}
