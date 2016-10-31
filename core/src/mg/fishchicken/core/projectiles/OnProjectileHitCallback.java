package mg.fishchicken.core.projectiles;

import mg.fishchicken.core.GameObject;


public interface OnProjectileHitCallback {
	
	/**
	 * Called when the supplied target gets hit with the supplied projectile.
	 * 
	 * @param projectile
	 * @param target
	 * @param context
	 */
	public abstract void onProjectileHit(Projectile projectile, GameObject originator, ProjectileTarget target);
}
