package mg.fishchicken.graphics.renderers;

import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locations.GameMapLoader;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

public class OrthogonalMinimapRenderer extends OrthogonalGameMapRenderer {

	public OrthogonalMinimapRenderer(GameState gameState, GameMap map) {
		super(gameState, map, null);
		computeCullingRectangle();
	}
	
	@Override
	protected boolean shouldRenderGrid() {
		return false;
	}
	
	public void render(float deltaTime, OrthographicCamera camera) {
		setView(camera);
		
		getSpriteBatch().begin();
		renderLayersExcludingColisions(deltaTime);
		renderCharacterCircles(deltaTime);
		getSpriteBatch().end();
		
	}
	
	public void computeCullingRectangle() {
		getCullingRectangle().set(0, 0, map.getWidth(), map.getHeight());
	}
	
	protected void renderCharacterCircles(float deltaTime) {
		if (!map.isWorldMap()) {
			Array<GameCharacter> members = GameState.getPlayerCharacterGroup().getMembers();
			for (int i = 0; i < members.size; ++i) {
				members.get(i).getCharacterCircle().draw(getSpriteBatch(), deltaTime);
				
			}
		} else {
			GameState.getPlayerCharacterGroup().getGroupGameObject().getCharacterCircle().draw(getSpriteBatch(), deltaTime);
		}
	}
	
	protected void renderLayersExcludingColisions (float delta) {
		for (MapLayer layer : map.getTiledMap().getLayers()) {
			
			boolean collisionLayer = layer.getProperties().get(GameMapLoader.PROPERTY_COLISIONS_LAYER) != null;
			
			if (collisionLayer) {
				continue;
			}
			if (layer.isVisible()) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer(delta, (TiledMapTileLayer) layer);
				} 				
			}				
		}
	}

}
