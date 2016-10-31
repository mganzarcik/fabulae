package mg.fishchicken.core.saveload;

import java.io.IOException;

import mg.fishchicken.core.ThingWithId;

import com.badlogic.gdx.files.FileHandle;

/**
 * All implementers of this Interface can be loaded from a XML file.
 * 
 * This does not mean they can also be saved into XML file.
 * 
 * Implementers of this usually represent "master data", 
 * i.e. data that defines the game, but is itself not changeable during
 * the game.
 * 
 * @author Annun
 *
 */
public interface XMLLoadable {

	/**
	 * Loads the this object from the supplied
	 * XML file.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void loadFromXML(FileHandle file) throws IOException;
	
	public void loadFromXMLNoInit(FileHandle file) throws IOException;
}
