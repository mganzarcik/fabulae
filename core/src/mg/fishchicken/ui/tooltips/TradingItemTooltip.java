package mg.fishchicken.ui.tooltips;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;

import com.badlogic.gdx.utils.StringBuilder;

public class TradingItemTooltip extends ItemTooltip {

	private GameCharacter merchant; 
	public TradingItemTooltip(InventoryItem item, ItemTooltipStyle style,  GameCharacter merchant) {
		super(item, style);
		this.merchant = merchant;
	}
	
	@Override
	protected void buildName(GameCharacter ic, StringBuilder fsb) {
		fsb.append(item.getName());
		if (item.getInventory() != null) {
			fsb.append(": ");
			fsb.append(item.getTradingCost(ic, merchant, merchant.equals(item
					.getInventory().getParentContainer())));
			fsb.append(" ");
			fsb.append(Strings.getString(InventoryItem.STRING_TABLE,
					"GoldPiecesAbbreviation"));
		}
		addLine(fsb.toString(), style.headingStyle);
		fsb.setLength(0);
	}	
}
