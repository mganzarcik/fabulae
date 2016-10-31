package mg.fishchicken.audio;

import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class StreamingSound extends AudioTrack<com.badlogic.gdx.audio.Music> {

	private com.badlogic.gdx.audio.Music sound;
	
	public StreamingSound() {
		super();
	}
	
	public StreamingSound(String fileName) {
		super();
		setFilename(fileName);
	}
	
	public StreamingSound(AudioTrack<?> soundToInitFrom) {
		super();
		initFrom(soundToInitFrom);
	}
	
	@Override
	public void setLooping(boolean value) {
		super.setLooping(value);
		getTrack().setLooping(value);
	}
	
	public com.badlogic.gdx.audio.Music getTrack() {
		if (sound == null) {
			sound = Assets.get(getFilename(), com.badlogic.gdx.audio.Music.class);
		}
		return sound;
	}
	
	protected boolean shouldPlay(int rollResult) {
		if (getTrack().isPlaying()) {
			return false;
		}
		return super.shouldPlay(rollResult);
	}
	
	protected void playTrack() {
		// make sure the track is stopped before playing it again
		// otherwise we can get weird stuttering issues
		stopTrack(); 
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
	}
	
	public void stop() {
		fadeOut();
	}

	public void stopTrack() {
		getTrack().stop();
	}
	
	public void setVolume(float volume) {
		super.setVolume(volume);
		getTrack().setVolume(volume);
	}
	
	@Override
	public void setTrack(com.badlogic.gdx.audio.Music track) {
		sound = track;
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
		assetStore.put(getFilename(),  com.badlogic.gdx.audio.Music.class);
	}

	@Override
	protected float getConfigurationVolumeModifier() {
		return Configuration.getSoundEffectsVolume();
	}

	@Override
	public void clearAssetReferences() {
		sound = null;
	}

}
