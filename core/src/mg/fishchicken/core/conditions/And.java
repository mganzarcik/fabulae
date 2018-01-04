package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.StringBuilder;

public class And extends Condition {

	protected Array<Condition> conditions = new Array<Condition>();
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		for (Condition condition : conditions) {
			if (!condition.execute(object, parameters)) {
				return false;
			}
		}
		return true;
	}
	

	@Override
	public Array<ConditionResult> evaluateWithDetails(Object object, Binding parameters) {
		Array<ConditionResult>  returnValue = new Array<Condition.ConditionResult>();
		for (Condition condition : conditions) {
			returnValue.addAll(condition.evaluateWithDetails(object, parameters));
		}
		return returnValue;
	}
	
	@Override
	public String toUIString() {
		StringBuilder fsb = StringUtil.getFSB();
		for (int i = 0; i < conditions.size; ++i) {
			if (i != 0) {
				fsb.append("\n");
			}
			fsb.append(conditions.get(i).toUIString());
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue; 
	}
	
	@Override
	public String toNegatedUIString() {
		StringBuilder fsb = StringUtil.getFSB();
		for (int i = 0; i < conditions.size; ++i) {
			fsb.append(conditions.get(i).toNegatedUIString());
			if (i != conditions.size-1) {
				fsb.append(" ");
				fsb.append(Strings.getString(STRING_TABLE, "or"));
				fsb.append(" ");
			}
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue; 
	}
	
	@Override
	protected String getStringTableNameKey() {
		return "";
	}
	
	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		for (int i = 0; i < conditionElement.getChildCount(); ++i) {
			Element childConditionElement = conditionElement.getChild(i);
			conditions.add(Condition.getCondition(childConditionElement));	
		}
	}

}
