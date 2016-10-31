package mg.fishchicken.ui.tooltips;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;

/**
 * Simple tooltip that contains multiple labels, 
 * allowing for different formatting per label.
 * @author ANNUN
 *
 */
public class CompositeTooltip extends SimpleTooltip {
	
	public CompositeTooltip(SimpleTooltipStyle style) {
		super(style);
	}

	/**
	 * Erase all contents of the tooltip.
	 */
	@Override
	public void clear() {
		super.clear();
		setHeight(0);
		setWidth(0);
	}
	
	/**
	 * Sets the text of this tooltip to the supplied text. This
	 * will create just one label that will contain the whole text.
	 * 
	 * This will erase any previous content of the tooltip.
	 */
	@Override
	public Label setText(CharSequence newText) {
		clear();
		return addLine(newText);
	}
	
	/**
	 * Add an empty line. In reality, this adds
	 * a line containing a simple space character
	 * using the default style.
	 */
	public void addLine() {
		addLine(" ");
	}
	
	/**
	 * Adds a new line of text to the tooltip.
	 * 
	 * This will create a new label, add it as new row
	 * to the tooltip and then return it.
	 * 
	 * The created label will use the default style.
	 * 
	 * @param newText
	 * @return
	 */
	public Label addLine(CharSequence newText) {
		return addLine(newText, labelStyle);
	}
	
	
	/**
	 * Adds a new line of text to the tooltip.
	 * 
	 * This will create a new label, add it as new row
	 * to the tooltip and then return it.
	 * 
	 * The created label will use the supplied style.
	 * 
	 * If the supplied text is empty or null, this will return null;
	 * 
	 * @param newText
	 * @return
	 */
	public Label addLine(CharSequence newText, LabelStyle style) {
		if (newText == null || newText.length() < 1) {
			return null;
		}
		Label label = newLabel(newText, style);
		Cell<?> cell = add(label).prefWidth(this.style.width).fill().align(Align.left).padLeft(this.style.padLeft).padRight(this.style.padRight).padTop(getRows() == 0 ? this.style.padTop : 0).padBottom(this.style.padBottom);
		row();
		if (getRows() > 1) {
			getCells().get(getRows()-2).padBottom(0);
		}
		pack();
		cell.width(label.getGlyphLayout().width);
		invalidateHierarchy();
		this.pack();
		return label;
	}
	
	private Label newLabel(CharSequence text, LabelStyle style) {
		Label label = new Label(text, style);
		label.setWrap(true);
		label.setAlignment(Align.left, Align.left);
		return label;
	}
}
