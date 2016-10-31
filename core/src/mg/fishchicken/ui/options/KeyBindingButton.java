package mg.fishchicken.ui.options;

import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.button.TextButtonWithSound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class KeyBindingButton extends TextButtonWithSound {

	private Array<Integer> currentBindings;
	private KeyBindings binding;
	
	public KeyBindingButton(KeyBindings binding, KeyBindingButtonStyle style) {
		super(binding.keysToString(), style);
		this.binding = binding;
		this.currentBindings = new Array<Integer>(binding.getKeys());
		
		addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				displayKeyPicker();
			}
		});
		
		getLabelCell().padRight(getStyle().labelPadRight).padLeft(getStyle().labelPadLeft);
	}
	
	public void reset() {
		currentBindings.clear();
		currentBindings.addAll(binding.getKeys());
		setText(binding.keysToString());
	}
	
	public KeyBindingButtonStyle getStyle() {
		return (KeyBindingButtonStyle) super.getStyle();
	}
	
	public KeyBindings getBinding() {
		return binding;
	}
	
	public Array<Integer> getSelectedKeys() {
		return currentBindings;
	}
	
	private void displayKeyPicker() {
		final Stage stage = getStage();
		if (stage == null) {
			return;
		}
		
		final Label infoTextModal = new Label(Strings.getString(UIManager.STRING_TABLE, "pressDesiredKey", binding.getUIName()), getStyle().infoTextModalStyle);
		infoTextModal.setWrap(true);
		infoTextModal.setWidth(getStyle().infoTextModalWidth);
		infoTextModal.validate();
		final Table table = new Table();
		table.add(infoTextModal).prefWidth(getStyle().infoTextModalWidth).fill();
		infoTextModal.setHeight(infoTextModal.getGlyphLayout().height);
		infoTextModal.setWidth(infoTextModal.getGlyphLayout().width);
		infoTextModal.setAlignment(Align.center, Align.left);
		table.setWidth(infoTextModal.getWidth());
		table.setHeight(infoTextModal.getHeight());
		
		stage.addActor(table);
		final EventListener captureListener = new EventListener() {
			
			@Override
			public boolean handle(Event event) {
				if (!(event instanceof InputEvent)) {
					event.cancel();
					return true;
				} else if (((InputEvent)event).getType() != Type.keyUp) { 
					event.cancel();
					return true;
				}
				return false;
			}
		};
		stage.addCaptureListener(captureListener);
		stage.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				currentBindings.clear();
				currentBindings.add(keycode);
				setText(Input.Keys.toString(keycode));
				stage.removeListener(this);
				stage.removeCaptureListener(captureListener);
				infoTextModal.remove();
				table.remove();
				getClickListener().exit(event, Gdx.input.getX(), Gdx.input.getY(), -1, null);
				return true;
			}
		});;
		table.pack();
		WindowPosition.CENTER.position(table);
	}
	
	public static class KeyBindingButtonStyle extends TextButtonWithSoundStyle {
		private LabelStyle infoTextModalStyle;
		private int infoTextModalWidth, labelPadLeft, labelPadRight;
	}
}
