package mg.fishchicken.screens.start;

import java.util.Comparator;

import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound;
import mg.fishchicken.ui.selectbox.SelectOption;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound.SelectBoxWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class AudioProfileSelectionField extends Table {

	private AudioProfileSelectionFieldStyle style;
	private Array<AudioProfile> audioProfiles;
	private SelectBoxWithSound<SelectOption<AudioProfile>> selectBoxWithSound;
	private AudioTrack<?> currentlyPlayingTrack;
	private Cell<?> SelectBoxWithSoundCell;
	
	public AudioProfileSelectionField(AudioProfileSelectionFieldStyle style) {
		super();
		this.style = style;
		audioProfiles = AudioProfile.getAllSelectableAudioProfiles(); 
		audioProfiles.sort(new Comparator<AudioProfile>() {
			@Override
			public int compare(AudioProfile arg0, AudioProfile arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
			
		});
		selectBoxWithSound = new SelectBoxWithSound<SelectOption<AudioProfile>>(style.selectBoxStyle);
		Array<SelectOption<AudioProfile>> options = new Array<SelectOption<AudioProfile>>();
		for (AudioProfile profile : audioProfiles) {
			options.add(new SelectOption<AudioProfile>(profile.getName(), profile));
		}
		selectBoxWithSound.setItems(options);
		SelectBoxWithSoundCell = add(selectBoxWithSound);
		TextButtonWithSound playButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "playAudioProfile"), style.playButtonStyle);
		playButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				SelectOption<AudioProfile> selected = selectBoxWithSound.getSelected();
				if (selected != null) {
					if (currentlyPlayingTrack != null) {
						currentlyPlayingTrack.stopTrack();
					}
					currentlyPlayingTrack = selected.value.getTrack("bark");
					if (currentlyPlayingTrack != null) {
						currentlyPlayingTrack.play();
					}
				}
			}
		});
		add(playButton).fill().width(style.playButtonWidth).height(style.playButtonHeight).padLeft(style.playButtonMargin);		
	}
	
	@Override
	public void setBounds(float x, float y, float width, float height) {
		super.setBounds(x, y, width, height);
		SelectBoxWithSoundCell.height(height);
		SelectBoxWithSoundCell.width(width-style.playButtonWidth-style.playButtonMargin);
	}
	
	@Override
	public void setHeight(float height) {
		super.setHeight(height);
		SelectBoxWithSoundCell.height(height);
	}
	
	@Override
	public void setWidth(float width) {
		super.setWidth(width);
		SelectBoxWithSoundCell.width(width-style.playButtonWidth-style.playButtonMargin);
	}
	
	public AudioProfile getSelected() {
		SelectOption<AudioProfile> selected = selectBoxWithSound.getSelected();
		return selected != null ? selected.value : null;
	}
	
	public void setSelected(AudioProfile audioProfile) {
		if (audioProfile == null) {
			return;
		}
		selectBoxWithSound.setSelected(new SelectOption<AudioProfile>(audioProfile.getName(), audioProfile));
	}
	
	public void setDisabled(boolean value) {
		selectBoxWithSound.setDisabled(value);
	}
	
	public static class AudioProfileSelectionFieldStyle {
		public TextButtonWithSoundStyle playButtonStyle;
		public SelectBoxWithSoundStyle selectBoxStyle;
		public int playButtonWidth, playButtonHeight, playButtonMargin; 
	}
}
