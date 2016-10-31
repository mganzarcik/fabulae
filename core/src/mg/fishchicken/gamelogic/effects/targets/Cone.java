package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamestate.GameObjectPosition;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.math.MathUtils;

public class Cone extends TargetType {

	private int length;
	private int degrees;
	private int targetX, targetY;
	
	public Cone() {
		super();
		degrees = 90;
		targetX = -1;
		targetY = -1;
	}
	
	/**
	 * Cones can be targeted anywhere.
	 */
	@Override
	public boolean isValidTarget() {
		return targetX != -1 && targetY != -1;
	}
	
	@Override
	public boolean requiresTargeting() {
		return true;
	}


	@Override
	public void setScriptResult(Object result) {	
		length = (Integer) result;
	}

	@Override
	public PositionArray getAffectedTiles(int x, int y) {
		GameObjectPosition position = getUser().position();
		Tile userTile = position.tile();
		Orientation orientation = Orientation.calculateOrientationToTarget(false, position.getX(), position.getY(), x, y);
		if (orientation == null) {
			return null;
		}
		
		float myAngle = orientation.getDegrees();
		targetX = Math.round(userTile.getX() + length*MathUtils.sinDeg(myAngle));
		targetY = Math.round(userTile.getY() + length*MathUtils.cosDeg(myAngle));
		
		PositionArray returnValue = MathUtil.getCone(userTile.getX(), userTile.getY(), degrees, length, orientation);
		returnValue.removeValue(userTile.getX(), userTile.getY());
		return returnValue;
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
		return length;
	}
	
	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "cone", length);
	}
}
