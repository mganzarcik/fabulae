package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamestate.Tile;

/**
 * Single target at any range.
 * 
 * @author ANNUN
 *
 */
public class Single extends TargetType {
	
	private int targetX, targetY;
	
	public Single() {
		super();
	}
	
	public Single(GameObject go) {
		super();
		addTarget(go);
		Tile tile = go.position().tile();
		targetX = tile.getX();
		targetY = tile.getY();
	}
	
	@Override
	public void addTarget(GameObject go) {
		targets.clear();
		targets.add(go);
	}
	
	/**
	 * Only valid if it contains at least one game object.
	 */
	@Override
	public boolean isValidTarget() {
		return targets.size > 0;
	}
	
	@Override
	public boolean requiresTargeting() {
		return true;
	}


	@Override
	public void setScriptResult(Object result) {	
	}

	@Override
	public PositionArray getAffectedTiles(int x, int y) {
		targetX = x;
		targetY = y;
		PositionArray returnValue = new PositionArray(1);
		returnValue.add(x, y);
		return returnValue;
	}

	@Override
	public float getTargetX() {
		if (targets.size == 1) {
			return targets.get(0).position().getX();
		}
		return targetX;
	}

	@Override
	public float getTargetY() {
		if (targets.size == 1) {
			return targets.get(0).position().getY();
		}
		return targetY;
	}

	@Override
	public float getSize() {
		return 1;
	}

	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "single");
	}
}
