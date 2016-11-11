package mg.fishchicken.core.conditions;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

import groovy.lang.Binding;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.util.XMLUtil;

/**
 * Returns true if the supplied GameObject is currently active.
 * <br /><br />
 * Examples:
 * 
 * <pre>
 * &lt;gameObjectActive id="gameObjectId" /&gt;
 * </pre>
 *   
 * @author annun
 *
 */
public class GameObjectActive extends Condition {
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		GameObject go = GameState.getGameObjectById(getParameter(XMLUtil.XML_ATTRIBUTE_ID));
		return go != null && go.isActive();
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XMLUtil.XML_ATTRIBUTE_ID, null) == null) {
			throw new GdxRuntimeException(XMLUtil.XML_ATTRIBUTE_ID+" must be set for condition GameObjectActive in element: \n\n"+conditionElement);
		}
	}

	@Override
	protected String getStringTableNameKey() {
		return "gameObjectActive";
	}
}
