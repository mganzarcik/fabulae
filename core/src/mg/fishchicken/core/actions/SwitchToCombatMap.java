package mg.fishchicken.core.actions;

import groovy.lang.Binding;

import java.io.IOException;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.gamelogic.characters.groups.CharacterGroup;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroupGameObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Transitions to combat map with the supplied enemy group. <br />
 * <br />
 * Example:
 * 
 * <pre>
 * 	&lt;switchToCombatMap enemyGroup="groupId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class SwitchToCombatMap extends Action {

	public static final String XML_ENEMY_GROUP = "enemyGroup";

	@Override
	protected void run(Object object, Binding parameters) {
		if (!gameState.getCurrentMap().isWorldMap()) {
			throw new GdxRuntimeException("SwitchToCombatMap can only be used on world maps.");
		}

		PlayerCharacterGroupGameObject playerGroup = GameState.getPlayerCharacterGroup().getGroupGameObject();

		try {
			CharacterGroup encounter = new CharacterGroup(Gdx.files.internal(Configuration.getFolderGroups()
					+ getParameter(XML_ENEMY_GROUP) + ".xml"));
			encounter.setShouldBeSaved(false);
			playerGroup.startRandomEncounter(encounter, true);
		} catch (IOException e) {
			throw new GdxRuntimeException("Error loading enemy group in SwitchToCombatMap.", e);
		}
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XML_ENEMY_GROUP, null) == null) {
			throw new GdxRuntimeException(XML_ENEMY_GROUP
					+ " must be set for action SwitchToCombatMap in element: \n\n" + conditionElement);
		}
	}
}
