package mg.fishchicken.gamelogic.characters;

import mg.fishchicken.core.ColoredThing;
import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.BasicActionsContainer;
import mg.fishchicken.gamestate.Position;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class CharacterCircle extends BasicActionsContainer implements AssetContainer, ColoredThing {

	private TextureRegion texture; 
	private AbstractGameCharacter character;
	private Position position;
	private Color color;
	
	public CharacterCircle(AbstractGameCharacter character) {
		super();
		setCharacter(character);
	}
	
	/**
	 * Sets the position of this character circle to the supplied position.
	 * This will detach the circle from its character and from then on, 
	 * its position must be manipulated externally.
	 * 
	 * @param position
	 */
	public void setPosition(Position position) {
		this.position = position;
	}
	
	/**
	 * Sets the character of this circle to the supplied value. This will attach
	 * the circle to the character.
	 * 
	 * @param character
	 */
	public void setCharacter(AbstractGameCharacter character) {
		this.character = character;
		this.position = character.position();
		this.color = new Color(character.getColor());
	}
	
	/**
	 * Sets the color of this Character Circle. Modifying the supplied color
	 * after it is set will have no effect. Use getColor to get a reference
	 * to the color that can be directly modified.
	 * 
	 * @param color
	 */
	public void setColor(final Color color) {
		this.color =  new Color(color);
	}
	
	@Override
	public Color getColor() {
		return color;
	}
	
	public void draw(Batch spriteBatch, float deltaTime) {
		if (character == null || position == null || color == null) {
			return;
		}
		for (int i = actions.size-1; i >=0 ; --i) {
			Action a = actions.get(i);
			if (!a.isPaused()) {
				a.update(deltaTime);
				if (a.isFinished()) {
					removeAction(a);
				}
			}
		}
		
		Color originalColor = spriteBatch.getColor();
		spriteBatch.setColor(color);
		
		Vector2 projectedCoordinates = MathUtil.getVector2();
		character.getMap().projectFromTiles(projectedCoordinates.set(position.getX(), position.getY()));
		
		spriteBatch.draw(getTexture(),
				projectedCoordinates.x+(character.getCircleOffsetX()*character.getScaleX()), projectedCoordinates.y+(character.getCircleOffsetY()*character.getScaleY()), getTexture().getRegionWidth()
						* character.getScaleX(), getTexture().getRegionHeight() * character.getScaleY());
		MathUtil.freeVector2(projectedCoordinates);
		spriteBatch.setColor(originalColor);
	}
	
	public AbstractGameCharacter getCharacter() {
		return character;
	}

	public TextureRegion getTexture() {
		if (texture == null) {
			this.texture =  Assets.getTextureRegion(Configuration.getFileCharacterCircleSprite());
		}
		return texture;
	}
	
	public void setTexture(TextureRegion texture) {
		this.texture = texture;
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
		assetStore.put(Configuration.getFileCharacterCircleSprite(), Texture.class);
	}

	@Override
	public void clearAssetReferences() {
		texture = null;
	}

}
