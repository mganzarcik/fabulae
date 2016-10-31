package mg.fishchicken.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TableStyle {
	public Drawable background;
	public int padTop, padBottom, padLeft, padRight;
	
	public void apply(Table table) {
		table.setBackground(background);
		table.pad(padTop, padLeft, padBottom, padRight);
	}
}
