package mg.fishchicken.ui.saveload;

import java.text.DateFormat;
import java.util.Date;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.ui.TableStyle;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class SavedGameButton extends Button {

	private final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
	private final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.MEDIUM);
	
	private SaveGameDetails game;
	
	public SavedGameButton(SaveGameDetails game, SavedGameButtonStyle style) {
		super(style);
		attachSoundListener();
		this.game = game;
		Table details = new Table();
		if (style.detailsStyle != null) {
			style.detailsStyle.apply(details);
		}
		details.add(new Label(game.getName(), style.nameStyle)).fill().expandX();
		details.row();
		details.add(new Label(game.getCurrentMapName(), style.mapAndDateStyle)).fill().expandX();
		details.row();
		details.add(new Label(game.getGameDate(), style.mapAndDateStyle)).fill().expandX();
		details.row();
		details.add(new Label(game.getCharacters().toString(", "), style.charactersStyle)).fill();
		
		Table savedAt = new Table();
		if (style.savedAtStyle != null) {
			style.savedAtStyle.apply(savedAt);
		}
		
		// this will calculate the width of string with a date of 2000-10-22
		// we determine the min width of the date cell this way to make sure 
		// we do not have variable widht of the cell if the date is shorter (like 2000-1-1)
		Label label = new Label(DATE_FORMAT.format(new Date(972172800000l)), style.savedAtDateStyle);
		label.layout();
		float minWidth = label.getWidth();
		label.setStyle(style.savedAtTimeStyle);
		label.layout();
		minWidth = minWidth < label.getWidth() ? label.getWidth() : minWidth;
		
		label = new Label(DATE_FORMAT.format(game.getSavedAt()), style.savedAtDateStyle);
		label.setAlignment(Align.center);
		savedAt.add(label).fill().minWidth(minWidth);
		savedAt.row();
		label = new Label(TIME_FORMAT.format(game.getSavedAt()), style.savedAtTimeStyle);
		label.setAlignment(Align.center);
		savedAt.add(label).fill().minWidth(minWidth);
		
		add(savedAt).fill();
		add(details).fill().expandX();
	}
	
	private void attachSoundListener() {
		addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				ButtonStyle style = getStyle();
				if (style instanceof SavedGameButtonStyle) {
					SavedGameButtonStyle castStyle = (SavedGameButtonStyle) style;
					if (castStyle.clickSound != null) {
						castStyle.clickSound.play(Configuration.getUIEffectsVolume());
					}
				}
				return false;
			}
		});
	}
	
	public SaveGameDetails getGame() {
		return game;
	}
	
	public static class SavedGameButtonStyle extends ButtonStyle {
		private LabelStyle savedAtDateStyle, savedAtTimeStyle, nameStyle, mapAndDateStyle, charactersStyle;
		private TableStyle detailsStyle, savedAtStyle;
		private Sound clickSound;
	}
	
}
