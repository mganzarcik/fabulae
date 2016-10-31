package mg.fishchicken.core.input.tools;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.util.Pair;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.DisarmTrapAction;
import mg.fishchicken.gamelogic.actions.LockpickAction;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locks.Lockable;
import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.locks.Lock;
import mg.fishchicken.gamestate.traps.Trap;

public enum Tool {
	
	LOCKPICK {
		public Pair<Class<? extends Action>, PositionedThing> getActionForObject(PlayerCharacterController pcc, GameObject actionPerformer, GameMap map, GameObject clickedGO) {
			if (clickedGO instanceof Lockable) {
				Lock lock = ((Lockable)clickedGO).getLock();
				if (lock != null && lock.isLocked() && lock.isPickable() && actionPerformer.canPerformAction(LockpickAction.class)) {
					pair.setLeft(LockpickAction.class);
					pair.setRight(clickedGO);
					return pair;
				}
			}
			return null;
		}
	}, 
	DISARM {
		public Pair<Class<? extends Action>, PositionedThing> getActionForObject(PlayerCharacterController pcc, GameObject actionPerformer, GameMap map, GameObject clickedGO) {
			PositionedThing actionTarget = pcc.getTrapLocationCurrentlyHovered();
			if (actionTarget == null) {
				actionTarget = clickedGO;
			}
			
			if (actionTarget instanceof Trapable) {
				Trap trap = ((Trapable)actionTarget).getTrap();
				if (trap != null && trap.isDetected() && !trap.isDisarmed() && actionPerformer.canPerformAction(DisarmTrapAction.class)) {
					pair.setLeft(DisarmTrapAction.class);
					pair.setRight(actionTarget);
					return pair;
				}
			}
			return null;
		}
	};
	
	private static final Pair<Class<? extends Action>, PositionedThing> pair = new Pair<Class<? extends Action>, PositionedThing>();
	
	/**
	 * Returns the action and its target for the specified performer, map, clicked coordinates and the game object that was found on the coordinates
	 * @param actionPerformer
	 * @param map
	 * @param tileCoordinates
	 * @param clickedGO
	 * @return
	 */
	public abstract Pair<Class<? extends Action>, PositionedThing> getActionForObject(PlayerCharacterController pcc, GameObject actionPerformer, GameMap map, GameObject clickedGO);
}
