package mg.fishchicken.ui.dialog;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ProgressDialog extends BorderedWindow {
	
	private ProgressBar progressBar;
	private Label current, textLabel;
	
	public ProgressDialog(ProgressDialogStyle style) {
		super(style);
		current = new Label("", style.textStyle);
		progressBar = new ProgressBar(0, 1, 1, false, style.progressBarStyle);
		progressBar.setHeight(style.barHeight);
		progressBar.setAnimateDuration(0.1f);
		textLabel = new Label("", style.textStyle);
	}
	
	public void init (ProgressDialogSettings settings, final ProgressDialogCallback callback) {
		clearChildren();
		ProgressDialogStyle style = (ProgressDialogStyle) getStyle();
		
		setTitle(settings.header);
		current.setText(Float.toString(settings.start));	
		progressBar.setRange(settings.start, settings.end);
		progressBar.setValue(settings.start);
		
		add(new Label(Float.toString(settings.start), style.textStyle));
		add(progressBar).fill().prefWidth(style.barWidth).prefHeight(style.barHeight);
		add(new Label(Float.toString(settings.end), style.textStyle));
		
		row();
		add(current).colspan(3).center();
		row();
		
		if (settings.text != null) {
			textLabel.setText(settings.text);
			add(textLabel).colspan(3).center();
			row();
		}
		
		if (settings.canCancel) {			
			Container<TextButtonWithSound> buttonsContainer = new Container<TextButtonWithSound>();
			TextButtonWithSound button = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "cancel"), style.cancelButtonStyle);
			button.addListener(new ChangeListener() {
				
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					callback.onCancelled((int)progressBar.getValue());
					UIManager.hideProgressDialog();
				}
			});
			buttonsContainer.setActor(button);
			buttonsContainer.fill().width(style.cancelButtonWidth).height(style.cancelButtonHeight);
			add(buttonsContainer).center().colspan(3);
		}
		pack();
	}
	
	public void update(float newValue) {
		progressBar.setValue(newValue);
		current.setText(Integer.toString((int)newValue));
	}
	
	static public class ProgressDialogStyle extends BorderedWindowStyle {
		public int barWidth, barHeight, cancelButtonWidth, cancelButtonHeight;
		public TextButtonWithSoundStyle cancelButtonStyle;
		public ProgressBarStyle progressBarStyle;
		public LabelStyle textStyle;
	}

}
