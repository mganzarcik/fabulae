package mg.fishchicken.gamelogic.inventory;

import java.io.IOException;

import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntMap;

/**
 * A ItemPile is a special kind of UsableGameObject that only exists if there is more
 * than one item in it.
 * 
 * The moment it contains only one item, it is destroyed and the last item is
 * put in its place on the map instead.
 * 
 * @author Annun
 * 
 */
public class ItemPile extends UsableGameObject {

	private boolean shouldBeRemoved;
	
	/**
	 * Empty constructor for game loading.
	 */
	public ItemPile() {
		super();
	}
	
	public ItemPile(String id) throws IOException {
		super(id, Gdx.files.internal(Configuration.getItemPileFile()));
	}
	
	public ItemPile(String id, Position position) throws IOException {
		this(id);
		position().set(position);
		setWidth(1);
		setHeight(1);
		shouldBeRemoved = false;
	}
	
	@Override
	protected Inventory createInventory() {
		return new ItemPileInventory();
	}
	
	private class ItemPileInventory extends UsableGameObjectInventory{

		public ItemPileInventory() {
			super(ItemPile.this);
		}
		
		@Override
		public void onItemRemove(InventoryItem item, BagType bagType) {
			super.onItemRemove(item, bagType);
			if (getBag(BagType.BACKPACK).size < 2) {
				shouldBeRemoved = true;
			}
		}

		@Override
		public void onItemAdd(InventoryItem item, BagType bagType) {
			super.onItemAdd(item, bagType);
			if (getBag(BagType.BACKPACK).size > 1) {
				shouldBeRemoved = false;
			}
		}
		
		@Override
		public void onInventoryClose() {
			super.onInventoryClose();
			if (shouldBeRemoved) {
				GameMap map = getMap();
				map.removeGameObject(ItemPile.this);
				IntMap<InventoryItem> backPack = getBag(BagType.BACKPACK);
				if (backPack.size == 1) {
					InventoryItem lastItem = backPack.values().next();
					Tile tile = position().tile();
					new PickableGameObject(lastItem, tile.getX(), tile.getY(), map);
					backPack.clear();
				}
			}
		}

	}

	
}
