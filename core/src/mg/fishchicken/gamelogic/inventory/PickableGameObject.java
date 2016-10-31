package mg.fishchicken.gamelogic.inventory;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.UsableGameObject;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemOwner;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.graphics.DrawableGameObject;
import mg.fishchicken.graphics.TextDrawer;
import mg.fishchicken.graphics.renderers.FloatingTextRenderer;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * GO that can be picked up by the PlayerCharacters.
 * 
 * Each PickableGO contains an InventoryItem that it represents on the GameMap.
 * 
 * Once picked up, the PickableGO is removed from the map and the InventoryItem
 * is added to the Inventory of the PlayerCharacter who picked it up.
 * 
 * @author Annun
 * 
 */
public class PickableGameObject extends DrawableGameObject implements Pickable, AssetContainer, TextDrawer {

	private InventoryItem item; // lazy init
	private String s_itemId;
	private ItemOwner owner;
	private FloatingTextRenderer floatingTextRenderer = new FloatingTextRenderer();
	
	/**
	 * Empty constructor for game loading.
	 */
	public PickableGameObject() {
		super();
		owner = new ItemOwner();
	}
	
	public PickableGameObject(String itemId) throws IOException {
		super(itemId,itemId);
		s_itemId = itemId;
		owner = new ItemOwner();
		setWidth(1);
		setHeight(1);
	}

	
	public PickableGameObject(InventoryItem item, float x, float y, GameMap map) {
		super(item.getId(), item.getId());
		setWidth(1);
		setHeight(1);
		this.item = item;
		s_itemId = item.getId();
		owner = new ItemOwner(item.getOwner());
		
		position().set(x, y);
		setMap(map);
	}
	
	@Override
	public String getName() {
		return getInventoryItem().getName();
	}
	
	@Override
	public TextureRegion getTexture() {
		TextureRegion returnValue = super.getTexture();
		if (returnValue == null) {
			returnValue = Assets.getTextureRegion(getInventoryItem().getMapIconFile());
			super.setTexture(returnValue);
		}
		return returnValue;
	}
	
	@Override
	public String getInventoryItemId() {
		return s_itemId;
	}
	
	public ItemOwner getOwner() {
		return owner;
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		getInventoryItem().gatherAssets(assetStore);
	}
	
	@Override
	public void clearAssetReferences() {
		setTexture(null);
	}

	@Override
	public boolean pickUp(InventoryContainer container) {
		InventoryCheckResult checkResult = container.getInventory().canAddItem(item);
		if (checkResult.getError() == null) {
			setMap(null);
			container.getInventory().addItem(getInventoryItem());
			return true;
		} else {
			Log.log(checkResult.getError(), LogType.INVENTORY);
			return false;
		}
	}
	
	@Override
	public boolean isAlwaysBehind() {
		return false;
	}

	@Override
	public InventoryItem getInventoryItem()  {
		if (item == null) {
			item = GameState.getItem(getInventoryItemId());
			item.getOwner().set(owner);
		}
		return item;
	}
	
	@Override
	public void onHit(Projectile projectile, GameObject user) {
		return;
	}
	
	@Override
	public Color getHighlightColor(float x, float y) {
		return UsableGameObject.shouldHighlightUsable(this, x, y) ? Color.WHITE : null;
	}
	
	@Override
	public int getHighlightAmount(float x, float y) {
		return 1;
	}
	
	@Override
	public void drawText(SpriteBatch spriteBatch, float deltaTime) {
		floatingTextRenderer.setText(item.getName());
		if (item.getOwner().includes(Faction.PLAYER_FACTION)) {
			floatingTextRenderer.setColor(Color.WHITE);
		} else {
			floatingTextRenderer.setColor(Color.RED);
		}
		floatingTextRenderer.render(spriteBatch, 0, this, (int)getMap().getTileSizeX(), (int)getMap().getTileSizeY());
	}

	@Override
	public boolean shouldDrawText() {
		return !UIManager.isAnythingOpen()
				&& contains(gameState.getPlayerCharacterController()
						.getMouseTileX(), gameState
						.getPlayerCharacterController().getMouseTileY())
				&& shouldDraw(null);
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		owner.writeToXML(writer);
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		owner.loadFromXML(root);
		super.loadFromXML(root);
	}
}
