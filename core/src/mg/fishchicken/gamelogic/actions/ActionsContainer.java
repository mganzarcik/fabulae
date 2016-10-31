package mg.fishchicken.gamelogic.actions;


public interface ActionsContainer {
	
	public <T extends Action> T addAction(Class<T> actionClass, Object... parameters);
	public void removeAction(Action a);
}
