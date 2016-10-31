package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the GameCharacter shout the supplied text. The action will finish
 * immediately once the text is displayed. <br />
 * <br />
 * Parameters:
 * <ol>
 * <li>text - String - the String to shout. Can be in a StringTable format
 * (stringTable.json#keyName)
 * </ol>
 * 
 * @author annun
 *
 */
public class ShoutAction extends BasicAction {

	private String text;
	private boolean shouted; 
	private GameCharacter character;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("ShoutAction only works on GameCharacter!");
		}
		character = (GameCharacter) ac;
		text = null;
		shouted = false;
		if (parameters.length > 0) {
			text = (String) parameters[0];
		}
	}

	@Override
	public void update(float deltaTime) {
		character.shout(text);
		shouted = true;
	}

	@Override
	public boolean isFinished() {
		return shouted;
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void reset() {
		shouted = false;
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		text = actionElement.getAttribute(XML_ATTRIBUTE_TEXT, null);
		if (text == null) {
			throw new GdxRuntimeException("ShoutAction must have attribute text specified! (Character +"+character+")");
		}
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_TEXT, text);
	}
}
