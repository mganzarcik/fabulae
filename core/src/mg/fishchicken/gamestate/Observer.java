package mg.fishchicken.gamestate;

public interface Observer<T extends ObservableState<T, CHANGES>, CHANGES> {

	public void hasChanged(T stateObject, CHANGES changes);
}
