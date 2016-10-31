package mg.fishchicken.core.assets;


/**
 * All implementers of this interface contain assets that should be loaded by
 * the AssetManager before they are first used.
 * 
 * All implementers therefore provide a common method to gather these assets
 * beforehand.
 * 
 * @author Annun
 * 
 */
public interface AssetContainer  {
	
	/**
	 * Gathers all assets that should be loaded by the AssetManager
	 * prior to using this object.
	 * 
	 * @param assetStore
	 */
	public void gatherAssets(AssetMap assetStore);
	
	/**
	 * This is called when an AssetContainer is unloaded from memory.
	 * 
	 * Any asset references should be cleared so that the container
	 * does not keep pointing to disposed assets. 
	 */
	public void clearAssetReferences();
}
