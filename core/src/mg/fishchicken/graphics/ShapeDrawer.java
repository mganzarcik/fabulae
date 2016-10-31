package mg.fishchicken.graphics;

import mg.fishchicken.core.PositionedThing;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface ShapeDrawer extends PositionedThing {

	public void drawShape(ShapeRenderer renderer, float deltaTime);
	public boolean shouldDrawShape();
}
