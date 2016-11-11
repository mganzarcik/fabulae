package mg.fishchicken.core.configuration;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;

import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;

public class ArmorModifiers implements ModifierContainer {

	private Array<Modifier> modifiers;
	
	public ArmorModifiers() {
		this.modifiers = new Array<Modifier>();
	}
	
	@Override
	public String getName() {
		return "";
	}

	@Override
	public void addModifier(Modifier modifier) {
		modifiers.add(modifier);
	}

	@Override
	public Iterator<Modifier> getModifiers() {
		return modifiers.iterator();
	}

	@Override
	public void onModifierChange() {
		
	}

}
