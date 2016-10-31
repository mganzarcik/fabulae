package mg.fishchicken.core.saveload;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlWriter;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * All implementers of this Interface can be saved to and loaded
 * from XML elements.
 * 
 * Most implementers of this interface represent the game state
 * and therefore will be saved into the savegame files.
 * 
 * @author Annun
 *
 */
public interface XMLSaveable {
	
	/**
	 * Writes the internal state of this object using the supplied
	 * XmlWriter.
	 * 
	 * @param writer
	 */
	public void writeToXML(XmlWriter writer) throws IOException;
	
	/**
	 * Loads the internal state of this object from the supplied
	 * XML element.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void loadFromXML(Element root) throws IOException;
}
