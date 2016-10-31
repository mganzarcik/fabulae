package mg.fishchicken.gamelogic.story;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.XmlReader.Element;

public class StoryPage {

	private Condition condition;
	@XMLField(fieldPath="title")
	private String title;
	@XMLField(fieldPath="text")
	private String text;
	@XMLField(fieldPath="image")
	private String image = null;
	
	public StoryPage(Element screenElement) {
		XMLUtil.readPrimitiveMembers(this, screenElement);
		if (image != null) {
			image = Configuration.addModulePath(image);
		}
		Element conditionElement = screenElement.getChildByName(XMLUtil.XML_CONDITION);
		if (conditionElement  != null && conditionElement.getChildCount() == 1) {
			condition = Condition.getCondition(conditionElement.getChild(0));
		}
	}
	
	public boolean isApplicable() {
		return condition == null ? true : condition.execute(GameState.getPlayerCharacterGroup(), new Binding());
	}
	
	public String getTitle() {
		return Strings.getString(title);
	}
	
	public String getText() {
		return Strings.getString(text);
	}
	
	public String getImage() {
		return image;
	}
	
}
