package box2dLight;

import mg.fishchicken.core.util.CoreUtil;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Vector2;

public class FixtureUserData {
	
	public static enum UserDataType {
		LIGHT_BLOCKER, LOS_BLOCKER_TILE, LOS_BLOCKER_POLYGON, LOS_BLOCKER_POLYGON_GROUND, LOS_BLOCKER_LINE
	}
	
	public String id;
	public Vector2 position;
	public UserDataType type;
	public Polygon polygon;
	public Polyline polyline;
	
	public FixtureUserData(UserDataType type)  {
		this.type = type;
	}
	
	public FixtureUserData(UserDataType type, String id)  {
		this(type);
		this.id = id;
	}
	
	public FixtureUserData(UserDataType type, String id, Polygon polygon)  {
		this(type, id);
		this.polygon = polygon;
	}
	
	public FixtureUserData(UserDataType type, String id, Polyline polyline)  {
		this(type, id);
		this.polyline = polyline;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FixtureUserData)) {
			return false;
		}
		
		FixtureUserData comparedUD = (FixtureUserData) obj;		
		return CoreUtil.equals(id, comparedUD.id) && CoreUtil.equals(type, comparedUD.type)
				&& CoreUtil.equals(position, comparedUD.position); 
	}
	
	@Override
	public int hashCode() {
		return (id + type.toString() + position.toString()).hashCode();
	}
}