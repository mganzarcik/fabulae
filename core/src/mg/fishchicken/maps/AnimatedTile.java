package mg.fishchicken.maps;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;

public class AnimatedTile extends AnimatedTiledMapTile {
	private float stateTime;
	private float animationDuration;
	private float frameDuration;
	private boolean randomized;
	private boolean hasRandomDelay;
	private int numberOfFrames; 
	private Array<StaticTiledMapTile> frameTiles;
	
	public AnimatedTile(float frameDuration, Array<StaticTiledMapTile> frameTiles, boolean randomDelay, boolean randomAnimation) {
		super(frameDuration, frameTiles);
		this.frameDuration = frameDuration;
		numberOfFrames = frameTiles.size;
		this.animationDuration = frameDuration * numberOfFrames;
		this.frameTiles = frameTiles;
		this.hasRandomDelay = randomDelay;
		this.randomized = randomAnimation;
	}
	
	public float getFrameDuration() {
		return frameDuration;
	}
	
	public float getAnimationDuration() {
		return animationDuration;
	}
	
	public boolean hasRandomAnimation() {
		return randomized;
	}
	
	public boolean hasRandomDelay() {
		return hasRandomDelay;
	}
	
	public void setStateTime(float stateTime) {
		this.stateTime = stateTime;
	}
	
	@Override
	public TextureRegion getTextureRegion () {
		int frameNumber = (int)(stateTime / frameDuration);
		frameNumber = frameNumber % frameTiles.size;
		return frameTiles.get(frameNumber).getTextureRegion();
	}
}
