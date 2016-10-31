package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Switches to the supplied map, placing the character group at the specified
 * coordinated. <br />
 * <br />
 * Example:
 * 
 * <pre>
 * &lt;switchToMap id="mapId" x="xCoordinate" y="yCoordinate" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class SwitchToMap extends Action {

	@Override
	protected void run(Object object, Binding parameters) {
		gameState.switchToMap(
			getParameter(XMLUtil.XML_ATTRIBUTE_ID), 
			new Tile(Integer.valueOf(getParameter(XMLUtil.XML_ATTRIBUTE_X)),
					Integer.valueOf(getParameter(XMLUtil.XML_ATTRIBUTE_Y)))
		);
		gameState.unpauseGame();
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XMLUtil.XML_ATTRIBUTE_ID, null) == null) {
			throw new GdxRuntimeException(XMLUtil.XML_ATTRIBUTE_ID
					+ " must be set for action SwitchToMap in element: \n\n" + conditionElement);
		}
		if (conditionElement.get(XMLUtil.XML_ATTRIBUTE_X, null) == null) {
			throw new GdxRuntimeException(XMLUtil.XML_ATTRIBUTE_X
					+ " must be set for action SwitchToMap in element: \n\n" + conditionElement);
		}
		if (conditionElement.get(XMLUtil.XML_ATTRIBUTE_Y, null) == null) {
			throw new GdxRuntimeException(XMLUtil.XML_ATTRIBUTE_Y
					+ " must be set for action SwitchToMap in element: \n\n" + conditionElement);
		}
	}

}
