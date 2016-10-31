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
	private Modifier arModifier;
	
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

	public void setArmorRating(int s_armorRating) {
		this.s_armorRating = s_armorRating;
		arModifier.setMod(ModifiableStat.ARMORRATING,s_armorRating);
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		super.loadFromXML(file);
		arModifier = new Modifier();
		arModifier.setName(getName());
		arModifier.setMod(ModifiableStat.ARMORRATING,s_armorRating);
		arModifier.setVisible(false);
		addModifier(arModifier);
	}

	@Override
	public InventoryItem createNewInstance() {
		// remove the arModifier since we don't want to copy it automatically
		// because we want to keep a reference to it
		removeModifier(arModifier);
		Armor returnValue = (Armor) super.createNewInstance();
		addModifier(arModifier);
		
		returnValue.arModifier = arModifier.copy();
		returnValue.addModifier(returnValue.arModifier);
		returnValue.setArmorRating(getAmorRating());
		return returnValue;
	}
}
