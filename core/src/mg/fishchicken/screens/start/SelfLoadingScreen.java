package mg.fishchicken.screens.start;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.screens.BasicStageScreen;
import mg.fishchicken.screens.ModuleLoadingScreen.LoadingScreenStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.loading.LoadingIndicator;
import mg.fishchicken.ui.loading.LoadingScreenBackground;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public abstract class SelfLoadingScreen extends BasicStageScreen {
	
	protected AssetManager assetManager;
	protected Skin skin;
	private boolean loading;
	private LoadingScreenBackground background;
	private LoadingIndicator loadingIndicator;
	
	protected abstract String getStyleName();
	protected abstract void playMusic();
	protected abstract void build();
	/**
	 * Gets the subtype of the loading screen. Used to determine the backgrounds to use.
	 * @return
	 */
	protected abstract String getLoadingScreenSubtype();
	/**
	 * Gets the type of the loading screen. Used to determine the backgrounds to use.
	 * @return
	 */
	protected String getLoadingScreenType() {
		return "menus";
	}
	
	public SelfLoadingScreen() {
		assetManager = Assets.getAssetManager();
	}
	
	private void buildLoadingIndicator() {
		LoadingScreenStyle style = UIManager.getSkin().get(LoadingScreenStyle.class);
		background = new LoadingScreenBackground(Configuration
				.getLoadingScreensConfiguration()
				.getConfigurationForScreenType(getLoadingScreenType()),
				getLoadingScreenSubtype());
		stage.addActor(background);
		loadingIndicator = new LoadingIndicator(style.loadingIndicatorStyle);
		stage.addActor(loadingIndicator);
		WindowPosition.CENTER.position(loadingIndicator);
	}
	
	@Override
	public void render(float delta) {
		String styleName = getStyleName();
		if (skin == null && loadingIndicator == null) {
			buildLoadingIndicator();
		}
		if (skin == null) {
			if (!loading) {
				assetManager.load(Configuration.getFolderUI()+styleName+".json", Skin.class);
				loadAdditionalAssets();
				loading = true;
			} else {
				if (assetManager.update()) {
					skin = assetManager.get(Configuration.getFolderUI()+styleName+".json", Skin.class);
					playMusic();
					loadingIndicator.remove();
					loadingIndicator = null;
					build();
				}			
			}
		} 
		super.render(delta);
	}
	
	/**
	 * Load any additional that are needed by this screen to the asset manager.
	 * 
	 * Default implementation does nothing.
	 */
	protected void loadAdditionalAssets() {
	}
	
	@Override
	public void show() {
		super.show();
		if (skin != null) {
			playMusic();
		}
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (loadingIndicator != null) {
			WindowPosition.CENTER.position(loadingIndicator);
		}
	}
	
	@Override
	public void hide() {
		dispose();
		super.hide();
	}
	
	@Override
	public void dispose() {
		assetManager.unload(Configuration.getFolderUI()+getStyleName()+".json");
		if (background != null) {
			background.dispose();
		}
		skin = null;
		super.dispose();
	}

}