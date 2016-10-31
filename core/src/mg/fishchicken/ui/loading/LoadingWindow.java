package mg.fishchicken.ui.loading;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.loading.LoadingIndicator.LoadingIndicatorStyle;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class LoadingWindow<T extends BorderedWindow> extends BorderedWindow {

	private final AssetManager am = Assets.getAssetManager();
	private T windowToLoad;
	private final Loader<T> loader;
	private boolean dependenciesLoaded = false;

	public LoadingWindow(T windowToLoad, LoadingIndicatorStyle style,
			Loader<T> loader) {
		super(windowToLoad.getTitleLabel().getText().toString(), windowToLoad
				.getStyle());
		setMovable(false);
		this.windowToLoad = windowToLoad;
		this.loader = loader;
		loader.load(am);
		Group parent = windowToLoad.getParent();
		windowToLoad.remove();
		windowToLoad.setVisible(false);
		parent.addActor(this);
		setVisible(true);
		Image spinner = new Image(style.spinnerImage);
		spinner.setOrigin(spinner.getWidth() / 2, spinner.getHeight() / 2);
		spinner.getColor().a = 0;
		spinner.addAction(Actions.fadeIn(0.8f));
		spinner.addAction(Actions.forever(Actions.rotateBy(-360, style.rotationDuration)));
		add(spinner).center();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "loading",""), style.loadingLabelStyle))
			.center().padTop(style.loadingLabelMarginTop);
		pack();
		
		float minWidth = getWidth(); 
		float minHeight = getHeight();
		setWidth(windowToLoad.getWidth() > minWidth ? windowToLoad.getWidth() : minWidth);
		setHeight(windowToLoad.getHeight() > minHeight ? windowToLoad.getHeight() : minHeight);
		setPosition(windowToLoad.getX(), windowToLoad.getY());
	}
	
	public boolean isLoading(BorderedWindow window) {
		return isVisible() && CoreUtil.equals(window, windowToLoad);
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if (loader.update(am)) {
			if (!dependenciesLoaded) {
				loader.loadDependencies(am);
				dependenciesLoaded = true;
				return;
			}
			Group parent = getParent();
			remove();
			setVisible(false);
			parent.addActor(windowToLoad);
			loader.onLoaded(am, windowToLoad);
			windowToLoad.setVisible(true);
		}
	}
	
	

	public static abstract class Loader<T extends BorderedWindow> {
		public abstract void load(AssetManager am);

		public void onLoaded(AssetManager am, T loadedWindow) {

		}
		
		protected void loadDependencies(AssetManager am) {
		}

		private boolean update(AssetManager am) {
			return am.update();
		}
	}
}
