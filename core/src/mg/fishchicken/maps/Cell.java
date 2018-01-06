package mg.fishchicken.maps;

import mg.fishchicken.core.GameState;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.math.MathUtils;

public class Cell extends com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell{
	boolean started = false;
	private boolean animated;
	private float stateTime;
	private float randomDelay;
	private float lastRandomStateTime;
	
	/**
	 * @return The tile currently assigned to this cell.
	 */
	public TiledMapTile getTile() {
		return super.getTile();
	}
	
	/**
	 * Updates the animation state time of the tile for this cell
	 * in case it is animated.
	 * 
	 * This should be called prior to calling getTextureRegion on the tile during rendering.
	 * 
	 */
	public void updateStateTime(float delta) {
		if (animated) {
			AnimatedTile aTile = (AnimatedTile)getTile();
			if (GameState.isPaused()) {
				aTile.setStateTime(stateTime);
				return;
			}
			stateTime += delta;
			
			if (aTile.hasRandomAnimation()) {
				if (started == false || stateTime > aTile.getFrameDuration()+randomDelay) {
					lastRandomStateTime = MathUtils.random(aTile.getAnimationDuration());
					stateTime = 0;
					started = true;
				}
				aTile.setStateTime(lastRandomStateTime);
			} else {
				aTile.setStateTime(stateTime);
			}
		}
	}
	
	/**
	 * Sets the tile to be used for this cell.
	 * 
	 * @param tile
	 */
	public Cell setTile(TiledMapTile tile) {
		super.setTile(tile);
		animated = false;
		if (tile instanceof AnimatedTile) {
			animated = true;
			stateTime = 0;
			randomDelay = 0;
			if (((AnimatedTile) tile).hasRandomDelay()) {
				randomDelay = MathUtils.random(((AnimatedTile) tile).getAnimationDuration());
				stateTime = randomDelay;
			}
		}
		return this;
	}
}
