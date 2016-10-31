package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.Role;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
/**
 * Returns true of the role of the character is the same 
 * as the one supplied.
 * 
 * <br /><br />
 * Example:
 * <pre>
 *	&lt;roleEqualTo role="elf"  targetObject="__pcAtDialogue" /&gt;
 * </pre>  
 * @author ANNUN
 *
 */
public class RoleEqualTo extends Condition {

	public static final String XML_ROLE = "role";
	
	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		if (object instanceof GameCharacter) {
			Role role = ((GameCharacter)object).getRole();
			return role != null && getParameter(XML_ROLE).equalsIgnoreCase(role.getId());
		}
		return false;
	}

	@Override
	protected String getStringTableNameKey() {
		return "RoleEqualTo";
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_ROLE) == null) {
			throw new GdxRuntimeException(XML_ROLE+" must be set for condition RoleEqualTo in element: \n\n"+conditionElement);
		}
	}
	
	@Override
	protected Object[] getStringNameParams() {
		return new Object[]{getParameter(XML_ROLE)};
	}

}
