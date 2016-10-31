package mg.fishchicken.gamelogic.modifiers;

import java.util.Iterator;

public interface ModifierContainer {
	
	public String getName();
	public void addModifier(Modifier modifier);
	public Iterator<Modifier> getModifiers();
	
	/**
	 * This should be called whenever a modifier belonging to this MC
	 * is changed.
	 */
	public void onModifierChange();
}
