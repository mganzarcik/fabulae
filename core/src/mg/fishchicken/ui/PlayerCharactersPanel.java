package mg.fishchicken.ui;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.ui.portraits.PlayerCharacterPortrait;
import mg.fishchicken.ui.portraits.PlayerCharacterPortrait.PlayerCharacterPortraitStyle;
import mg.fishchicken.ui.tooltips.SimpleTooltip;
import mg.fishchicken.ui.tooltips.SimpleTooltip.SimpleTooltipStyle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;

public class PlayerCharactersPanel extends Table {
	
	private GameState gameState;
	private PlayerCharacterGroup group;
	private PlayerCharactersPanelStyle style;
	private Array<PlayerCharacterPortrait> pcPanels;
	private Vector2 tempVector;
	
	public PlayerCharactersPanel(GameState gameState, PlayerCharacterGroup group, PlayerCharactersPanelStyle style) {
		this.style = style;
		this.gameState = gameState;
		this.group = group;
		this.pcPanels = new Array<PlayerCharacterPortrait>();
		this.tempVector = new Vector2();
		addListener(new PortraitDragListener());
		loadPCPanels(gameState);
	}
	
	public void loadPCPanels(GameState gameState) {
		this.clearChildren();
		pcPanels.clear();
		
		for (GameCharacter pc : group.getPlayerCharacters()) {
			PlayerCharacterPortrait pcp = new PlayerCharacterPortrait(pc, gameState, style.portraitStyle);
			pcPanels.add(pcp);
			this.add(pcp).pad(style.portraitGap/2);
			if (style.inRows) {
				this.row();
			}
		}
		pack();
		setX(style.x);
		setY(Gdx.graphics.getHeight() - getHeight() - style.y);	
	}
	
	/**
	 * Returns true if the supplied actor is part of of PlayerCharacterPortrait,
	 * or the portrait itself.
	 * 
	 * @param actor
	 * @return
	 */
	public boolean isPCPortrait(Actor actor) {
		return getPCPortrait(actor) != null;
	}
	
	/**
	 * Returns the PCPortrait that the supplied actor is a part of or 
	 * null if it is not part of any PCPortrait.
	 * 
	 * @param actor
	 * @return
	 */
	private PlayerCharacterPortrait getPCPortrait(Actor actor) {
		if (actor == null) {
			return null;
		}
		for (PlayerCharacterPortrait pcp : pcPanels) {
			if (pcp.isAscendantOf(actor)) {
				return pcp;
			}
		}
		return null;
	}
	
	public void updatePosition(int screenWidth, int screenHeight) {
		setY(screenHeight - getHeight() - style.y);
		setX(style.x);
	}

	public static class PlayerCharactersPanelStyle {
		private float x, y, portraitGap;
		private boolean inRows;
		private PlayerCharacterPortraitStyle portraitStyle;
		private SimpleTooltipStyle swapTooltipStyle;
	}
	
	private class PortraitDragListener extends DragListener {
		private PlayerCharacterPortrait draggedPCPortrait = null;
		private SimpleTooltip tooltip = new SimpleTooltip(style.swapTooltipStyle);
		
		@Override
		public void drag(InputEvent event, float x, float y, int pointer) {
			if (draggedPCPortrait != null) {
				if (style.inRows) {
					setPositionWithinStage(draggedPCPortrait, draggedPCPortrait.getX(), y);
				} else {
					setPositionWithinStage(draggedPCPortrait, x, draggedPCPortrait.getY());
				}
				
				PlayerCharacterPortrait hoveredPCPortrait = getPCPortrait(hit(x, y, true));
				if (hoveredPCPortrait != null) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE,
							"swapCharactersTooltip", draggedPCPortrait.getCharacter()
									.getName(), hoveredPCPortrait.getCharacter()
									.getName()));
					tempVector.set(x, y);
					localToStageCoordinates(tempVector);
					UIManager.setHoveredItemsPosition(tempVector.x, tempVector.y);
					UIManager.setToolTip(tooltip);
				}
			}
		}
		
		@Override
		public void dragStart(InputEvent event, float x, float y, int pointer) {
			draggedPCPortrait = getPCPortrait(hit(x, y, true));
			if (draggedPCPortrait != null) {
				draggedPCPortrait.setTouchable(Touchable.disabled);
				Cell<?> cell = getCell(draggedPCPortrait);
				cell.setActor(null);
				cell.width(draggedPCPortrait.getWidth());
				cell.height(draggedPCPortrait.getHeight());
				addActor(draggedPCPortrait);
				setPositionWithinStage(draggedPCPortrait, x, y);
			}
		}
		
		@Override
		public void dragStop(InputEvent event, float x, float y, int pointer) {
			if (draggedPCPortrait != null) {
				PlayerCharacterPortrait portraitToSwap = getPCPortrait(hit(x,
						y, true));
				if (portraitToSwap != null) {
					Array<GameCharacter> pcs = group.getPlayerCharacters();
					pcs.swap(pcs.indexOf(portraitToSwap.getCharacter(), false),
							pcs.indexOf(draggedPCPortrait.getCharacter(), false));
				}
				loadPCPanels(gameState);
			}
		}
		
		private void setPositionWithinStage(Actor actor, float x, float y) {
			Stage stage = getStage();
			if (stage != null) {
				tempVector.set(x, y);
				localToStageCoordinates(tempVector);
				float stageWidth = stage.getWidth();
				float stageHeight = stage.getHeight();
				float actorWidth = actor.getWidth();
				float actorHeight = actor.getHeight();
				
				if (tempVector.x < 0) {
					tempVector.x = 0;
				}
				if (tempVector.y < 0) {
					tempVector.y = 0;
				}
				if (tempVector.x + actorWidth > stageWidth) {
					tempVector.x = stageWidth - actorWidth;
				}
				if (tempVector.y + actorHeight > stageHeight) {
					tempVector.y = stageHeight - actorHeight;
				}
				stageToLocalCoordinates(tempVector);
				x = tempVector.x;
				y = tempVector.y;
			}
			actor.setPosition(x, y);
		}
	}
}
