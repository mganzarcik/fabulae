package mg.fishchicken.core.actions;

import groovy.lang.Binding;

import java.util.Locale;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Logs the supplied message into the supplied game log.
 * <br /><br />
 * <br /><br />
* Example:
 * 
 * <pre>
 * 	&lt;logMessage message="myStringTable#hellowWorld" logType="info" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class LogMessage extends Action {

	public static final String XML_MESSAGE = "message";
	public static final String XML_LOG_TYPE = "logType";

	@Override
	protected void run(Object object, Binding parameters) {
		String param = object.toString();
		if (object instanceof GameObject) {
			param = ((GameObject)object).getName();
		}
			
		Log.log(Strings.getString(getParameter(XML_MESSAGE), new Object[]{param}), LogType.valueOf(getParameter(XML_LOG_TYPE).toUpperCase(Locale.ENGLISH)));
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_MESSAGE, null) == null) {
			throw new GdxRuntimeException(XML_MESSAGE+" must be set for action LogMessage in element: \n\n"+conditionElement);
		}
		if (conditionElement.get(XML_LOG_TYPE, null) == null) {
			throw new GdxRuntimeException(XML_LOG_TYPE+" must be set for action LogMessage in element: \n\n"+conditionElement);
		}
	}

}
