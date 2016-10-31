package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.PositionArray;

/**
 * Circular area target defined by the center 
 * coordinates and radius.
 * 
 * @author ANNUN
 *
 */
public class Area extends TargetType {
	private int radius;
	private int targetX, targetY;
	
	/**
	 * Area effects can be targeted everywhere.
	 */
	@Override
	public boolean isValidTarget() {
		return true;
	}
	
	@Override
	public boolean requiresTargeting() {
		return true;
	}

	@Override
	public void setScriptResult(Object result) {	
		radius = (Integer) result;
	}

	@Override
	public PositionArray getAffectedTiles(int x, int y) {
		targetX = x;
		targetY = y;
		return MathUtil.getCircle(x, y, radius, true);
	}

	@Override
	public float getTargetX() {
		return targetX;
	}

	@Override
	public float getTargetY() {
		return targetY;
	}

	@Override
	public float getSize() {
		return radius;
	}
	
	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "area", radius);
	}

}
