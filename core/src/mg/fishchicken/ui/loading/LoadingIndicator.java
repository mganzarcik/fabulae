package mg.fishchicken.ui.loading;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class LoadingIndicator extends Table {

	private Label loadingWhatLabel;
	
	public LoadingIndicator(LoadingIndicatorStyle style) {
		if (style.spinnerImage != null) {
			Image spinner = new Image(style.spinnerImage);
			spinner.setOrigin(spinner.getWidth() / 2, spinner.getHeight() / 2);
			spinner.addAction(Actions.forever(Actions.rotateBy(-360, style.rotationDuration)));
			add(spinner).center();
			row();
		}
		loadingWhatLabel = new Label(Strings.getString(UIManager.STRING_TABLE, "loading", ""), style.loadingLabelStyle);
		add(loadingWhatLabel)
			.center().padTop(style.loadingLabelMarginTop);
		pack();
	}
	
	public void setWhat(String what) {
		loadingWhatLabel.setText(Strings.getString(UIManager.STRING_TABLE, "loading", what));
	}
	
	public static class LoadingIndicatorStyle {
		public Drawable spinnerImage;
		public LabelStyle loadingLabelStyle;
		public float rotationDuration = 2;
		public int loadingLabelMarginTop = 10;
	}
}
