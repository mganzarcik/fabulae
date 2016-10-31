package mg.fishchicken.pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.input.tools.Tool;
import mg.fishchicken.core.util.Pair;
import mg.fishchicken.core.util.PositionArray;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.traps.TrapLocation;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.pathfinding.Path.Step;
import mg.fishchicken.pathfinding.heuristics.ClosestHeuristic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * A path finder implementation that uses the AStar heuristic based algorithm
 * to determine a path. 
 * 
 * @author Kevin Glass
 */
public class AStarPathFinder implements PathFinder {
	/** The set of nodes that have been searched through */
	private ArrayList<Node> closed = new ArrayList<Node>();
	/** The set of nodes that we do not yet consider fully searched */
	private PriorityList open = new PriorityList();
	
	/** The map being searched */
	private GameMap map;
	/** The maximum depth of search we're willing to accept before giving up */
	private int maxSearchDistance;
	
	/** The complete set of nodes across the map */
	private Node[][] nodes;
	/** True if we allow diagonal movement */
	private boolean allowDiagMovement;
	/** The heuristic we're applying to determine which nodes to search first */
	private AStarHeuristic heuristic;
	/** The node we're currently searching from */
	private Node current;

	/** The x coordinate of the tile we're moving to */
	private int targetX;
	/** The y coordinate of the tile we're moving to */
	private int targetY;
	private boolean destinationBlocked;
	private Object target;
	private Class<? extends Action> finalAction;

	private PositionArray temPositionArray = new PositionArray();
	private ObjectSet<GameObject> tempSet = new ObjectSet<GameObject>();
	private PlayerCharacterController pcc;
	
	/**
	 * Create a path finder with the default heuristic - closest to target.
	 * 
	 * @param map The map to be searched
	 * @param maxSearchDistance The maximum depth we'll search before giving up
	 * @param allowDiagMovement True if the search should try diagonal movement
	 */
	public AStarPathFinder(GameMap map, PlayerCharacterController pcc, int maxSearchDistance, boolean allowDiagMovement) {
		this(map, pcc, maxSearchDistance, allowDiagMovement, new ClosestHeuristic());
	}

	/**
	 * Create a path finder 
	 * 
	 * @param heuristic The heuristic used to determine the search order of the map
	 * @param map The map to be searched
	 * @param maxSearchDistance The maximum depth we'll search before giving up
	 * @param allowDiagMovement True if the search should try diaganol movement
	 */
	public AStarPathFinder(GameMap map, PlayerCharacterController pcc, int maxSearchDistance, 
						   boolean allowDiagMovement, AStarHeuristic heuristic) {
		this.heuristic = heuristic;
		this.map = map;
		this.pcc = pcc;
		this.maxSearchDistance = maxSearchDistance;
		this.allowDiagMovement = allowDiagMovement;
		
		nodes = new Node[map.getMapWidth()][map.getMapHeight()];
		for (int x=0;x<map.getMapWidth();x++) {
			for (int y=0;y<map.getMapHeight();y++) {
				nodes[x][y] = new Node(x,y);
			}
		}
	}
	
	public Path findPath(GameObject mover, int sx, int sy, int tx, int ty) {
		Path path = new Path();
		findPath(mover, sx, sy, tx, ty, path);
		return path;
	}
	
	public void findPath(GameObject mover, int sx, int sy, int tx, int ty, Path path) {
		findPath(mover, sx, sy, tx, ty, path, false);
	}
	
	public Class<? extends Action> getActionForDestination(GameObject mover, int x, int y) {
		GameObject goAtCoords = map.getGameObjectAt(x, y);
		target = goAtCoords;
		Tool activeTool = pcc.getActiveTool();
		Class<? extends Action> action = null;
		if(activeTool != null) {
			Pair<Class<? extends Action>, PositionedThing> pair = activeTool.getActionForObject(pcc, mover, map, goAtCoords);
			if (pair != null) {
				action = pair.getLeft();
				target = pair.getRight();
			} 
		} else  {
			action = map.getActionForTarget(mover, goAtCoords);
		}
		return action;
	}
	
