package mg.fishchicken.gamelogic.effects.targets;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Tile;

public class SeveralAdjacent extends AllAdjacent {

	protected int length;
	private float targetX, targetY; 
	
	public SeveralAdjacent() {
		super();
		length = 2;
	}
	
	@Override
	public float getSize() {
		return length;
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
			MathUtil.boxValue(length, 1, 3);
		}
	}

	@Override
	public boolean requiresTargeting() {
		return true;
	}

	@Override
	public boolean isValidTarget() {
		if (length == 1) {
			return targets.size > 0;
		}
		return true;
	}
	
	@Override
	public void setTarget(int x, int y, GameMap map) {
		Tile userTile = getUser().position().tile();
		PositionArray lineTo = MathUtil.getLine(userTile.getX(), userTile.getY(), x, y, 2);
		if (lineTo.size() < 2) {
			targetX = x;
			targetY = y;
		} else {
			targetX=lineTo.getX(1);
			targetY=lineTo.getY(1);
		}
		super.setTarget(x, y, map);
	}

	@Override
	protected PositionArray getAffectedTiles(int x, int y) {
		Tile userTile = getUser().position().tile();
		int userX = userTile.getX();
		int userY = userTile.getY();
		float dst = MathUtil.distance((int)targetX, (int)targetY, userX, userY);
		if ((length > 1 && dst == 0) || dst >= 2) {
			return null;
		}
		
		PositionArray returnValue = new PositionArray();
		returnValue.add((int)targetX, (int)targetY);
		
		if (length > 1) {
			PositionArray tiles = super.getAffectedTiles(userX, userY);
			while (returnValue.size() < length) {
				addNextTile(returnValue.getX(returnValue.size()-1), returnValue.getY(returnValue.size()-1), tiles, returnValue);
			}
		}
		
		return returnValue;
	}
	
	private void addNextTile(int x, int y, PositionArray tiles, PositionArray result) {
		int index = 0;
		for (int i = 0; i < tiles.size(); ++i) {
			index = i;
			if (tiles.getX(i) == x && tiles.getY(i) == y) {
				break;
			}
		}
		
		++index;
		if (index >= tiles.size()) {
			index = 0;
		}
		result.add(tiles.getX(index), tiles.getY(index));
	}
	
	@Override
	public String getUIString() {
		return Strings.getString(STRING_TABLE, "severalAdjacent", length);
	}

}
