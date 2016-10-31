package mg.fishchicken.pathfinding;

import mg.fishchicken.gamelogic.actions.Action;

import com.badlogic.gdx.utils.Array;

/**
 * A path determined by some path finding algorithm. A series of steps from
 * the starting location to the target location. This includes a step for the
 * initial location.
 * 
 * @author Kevin Glass
 */
public class Path {
	
	/** The list of steps building up this path */
	protected Array<Step> steps = new Array<Step>();
	
	private Class<? extends Action> finalAction;
	private Object target;
	
	/**
	 * Create an empty path
	 */
	public Path() {
		
	}
	
	public void setFinalAction(Class<? extends Action> action) {
		this.finalAction = action;
	}
	
	public Class<? extends Action> getFinalAction() {
		return finalAction;
	}
	
	public void setTarget(Object target) {
		this.target = target;
	}
	
	public Object getTarget() {
		return target;
	}
	/**
	 * Gets the last step on this path.
	 * @return
	 */
	public Step getLastStep() {
		if (steps.size > 0) {
			return steps.get(steps.size - 1);
		}
		return null;
	}

	/**
	 * Get the length of the path, i.e. the number of steps
	 * 
	 * @return The number of steps in this path
	 */
	public int getLength() {
		return steps.size;
	}
	
	public int getTotalMoveCost() {
		int total = 0;
		for (Step step : steps) {
			total += step.getMoveCost();
		}
		return total;
	}
	
	public int getTotalActionCost() {
		int total = 0;
		for (Step step : steps) {
			total += step.getActionCost();
		}
		return total;
	}
	
	/**
	 * Get the step at a given index in the path
	 * 
	 * @param index The index of the step to retrieve. Note this should
	 * be >= 0 and < getLength();
	 * @return The step information, the position on the map.
	 */
	public Step getStep(int index) {
		return steps.get(index);
	}
	
	/**
	 * Get the x coordinate for the step at the given index
	 * 
	 * @param index The index of the step whose x coordinate should be retrieved
	 * @return The x coordinate at the step
	 */
	public int getX(int index) {
		return getStep(index).x;
	}

	/**
	 * Get the y coordinate for the step at the given index
	 * 
	 * @param index The index of the step whose y coordinate should be retrieved
	 * @return The y coordinate at the step
	 */
	public int getY(int index) {
		return getStep(index).y;
	}

	/**
	 * Append a step to the path.  
	 * 
	 * @param x The x coordinate of the new step
	 * @param y The y coordinate of the new step
	 */
	public Step appendStep(int x, int y, int cost, int actionCost) {
		Step step = new Step(x,y, cost, actionCost);
		steps.add(step);
		return step;
	}
	
	/**
	 * Prepend a step to the path.  
	 * 
	 * @param x The x coordinate of the new step
	 * @param y The y coordinate of the new step
	 */
	public Step prependStep(int x, int y, int cost, int actionCost) {
		Step step = new Step(x, y, cost, actionCost);
		steps.insert(0, step);
		return step;
	}
	
	/**
	 * Check if this path contains the given step
	 * 
	 * @param x The x coordinate of the step to check for
	 * @param y The y coordinate of the step to check for
	 * @return True if the path contains the given step
	 */
	public boolean contains(int x, int y) {
		return steps.contains(new Step(x,y), false);
	}
	
	/**
	 * Removes all steps from this Path.
	 */
	public void clear() {
		steps.clear();
	}
	
	/**
	 * Removes and returns the step at the specified index. 
	 * 
	 * @param index
	 * @return
	 */
	public Step removeStep(int index) {
		return steps.removeIndex(index);
	}
	
	/**
	 * A single step within the path
	 * 
	 * @author Kevin Glass
	 */
	public static class Step {
		/** The x coordinate at the given step */
		private int x;
		/** The y coordinate at the given step */
		private int y;
		/** The cost to move to this step. */
		private int moveCost;
		/** The cost to perform the action of this step. */
		private int actionCost;
		/** Whether this step is an end step.
		 * Normally, the last step on the path is the end step,
		 * but we could have a step on the path sooner thats marked as an end step.
		 * If such a step is encountered during a MoveToAction, the action
		 * will terminate on that step.
		 */
		private boolean endStep;
		
		private Step(int x, int y) {
			this.x = x;
			this.y = y;
			this.moveCost = 1;
			this.actionCost = 0;
		}
		
		/**
		 * Create a new step
		 * 
		 * @param x The x coordinate of the new step
		 * @param y The y coordinate of the new step
		 */
		public Step(int x, int y, int moveCost, int actionCost) {
			this(x, y);
			this.moveCost = moveCost;
			this.actionCost = actionCost;
		}
		
		public boolean isEndStep() {
			return endStep;
		}
		
		public void setEndStep(boolean value) {
			this.endStep = value;
		}
		
		/**
		 * Get the x coordinate of the new step
		 * 
		 * @return The x coodindate of the new step
		 */
		public int getX() {
			return x;
		}

		/**
		 * Get the y coordinate of the new step
		 * 
		 * @return The y coodindate of the new step
		 */
		public int getY() {
			return y;
		}
		
		/**
		 * Returns the AP cost of getting into this step.
		 * @return
		 */
		public int getMoveCost() {
			return moveCost;
		}
		
		public void setMoveCost(int cost) {
			this.moveCost = cost;
		}
		
		/**
		 * Returns the AP cost of performing the action associated with this step.
		 * @return
		 */
		public int getActionCost() {
			return actionCost;
		}
		
		public void setActionCost(int cost) {
			this.actionCost = cost;
		}
		
		/**
		 * @see Object#hashCode()
		 */
		public int hashCode() {
			return x*y;
		}

		/**
		 * @see Object#equals(Object)
		 */
		public boolean equals(Object other) {
			if (other instanceof Step) {
				Step o = (Step) other;
				
				return (o.x == x) && (o.y == y);
			}
			
			return false;
		}
	}
}
