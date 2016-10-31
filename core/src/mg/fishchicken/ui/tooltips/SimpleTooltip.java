package mg.fishchicken.ui.tooltips;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class SimpleTooltip extends Table {
	
	private Label label;
	protected SimpleTooltipStyle style;
	protected LabelStyle labelStyle;
	private Cell<?> labelCell;
	
	public SimpleTooltip(SimpleTooltipStyle style) {
		super();
		this.style =style;
		this.labelStyle = style;
		if (style.background != null) {
			setBackground(style.background);
			this.labelStyle = new LabelStyle(style.font, style.fontColor);
		}
		label = new Label("", labelStyle);
		label.setWrap(true);
		label.setAlignment(Align.left, Align.left);
		labelCell = add(label)
				.prefWidth(style.width)
				.pad(style.padTop, style.padLeft, style.padBottom, style.padRight);
	}

	public Label setText(CharSequence newText) {
		labelCell.width(style.width);
		label.setWidth(style.width);
		label.setText(newText);
		label.invalidate();
		pack();
		labelCell.width(label.getGlyphLayout().width);
		invalidateHierarchy();
		pack();
		return label;
	}
	
	public boolean shouldDisplay() {
		return true;
	}
	
	public static class SimpleTooltipStyle extends LabelStyle {
		public float width = 300;
		public int padLeft, padRight, padTop, padBottom;
	}
}
