package mg.fishchicken.core.statemachine;

import groovy.lang.Binding;

import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.actions.Action;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public abstract class State<T extends Transition> implements ThingWithId {	
	
	public final static String XML_TRANSITION = "transition";
	
	private StateMachine<? extends State<? extends Transition>, ? extends Transition> parentMachine;
	private String s_id, s_name, s_description;
	private Array<T> transitions;
	private Action action;
	
	public State(StateMachine<? extends State<? extends Transition>, ? extends Transition> parentMachine) {
		this.parentMachine = parentMachine;
		transitions = new Array<T>();
	}
	
	public String getId() {
		return s_id;
	}

	public String getName() {
		return StringUtil.replaceCharacterNames(Strings.getString(s_name));
	}

	public void setName(String name) {
		s_name = name;
	}

	public String getDescription() {
		return StringUtil.replaceCharacterNames(Strings.getString(s_description));
	}

	public void setDescription(String description) {
		s_description = description;
	}

	public T getTransitionForEvent(String eventId) {
		for (T transition : transitions) {
			if (transition.isValidForEvent(eventId)) {
				return transition;
			}
		}
		return null;
	}
	
	public StateMachine<? extends State<? extends Transition>, ? extends Transition> getParentStateMachine() {
		return parentMachine;
	}
	
	public void executeAction(Binding binding) {
		if (action != null) {
			action.execute(getObjectForAction(), binding);
		}
	}
	
	protected Object getObjectForAction() {
		return getParentStateMachine();
	}

	public void loadFromXML(Element stateElement) {
		XMLUtil.readPrimitiveMembers(this, stateElement);
		action = Action.getAction(stateElement.getChildByName(XMLUtil.XML_ACTION));
		loadTransitions(stateElement);
		if (s_id != null) {
			s_id = s_id.toLowerCase(Locale.ENGLISH);
		}
	}
	
	private void loadTransitions(Element stateElement) {
		Array<Element> transitionElements = stateElement.getChildrenByName(XML_TRANSITION);
		for (Element transitionElement : transitionElements) {
			transitions.add(createTransition(transitionElement));
		}
	}
	
	protected abstract T createTransition(Element transitionElement);
}
