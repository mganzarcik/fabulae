package mg.fishchicken.audio;

import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.math.Vector2;

public interface AudioOriginator {

	/**
	 * Returns the distance of this originator from Player in tiles.
	 * It is up to the implementers to decide what exactly this means.
	 * 
	 * This distance will then be used to calculate the volume
	 * of the playing sound, if necessary.
	 * 
	 * @return
	 */
	public float getDistanceToPlayer();
	
	/**
	 * Whether or not the volume of the audio originating from this
	 * should be modified based on the camera and the distance 
	 * of the PCs from the originator.
	 * 
	 * @return
	 */
	public boolean shouldModifyVolume();

	/**
	 * Whether the originator has already been visited by the Player.
	 * 
	 * Used for OnEntry events where replay = false.
	 * 
	 * @return
	 */
	public boolean alreadyVisited();

	/**
	 * What is the maximum distance in tiles at which the audio is stil audible.
	 * 
	 * @return
	 */
	public float getSoundRadius();

	/**
	 * The position in tiles of the origin of the audio.
	 * 
	 * @return
	 */
	public Vector2 getSoundOrigin();

	/**
	 * The GameMap this originator belongs to.
	 * 
	 * @return
	 */
	public GameMap getMap();
}
