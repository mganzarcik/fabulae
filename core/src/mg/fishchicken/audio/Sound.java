package mg.fishchicken.audio;

import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;

public class Sound extends AudioTrack<com.badlogic.gdx.audio.Sound>{

	com.badlogic.gdx.audio.Sound sound;
	
	private Long soundId = null;
	
	public Sound() {
		super();
	}
	
	public Sound(String fileName) {
		super();
		setFilename(fileName);
	}
	
	public Sound(AudioTrack<?> sountToInitFrom) {
		super();
		initFrom(sountToInitFrom);
	}
	
	protected void playTrack() {
		if (getVolume() <= 0) {
			return;
		}
		// if we are looping and playing, stop the looped sound first
		// otherwise it will never stop
		if (soundId != null && isLooping()) {
			getTrack().stop(soundId);
		}
		soundId = getTrack().play(getVolume());
		getTrack().setLooping(soundId, isLooping());
	}
	
	@Override
	public void setLooping(boolean value) {
		super.setLooping(value);
		getTrack().setLooping(soundId, value);
	}
	
	public void stop() {
		delayedPlayIn = 0;
		if (soundId != null && isLooping()) {
			fadeOut();
		} else {
			stopTrack();
		}
	}
	
	public void stopTrack() {
		delayedPlayIn = 0;
		if (soundId != null) {
			getTrack().stop(soundId);
			soundId = null;
		}
	}
	
	protected boolean shouldPlay(int rollResult) {
		// if we are playing a looping sound, don't play it again
		if (soundId != null && isLooping()) {
			return false;
		}
		return super.shouldPlay(rollResult);
	}
	
	public com.badlogic.gdx.audio.Sound getTrack() {
		if (sound == null) {
			sound = Assets.get(getFilename(), com.badlogic.gdx.audio.Sound.class);
		}
		return sound;
	}
	
	public void setVolume(float volume) {
		super.setVolume(volume);
		if (soundId != null) {
			getTrack().setVolume(soundId, volume);
		}
	}
	
	@Override
	public void setTrack(com.badlogic.gdx.audio.Sound track) {
		sound = track;
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		assetStore.put(getFilename(),  com.badlogic.gdx.audio.Sound.class);
	}

	@Override
	public void clearAssetReferences() {
		sound = null;
	}

	@Override
	protected float getConfigurationVolumeModifier() {
		return Configuration.getSoundEffectsVolume();
	}

}
