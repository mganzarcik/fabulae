package mg.fishchicken.gamelogic.actions;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A simple ActionsContainer that also implements the 
 * Iterable interface.
 * 
 * Can be used in tandem with 
 * {@link mg.fishchicken.core.util.XMLUtil#readActions(ActionsContainer, com.badlogic.gdx.utils.XmlReader.Element)} 
 * to easily load Actions from a XML file.
 * 
 * @author ANNUN
 *
 */
public class BasicActionsContainer implements ActionsContainer, Iterable<Action>{

	protected Array<Action> actions;
	private ActionsContainer acToInit;
	
	/**
	 * Creates a basic actions container where any actions added to it will
	 * be initialized with the BasicACtionsContainer itself.
	 * 
	 */
	protected BasicActionsContainer() {
		actions = new Array<Action>();
		this.acToInit = this;
	}
	
	/**
	 * Creates a basic actions container where any actions added to it will
	 * be initialized with the supplied AC.
	 * 
	 * This is useful if you want to use this container as storage for
	 * actions of other containers.
	 * 
	 * @param acToInit
	 */
	public BasicActionsContainer(ActionsContainer acToInit) {
		actions = new Array<Action>();
		this.acToInit = acToInit;
	}
	
	public void clear() {
		actions.clear();
	}
	
	public void addAction(Action a) {
		actions.add(a);
	}
	
	@Override
	public <T extends Action> T addAction(Class<T> actionClass,
			Object... parameters) {
		T a;
		try {
			a = actionClass.newInstance();
			a.init(acToInit, parameters); 
			addAction(a);
		} catch (InstantiationException e) {
			throw new GdxRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}
		return a;
	}

	public int size() {
		return actions.size;
	}
	
	public Action getAction(int index) {
		return actions.get(index);
	}
	
	public Action removeFirst() {
		if (actions.size > 0) {
			return actions.removeIndex(0);
		} 
		return null;
	}
	
	@Override
	public void removeAction(Action a) {
		actions.removeValue(a, false);
	}

	@Override
	public Iterator<Action> iterator() {
		return actions.iterator();
	}

}
