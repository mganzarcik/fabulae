package mg.fishchicken.screens.start;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.gamelogic.characters.PlayerCharacter;
import mg.fishchicken.screens.start.CharacterCreationButtonCallback.CharacterCreationCallback;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CharacterCreationButton extends Container<Actor> {

	private PlayerCharacter character;
	private TextButtonWithSound noCharacter, editCharacter, deleteCharacter;
	private boolean changed;
	private String characterPortraitFile;
	private Image characterImage;
	
	public CharacterCreationButton(final CharacterCreationButtonStyle style, final CharacterCreationButtonCallback callback) {
		super();
		changed = true;
		noCharacter = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "createCharacter"), style.newCharacterButtonStyle);
		noCharacter.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				callback.onCreate(new CharacterCreationCallback() {
					@Override
					public void setCharacter(PlayerCharacter newCharacter) {
						character = newCharacter;
						changed = true;
					}
				});
			}
		});
		
		editCharacter = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "editCharacter"), style.editCharacterButtonStyle);
		editCharacter.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				callback.onEdit(character);
				changed = true;
			}
		});
		
		deleteCharacter = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "deleteCharacter"), style.deleteCharacterButtonStyle);
		deleteCharacter.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				callback.onDelete(character);
				character = null;
				changed = true;
			}
		});
	}	
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if (changed) {
			changed = false;
			
			this.setActor(null);
			
			if (character == null) {
				setActor(noCharacter);
			} else {
				Table characterTable = new Table();
				characterImage = null;
				if (character.getPortraitFile() != null) {
					characterPortraitFile = character.getPortraitFile();
					characterImage = new Image(character.getPortrait());
				}
				characterTable.add(characterImage).expand().fill().colspan(2);
				characterTable.row();
				characterTable.add(editCharacter);
				characterTable.add(deleteCharacter);
				setActor(characterTable);
			}
			fill();
			pack();
		} else if (character != null && !CoreUtil.equals(characterPortraitFile, character.getPortraitFile())) {
			characterPortraitFile = character.getPortraitFile();
			if (characterImage != null) {
				characterImage.setDrawable(new TextureRegionDrawable(character.getPortrait()));
			} else {
				characterImage = new Image(character.getPortrait());
			}
		}
	}
	
	public PlayerCharacter getCharacter() {
		return character;
	}
	
	public static class CharacterCreationButtonStyle {
		TextButtonWithSoundStyle newCharacterButtonStyle, editCharacterButtonStyle, deleteCharacterButtonStyle;
	}
}
