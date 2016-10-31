package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Tile;

public class Line extends TargetType {

	private int length, size;
	private float targetX, targetY; 
	
	public Line() {
		super();
		length = -1;
	}
	
	@Override
	public float getSize() {
		return size;
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
	public void setScriptResult(Object result) {
		if (result != null) {
			length = (Integer) result;
		}
	}

	@Override
	public boolean requiresTargeting() {
		return true;
	}

	@Override
	public boolean isValidTarget() {
		return size>0;
	}
	
	@Override
	public void setTarget(int x, int y, GameMap map) {
		targetX = x;
		targetY = y;
		super.setTarget(x, y, map);
	}

	@Override
	protected PositionArray getAffectedTiles(int x, int y) {
		Tile pos = getUser().position().tile();
		PositionArray line = MathUtil.getLine(pos.getX(), pos.getY(), x, y, length);
		if (line.size()>1) {
			line.removeIndex(0);
		}
		size = line.size();
		return line;
	}

	@Override
	public String getUIString() {
		if (length > 0) {
			return Strings.getString(STRING_TABLE, "lineLimited", length);
		}
		return Strings.getString(STRING_TABLE, "lineUnlimited");
	}
}
