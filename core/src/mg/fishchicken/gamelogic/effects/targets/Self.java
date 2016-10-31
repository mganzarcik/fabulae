package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamelogic.characters.GameCharacter;

/**
 * Targets the caster herself.
 * 
 * @author ANNUN
 *
 */
public class Self extends TargetType {
	
	@Override
	public boolean requiresTargeting() {
		return false;
	}
	
	/**
	 * Self target is always valid.
	 */
	@Override
	public boolean isValidTarget() {
		return true;
	}

	
	@Override
	public void setUser(GameCharacter user) {
		super.setUser(user);
		addTarget(user);
	}

	@Override
	public void addTarget(GameObject go) {
		// we only ever add the user
		targets.clear();
		targets.add(getUser());
	}
	
	@Override
	public void setScriptResult(Object result) {	
	}

	@Override
	public PositionArray getAffectedTiles(int x, int y) {
		PositionArray returnValue = new PositionArray();
		returnValue.add(getUser().position().tile());
		return returnValue;
	}


	@Override
	public float getTargetX() {
		return getUser().position().getX();
	}


	@Override
	public float getTargetY() {
		return getUser().position().getY();
	}

	@Override
	public float getSize() {
		return 1;
	}

	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "self");
	}
}
