package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.UsableGameObject;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if the supplied Usable is in the supplied state.
 * <br/><br/>
 * Example:
 * <pre>
 * &lt;usableInState usable="usableId" state="stateId" /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class UsableInState extends Condition {

	public static final String XML_STATE = "state";
	public static final String XML_USABLE = "usable";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		UsableGameObject usable = null;
		
		if (object instanceof UsableGameObject) {
			usable = (UsableGameObject) object;
		}
		String usableId = getParameter(XML_USABLE);
		if (usableId != null) {
			usable = (UsableGameObject) GameState.getGameObjectById(usableId);
		}
		return getParameter(XML_STATE).equalsIgnoreCase(usable.getState());
	}

	@Override
	protected String getStringTableNameKey() {
		return "usableInState";
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_STATE)};
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_USABLE, null) == null) {
			throw new GdxRuntimeException(XML_USABLE+" must be set for condition UsableInState in element: \n\n"+conditionElement);
		}
		if (conditionElement.get(XML_STATE, null) == null) {
			throw new GdxRuntimeException(XML_STATE+" must be set for condition UsableInState in element: \n\n"+conditionElement);
		}
	}

}
