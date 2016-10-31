package mg.fishchicken.ui.debug;

import java.io.IOException;
import java.io.StringWriter;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.scenes.scene2d.ui.ForcedScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlWriter;

public class GameObjectViewer extends BorderedWindow {

	private Label goName, goId, goInternalId;
	private Label goDetails;
	
	public GameObjectViewer(GameObjectViewerStyle style) {
		super(Strings.getString(UIManager.STRING_TABLE, "goViewer"), style);
		
		goName = new Label("", style.headingStyle);
		goId = new Label("", style.headingStyle);
		goInternalId = new Label("", style.headingStyle);
		goDetails = new Label("", style.detailsStyle);
		goDetails.setWrap(true);
		goDetails.setAlignment(Align.topLeft);
		ScrollPane scrollPane = new ForcedScrollPane(goDetails, style.scrollPaneStyle);
		
		Table headingTable = new Table();
		headingTable.add(goName).fill();
		headingTable.add(goId).fill();
		headingTable.add(goInternalId).fill().expand();
		add(headingTable).left().width(style.scrollPaneWidth);
		row();
		add(scrollPane).width(style.scrollPaneWidth).height(style.scrollPaneHeight).fill().colspan(3);
		pack();
	}
	
	public void setGo(GameObject go) {
		if (go == null) {
			goName.setText("");
			goId.setText("");
			goInternalId.setText("");
			goDetails.setText("");
			return;
		}
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		String result;
		try {
			go.writeToXML(writer);
			result = stringWriter.toString();
			result = result.replace("\t", "    ");
			stringWriter.close();
		} catch (IOException e) {
			result = "Error: "+e.getMessage();
		}
		goName.setText(go.getName());
		goId.setText(" ("+go.getId()+")");
		goInternalId.setText(" ["+go.getInternalId()+"]");
		goDetails.setText(result);
		pack();
	}
	

	public static class GameObjectViewerStyle extends BorderedWindowStyle {
		private LabelStyle detailsStyle, headingStyle;
		private ScrollPaneStyle scrollPaneStyle;
		private int scrollPaneWidth, scrollPaneHeight;
	}
}
