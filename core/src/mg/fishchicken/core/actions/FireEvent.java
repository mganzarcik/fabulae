package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.UsableGameObject;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Fires the supplied event on the UsableGameObject the action is fired from. If the
 * optional parameter "usable" is supplied, the event will be instead fired on the 
 * UsableGameObject with the given ID. Note that in this case, the object must
 * be already "known", i.e. it must have been previously loaded at once by the game 
 * (see {@link GameState#getGameObjectById(String)} for more info).
 * <br /><br />
* Example:
 * 
 * <pre>
 * 	&lt;fireEvent event="eventId" usable="usableId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class FireEvent extends Action {

	public static final String XML_EVENT = "event";
	public static final String XML_USABLE = "usable";

	
	@Override
	protected void run(Object object, Binding parameters) {
		UsableGameObject usable = null;
		if (object instanceof UsableGameObject) {
			usable = (UsableGameObject) object;
		}
		
		String usableId = getParameter(XML_USABLE);
		if (usableId != null) {
			usable = (UsableGameObject) GameState.getGameObjectById(usableId);
		}
		
		usable.processEvent(getParameter(XML_EVENT), null);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_EVENT, null) == null) {
			throw new GdxRuntimeException(XML_EVENT+" must be set for action FireEvent in element: \n\n"+conditionElement);
		}
	}

}
