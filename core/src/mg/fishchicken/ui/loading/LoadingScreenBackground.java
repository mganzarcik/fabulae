package mg.fishchicken.ui.loading;

import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.configuration.LoadingScreens.LoadingScreensImagesList;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.OrderedAssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * This is a "smart" background used on loading screens. It will load its first background image
 * asynchronously by itself and then, based on the supplied configuration, will dynamically
 * change the background images as time passes while continuing to load more background images
 * as needed.
 * 
 * It uses an internal asset manager to perform the asset loading.
 * 
 * @author ANNUN
 *
 */
public class LoadingScreenBackground extends Image implements Disposable {

	private float timeSinceImageChange;
	private int imageIndex;
	private LoadingScreensImagesList configuration;
	private AssetManager am;
	private Array<Texture> loadedImages;
	private Texture currentImage;
	private DetermineNextImageAction determineNextImageAction;
	private boolean needsNewBG;
	
	public LoadingScreenBackground(LoadingScreensImagesList configuration, String type) {
		super();
		am = new OrderedAssetManager(new InternalFileHandleResolver());
		determineNextImageAction = new DetermineNextImageAction();
		reset(configuration, type);
	}
	
	public void reset(LoadingScreensImagesList configuration, String type) {
		am.clear();
		needsNewBG = true;
		this.timeSinceImageChange = 0;
		this.configuration = configuration;
		loadedImages = new Array<Texture>();
		if (configuration != null) {
			configuration.loadImages(type, am);
		}
		imageIndex = 0;
		setFillParent(false);
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if (configuration != null) {
			am.update();
			int loadedCount = am.getLoadedAssets();
			if (loadedCount < 1) {
				return;
			}
			timeSinceImageChange += delta;
			int interval = configuration.getChangeInterval();
			if (getDrawable() == null) {
				needsNewBG = true;
				determineNextImageAction.act(delta);
			} else if (interval > 0 && timeSinceImageChange > interval && loadedCount > 1) {
				needsNewBG = true;
				addAction(Actions.sequence(Actions.fadeOut(0.1f), determineNextImageAction));
			}
		}
		
		if (getStage() != null) {
			WindowPosition.CENTER.position(this);
		}
	}
	
	@Override
	public void dispose() {
		am.dispose();
	}
	
	private class DetermineNextImageAction extends Action {

		@Override
		public boolean act(float delta) {
			if (!needsNewBG) {
				return true;
			}
			needsNewBG = false;
			loadedImages.clear();
			am.getAll(Texture.class, loadedImages);
			if (configuration.isRandom()) {
				if (currentImage != null) {
					loadedImages.removeValue(currentImage, false);
				}
				currentImage = loadedImages.random();
			} else {
				if (++imageIndex >= loadedImages.size) {
					imageIndex = 0;
				}
				currentImage = loadedImages.get(imageIndex);
			}
			timeSinceImageChange = 0;
			setDrawable(new TextureRegionDrawable(new TextureRegion(currentImage)));
			getColor().a = 0;
			addAction(Actions.fadeIn(0.1f));
			if (getStage() != null) {
				Stage stage = getStage();
				setFillParent(currentImage.getWidth() > stage.getWidth() || currentImage.getHeight() > stage.getHeight());
			}
			pack();
			return true;
		}
		
	}
	
}
