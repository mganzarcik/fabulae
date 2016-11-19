package mg.fishchicken.gamelogic.actions;

import java.io.IOException;
import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter.State;
import mg.fishchicken.gamelogic.characters.CharacterFilter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.SkillCheckModifier;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.effects.targets.Single;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamestate.GameObjectPosition;
import mg.fishchicken.gamestate.characters.Stats;

/**
 * Makes the supplied GameCharacter use the supplied Perk.
 * <br><br>
 * XML Parameters:
 * <ol>
 * 		<li> id - the id of the perk to use
 * 		<li> x - x-coordinate of the target
 * 		<li> y - y-coordinate of the target
 * </ol>
 * <br><br>
 * Code parameters:
 * <ol>
 * 		<li> perk - mg.fishchicken.gamelogic.characters.perks.Perk - perk to use
 * 		<li> target - mg.fishchicken.gamelogic.effects.targets.TargetType - the target
 * 		<li> useForNoAp - boolean - whether the perk should be used for no AP cost (optional, default false)
 * </ol>
 * @author ANNUN
 *
 */
public class UsePerkAction extends AttackAction implements SkillCheckModifier  {

	protected String perkId;
	protected Perk perk;
	protected GameCharacter user;
	private boolean isFinished;
	private Array<Modifier> appliedModifiers;
	private Array<GameObject> targets;
	private Array<GameCharacter> alreadyExecuted;
	private TargetType effectTarget;
	private boolean perkUseInProgress;
	private GameCharacter currentTarget;
	protected boolean useForNoAP;
	private float targetX, targetY;
	private boolean moveToInitiated;
	
	public UsePerkAction() {
	}

	public UsePerkAction(GameCharacter user, Perk perk, TargetType target) {
		init(user, perk);
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("UsePerkAction only works on GameCharacter!");
		}
		if (appliedModifiers == null) {
			appliedModifiers = new Array<Modifier>();
		} else {
			appliedModifiers.clear();
		}
		
		if (targets == null) {
			targets = new Array<GameObject>();
		} else {
			targets.clear();
		}
		
		if (alreadyExecuted == null) {
			alreadyExecuted = new Array<GameCharacter>();
		} else {
			alreadyExecuted.clear();
		}
		
