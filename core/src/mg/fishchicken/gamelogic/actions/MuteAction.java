package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Mutes the Chatter on the supplied GameCharacter.
 * 
 * @author annun
 *
 */
public class MuteAction extends BasicAction {
	
	private boolean muted;
	private GameCharacter character;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("MuteAction can only be used on GameCharacter!");
		}
		muted = false;
		character = (GameCharacter) ac;
	}

	@Override
	public void update(float deltaTime) {
		character.muteChatter();
		muted = true;
	}

	@Override
	public boolean isFinished() {
		return muted;
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void reset() {	
		muted = false;
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
	}

}
