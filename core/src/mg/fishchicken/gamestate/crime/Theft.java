package mg.fishchicken.gamestate.crime;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemOwner;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Theft extends Crime<InventoryItem> {
	
	public Theft() {
	}
	
	public Theft(GameCharacter thief, InventoryItem stolenItem) {
		super(thief, stolenItem);
	}

	@Override
	public boolean canBePaidOff() {
		return true;
	}
	
	@Override
	public Faction getVictimFaction() {
		Faction faction = getCrimeTarget().getOwner().getOwnerFaction();
		if (faction == null) {
			faction = Faction.NO_FACTION;
		}
		return faction;
	}

	@Override
	protected int getBaseFineAmount() {
		return MathUtils.ceil(getCrimeTarget().getCost() * Configuration.getTheftFineMultiplier());
	}
	
	@Override
	public int getDispositionPenalty() {
		return Configuration.getTheftDispositionPenalty();
	}
	
	@Override
	protected void writeTargetToXml(XmlWriter writer) throws IOException {
		InventoryItem item = getCrimeTarget();
		writer.attribute(XMLUtil.XML_ATTRIBUTE_ID, item.getId())
				.attribute(Inventory.XML_ATTRIBUTE_STACK_SIZE,
						item.getStackSize());
		ItemOwner itemOwner = item.getOwner();
		String ownerId =itemOwner.getOwnerCharacterId();
		if (ownerId != null) {
			writer.attribute(Inventory.XML_ATTRIBUTE_OWNER_CHARACTER, ownerId);
		}
		Faction faction = itemOwner.getOwnerFaction();
		if (faction != null) {
			writer.attribute(Inventory.XML_ATTRIBUTE_OWNER_FACTION, faction);
		}
	}

	@Override
	protected InventoryItem readTargetFromXml(Element targetElement)
			throws IOException {
		InventoryItem item = GameState.getItem(targetElement.getAttribute(XMLUtil.XML_ATTRIBUTE_ID));
		
		item.getOwner().set(
				targetElement.getAttribute(Inventory.XML_ATTRIBUTE_OWNER_CHARACTER,
						null),
				Faction.getFaction(targetElement.getAttribute(
						Inventory.XML_ATTRIBUTE_OWNER_FACTION, null)),
				targetElement.getBoolean(Inventory.XML_ATTRIBUTE_OWNER_FIXED, false));
		
		Integer stackSize =  Integer.parseInt(targetElement.getAttribute(Inventory.XML_ATTRIBUTE_STACK_SIZE));
		if (stackSize < 0) {
			item.setInfinite(true);
		} else {
			for (int k = 1; k < stackSize; ++k) {
				InventoryItem newItem = item.createNewInstance();
				newItem.getOwner().set(item.getOwner());
				item.addToStack(newItem);
			}
		}
		return item;
	}
	
}
