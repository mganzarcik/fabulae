package mg.fishchicken.core.projectiles;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.Effect.PersistentEffect;
import mg.fishchicken.gamelogic.effects.EffectContainer;

import com.badlogic.gdx.utils.Array;

public interface ProjectileTarget {

	/**
	 * Size of the target. This will be used
	 * to scale any area effect graphics the effect targeting
	 * this target might have.
	 * @return
	 */
	public float getSize();
	
	/**
	 * Gets the x coordinate of the target.
	 * 
	 * In case of area targets, this is usually the center.
	 * 
	 * @return
	 */
	public float getTargetX();
	
	/**
	 * Gets the y coordinate of the target.
	 * 
	 * In case of area targets, this is usually the center.
	 * 
	 * @return
	 */
	public float getTargetY();
	
	/**
	 * Gets all GameObjects that belong to this target.
	 * 
	 * @return
	 */
	public GameObject[] getGameObjects();
	
	/**
	 * This executes when the target gets hit by a
	 * Projectile.
	 * 
	 * @param projectile
	 */
	public void onHit(Projectile projectile, GameObject originator);
	
	/**
	 * Adds a new persistent effect build from the supplied effect
	 * and with the supplied duration to this target. The user
	 * is the the effect's originator.
	 * 
	 * @param pe
	 */
	public void addPersistentEffect(EffectContainer container, Effect effect, float duration, GameObject user);
	
	/**
	 * Removes all persistent effects with the supplied id from
	 * this target.
	 * 
	 * @param id
	 */
	public void removePersitentEffect(String id);
	
	/**
	 * Returns all persistent effects of the supplied type
	 * that currently belong to this target. If no type is specified,
	 * all active persistent effects are returned.
	 * 
	 * @param type
	 * @return
	 */
	public Array<PersistentEffect> getPersistentEffectsByType(String... types);
	
	
	/**
	 * Filters out game objects that belong to this target but are
	 * not actually viable for the supplied effect. 
	 * 
	 * @param effectContainer
	 * @param user
	 * @return true if any viable targets remained after the filtering
	 */
	public boolean filterUnviableTargets(Effect effect, EffectContainer effectContainer, GameObject user);
	
}
