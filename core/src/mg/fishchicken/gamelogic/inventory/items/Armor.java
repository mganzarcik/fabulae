package mg.fishchicken.gamelogic.inventory.items;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamelogic.modifiers.Modifier;

import com.badlogic.gdx.files.FileHandle;

public class Armor extends InventoryItem {

	public static enum ArmorClass { 
		NONE, LIGHT, MEDIUM, HEAVY
	};
	
	private int s_armorRating;
	
	public Armor() {
		super();
	}
	
	public Armor(FileHandle file) throws IOException {
		super(file);
	}
	
	@Override
	public int canBeAddedTo(BagType bag, int slot, InventoryContainer container) {
		if (BagType.EQUIPPED == bag && !canBeEquippedDuringCombat() && GameState.isCombatInProgress()) {
			return 0;
		}
		
		return super.canBeAddedTo(bag, slot, container);
	}
	
	@Override
	public boolean canBeUnequipped(int slot, InventoryContainer container) {
		if (!canBeEquippedDuringCombat() && GameState.isCombatInProgress()) {
			return false;
		}
		return true;
	}
	
	protected boolean canBeEquippedDuringCombat() {
		return false;
	}

	public int getAmorRating() {
		return s_armorRating;
	}

	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		super.loadFromXML(file);
		Modifier arModifier = new Modifier();
		arModifier.setName(getName());
		arModifier.setMod(ModifiableStat.ARMORRATING,s_armorRating);
		arModifier.setVisible(false);
		addModifier(arModifier);
	}
	
}
