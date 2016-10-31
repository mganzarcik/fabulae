package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.projectiles.OnProjectileHitCallback;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.projectiles.ProjectileTarget;
import mg.fishchicken.core.projectiles.ProjectileType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter.State;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.combat.CombatManager;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.characters.Stats;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the supplied character attack the other supplied character.
 * 
 * Based on the weapons equipped, the attacker will either move
 * to the target and then attack using melee, or he will move to
 * range and use a ranged weapon.
 * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>targetNpc - NonPlayerCharacter - the NPC to attack
 * </ol>
 * @author Annun
 *
 */
public class AttackAction extends MoveToAction implements OnProjectileHitCallback {
	
	protected GameCharacter character;
	protected GameCharacter target;
	protected boolean attackFinished;
	protected Array<Weapon> attackingWeapons = new Array<Weapon>();
	protected int attackedWeaponCount;
	
	private Stats stats;
	private String targetId;
	private Tile targetPosition;
	private boolean startedAttack;
	private boolean actionFinished;
	private boolean hasAttacked;
	private boolean rangedAttack;
	private int totalAPCost;
	private ObjectMap<Projectile, Weapon> projetileWeaponMap = new ObjectMap<Projectile, Weapon>();
	
	public AttackAction() {
	}

	public AttackAction(GameCharacter attacker, GameCharacter target) {
		init(attacker, target);
	}
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("AttackAction only works on GameCharacter!");
		}
		startedAttack = false;
		actionFinished = false;
		attackFinished = false;
		hasAttacked = false;
		rangedAttack = false;
		attackedWeaponCount = 0;
		attackingWeapons.clear();
		projetileWeaponMap.clear();
		character = (GameCharacter) ac;
		stats = character.stats();
		targetPosition = null;
		if (parameters.length == 1) {
			this.target = (GameCharacter)parameters[0];
			targetId = target.getInternalId();
			target.pauseAllActions();
			targetPosition = target.position().tile();
			super.init(ac, targetPosition.getX(), targetPosition.getY());
		} else if (parameters.length == 2) { 
			// if we had two params supplied, we assume they are coordinates and we degrade to a simple MoveToAction
			super.init(ac, (Integer)parameters[0], (Integer)parameters[1]);
		}
		if (target != null) {
			rangedAttack = determineAttackingWeapons(character, targetPosition.getX(), targetPosition.getY(), attackingWeapons);
			// check if we cannot perform the attack at all - if we cannot, we are done immediately
			if (isNextToTarget()
					&& rangedAttack
					|| ((!isNextToTarget() && rangedAttack) || (isNextToTarget() && !rangedAttack)) && getAPCostToAttack() > stats.getAPAct()) {
				actionFinished = true;
				attackFinished = true;
			}
		}
	}

	/**
	 * Returns whether or not the attack of the supplied tile coordinates by the
	 * supplied character will be ranged.
	 * 
	 * @param attacker
	 * @param targetX
	 * @param targetY
	 * 
	 * @return true if the attack will be ranged, false otherwise
	 */
	public static boolean isRangedAttack(GameCharacter attacker, int targetX, int targetY) {
		return determineAttackingWeapons(attacker, targetX, targetY, null);
	}
	
	/**
	 * Determines which weapons will the attacker use when attacking the supplied target tile from the attacker's current position.
	 * 
	 * It returns whether or not the attack will be ranged.
	 * 
	 * @param attacker
	 * @param targetX
	 * @param targetY
	 * 
	 * @return true if the attack will be ranged, false otherwise
	 */
	public static boolean determineAttackingWeapons(GameCharacter attacker, int targetX, int targetY, Array<Weapon> attackingWeapons) {
		Tile tile = attacker.position().tile();
		return determineAttackingWeapons(attacker, tile.getX(), tile.getY(), targetX, targetY, attackingWeapons);
	}
	
	/**
	 * Determines which weapons will the attacker use when attacking the supplied target tile from the supplied from tile.
	 * 
	 * It returns whether or not the attack will be ranged.
	 * 
	 * @param attacker
	 * @param fromX
	 * @param fromY
	 * @param targetX
	 * @param targetY
	 * 
	 * @return true if the attack will be ranged, false otherwise
	 */
	public static boolean determineAttackingWeapons(GameCharacter attacker, int fromX, int fromY, int targetX, int targetY, Array<Weapon> attackingWeapons) {
		boolean returnValue = false;
		Inventory intentory = attacker.getInventory();
		InventoryItem rightEquipped = intentory.getEquipped(ItemSlot.RIGHTHAND);
		InventoryItem leftEquipped = intentory.getEquipped(ItemSlot.LEFTHAND);
		if (attackingWeapons != null) {
			attackingWeapons.clear();
		}
		
		if (Vector2.dst(fromX, fromY, targetX, targetY) < 2) {
			returnValue = true;
			if (rightEquipped instanceof Weapon && !((Weapon)rightEquipped).isRanged()) {
				if (attackingWeapons != null) {
					attackingWeapons.add((Weapon)rightEquipped);
				}
				returnValue = false;
			} 
			
			if (leftEquipped instanceof Weapon && !((Weapon)leftEquipped).isRanged()) {
				if (attackingWeapons != null) {
					attackingWeapons.add((Weapon)leftEquipped);
				}
				returnValue = false;
			} 
			
			if (!(rightEquipped instanceof Weapon) && !(leftEquipped instanceof Weapon)) {
				returnValue = false;
			}
		} else {
			if (rightEquipped instanceof Weapon && ((Weapon)rightEquipped).isRanged()) {
				if (attackingWeapons != null) {
					attackingWeapons.add((Weapon)rightEquipped);
				}
				returnValue = true;
			}
			
			if (leftEquipped instanceof Weapon && ((Weapon)leftEquipped).isRanged()) {
				if (attackingWeapons != null) {
					attackingWeapons.add((Weapon)leftEquipped);
				}
				returnValue = true;
			}
		}
		
		return returnValue;	
	}

	/**
	 * Calculates the random amount of damage the attacker would deal
	 * to the target using the supplied weapon.
	 * 
	 * This takes into account all the modifiers when determining damage.
	 * 
	 * If ignoreTargetAR is set to true, target's armor rating
	 * will not affect the damage.
	 * 
	 * @param weapon
	 * @param attacker
	 * @param target
	 * @param ignoreTargetAR
	 * @return
	 */
	public static float calculateDamage(Weapon weapon, GameCharacter attacker, GameCharacter target) {
		
		float damage;
		boolean unarmed = weapon == null;
		if (unarmed) {
			int unarmedRank = attacker.stats().skills().getSkillRank(Skill.UNARMED); 
			damage = 1 + unarmedRank/ 2
					+ (unarmedRank > 0 ? GameState.getRandomGenerator().nextInt(unarmedRank) : 0);
		} else {
			// base damage
			int damMax = weapon.getWeaponDamageMax();
			int damMin = weapon.getWeaponDamageMin();
			damage = (weapon.getWeaponBonus() + attacker.stats().skills().getSkillRank(weapon.getWeaponSkill())
					+ damMin + (damMax - damMin > 0 ? GameState.getRandomGenerator().nextInt(damMax - damMin) : 0));
		}
		// apply character modifiers
		damage = attacker.stats().applyDamageModifiers(damage, unarmed);

		// reduce by targets armor rating
		damage = ((float) damage / 100f) * (100 - (float) target.stats().getAR());

		if (damage < 1) {
			damage = 1;
		}

		return damage;
	}
	
	@Override
	public void onRemove(ActionsContainer ac) {
		super.onRemove(ac);
		if (target != null) {
			target.resumeAllActions();
		}
	}
	
	@Override
	public void update(float deltaTime) {
		if (target == null && targetId != null) {
			this.target = (GameCharacter)GameState.getGameObjectByInternalId(targetId);
			if (target == null) {
				this.target = (GameCharacter)GameState.getGameObjectById(targetId);
			}
			target.pauseAllActions();
			targetPosition = target.position().tile();
			super.init(character,targetPosition.getX(), targetPosition.getY());
		}
		
		super.update(deltaTime);
		if(super.isFinished() && target != null && (isNextToTarget() || rangedAttack)) {
			if (!startedAttack) {
				startAttack();
			} else {
				if (character.hasAtackAnimationHit() && !hasAttacked) {
					hasAttacked = true;
					for (Weapon weapon : attackingWeapons) {
						String projectileId = weapon.getProjectile();
						if (projectileId == null) {
							attackTargetWith(weapon);
						} else {
							projetileWeaponMap.put(new Projectile(ProjectileType.getType(projectileId), character, target, this), weapon);
						}
					}
					if (canAttackWithFists()) {
						attackTargetWith(null);
					}
				}
				if (character.isAnimationFinished() && attackFinished) {
					actionFinished = true;
					stats.addToAP(-totalAPCost);
					character.setState(State.IDLE);
				}
			}
		}
	}
	
	private void startAttack() {
		// determine weapons once more, since our position might have changed since init
		determineAttackingWeapons(character, targetPosition.getX(), targetPosition.getY(), attackingWeapons);
		totalAPCost = getAPCostToAttack();
		if ((character.
				stats().getAPAct() >= totalAPCost)
				&& (attackingWeapons.size > 0 || canAttackWithFists())) {
			startedAttack = true;
			character.setIsSneaking(false);
			character.setOrientation(Orientation
					.calculateOrientationToTarget(character, target));
			if (rangedAttack) {
				character.setState(State.ATTACKRANGED);
			} else {
				character.setState(State.ATTACKMELEE);
			}
		} else {
			startedAttack = true;
			actionFinished = true;
		}
	}
	
	protected int getAPCostToAttack() {
		return GameState.isCombatInProgress() ? stats.getAPCostToAttack() : 0;
	}
	
	private boolean canAttackWithFists() {
		Inventory inventory = character.getInventory();
		InventoryItem rightEquipped = inventory.getEquipped(ItemSlot.RIGHTHAND);
		InventoryItem leftEquipped = inventory.getEquipped(ItemSlot.LEFTHAND);
		
		// we can only attack with fists if we have no weapon equipped and are next to the target
		return !(rightEquipped instanceof Weapon) && !(leftEquipped instanceof Weapon)  && isNextToTarget();
	}
	private boolean isNextToTarget() {
		return MathUtil.isNextToOrOnTarget(character, target);
	}
	
	/**
	 * Attacks the target with the supplied weapon.
	 * 
	 * @param weapon
	 * @param cth
	 * @return true if the attack was a hit, false otherwise
	 */
	protected boolean attackTargetWith(Weapon weapon) {
		int cth = getChanceToHit(character, target, weapon);
		int roll = GameState.getRandomGenerator().nextInt(100);
		boolean hit = roll < cth;

		String weaponName = weapon != null ? weapon.getName() : "fists";
		
		Log.logLocalized("AttackRoll", LogType.COMBAT, character.getName(), cth, target.getName(),weaponName, roll,hit? Strings.getString(CombatManager.STRING_TABLE, "hit") : Strings.getString(CombatManager.STRING_TABLE, "miss"));
		
		if (hit) {
			// ((Weapon Damage  + Weapon Bonus + Weapon Skill) / 100) * Enemy Armor Rating
			float damage = calculateDamage(weapon, character, target);
			
			Log.logLocalized("attackHit",LogType.COMBAT, character.getName(), target.getName(), MathUtil.toUIString(damage),weaponName);
			
			target.dealDamage(damage, character);
			if (weapon != null) {
				weapon.executeEffects(character, target);
			}
			
			target.onAttack(character);
		}
		if (++attackedWeaponCount >= attackingWeapons.size) {
			attackFinished = true;
		}
		return hit;
	}
	
	/**
	 * Gets the chance to hit (in percent) for the supplied attacker against the supplied defender
	 * if the attacker was using the supplied weapon. It is assumed the attacker attacks from his
	 * current position.
	 * 
	 * @param attacker
	 * @param defender
	 * @param attackingWeapon
	 * @return
	 */
	public static int getChanceToHit(GameCharacter attacker, GameCharacter defender, Weapon attackingWeapon) {
		Tile from = attacker.position().tile();
		return getChanceToHit(attacker, defender, attackingWeapon, from.getX(), from.getY());
	}
	
	/**
	 * Gets the chance to hit (in percent) for the supplied attacker against the supplied defender
	 * if the attacker was using the supplied weapon. It is assumed the attacker attacks from 
	 * the supplied position.
	 * 
	 * @param attacker
	 * @param defender
	 * @param attackingWeapon
	 * @param fromX
	 * @param fromY
	 * @return
	 */
	public static int getChanceToHit(GameCharacter attacker, GameCharacter defender, Weapon attackingWeapon, int fromX, int fromY) {
		Orientation targetOrientation = defender.getOrientation();
		Tile to = defender.position().tile();
		Orientation attackerOrientation = Orientation.calculateOrientationToTarget(defender.getMap().isIsometric(), fromX, fromY, to.getX(), to.getY());
		Stats stats = attacker.stats();
		int bonus = 0;
		int cth = attackingWeapon != null ? stats.getChanceToHit(attackingWeapon.getSlot()) : stats.getChanceToHit(ItemSlot.RIGHTHAND);
		if (attackingWeapon == null || !attackingWeapon.isRanged()) {
			if (targetOrientation == attackerOrientation) {
				bonus = Configuration.getCtHBonusBack();
			} else if (targetOrientation.getOpposite() != attackerOrientation && 
					targetOrientation.getClockwise().getOpposite() != attackerOrientation &&
					targetOrientation.getAntiClockwise().getOpposite() != attackerOrientation) {
				bonus = Configuration.getCtHBonusSide();
			}
		}
		int ctdop = defender.stats().getDodgeOrParryChance(attackingWeapon);
		return MathUtil.boxValue(cth + bonus - ctdop, 1, 99);
	}
	
	@Override
	public boolean noMoreSteps() {
		if (target != null && ((rangedAttack && character.canSeeTile(targetPosition.getX(), targetPosition.getY())) || (isNextToTarget()))) {
			return true;
		}
		return super.noMoreSteps();
	}
	
	@Override
	public boolean isFinished() {
		return super.isFinished()
				&& (target == null || actionFinished || (!isNextToTarget() && !rangedAttack) || (rangedAttack && !character
						.canSeeTile(targetPosition.getX(), targetPosition.getY())));
	}

	@Override
	public void onProjectileHit(Projectile projectile, GameObject user, ProjectileTarget target) {
		if (projetileWeaponMap.containsKey(projectile)) {
			Weapon weapon = projetileWeaponMap.get(projectile);
			if (!attackTargetWith(weapon) && projectile != null) {
				projectile.kill();
			}
		}
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		targetId = actionElement.getAttribute(XML_ATTRIBUTE_TARGET, null);
		if (targetId == null) {
			throw new GdxRuntimeException("target must be specified!");
		}
	}
	
	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_TARGET, targetId);
	}
}
