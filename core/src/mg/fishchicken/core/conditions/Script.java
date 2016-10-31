package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.GroovyUtil;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * 
 * This condition contains as a body a Groovy script that will be executed on
 * runtime to determine the outcome.
 * 
 * @author ANNUN
 * 
 */
public class Script extends Condition {

	private static int idGenerator = 0;
	
	public static final String GROOVY_OBJECT = "object";
	public static final String GROOVY_PARAMETERS = "parameters";
	private String description;
	private groovy.lang.Script script;
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		Binding context = new Binding();
		context.setVariable(GROOVY_OBJECT, object);
		context.setVariable(GROOVY_PARAMETERS, conditionParameters);
		script.setBinding(context);
		return GroovyUtil.evaluateCondition(script);
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		script = XMLUtil.readScript("scriptCondition_"+(idGenerator++), conditionElement);
		description = conditionElement.getAttribute(XMLUtil.XML_DESCRIPTION, null);
	}

	@Override
	public String toUIString() {
		if (description != null) {
			return Strings.getString(description);
		}
		return "";
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "";
	}

}