	public void findPath(GameObject mover, int sx, int sy, int tx, int ty, Path path, boolean addLastStepEvenIfBlocked) {
		findPath(mover, map.getGameObjectAt(tx,ty), sx, sy, tx, ty, path, addLastStepEvenIfBlocked, getActionForDestination(mover, tx, ty));
	}
	
	public void findPath(GameObject mover, GameObject targetGO, int sx, int sy, int tx, int ty, Path path, boolean addLastStepEvenIfBlocked, Class<? extends Action> finalAction) {
		path.clear();
		
		// if the target is unavailable, we are done
		if (map.tileUnavailable(tx, ty)) {
			return;
		}
		
		this.target = targetGO;
		this.finalAction = finalAction;
		
		path.setFinalAction(finalAction);
		path.setTarget(target);
		
		// if we have no action, we are done
		if (finalAction == null) {
			return;
		}
		
		int minDistance = MoveToAction.class.equals(finalAction) ? 1 : 2;
		if (!mover.canPerformAction(MoveToAction.class) && Vector2.dst(sx, sy, tx, ty) >= minDistance) {
			return;
		}
		
		current = null;
		this.destinationBlocked = false;
		
		// easy first check, if the destination is blocked, we can't get there
		this.targetX = tx;
		this.targetY = ty;

		if (tx < 0 || ty < 0 || tx >= map.getMapWidth() || ty >= map.getMapHeight()) {
			return;
		}
		
		destinationBlocked = map.blocked(mover, tx, ty);
		 
		if (destinationBlocked && !pathToTargetExists(mover)) {
			return;
		}
		
		// if we are already on target, return a path with a single step
		if (sx == tx && sy == ty) {
			path.appendStep(tx, ty, 0, mover.getCostForAction(finalAction, this.target));
			return;
		}

		for (int x=0;x<map.getMapWidth();x++) {
			for (int y=0;y<map.getMapHeight();y++) {
				nodes[x][y].reset();
			}
		}
		
		// initial state for A*. The closed group is empty. Only the starting
		// tile is in the open list and it's cost is zero, i.e. we're already there
		nodes[sx][sy].cost = 0;
		nodes[sx][sy].depth = 0;
		closed.clear();
		open.clear();
		addToOpen(nodes[sx][sy]);
		
		nodes[tx][ty].parent = null;
		
		// while we haven't found the goal and haven't exceeded our max search depth
		int maxDepth = 0;
		while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
			// pull out the first node in our open list, this is determined to 
			// be the most likely to be the next step based on our heuristic
			int lx = sx;
			int ly = sy;
			if (current != null) {
				lx = current.x;
				ly = current.y;
			}
			
			current = getFirstInOpen();
			
			if (current == nodes[tx][ty]) {
				if (isValidLocation(mover,lx,ly,tx,ty)) {
					break;
				}
			}
			
			removeFromOpen(current);
			addToClosed(current);
			
			// search through all the neighbours of the current node evaluating
			// them as next steps
			for (int x=-1;x<2;x++) {
				for (int y=-1;y<2;y++) {
					// not a neighbour, its the current tile
					if ((x == 0) && (y == 0)) {
						continue;
					}
					
					// if we're not allowing diagonal movement then only 
					// one of x or y can be set
					if (!allowDiagMovement) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}
					
					// determine the location of the neighbour and evaluate it
					int xp = x + current.x;
					int yp = y + current.y;
					
					if (isValidLocation(mover,current.x,current.y,xp,yp)) {
						float movementCost = getMovementCost(mover, current.x, current.y, xp, yp);
						int apCost = getApMoveCost(mover, current.x, current.y, xp, yp);
						float finalCost = GameState.isCombatInProgress() ? apCost : movementCost;
						
						// the cost to get to this node is cost the current plus the movement
						// cost to reach this node. Note that the heuristic value is only used
						// in the sorted open list
						float nextStepCost = current.cost + finalCost;
						Node neighbour = nodes[xp][yp];
						
						// if the new cost we've determined for this node is lower than 
						// it has been previously makes sure the node hasn't been discarded. We've
						// determined that there might have been a better path to get to
						// this node so it needs to be re-evaluated
						if (nextStepCost < neighbour.cost) {
							if (inOpenList(neighbour)) {
								removeFromOpen(neighbour);
							}
							if (inClosedList(neighbour)) {
								removeFromClosed(neighbour);
							}
						}
						
						// if the node hasn't already been processed and discarded then
						// reset it's cost to our current cost and add it as a next possible
						// step (i.e. to the open list)
						if (!inOpenList(neighbour) && !(inClosedList(neighbour))) {
							neighbour.cost = nextStepCost;
							neighbour.apCost = apCost;
							neighbour.heuristic = getHeuristicCost(mover, xp, yp, tx, ty);
							maxDepth = Math.max(maxDepth, neighbour.setParent(current));
							addToOpen(neighbour);
						} 
					}
				}
			}
		}

		// since we've got an empty open list or we've run out of search 
		// there was no path. Just return null
		if (nodes[tx][ty].parent == null) {
			return;
		}
		
		// At this point we've definitely found a path so we can uses the parent
		// references of the nodes to find out way from the target location back
		// to the start recording the nodes on the way.
		Node target = nodes[tx][ty];
		boolean first = true;
		while (target != nodes[sx][sy]) {
			if (!first || !destinationBlocked || (addLastStepEvenIfBlocked && !MoveToAction.class.equals(finalAction))) {
				Step step = path.prependStep(target.x, target.y, (int)target.apCost, 0);
				if (first && mover instanceof GameCharacter) {
					step.setActionCost(mover.getCostForAction(finalAction, this.target));
					if (destinationBlocked) {
						step.setMoveCost(0);
					}
				}
			}
			first = false;
			target = target.parent;
		}
		path.prependStep(sx,sy, 0, 0);
		
		// thats it, we have our path 
		return;
	}
	
	private boolean pathToTargetExists(GameObject mover) {
		for (int i = -1; i <= 1; ++i) {
			for (int j = -1; j <= 1; ++j) {
				if (i == 0 && j == 0) {
					continue;
				}
				if (!map.blocked(mover, targetX+i, targetY+j)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Get the first element from the open list. This is the next
	 * one to be searched.
	 * 
	 * @return The first element in the open list
	 */
	protected Node getFirstInOpen() {
		return (Node) open.first();
	}
	
	/**
	 * Add a node to the open list
	 * 
	 * @param node The node to be added to the open list
	 */
	protected void addToOpen(Node node) {
		node.setOpen(true);
		open.add(node);
	}
	
	/**
	 * Check if a node is in the open list
	 * 
	 * @param node The node to check for
	 * @return True if the node given is in the open list
	 */
	protected boolean inOpenList(Node node) {
		return node.isOpen();
	}
	
	/**
	 * Remove a node from the open list
	 * 
	 * @param node The node to remove from the open list
	 */
	protected void removeFromOpen(Node node) {
		node.setOpen(false);
		open.remove(node);
	}
	
	/**
	 * Add a node to the closed list
	 * 
	 * @param node The node to add to the closed list
	 */
	protected void addToClosed(Node node) {
		node.setClosed(true);
		closed.add(node);
	}
	
	/**
	 * Check if the node supplied is in the closed list
	 * 
	 * @param node The node to search for
	 * @return True if the node specified is in the closed list
	 */
	protected boolean inClosedList(Node node) {
		return node.isClosed();
	}
	
	/**
	 * Remove a node from the closed list
	 * 
	 * @param node The node to remove from the closed list
	 */
	protected void removeFromClosed(Node node) {
		node.setClosed(false);
		closed.remove(node);
	}
	
	/**
	 * Check if a given location is valid for the supplied mover
	 * 
	 * @param mover The mover that would hold a given location
	 * @param sx The starting x coordinate
	 * @param sy The starting y coordinate
	 * @param x The x coordinate of the location to check
	 * @param y The y coordinate of the location to check
	 * @return True if the location is valid for the given mover
	 */
	protected boolean isValidLocation(GameObject mover, int sx, int sy, int x, int y) {
		boolean invalid = x < 0 || y < 0 || x >= map.getMapWidth() || y >= map.getMapHeight();
		
		if (!invalid && (sx != x || sy != y)) {
			if (!destinationBlocked || x != targetX || y != targetY) {
				// do not allow if the destination is blocked, or if both neighboring tiles are blocked
				// (meaning we could only get there diagonally)
				invalid = map.blocked(mover, x, y) || (map.blocked(mover, sx, y) && map.blocked(mover, x, sy));
			}
		}
		
		return !invalid;
	}
	
	/**
	 * Get the cost for the mover to move from the suppled tile to the other supplied tile.
	 */
	private float getMovementCost(GameObject mover, int fromX, int fromY, int toX, int toY) {
		float cost = 0;
		if (mover instanceof AbstractGameCharacter) {
			AbstractGameCharacter character = (AbstractGameCharacter) mover;
			cost = map.getMoveCost(character, toX, toY);
			// player characters will try to avoid known traps if possible
			// unless the trap is at our target and we have a disarm tool active
			if (character.belongsToPlayerFaction()) {
				TrapLocation trap = map.getDetectedTrapLocationAt(toX, toY);
				if (trap != null) {
					Tile target = trap.getOriginatorGameObject().position().tile();
					if (pcc.getActiveTool() != Tool.DISARM || (target.getX() != toX && target.getY() != toY)) {
						cost = cost * 50f;
					}
				}
			}
			// if the tile is not the destination or the destination is not blocked, some
			// special rules apply for diagonal movement
			if (fromX != toX && fromY != toY && (!destinationBlocked || (targetX != toX && targetY != toY))) {
				// moving diagonally is 1.5 times more costly than moving straight if both neighboring tiles are unblocked
				// to make sure characters don't move diagonally optically away from the target at first if there
				// is some obstacle in their path, only to then return diagonally back later
				if (!map.blocked(mover, fromX, toY) && !map.blocked(mover, toX, fromY)) {
					cost = 1.5f * cost;
				} 
				else {
					// otherwise it is 2.5 times the cost, discouraging movement diagonally through blocked corners
					// since it can cause character clipping or even moving through walls and other weird bugs
					cost = 2.5f * cost;
				}	
			}
		}
		return cost;
	}	
	
	private int getApMoveCost(GameObject mover, int fromX, int fromY, int toX, int toY) {
		int cost = 0;
		
		if (mover instanceof AbstractGameCharacter) {
			AbstractGameCharacter character = (AbstractGameCharacter) mover;
			float moveCost = map.getAPMoveCost(character, toX, toY);
			
			// sneaking and detecting traps costs twice as much
			if (character.isSneaking() || character.isDetectingTraps()) {
				moveCost *= 2;
			}
			cost += moveCost;
			// diagonal movements count as two steps, so double the cost
			if (fromX != toX && fromY != toY) {
				cost += moveCost;
			}
			// movements next to an enemy have a cost penalty
			if (nextToEnemy(character, fromX, fromY)) {
				cost += Configuration.getDisengageMovementPenalty();
			}
			
		}
		return cost;
	}
	
	/**
	 * Returns true if the supplied tile on the supplied map is next to any enemy
	 * of the supplied mover.
	 * 
	 * This method is definitely not thread safe.
	 * 
	 * @param mover
	 * @param tile
	 * @param map
	 * @return
	 */
	public boolean nextToEnemy(AbstractGameCharacter mover, int x, int y) {
		temPositionArray.clear();
		temPositionArray.add(x-1, y);
		temPositionArray.add(x, y-1);
		temPositionArray.add(x+1, y);
		temPositionArray.add(x, y+1);
		temPositionArray.add(x+1, y+1);
		temPositionArray.add(x-1, y-1);
		temPositionArray.add(x+1, y-1);
		temPositionArray.add(x-1, y+1);
		tempSet.clear();
		map.getAllObjectsInArea(tempSet, temPositionArray, GameCharacter.class);
		
		for (GameObject neighbor : tempSet) {
			if (Faction.areHostile((AbstractGameCharacter)neighbor, mover)) {
				return true;
			}
		}
		
		return false;
	}


	/**
	 * Get the heuristic cost for the given location. This determines in which 
	 * order the locations are processed.
	 * 
	 * @param mover The entity that is being moved
	 * @param x The x coordinate of the tile whose cost is being determined
	 * @param y The y coordiante of the tile whose cost is being determined
	 * @param tx The x coordinate of the target location
	 * @param ty The y coordinate of the target location
	 * @return The heuristic cost assigned to the tile
	 */
	public float getHeuristicCost(GameObject mover, int x, int y, int tx, int ty) {
		return heuristic.getCost(map, mover, x, y, tx, ty);
	}
	
	/**
	 * A list that sorts any element provided into the list
	 *
	 * @author kevin
	 */
	private static class PriorityList {
		/** The list of elements */
		private List<Node> list = new LinkedList<Node>();
		
		/**
		 * Retrieve the first element from the list
		 *  
		 * @return The first element from the list
		 */
		public Object first() {
			return list.get(0);
		}
		
		/**
		 * Empty the list
		 */
		public void clear() {
			list.clear();
		}
		
		/**
		 * Add an element to the list - causes sorting
		 * 
		 * @param o The element to add
		 */
		public void add(Node o) {
			// float the new entry 
			for (int i=0;i<list.size();i++) {
				if ((list.get(i)).compareTo(o) > 0) {
					list.add(i, o);
					break;
				}
			}
			if (!list.contains(o)) {
				list.add(o);
			}
			//Collections.sort(list);
		}
		
		/**
		 * Remove an element from the list
		 * 
		 * @param o The element to remove
		 */
		public void remove(Node o) {
			list.remove(o);
		}
	
		/**
		 * Get the number of elements in the list
		 * 
		 * @return The number of element in the list
 		 */
		public int size() {
			return list.size();
		}
		
		public String toString() {
			StringBuilder fsb = StringUtil.getFSB();
			fsb.append("{");
			for (int i=0;i<size();i++) {
				fsb.append(list.get(i).toString());
				fsb.append(",");
			}
			fsb.append("}");
			String returnValue = fsb.toString();
			StringUtil.freeFSB(fsb);
			return returnValue;
		}
	}
	
	/**
	 * A single node in the search graph
	 */
	private static class Node implements Comparable<Node> {
		/** The x coordinate of the node */
		private int x;
		/** The y coordinate of the node */
		private int y;
		/** The path cost for this node */
		private float cost;
		/** The action points cost for this node */
		private int apCost;
		/** The parent of this node, how we reached it in the search */
		private Node parent;
		/** The heuristic cost of this node */
		private float heuristic;
		/** The search depth of this node */
		private int depth;
		/** In the open list */
		private boolean open;
		/** In the closed list */
		private boolean closed;
		
		/**
		 * Create a new node
		 * 
		 * @param x The x coordinate of the node
		 * @param y The y coordinate of the node
		 */
		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		/**
		 * Set the parent of this node
		 * 
		 * @param parent The parent node which lead us to this node
		 * @return The depth we have no reached in searching
		 */
		public int setParent(Node parent) {
			depth = parent.depth + 1;
			this.parent = parent;
			
			return depth;
		}
		
		/**
		 * @see Comparable#compareTo(Object)
		 */
		public int compareTo(Node other) {
			
			float f = heuristic + cost;
			float of = other.heuristic + other.cost;
			
			if (f < of) {
				return -1;
			} else if (f > of) {
				return 1;
			} else {
				return 0;
			}
		}
		
		/**
		 * Indicate whether the node is in the open list
		 * 
		 * @param open True if the node is in the open list
		 */
		public void setOpen(boolean open) {
			this.open = open;
		}
		
		/**
		 * Check if the node is in the open list
		 * 
		 * @return True if the node is in the open list
		 */
		public boolean isOpen() {
			return open;
		}
		
		/**
		 * Indicate whether the node is in the closed list
		 * 
		 * @param closed True if the node is in the closed list
		 */
		public void setClosed(boolean closed) {
			this.closed = closed;
		}
		
		/**
		 * Check if the node is in the closed list
		 * 
		 * @return True if the node is in the closed list
		 */
		public boolean isClosed() {
			return closed;
		}

		/**
		 * Reset the state of this node
		 */
		public void reset() {
			closed = false;
			open = false;
			cost = 0;
			depth = 0;
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "[Node "+x+","+y+"]";
		}
	}

}
