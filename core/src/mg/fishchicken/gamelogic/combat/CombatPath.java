package mg.fishchicken.gamelogic.combat;

import java.util.Iterator;

import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.AttackAction;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.graphics.lights.GamePointLight;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.Path.Step;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * This represents a path that a character might take during combat.
 * 
 * The path knows how to calculate itself based on the target tile,
 * how to draw itself and how to determine the AP cost of each step.
 * 
 * @author ANNUN
 *
 */
public class CombatPath {
	public static final int ALL = -1;
	
	protected Path path;
	private Array<GamePointLight> lights = new Array<GamePointLight>();
	
	public CombatPath() {
		path = new Path();
	}
	
	public void clear() {
		path.clear();
		destroyLights();
	}
	
	public void setTarget(GameCharacter mover, int x, int y, GameMap map) {
		this.clear();
		
		Tile pos = mover.position().tile();
		
		Class<? extends Action> action = map.getPathFinder().getActionForDestination(mover, x, y);
		
		// we will only calculate the combat path during combat
		// if the target tile is no further than the max tiles the character
		// can move * 1.5 to avoid computing needlessly long paths
		if (mover.position().dst(x, y) >= ((mover.stats().getAPMax() * 1.5f) / Configuration.getAPCostMove()) &&
				MoveToAction.class.equals(action)) {
			return;
		}
		map.getPathFinder().findPath(mover, pos.getX(), pos.getY(), x, y, path, true);
		
		Class<? extends Action> finalAction = path.getFinalAction();
		Object target = path.getTarget();
		
		// if we are attacking with a ranged weapon, remove all but the last step
		if (AttackAction.class.equals(finalAction)
				&& AttackAction.isRangedAttack(mover, x, y)) {
			Step lastStep = path.getLastStep();
			path.clear();
			if (lastStep != null) {
				path.appendStep(lastStep.getX(), lastStep.getY(), 0, lastStep.getActionCost());
			}
		}
		else {
			if (target instanceof Trapable) {
				((Trapable)target).findSafeDisarmPath(path, mover, map.getPathFinder(), finalAction);
			}
			if (path.getLength() > 0) {
				path.removeStep(0);
			}
		}
		
		createLights(map, path);
	}
	
	public Class<? extends Action> getAction() {
		return path.getFinalAction();
	}
	
	public Object getTarget() {
		return path.getTarget();
	}
	
	public int getLength() {
		return path.getLength();
	}
	
	public Step getStep(int i) {
		return path.getStep(i);
	}
	
	public Step getLastStep() {
		return path.getLastStep();
	}
	
	public boolean contains(int x, int y) {
		return path.contains(x, y);
	}
	
	public void createLights(GameMap map, Path path) {
		createLights(map, path, Color.WHITE, 1, ALL);
	}
	
	public void createLights(GameMap map, Path path, Color color, int radius, int numberOfLights) {
		
		for (int i = 0; i < path.getLength() && (numberOfLights == ALL || i <= numberOfLights); ++i) {
			Step step = path.getStep(i);
			Vector2 tempVector = MathUtil.getVector2().set(step.getX()+0.5f, step.getY()+0.5f);
			map.projectFromTiles(tempVector);
			GamePointLight newLight = new GamePointLight("pathLight",
					map.getLightsRayHandler(), map, new LightDescriptor(color,
							radius, null), tempVector.x, tempVector.y, 8, map.isIsometric());
			MathUtil.freeVector2(tempVector);
			newLight.setStaticLight(true);
			newLight.setXray(true);
			newLight.setSoft(false);
			newLight.setSoftnessLenght(3);
			lights.add(newLight);
		}
	}
	
	public void destroyLights() {
		Iterator<GamePointLight> iterator = lights.iterator();
		while (iterator.hasNext()) {
			GamePointLight light = iterator.next();
			if (!light.wasRemoved()) {
				light.remove();
			}
			iterator.remove();
		}
	}	
}
