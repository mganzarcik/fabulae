package mg.fishchicken.core.conditions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameState;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Returns true if combat is currently in progress.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * &lt;ssCombatInProgress /&gt;
 * </pre>
 * @author ANNUN
 *
 */
public class IsCombatInProgress extends Condition {

	@Override
	protected boolean evaluate(Object object, Binding parameters) {
		return GameState.isCombatInProgress();
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
	}

	@Override
	protected String getStringTableNameKey() {
		return "isCombatInProgress";
	}
	
}
