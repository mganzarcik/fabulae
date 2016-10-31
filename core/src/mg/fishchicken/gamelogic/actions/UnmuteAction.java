package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Unmutes the Chatter on the supplied GameCharacter.
 * 
 * @author annun
 *
 */
public class UnmuteAction extends BasicAction {
	
	private boolean unmuted;
	private GameCharacter character;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("UnmuteAction can only be used on GameCharacter!");
		}
		unmuted = false;
		character = (GameCharacter) ac;
	}

	@Override
	public void update(float deltaTime) {
		character.unmuteChattter();
		unmuted = true;
	}

	@Override
	public boolean isFinished() {
		return unmuted;
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void reset() {	
		unmuted = false;
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
	}

}
