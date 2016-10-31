package mg.fishchicken.graphics.renderers;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.dialogue.Chatter;
import mg.fishchicken.gamelogic.dialogue.Chatter.ChatterType;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * Renders floating text above game objects.
 * 
 * Can work together with chatter, in which case it takes care of randomly
 * selecting one and rendering it periodically. It can also be used to display
 * arbitrary strings using the {@link #setText(String)} method.
 * 
 * @author Annun
 *
 */
public class FloatingTextRenderer {

	private Chatter chatter;
	private float stateTime;
	private String currentText;
	private float fadeTime;
	private GlyphLayout glyphLayout;
	private Color tempColor;
	private Color color;
	private boolean muteChatter;

	public FloatingTextRenderer() {
		currentText = null;
		fadeTime = 0;
		glyphLayout = new GlyphLayout();
		tempColor = new Color();
		color = new Color(Color.WHITE);
		chatter = null;
		muteChatter = false;
	}

	public FloatingTextRenderer(Chatter chatter) {
		this();
		this.chatter = chatter;
	}
	
	/**
	 * Sets the chatter of this renderer to the supplied one.
	 * 
	 * @param chatter
	 */
	public void setChatter(Chatter chatter) {
		this.chatter = chatter;
	}

	/**
	 * Sets the color the renderer should use when rendering the text.
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Makes the renderer display the supplied text. Once the text fades away,
	 * the renderer will resume normal operation and display strings from its
	 * Chatter, if it has any (unless muted).
	 * 
	 * @param text
	 */
	public void setText(String text) {
		stateTime = 0;
		setDrawnText(text);
	}

	private void setDrawnText(String text) {
		currentText = Strings.getString(text);
	}

	private void determineTextToDisplay(float delta, ObjectSet<GameLocation> locations) {
		Array<String> texts = chatter != null ? chatter.getTexts(ChatterType.GENERAL, locations) : null;
		if (texts == null || texts.size < 1) {
			currentText = null;
			return;
		}
		stateTime += delta;
		if (stateTime < chatter.getCheckFrequency(ChatterType.GENERAL, locations)) {
			return;
		}
		stateTime = 0;

		if (MathUtils.random(100) > chatter.getChanceToSay(ChatterType.GENERAL, locations)) {
			return;
		}

		setDrawnText(texts.random());
	}

	/**
	 * Mutes or unmutes the chatter in the renderer, if it has any. Muted
	 * chatter will prevent the renderer from automatically rendering any
	 * chatter.
	 * 
	 * If a text is set to the renderer via {@link #setText(String)}, it will
	 * still be draw.
	 */
	public void setMuteChatter(boolean value) {
		muteChatter = value;
	}

	/**
	 * Draws a floating text above the supplied GO.
	 * 
	 * This is a smart function - it determines if a chatter should be drawn,
	 * and if it should, then it makes sure it is is displayed for the correct
	 * duration and then faded out nicely.
	 * 
	 * This should be called every frame.
	 * 
	 * @param batch
	 *            - batch used to render the text.
	 * @param deltaTime
	 *            - the time passed since the last frame. If zero is supplied
	 *            here, no fading effect will occur and the text will never
	 *            disappear by itself
	 * @param go
	 *            - game object above which to display the text
	 * @param objectWidth
	 *            - width of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 * @param objectHeight
	 *            - height of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 */
	public void render(Batch batch, float deltaTime, GameObject go, int objectWidth, int objectHeight) {
		render(batch, deltaTime, go.getMap(), go.position(), null, objectWidth, objectHeight);
	}

	/**
	 * Draws a floating text above the supplied GO.
	 * 
	 * This is a smart function - it determines if a chatter should be drawn,
	 * and if it should, then it makes sure it is is displayed for the correct
	 * duration and then faded out nicely.
	 * 
	 * This should be called every frame.
	 * 
	 * @param batch
	 *            - batch used to render the text.
	 * @param deltaTime
	 *            - the time passed since the last frame. If zero is supplied
	 *            here, no fading effect will occur and the text will never
	 *            disappear by itself
	 * @param character
	 *            - character above which to display the text
	 * @param objectWidth
	 *            - width of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 * @param objectHeight
	 *            - height of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 */
	public void render(Batch batch, float deltaTime, AbstractGameCharacter character, int objectWidth, int objectHeight) {
		render(batch, deltaTime, character.getMap(), character.position(), character.getCurrentLocations(),
				objectWidth, objectHeight);
	}

	/**
	 * Draws a floating text above the supplied position on the supplied map.
	 * 
	 * This is a smart function - it determines if a chatter should be drawn,
	 * and if it should, then it makes sure it is is displayed for the correct
	 * duration and then faded out nicely.
	 * 
	 * This should be called every frame.
	 * 
	 * @param batch
	 *            - batch used to render the text.
	 * @param deltaTime
	 *            - the time passed since the last frame. If zero is supplied
	 *            here, no fading effect will occur and the text will never
	 *            disappear by itself
	 * @param position
	 *            - position of the game object above which the chatter should
	 *            be displayed
	 * @param objectWidth
	 *            - width of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 * @param objectHeight
	 *            - height of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 */
	public void render(Batch batch, float deltaTime, GameMap map, Position position, int objectWidth, int objectHeight) {
		render(batch, deltaTime, map, position, null, objectWidth, objectHeight);
	}

	/**
	 * Draws a floating text above the supplied position on the supplied map.
	 * 
	 * This is a smart function - it determines if a chatter should be drawn,
	 * and if it should, then it makes sure it is is displayed for the correct
	 * duration and then faded out nicely.
	 * 
	 * This should be called every frame.
	 * 
	 * @param batch
	 *            - batch used to render the text.
	 * @param deltaTime
	 *            - the time passed since the last frame. If zero is supplied
	 *            here, no fading effect will occur and the text will never
	 *            disappear by itself
	 * @param position
	 *            - position of the game object above which the chatter should
	 *            be displayed
	 * @param objectWidth
	 *            - width of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 * @param objectHeight
	 *            - height of the supplied game object in pixels. This is the
	 *            actual width, not the width of the texture
	 */
	private void render(Batch batch, float deltaTime, GameMap map, Position position,
			ObjectSet<GameLocation> locations, int objectWidth, int objectHeight) {
		if (currentText == null && chatter != null && !muteChatter) {
			determineTextToDisplay(deltaTime, locations);
		}

		if (currentText == null) {
			return;
		}

		fadeTime += deltaTime;

		float alpha = 1f;
		if (fadeTime > Configuration.getChatterFadeTime()) {
			alpha = (0.3f - (fadeTime - Configuration.getChatterFadeTime())) / 0.3f;
		}
		if (alpha <= 0) {
			currentText = null;
			fadeTime = 0;
			return;
		} else if (alpha > 1) {
			alpha = 1;
		}

		BitmapFont font = UIManager.getFloatingTextFont();

		tempColor.set(color.r, color.g, color.b, alpha);

		glyphLayout.setText(font, currentText, tempColor, Configuration.getChatterWidth(), Align.left, true);

		Vector3 projectedCoordinates = MathUtil.getVector3().set(position.getX(), position.getY(), 0);
		map.projectFromTiles(projectedCoordinates);
		map.getCamera().project(projectedCoordinates);

		float textX = projectedCoordinates.x - (glyphLayout.width / 2) + (objectWidth / 2f);
		float textY = projectedCoordinates.y + objectHeight + glyphLayout.height;
		if (!map.isIsometric()) {
			textY += map.getTileSizeY() / 2;
		} else {
			textX += map.getTileSizeX() / 2;
		}

		font.draw(batch, glyphLayout, textX, textY);

		MathUtil.freeVector3(projectedCoordinates);
	}
}
