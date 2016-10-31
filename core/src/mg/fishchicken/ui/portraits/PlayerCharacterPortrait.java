package mg.fishchicken.ui.portraits;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamestate.GameObjectPosition;
import mg.fishchicken.gamestate.characters.Stats;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;
import mg.fishchicken.ui.tooltips.SimpleTooltip;
import mg.fishchicken.ui.tooltips.SimpleTooltip.SimpleTooltipStyle;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.StatBar;
import com.badlogic.gdx.scenes.scene2d.ui.StatBar.StatBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * UI element that displays the Character portrait.
 * 
 * It also contains buttons to access various character screens like the
 * inventory screen.
 * 
 * @author Annun
 * 
 */
public class PlayerCharacterPortrait extends Table implements EventListener {
	
	private GameState gameState;
	private GameCharacter pc;
	private Stats stats;
	private Button pcPortrait;
	private PlayerCharacterPortraitStyle style;
	private StatBar hp, mp, sp, ap, sneakTiles;
	private SimpleTooltip tooltip;
	private TextButtonWithSound levelUpIndicator;
	
	public PlayerCharacterPortrait(final GameCharacter pc, final GameState gameState, PlayerCharacterPortraitStyle style) {
		this.gameState = gameState;
		this.pc = pc;
		this.stats = pc.stats();
		this.style = style;
		
		tooltip = new SimpleTooltip(style.tooltipStyle);
		
		pcPortrait = new Button(style.frameStyle);
		levelUpIndicator = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "levelUpIndicator"), style.levelUpIndicatorStyle);
		levelUpIndicator.setPosition(style.levelUpIndicatorX, style.levelUpIndicatorY);
		
		WidgetGroup stack = new WidgetGroup();
		pcPortrait.add(stack).width(style.portraitWidth).height(style.portraitHeight);
		if (pc.getPortrait() != null) {
			Image portrait = new Image(pc.getPortrait());
			portrait.setWidth(style.portraitWidth);
			portrait.setHeight(style.portraitHeight);
			stack.addActor(portrait);
			stack.addActor(levelUpIndicator);
		}
		pcPortrait.row();
		hp = new StatBar(pc, false, style.hpBarStyle) {
			@Override
			protected float getStatCurr(GameCharacter character) {
				return stats.getHPAct();
			}
			@Override
			protected float getStatMax(GameCharacter character) {
				return stats.getHPMax();
			}
		};
		pcPortrait.add(hp).expand().fill();
		pcPortrait.row();
		
		sp = new StatBar(pc, false, style.spBarStyle) {
			@Override
			protected float getStatCurr(GameCharacter character) {
				return stats.getSPAct();
			}
			@Override
			protected float getStatMax(GameCharacter character) {
				return stats.getSPMax();
			}
		};
		pcPortrait.add(sp).expand().fill();
		pcPortrait.row();
		
		mp = new StatBar(pc, false, style.mpBarStyle) {
			@Override
			protected float getStatCurr(GameCharacter character) {
				return stats.getMPAct();
			}
			@Override
			protected float getStatMax(GameCharacter character) {
				return stats.getMPMax();
			}
		};
		pcPortrait.add(mp).expand().fill();
		pcPortrait.row();
		
		ap = new StatBar(pc, false, style.apBarStyle) {
			@Override
			protected float getStatCurr(GameCharacter character) {
				return stats.getAPAct();
			}
			@Override
			protected float getStatMax(GameCharacter character) {
				return stats.getAPMax();
			}
		};
		pcPortrait.add(ap).expand().fill();
		pcPortrait.row();
		
		sneakTiles = new StatBar(pc, false, style.sneakBarStyle) {
			@Override
			protected float getStatCurr(GameCharacter character) {
				return Configuration.getTilesPerStealthCheck()-character.getTilesTillStealthCheck();
			}
			@Override
			protected float getStatMax(GameCharacter character) {
				return Configuration.getTilesPerStealthCheck();
			}
		};
		pcPortrait.add(sneakTiles).expand().fill();
		
		if (pc.isSelected()) {
			pcPortrait.setDisabled(true);
			pcPortrait.setChecked(true);
			pcPortrait.setDisabled(false);
		}
		
		this.border(pcPortrait).colspan(3);
		this.row();
		this.border(pcPortrait);
		this.add(pcPortrait);
		this.border(pcPortrait);
		this.row();
		this.border(pcPortrait).colspan(3);
		this.addListener(this);
		this.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (getTapCount() >= 2) {
					doubleClicked();
				}
			}
		});
		
		setHeight(style.portraitHeight+hp.getHeight()+mp.getHeight()+sp.getHeight()+ap.getHeight()+sneakTiles.getHeight()+style.borderHeight*2);
		setWidth(style.portraitWidth+style.borderWidth*6);
	}
	
	public GameCharacter getCharacter() {
		return pc;
	}
	
	private Cell<?> border(Button pcPortrait) {
		return this.add(new PortraitBorder(pcPortrait, style.borderSelected, style.borderNotSelected)).prefWidth(style.borderWidth).prefHeight(style.borderHeight).fill();
	}
	
	private void setChecked(boolean checked) {
		pcPortrait.setDisabled(true);
		pcPortrait.setChecked(checked);
		pcPortrait.setDisabled(false);
		for (Actor a : pcPortrait.getChildren()) {
			if (a instanceof Button) {
				((Button) a).setChecked(checked);
			}
		}
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		setChecked(pc.isSelected());
		Stats stats = pc.stats();
		
		levelUpIndicator.setVisible(stats.getSkillPoints() > 0 || stats.getPerkPoints() > 0);
		//ap.setVisible(GameState.isCombatInProgress() && pc.isActive());
		//sneakTiles.setVisible(pc.isSneaking());
	}
	
	@Override
	public boolean handle(Event event) {
		if (pc.isActive() && pc.belongsToPlayerFaction()) {
			if ((event instanceof ChangeEvent) && !pcPortrait.isDisabled()) {
				return changed((ChangeEvent)event, event.getTarget());
			} else if (event instanceof InputEvent) {
				InputEvent inputEvent = (InputEvent) event;
				if (Type.touchDown == inputEvent.getType() && Buttons.RIGHT == inputEvent.getButton()) {
					return rightClicked(inputEvent, inputEvent.getTarget());
				} 
			}
		} 
		
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (Type.enter.equals(inputEvent.getType())) {
				Actor target = inputEvent.getTarget();
				if (hp.equals(target)) {
					tooltip.setText(Strings.getString(ModifiableStat.STRING_TABLE, "HitPoints")+": "+Math.ceil(stats.getHPAct())+" / "+stats.getHPMax());
				} else if (sp.equals(target)) {
					tooltip.setText(Strings.getString(ModifiableStat.STRING_TABLE, "Stamina")+": "+stats.getSPAct()+" / "+stats.getSPMax());
				} else if (mp.equals(target)) {
					tooltip.setText(Strings.getString(ModifiableStat.STRING_TABLE, "Mana")+": "+stats.getMPAct()+" / "+stats.getMPMax());
				} else if (ap.equals(target)) {
					tooltip.setText(Strings.getString(ModifiableStat.STRING_TABLE, "ActionPoints")+": "+stats.getAPAct()+" / "+stats.getAPMax());
				} else if (sneakTiles.equals(target)) {
					tooltip.setText(Strings.getString(GameCharacter.STRING_TABLE, "Visibility")+":"+(Configuration.getTilesPerStealthCheck()-pc.getTilesTillStealthCheck()));
				} else if (levelUpIndicator.isAscendantOf(target)) {
					tooltip.setText(Strings.getString(UIManager.STRING_TABLE, "levelUpTooltip", pc.getName()));
				} else {
					tooltip.setText(pc.getName());
				}
				gameState.getPlayerCharacterController().updateTargetSelection(pc.position().tile());
				UIManager.setToolTip(tooltip);
			}
			if (Type.exit.equals(inputEvent.getType())) {
				UIManager.hideToolTip();
			}
		}
		
		return true;
	}
	
	private boolean rightClicked (InputEvent event, Actor actor) {
		pc.displayInventory();
		return true;
	}
	
	
	private boolean doubleClicked () {
		// move the camera to the character
		GameObjectPosition position = pc.position();
		GameMap map = gameState.getCurrentMap();
		if (map.isWorldMap()) {
			position = GameState.getPlayerCharacterGroup().getGroupGameObject().position();
		}
		
		Vector3 tempVector = MathUtil.getVector3().set(position.getX(), position.getY(), 0);
		map.projectFromTiles(tempVector);
		map.getCamera().position.set(tempVector);
		MathUtil.freeVector3(tempVector);
		gameState.cameraMoved();
		
		return true;
	}
	
	public boolean changed (ChangeEvent event, Actor actor) {
		if (actor instanceof StatBar) {
			return false;
		}
		if (UIManager.getDisplayedCharacter() != null) {
			if (!pc.equals(UIManager.getDisplayedCharacter())) {
				if (levelUpIndicator.equals(actor) && !UIManager.isPerksScreenOpen()) {
					UIManager.togglePerks(pc);
				} else {
					UIManager.switchDisplayedCharacter(pc);
				}
			} else if (levelUpIndicator.equals(actor)) {
				UIManager.togglePerks(pc);
			}
		} else {
			PlayerCharacterController controller = gameState.getPlayerCharacterController();
			if (controller.isTargetSelectionInProgress()) {
				Vector2 tempVector = MathUtil.getVector2();
				boolean returnValue = controller.handleTargetSelection(Buttons.LEFT, pc.position().setVector2(tempVector));
				MathUtil.freeVector2(tempVector);
				return returnValue;
			}
			
			if (levelUpIndicator.equals(actor)) {
				UIManager.togglePerks(pc);
				return true;
			}
			
			if (!controller.isMultiSelectActive()) {
				boolean wasSelected = pc.isSelected();
				GameState.getPlayerCharacterGroup().selectOnlyMember(pc);
				if (!wasSelected) {
					pc.getAudioProfile().playCharacterBark(pc);
				}
			} else {
				GameState.getPlayerCharacterGroup().toggleMemberSelection(pc);
			}
		}
		return true;
	}
	
	static public class PlayerCharacterPortraitStyle {
		public float portraitWidth, portraitHeight, borderWidth, borderHeight, levelUpIndicatorX, levelUpIndicatorY;
		public Drawable borderSelected, borderNotSelected;
		public SimpleTooltipStyle tooltipStyle;
		public ButtonStyle frameStyle;
		public StatBarStyle hpBarStyle, spBarStyle, mpBarStyle, apBarStyle, sneakBarStyle;
		public TextButtonWithSoundStyle levelUpIndicatorStyle;

		public PlayerCharacterPortraitStyle () {
			portraitWidth = 120;
			portraitHeight = 150;
			borderWidth = 1;
			borderHeight = 1;
			levelUpIndicatorX = 10; 
			levelUpIndicatorY = 10;
		}

	}
}
