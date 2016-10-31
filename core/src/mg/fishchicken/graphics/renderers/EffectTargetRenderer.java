package mg.fishchicken.graphics.renderers;

import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.gamelogic.combat.CombatPath;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.locations.GameMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;

public class EffectTargetRenderer extends CombatPathRenderer {

	public EffectTargetRenderer(PlayerCharacterController controller) {
		super(controller);
	}
	
	@Override
	protected CombatPath getPathToRender() {
		return controller.getEffectTarget();
	}

	@Override
	protected void drawStepCost(GameMap map, Batch spriteBatch, int cost,
			Vector3 coordinates, float transparency) {
		if (cost < 1) {
			return;
		}
		super.drawStepCost(map, spriteBatch, cost, coordinates, transparency);
	}
	
	@Override
	protected Color getColorForCost(GameMap map, int cost) {
		if (controller.getWaitingForClickConfirmation()) {
			return CONFIRMED;
		}
		if (((TargetType)getPathToRender()).isValidTarget()) {
			return VALID;
		}
		return INVALID;
	}
	
}
