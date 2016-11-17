package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Enables AI on the supplied GameCharacter.
 * 
 * @author annun
 *
 */
public class EnableAIAction extends BasicAction {
	
	private boolean enabled;
	private GameCharacter character;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof GameCharacter)) {
			throw new GdxRuntimeException("EnableAIAction can only be used on GameCharacter!");
		}
		enabled = false;
		character = (GameCharacter) ac;
	}

	@Override
	public void update(float deltaTime) {
		character.brain().enable();
		enabled = true;
	}

	@Override
	public boolean isFinished() {
		return enabled;
	}

	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void reset() {	
		enabled = false;
	}
	
	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
	}

}
