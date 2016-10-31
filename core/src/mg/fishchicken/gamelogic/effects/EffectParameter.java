package mg.fishchicken.gamelogic.effects;

import java.io.IOException;

import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.effects.Effect.EffectParameterDefinition;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class EffectParameter implements XMLSaveable {
	private String name;
	private String s_strValue;
	private Boolean s_boolValue;
	private Float s_flValue;
	private Integer s_intValue;
	
	/**
	 * Creates a new EffectParameter using the supplied definition from the supplied
	 * Effects element. The effects element should contain this parameter as an attribute.
	 * 
	 * @param definition
	 * @param effectElement
	 * @throws MissingParameterException
	 */
	public EffectParameter(EffectParameterDefinition definition, Element effectElement)  throws MissingParameterException{
		this.name = definition.getName();
		Object parameterValue = effectElement.get(definition.getName(),null);
		if (definition.isMandatory() && parameterValue == null) {
			throw new MissingParameterException(this);
		}
		if (parameterValue != null) {
			if (XMLUtil.XML_INTEGER.equalsIgnoreCase(definition.getType())) {
				s_intValue = Integer.valueOf((String)parameterValue);
			} else if (XMLUtil.XML_FLOAT.equalsIgnoreCase(definition.getType())) {
				s_flValue = Float.valueOf((String)parameterValue);
			} else if (XMLUtil.XML_BOOLEAN.equalsIgnoreCase(definition.getType())) {
				s_boolValue = Boolean.valueOf((String)parameterValue);
			} else if (XMLUtil.XML_STRING.equalsIgnoreCase(definition.getType())) {
				s_strValue =  (String)parameterValue;
			}
		}
	}
	
	/**
	 * Creates a new parameters from the supplied parameter element.
	 * 
	 * The element should have been created by calling {@link EffectParameter#writeToXML(XmlWriter)}.
	 * @param parameterElement
	 * @throws IOException
	 */
	public EffectParameter(Element parameterElement) throws IOException {
		loadFromXML(parameterElement);
	}
	
	public EffectParameter(EffectParameter paramToCopy) {
		this.name = paramToCopy.name;
		this.s_boolValue = paramToCopy.s_boolValue;
		this.s_flValue = paramToCopy.s_flValue;
		this.s_intValue = paramToCopy.s_intValue;
		this.s_strValue = paramToCopy.s_strValue;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isNull() {
		return s_strValue == null && s_boolValue == null && s_flValue == null && s_intValue == null;
	}
	
	public Object getValue() {
		if (s_intValue != null) {
			return s_intValue;
		} else if (s_flValue != null) {
			return s_flValue;
		} else if (s_boolValue != null) {
			return s_boolValue;
		}
		return s_strValue;
	}

	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(name);
		XMLUtil.writePrimitives(this, writer);
		writer.pop();
		
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		this.name = root.getName();
		XMLUtil.readPrimitiveMembers(this, root);
	}

}
