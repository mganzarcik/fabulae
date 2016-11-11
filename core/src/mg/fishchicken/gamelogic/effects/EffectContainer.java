package mg.fishchicken.gamelogic.effects;

import mg.fishchicken.core.ThingWithId;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public interface EffectContainer extends ThingWithId {

	/**
	 * Returns the unlocalized name of this effect container. It may
	 * be human readable, or in a stringTable#key format.
	 * 
	 * @return
	 */
	public String getRawName();
	
	/**
	 * Adds a new effect to this container.
	 * 
	 * @param effect
	 * @param effectParameters
	 */
	public void addEffect(Effect effect, Array<EffectParameter> effectParameters);
	
	/**
	 *Returns all effects and their parameters of this EC.
	 * 
	 * @return
	 */
	public ObjectMap<Effect, Array<EffectParameter>> getEffects();
}
