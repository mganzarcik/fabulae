package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;


public class Or extends And {
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		for (Condition condition : conditions) {
			if (condition.execute(object, parameters)) {
				return true;
			}
		}
		return false;
	}
	
	public Array<ConditionResult> evaluateWithDetails(Object object, Binding parameters) {
		Array<ConditionResult> result = new Array<ConditionResult>();
		result.add(new ConditionResult(toUIString(), evaluate(object, parameters)));
		return result;
	}
	
	@Override
	public String toUIString() {
		StringBuilder fsb = StringUtil.getFSB();
		boolean inBrackets = false;
		for (int i = 0; i < conditions.size; ++i) {
			Condition condition = conditions.get(i);
			boolean multipleConditions = condition instanceof And || condition instanceof Or;
			String conditionText = conditions.get(i).toUIString();
			if (i != 0) {
				conditionText = conditionText.toLowerCase();
				if (inBrackets) {
					fsb.append(")");
					inBrackets = false;
				} 
				fsb.append(" ");
				fsb.append(Strings.getString(STRING_TABLE, "or"));
				fsb.append(" ");
				if (multipleConditions) {
					fsb.append("(");
					inBrackets = true;
				}
				
			} else if (multipleConditions) {
				fsb.append("(");
				inBrackets = true;
			}
			fsb.append(conditionText);
		}
		if (inBrackets) {
			fsb.append(")");
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue;
	}
	
	@Override
	public String toNegatedUIString() {
		StringBuilder fsb = StringUtil.getFSB();
		for (int i = 0; i < conditions.size; ++i) {
			if (i != 0) {
				fsb.append("\n");
			}
			fsb.append(conditions.get(i).toNegatedUIString());
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		return returnValue; 
	}
}
