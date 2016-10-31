package mg.fishchicken.core.statemachine;

import groovy.lang.Binding;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public abstract class StateMachine<S extends State<T>, T extends Transition> implements XMLSaveable, ThingWithId {
	
	public static final String XML_START_STATE = "startState";
	public static final String XML_END_STATE = "endState";
	public static final String XML_STATE = "state";
	public static final String XML_CURRENT_STATE = "currentState";
	
	private String s_id, s_name;
	private boolean s_fireActionOnStart;
	private ObjectMap<String, S> states;
	protected S currentState; 
	private String s_startState, s_endState;
	private Array<String> endStates;
	
	public StateMachine(String id) {
		s_id = id; 
		s_name = "";
		s_fireActionOnStart = true;
		states = new ObjectMap<String, S>();
	}
	
	public StateMachine(FileHandle file) throws IOException {
		this(file.nameWithoutExtension().toLowerCase(Locale.ENGLISH));
		loadFromXML(file);
	}
	
	public String getId() {
		return s_id;
	}
	
	public String getName() {
		return Strings.getString(s_name);
	}

	public boolean isStarted() {
		return currentState != null;
	}
	
	public boolean isFinished() {
		return currentState != null && endStates.contains(currentState.getId().toLowerCase(Locale.ENGLISH), false);
	}
	
	public void start() {
		goToState(getStartState(), s_fireActionOnStart);
	}
	
	protected S getStartState() {
		return states.get(s_startState);
	}
	
	/**
	 * Returns the name of the state this machine is currently in.
	 * 
	 * Returns an empty string if the machine is not started.
	 * 
	 * @return
	 */
	public String getState() {
		if (currentState != null) {
			return currentState.getId();
		}
		return "";
	}
	
	protected T getTransitionForEvent(String eventId) {
		if (currentState != null) {
			return currentState.getTransitionForEvent(eventId);
		}
		return null;
	}
	
	public boolean processEvent(String eventId) {
		Log.log("Processing event {0} for {1} which is currently in state {2}.", Log.LogType.STATE_MACHINE, eventId, s_id, getState());
		T transition = getTransitionForEvent(eventId);
		
		if (transition == null) {
			Log.log("No valid transition found for event {0}, ignoring.", Log.LogType.STATE_MACHINE, eventId, s_id);
			return false;
		}
		
		if (!isFinished()) {	
			return goToState(transition);
		}
		Log.log("Either not started or already finished, ignoring.", Log.LogType.STATE_MACHINE);
		return false;
	}
	
	protected boolean goToState(T transition) {
		S nextState = getStateForId(transition.getToState());
		if (nextState == null) {
			Log.log("Could not find state {0} for transition event {1} in {2}", LogType.ERROR, transition.getToState(), transition.getEventId(), s_id);
			return false;
		}
		Log.log("{0} entering state {1}.", Log.LogType.STATE_MACHINE, s_id, nextState.getId());
		goToState(nextState, true);
		return true;
	}
	
	private void goToState(S state, boolean fireAction) {
		currentState = state;
		if (fireAction) {
			state.executeAction(buildActionBinding());
		}
	}
	
	protected Binding buildActionBinding() {
		return new Binding();
	}
	
	protected S getStateForId(String stateId) {
		String lowerCaseId = stateId.toLowerCase(Locale.ENGLISH);
		if (states.containsKey(lowerCaseId)) {
			return states.get(lowerCaseId);
		}
		
		return null;
	}

	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		/*
		 * We only save the current state of the quest.
		 * 
		 * Since state machines are master data, they are loaded during
		 * game startup. When a game is loaded, they are then 
		 * set to their current states.
		 */
		if (currentState != null) {
			writer.attribute(XML_CURRENT_STATE, currentState.getId());
		}
	}

	@Override
	public String toString() {
		return s_id;
	}
	
	public void loadFromXML(FileHandle machineFile) throws IOException {
		loadFromXML(new XmlReader().parse(machineFile));
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
		readStates(root);
		s_startState = s_startState.toLowerCase(Locale.ENGLISH);
		endStates = new Array<String>();
		
		if (s_endState != null) {
			String[] endStatesSplit = s_endState.toLowerCase(Locale.ENGLISH).trim().split(",");
			for (String state : endStatesSplit) {
				endStates.add(state.trim());
			}
		}
		
		if (s_startState == null) {
			throw new GdxRuntimeException("Start state must be defined in state machine "+getId());
		}
		if (endStates.contains(s_startState, false)) {
			throw new GdxRuntimeException("Start state and end state must be different in state machine "+getId());
		}
	}
	
	private void readStates(Element root) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			Element stateElement = root.getChild(i);
			if (!XML_STATE.equalsIgnoreCase(stateElement.getName())) {
				continue;
			}
			S newState = createState(stateElement);
			newState.loadFromXML(stateElement);
			states.put(newState.getId(), newState);
		}
		
		String currentStateId = root.get(XML_CURRENT_STATE, null);
		if (currentStateId != null) {
			currentState = getStateForId(currentStateId);
		}
	}
	
	protected abstract S createState(Element element);
}
