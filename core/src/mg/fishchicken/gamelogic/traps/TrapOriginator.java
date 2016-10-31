package mg.fishchicken.gamelogic.traps;

import java.util.Locale;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.traps.Trap;
import mg.fishchicken.pathfinding.AStarPathFinder;
import mg.fishchicken.pathfinding.Path;

import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Simple dummy game object that exists only to determine where a trap
 * projectile should originate and is saved as a killer in case the trap
 * killed a character.
 * 
 * Originators are not saved and never active.
 *
 */
public class TrapOriginator extends GameObject implements Trapable  {
	
	private String name;
	private boolean isTrapLocation;
	private Trapable parentTrapable;
	
	public TrapOriginator(TrapLocation trapLocation, float[] vertices) {
		this(trapLocation.getInternalId(), trapLocation.getTrap().getType().getName(), trapLocation.getMap(), trapLocation);
		isTrapLocation = true;
		Vector2 polygonCenter = GeometryUtils.polygonCentroid(vertices, 0, vertices.length, MathUtil.getVector2());
		position().set(polygonCenter);
		MathUtil.freeVector2(polygonCenter);
	}
	
	private TrapOriginator(String id, String name, GameMap map, Trapable parentTrapable) {
		super(id, id);
		setShouldBeSaved(false);
		setActive(false);
		setWidth(1);
		setHeight(1);
		s_projectileOriginXOffset = 0f;
		s_projectileOriginYOffset = 0f;
		this.name = name;
		this.parentTrapable = parentTrapable;
		setMap(map);
	}
	
	public boolean isTrapLocation() {
		return isTrapLocation;
	}
	
	@Override
	public Path findSafeDisarmPath(Path path, GameObject mover, AStarPathFinder pathFinder, Class<? extends Action> action) {
		return parentTrapable.findSafeDisarmPath(path, mover, pathFinder, action);
	}
	
	public Trap getTrap() {
		return parentTrapable.getTrap();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isMapRemovable() {
		return false;
	}
	
	@Override
	protected String calculateInternalId(String id, String type) {
		return (type+"#"+this.getClass().getSimpleName()+id).toLowerCase(Locale.ENGLISH);
	}
	
	@Override
	public void onHit(Projectile projectile, GameObject originator) {
	}

	@Override
	public GameObject getOriginatorGameObject() {
		return this;
	}

}
