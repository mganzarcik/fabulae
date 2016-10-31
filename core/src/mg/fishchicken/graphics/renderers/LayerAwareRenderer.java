package mg.fishchicken.graphics.renderers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

public interface LayerAwareRenderer extends TiledMapRenderer {
	
	public void renderTileLayers(Array<TiledMapTileLayer> layers);
	public void renderExcludingLayers (Array<MapLayer> layers);
	public Batch getSpriteBatch ();
	public void dispose();
	public void renderTile(TiledMapTileLayer layer, int col, int row, float color, float width, float height);
}
