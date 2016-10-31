package mg.fishchicken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ContainerStyle {
	
	public Drawable background;
	public int padTop, padBottom, padLeft, padRight;
	
	public void apply(Container<?> container) {
		container.setBackground(background);
		container.pad(padTop, padLeft, padBottom, padRight);
	}
}
