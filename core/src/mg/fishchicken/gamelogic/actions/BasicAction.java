package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlWriter;
import com.badlogic.gdx.utils.XmlReader.Element;

import mg.fishchicken.core.util.XMLUtil;

/**
 * Basic abstract implementation of an Action.
 * 
 * All subclasses will automatically use a dedicated slot for their type,
 * meaning it will be possible to have only one instance of the subclass on a
 * given GO.
 * 
 * In order to change this behavior, children need to override the
 * getActionSlot() method.
 * 
 * @author Annun
 * 
 */
public abstract class BasicAction extends Action {
	private boolean isPaused = false;
	
	public void pause() {
		isPaused = true;
	}
	public void resume() {
		isPaused = false;
	}
	public boolean isPaused() {
		return isPaused;
	}
	
	@Override
	public boolean isVerbAction() {
		return Action.VERB_ACTIONS.contains(this.getClass(), false);
	}
	
	public int getActionSlot() {
		return this.getClass().hashCode();
	}
	
	public void onRemove(ActionsContainer ac) {
		
	}
	
	@Override
	public void loadFromXML(Element actionElement) {
		XMLUtil.readPrimitiveMembers(this, actionElement);
		readAndValidateParamateresFromXML(actionElement);
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(getClass().getSimpleName());
		XMLUtil.writePrimitives(this, writer);
		writeParametersToXML(writer);
		writer.pop();
		
	}
	
	public abstract void readAndValidateParamateresFromXML(Element actionElement);
	
	public abstract void writeParametersToXML(XmlWriter writer) throws IOException ;
	
}
