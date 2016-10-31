package mg.fishchicken.ui.options;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.configuration.SupportedResolution;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.graphics.CustomResolution;
import mg.fishchicken.graphics.Resolution;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.ConfirmCancelKeyboardListener;
import mg.fishchicken.ui.TableStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.checkbox.CheckboxWithSound;
import mg.fishchicken.ui.checkbox.CheckboxWithSound.CheckBoxWithSoundStyle;
import mg.fishchicken.ui.options.KeyBindingButton.KeyBindingButtonStyle;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound.SelectBoxWithSoundStyle;
import mg.fishchicken.ui.selectbox.SelectOption;
import mg.fishchicken.ui.slider.SliderWithSound;
import mg.fishchicken.ui.slider.SliderWithSound.SliderWithSoundStyle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class GameOptionsPanel extends BorderedWindow {

	private GameOptionsPanelStyle style;
	private SliderWithSound audioSliderWithSound, musicSliderWithSound, uiEffectsSliderWithSound, scrollSpeedSliderWithSound, tooltipDelaySliderWithSound, combatSpeedSliderWithSound;
	private CheckboxWithSound fullscreenCheckBox, mouseMoveCheckbox, characterBarksCheckbox;
	private SelectBoxWithSound<SelectOption<Resolution>> resolutionSelect;
	private boolean resolutionChanged;
	private SelectOption<Resolution> originalResolution;
	private SelectOption<Resolution> customResolutionOption;
	private Array<KeyBindingButton> keyButtons;
	private ScrollPane backScrollPane;
	private GameOptionsPanelCallback callback;
	
	public GameOptionsPanel(Skin skin, final GameState gameState, final GameOptionsPanelCallback callback) {
		super(Strings.getString(UIManager.STRING_TABLE, "gameOptionsTitle"),
				skin.get("gameOptions",
						GameOptionsPanelStyle.class));
		this.callback = callback;
		resolutionChanged = false;
		style = (GameOptionsPanelStyle) getStyle();

		if (style.headerImage != null) {
			add(new Image(style.headerImage));
			row();
		}
		
		add(buildOptions());
		row();
		
		add(buildButtons());
		pack();
		
		setConfigurationValuesToFields();
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible && !isVisible()) {
			backScrollPane.scrollTo(0, backScrollPane.getWidget().getHeight()	, 1, 1);
		}
		super.setVisible(visible);
	}
	
	private Table buildOptions() {
		Table firstCol = new Table();
		if (style.firstColStyle != null) {
			style.firstColStyle.apply(firstCol);
		}
		Table secondCol = new Table();
		if (style.secondColStyle != null) {
			style.secondColStyle.apply(secondCol);
		}
		
		if (Gdx.graphics.supportsDisplayModeChange()) {
			firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "graphics"), style.headingStyle))
						.fill().colspan(2).padBottom(style.headingMarginBottom).padLeft(style.headingMarginLeft).padRight(style.headingMarginRight);
			firstCol.row();
			firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "resolution"), style.fieldLabelStyle))
					.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
			resolutionSelect = new SelectBoxWithSound<SelectOption<Resolution>>(style.resolutionSelectStyle);
			Array<SelectOption<Resolution>> items = new Array<SelectOption<Resolution>>();
			for (SupportedResolution supportedRes : SupportedResolution.values()) {
				for (DisplayMode resolution : Gdx.graphics.getDisplayModes()) {
					if (supportedRes.getHeight() == resolution.height && supportedRes.getWidth() == resolution.width) {
						SelectOption<Resolution> newOption = new SelectOption<Resolution>(
								resolution.width
								+ "x"
								+ resolution.height
								+ " (" + supportedRes.getAspectRatio() + ")", supportedRes);
						if (!items.contains(newOption, false)) {
							items.add(newOption);
						}
					}
					
				}
			}
			resolutionSelect.setItems(items);
			resolutionSelect.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					resolutionChanged = originalResolution != resolutionSelect.getSelected();
				}
				
			});
			firstCol.add(resolutionSelect);
			firstCol.row();
			firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "fullscreen"), style.fieldLabelStyle))
					.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
			fullscreenCheckBox = new CheckboxWithSound(Strings.getString(UIManager.STRING_TABLE, "fullscreenCheckbox"), style.checkboxStyle);
			fullscreenCheckBox.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					resolutionChanged = fullscreenCheckBox.isChecked() != Gdx.graphics.isFullscreen();
				}
				
			});
			firstCol.add(fullscreenCheckBox).left();
			firstCol.row();
		}
		
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "audio"), style.headingStyle))
					.fill().colspan(2).padBottom(style.headingMarginBottom).padTop(style.headingMarginTop).padLeft(style.headingMarginLeft).padRight(style.headingMarginRight);;
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "soundEffectsVolume"), style.fieldLabelStyle))
					.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		audioSliderWithSound = new SliderWithSound(0, 10, 1, false, style.audioSliderStyle);
		audioSliderWithSound.setHeight(style.sliderHeight);
		audioSliderWithSound.setWidth(style.sliderWidth);
		audioSliderWithSound.setAnimateDuration(0.1f);
		firstCol.add(audioSliderWithSound);
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "musicVolume"), style.fieldLabelStyle))
					.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		musicSliderWithSound = new SliderWithSound(0, 10, 1, false, style.musicSliderStyle);
		musicSliderWithSound.setHeight(style.sliderHeight);
		musicSliderWithSound.setWidth(style.sliderWidth);
		musicSliderWithSound.setAnimateDuration(0.1f);
		firstCol.add(musicSliderWithSound);
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "uiVolume"), style.fieldLabelStyle))
				.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		uiEffectsSliderWithSound= new SliderWithSound(0, 10, 1, false, style.uiEffectsSliderStyle);
		uiEffectsSliderWithSound.setHeight(style.sliderHeight);
		uiEffectsSliderWithSound.setWidth(style.sliderWidth);
		uiEffectsSliderWithSound.setAnimateDuration(0.1f);
		firstCol.add(uiEffectsSliderWithSound);
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "characterBarks"), style.fieldLabelStyle))
				.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		characterBarksCheckbox = new CheckboxWithSound(Strings.getString(UIManager.STRING_TABLE, "characterBarksCheckbox"), style.checkboxStyle);
		firstCol.add(characterBarksCheckbox).left();
		firstCol.row();
		
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "userInterface"), style.headingStyle))
					.fill().colspan(2).padBottom(style.headingMarginBottom).padTop(style.headingMarginTop).padLeft(style.headingMarginLeft).padRight(style.headingMarginRight);;
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "scrollSpeed"), style.fieldLabelStyle))
					.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		scrollSpeedSliderWithSound = new SliderWithSound(10, 100, 10, false, style.scrollSpeedSliderStyle);
		scrollSpeedSliderWithSound.setHeight(style.sliderHeight);
		scrollSpeedSliderWithSound.setWidth(style.sliderWidth);
		scrollSpeedSliderWithSound.setAnimateDuration(0.1f);
		firstCol.add(scrollSpeedSliderWithSound);
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "tooltipDelay"), style.fieldLabelStyle))
					.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		tooltipDelaySliderWithSound = new SliderWithSound(0, 10, 1, false, style.tooltipDelaySliderStyle);
		tooltipDelaySliderWithSound.setHeight(style.sliderHeight);
		tooltipDelaySliderWithSound.setWidth(style.sliderWidth);
		tooltipDelaySliderWithSound.setAnimateDuration(0.1f);
		firstCol.add(tooltipDelaySliderWithSound);
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "combatSpeed"), style.fieldLabelStyle))
				.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		combatSpeedSliderWithSound = new SliderWithSound(1, 4, 0.2f, false, style.combatSpeedSliderStyle);
		combatSpeedSliderWithSound.setHeight(style.sliderHeight);
		combatSpeedSliderWithSound.setWidth(style.sliderWidth);
		combatSpeedSliderWithSound.setAnimateDuration(0.1f);
		firstCol.add(combatSpeedSliderWithSound);
		firstCol.row();
		firstCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "neverMoveMouse"), style.fieldLabelStyle))
				.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
		mouseMoveCheckbox = new CheckboxWithSound(Strings.getString(UIManager.STRING_TABLE, "mouseMoveCheckbox"), style.checkboxStyle);
		firstCol.add(mouseMoveCheckbox).left();
		firstCol.row();
		
		secondCol.add(new Label(Strings.getString(UIManager.STRING_TABLE, "keyBindings"), style.headingStyle))
					.fill().colspan(2).padLeft(style.headingMarginLeft).padRight(style.headingMarginRight);
		secondCol.row();
		
		keyButtons = new Array<KeyBindingButton>();
		
		for (KeyBindings binding : KeyBindings.values()) {
			secondCol.add(new Label(binding.getUIName(), style.fieldLabelStyle))
					.fill().padLeft(style.itemLabelMarginLeft).padRight(style.itemLabelMarginRight).padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
			KeyBindingButton button = new KeyBindingButton(binding, style.keyBindingsButtonStyle);
			secondCol.add(button).fill().padTop(style.itemMarginTop).padBottom(style.itemMarginBottom);
			keyButtons.add(button);
			secondCol.row();
		}
		
		firstCol.pack();
		secondCol.pack();
		
		backScrollPane = new ScrollPane(secondCol, style.keyBindingsScrollPaneStyle);
		backScrollPane.setFadeScrollBars(false);
		backScrollPane.setOverscroll(false, false);
		backScrollPane.layout();
		
		Table table = new Table();
		table.add(firstCol).align(Align.top);
		table.add(backScrollPane).align(Align.top).fill().prefHeight(firstCol.getHeight()).prefWidth(secondCol.getWidth()+backScrollPane.getScrollBarWidth());

		return table;
	}

	private Table buildButtons() {
		Table buttonRow = new Table();

		final TextButtonWithSound backButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "cancel"), style.cancelButtonStyle);
		backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				callback.onClose(GameOptionsPanel.this);
			}
		});	

		final TextButtonWithSound okButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "ok"), style.okButtonStyle);
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applyConfiguration();
				saveConfiguration();
				callback.onClose(GameOptionsPanel.this);
			}
		});
		
		addListener(new ConfirmCancelKeyboardListener(okButton, backButton));

		buttonRow.add(backButton).prefWidth(style.backButtonWidth)
				.prefHeight(style.backButtonHeight).padBottom(style.backButtonMarginBottom)
				.padLeft(style.backButtonMarginLeft)
				.padRight(style.backButtonMarginRight)
				.padTop(style.backButtonMarginTop);

		buttonRow.add(okButton).prefWidth(style.okButtonWidth)
				.prefHeight(style.okButtonHeight).padBottom(style.okButtonMarginBottom)
				.padLeft(style.okButtonMarginLeft)
				.padRight(style.okButtonMarginRight)
				.padTop(style.okButtonMarginTop);
		
		return buttonRow;
	}
	
	// adapted from com.badlogic.gdx.backends.lwjgl.LwjglGraphics.setWindowedMode(int, int)
	private DisplayMode findFullscreenDisplayMode(Resolution resolution) {
		DisplayMode[] modes = Gdx.graphics.getDisplayModes();
		DisplayMode monitorDisplayMode = Gdx.graphics.getDisplayMode();
		DisplayMode foundDisplayMode = null;
		int freq = 0;

		for (int i = 0; i < modes.length; i++) {
			DisplayMode current = modes[i];

			if ((current.width == resolution.getWidth()) && (current.height == resolution.getHeight())) {
				if ((foundDisplayMode == null) || (current.refreshRate >= freq)) {
					if ((foundDisplayMode == null) || (current.bitsPerPixel > foundDisplayMode.bitsPerPixel)) {
						foundDisplayMode = current;
						freq = foundDisplayMode.refreshRate;
					}
				}

				// if we've found a match for bpp and frequence against the
				// original display mode then it's probably best to go for this one
				// since it's most likely compatible with the monitor
				if ((current.bitsPerPixel == monitorDisplayMode.bitsPerPixel)
					&& (current.refreshRate == monitorDisplayMode.refreshRate)) {
					foundDisplayMode = current;
					break;
				}
			}
		}
		return foundDisplayMode;
	}

	private void applyConfiguration() {
		if (resolutionSelect != null && resolutionChanged) {
			Resolution selectedMode = resolutionSelect.getSelected().value;
			if (fullscreenCheckBox.isChecked()) {
				Gdx.graphics.setFullscreenMode(findFullscreenDisplayMode(selectedMode));
			} else {
				Gdx.graphics.setWindowedMode(selectedMode.getWidth(), selectedMode.getHeight());
			}
			
			Configuration.setScreenWidth(selectedMode.getWidth());
			Configuration.setScreenHeight(selectedMode.getHeight());
			Configuration.setFullscreen(fullscreenCheckBox.isChecked());
		}

		Configuration.setCharacterBarksEnabled(characterBarksCheckbox.isChecked());
		Configuration.setUIEffectsVolume(uiEffectsSliderWithSound.getValue() / 10f);
		Configuration.setMusicVolume(musicSliderWithSound.getValue() / 10f);
		Configuration.setSoundEffectsVolume(audioSliderWithSound.getValue() / 10f);
		Configuration.setScrollSpeed(scrollSpeedSliderWithSound.getValue());
		Configuration.setTooltipDelay(tooltipDelaySliderWithSound.getValue() / 10f);
		Configuration.setCombatSpeedMultiplier(combatSpeedSliderWithSound.getValue());
		Configuration.setMoveMouse(!mouseMoveCheckbox.isChecked());
		
		for (KeyBindingButton keyButton : keyButtons) {
			KeyBindings binding = keyButton.getBinding();
			binding.getKeys().clear();
			binding.getKeys().addAll(keyButton.getSelectedKeys());
		}
	}
	
	private void saveConfiguration() {
		Configuration.writeOptions(Gdx.files);
	}
	
	private void updateResolution() {
		if (resolutionSelect != null) {
			int currWidth = Gdx.graphics.getWidth();
			int currHeight = Gdx.graphics.getHeight();
			boolean found = false;
			originalResolution = null;
			Array<SelectOption<Resolution>> options = resolutionSelect.getItems();
			for (SelectOption<Resolution> option : options) {
				if (option.value.getWidth() == currWidth && option.value.getHeight() == currHeight) {
					if (customResolutionOption != null && option != customResolutionOption) {
						options.removeValue(customResolutionOption, false);
						resolutionSelect.setItems(new Array<SelectOption<Resolution>>(options));
						customResolutionOption = null;
					}
					resolutionSelect.setSelected(option);
					originalResolution = option;
					found = true;
					break;
				}
			}
			if (!found) {
				if (customResolutionOption != null && options.contains(customResolutionOption, false)) {
					options.removeValue(customResolutionOption, false);
				}
				customResolutionOption = new SelectOption<Resolution>(currWidth
						+ "x" + currHeight + " (" + Strings.getString(UIManager.STRING_TABLE, "customResolution") + ")", new CustomResolution(currWidth,
						currHeight));
				options.add(customResolutionOption);
				resolutionSelect.setItems(new Array<SelectOption<Resolution>>(options));
				resolutionSelect.setSelected(customResolutionOption);
				originalResolution = customResolutionOption;
			}
		}
		
		if (fullscreenCheckBox != null) {
			fullscreenCheckBox.setChecked(Gdx.graphics.isFullscreen());
		}
	}
	
	public void setConfigurationValuesToFields() {
		updateResolution();
		resolutionChanged = false;
		
		audioSliderWithSound.setValue((int) (Configuration.getSoundEffectsVolume() * 10));
		musicSliderWithSound.setValue((int) (Configuration.getMusicVolume() * 10));
		uiEffectsSliderWithSound.setValue((int) (Configuration.getUIEffectsVolume() * 10));
		characterBarksCheckbox.setChecked(Configuration.areCharacterBarksEnabled());
		scrollSpeedSliderWithSound.setValue(Configuration.getScrollSpeed());
		tooltipDelaySliderWithSound.setValue((int) (Configuration.getTooltipDelay()*10));
		combatSpeedSliderWithSound.setValue(Configuration.getCombatSpeedMultiplier());
		mouseMoveCheckbox.setChecked(!Configuration.getMoveMouse());
		
		for (KeyBindingButton keyButton : keyButtons) {
			keyButton.reset();
		}
	}
	
	public void onResize() {
		if (!resolutionChanged) {
			updateResolution();
			resolutionChanged = false;
		}
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if (fullscreenCheckBox != null && resolutionSelect != null) {
			fullscreenCheckBox.setDisabled(false);
			SelectOption<Resolution> selectedResolution = resolutionSelect.getSelected();
			if (selectedResolution != null && selectedResolution.value instanceof CustomResolution) {
				fullscreenCheckBox.setDisabled(true);
				if (fullscreenCheckBox.isChecked()) {
					fullscreenCheckBox.setChecked(false);
				}
			}
		}
	}
	

	public static class GameOptionsPanelStyle extends BorderedWindowStyle {
		int okButtonWidth = 70,
			okButtonHeight = 30,
			okButtonMarginTop = 0,
			okButtonMarginBottom = 10,
			okButtonMarginLeft = 15,
			okButtonMarginRight = 15,
			backButtonWidth = 70,
			backButtonHeight = 30,
			backButtonMarginTop = 0,
			backButtonMarginBottom = 10,
			backButtonMarginLeft = 15,
			backButtonMarginRight = 15,
			headingMarginTop = 10, 
		   	headingMarginBottom = 0,
   			headingMarginLeft = 0,
			headingMarginRight = 0,
		   	itemMarginTop = 0,
		   	itemMarginBottom = 0,
		   	itemLabelMarginLeft = 0,
		   	itemLabelMarginRight = 10,
			sliderHeight = 20,
			sliderWidth = 200;
		SliderWithSoundStyle audioSliderStyle, musicSliderStyle, uiEffectsSliderStyle, tooltipDelaySliderStyle, scrollSpeedSliderStyle, combatSpeedSliderStyle;
		LabelStyle headingStyle, fieldLabelStyle;
		CheckBoxWithSoundStyle checkboxStyle;
		TextButtonWithSoundStyle okButtonStyle, cancelButtonStyle;
		SelectBoxWithSoundStyle resolutionSelectStyle;
		KeyBindingButtonStyle keyBindingsButtonStyle;
		ScrollPaneStyle keyBindingsScrollPaneStyle;
		TableStyle firstColStyle, secondColStyle;
		Drawable headerImage; // optional
	}

}
