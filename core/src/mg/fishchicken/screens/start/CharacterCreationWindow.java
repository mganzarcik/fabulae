package mg.fishchicken.screens.start;

import java.io.IOException;
import java.util.Comparator;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.Gender;
import mg.fishchicken.gamelogic.characters.Race;
import mg.fishchicken.gamelogic.characters.Role;
import mg.fishchicken.graphics.models.CharacterModel;
import mg.fishchicken.screens.start.AudioProfileSelectionField.AudioProfileSelectionFieldStyle;
import mg.fishchicken.screens.start.ModelSelectionField.ModelSelectionFieldStyle;
import mg.fishchicken.screens.start.PortraitSelectionField.PortraitSelectionFieldStyle;
import mg.fishchicken.screens.start.RaceInfoPanel.RaceInfoPanelStyle;
import mg.fishchicken.screens.start.StartGameScreen.StartMenuWindowStyle;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.ConfirmCancelKeyboardListener;
import mg.fishchicken.ui.ContainerStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound.SelectBoxWithSoundStyle;
import mg.fishchicken.ui.selectbox.SelectOption;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ForcedScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

public class CharacterCreationWindow extends BorderedWindow {
	
	private CharacterCreationWindowStyle style ;
	private GameCharacter character;
	private TextField nameField;
	private PortraitSelectionField portraitField;
	private ModelSelectionField modelField;
	private AudioProfileSelectionField audioProfileField;
	private SelectBoxWithSound<SelectOption<Role>> roleField;
	private SelectBoxWithSound<SelectOption<Race>> raceField;
	private SelectBoxWithSound<SelectOption<Gender>> genderField;
	private RaceInfoPanel raceInfoPanel;
	private Label raceDescription, roleDescription;
	private TextButtonWithSound okButton;
	private Array<Role> availableRoles;
	private OkCancelCallback<GameCharacter> callback;
	private boolean editOnly;
	
	/**
	 * Creates a new character creation window that will be only used to edit existing characters
	 * during ongoing game.
	 * 
	 * {@link #setCharacter(GameCharacter)} must be called to set the edited character before displaying
	 * the window.
	 * 
	 * @param skin
	 * @param styleName
	 * @param portraits
	 * @param callback
	 */
	public CharacterCreationWindow(final Skin skin, String styleName, final OkCancelCallback<GameCharacter> callback) {
		this(skin, styleName, Role.getAllSelectableRoles(), true, callback);
	}
	
	/**
	 * Creates a new character creation window that can be used to edit characters before the start of a game.
	 * 
	 * {@link #setCharacter(GameCharacter)} must be called to set the edited character before displaying
	 * the window.
	 * 
	 * @param skin
	 * @param styleName
	 * @param availableRoles
	 * @param editOnly
	 * @param portraits
	 * @param callback
	 */
	public CharacterCreationWindow(final Skin skin, String styleName, Array<Role> availableRoles, final OkCancelCallback<GameCharacter> callback) {
		this(skin, styleName, availableRoles, false, callback);
	}
	
	private CharacterCreationWindow(final Skin skin, String styleName, Array<Role> availableRoles, boolean editOnly, final OkCancelCallback<GameCharacter> callback) {
		super(Strings.getString(
				UIManager.STRING_TABLE, "characterCreationTitle"), skin.get(styleName, CharacterCreationWindowStyle.class));

		this.editOnly = editOnly;
		this.callback = callback;
		this.style = (CharacterCreationWindowStyle)super.getStyle();
		this.availableRoles = availableRoles;
		build();
	}
	
