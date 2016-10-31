package mg.fishchicken.ui.formation;


import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class FormationEditorPanel extends BorderedWindow implements EventListener {

	private PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
	private FormationEditorButton draggedItem;
	private FormationEditorButton draggedFrom;
	private ObjectMap<Tile, FormationEditorButton> table;
	
	public FormationEditorPanel(FormationEditorPanelStyle style) {
		super(style);
		draggedItem = new FormationEditorButton(style.emptySlotStyle, style.occupiedSlotStyle);
		draggedFrom = null;
		table = new ObjectMap<Tile, FormationEditorButton>();
	}
	
	public FormationEditorPanelStyle getStyle() {
		return (FormationEditorPanelStyle) super.getStyle();
	}
	
	public void refresh() {
		clearChildren();
		table.clear();
		final FormationEditorPanelStyle style = getStyle();
		draggedFrom = null;
		draggedItem.setCharacterIndex(null);
		
		setTitle(Strings.getString(UIManager.STRING_TABLE, "formationEditorHeading"));
		
		Table formationTable = new Table();
		ScrollPane srollPane = new ScrollPane(formationTable, style.scrollPaneStyle);
		srollPane.setFadeScrollBars(false);
		srollPane.setOverscroll(false, false);
		srollPane.addListener(new EventListener() {

			@Override
			public boolean handle(Event event) {
				if (event instanceof InputEvent) {
					InputEvent inputEvent = (InputEvent) event;
					if (Type.mouseMoved.equals(inputEvent.getType())) {
						draggedItem.setPosition(inputEvent.getStageX()
								- style.slotWidth / 2f, inputEvent.getStageY()
								+ style.slotHeight / 2f);
					}
				}
				return false;
			}
			
		});
		
		ObjectMap<Integer, Tile> groupFormation = pcg.formation().getFormation();
		
		ObjectMap<Integer, Tile> formation = new ObjectMap<Integer, Tile>();
		
		for (Entry<Integer, Tile> entry : groupFormation.entries()) {
			formation.put(entry.key, new Tile(entry.value));
		}
		
		int minX = 0, maxX = 0, minY = 0, maxY = 0;
		
		for (Tile position : formation.values()) {
			if (minX > position.getX()) {
				minX = position.getX();
			}
			if (maxX < position.getX()) {
				maxX = position.getX();
			}
			
			if (minY > position.getY()) {
				minY = position.getY();
			}
			if (maxY < position.getY()) {
				maxY = position.getY();
			}
		}
				
		int panelCenterX = style.rows / 2;
		int panelCenterY = style.cols / 2;
		
		int formationCenterX = (maxX - minX) / 2 + minX;
		int formationCenterY = (maxY - minY) / 2 + minY;
		
		int distanceX = Math.abs(formationCenterX - panelCenterX);
		int distanceY = Math.abs(formationCenterY - panelCenterY);
		
		
		for (int y = style.rows-1; y >= 0; --y) {
			for (int x = 0; x < style.cols; ++x) {
				FormationEditorButton button = new FormationEditorButton(style.emptySlotStyle, style.occupiedSlotStyle);
				button.addListener(this);
				for (Entry<Integer, Tile> entry : formation.entries()) {
					if (x == entry.value.getX() + distanceX && y == entry.value.getY() + distanceY) {
						button.setCharacterIndex(entry.key);
						break;
					}
				}
				table.put(new Tile(x, y), button);
				formationTable.add(button).prefWidth(style.slotWidth).prefHeight(style.slotHeight).space(style.slotSpacing);
			}
			formationTable.row();
		}
		add(srollPane)
				.prefHeight(style.rows * (style.slotHeight + style.slotSpacing))
				.prefWidth(style.cols * (style.slotWidth + style.slotSpacing));
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		draggedItem.setVisible(draggedItem.getCharacterIndex() != null);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (draggedItem.isVisible()) {
			draggedItem.draw(batch, parentAlpha);
		}
	}
	
	@Override
	public boolean handle(Event event) {
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.touchDown.equals(inputEvent.getType())) {
				clicked(inputEvent, inputEvent.getTarget());
			}
		}
		return false;
	}
	
	public boolean clicked (InputEvent inputEvent, Actor actor) {
		if (actor instanceof Label) {
			actor = actor.getParent();
		}
		if (actor instanceof FormationEditorButton) {
			FormationEditorButton clickedSlot = (FormationEditorButton)actor;
			Integer slotItem = clickedSlot.getCharacterIndex();
			Integer draggedItem = this.draggedItem.getCharacterIndex();		
			
			if (draggedItem != null && slotItem == null) {
				putItemDown(clickedSlot);
				return true;
			} else if (slotItem != null && draggedItem == null) {
				pickItemUp(clickedSlot);
				return true;
			} else if (slotItem != null && draggedItem != null) {
				swapItems(clickedSlot);
				return true;
			}
		}
		return false;
	}
	
	protected boolean putItemDown(FormationEditorButton clickedSlot) {
		clickedSlot.setCharacterIndex(draggedItem.getCharacterIndex());
		draggedItem.setCharacterIndex(null);
		draggedFrom = null;
		return true;
	}
	
	protected void swapItems(FormationEditorButton clickedSlot) {
		Integer clickedIndex = clickedSlot.getCharacterIndex();
		clickedSlot.setCharacterIndex(draggedItem.getCharacterIndex());
		draggedItem.setCharacterIndex(clickedIndex);
	}
	
	protected void pickItemUp(FormationEditorButton clickedSlot) {
		draggedFrom = clickedSlot;
		draggedItem.setCharacterIndex(clickedSlot.getCharacterIndex());
		clickedSlot.setCharacterIndex(null);
	}
	
	@Override
	public boolean remove() {
		boolean returnValue = super.remove();
		if (table.size < 1 || !returnValue) {
			return returnValue;
		}
		if (draggedFrom != null) {
			draggedFrom.setCharacterIndex(draggedFrom.getCharacterIndex());
			draggedFrom.setCharacterIndex(null);
			draggedFrom = null;
		}
		
		Entry<Tile, FormationEditorButton> leader = null;
		
		for (Entry<Tile, FormationEditorButton> entry : table.entries()) {
			Integer index = entry.value.getCharacterIndex();
			if (index != null &&  0 == index) {
				leader = entry;
				break;
			}
		}
		
		ObjectMap<Integer, Tile> newFormation = new ObjectMap<Integer, Tile>();
		
		for (Entry<Tile, FormationEditorButton> entry : table.entries()) {
			if (entry.value.getCharacterIndex() != null) {
				Tile coords = new Tile(
						entry.key.getX() - leader.key.getX(), 
						entry.key.getY() - leader.key.getY());
				newFormation.put(entry.value.getCharacterIndex(), coords);
			}
		}
		
		pcg.formation().setFormation(newFormation);
		return returnValue;
	}
	
	public static class FormationEditorPanelStyle extends BorderedWindowStyle {
		private int rows, cols, slotWidth, slotHeight, slotSpacing;
		private TextButtonWithSoundStyle occupiedSlotStyle, emptySlotStyle;
		private ScrollPaneStyle scrollPaneStyle;
	}
}
