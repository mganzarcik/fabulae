package mg.fishchicken.core.configuration;

import java.util.Iterator;

import mg.fishchicken.gamelogic.inventory.items.Armor.ArmorClass;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;

import com.badlogic.gdx.utils.Array;

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
