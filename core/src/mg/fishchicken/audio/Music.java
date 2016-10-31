package mg.fishchicken.audio;

import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class Music extends AudioTrack<com.badlogic.gdx.audio.Music> {

	private com.badlogic.gdx.audio.Music music;

	private static Music playingMusic; 
	
	public Music() {
		super();
	}
	
	public Music(String fileName) {
		super();
		setFilename(fileName);
	}
	
	public Music(AudioTrack<?> sountToInitFrom) {
		super();
		initFrom(sountToInitFrom);
	}
	
	public com.badlogic.gdx.audio.Music getTrack() {
		if (music == null) {
			music = Assets.get(getFilename(), com.badlogic.gdx.audio.Music.class);
		}
		return music;
	}
	
	@Override
	public void setLooping(boolean value) {
		super.setLooping(value);
		getTrack().setLooping(value);
	}
	
	/**
	 * Updates this audiotrack in time. Used
	 * mostly for fading away tracks.
	 * 
	 * This should be called each frame.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (this.equals(playingMusic)) {
			if (!getTrack().isPlaying()) {
				playingMusic = null;
			}
		}
	}

	protected boolean shouldPlay(int rollResult) {
		if (getTrack().isPlaying()) {
			return false;
		}
		return super.shouldPlay(rollResult);
	}
	
	protected void playTrack() {
		if (playingMusic != null) {
			playingMusic.stop();
		}
		getTrack().setVolume(getVolume());
		int retryCounter = 0;
		Exception exception = null;
		while (retryCounter < 10) {
			try {
				getTrack().play();
				exception = null;
				break;
			} catch (GdxRuntimeException e) {
				exception = e;
			}
		}
		if (exception != null) {
			Log.log("Error playng music: "+exception, LogType.ERROR);
		}
		playingMusic = this;
	}
	
	public void stop() {
		delayedPlayIn = 0;
		fadeOut();
	}

	public void stopTrack() {
		delayedPlayIn = 0;
		getTrack().stop();
		playingMusic = null;
	}
	
	public void setVolume(float volume) {
		super.setVolume(volume);
		getTrack().setVolume(volume);
	}
	
	@Override
	public void setTrack(com.badlogic.gdx.audio.Music track) {
		music = track;
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
		assetStore.put(getFilename(),  com.badlogic.gdx.audio.Music.class);
	}
	
	public static boolean isPlayingMusic() {
		return playingMusic != null;
	}
	
	public static void stopPlayingMusic() {
		if (playingMusic != null) {
			playingMusic.stopTrack();
			playingMusic = null;
		}
	}
	
	public static void updatePlayingMusicVolume() {
		if (playingMusic != null) {
			playingMusic.modifyVolume();
		}
	}

	@Override
	public void clearAssetReferences() {
		music = null;
	}

	@Override
	protected float getConfigurationVolumeModifier() {
		return Configuration.getMusicVolume();
	}

}
