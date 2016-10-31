package mg.fishchicken.ui.camp;

import java.io.IOException;
import java.util.Comparator;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.core.BasicCallback;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroupGameObject;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.survival.SurvivalFastForwardCallback;
import mg.fishchicken.gamelogic.survival.SurvivalManager;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.ModalEnabledSelectBox;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.dialog.OkCancelCallback;
import mg.fishchicken.ui.dialog.ProgressDialogSettings;
import mg.fishchicken.ui.selectbox.SelectBoxWithSound.SelectBoxWithSoundStyle;
import mg.fishchicken.ui.selectbox.SelectOption;
import mg.fishchicken.ui.tooltips.SimpleTooltip;
import mg.fishchicken.ui.tooltips.SimpleTooltip.SimpleTooltipStyle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class CampPanel extends BorderedWindow implements EventListener  {

	public static final String XML_CAMP_PANEL = "campPanel";
	public static final String XML_LAST_HUNTER = "lastHunter";
	public static final String XML_LAST_WATER_SCOUT = "lastWaterScout";
	
	private static final String HUNT = "hunt";
	private static final String WATER = "water";
	private static final String SLEEP = "sleep";
	private static final String PACKUP = "packup";
	private static final String GATHER = "gather";
	private static final String HUNTER_SELECT = "hunter_select";
	private static final String WATER_SELECT = "water_select";
	
	private GameCharacter lastHunter, lastWaterScout;
	private CampPanelStyle style;
	private SimpleTooltip tooltip;
	private Label food;
	private Label water;
	private AudioTrack<?> playingMusic = null;
	private Container<Table> pcSelects;
	private GameState gameState;
	ModalEnabledSelectBox<GameCharacter> hunterSelect, waterSelect;
	
	public CampPanel(GameState gameState, CampPanelStyle style) {
		super(Strings.getString(UIManager.STRING_TABLE, "campHeading"), style);
		this.gameState = gameState;
		this.style = style;
		tooltip = new SimpleTooltip(style.tooltipStyle);
		addListener(this);
		hunterSelect = new ModalEnabledSelectBox<GameCharacter>(style.hunterSelectStyle);
		hunterSelect.setName(HUNTER_SELECT);
		waterSelect = new ModalEnabledSelectBox<GameCharacter>(style.waterSelectStyle);
		waterSelect.setName(WATER_SELECT);
		food = new Label("", style.textStyle);
		food.setAlignment(Align.center);
		water = new Label("", style.textStyle);
		water.setAlignment(Align.center);
		pcSelects = new Container<Table>();
		pcSelects.setBackground(style.buttonBackground);
	}
	
	private void addButton(Table table, String text, TextButtonWithSoundStyle buttonStyle, String buttonName) {
		TextButtonWithSound button = new TextButtonWithSound(text, buttonStyle);
		button.setName(buttonName);
		table.add(button).padLeft(style.buttonPadding).padRight(style.buttonPadding);
	}

	public void rebuild() {
		clearChildren();
		
		add(new Image(style.images[MathUtils.random(0, style.images.length-1)], Scaling.none)).fill().padBottom(style.imageMarginBottom);
		row();
		Table foodWaterInfo = new Table();
		foodWaterInfo.add(food).fill().expandX();
		foodWaterInfo.add(water).fill().expandX();
		add(foodWaterInfo).fill();
		row();
		add(buildSelects()).fill();
		row();
		add(buildButtons()).fill().padTop(style.buttonsMarginTop);
		
		recalculateChangedItems();
		
		playMusic();
	}
	
	private void playMusic() {
		GameMap map = gameState.getCurrentMap();
		if (map != null) {
			Tile position = GameState.getPlayerCharacterGroup().getGroupGameObject().position().tile();
			playingMusic = map.playCampMusic(position.getX(), position.getY());
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!visible && playingMusic != null) {
			playingMusic.stop();
			playingMusic = null;
		}
	}
	
	
	private Actor buildSelects() {
		Table table = new Table();
		
		table.add(new Label(Strings.getString(UIManager.STRING_TABLE, "chooseHunter"), style.headingStyle)).padRight(10);
		Array<SelectOption<GameCharacter>> options = buildPCItems(Skill.HUNTING);
		hunterSelect.setItems(options);
		
		if (lastHunter != null) {
			for (SelectOption<GameCharacter> option : options) {
				if (lastHunter == option.value) {
					hunterSelect.setSelected(option);
					break;
				}
			}
		}
		
		table.add(hunterSelect).padRight(30);

		table.add(new Label(Strings.getString(UIManager.STRING_TABLE, "chooseWaterScout"), style.headingStyle)).padRight(10);
		options = buildPCItems(Skill.SCOUTING);
		waterSelect.setItems(options);
		if (lastWaterScout != null) {
			for (SelectOption<GameCharacter> option : options) {
				if (lastWaterScout == option.value) {
					waterSelect.setSelected(option);
					break;
				}
			}
		}
		table.add(waterSelect);
		
		pcSelects.setActor(table);
		
		return pcSelects;
	}
	
	private Array<SelectOption<GameCharacter>> buildPCItems(final Skill skill) {
		Array<SelectOption<GameCharacter>> items = new Array<SelectOption<GameCharacter>>();
		items.add(new SelectOption<GameCharacter>(Strings.getString(UIManager.STRING_TABLE, "noChoice"), null));
		
		Array<GameCharacter> viableCharacters = new Array<GameCharacter>();
		
		for (GameCharacter pc : GameState.getPlayerCharacterGroup().getPlayerCharacters()) {
			if (pc.isActive() && pc.stats().skills().getSkillRank(skill) > 0) {
				viableCharacters.add(pc);
			}
		}
		
		// sort based on desired skill from most skilled to least skilled
		viableCharacters.sort(new Comparator<GameCharacter>() {
			@Override
			public int compare(GameCharacter o1, GameCharacter o2) {
				return Integer.compare(o2.stats().skills().getSkillRank(skill), o1.stats().skills().getSkillRank(skill));
			}
		});
		
		for (GameCharacter pc : viableCharacters) {
			items.add(new SelectOption<GameCharacter>(pc.getName() + " ("+skill.toUIString()+": "+pc.stats().skills().getSkillRank(skill)+")", pc));
		}
		
		return items;
	}
	
	private void recalculateChangedItems() {
		PlayerCharacterGroup group = GameState.getPlayerCharacterGroup();
		food.setText(" "+Strings.getString(AbstractGameCharacter.STRING_TABLE,"food")+": "+MathUtil.toUIString(group.getFood()) +" / "+MathUtil.toUIString(group.getMaxFood()));
		water.setText(" "+Strings.getString(AbstractGameCharacter.STRING_TABLE,"water")+": "+MathUtil.toUIString(group.getWater()) +" / "+MathUtil.toUIString(group.getMaxWater()));
		
		buildSelects();
	}
	
	private Actor buildButtons() {
		Table buttonTable = new Table();
		addButton(buttonTable, Strings.getString(UIManager.STRING_TABLE, "campGatherButton"), style.gatherButtonStyle, GATHER);
		addButton(buttonTable, Strings.getString(UIManager.STRING_TABLE, "campSleepButton"), style.sleepButtonStyle, SLEEP);
		addButton(buttonTable, Strings.getString(UIManager.STRING_TABLE, "campPackupButton"), style.packUpButtonStyle, PACKUP);
		
		Container<Table> buttonsContainer = new Container<Table>();
		buttonsContainer.setBackground(style.buttonBackground);
		buttonsContainer.setActor(buttonTable);
		
		return buttonsContainer;
	}
	
	@Override
	public boolean handle(Event event) {
		if ((event instanceof ChangeEvent)) {
			return changed((ChangeEvent)event, event.getTarget().getName());
		}
		
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			String targetName = inputEvent.getTarget().getName();
			Type eventType = inputEvent.getType();
			
			if (Type.touchUp == eventType) {
				if (UIManager.isCharacterScreenOpen() && UIManager.getDraggedItem() == null) {
					UIManager.closeMutuallyExclusiveScreens();
				}
			}
			
			if (Type.enter == eventType) {
				boolean setTooltip = true;
				if (HUNT.equals(targetName)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "campHuntTooltip"));
				} else if (WATER.equals(targetName)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "campWaterTooltip"));
				} else if (SLEEP.equals(targetName)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "campSleepTooltip"));
				} else if (PACKUP.equals(targetName)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "campPackUpTooltip"));
				} else if (GATHER.equals(targetName)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "campGatherTooltip"));
				} else {
					setTooltip = false;
				}
				if (setTooltip) {
					UIManager.setToolTip(tooltip);
				}
			}
			if (Type.exit == eventType) {
				UIManager.hideToolTip();
			}
		}
		
		return true;
	}
	
	public boolean changed (ChangeEvent event, String actorName) {
		UIManager.hideToolTip();
		if (GATHER.equals(actorName)) {
			performGather();
		} else if (SLEEP.equals(actorName)) {
			final int sleepDuration = Configuration.getSleepDuration();
			if (sleepDuration <= 0) {
				return true;
			}
			GameState.getPlayerCharacterGroup().sleep(sleepDuration, new BasicCallback() {
				@Override
				public void callback() {
					recalculateChangedItems();
				}
			});
		} else if (PACKUP.equals(actorName)) {
			UIManager.displayConfirmation(
					Strings.getString(SurvivalManager.STRING_TABLE, "packCampQuestion"),
					Strings.getString(SurvivalManager.STRING_TABLE, "packCampConfirmation",Configuration.getPackingCampDuration()),
					new OkCancelCallback<Void>() {
						@Override
						public void onOk(Void nada) {
							ProgressDialogSettings settings = new ProgressDialogSettings(Strings.getString(SurvivalManager.STRING_TABLE, "PackingUp"), Configuration.getPackingCampDuration(), false);
							gameState.fastForwardTimeBy(settings, new SurvivalFastForwardCallback() {
								@Override
								public void onFinished() {
									UIManager.hideCampPanel();
								}
							}, false);
							
						}
					});
		} 
		return true;		
	}
	
	private void performGather() {
		final GameCharacter hunter = hunterSelect.getSelected().value;
		final GameCharacter waterScout = waterSelect.getSelected().value;
		
		int timeToGather = 0;
		
		if (hunter != null || waterScout != null) {
			++timeToGather;
			if (hunter == waterScout) {
				++timeToGather;
			}
		}
		
		final int finalTime = timeToGather;
		
		ProgressDialogSettings settings = new ProgressDialogSettings(Strings.getString(SurvivalManager.STRING_TABLE, "GatheringResources"), finalTime, false);
		
		gameState.fastForwardTimeBy(settings, new SurvivalFastForwardCallback() {
			@Override
			public void onFinished() {
				GameMap map = gameState.getCurrentMap();
				PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
				PlayerCharacterGroupGameObject playerGroupGO = GameState.getPlayerCharacterGroup().getGroupGameObject();;
				
				if (hunter != null) {
					lastHunter = hunter; 
					hunter.position().set(playerGroupGO.position());
					boolean huntResult = hunter.stats().rollSkillCheck(Skill.HUNTING, map); 
					if (huntResult) {
						int foundFood = hunter.stats().getAmountOfFoodFound();
						Log.logLocalized(SurvivalManager.STRING_TABLE, "foodFound", LogType.SURVIVAL, hunter.getName(), hunter.stats().getGender().getPronoun(), foundFood);
						pcg.addFood(foundFood);
					} else {
						Log.logLocalized(SurvivalManager.STRING_TABLE, "noFoodFound", LogType.SURVIVAL, hunter.getName());
					}
				}
				if (waterScout != null) {
					lastWaterScout = waterScout;
					waterScout.position().set(playerGroupGO.position());
					boolean waterResult = waterScout.stats().rollSkillCheck(Skill.SCOUTING, map);
					if (waterResult) {
						Log.logLocalized(SurvivalManager.STRING_TABLE, "waterFound", LogType.SURVIVAL, waterScout.getName());
						pcg.addWater(pcg.getMaxWater());
					} else {
						Log.logLocalized(SurvivalManager.STRING_TABLE, "noWaterFound", LogType.SURVIVAL, waterScout.getName());
					}
				}
				Log.logLocalized(SurvivalManager.STRING_TABLE, "gatheringFinished", LogType.SURVIVAL, finalTime);
				
				recalculateChangedItems();
			}
		}, false);
				
	}
	
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_CAMP_PANEL);
		if (lastHunter != null) {
			writer.element(XML_LAST_HUNTER, lastHunter.getInternalId());
		}
		if (lastWaterScout != null) {
			writer.element(XML_LAST_WATER_SCOUT, lastWaterScout.getInternalId());
		}
		writer.pop();
	}
	
	public void loadFromXML(Element element) {
		Element uiElement = element.getChildByName(XML_CAMP_PANEL);
		if (uiElement != null) {
			String id = uiElement.get(XML_LAST_HUNTER, null);
			if (id != null) {
				this.lastHunter = (GameCharacter) GameState.getGameObjectByInternalId(id);
			}
			
			id = uiElement.get(XML_LAST_WATER_SCOUT, null);
			if (id != null) {
				this.lastWaterScout = (GameCharacter) GameState.getGameObjectByInternalId(id);
			}
		}
	}
	
	public static class CampPanelStyle extends BorderedWindowStyle{
		private int imageMarginBottom, buttonsMarginTop;
		private float buttonPadding;
		private Drawable buttonBackground;
		private Drawable[] images;
		private SelectBoxWithSoundStyle hunterSelectStyle, waterSelectStyle;
		private SimpleTooltipStyle tooltipStyle;
		private LabelStyle headingStyle, textStyle;
		private TextButtonWithSoundStyle gatherButtonStyle, sleepButtonStyle, packUpButtonStyle;
	}
	
}
