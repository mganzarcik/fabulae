package mg.fishchicken.graphics;

import mg.fishchicken.core.PositionedThing;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface TextDrawer extends PositionedThing {

	public void drawText(SpriteBatch spriteBatch, float deltaTime);
	public boolean shouldDrawText();
}
