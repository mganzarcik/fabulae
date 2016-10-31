package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamelogic.characters.GameCharacter;

/**
 * Effect target that contains all
 * tiles adjacent to the user.
 * 
 * @author ANNUN
 *
 */
public class AllAdjacent extends TargetType {
	
	@Override
	public float getSize() {
		return 2;
	}
	
	@Override
	public void setUser(GameCharacter user) {
		super.setUser(user);
		setTarget((int)user.position().getX(), (int)user.position().getY(), user.getMap());
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
	public void setScriptResult(Object result) {
	}

	@Override
	public boolean requiresTargeting() {
		return false;
	}

	@Override
	public boolean isValidTarget() {
		return true;
	}

	@Override
	protected PositionArray getAffectedTiles(int x, int y) {
		PositionArray returnValue = new PositionArray(8);
		
		int currX = x-1;
		int currY = y+1;

		for (int i = -1; i <= 1; ++i) {
			currX = x+i;
			returnValue.add(currX, currY);
		}
		for (int i = 0; i >= -1; --i) {
			currY = y+i;
			returnValue.add(currX, currY);
		}
		for (int i = 0; i >= -1; --i) {
			currX = x+i;
			returnValue.add(currX, currY);
		}
		for (int i = 0; i <= 1; ++i) {
			currY = y+i;
			returnValue.add(currX, currY);
		}
		return returnValue;
	}
	
	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "allAdjacent");
	}

}
