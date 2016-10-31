package mg.fishchicken.core.conditions;

import groovy.lang.Binding;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Basic Condition used in xml files to determine under what circumstances stuff happens.
 * 
 * Each condition defined in an xml file has to have a corresponding class in this package.
 * 
 * @author ANNUN
 *
 */
public abstract class Condition {

	public static final String STRING_TABLE = "conditions."+Strings.RESOURCE_FILE_EXTENSION;
	public static final String PARAM_TARGET_OBJECT = "targetObject";
	public static final String PARAM_PC_AT_DIALOGUE = "__pcAtDialogue";
	public static final String PARAM_NPC_AT_DIALOGUE = "__npcAtDialogue";
	public static final String PARAM_USER = "__user";
	public static final String PARAM_INITIAL_OBJECT = "__initialObject";
	public static final String PARAM_GLOBAL = "__global";
	public static final String PARAM_ENTERING_CHARACTER = "__enteringCharacter";
	public static final String PARAM_PLAYER = "__player";
	
	protected static GameState gameState;
	
	public static void setGameState(GameState gameState) {
		Condition.gameState = gameState;
	}
	
	protected ConditionParameter[] conditionParameters;

	/**
	 * Creates a new Condition instance from the supplied XML element.
	 * @param conditionElement
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Condition getCondition(Element conditionElement) {
		if (conditionElement == null) {
			return null;
		}
		String implementationClassName =  Condition.class.getPackage().getName()
				+ "." + StringUtil.capitalizeFirstLetter(conditionElement.getName());
		Class<? extends Condition> conditionClass;
		try {
			conditionClass = (Class<? extends Condition>) Class
					.forName(implementationClassName);
			if (Modifier.isAbstract(conditionClass.getModifiers())) {
				throw new ClassNotFoundException();
			}
		} catch (ClassNotFoundException e) {
			conditionClass = And.class;
		}
		try {
			Condition newCondition = conditionClass.newInstance();
			newCondition.validateAndLoadFromXML(conditionElement);
			readParameters(newCondition, conditionElement);
			return newCondition;
		} catch (Exception e) {
			throw new GdxRuntimeException("Cannot load condition from element: \n\n"+conditionElement,e);
		}
	}
	
	protected static void readParameters(Condition condition, Element conditionElement) {
		ObjectMap<String, String> attributes = conditionElement.getAttributes();
		if (attributes == null) {
			return;
		}
		condition.conditionParameters = new ConditionParameter[attributes.size];
		int i = 0;
		for (String paramName : attributes.keys()) {
			condition.conditionParameters[i] = new ConditionParameter(paramName, attributes.get(paramName));
			++i;
		}
	}
	
	/**
	 * Gets the value of the given parameter from the supplied array of parameters.
	 * 
	 * Returns null if such parameter does not exist.
	 * 
	 * @param parameterName
	 * @param parameters
	 * @return
	 */
	public static String getParameter(String parameterName, ConditionParameter... parameters) {
		if (parameters == null) {
			return null;
		}
		for (ConditionParameter param : parameters) {
			if (parameterName.equals(param.name)) {
				return param.value;
			}
		}
		return null;
	}

	/**
	 * Gets a parameter with the given name associated with this Condtition.
	 * 
	 * Returns null if it does not exist.
	 * 
	 * @param parameterName
	 * @return
	 */
	public String getParameter(String parameterName) {
		return getParameter(parameterName, conditionParameters);
	}
	
	/**
	 * Evaluates this condition on the supplied object and with the supplied parameters.
	 * 
	 * The supplied parameters can contain special keys (see the PARAM_
	 * constants in this class).
	 * 
	 * If such parameters are provided and this condition has a targetObject
	 * param specified, one of these will be used when evaluating instead of
	 * whatever was passed in as the object.
	 * 
	 * @param object
	 *            - on which object his should be executed. Depending on the
	 *            parameters passed, this might not actually be used
	 * @param parameters
	 * @return true if the condition evaluated as true, false otherwise
	 */
	public final boolean execute(Object object, Binding parameters) {
		if (getParameter(PARAM_TARGET_OBJECT) != null) {
			String testObject = getParameter(PARAM_TARGET_OBJECT);
			if (PARAM_PC_AT_DIALOGUE.equals(testObject)) {
				object = parameters.getVariable(testObject);
			} else if (PARAM_NPC_AT_DIALOGUE.equals(testObject)) {
				object = parameters.getVariable(testObject);
			} else if (PARAM_ENTERING_CHARACTER.equals(testObject)) {
				object = parameters.getVariable(testObject);
			} else if (PARAM_USER.equals(testObject)) {
				object = parameters.getVariable(testObject);
			} else if (PARAM_PLAYER.equals(testObject)) {
				object = GameState.getPlayerCharacterGroup();
			} else if (PARAM_GLOBAL.equals(testObject)) {
				object = gameState.variables();
			} else {
				object = GameState.getGameObjectById(testObject);
			}
		}
		boolean returnValue = evaluate(object, parameters);
		Log.log("Evaluated {0} with parameters {1} on object {2}, condition returned {3}", Log.LogType.CONDITION, this.getClass().getName(), Arrays.toString(conditionParameters), object, returnValue);
		return returnValue;
	}
	
	/**
	 * Evaluates this condition on the supplied object.
	 * 
	 * @param object
	 * @param parameters
	 * @return
	 */
	protected abstract boolean evaluate(Object object, Binding parameters);

	/**
	 * Evaluates this condition on the supplied object.
	 * 
	 * Will return an array containing the results of the evaluation.
	 * 
	 * If this condition contains multiple conditions inside, each evaluated 
	 * subcondition and its result will be included in the returned array.
	 * 
	 * @return
	 */
	public Array<ConditionResult> evaluateWithDetails(Object object, Binding parameters) {
		Array<ConditionResult> result = new Array<ConditionResult>();
		result.add(new ConditionResult(toUIString(), evaluate(object, parameters)));
		return result;
	}
	
	/**
	 * Returns a human-readable representation of this Condition.
	 * @return
	 */
	public String toUIString() {
		return toUIString("");
	}
	
	public String toNegatedUIString() {
		return toUIString("not");
	}
	
	protected String toUIString(String keyPrefix) {
		 return Strings.getString(getNameStringTable(), keyPrefix+getStringTableNameKey(), getStringNameParams());
	}
	
	protected String getNameStringTable() {
		return STRING_TABLE;
	}
	
	protected abstract String getStringTableNameKey();
	
	protected Object[] getStringNameParams() {
		return new Object[0];
	}
	
	/**
	 * Validates the supplied element to make sure it contains all required
	 * parameters and also loads anything extra that is required from the
	 * element into the condition.
	 * 
	 * @param conditionElement
	 * @throws GdxRuntimeException in case something is invalid in the element
	 */
	public abstract void validateAndLoadFromXML(Element conditionElement);
	
	public static class ConditionParameter {
		public String name;
		public String value;
		
		public ConditionParameter(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return name +" => "+ value;
		}
	}
	
	public static class ConditionResult {
		public final boolean passed;
		public final String conditionName;
		
		public ConditionResult(String conditionName, boolean result) {
			this.conditionName = conditionName;
			this.passed = result;
		}
	}

	/**
	 * Returns true if all the conditions results in the supplied array passed,
	 * or if the array is null or empty.
	 * 
	 * @param results
	 * @return
	 */
	public static boolean areResultsOk(Array<ConditionResult> results) {
		if (results == null || results.size < 1) {
			return true;
		}
		for (ConditionResult result : results) {
			if (!result.passed) {
				return false;
			}
		}
		
		return true;
	}
	
}
