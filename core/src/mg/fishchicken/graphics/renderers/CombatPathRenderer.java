package mg.fishchicken.graphics.renderers;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.combat.CombatPath;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.pathfinding.Path.Step;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

public class CombatPathRenderer {
	
	public static final Color INVALID = new Color(1f, 0.2f, 0.2f, 1f);
	public static final Color VALID = new Color(0.2f, 1f, 0.2f, 1f);
	public static final Color CONFIRMED = Color.YELLOW;
	
	protected PlayerCharacterController controller;
	private GlyphLayout fontLayout;
	private Color tempColor;
	
	public CombatPathRenderer(PlayerCharacterController controller) {
		this.controller = controller;
		this.fontLayout = new GlyphLayout();
		this.tempColor = new Color();
	}
	
	protected CombatPath getPathToRender() {
		return controller.getCombatPath();
	}
	
	public void draw(GameMap map, Batch spriteBatch, float transparency) {
		draw(map, spriteBatch, CombatPath.ALL, transparency);
	}
	
	public void draw(GameMap map, Batch spriteBatch, int maxSteps, float transparency) {
		CombatPath path = getPathToRender();
		if (path == null || path.getLength() < 0) {
			return;
		}
		Vector3 projectedCoordinates = MathUtil.getVector3();
		Vector3 projectedCoordinatesText = MathUtil.getVector3();
		int currentCost = 0;
		for (int i = 0; i < path.getLength() && (maxSteps == CombatPath.ALL || i <= maxSteps); ++i) {
			Step step = path.getStep(i);
			float xOffset = map.isIsometric() ? 0.5f : 0;
			float yOffset = map.isIsometric() ? 0.5f : 0;
			
			map.projectFromTiles(projectedCoordinates.set(step.getX()+xOffset, step.getY()+yOffset, 0));
			map.projectFromTiles(projectedCoordinatesText.set(step.getX()+0.5f, step.getY()+0.5f, 0));
			map.getCamera().project(projectedCoordinates);
			map.getCamera().project(projectedCoordinatesText);
		
			int stepCost = step.getMoveCost()+step.getActionCost();
			currentCost += stepCost;
			Color color = getColorForCost(map, currentCost);
			spriteBatch.setColor(color.r, color.g, color.b, transparency);
			
			if (GameState.isCombatInProgress() && stepCost > 0) {
				drawStepCost(map, spriteBatch, currentCost, projectedCoordinatesText, transparency);
			}
			xOffset = map.isIsometric() ? map.getTileSizeX() : 0;
			yOffset = map.isIsometric() ? map.getTileSizeY()/2 : 0;
			final float tileWidth =  map.isIsometric() ? map.getTileSizeX() * 2 : map.getTileSizeX();
			final float tileHeight = map.getTileSizeY();
			
			spriteBatch.draw(map.getGridTexture(), projectedCoordinates.x-xOffset, projectedCoordinates.y-yOffset,
					tileWidth,
					tileHeight);
		}
		MathUtil.freeVector3(projectedCoordinates);
		MathUtil.freeVector3(projectedCoordinatesText);
	}
	
	protected void drawStepCost(GameMap map, Batch spriteBatch, int cost, Vector3 coordinates, float transparency) {
		BitmapFont font = UIManager.getCombatPathFont();
		String text = Integer.toString(cost);
		tempColor.set(spriteBatch.getColor().r, spriteBatch.getColor().g,spriteBatch.getColor().b,spriteBatch.getColor().a+0.3f);
		fontLayout.setText(font, text, tempColor, 0, Align.left, false);
		font.draw(spriteBatch,fontLayout,coordinates.x-fontLayout.width/2, coordinates.y+fontLayout.height/2);
	}
	
	protected Color getColorForCost(GameMap map, int cost) {
		if (!GameState.isCombatInProgress()) {
			return VALID;
		}
		GameCharacter leader = GameState.getPlayerCharacterGroup().getGroupLeader();
		if (leader == null || cost > leader.stats().getAPAct()) {
			return INVALID;
		} else if (!controller.getWaitingForClickConfirmation()) {
			return VALID;
		} 
		return CONFIRMED;
		
	}
}
