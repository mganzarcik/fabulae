package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Disables AI on the supplied GameCharacter.
 * 
 * @author annun
 *
 */
public class DisableAIAction extends BasicAction {
	
	private boolean disabled;
	private GameCharacter character;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("DisableAIAction can only be used on GameCharacter!");
		}
		disabled = false;
		character = (GameCharacter) ac;
	}

	@Override
	public void update(float deltaTime) {
		character.disableAI();
		disabled = true;
	}

	@Override
	public boolean isFinished() {
		return disabled;
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void reset() {	
		disabled = false;
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
	}

}
