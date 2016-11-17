package mg.fishchicken.gamelogic.characters;

import java.io.IOException;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import groovy.lang.Binding;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;

/**
 * A combination of a AIScript and its variables.<br />
 * Looks like this in xml:<br />
 * <br />
 * 
 * <pre>
 * &lt;ai&gt;
 * 		&lt;script&gt;Patrol&lt;/script&gt;
 * 		&lt;variables&gt;
 * 			&lt;coordinates type="IntArray"&gt;
 * 				31, 26, 
 * 				32, 31,
 * 				37, 33,
 * 				33, 32 
 * 			&lt;/coordinates&gt;
 * 		&lt;/variables&gt;
 * 	&lt;/ai&gt;
 * </pre>
 * 
 * @author micha
 *
 */
public class AIScriptPackage implements XMLSaveable {
	public static final String XML_AI = "ai";
	private static final String XML_SCRIPT_ID = "scriptId";
	private AIScript aiScript;
	private ObjectMap<String, Object> parameters;

	/**
	 * Creates a new AIScript package with the supplied script and no variables
	 * @param script
	 * @throws IOException
	 */
	public AIScriptPackage(AIScript script) throws IOException {
		this(script, new ObjectMap<String, Object>());
	}

	/**
	 * Creates a new AIScript package with the supplied script and parameters
	 * @param script
	 * @throws IOException
	 */
	public AIScriptPackage(AIScript script, ObjectMap<String, Object> parameters) throws IOException {
		this.aiScript = script;
		this.parameters = parameters;
	}
	
	/**
	 * Creates a new AIScript package by parsing it from the supplied
	 * xml root element.
	 * @param script
	 * @throws IOException
	 */
	public AIScriptPackage(Element aiElement) throws IOException {
		this.loadFromXML(aiElement);
	}
	
	public Action run(GameObject go) {
		if (aiScript != null) {
			Binding binding = new Binding();
			binding.setVariable("character", go);
			for (Entry<String, Object> parameter : parameters.entries()) {
				binding.setVariable(parameter.key, parameter.value);
			}
			return (Action) aiScript.run(binding);
		}
		return null;
	}

	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_AI);
		if (aiScript != null) {
			writer.element(XML_SCRIPT_ID, aiScript.getId());
		}
		if (parameters != null && parameters.size > 0) {
			writer.element(XMLUtil.XML_PARAMETERS);
			for (Entry<String, Object> variable : parameters.entries()) {
				writer.element(variable.key);
				String type = XMLUtil.XML_STRING;
				String value = null;
				if (variable.value instanceof Boolean) {
					type = XMLUtil.XML_BOOLEAN;
					value = Boolean.toString((Boolean)variable.value);
				} else if (variable.value instanceof Integer) {
					type = XMLUtil.XML_INTEGER;
					value = Integer.toString((Integer)variable.value);
				} else if (variable.value instanceof Float) {
					type = XMLUtil.XML_FLOAT;
					value = Float.toString((Float)variable.value);
				} if (variable.value instanceof String) {
					type = XMLUtil.XML_STRING;
					value =(String)variable.value;
				} else if (variable.value instanceof IntArray) {
					type = XMLUtil.XML_INTARRAY;
					value = ((IntArray)variable.value).toString(",");
				} else if (variable.value instanceof FloatArray) {
					type = XMLUtil.XML_FLOATARRAY;
					value = ((FloatArray)variable.value).toString(",");
				} else if (variable.value instanceof Array) {
					type = XMLUtil.XML_STRINGARRAY;
					value = ((Array<?>)variable.value).toString(",");
				}
				writer.attribute(XMLUtil.XML_TYPE, type);
				writer.text(value);
				writer.pop();
			}
			writer.pop();
		}
		writer.pop();
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		Element myElement = root.getChildByName(XML_AI);
		if (myElement == null) {
			return;
		}
		Element scriptElement = myElement.getChildByName(XML_SCRIPT_ID);
		aiScript = scriptElement != null ? AIScript.getAIScript(scriptElement.getText()) : null;
		parameters = new ObjectMap<String, Object>();
		Element parametersElement = myElement.getChildByName(XMLUtil.XML_PARAMETERS);
		if (parametersElement != null) {
			for (int i = 0; i < parametersElement.getChildCount(); ++i) {
				Element variableElement = parametersElement.getChild(i);
				String varName = variableElement.getName();
				if ("character".equals(varName)) {
					throw new GdxRuntimeException(
							"Cannot use \"character\" as a variable name for AIScript, this is a reserved variable name.");
				}
				String varType = variableElement.get(XMLUtil.XML_TYPE);
				String varText = variableElement.getText();
				Object varValue = null;
				if (XMLUtil.XML_BOOLEAN.equalsIgnoreCase(varType)) {
					varValue = Boolean.parseBoolean(varText);
				} else if (XMLUtil.XML_FLOAT.equalsIgnoreCase(varType)) {
					varValue = Float.parseFloat(varText);
				} else if (XMLUtil.XML_INTEGER.equalsIgnoreCase(varType)) {
					varValue = Integer.parseInt(varText);
				} else if (XMLUtil.XML_INTARRAY.equalsIgnoreCase(varType)) {
					IntArray intArray = new IntArray();
					String[] integers = varText.replaceAll("\\s", "").split(",");
					for (String num : integers) {
						intArray.add(Integer.parseInt(num));
					}
					varValue = intArray;
				} else if (XMLUtil.XML_FLOATARRAY.equalsIgnoreCase(varType)) {
					FloatArray floatArray = new FloatArray();
					String[] floats = varText.replaceAll("\\s", "").split(",");
					for (String num : floats) {
						floatArray.add(Float.parseFloat(num));
					}
					varValue = floatArray;
				} else if (XMLUtil.XML_STRINGARRAY.equalsIgnoreCase(varType)) {
					Array<String> stringArray = new Array<String>();
					String[] strings = varText.replaceAll("\\s", "").split(",");
					for (String str : strings) {
						stringArray.add(str);
					}
					varValue = stringArray;
				} else {
					varValue = varText;
				}
				parameters.put(varName, varValue);
			}
		}
		
	}
}
