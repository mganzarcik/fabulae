package mg.fishchicken.screens.start;

import java.io.IOException;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter.State;
import mg.fishchicken.graphics.animations.CharacterAnimationMap;
import mg.fishchicken.graphics.models.CharacterModel;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class ModelSelectionField extends Table {

	private Array<CharacterModel> models;
	private CharacterModel selected;
	private Image modelImage;
	private TextButtonWithSound nextButton, prevButton;
	
	public ModelSelectionField(ModelSelectionFieldStyle style) {
		super();
		models = CharacterModel.getAllSelectableModels(); 
		modelImage = new Image();
		Container<Image> imageContainer = new Container<Image>(modelImage);
		imageContainer.setBackground(style.modelBackground);
		add(imageContainer).width(style.modelWidth).height(style.modelHeight).fill().colspan(2);
		row();
		prevButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "previousModel"), style.prevButtonStyle);
		prevButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int prevIndex = models.indexOf(selected, false)-1;
				if (prevIndex < 0) {
					prevIndex = models.size-1;
				}
				setSelected(models.get(prevIndex));
			}
		});
		add(prevButton).fill().width(style.buttonsWidth).height(style.buttonsHeight).padTop(style.buttonsMarginTop);
		
		nextButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "nextModel"), style.nextButtonStyle);
		nextButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int nextIndex = models.indexOf(selected, false)+1;
				if (nextIndex >= models.size) {
					nextIndex = 0;
				}
				setSelected(models.get(nextIndex));
			}
		});
		add(nextButton).fill().width(style.buttonsWidth).height(style.buttonsHeight).padTop(style.buttonsMarginTop);
	}
	
	public CharacterModel getSelected() {
		return selected;
	}
	
	public void setSelected(CharacterModel model) {
		if (model == null) {
			model = models.random();
		}

		try {
			selected = model;
			CharacterAnimationMap map = new CharacterAnimationMap(selected, null, 1);
			modelImage.setDrawable(new TextureRegionDrawable(map.getAnimation(
					State.IDLE, Orientation.DOWN, true).getKeyFrame(0)));
		} catch (IOException e) {
			Log.log("Error displaying model {0}.", LogType.ERROR, model.getId());
			selected = null;
		}
	}
	
	public void setDisabled(boolean value) {
		nextButton.setVisible(!value);
		prevButton.setVisible(!value);
	}
	
	public static class ModelSelectionFieldStyle {
		public int modelWidth = 128, 
				   modelHeight = 128,
				   buttonsHeight = 30,
				   buttonsWidth = 30,
				   buttonsMarginTop = 2;
		public Drawable modelBackground;
		public TextButtonWithSoundStyle prevButtonStyle, nextButtonStyle;
	}
}
