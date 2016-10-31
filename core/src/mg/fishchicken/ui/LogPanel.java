package mg.fishchicken.ui;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ForcedScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class LogPanel extends Table {

	private LogPanelStyle style;
	private Table messages;
	private ForcedScrollPane srollPane;
	
	public LogPanel(LogPanelStyle style) {
		this.style = style;
		buildComponent();
	}
	
	private void buildComponent() {
		messages = new Table();
		srollPane = new ForcedScrollPane(messages, style.scrollPaneStyle);
		int width = (int) (Gdx.graphics.getWidth() * (style.widthPercent / 100f));
		add(srollPane).prefSize(width, style.height).top();
		setWidth(width);
		setHeight(style.height);
	}
	
	/**
	 * Adds a new line of text to the log panel.
	 * 
	 * This will create a new label and add it as new row
	 * to the log panel.
	 * 
	 * The created label will use the supplied color.
	 * 
	 * @param newText
	 * @return
	 */
	public void logMessage(String text, Color color, boolean logTime) {
		Label label = newLabel(text, "logLabel");
		if (!logTime) {
			messages.add(label).fillX().expandX().align(Align.left).padLeft(10).top();
		} else {
			Table table = new Table();
			table.add(new Label(GameState.getCurrentGameDate().toString(false), style.textStyle)).top();
			table.add(label).padLeft(10).fillX().expandX().top();
			messages.add(table).fillX().expandX().align(Align.left).padLeft(10).top();
		}
		messages.row();
		label.setColor(color);
		srollPane.layout();
		srollPane.setScrollY(srollPane.getMaxY());
		
		if (messages.getCells().size > Configuration.getMaxMessagesInLog()) {
			@SuppressWarnings("rawtypes")
			Cell cell = messages.getCells().removeIndex(0);
			if (cell.getActor() != null) {
				messages.removeActor((Actor) cell.getActor());
			}
			cell.clearActor();
			
		}
	}
	
	public void updateSizeAndPosition(int screenWidth, int screenHeight) {
		if (style.yAlign != null) {
			if ("center".equals(style.yAlign)) {
				setY(screenHeight / 2 - (style.height / 2));
			} else if ("top".equals(style.yAlign)) {
				setY(screenHeight - style.height);
			} else {
				setY(0);
			}  
		} else {
			setY(style.y);
		}
		
		int width = style.width > 0 ? style.width : (int) (Gdx.graphics.getWidth() * (style.widthPercent / 100f));
		setWidth(width);
		if (style.xAlign != null) {
			if ("center".equals(style.xAlign)) {
				setX(screenWidth / 2 - (width / 2));
			} else if ("right".equals(style.xAlign)) {
				setX(screenWidth - width);
			} else {
				setX(0);
			}  
		} else {
			setX(style.x);
		}
	}
	
	/**
	 * Erase all messages in the log panel.
	 */
	public void clearMessages() {
		messages.clear();
	}
	
	private Label newLabel(CharSequence text, String styleName) {
		Label label = new Label("", style.textStyle);
		label.setWrap(true);
		label.setText(text);
		label.setAlignment(Align.left, Align.left);
		return label;
	}
	
	public static class LogPanelStyle {
		private int width, widthPercent, height, x, y;
		private String xAlign, yAlign;
		private ScrollPaneStyle scrollPaneStyle;
		private LabelStyle textStyle;
	}
}
