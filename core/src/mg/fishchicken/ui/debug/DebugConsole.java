package mg.fishchicken.ui.debug;

import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.GroovyUtil;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ForcedScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class DebugConsole extends BorderedWindow {

	private Label result;
	private TextField input;
	private TextButtonWithSound submitButton;
	private long scriptCounter;
	
	public DebugConsole(DebugConsoleStyle style) {
		super(Strings.getString(UIManager.STRING_TABLE, "debugConsole"), style);
		scriptCounter = 0;

		submitButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "execute"), style.buttonStyle);
		submitButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				submit();
			}
		});
		input = new TextField("", style.textFieldStyle);
		input.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (KeyBindings.CONFIRM.is(keycode)) {
					if (StringUtil.nullOrEmptyString(input.getText())) {
						return false;
					}
					submitButton.toggle();
					return true;
				}				
				return false;
			}
		});
	
		result = new Label("", style.resultsStyle);
		result.setWrap(true);
		result.setAlignment(Align.topLeft);
		ScrollPane scrollPane = new ForcedScrollPane(result, style.scrollPaneStyle);
		add(scrollPane).width(style.scrollPaneWidth).height(style.scrollPaneHeight).fill().colspan(3);
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "input"), style.labelStyle)).fill();
		add(input).fill().expand()
			.padRight(style.inputMarginRight).padLeft(style.inputMarginLeft);
		add(submitButton).minWidth(style.buttonWidth).minHeight(style.buttonHeight);
		
		pack();
	}
	
	private void submit() {
		String text = input.getText();
		Object result = null;
		try {
			result = GroovyUtil.createScript(generateScriptName(), text).run();
		} catch (Throwable t) {
			result = t;
		}
		if (result == null) {
			result = "void";
		}
		this.result.setText(result.toString());
	}
	
	private String generateScriptName() {
        return "DebugConsoleScript" + (++scriptCounter);
    }
	
	@Override
	public void act(float delta) {
		super.act(delta);
		submitButton.setDisabled(StringUtil.nullOrEmptyString(input.getText()));
	}

	public static class DebugConsoleStyle extends BorderedWindowStyle {
		private TextFieldStyle textFieldStyle;
		private LabelStyle resultsStyle, labelStyle;
		private ScrollPaneStyle scrollPaneStyle;
		private TextButtonWithSoundStyle buttonStyle;
		private int scrollPaneWidth, scrollPaneHeight, inputMarginRight, inputMarginLeft, buttonWidth, buttonHeight;
	}
}
