package mg.fishchicken.gamelogic.inventory.items;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;

public class SimpleItem extends InventoryItem {

	private boolean s_isGold;
	private boolean s_isWater;
	private boolean s_isFood;
	
	public SimpleItem() {
		super();
	}
	
	public SimpleItem(FileHandle file) throws IOException {
		super(file);
	}

	public boolean isGold() {
		return s_isGold;
	}

	public boolean isWater() {
		return s_isWater;
	}
	
	public boolean isFood() {
		return s_isFood;
	}

}
