package mg.fishchicken.core.input.tools;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.util.Pair;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.AttackAction;
import mg.fishchicken.gamelogic.actions.DisarmTrapAction;
import mg.fishchicken.gamelogic.actions.LockpickAction;
import mg.fishchicken.gamelogic.actions.TalkToAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locks.Lockable;
import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.locks.Lock;
import mg.fishchicken.gamestate.traps.Trap;

public enum Tool {
	ATTACK {
		public Class<? extends Action> getActionClass() {
			return AttackAction.class;
		}
		
		public boolean isApplicableFor(PositionedThing clickedGO) {
			return (clickedGO instanceof GameCharacter)
					&& GameState.getPlayerCharacterGroup().getGroupLeader() != clickedGO;
		}
	},
	TALKTO {
		@Override
		public Class<? extends Action> getActionClass() {
			return TalkToAction.class;
		}

		@Override
		public boolean isApplicableFor(PositionedThing clickedGO) {
			return (clickedGO instanceof GameCharacter) 
					&& !((GameCharacter)clickedGO).isHostileTowardsPlayer() 
					&& GameState.getPlayerCharacterGroup().getGroupLeader() != clickedGO;
		}
	},
	LOCKPICK {
		@Override
		public Class<? extends Action> getActionClass() {
			return LockpickAction.class;
		}

		@Override
		public boolean isApplicableFor(PositionedThing clickedGO) {
			if (clickedGO instanceof Lockable) {
				Lock lock = ((Lockable)clickedGO).getLock();
				return lock != null && lock.isLocked() && lock.isPickable();
			}
			return false;
		}
	}, 
	DISARM {
		@Override
		public Pair<Class<? extends Action>, PositionedThing> getActionForObject(PlayerCharacterController pcc, GameObject actionPerformer, GameMap map, PositionedThing clickedGO) {
			PositionedThing actionTarget = pcc.getTrapLocationCurrentlyHovered();
			if (actionTarget == null) {
				actionTarget = clickedGO;
			}
			return super.getActionForObject(pcc, actionPerformer, map, actionTarget);
		}

		@Override
		public Class<? extends Action> getActionClass() {
			return DisarmTrapAction.class;
		}

		@Override
		public boolean isApplicableFor(PositionedThing clickedGO) {
			if (clickedGO instanceof Trapable) {
				Trap trap = ((Trapable)clickedGO).getTrap();
				return trap != null && trap.isDetected() && !trap.isDisarmed();
			}
			return false;
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
	public Pair<Class<? extends Action>, PositionedThing> getActionForObject(PlayerCharacterController pcc,
			GameObject actionPerformer, GameMap map, PositionedThing clickedGO) {
		if (actionPerformer != clickedGO && isApplicableFor(clickedGO) && actionPerformer.canPerformAction(getActionClass())) {
			pair.setLeft(getActionClass());
			pair.setRight(clickedGO);
			return pair;
		}
		return null;
	}

	public abstract Class<? extends Action> getActionClass();
	
	public abstract boolean isApplicableFor(PositionedThing clickedGO);
}