		perkId = null;
		perk = null;
		effectTarget = null;
		currentTarget = null;
		perkUseInProgress = false;
		moveToInitiated = false;
		user = (GameCharacter) ac;
		isFinished = false;
		useForNoAP = false;
		if (parameters.length >= 2) {
			perk = (Perk) parameters[0];
			perkId = perk.getId();
			effectTarget = (TargetType) parameters[1];
			if (perk.isAttack()) {
				targets.addAll(effectTarget.getGameObjects());
			}
			if (parameters.length == 3) {
				useForNoAP = (Boolean)parameters[2];
			}
			// don't do anything for passive perks
			if (!perk.isActivated()) {
				isFinished = true;
				logCannotUsePassiveError();
			} else if (!perk.canBeActivated(user, useForNoAP)) {
				isFinished = true;
				logCannotUseActivatedError();
			}
		}
	}
	
	protected void logCannotUsePassiveError() {
		Log.logLocalized("cannotUsePassivePerk", LogType.COMBAT, perk.getName(), user.getName());
	}
	
	protected void logCannotUseActivatedError() {
		Log.logLocalized("cannotUseActivatedPerk",LogType.COMBAT, perk.getName(), user.getName(),user.stats().getGender().getPronoun().toLowerCase());
	}
	
	@Override
	public void update(float deltaTime) {
		if (perk == null && perkId != null) {
			// TODO: this probably needs fixing for world maps?
			perk = getPerk(perkId);
			effectTarget = perk.getTargetTypeInstance(user);
			effectTarget.setTarget((int)targetX, (int)targetY, user.getMap());
			if (perk.isAttack()) {
				targets.addAll(effectTarget.getGameObjects());
			}
		}
		if (!isFinished) {
			// handle activated perks that are not attacks
			if (!perk.isAttack()) {
				if (!perkUseInProgress) {
					if (user.getMap() == null || user.canSeeTile((int)effectTarget.getTargetX(), (int)effectTarget.getTargetY())) {
						useSpellLikePerk();
					} else {
						moveTo(deltaTime);
					}
				} else {
					if (user.isAnimationFinished()) {
						user.setState(State.IDLE);
						isFinished = true;
					}
				}
			// handle activated attack perks
			} else {
				useAttackPerk(deltaTime);
			}
		}
	}
	
	private void useAttackPerk(float deltaTime) {
		if (targets.size > 0 || currentTarget != null) {
			if (currentTarget == null) {
				Log.logLocalized("usedPerk", Log.LogType.COMBAT, user.getName(), perk.getName());
				GameObject target = targets.pop();
				while (!(target instanceof GameCharacter) && targets.size > 0) {
					target = targets.pop();
				}
				if (target instanceof GameCharacter) {
					currentTarget = (GameCharacter) target;
				}
				if (currentTarget != null) {
					super.init(user, currentTarget);
				} else {
					isFinished = true;
				}
			} else {
				if (!super.isFinished()) {
					super.update(deltaTime);
				} else {
					currentTarget = null;
				}
			}
		} else {
			if (GameState.isCombatInProgress()) {
				applyCost();
			}
			isFinished = true;
		}
	}
	
	private void moveTo(float deltaTime) {
		if (!moveToInitiated) {
			super.init(user, (int)effectTarget.getTargetX(), (int)effectTarget.getTargetY());
			moveToInitiated = true;
		} else {
			if (!super.isFinished()) {
				super.update(deltaTime);
			} else {
				isFinished = true;
			}
		}
	}
	
	/**
	 * Returns the id of the perk currently cast by this action.
	 * @return
	 */
	public String getPerkId() {
		return perkId;
	}
	
	protected Perk getPerk(String id) {
		return Perk.getPerk(id);
	}
	
	@Override
	public boolean noMoreSteps() {
		if (perk.isAttack()) {
			return super.noMoreSteps();
		} else {
			if (user.canSeeTile((int)effectTarget.getTargetX(), (int)effectTarget.getTargetY())) {
				return true;
			} else {
				return super.noMoreSteps();
			}
		}
	}
	
	
	@Override
	protected int getAPCostToAttack() {
		// return zero here, we will take care of AP cost in the attackTargetWith method
		return 0;
	}
	
	@Override
	protected boolean attackTargetWith(Weapon weapon) {
		applyPerkModifiers();
		boolean hit = super.attackTargetWith(weapon);
		removeAppliedModifiers();
		if (hit && !alreadyExecuted.contains(currentTarget, false)) {
			perk.executeEffects(user, new Single(currentTarget));
			alreadyExecuted.add(currentTarget);
		}
		return hit;
	}
	
	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	private void useSpellLikePerk() {
		Log.logLocalized("usedPerk", Log.LogType.COMBAT, user.getName(), perk.getName());
		
		if (user.getMap() != null) {
			GameObjectPosition userPosition = user.position();
			if (!userPosition.tile().equals((int)effectTarget.getTargetX(), (int)effectTarget.getTargetY())) {
				user.setOrientation(Orientation
						.calculateOrientationToTarget(user.getMap().isIsometric(),
								userPosition.getX(), userPosition.getY(),
								effectTarget.getTargetX(), effectTarget.getTargetY()));
			}
		}
		
		if (perk.getAnimationState() != null) {
			user.setState(perk.getAnimationState());
		} else {
			user.setState(State.CAST);
		}
		perk.executeEffects(user, effectTarget);
		if (GameState.isCombatInProgress()) {
			applyCost();
		}
		perkUseInProgress = true;
		if (user.isSneaking() 
				&& user.getAllCharactersInSightRadius(null, CharacterFilter.NOT_SAME_FACTION, CharacterFilter.AWAKE)
				&& !user.stats().rollSkillCheck(Skill.SNEAKING, this)) {
			user.setIsSneaking(false);
		}
	}
	
	private void applyPerkModifiers() {
		Iterator<Modifier> modifiers = perk.getModifiers();
		Stats stats = user.stats();
		while (modifiers.hasNext()) {
			Modifier mod = modifiers.next().copy();
			appliedModifiers.add(mod);
			stats.addModifier(mod);
		}
	}
	
	private void removeAppliedModifiers() {
		Stats stats = user.stats();
		for (Modifier mod : appliedModifiers) {
			stats.removeModifier(mod);
		}
		appliedModifiers.clear();
	}
	
	protected void applyCost() {
		Stats stats = user.stats();
		if (!useForNoAP) {
			stats.addToAP(-perk.getApCost(user));
		}
		stats.addToHP(-perk.getHpCost());
		stats.addToSP(-perk.getSpCost());
		stats.addToMP(-perk.getMpCost());
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		perkId = actionElement.getAttribute(XMLUtil.XML_ATTRIBUTE_ID, null);
		if (perkId == null) {
			throw new GdxRuntimeException("id must be specified!");
		}
		try {
			targetX = actionElement.getFloatAttribute(XML_ATTRIBUTE_X);
		} catch (GdxRuntimeException e) {
			throw new GdxRuntimeException("x must be specified!");
		}
		try {
			targetY = actionElement.getFloatAttribute(XML_ATTRIBUTE_Y);
		} catch (GdxRuntimeException e) {
			throw new GdxRuntimeException("x must be specified!");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XMLUtil.XML_ATTRIBUTE_ID, perkId);
		if (effectTarget != null) {
			writer.attribute(XML_ATTRIBUTE_X, effectTarget.getTargetX());
			writer.attribute(XML_ATTRIBUTE_Y, effectTarget.getTargetY());
		}
	}

	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		if (Skill.SNEAKING == skill) {
			return Configuration.getUsePerkStealthModifier() + skillUser.getMap().getSkillCheckModifier(skill, skillUser);
		}
		return skillUser.getMap().getSkillCheckModifier(skill, skillUser);
	}
		
}
