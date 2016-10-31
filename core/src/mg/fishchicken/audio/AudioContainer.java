package mg.fishchicken.audio;

public interface AudioContainer {

	/**
	 * Adds the supplied track to this originator.
	 * 
	 * @param track
	 */
	public void addTrack(AudioTrack<?> track, String type);
	
}
