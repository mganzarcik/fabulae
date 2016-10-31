package mg.fishchicken.gamelogic.traps;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.gamestate.SaveablePolygon;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.traps.Trap;
import mg.fishchicken.graphics.renderers.FilledPolygonRenderer;
import mg.fishchicken.pathfinding.AStarPathFinder;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.Path.Step;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Trap location is a special game location that can be trapped.
 * 
 * The trap will be sprung when any character enters the location.
 *
 */
public class TrapLocation extends GameLocation implements Trapable {

	private Trap trap;
	private TrapOriginator trapOriginator;
	
	public TrapLocation() {
		super();
	}
	
	public TrapLocation(String id, String type, SaveablePolygon polygon, boolean detected,  boolean disarmed) {
		super(id, type, polygon);
		trap = new Trap(detected, disarmed, TrapType.getTrap(type));
	}
	
	@Override
	public void setMap(GameMap map) {
		super.setMap(map);
		trapOriginator = new TrapOriginator(this, s_polygon.getTransformedVertices()); 
	}

	@Override
	public void onEntry(AbstractGameCharacter character) {
		super.onEntry(character);
		if (character instanceof GameCharacter) {
			if (TrapType.checkTrap((GameCharacter)character, this, "trapSprung")) {
				// if a trap was sprung, stop the character
				character.removeAllVerbActions();
			}
		}
	}
	
	@Override
	public Position position() {
		return trapOriginator.position();
	}

	@Override
	public Trap getTrap() {
		return trap;
	}
	
	@Override
	public Path findSafeDisarmPath(Path path, GameObject mover, AStarPathFinder pathFinder, Class<? extends Action> action) {
		Tile src = mover.position().tile();
		Tile target = getOriginatorGameObject().position().tile();
		pathFinder.findPath(mover, src.getX(), src.getY(), target.getX(), target.getY(), path, true);
		
		Step lastStep = path.getLastStep();
		
		// once we have the path to the originator (which is in our middle)
		// we just remove all the steps that are still part of us
		int length = path.getLength();
		for (int i = length-1; i > 0; --i) {
			Step step = path.getStep(i);
			if (contains(step.getX(), step.getY())) {
				path.removeStep(i);
			}
		}
		
		Step newLastStep = path.getLastStep();
		if (!newLastStep.equals(lastStep)) {
			newLastStep.setEndStep(true);
			path.appendStep(lastStep.getX(), lastStep.getY(), 0, lastStep.getActionCost());
		}
		
		return path;
	}

	@Override
	public GameObject getOriginatorGameObject() {
		return trapOriginator;
	}
	
	public FilledPolygonRenderer createRenderer() {
		return new FilledPolygonRenderer(s_polygon, getMap());
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		trap.writeToXML(writer);
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		trap = new Trap(root);
	}

}
