package mg.fishchicken.gamelogic.actions;

import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.items.Weapon;

/**
 * Makes the supplied character pretend attack the other supplied character.
 * 
 * Based on the weapons equipped, the attacker will either move
 * to the target and then attack using melee, or he will move to
 * range and use a ranged weapon. In both cases, no damage will be dealt,
 * no crime committed and nothing will be logged.
 * 
 * This actions always also costs 0 ap.
 * <br /><br />
 * Parameters:
 * <ol>
 * 	<li>targetNpc - NonPlayerCharacter - the NPC to attack
 * </ol>
 * @author Annun
 *
 */
public class MockAttackAction extends AttackAction {
	
	public MockAttackAction() {
	}

	public MockAttackAction(GameCharacter attacker, GameCharacter target) {
		super(attacker, target);
	}
	
	protected int getAPCostToAttack() {
		return 0;
	}
	
	@Override
	protected boolean shouldStartCombat() {
		return false;
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

		if (hit) {
			target.onAttack(character);
		}
		if (++attackedWeaponCount >= attackingWeapons.size) {
			attackFinished = true;
		}
		return hit;
	}
}