	private void build() {
		final TextButtonWithSound cancelButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "cancel"), style.backButtonStyle);
		cancelButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				callback.onCancel();
			}
		});
		
		okButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "ok"), style.okButtonStyle);
		okButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				try {
					setValuesToCharacter();
					callback.onOk(character);
				} catch (IOException e) {
					throw new GdxRuntimeException(e);
				}
			}
		});
		
		ConfirmCancelKeyboardListener keyboardListener = new ConfirmCancelKeyboardListener(okButton, cancelButton);
		
		addListener(keyboardListener);
		
		createFields(keyboardListener);
		
		Table buttonRow = new Table();
		buttonRow.add(cancelButton)
						.height(style.backButtonHeight)
						.width(style.backButtonWidth)
						.padLeft(style.backButtonMarginLeft)
						.padRight(style.backButtonMarginRight)
						.padTop(style.backButtonMarginTop)
						.padBottom(style.backButtonMarginBottom);
		buttonRow.add(okButton)
						.height(style.okButtonHeight)
						.width(style.okButtonWidth)
						.padLeft(style.okButtonMarginLeft)
						.padRight(style.okButtonMarginRight)
						.padTop(style.okButtonMarginTop)
						.padBottom(style.okButtonMarginBottom);
		add(buttonRow).align(Align.center).fillX()
			.padTop(style.buttonsMarginBottom).colspan(3);
		
		pack();
	}
	
	public void setCharacter(GameCharacter character) {
		this.character = character;
		
		nameField.setText(character.getName() != null ? character.getName() : "");
		nameField.setDisabled(!character.isPlayerEditable());
		portraitField.setSelected(character.getPortraitFile());
		portraitField.setDisabled(!character.isPlayerEditable());
		CharacterModel model = character.getModel();
		modelField.setSelected(model);
		modelField.setDisabled(!character.isPlayerEditable() || (model != null && !model.isSelectable()));
		audioProfileField.setSelected(character.getAudioProfile());
		audioProfileField.setDisabled(!character.isPlayerEditable());
		
		Role selectedRole = null;
		if (roleField != null) {
			selectedRole = character.getRole();
			SelectOption<Role> selectedOption = selectedRole != null ? new SelectOption<Role>(selectedRole.getName(), selectedRole) : null;
			Array<SelectOption<Role>> options = new Array<SelectOption<Role>>();
			for (Role role : availableRoles) {
				SelectOption<Role> option = new SelectOption<Role>(role.getName(), role);
				options.add(option);
			}
			if (selectedOption != null && !options.contains(selectedOption, false)) {
				options.add(selectedOption);
			}
			
			options.sort(new Comparator<SelectOption<Role>>() {
				@Override
				public int compare(SelectOption<Role> arg0, SelectOption<Role> arg1) {
					return arg0.value.getName().compareTo(arg1.value.getName());
				}
				
			});
			
			if (selectedRole == null) {
				if (character.isPlayerEditable()) {
					selectedOption = options.get(0);
				} else {
					selectedOption = new SelectOption<Role>("", null);
					options.insert(0, selectedOption);
				}
				selectedRole = selectedOption.value;
			}
			
			roleField.setItems(options);
			
			if (selectedOption != null) {
				roleField.setSelected(selectedOption);
			} 
			
			roleField.setDisabled(!character.isPlayerEditable() || editOnly);
			roleDescription.setText(selectedRole != null ? selectedRole.getDescription() : "");
		}
		
		Gender selectedGender = character.stats().getGender();
		setGenderOptionsBasedOnRole(selectedRole);
		
		if (selectedGender != null) {
			genderField.setSelected(new SelectOption<Gender>(Strings.getString(
					GameCharacter.STRING_TABLE, selectedGender.name()),
					selectedGender));
		}
		genderField.setDisabled(!character.isPlayerEditable() || editOnly);
		
		if (raceField != null) {
			setRaceOptionsBasedOnRole(selectedRole);
			Race race = character.stats().getRace();
			if (race != null) {
				raceField.setSelected(new SelectOption<Race>(race.getName(), race));
			}
			raceField.setDisabled(!character.isPlayerEditable() || editOnly);
		}
		pack();
	}

	private void setGenderOptionsBasedOnRole(Role role) {
		Array<SelectOption<Gender>> genderOptions = new Array<SelectOption<Gender>>();
		for (Gender gender : (role != null ? role.getGenders().items : Gender.values())) {
			SelectOption<Gender> option = new SelectOption<Gender>(Strings.getString(GameCharacter.STRING_TABLE, gender.name()), gender);
			genderOptions.add(option);
		}
		genderField.setItems(genderOptions);
	}
	
	private void setRaceOptionsBasedOnRole(Role role) {
		Array<SelectOption<Race>> options = new Array<SelectOption<Race>>();
		Array<Race> races = (role != null ? role.getRaces() : Race.getAllPlayableRaces()); 
	    races.sort(new Comparator<Race>() {
			@Override
			public int compare(Race arg0, Race arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		for (Race race : races) {
			SelectOption<Race> option = new SelectOption<Race>(race.getName(), race);
			options.add(option);
		}
		raceField.setItems(options);
	}
	
	private void createFields(ConfirmCancelKeyboardListener keyboardListener) {
		
		Table col1 = new Table();
		Table col2 = new Table();
		Table col3 = new Table();
		
		add(col1).align(Align.top);
		add(col2).align(Align.top);
		add(col3).align(Align.top);
		row();
		
		ObjectMap<String, FileHandle> portraits = Assets.getAssetFiles(Configuration.getFolderPCPortraits(), "png");
		
		portraitField = new PortraitSelectionField(portraits.keys().toArray(), style.portraitFieldStyle);
		col1.add(portraitField).padRight(style.formColsMargin).padBottom(style.formRowsMargin);
		col1.row();
		modelField = new ModelSelectionField(style.modelFieldStyle);
		col1.add(modelField).padRight(style.formColsMargin);
		
		nameField = new TextField("", style.nameFieldStyle);
		nameField.addListener(keyboardListener);
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "name")+":", style.nameFieldLabelStyle))
			.fill().padRight(style.formLabelsMargin).padBottom(style.formDescriptionsMargin).height(style.formRowsHeight);
		col2.add(nameField).fill().padBottom(style.formDescriptionsMargin).padRight(style.formColsMargin).height(style.formRowsHeight);
		
		if (availableRoles != null && availableRoles.size > 0) {
			roleField = new SelectBoxWithSound<SelectOption<Role>>(style.roleFieldStyle);
			roleField.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					SelectOption<Role> selected = roleField.getSelected();
					if (roleDescription != null && selected != null) {
						roleDescription.setText(selected.value != null ? selected.value.getDescription() : "");
						pack();
					}
					if (selected != null) {
						setRaceOptionsBasedOnRole(selected.value);
						setGenderOptionsBasedOnRole(selected.value);
					}
				}
			});
			roleField.setDisabled(editOnly);
			col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "role")+":", style.roleFieldLabelStyle))
				.fill().padRight(style.formLabelsMargin).padBottom(style.formDescriptionsMargin).height(style.formRowsHeight);
			col2.add(roleField).fill().padBottom(style.formDescriptionsMargin).height(style.formRowsHeight);
			col2.row();
			roleDescription = new Label("", style.roleDescriptionStyle);
			roleDescription.setWrap(true);
			roleDescription.setAlignment(Align.topLeft);
			
			Container<Label> roleContainer = new Container<Label>(roleDescription);
			if (style.roleDescriptionContainerStyle != null) {
				style.roleDescriptionContainerStyle.apply(roleContainer);
			}
			roleContainer.fill();
			
			ForcedScrollPane scrollPane = new ForcedScrollPane(roleContainer, style.roleDescriptionScrollPaneStyle);
			col2.add(scrollPane).colspan(4).fill().padBottom(style.formRowsMargin).height(style.roleDescriptionHeight);
		} else {
			col2.add();
			col2.add();
		}
		
		col2.row();
		
		Array<Race> races = Race.getAllPlayableRaces();
		if (races.size > 1) {
			raceField = new SelectBoxWithSound<SelectOption<Race>>(style.raceFieldStyle);
			raceField.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					SelectOption<Race> selected = raceField.getSelected();
					if (selected != null) {
						raceInfoPanel.setRace(selected.value);
						raceDescription.setText(selected.value.getDescription());
						pack();
					}
				}
			});
			raceField.setDisabled(editOnly);
			col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "race")+":", style.raceFieldLabelStyle))
				.fill().padRight(style.formLabelsMargin).padBottom(style.formDescriptionsMargin);
			col2.add(raceField).fill().padBottom(style.formDescriptionsMargin).padRight(style.formColsMargin);
		}
		
		genderField = new SelectBoxWithSound<SelectOption<Gender>>(style.genderFieldStyle);
		genderField.setDisabled(editOnly);
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "gender")+":", style.genderFieldLabelStyle))
			.fill().padRight(style.formLabelsMargin).padBottom(style.formDescriptionsMargin).height(style.formRowsHeight);
		col2.add(genderField).fill().padBottom(style.formDescriptionsMargin).height(style.formRowsHeight);
		
		if (raceField == null) { 
			col2.add();
			col2.add();
		} else {
			col2.row();
			raceDescription = new Label(races.get(0).getDescription(), style.raceFieldDescriptionStyle);
			raceDescription.setWrap(true);
			raceDescription.setAlignment(Align.topLeft);
			
			Container<Label> raceContainer = new Container<Label>(raceDescription);
			if (style.raceDescriptionContainerStyle != null) {
				style.raceDescriptionContainerStyle.apply(raceContainer);
			}
			raceContainer.fill();
			
			ForcedScrollPane scrollPane = new ForcedScrollPane(raceContainer, style.raceFieldDescriptionScrollPaneStyle);
			col2.add(scrollPane).colspan(4).fill().padBottom(style.formRowsMargin).height(style.raceDescriptionHeight);
		}
		col2.row();
		
		audioProfileField = new AudioProfileSelectionField(style.audioProfileFieldStyle);
		
		col2.add(new Label(Strings.getString(GameCharacter.STRING_TABLE, "audioProfile")+":", style.audioProfileFieldLabelStyle))
			.fill().padRight(style.formLabelsMargin).padBottom(style.formDescriptionsMargin).height(style.formRowsHeight);
		col2.add(audioProfileField).fill().padBottom(style.formDescriptionsMargin).height(style.formRowsHeight);
		col2.row();
		
		raceInfoPanel = new RaceInfoPanel(style.raceInfoPanelStyle, editOnly);
		col3.add(raceInfoPanel).padLeft(style.formColsMargin);
	}
	
	private void setValuesToCharacter() throws IOException {
		character.setName(nameField.getText());
		character.setDescription(nameField.getText());
		character.setPortraitFile(portraitField.getSelected());
		if (!editOnly) {
			character.stats().setGender(genderField.getSelected().value);
			if (roleField != null && roleField.getSelected() != null) {
				Role currentRole = character.getRole();
				if (currentRole != null && roleField.getSelected().value != currentRole) {
					availableRoles.add(currentRole);
				}
				character.setRole(roleField.getSelected().value);
			}
			Race race = Race.getAllPlayableRaces().first();
			if (raceField != null && raceField.getSelected() != null) {
				race = raceField.getSelected().value;
			}
			character.stats().setRace(race)
							 .setHPMax(race.getMaxHPGain())
							 .setHPAct(race.getMaxHPGain())
							 .setMPMax(race.getMaxMPGain())
							 .setMPAct(race.getMaxMPGain())
							 .setSPMax(race.getMaxSPGain())
							 .setSPAct(race.getMaxSPGain())
							 .setAPAct(race.getMaxAP())
							 .setLevel(1)
							 .setExperience(0)
							 .setInvincible(false)
							 .setPerkPoints(Configuration.getPerkPointGainPerLevel())
							 .setSkillPoints(0)
							 .skills()
							 	.set(race.getInherentSkills());
			character.getInventory().clear();
			// model must be set before inventory is manipulated and after stats are set
			character.setModel(modelField.getSelected(), audioProfileField.getSelected());
			race.getInventory().copyAllItemsTo(character.getInventory());
		} else {
			character.setModel(modelField.getSelected(), audioProfileField.getSelected());
		}
	}

	@Override
	public void act(float delta) {
		if (character == null) {
			return;	
		}
		if (StringUtil.nullOrEmptyString(nameField.getText()) ||
				genderField.getSelected() == null ||
				(roleField != null && roleField.getSelected() == null) || 
				(raceField != null && raceField.getSelected() == null)) {
			okButton.setDisabled(true);
		} else {
			okButton.setDisabled(false);
		}
		super.act(delta);
	}

	public static class CharacterCreationWindowStyle extends
			StartMenuWindowStyle {
		TextButtonWithSoundStyle backButtonStyle, okButtonStyle;
		PortraitSelectionFieldStyle portraitFieldStyle;
		ModelSelectionFieldStyle modelFieldStyle;
		TextFieldStyle nameFieldStyle;
		LabelStyle nameFieldLabelStyle, roleFieldLabelStyle,
				roleDescriptionStyle, raceFieldLabelStyle,
				raceFieldDescriptionStyle, genderFieldLabelStyle,
				audioProfileFieldLabelStyle;
		SelectBoxWithSoundStyle roleFieldStyle, raceFieldStyle, genderFieldStyle;
		ScrollPaneStyle roleDescriptionScrollPaneStyle,
				raceFieldDescriptionScrollPaneStyle;
		AudioProfileSelectionFieldStyle audioProfileFieldStyle;
		RaceInfoPanelStyle raceInfoPanelStyle;
		ContainerStyle raceDescriptionContainerStyle, roleDescriptionContainerStyle;
		int okButtonWidth = 70, okButtonHeight = 30, okButtonMarginTop = 0,
				okButtonMarginBottom = 10, okButtonMarginLeft = 15,
				okButtonMarginRight = 15, backButtonWidth = 70,
				backButtonHeight = 30, backButtonMarginTop = 0,
				backButtonMarginBottom = 10, backButtonMarginLeft = 15,
				backButtonMarginRight = 15, formLabelsMargin = 10,
				formRowsMargin = 10, formRowsHeight = 30, formColsMargin = 20,
				formDescriptionsMargin = 5, roleDescriptionHeight = 160,
				raceDescriptionHeight = 125;
	}

}
