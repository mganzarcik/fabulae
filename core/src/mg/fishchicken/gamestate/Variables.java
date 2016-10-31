package mg.fishchicken.gamestate;

import java.io.IOException;
import java.util.Iterator;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Variables extends ObservableState<Variables, Variables.VariableChange> {

	public static final String XML_VARIABLE = "variable";
	
	private ObjectMap<String, Object> variables = new ObjectMap<String, Object>();
	private VariableChange changes = new VariableChange();
	
	public Variables setVariable(String variableName, GameObject value) {
		setVariableInternal(variableName, value);
		return this;
	}
	
	public Variables setVariable(String variableName, int value) {
		setVariableInternal(variableName, value);
		return this;
	}
	
	public Variables setVariable(String variableName, float value) {
		setVariableInternal(variableName, value);
		return this;
	}
	
	public Variables setVariable(String variableName, double value) {
		// currently no need for double precision
		// (this is going to come back to haunt me, isn't it)
		setVariableInternal(variableName, (float)value); 
		return this;
	}
	
	public Variables setVariable(String variableName, String value) {
		setVariableInternal(variableName, value);
		return this;
	}
	
	public Variables setVariable(String variableName, boolean value) {
		setVariableInternal(variableName, value);
		return this;
	}
	
	private void setVariableInternal(String variableName, Object value) {
		changes.name = variableName;
		changes.newValue = value;
		changes.oldValue = variables.get(variableName, null);
		variables.put(variableName, value);
		changed(changes);
	}
	
	public String getStringVariable(String variableName) {
		Object value = variables.get(variableName);
		return value == null ? null : value.toString();
	}
	
	public Float getFloatVariable(String variableName) {
		Object value = variables.get(variableName);
		if (value instanceof Float) {
			return (Float) value;
		}
		if (value instanceof String) {
			return Float.parseFloat((String)value);
		}
		return null;
	}
	
	public Integer getIntegerVariable(String variableName) {
		Object value = variables.get(variableName);
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof String) {
			return Integer.parseInt((String)value);
		}
		return null;
	}
	
	public boolean getBooleanVariable(String variableName) {
		Object value = variables.get(variableName);
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		if (value instanceof String) {
			return Boolean.parseBoolean((String)value);
		}
		return false;
	}
	
	public GameObject getGameObjectVariable(String variableName) {
		Object value = variables.get(variableName);
		if (value instanceof GameObject) {
			return (GameObject) value;
		}
		if (value instanceof String) {
			return GameState.getGameObjectById((String)value);
		}
		return null;
	}
	
	public Variables removeVariable(String variableName) {
		variables.remove(variableName);
		return this;
	}
	
	public Iterator<String> getVariableNames() {
		return variables.keys();
	}
	
	public Object getVariable(String variableName) {
		return variables.get(variableName);
	}
	
	public void clear() {
		variables.clear();
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		Element myElement = root.getChildByName(XMLUtil.XML_VARIABLES);
		if (myElement != null) {
			for (int i = 0; i< myElement.getChildCount(); ++i) {
				Element variable = myElement.getChild(i);
				setVariable(variable.getAttribute(XMLUtil.XML_ATTRIBUTE_NAME), variable.getAttribute(XMLUtil.XML_ATTRIBUTE_VALUE));
			}
		}
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		Iterator<String> variables = getVariableNames();
		if (!variables.hasNext()) {
			return ;
		}
		writer.element(XMLUtil.XML_VARIABLES);
		while (variables.hasNext()) {
			String variable = variables.next();
			Object value = getVariable(variable);
			writer.element(XML_VARIABLE).attribute(XMLUtil.XML_ATTRIBUTE_NAME, variable).attribute(XMLUtil.XML_ATTRIBUTE_VALUE, value.toString()).pop();
		}
		writer.pop();
	}
	
	public static class VariableChange {
		private String name;
		private Object oldValue, newValue;
		
		public String getName() {
			return name;
		}
		
		public Object getOldValue() {
			return oldValue;
		}
		
		public Object getNewValue() {
			return newValue;
		}
	}

}
