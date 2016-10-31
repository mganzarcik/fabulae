package mg.fishchicken.gamelogic.effects.targets;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.util.Iterator;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.projectiles.ProjectileTarget;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.combat.CombatPath;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.Effect.PersistentEffect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Represents a shape that can be targeted. It can be a single
 * tile, or a circle, cone, or anything else.
 * 
 * Every TargetType can be manually targetable or not.
 * 
 * Manually targetable TargetTypes require the player to actually choose a tile
 * on which they want to use the Effect. Once a tile is chosen,
 * the TargetType uses its internal logic to find actual targets in that area
 * and populate its list of targets, which can then be accessed via the 
 * getTargets() method.
 * 
 * If the TargetType does not require targeting, it populates the list
 * of targets automatically, usually based on the location of the user
 * of the effect.
 * 
 * It's an extension of the CombatPath, meaning it can also draw itself
 * and all that jazz.
 * 
 * @author ANNUN
 *
 */
public abstract class TargetType extends CombatPath implements ProjectileTarget {
	
	public static final String STRING_TABLE = "targetType."+Strings.RESOURCE_FILE_EXTENSION;
	
	@SuppressWarnings("unchecked")
	public static TargetType getTargetTypeInstance(String targetType, Script targetScript) {
		try {
			Class<? extends TargetType> effectClass = ClassReflection.forName(TargetType.class.getPackage().getName()+"."+targetType);
			TargetType target = ClassReflection.newInstance(effectClass);
			target.targetScript = targetScript;
			return target;
		} catch(ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	private GameCharacter user;
	protected Array<GameObject> targets;
	private int apCost;
	private boolean renderMoveToPath;
	private Script targetScript = null;
	
	public abstract void setScriptResult(Object result);
	public abstract boolean requiresTargeting();
	public abstract boolean isValidTarget();
	
	/**
	 * Returns a human readable description
	 * of this TargetType.
	 * @return
	 */
	public abstract String getUIString();

	public TargetType() {
		super();
		targets = new Array<GameObject>();
		renderMoveToPath = false;
	}

	@Override
	public void setTarget(GameCharacter mover, int x, int y, GameMap map) {
		setTarget(x, y, map);
	}
	
	public void setUser(GameCharacter user) {
		this.user = user;
	}
	
	public GameCharacter getUser() {
		return user;
	}
	
	public void setTarget(int x, int y, GameMap map) {
		if (map == null) {
			return;
		}
		targets.clear();
		PositionArray tiles = getAffectedTiles(x, y);
		if (tiles == null) {
			return;
		}
		clear();
		int existingMoveCost = 0;
		if (renderMoveToPath) {
			Tile position = getUser().position().tile();
			map.getPathFinder().findPath(getUser(), position.getX(), position.getY(), (int)getTargetX(), (int)getTargetY(), path, true);
			// remove the last and first step
			if (path.getLength() > 0) {
				path.removeStep(0);
				if (path.contains((int)getTargetX(), (int)getTargetY())) {
					path.removeStep(path.getLength()-1);
				}
			}
			existingMoveCost = path.getTotalMoveCost();
		}
		for (int i = 0; i < tiles.size(); ++i) {
			int tileX = tiles.getX(i);
			int tileY = tiles.getY(i);
			ObjectSet<GameObject> hitTargets = map.getAllGameObjectsAt(tileX+0.5f, tileY+0.5f, GameCharacter.class);
			for(GameObject go : hitTargets) {
				addTarget(go);
			}
			boolean isLastStep = ((int)getTargetX() == tileX && (int)getTargetY() == tileY);
			path.appendStep(tileX, tileY, isLastStep ? existingMoveCost : 0, isLastStep ? apCost : 0);
		}
		if (requiresTargeting()) {
			createLights(map, path);
		}
	}
	
	/**
	 * Tells this effect target to also render the path 
	 * required to get to the selected tile.
	 * 
	 * @param value
	 */
	public void setRenderMoveToPath(boolean value) {
		renderMoveToPath = value;
	}
	
	/**
	 * Returns a list of tiles that belong to the effect target
	 * around the supplied center coordinate.
	 * 
	 * If null is returned, the effect target will not 
	 * recalculate its current targets, but keep
	 * whatever was selected last time it's position changed.
	 * 
	 * If an empty list is returned, the target will become empty.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	protected abstract PositionArray getAffectedTiles(int x, int y);
	
	@Override
	public GameObject[] getGameObjects() {
		return targets.toArray(GameObject.class);
	}
	
	/**
	 * Adds the supplied game object to the list 
	 * of targets that belong to this TargetType
	 * 
	 * @param go
	 */
	public void addTarget(GameObject go) {
		targets.add(go);
	}

	/**
	 * Called when the supplied projectile hits this
	 * TargetType.
	 */
	public void onHit(Projectile projectile, GameObject originator) {
		for (GameObject go : targets) {
			go.onHit(projectile, originator);
		}
	}
	
	@Override
	public boolean filterUnviableTargets(Effect effect,
			EffectContainer effectContainer, GameObject user) {
		Iterator<GameObject> goIterator = targets.iterator();

		while (goIterator.hasNext()) {
			GameObject go = goIterator.next();
			if (!go.filterUnviableTargets(effect, effectContainer, user)) {
				goIterator.remove();
			}
		}
		
		return targets.size > 0;
	}
	
	@Override
	public void addPersistentEffect(EffectContainer container, Effect effect, float duration, GameObject user) {
		for (GameObject go : targets) {
			go.addPersistentEffect(container, effect, duration, user);
		}
	}
	
	@Override
	public void removePersitentEffect(String id) {
		for (GameObject go : targets) {
			go.removePersitentEffect(id);
		}
	}
	
	@Override
	public Array<PersistentEffect> getPersistentEffectsByType(String... types) {
		Array<PersistentEffect> returnValue = new Array<Effect.PersistentEffect>();
				
		for (GameObject go : targets) {
			returnValue.addAll(go.getPersistentEffectsByType(types));
		}
		
		return returnValue;
	}

	/**
	 * Gets the AP it will cost to execute the effect of this target.
	 * 
	 * @return
	 */
	public int getApCost() {
		return apCost;
	}
	
	/**
	 * Sets the AP it will cost to execute the effect of this target.
	 * 
	 * @return
	 */
	public void setApCost(int apCost) {
		this.apCost = apCost;
	}
	
	public void executeTargetScript(Object user, ObjectMap<String, Object> parameters) {
		if (targetScript == null) {
			return;
		}
		Binding context = new Binding();
		context.setVariable(Effect.USER, user);
		if (parameters != null) {
			for (String paramName : parameters.keys()) {
				context.setVariable(paramName, parameters.get(paramName));
			}
		}
		targetScript.setBinding(context);
        setScriptResult(targetScript.run());
	}
}
