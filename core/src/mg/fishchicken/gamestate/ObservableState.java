package mg.fishchicken.gamestate;

import java.io.IOException;

import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Object representing some small state. It is observable by observers
 * and should report all state changes to them. It is also saveable and loadable
 * to its own XML element.
 *
 * @param <MYSELF>
 * @param <CHANGES>
 */
public abstract class ObservableState<MYSELF extends ObservableState<MYSELF, CHANGES>, CHANGES> implements XMLSaveable {

	private Array<Observer<MYSELF, CHANGES>> observers = new Array<Observer<MYSELF, CHANGES>>();
	
	/**
	 * Add a new observer to this state. Any state changes will be reported to it.
	 * @param observer
	 */
	public void addObserver(Observer<MYSELF, CHANGES> observer) {
		if (!observers.contains(observer, false)) {
			observers.add(observer);
		}
	}
	
	/**
	 * Remove an observer from this state.
	 * @param observer
	 */
	public void removeObserver(Observer<MYSELF, CHANGES> observer) {
		observers.removeValue(observer, false);
	}
	
	@SuppressWarnings("unchecked")
	protected void changed(CHANGES changes) {
		for (int i = 0; i < observers.size; ++i) {
			observers.get(i).hasChanged((MYSELF)this, changes);
		}
	}
	
	/**
	 * Returns the name of the xml element this is save to and loaded from.
	 * @return
	 */
	public String getXMLElementName() {
		return StringUtil.lowercaseFirstLetter(this.getClass().getSimpleName());
	}
	
	protected void readXMLContents(Element element) throws IOException {	
	}
	
	protected void writeXMLContents(XmlWriter writer) throws IOException {	
	}
	
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		Element myElement = root.getChildByName(getXMLElementName());
		if (myElement != null) {
			XMLUtil.readPrimitiveMembers(this, myElement);
			readXMLContents(myElement);
		}
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(getXMLElementName());
		XMLUtil.writePrimitives(this, writer, true);
		writeXMLContents(writer);
		writer.pop();
	}
}
