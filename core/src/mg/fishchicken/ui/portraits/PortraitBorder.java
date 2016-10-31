package mg.fishchicken.ui.portraits;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class PortraitBorder extends Table {

	private Image selectedBorder;
	private Image notSelectedBorder;
	private Button characterPortrait;
	
	public PortraitBorder(Button characterPortrait, Drawable selectedImage, Drawable notSelectedImage) {
		this.characterPortrait = characterPortrait;
		selectedBorder = new Image(selectedImage);
		notSelectedBorder = new Image(notSelectedImage);
	}
	
	@Override
	public void act(float delta) {
		Actor toAdd = null;
		if (characterPortrait.isChecked()) {
			selectedBorder.setVisible(true);
			notSelectedBorder.setVisible(false);
			toAdd = selectedBorder;
		} else {
			selectedBorder.setVisible(false);
			notSelectedBorder.setVisible(true);
			toAdd = notSelectedBorder;
		}
		
		this.clearChildren();
		this.add(toAdd).expand().fill();
		super.act(delta);
	}
	
}
