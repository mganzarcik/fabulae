package mg.fishchicken.core.statemachine;

import java.util.Locale;

import groovy.lang.Binding;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.XmlReader.Element;

public class Transition {

	private String s_event, s_toState;
	private Condition condition;
	private State<?> parentState;
	
	public Transition(State<?> parentState) {
		this.parentState = parentState;
	}
		
	public String getEventId() {
		return s_event;
	}
	
	public boolean isValidForEvent(String eventId) {
		if (!s_event.equalsIgnoreCase(eventId)) {
			return false;
		}
		if (condition == null) {
			return true;
		}
		
		if (condition.execute(parentState.getParentStateMachine(), buildConditionBinding())) {
			return true;
		}
		return false;
	}
	
	protected Binding buildConditionBinding() {
		return new Binding();
	}
	
	public String getToState() {
		return s_toState;
	}
	
	public void loadFromXML(Element transitionElement) {
		XMLUtil.readPrimitiveMembers(this, transitionElement);
		s_event = s_event.toLowerCase(Locale.ENGLISH);
		s_toState = s_toState.toLowerCase(Locale.ENGLISH);
		Element conditionElement = transitionElement.getChildByName(XMLUtil.XML_CONDITION);
		if (conditionElement != null&& conditionElement.getChildCount() == 1) {
			condition = Condition.getCondition(conditionElement.getChild(0));
		}
	}
	
}
