package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * 
 * This action contains as a body a Groovy script that will be executed on
 * runtime to do whatever needs doing. The script will have access to
 * a variable "targetObject", which will refer to the GameObject this 
 * action was fired on. 
 * 
 * @author ANNUN
 * 
 */
public class Script extends Action {

	private static int idGenerator = 0;
	
	public static final String GROOVY_PARAMETERS = "parameters";
	
	private groovy.lang.Script script;
	
	@Override
	protected void run(Object object, Binding parameters) {
		Binding context = new Binding();
		context.setVariable(Condition.PARAM_TARGET_OBJECT, object);
		context.setVariable(GROOVY_PARAMETERS, parameters);
		script.setBinding(context);
		script.run();		
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		script = XMLUtil.readScript("scriptAction_" + (idGenerator++), conditionElement);
	}

}
