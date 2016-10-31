package mg.fishchicken.audio;

import mg.fishchicken.core.assets.AssetMap;

import com.badlogic.gdx.utils.Disposable;

/**
 * This is a dummy audio track that does not contain any audio.
 * 
 * Calling play or stop on it has no effect.
 *
 */
public class EmptyTrack extends AudioTrack<Disposable> {
	
	public static final EmptyTrack INSTANCE = new EmptyTrack();
	
	public EmptyTrack() {
		super();
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
	}

	@Override
	public void clearAssetReferences() {
	}

	@Override
	protected float getConfigurationVolumeModifier() {
		return 0;
	}

	@Override
	public void setTrack(Disposable track) {
	}

	@Override
	public void stop() {
	}

	@Override
	public void stopTrack() {
	}

	@Override
	protected void playTrack() {
	}
	
	@Override
	public void fadeIn(float duration) {
	}
	
	@Override
	public void fadeOut(float duration) {
	}

	@Override
	public float getBaseVolume() {
		return 0;
	}
	
}
