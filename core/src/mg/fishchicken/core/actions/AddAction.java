package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.GameObject;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Adds the supplied mg.fishchicken.gamelogic.actions.Action to the supplied GameObject.
 * <br /><br />
 * Examples:
 * 
 * <pre>
 * &lt;addAction targetObject="__npcAtDialogue"&gt;
 * 	&lt;moveToAction x = "3" y = "5" /&gt;
 * &lt;/addAction&gt;
 * </pre>
 *   
 * @author annun
 * @see mg.fishchicken.gamelogic.actions.Action
 *
 */
public class AddAction extends Action {
	
	private Element actionElement;
	
	@Override
	protected void run(Object object, Binding parameters) {
		if (object instanceof GameObject) {
			GameObject go = (GameObject)object;
			go.addAction(mg.fishchicken.gamelogic.actions.Action.readFromXML(actionElement, go), false);
		}
	}

	@Override
	public void validateAndLoadFromXML(Element actionElement) {
		if (actionElement.getChildCount() < 1) {
			throw new GdxRuntimeException("AddAction must have at least one child element!");
		}
		this.actionElement = actionElement.getChild(0);
	}

}
