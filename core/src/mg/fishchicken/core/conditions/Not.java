package mg.fishchicken.core.conditions;

import groovy.lang.Binding;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Not extends Condition {

	private Condition negatedCondition;
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		return !negatedCondition.execute(object, parameters);
	}


	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.getChildCount() != 1) {
			throw new GdxRuntimeException("Not condition can only contain one subcondition! Error found in element: \n\n "+conditionElement);
		}
		negatedCondition = Condition.getCondition(conditionElement.getChild(0));
	}


	@Override
	public String toUIString() {
		return negatedCondition.toNegatedUIString();
	}
	
	@Override
	public String toNegatedUIString() {
		return negatedCondition.toUIString();
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "";
	}
}
