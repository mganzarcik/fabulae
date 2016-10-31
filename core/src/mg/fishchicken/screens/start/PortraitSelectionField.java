package mg.fishchicken.screens.start;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class PortraitSelectionField extends Table {

	private Array<String> portraits;
	private String selected;
	private Image portraitImage;
	private TextButtonWithSound nextButton, prevButton;
	
	public PortraitSelectionField(Array<String> portraitsToUse, PortraitSelectionFieldStyle style) {
		super();
		this.portraits = portraitsToUse; 
		portraits.sort();
		portraitImage = new Image();
		add(portraitImage).width(style.portraitWidth).height(style.portraitHeight).fill().colspan(2);
		row();
		prevButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "previousPortrait"), style.prevButtonStyle);
		prevButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int prevIndex = portraits.indexOf(selected, false)-1;
				if (prevIndex < 0) {
					prevIndex = portraits.size-1;
				}
				setSelected(portraits.get(prevIndex));
			}
		});
		add(prevButton).fill().width(style.buttonsWidth).height(style.buttonsHeight).padTop(style.buttonsMarginTop);
		
		nextButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "nextPortrait"), style.nextButtonStyle);
		nextButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int nextIndex = portraits.indexOf(selected, false)+1;
				if (nextIndex >= portraits.size) {
					nextIndex = 0;
				}
				setSelected(portraits.get(nextIndex));
			}
		});
		add(nextButton).fill().width(style.buttonsWidth).height(style.buttonsHeight).padTop(style.buttonsMarginTop);
	}
	
	public String getSelected() {
		return selected;
	}
	
	public void setDisabled(boolean value) {
		nextButton.setVisible(!value);
		prevButton.setVisible(!value);
	}
	
	public void setSelected(String portraitFile) {
		selected = portraitFile != null ? portraitFile : portraits.random();
		portraitImage.setDrawable(new TextureRegionDrawable(Assets.getTextureRegion(selected)));
	}
	
	public static class PortraitSelectionFieldStyle {
		public int portraitWidth = 150, 
				   portraitHeight = 200,
				   buttonsHeight = 30,
				   buttonsWidth = 30,
				   buttonsMarginTop = 2;
		
		public TextButtonWithSoundStyle prevButtonStyle, nextButtonStyle;
	}
}
