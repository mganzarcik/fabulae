package mg.fishchicken.screens.start;

import java.util.Iterator;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.PlayerCharacter;
import mg.fishchicken.gamelogic.characters.Role;
import mg.fishchicken.gamelogic.characters.groups.Formation;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.screens.start.CharacterCreationButton.CharacterCreationButtonStyle;
import mg.fishchicken.screens.start.CharacterCreationButtonCallback.CharacterCreationCallback;
import mg.fishchicken.screens.start.StartGameScreen.StartMenuWindowStyle;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class PartyCreationScreen extends SelfLoadingScreen {

	private enum ScreenState {
		CHARACTER_SELECTION,
		CHARACTER_CREATION,
		CHARACTER_EDIT
	}
	
	private static final String STYLE_NAME = "partyCreationStyle";
	public static final String CREATED_TYPE = "created";
	
	private GameState gameState;
	private FishchickenGame game;
	private BorderedWindow characterSelectionMenu; 
	private CharacterCreationWindow characterCreationWindow;
	private Array<CharacterCreationButton> group;
	private int createdCharCount;
	private TextButtonWithSound startGameButton;
	private ScreenState currentState;
	private CharacterCreationCallback currentCharacterSetter;
	private PlayerCharacter editedCharacter;
	private Array<Role> mandatoryRoles;
	private Array<Role> availableRoles;
	private Label startGameWarning;
	private Cell<?> startGameWarningCell;
	private CharacterCreationWindowLoader characterCrationLoader;
	
	public PartyCreationScreen(FishchickenGame game, GameState gameState) {
		super();
		createdCharCount = 0;
		this.gameState = gameState;
		this.game = game;
		this.group = new Array<CharacterCreationButton>();
		this.characterCrationLoader= new CharacterCreationWindowLoader();
		this.currentState = ScreenState.CHARACTER_SELECTION;
		this.availableRoles = Role.getAllSelectableRoles();
		this.mandatoryRoles = new Array<Role>(Role.class);
		for (Role role : availableRoles) {
			if (role.isMandatory()) {
				mandatoryRoles.add(role);
			}
		}
	}
	
	@Override
	protected void build() {
		stage.clear();
		
		createCharacterSelectionMenu();
		createCharacterCreationMenu();
	}
	
	private void createCharacterSelectionMenu() {
		PartyCreationWindowStyle style = skin.get("partyCreationMenu",
				PartyCreationWindowStyle.class);
		
		characterSelectionMenu = new BorderedWindow(Strings.getString(
				UIManager.STRING_TABLE, "partyCreationTitle"), style);
		characterSelectionMenu.setMovable(false);
		
		int count = Configuration.getNumberOfCharactersToCreate();

		if (style.headerImage != null) {
			characterSelectionMenu.add(new Image(style.headerImage))
				.colspan(style.cols)
				.padBottom(style.buttonsMarginTop);
			characterSelectionMenu.row();
		}
		
		for (int i = 0; i < count; ++i) {
			CharacterCreationButton button = createCharacterButton(i, characterSelectionMenu, style.characterCreationButtonStyle);
			group.add(button);
			Cell<?> cell = characterSelectionMenu.add(button)
									.fill()
									.prefWidth(style.buttonWidth)
									.prefHeight(style.buttonHeight)
									.padBottom(style.buttonSpacing/2f);
			if (i >= style.cols) {
				cell.padTop(style.buttonSpacing/2f);
			}
			if (i < count - (count / style.cols)) {
				cell.padBottom(style.buttonSpacing/2f);
			}
			if ((i+1) % style.cols == 0) {
				characterSelectionMenu.row();
				cell.padRight(style.buttonsMarginRight);
			} else {
				cell.padRight(style.buttonSpacing/2f);
			}
			if ((i+1) % style.cols == 1) {
				cell.padLeft(style.buttonsMarginLeft);
			} else {
				cell.padLeft(style.buttonSpacing/2f);
			}
		}
		
		characterSelectionMenu.row();
		
		final TextButtonWithSound backButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "back"), style.backButtonStyle);
		backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.setScreen(new StartGameScreen(game, gameState));
			}
		});
		
		startGameButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "startGame"), style.startButtonStyle);
		startGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				startNewGame();
			}
		});
		
		startGameWarning = new Label("", style.startGameWarningStyle);
		startGameWarning.setWrap(true);
		
		if (mandatoryRoles.size < 1) {
			startGameWarning.setText(Strings.getString(UIManager.STRING_TABLE, "needToCreateAtLeastOneChar"));
		} else {
			Object[] roles = new Object[mandatoryRoles.size];
			int i = 0;
			for (Role role : mandatoryRoles) {
				roles[i++] = role.getName();
			}
			startGameWarning.setText(Strings.getString(UIManager.STRING_TABLE, "needToCreateRoles", roles));
		}
		
		startGameWarningCell = characterSelectionMenu.add(startGameWarning).colspan(style.cols).align(Align.center).fillX()
			.padTop(style.buttonsMarginBottom).padLeft(style.buttonsMarginLeft).padRight(style.buttonsMarginRight);
		characterSelectionMenu.row();
		
		Table buttonRow = new Table();
		buttonRow.add(backButton)
						.height(style.backButtonHeight)
						.width(style.backButtonWidth)
						.padLeft(style.backButtonMarginLeft)
						.padRight(style.backButtonMarginRight)
						.padTop(style.backButtonMarginTop)
						.padBottom(style.backButtonMarginBottom);
		buttonRow.add(startGameButton)
						.height(style.startButtonHeight)
						.width(style.startButtonWidth)
						.padLeft(style.startButtonMarginLeft)
						.padRight(style.startButtonMarginRight)
						.padTop(style.startButtonMarginTop)
						.padBottom(style.startButtonMarginBottom);
		characterSelectionMenu.add(buttonRow).colspan(style.cols).align(Align.center).fillX()
			.padTop(style.buttonsMarginBottom);

		characterSelectionMenu.pack();
		stage.addActor(characterSelectionMenu);
		WindowPosition.CENTER.position(characterSelectionMenu);
	}
	
	private void createCharacterCreationMenu() {
		characterCreationWindow = new CharacterCreationWindow(skin, "default", availableRoles, new OkCancelCallback<GameCharacter>() {
			
			@Override
			public void onOk(GameCharacter character) {
				++createdCharCount;
				if (currentState == ScreenState.CHARACTER_CREATION) {
					currentCharacterSetter.setCharacter(editedCharacter);
				}
				if (editedCharacter.getRole() != null) {
					availableRoles.removeValue(editedCharacter.getRole(), false);
				}
				currentState = ScreenState.CHARACTER_SELECTION;
			}
			
			@Override
			public void onCancel() {
				currentState = ScreenState.CHARACTER_SELECTION;
			}
		});
		
		characterCreationWindow.setMovable(false);
		characterCreationWindow.setVisible(false);
		stage.addActor(characterCreationWindow);
	}
	
	private CharacterCreationButton createCharacterButton(final int index, Table table, CharacterCreationButtonStyle style) {
		CharacterCreationButton button = new CharacterCreationButton(style,
				new CharacterCreationButtonCallback() {
					@Override
					public void onCreate(CharacterCreationCallback characterSetter) {
						createNewCharacter(index, characterSetter);
					}
					@Override
					public void onEdit(PlayerCharacter character) {
						editCharacter(character);
					}
					
					@Override
					public void onDelete(PlayerCharacter character) {
						removeCharacter(character);
					}
				});
		return button;
	}
	
	@Override
	public void render(float delta) {
		if (characterSelectionMenu != null) {
			characterSelectionMenu.setVisible(currentState == ScreenState.CHARACTER_SELECTION);
			if (currentState == ScreenState.CHARACTER_CREATION || currentState == ScreenState.CHARACTER_EDIT) {
				characterCreationWindow.setVisible(true);
				WindowPosition.CENTER.position(characterCreationWindow);
			} else {
				characterCreationWindow.setVisible(false);
			}
		}
		
		if (startGameButton != null && characterSelectionMenu.isVisible()) {
			boolean booleanOldDisabled = startGameButton.isDisabled();
			boolean disabled = createdCharCount < 1;
			if (!disabled && mandatoryRoles.size > 0) {
				for (Role role : mandatoryRoles) {
					boolean found = false;
					for (CharacterCreationButton charButton : group) {
						PlayerCharacter character = charButton.getCharacter();
						if (character != null && role == character.getRole()) {
							found = true;
							break;
						}
					}
					if (!found) {
						disabled = true;
						break;
					}
				}
			}
			if (booleanOldDisabled != disabled) {
				startGameButton.setDisabled(disabled);
				startGameWarning.setVisible(disabled);
				if (!disabled) {
					startGameWarningCell.clearActor().height(0);
				} else {
					startGameWarningCell.setActor(startGameWarning).height(startGameWarning.getHeight());
				}
				characterSelectionMenu.pack();
				WindowPosition.CENTER.position(characterSelectionMenu);
			}
		}
		super.render(delta);
	}
	
	@Override
	protected void loadAdditionalAssets() {
		super.loadAdditionalAssets();
		characterCrationLoader.load(Assets.getAssetManager());
	}
	
	public void createNewCharacter(int index, CharacterCreationCallback characterSetter) {
		currentState = ScreenState.CHARACTER_CREATION;
		currentCharacterSetter = characterSetter;
		editedCharacter = new PlayerCharacter(CREATED_TYPE+index, CREATED_TYPE);
		characterCreationWindow.setCharacter(editedCharacter);
	}
	
	public void editCharacter(PlayerCharacter character) {
		currentState = ScreenState.CHARACTER_EDIT;
		editedCharacter = character;
		characterCreationWindow.setCharacter(character);
	}
	
	public void removeCharacter(GameCharacter character) {
		Role role = character.getRole();
		if (role!= null && !availableRoles.contains(role, false)) {
			availableRoles.add(role);
		}
		--createdCharCount;
	}
	
	private void startNewGame() {
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		Iterator<CharacterCreationButton> iterator = group.iterator();
		while (iterator.hasNext()) {
			CharacterCreationButton charButton = iterator.next();
			PlayerCharacter character = charButton.getCharacter();
			if (character == null) {
				continue;
			}
			
			Role role = character.getRole();
			if (role == null || role.isMemberAtStart()) {
				pcg.addMember(character);
				pcg.selectMember(character);
				character.undispose();
				iterator.remove(); // remove the button so that the character won't get disposed when we leave this screen
			} else {
				gameState.addUnassignedGameObject(character);
			}
			character.clearAssetReferences();
			pcg.addCreatedCharacter(character);
		}
		Formation startFormation = Configuration.getStartFormation();
		if (startFormation != null) {
			pcg.formation().setFormation(startFormation.getFormation());
		}
		game.switchToMap(Configuration.getStartMap());
		Music.stopPlayingMusic();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (characterSelectionMenu != null) {
			WindowPosition.CENTER.position(characterSelectionMenu);
		}
		if (characterCreationWindow != null) {
			WindowPosition.CENTER.position(characterCreationWindow);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		characterCrationLoader.unload(Assets.getAssetManager());
		
	}

	@Override
	protected String getStyleName() {
		return STYLE_NAME;
	}

	@Override
	protected void playMusic() {
	}
	
	public static class PartyCreationWindowStyle extends StartMenuWindowStyle {
		CharacterCreationButtonStyle characterCreationButtonStyle;
		TextButtonWithSoundStyle backButtonStyle, startButtonStyle;
		LabelStyle startGameWarningStyle;
		int cols = 2,
			startButtonWidth = 70,
			startButtonHeight = 30,
			startButtonMarginTop = 0,
			startButtonMarginBottom = 10,
			startButtonMarginLeft = 15,
			startButtonMarginRight = 15,
			backButtonWidth = 70,
			backButtonHeight = 30,
			backButtonMarginTop = 0,
			backButtonMarginBottom = 10,
			backButtonMarginLeft = 15,
			backButtonMarginRight = 15;
	}

	@Override
	protected String getLoadingScreenSubtype() {
		return "partyCreation";
	}
}