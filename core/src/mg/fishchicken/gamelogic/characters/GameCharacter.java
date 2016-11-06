package mg.fishchicken.gamelogic.characters;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;
import java.util.Iterator;

import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.ColorUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.ChainAction;
import mg.fishchicken.gamelogic.actions.MoveToAction;
import mg.fishchicken.gamelogic.actions.WanderAction;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.characters.perks.PerksContainer;
import mg.fishchicken.gamelogic.dialogue.Chatter;
import mg.fishchicken.gamelogic.dialogue.Chatter.ChatterType;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.Effect.PersistentEffect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.GameCharacterInventory;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemOwner;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.gamelogic.magic.SpellsContainer;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.story.StorySequence;
import mg.fishchicken.gamelogic.traps.TrapOriginator;
import mg.fishchicken.gamelogic.traps.TrapType;
import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.Observer;
import mg.fishchicken.gamestate.characters.DamageInfo;
import mg.fishchicken.gamestate.characters.Skills;
import mg.fishchicken.gamestate.characters.Skills.SkillChange;
import mg.fishchicken.gamestate.characters.Stats;
import mg.fishchicken.gamestate.characters.Stats.StatChange;
import mg.fishchicken.gamestate.characters.Survival;
import mg.fishchicken.gamestate.characters.Survival.SurvivalChange;
import mg.fishchicken.gamestate.crime.Assault;
import mg.fishchicken.gamestate.crime.Murder;
import mg.fishchicken.gamestate.traps.Trap;
import mg.fishchicken.graphics.TextDrawer;
import mg.fishchicken.graphics.animations.Animation;
import mg.fishchicken.graphics.animations.ItemAnimationMap;
import mg.fishchicken.graphics.models.ItemModel;
import mg.fishchicken.graphics.renderers.FloatingTextRenderer;
import mg.fishchicken.graphics.renderers.GameMapRenderer;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class GameCharacter extends AbstractGameCharacter implements XMLLoadable, TextDrawer, PerksContainer, SpellsContainer, SkillCheckModifier, InventoryContainer {
	
	public static enum Skill {
		ARMOR, SWORD, DAGGER, AXE, STAFF, BOW, UNARMED, THROWN, DUALWIELDING, DODGE, 
		CLIMBING, SWIMMING, HUNTING, SCOUTING, SNEAKING, PERSUASION, TRAPS, LOCKPICKING, 
		SOMATIC, ACOUSTIC, MIND, FOCUS;
		
		public String toUIString() {
			return Strings.getString(ModifiableStat.STRING_TABLE, toString());
		}
	}
	
	private static final int[] PAPER_DOLL_RENDER_ORDER_BODY = new int[]{
		Inventory.ItemSlot.TORSO.getSlot(),
		Inventory.ItemSlot.LEGS.getSlot(),
		Inventory.ItemSlot.ARMS.getSlot(),
		Inventory.ItemSlot.BELT.getSlot(),
		Inventory.ItemSlot.FEET.getSlot(),
		Inventory.ItemSlot.HEAD.getSlot(),
		Inventory.ItemSlot.CLOAK.getSlot()
	};	
	
	private static final int[] PAPER_DOLL_RENDER_ORDER_HANDS = new int[]{
		Inventory.ItemSlot.LEFTHAND.getSlot(),
		Inventory.ItemSlot.RIGHTHAND.getSlot()
	};	

	public static GameCharacter getCharacter(String type, String id) throws IOException {
		GameObject go = id != null ?  GameState.getGameObjectByInternalId(id) : null;
		if (go instanceof GameCharacter) {
			return (GameCharacter) go;
		}
		return loadCharacter(type, gameState.getCurrentMap());
	}
	
	public static GameCharacter loadCharacter(String type) throws IOException {
		return loadCharacter(type, null);
	}
	
	public static GameCharacter loadCharacter(String type,
			GameMap map) throws IOException {
		return loadCharacter(type, Gdx.files.internal(Configuration.getFolderCharacters()+type+".xml"), map);
	}
	
	public static GameCharacter loadCharacter(String id, FileHandle file,
			GameMap map) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		if (PlayerCharacter.class.getSimpleName().equalsIgnoreCase(
				root.getName())) {
			return new PlayerCharacter(id, file, map);
		}
		return new GameCharacter(id, file, map);
	}
	
	private Array<DamageInfo> damageQueue; // damage waiting to be dealt to this character
	
	private Stats stats;
	private Survival survival;
	
	private Array<Perk> perks;
	private Array<Spell> spells;
	
	private float s_combatStartX, s_combatStartY;
	private boolean s_shouldReturnAfterCombat;
	private boolean s_shouldSearchAfterCombat;
	private GameObject s_killer;
	private boolean s_isAsleep;
	private boolean s_isInvisible;
	private boolean s_isEssential;
	private mg.fishchicken.core.actions.Action onDeathAction;
	private Action combatEndAction;
	private ObjectMap<String, PersistentEffect> effects;
	private int s_spellImmunityCount;
	private boolean s_metNPCBefore;
	private String s_dialogueId;
	private Chatter s_chatter;
	private boolean s_mutedChatter;
	private boolean s_selected;
	private String s_portraitFile;
	private Role s_role;
	private boolean s_isSneaking;
	private int s_tilesTillStealthCheck;
	private GameCharacterInventory inventory;
	private String s_fineDialogueId;
	private String s_lawEnfoncerDialogueId;
	private boolean s_lawEnfoncer;
	private boolean s_isDetectingTraps;
	private boolean s_playerEditable;
	
	// non state properties
	private FloatingTextRenderer chatterRenderer;
	private FloatingTextRenderer floatingTextRenderer;
	private TextureRegion portrait; // lazy init
	private boolean inventoryOpenedThisTurn;
	private IntMap<ItemAnimationMap> itemAnimationMaps;
	private IntMap<ItemModel> itemModels;
	private IntMap<Animation> itemAnimations;
	
	/**
	 * Empty constructor for game loading.
	 */
	public GameCharacter() {
		super();
		init();
	}
	
	/**
	 * Constructor for character creation.
	 */
	protected GameCharacter(String id, String type) {
		super(id, type);
		init();
		chatterRenderer = new FloatingTextRenderer();
	}
	
	public GameCharacter(String id, FileHandle characterFile,  GameMap map) throws IOException {
		super(id, characterFile.nameWithoutExtension());
		init();
		loadFromXML(characterFile);
		setMap(map);
	}
	
	private void init() {
		// width and height of characters is always one, since they occupy only one tile
		setWidth(1);
		setHeight(1);
		itemAnimationMaps = new IntMap<ItemAnimationMap>();
		itemModels = new IntMap<ItemModel>();
		itemAnimations = new IntMap<Animation>();
		floatingTextRenderer = new FloatingTextRenderer();
		inventory = new GameCharacterInventory(this);
		stats = new Stats(this, inventory);
		stats.addObserver(new Observer<Stats, Stats.StatChange>() {
			@Override
			public void hasChanged(Stats stateObject, StatChange changes) {
				endInvalidEffects();
			}
		});
		stats.skills().addObserver(new Observer<Skills, Skills.SkillChange>() {
			@Override
			public void hasChanged(Skills stateObject, SkillChange changes) {
				if (changes.getSkill() == Skill.SNEAKING && animations != null) {
					updateAnimationsSpeed();
				}
			}
		});
		s_spellImmunityCount = 0;
		s_selected = false;
		s_isAsleep = false;
		s_isEssential = false;
		s_lawEnfoncer = false;
		s_isDetectingTraps = false;
		s_playerEditable = true;
		s_chatter = new Chatter();
		damageQueue = new Array<DamageInfo>();
		effects = new ObjectMap<String, PersistentEffect>();
		perks = new Array<Perk>();
		spells = new Array<Spell>();
		survival = new Survival(gameState, GameState.getPlayerCharacterGroup(), stats);
		survival.addObserver(new Observer<Survival, Survival.SurvivalChange>() {
			@Override
			public void hasChanged(Survival stateObject,
					SurvivalChange params) {
				if (params.updateFinished) {
					checkDeath();
				} 
				if (params.messageKey != null) {
					params.messageParams.insert(0, getName());
					Log.logLocalized(params.messageKey, LogType.SURVIVAL, params.messageParams.toArray(Object.class));
				}
			}
		});
		setPlayingAnimation(true);
	}
	
	@Override
	public void update(float deltaTime) {
		if (chatterRenderer != null) {
			chatterRenderer.setMuteChatter(s_mutedChatter);
		}
		Iterator<DamageInfo> damageIterator = damageQueue.iterator();
		while (damageIterator.hasNext()) {
			DamageInfo dinfo = damageIterator.next();
			dinfo.setDelay(dinfo.getDelay() - deltaTime);
			if (dinfo.getDelay() <= 0) {
				dealDamage(dinfo.getDamage(), dinfo.getSource());
				damageIterator.remove();
			}
		}
		
		checkDeath();
	
		super.update(deltaTime); 
		
		if (isActive()) {
			if (combatEndAction != null) {
				combatEndAction.update(deltaTime);
				// the null check is important here, since the update to the search action
				// might have initiated combat, which would zero out the search action
				if (combatEndAction.isFinished()) {
					combatEndAction = null;
					clearLastKnownEnemyPosition();
				}
			} 
			if (!GameState.isCombatInProgress() && getMap() != null && gameState.getCurrentMap() != null) {
				// convert real-time delta time into game turns
				float effectDurationDelta = (deltaTime * gameState.getCurrentMap().getGameTimeMultiplier())
						/ Configuration.getCombatTurnDurationInGameSeconds();
				updatePersistentEffects(effectDurationDelta);
				gameState.getCrimeManager().checkForCriminals(this);
			}
			
			if (State.ONHIT.equals(getState())) {
				if (isAnimationFinished()) {
					setState(State.IDLE);
				}
			}
			
			if (isMemberOfPlayerGroup() && gameState.getCurrentMap() != null && !GameState.isCombatInProgress()) {
				updateSurvival(deltaTime * gameState.getCurrentMap().getGameTimeMultiplier());
				stats.setAPAct(stats.getAPMax());
			}
		}
	};
	
	public boolean isLawEnfoncer() {
		return s_lawEnfoncer;
	}
	
	public void updateSurvival(float gameSeconds) {
		if (!isAsleep()) {
			boolean isPC = isMemberOfPlayerGroup();
			survival.update(gameSeconds / 3600, !isPC, !isPC);
		}
	}
	
	public void hasSlept(float duration) {
		if (isActive()) {
			boolean isPC = isMemberOfPlayerGroup();
			survival.hasSlept(duration, !isPC, !isPC);
			setIsAsleep(false);
		}
	}
	
	public Stats stats() {
		return stats;
	}
	
	@Override
	protected Color getViewConeColor() {
		if(isHostileTowardsPlayer()) {
			return Color.RED;
		}
		return super.getViewConeColor();
	}
	
	protected void handleDetectTraps() {
		// only PCs can currently detect traps
		if (!s_isDetectingTraps || getMap() == null || !isMemberOfPlayerGroup())  {
			return;
		}
		
		boolean detected = false;
		int skillLevel = stats.skills().getSkillRank(Skill.TRAPS); 
		// search for traps is sight radius, have to include inactives since trap originators are like that
		ObjectSet<GameObject> trapableGOs = new ObjectSet<GameObject>();
		getMap().getAllObjectsInArea(trapableGOs, getVisibleArea(), true, Trapable.class);
		for (GameObject go :trapableGOs) {
			Trap trap  = ((Trapable) go).getTrap();
			if (canDetectTrap(trap, skillLevel)) {
				String key = "trapDetectedOnGameObject";
				if (go instanceof TrapOriginator) {
					if (((TrapOriginator)go).isTrapLocation()) {
						key = "trapDetected";
					} else {
						key = "trapDetectedOnTransitionDoor";
					}
				}
				Log.logLocalized(TrapType.STRING_TABLE, key,
						LogType.INFO, getName(), trap.getType().getName(),
						go.getName());
				detectTrap(trap);
				detected = true;
			}
		}
		
		if (detected) {
			removeAllVerbActions();
		}
	}
	
	private void detectTrap(Trap trap) {
		trap.setDetected(true);
		stats.giveExperience(trap.getType().getLevel() * 2);
	}
	
	private boolean canDetectTrap(Trap trap, int skillLevel) {
		return trap != null && !trap.isDisarmed() && !trap.isDetected() && trap.getType().getLevel() <= skillLevel + 1;
	}
	
	@Override
	protected void handleStealth() {
		super.handleStealth();
		if (isSneaking() && !isInvisible()) {
			ObjectSet<AbstractGameCharacter> characters = new ObjectSet<AbstractGameCharacter>();
			getMap().getAllObjectsInArea(characters, getVisibleArea(), AbstractGameCharacter.class);
			Iterator<AbstractGameCharacter> itertor = characters.iterator();
			while (itertor.hasNext()) {
				AbstractGameCharacter character  = itertor.next();
				if (this.getFaction().equals(character.getFaction())) {
					itertor.remove();
					continue;
				}
				if (character.getViewConeArea().contains((int)position.getX(), (int)position.getY())) {
					setIsSneaking(false);
					Log.logLocalized("detectedBy", LogType.CHARACTER,  getName(), character.getName());
				}
				
				if (!isSneaking()) {
					return;
				}
			}
			
			if (characters.size > 0) {
				--s_tilesTillStealthCheck;
				if (s_tilesTillStealthCheck < 1) {
					if (!stats.rollSkillCheck(Skill.SNEAKING, getMap())) {
						Log.logLocalized("detectedBy", LogType.CHARACTER,  getName(), characters.iterator().next().getName());
						setIsSneaking(false);
					} else {
						s_tilesTillStealthCheck = Configuration.getTilesPerStealthCheck() + stats.skills().getSkillRank(Skill.SNEAKING);
					}
				}
			} else {
				s_tilesTillStealthCheck = Configuration.getTilesPerStealthCheck() + stats.skills().getSkillRank(Skill.SNEAKING);
			}
			
		}
	}
	
	@Override
	public void removeAllActions() {
		super.removeAllActions();
		if (combatEndAction != null) {
			combatEndAction.onRemove(this);
			combatEndAction = null;
		}
	}
	
	@Override
	protected void determineNextAIAction() {
		if (!isAsleep() && !belongsToPlayerFaction() && combatEndAction == null) {
			super.determineNextAIAction();
		}
	}
	
	@Override
	public void setState(String state, float delay) {
		if (s_isAsleep) {
			return;
		}
		if (isSneaking()) {
			if (State.IDLE.equals(state)) {
				state = State.SNEAKINGIDLE;
			} else if (State.WALK.equals(state)) {
				state = State.SNEAKING;
			}
		}
		
		super.setState(state, delay);
		updatePaperDollAnimations();
	}
	
	@Override
	public void setOrientation(Orientation o) {
		super.setOrientation(o);
		updatePaperDollAnimations();
	}
	
	@Override
	protected void changedTile() {
		handleDetectTraps();
		
		super.changedTile();
		
		if (GameState.isCombatInProgress()) {
			broadcastMyPositionToEnemiesInSight();
		}
	}
	
	public boolean isSelected() {
		return s_selected;
	}
	
	public void setSelected(boolean selected) {
		this.s_selected = selected;
		resetCharacterCircleColor();
	}
	
	@Override
	public void onAttack(GameCharacter attacker, float delay) {
		super.onAttack(attacker, delay);
		setIsAsleep(false);
	}
	
	/**
	 * Make the character shout the supplied text. This will make the text
	 * appear above the head of the character as a floating text, if the character
	 * is visible and active.
	 * 
	 * @param textToShout
	 */
	public void shout(String textToShout) {
		chatterRenderer.setText(textToShout);
	}
	
	/**
	 * Mutes chatter on this character, preventing it
	 * from automatically rendering any text.
	 */
	public void muteChatter() {
		s_mutedChatter = true;
	}
	
	/**
	 * Unmutes chatter on this character, allowing it
	 * to automatically render text if it has Chatter defined.
	 */
	public void unmuteChattter() {
		s_mutedChatter = false;
	}
	
	@Override
	public boolean shouldRenderDestination() {
		return isSelected() && isMemberOfPlayerGroup();
	}
	
	@Override
	public void draw(GameMapRenderer renderer, float deltaTime) {
		super.draw(renderer, deltaTime);
		if (getModel().isPaperdoll()) {
			drawPaperdoll(renderer, deltaTime, PAPER_DOLL_RENDER_ORDER_BODY);
		}
		drawPaperdoll(renderer, deltaTime, PAPER_DOLL_RENDER_ORDER_HANDS);
	}
	
	private void drawPaperdoll(GameMapRenderer renderer, float deltaTime, int[] slots) {
		float stateTime = getAnimationStateTime();
		GameMap map = getMap();
		boolean loadIfRequired = !map.isDisposed();
		boolean isometric = map.isIsometric();
		float tileSizeX = map.getTileSizeX();
		float tileSizeY = map.getTileSizeY();
		float scaleX = getScaleX();
		float scaleY = getScaleY();
		PolygonSpriteBatch spriteBatch = renderer.getSpriteBatch();
		
		Vector2 projectedCoordinates = position.setVector2(MathUtil.getVector2());
		map.projectFromTiles(projectedCoordinates);
		float x = projectedCoordinates.x;
		float y = projectedCoordinates.y;
		MathUtil.freeVector2(projectedCoordinates);
		
		for (int i = 0; i < slots.length; ++i) {
			int key = slots[i];
			Animation animation = itemAnimations.get(key);
			ItemAnimationMap animations = itemAnimationMaps.get(key);
			if (animations == null) {
				continue;
			}
			if (animation == null) {
				animation = animations.getAnimation(s_state, s_orientation, loadIfRequired);
				if (animation != null) {
					itemAnimations.put(key, animation);
				} else {
					continue;
				}
			}
			
			Vector2 offset = animations.getMiddleOffset(s_state, s_orientation);
			float offsetX, offsetY;
			if (isometric) {
				offsetX = (-offset.x + tileSizeX) * scaleX;
				offsetY = -offset.y * scaleY;
			} else {
				offsetX = (-offset.x + tileSizeX / 2) * scaleX;
				offsetY = (-offset.y + tileSizeY / 2) * scaleY;
			}
			
			spriteBatch.draw(animation.getKeyFrame(stateTime), x+offsetX, y+offsetY, animation.getFrameWidth() * scaleX,
					animation.getFrameHeight() * scaleY);
		}
	}	
	
	private void updatePaperDollAnimations() {
		GameMap map = getMap();
		boolean loadIfRequired = map != null && !map.isDisposed();
		itemAnimations.clear();
		for (int i = 0; i < PAPER_DOLL_RENDER_ORDER_BODY.length; ++i) {
			int key = PAPER_DOLL_RENDER_ORDER_BODY[i];
			ItemAnimationMap animations = itemAnimationMaps.get(key);
			itemAnimations.put(key, animations == null ? null : animations.getAnimation(s_state, s_orientation, loadIfRequired));
		}
		for (int i = 0; i < PAPER_DOLL_RENDER_ORDER_HANDS.length; ++i) {
			int key = PAPER_DOLL_RENDER_ORDER_HANDS[i];
			ItemAnimationMap animations = itemAnimationMaps.get(key);
			itemAnimations.put(key, animations == null ? null : animations.getAnimation(s_state, s_orientation, loadIfRequired));
		}
	}
	
	private void updateAnimationsSpeed() {
		float speed = getSpeed();
		animations.setSpeed(speed);
		for (ItemAnimationMap items : itemAnimationMaps.values()) {
			items.setSpeed(speed);
		}
	}
	
	@Override
	public void drawText(SpriteBatch spriteBatch, float deltaTime) {
		if (!isActive()) {
			return;
		}
		if (GameState.isPaused()) {
			deltaTime = 0;
		}
		FloatingTextRenderer rendererToUse = chatterRenderer;
		if (contains(gameState.getPlayerCharacterController().getMouseTileX(), gameState.getPlayerCharacterController().getMouseTileY())) {
			floatingTextRenderer.setText(getName());
			deltaTime = 0;
			if (isMemberOfPlayerGroup()) {
				floatingTextRenderer.setColor(ColorUtil.GREEN_FIFTY);
			} else if (isHostileTowardsPlayer()) {
				floatingTextRenderer.setColor(ColorUtil.RED_FIFTY);
			} else {
				floatingTextRenderer.setColor(ColorUtil.WHITE_FIFTY);
			}
			rendererToUse = floatingTextRenderer;
		}
		if (rendererToUse != null) {
			rendererToUse.render(spriteBatch, deltaTime, this, animations.getObjectWidth(), animations.getObjectHeight());
		}
	}
	
	@Override
	public boolean shouldDrawText() {
		return !UIManager.isAnythingOpen() && isActive() && !isAsleep() && shouldDraw(null);
	}
	
	public void setMetNPCBefore(boolean value) {
		this.s_metNPCBefore = value;
	}
	
	/**
	 * Returns true if this NPC has been met by the Player before.
	 * 
	 * This usually means it has been talked to by any PlayerCharacter.
	 * 
	 * @return
	 */
	public boolean getMetNPCBefore() {
		return s_metNPCBefore;
	}
	
	@Override
	public String getDialogueId() {
		return s_dialogueId != null ? s_dialogueId : (s_role != null ? s_role.getDialogueId() : null);
	}

	@Override
	public GameCharacter getRepresentative() {
		return this;
	}
	
	public void setDialogueId(String dialogueId) {
		this.s_dialogueId = dialogueId;
	}
	
	@Override
	public boolean alreadyVisited() {
		return getMetNPCBefore();
	}
	
	@Override
	public void updateVisibleArea(boolean recalculateLOS) {
		if (isAsleep()) {
			clearVisibleArea();
			addToVisibleArea((int)position.getX(), (int)position.getY());
			return;
		}
		super.updateVisibleArea(recalculateLOS);
	}
	
	/**
	 * Checks whether we are dead or not  and performs the necessary actions if not.
	 * 
	 * @return
	 */
	private void checkDeath() {
		if (isActive() && !stats.isInvincible() && stats.getHPAct() < 1) {
			die();
		}
	}
	
	/**
	 * Makes this character truly dead.
	 * 
	 * Sets HP, SP and MP to zero, plays the death animation, sets it as
	 * inactive and drops all items.
	 */
	public void die() {
		if (!State.DEATH.equals(getState())) {
			setState(State.DEATH);
		} 
		
		Log.logLocalized("DeathMessage", LogType.COMBAT, this.getName());
		
		PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
		if (pcg.getNonPlayerCharacters().contains(this, true)) {
			pcg.removeNonPlayerCharacter(this);
		}
		
		if (isSelected()) {
			pcg.deselectMember(this);
		}
		survival.removeAllModifiers();
		stats.setHPAct(0);
		stats.setMPAct(0);
		stats.setSPAct(0);
		setGlobal(false);
		disableAI();
		setActive(false);
		updateVisibleArea();
		
		inventory.dropEverythingToTheGround();
		
		if (onDeathAction != null) {
			Binding params = new Binding();
			params.setVariable(Condition.PARAM_INITIAL_OBJECT, this);
			onDeathAction.execute(this, params);
		}
		
		if (s_killer instanceof GameCharacter) {
			gameState.getCrimeManager().registerNewCrime(new Murder((GameCharacter)s_killer, this));
		}
		
		// if we died and now all player characters are dead or we are essential, we end the game
		if (isEssential() || !pcg.hasActiveMembers()) {
			UIManager.displayStorySequence(StorySequence.ENDING_DEFEAT);
		}
	}
	
	/**
	 * Returns the GO that killed this character.
	 * 
	 * @return
	 */
	public GameObject getKiller() {
		return s_killer;
	}

	/**
	 * Gets the role the character should have in a dialogue.
	 * 
	 * @return
	 */
	public Role getRole() {
		return s_role;
	}

	public void setRole(Role role) {
		s_role = role;
	}
	
	@Override
	public Faction getFaction() {
		if (isMemberOfPlayerGroup()){
			return Faction.PLAYER_FACTION;
		}
		return super.getFaction();
	}

	public void setFaction(Faction faction) {
		super.setFaction(faction);
		endInvalidEffects();
	}
	
	public String getPortraitFile() {
		return s_portraitFile;
	}
	
	public void setPortraitFile(String file) {
		s_portraitFile = file;
		portrait = null;
	}
	
	public TextureRegion getPortrait() {
		if (portrait == null && s_portraitFile != null) {
			portrait = Assets.getTextureRegion(s_portraitFile);
		}
		return portrait;
	}	
	
	@Override
	public boolean shouldStartCombat() {
		if (isInvisible() || isSneaking() || isAsleep()) {
			return false;
		}
		return super.shouldStartCombat();
	}
	
	@Override
	public float getSpeed() {
		float speed = stats.getRace().getWalkSpeed();
		if (s_isSneaking) {
			speed = stats.getRace().getSneakingSpeed()
					+ ((stats.getRace().getWalkSpeed() - stats.getRace().getSneakingSpeed()) / 5)
					* stats.skills().getSkillRank(Skill.SNEAKING);
		}
		
		if (s_isDetectingTraps) {
			float detectSpeed = stats.getRace().getDetectingTrapsSpeed()
					+ ((stats.getRace().getWalkSpeed() - stats.getRace().getDetectingTrapsSpeed()) / 5)
					* stats.skills().getSkillRank(Skill.TRAPS);
			
			if (detectSpeed < speed) {
				speed = detectSpeed;
			}
		}
		return speed;
	}
	
	@Override
	public boolean filterUnviableTargets(Effect effect,
			EffectContainer effectContainer, GameObject user) {
		if (effectContainer instanceof Spell && s_spellImmunityCount > 0) {
			Log.logLocalized("unaffectedBy", LogType.COMBAT, this.getName(), ((Spell)effectContainer).getName());
			--s_spellImmunityCount;
			endInvalidEffects();
			return false;
		}
		return super.filterUnviableTargets(effect, effectContainer, user);
	}
	
	public void setSpellImmunityCount (int value) {
		s_spellImmunityCount = value;
		endInvalidEffects();
	}
	
	public int getSpellImmunityCount() {
		return s_spellImmunityCount;
	}
	
	@Override
	public void addPersistentEffect(EffectContainer container, Effect effect, float duration, GameObject user) {
		PersistentEffect pe = new PersistentEffect(container, effect, duration, user, this);
		effects.put(pe.getId(), pe);
	}
	
	public void removePersitentEffect(String id) {
		effects.remove(id);
	}
	
	@Override
	public Array<PersistentEffect> getPersistentEffectsByType(String... types) {
		Array<PersistentEffect> returnValue = new  Array<PersistentEffect>();
		for (PersistentEffect pe : effects.values()) {
			if (pe.isOfTypes(types)) {
				returnValue.add(pe);
			}
		}
		return returnValue;
	}
	
	private void endInvalidEffects() {
		Array<PersistentEffect> effectsToRemove = new Array<PersistentEffect>();
		for (PersistentEffect pe : effects.values()) {
			if (!pe.isConditionValid()) {
				effectsToRemove.add(pe);
			}
		}
		
		for (PersistentEffect pe : effectsToRemove) {
			// need to remove it first so that it does
			// not trigger an endless loop in case it modifies
			// something on us in its on end action that would trigger this
			// method again
			if (effects.remove(pe.getId()) != null) {
				pe.finish();
			}
		}
	}
	
	@Override
	public void addPerk(Perk perk) {
		perks.add(perk);
		// if it's a passive perk, add its modifiers
		// don't do this if loading a game, since in that case mods will be loaded separately
		if (!GameState.isLoadingGame() && !perk.isActivated()) {
			Iterator<Modifier> modifiers = perk.getModifiers();
			while (modifiers.hasNext()) {
				stats.addModifier(modifiers.next().copy());
			}
		}
	}
	
	@Override
	public void addSpell(Spell spell) {
		if (!spells.contains(spell, false)) {
			spells.add(spell);
		}
	}
	
	@Override
	public Array<Perk> getPerks() {
		return perks;
	}
	
	@Override
	public Array<Spell> getSpells() {
		return spells;
	}
	
	/**
	 * Returns true if this character has the supplied Spell.
	 * @param spell
	 * @return
	 */
	public boolean hasSpell(Spell spell) {
		return spells.contains(spell, false);
	}
	
	/**
	 * Returns true if this character has the supplied Perk.
	 * @param perk
	 * @return
	 */
	public boolean hasPerk(Perk perk) {
		return perks.contains(perk, false);
	}

	public void onCombatStart() {
		removeAllVerbActions();
		if (combatEndAction != null) { 
			combatEndAction.onRemove(this);
			combatEndAction = null;
		}
		
		s_combatStartX = position.getX();
		s_combatStartY = position.getY();
		
		if (MathUtils.random(100) < s_chatter.getChanceToSay(ChatterType.COMBAT_STARTED, getCurrentLocations())) {
			shout(s_chatter.getTexts(ChatterType.COMBAT_STARTED, getCurrentLocations()).random());
		}
	}
	
	/**
	 * Does stuff that should happen on the start of each of this
	 * character's turns.
	 */
	public void onTurnStart() {
		stats.setAPAct(stats.getAPMax());
		inventoryOpenedThisTurn = false;
		updatePersistentEffects(1);
	}
	
	/**
	 * Updates persistent effects currently active on this character.
	 * 
	 * @param deltaTurns - time passed since last calling this method, in combat turns
	 */
	public void updatePersistentEffects(float deltaTurns) {
		ObjectMap.Values<PersistentEffect> iterator = new ObjectMap.Values<PersistentEffect>(effects);
		while (iterator.hasNext()) {
			PersistentEffect pe = iterator.next();
			if (pe.executePersistentEffect(deltaTurns)) {
				iterator.remove();
				pe.finish();
			}
		}
	}
	
	public void onCombatEnd() {
		if (belongsToPlayerFaction()) {
			return;
		}
		
		combatEndAction = null;
		resetCharacterCircleColor();
		if (s_shouldReturnAfterCombat || (getLastKnownEnemyPosition() != null && s_shouldSearchAfterCombat)) {
			ChainAction chain = new ChainAction(this);
			if (getLastKnownEnemyPosition() != null && s_shouldSearchAfterCombat) {
				chain.addAction(new WanderAction(this, getLastKnownEnemyPosition(), 5, 60, 30));
			}
			if (s_shouldReturnAfterCombat) {
				chain.addAction(new MoveToAction(this, (int)s_combatStartX, (int)s_combatStartY));
			}
			if (chain.size() > 0) {
				combatEndAction = chain;
			}
		} 
	}
	
	@Override
	public <T extends Action> T addAction(Class<T> actionClass,
			Object... parameters) {
		T action =  super.addAction(actionClass, parameters);
		endInvalidEffects();
		return action;
	}
	
	/**
	 * Gets the AP cost of performing the supplied action on the supplied target.
	 * @param action
	 * @param target
	 * @return
	 */
	@Override
	public int getCostForAction(Class<? extends Action> action, Object target) {
		return Action.getAPCostForAction(stats(), action, target);
	}
	
	@Override
	public void updateTurnAction(float deltaTime) {
		if (!isAsleep()) {
			super.updateTurnAction(deltaTime);
		}
	}
	
	public boolean isMemberOfPlayerGroup() {
		return GameState.getPlayerCharacterGroup().containsCharacter(this);
	}
	
	/**
	 * Deals the supplied damage to this character. If the character dies, the
	 * supplied GO will be registered as the killer.
	 * 
	 * This should be used if recording the killer is important, for example for
	 * subsequent experience points distribution.
	 * 
	 * @param damage
	 * @param damageSource
	 * @return true if this character got killed
	 */
	public boolean dealDamage(float damage, GameObject damageSource) {
		return dealDamage(damage, damageSource, 0);
	}
	
	/**
	 * Deals the supplied damage to this character. If the character dies, the
	 * supplied GO will be registered as the killer.
	 * 
	 * This should be used if recording the killer is important, for example for
	 * subsequent experience points distribution.
	 * 
	 * If delay is specified, the actual damage dealing occurs only after that
	 * many seconds.
	 * 
	 * @param damage
	 * @param damageSource
	 * @return true if this character got killed (if delay == 0) or will get
	 *         killed by all damage pending to be dealt to it
	 */
	public boolean dealDamage(float damage, GameObject damageSource, float delay) {
		if (delay <= 0) {
			stats.addToHP(-damage);
			s_killer = damageSource;
			if (damageSource instanceof GameCharacter && (stats.getHPAct() > 0 || stats.isInvincible())) {
				gameState.getCrimeManager().registerNewCrime(new Assault((GameCharacter)damageSource, this));
			}
			return isActive();
		} else {
			damageQueue.add(new DamageInfo(damage, delay, damageSource));
			return willDieFromPendingDamage();
		}
	}
	
	private boolean willDieFromPendingDamage() {
		if (stats.isInvincible()) {
			return false;
		}
		float actualHP = stats.getHPAct();
		for (DamageInfo dinfo : damageQueue) {
			actualHP -= dinfo.getDamage();
		}
		return actualHP <= 0;
	}
	
	public void displayInventory() {
		if (GameState.isCombatInProgress() && !inventoryOpenedThisTurn) {
			if (stats.getAPAct() < Configuration.getAPCostInventoryOpen()) {
				Log.logLocalized("cannotOpenInventory", LogType.COMBAT, getName(),
						stats.getGender().getPossesivePronoun()
								.toLowerCase(), stats.getGender().getPronoun()
								.toLowerCase(), Configuration
								.getAPCostInventoryOpen());
				return;
			}
			inventoryOpenedThisTurn = true;
			stats.addToAP(-Configuration.getAPCostInventoryOpen());
		}
		UIManager.toggleInventory(this);
	}
	
	@Override
	public boolean isInvisible() {
		return s_isInvisible;
	}
	
	public void setIsInvisible(boolean value) {
		if (s_isInvisible == value) {
			return;
		}
		s_isInvisible = value;
		
		if (value) {
			Color currentColor = getColor();
			currentColor.a = Configuration.getInvisibleCharacterAlpha();
		} else {
			Color currentColor = getColor();
			currentColor.a = 1;
			
			if (!GameState.isCombatInProgress() && shouldStartCombat()) {
				gameState.startCombat();
			}
			
			if (GameState.isCombatInProgress()) {
				broadcastMyPositionToEnemiesInSight();
			}
		}
		endInvalidEffects();
	}
	
	/**
	 * Whether or not this character is asleep.
	 * <br /><br />
	 * Characters that are asleep do not move, eat, drink and 
	 * they do not pay attention to anything.
	 * <br /><br />
	 * They wake up when attacked.
	 * @return
	 */
	public boolean isAsleep() {
		return s_isAsleep;
	}
	
	/**
	 * Puts this character to sleep or waker her up.
	 * 
	 * Sleeping pauses all verb actions currently in progress,
	 * shrinks the visible area to only the tile the character is
	 * currently on, sets the state to Sleep and prints a log
	 * message.
	 * 
	 * @param value
	 */
	public void setIsAsleep(boolean value) {
		if (value == false && s_isAsleep == true) {
			if (isActive()) {
				Log.logLocalized("wokeUp", LogType.CHARACTER, getName());
			}
			allowAllActions("__internalSleepForbidder");
			this.s_isAsleep = value;
			setState(State.IDLE);
		} else if (value == true && s_isAsleep == false) {
			if (isActive()) {
				Log.logLocalized("fellAsleep", LogType.CHARACTER, getName());
			}
			removeAllVerbActions();
			forbidActions("__internalSleepForbidder", Action.VERB_ACTIONS);
			setState(State.SLEEP);
			this.s_isAsleep = value;
		}
		updateVisibleArea();
		endInvalidEffects();
	}

	/**
	 * Returns true if this character is essential. 
	 * 
	 * If an essential character dies, the game ends,
	 * regardless of whether the player has any other 
	 * alive characters.
	 * 
	 * @return
	 */
	public boolean isEssential() {
		return s_isEssential;
	}
	
	/**
	 * Marks this character as essential or not. 
	 * 
	 * If an essential character dies, the game ends,
	 * regardless of whether the player has any other 
	 * alive characters.
	 * 
	 * @param value
	 */
	public void setIsEssential(boolean value) {
		s_isEssential = value;
	}
	
	@Override
	public boolean canTalkTo(GameObject talker) {
		return !isAsleep() && super.canTalkTo(talker);
	}
	
	@Override
	public boolean canTradeWith(Faction faction) {
		return super.canTradeWith(faction) && !isAsleep();
	}
	
	@Override
	public int getSkillCheckModifier(Skill skill, GameCharacter skillUser) {
		int returnValue = 0;
		
		if (Skill.PERSUASION.equals(skill)) {
			int disposition = getFaction().getDispositionTowards(skillUser);
			if (disposition == 100) {
				returnValue = 100; 
			} else if (disposition >= 75) {
				returnValue = 30;
			} else if (disposition >= 50) {
				returnValue = 15;
			} else if (disposition <= -25) {
				returnValue = -15;
			}
		}
		
		if (Skill.SCOUTING.equals(skill)) {
			return (skillUser.stats.getLevel() - stats.getLevel()) * 10;
		}
		
		return returnValue;
	}
	
	@Override
	public boolean finishedTurn() {
		if (isAsleep() || stats.getAPAct() <= 0) {
			removeCurrentTurnAction();
			return true;
		}
		return super.finishedTurn();
	}
	
	@Override
	public void resetCharacterCircleColor() {
		if (belongsToPlayerFaction() && !isSelected()) {
			getCharacterCircle().setColor(ColorUtil.BLACK_FIFTY);
		} else {
			super.resetCharacterCircleColor();
		}
	}
	
	/**
	 * This replaces the character's defined AIScript with a new one.
	 * If you wish to restore the original AI script the character was
	 * loaded with, just call restoreAIScript.
	 * {@link #restoreAIScript()}.
	 * 
	 * If the character is currently controlled by the player,
	 * this will also take the control away.
	 * 
	 * @param newAIScript
	 */
	public void setOverrideAIScript(AIScriptPackage newAIScript) {
		super.setOverrideAIScript(newAIScript);
		GameState.getPlayerCharacterGroup().temporarilyRemoveMember(this);
	}
	
	/**
	 * Restores the original, XML master data defined AI Script 
	 * of this character in case it was replaced by calling 
	 * {@link #setOverrideAIScript(Script)}.
	 * 
	 * If the character was controlled by the player at the time of the override,
	 * this will restore the control.
	 * 
	 */
	public void restoreAIScript() {
		super.restoreAIScript();
		GameState.getPlayerCharacterGroup().restoreTemporarilyRemovedMember(this);
	}
	
	/**
	 * Returns the number of tiles this character can safely cross while sneaking
	 * before another stealth check will have to be made.
	 */
	public int getTilesTillStealthCheck() {
		return s_tilesTillStealthCheck;
	}
	
	/**
	 * Returns the ID of the dialogue that should be initiated by this character
	 * if he sees a crime and wants to demand a fine. This will never return null.
	 */
	public String getFineDialogueId() {
		if (s_fineDialogueId != null) {
			return s_fineDialogueId;
		}
		return Configuration.getDefaultFineDialogueId();
	}
	
	/**
	 * Returns the ID of the dialogue that should be initiated by this character
	 * if he is a law enfoncer and sees a criminal. This will never return null.
	 */
	public String getLawEnfoncerDialogueId() {
		if (s_lawEnfoncerDialogueId != null) {
			return s_lawEnfoncerDialogueId;
		}
		return Configuration.getDefaultLawEnfoncerDialogueId();
	}
	
	/**
	 * Returns the Chatter assigned to this character.
	 */
	public Chatter getChatter() {
		return s_chatter;
	}
	
	/**
	 * Sets the Chatter assigned to this character to the one
	 * with the supplied id.
	 */
	public void setChatter(String chatterId) {
		s_chatter = Chatter.getChatter(chatterId);
		chatterRenderer.setChatter(s_chatter);
	}
	
	@Override
	public boolean isDetectingTraps() {
		return s_isDetectingTraps;
	}
	
	public void setIsDetectingTraps(boolean value) {
		if (s_isDetectingTraps == value) {
			return;
		}
		
		s_isDetectingTraps = value;
		
		// recalculate animation speed
		updateAnimationsSpeed();
		
		if (value) {
			Log.logLocalized("startedDetectingTraps", LogType.CHARACTER, getName());
			handleDetectTraps();
		} else {
			Log.logLocalized("stoppedDetectingTraps", LogType.CHARACTER, getName());
		}
	}
	
	/**
	 * Tells this character to sneak or stop sneaking.
	 * 
	 * @param value
	 */
	public void setIsSneaking(boolean value) {
		if (s_isSneaking == value) {
			return;
		}
		tempSet.clear();
		if (value && getAllCharactersInSightRadius(tempSet, CharacterFilter.NOT_SAME_FACTION)) {
			Log.logLocalized("cannotSneakOthersNearby", LogType.CHARACTER, getName());
			return;
		}
		
		s_isSneaking = value;
		
		// call the state setter to properly set a sneaking state if necessary
		setState(getState());
		// recalculate animation speed
		updateAnimationsSpeed();
		
		if (value) {
			getAudioProfile().getTrack(AudioProfile.SNEAK).play(this);
			Log.logLocalized("startedSneaking", LogType.CHARACTER, getName());
			s_tilesTillStealthCheck = Configuration.getTilesPerStealthCheck();
			handleStealth();
		} else {
			Log.logLocalized("stoppedSneaking", LogType.CHARACTER, getName());
			if (shouldStartCombat()) {
				gameState.startCombat();
			}
		}
	}
	
	@Override
	public boolean isSneaking() {
		return s_isSneaking;
	}
	
	/**
	 * Gets the current amount of gold the game character owns.
	 * @return
	 */
	public int getGold() {
		if (isMemberOfPlayerGroup()) {
			return GameState.getPlayerCharacterGroup().getGold();
		}
		return 0;
	}

	/**
	 * Adds the supplied amount of gold to the character.
	 * @param gold
	 */
	public void addGold(int gold) {
		if (isMemberOfPlayerGroup()) {
			GameState.getPlayerCharacterGroup().addGold(gold);
		}
	}
	
	public void addItemModel(ItemModel model, int slot) {
		model.loadAssets();
		ItemAnimationMap animations = model.getAnimationMapInstance();
		animations.setSpeed(getSpeed());
		itemAnimationMaps.put(slot, animations);
		itemModels.put(slot, model);
	}
	
	public void removeItemModel(int slot) {
		itemAnimationMaps.remove(slot);
		ItemModel model = itemModels.remove(slot);
		if (model != null) {
			model.unloadAssets();
			updatePaperDollAnimations();
		}
	}
	
	public void recalculateAllItemModels() {
		for (ItemModel model : itemModels.values()) {
			model.unloadAssets();
		}
		itemAnimationMaps.clear();
		itemModels.clear();
		
		IntMap<InventoryItem> equipped = getInventory().getBag(BagType.EQUIPPED);
		
		for (InventoryItem item : equipped.values()) {
			ItemModel model = item.getFinalItemModel(this);
			if (model != null) {
				addItemModel(model, item.getSlot());
			}
		}
		
		updatePaperDollAnimations();
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		super.gatherAssets(assetStore);
		inventory.gatherAssets(assetStore);
		if (s_portraitFile != null) {
			assetStore.put(s_portraitFile, Texture.class);
		}
	}
	
	@Override
	public void clearAssetReferences() {
		super.clearAssetReferences();
		inventory.clearAssetReferences();
		portrait = null;
	}
	
	public void loadFromXML(FileHandle characterFile) throws IOException {
		loadFromXMLNoInit(characterFile);
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		loadFromXML(root);
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		stats.loadFromXML(root);
		super.loadFromXML(root);
		if (s_portraitFile != null) {
			s_portraitFile = Configuration.addModulePath(s_portraitFile);
		}
		survival.loadFromXML(root);
		
		XMLUtil.readPerks(this, root.getChildByName(XML_PERKS));
		XMLUtil.readSpells(this, root.getChildByName(XML_SPELLS));
		inventory.loadFromXML(root);
		
		onDeathAction = mg.fishchicken.core.actions.Action.getAction(root.getChildByName(XML_ON_DEATH));
		
		Element combatEndSearchActionElement = root.getChildByName(XML_COMBAT_END_SEARCH_ACTION);
		if (combatEndSearchActionElement != null && combatEndSearchActionElement.getChildCount() > 0) {
			combatEndAction = Action.readFromXML(combatEndSearchActionElement.getChild(0), this);
		}
		
		Element effectsElement = root.getChildByName(Effect.XML_PERSISTENT);
		if (effectsElement != null) {
			for (int i = 0; i < effectsElement.getChildCount(); ++i) {
				PersistentEffect pe = new PersistentEffect(effectsElement.getChild(i), this);
				effects.put(pe.getId(), pe);
			}
		}
		
		Element damageElement = root.getChildByName(XML_DAMAGE_QUEUE);
		if (damageElement != null) {
			for (int i = 0; i < damageElement.getChildCount(); ++i) {
				damageQueue.add(new DamageInfo(damageElement.getChild(i)));
			}
		}
		
		setSelected(s_selected);
		
		chatterRenderer = new FloatingTextRenderer(s_chatter);
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		stats.writeToXML(writer);
		survival.writeToXML(writer);
		
		XMLUtil.writePerks(this, writer);
		XMLUtil.writeSpells(this, writer);
		inventory.writeToXML(writer);
		
		if (combatEndAction != null) {
			writer.element(XML_COMBAT_END_SEARCH_ACTION);
			combatEndAction.writeToXML(writer);
			writer.pop();
		}
		
		if (effects.size > 0) {
			writer.element(Effect.XML_PERSISTENT);
			for (PersistentEffect effect : effects.values()) {
				effect.writeToXML(writer);
			}
			writer.pop();
		}
		
		if (damageQueue.size > 0) {
			writer.element(XML_DAMAGE_QUEUE);
			for (DamageInfo dinfo: damageQueue) {
				dinfo.writeToXML(writer);
			}
			writer.pop();
		}
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
	
	@Override
	public boolean isOwnerOf(InventoryItem item) {
		ItemOwner owner = item.getOwner();
		if (isMemberOfPlayerGroup()) {
			// player character group members share everything
			return GameState.getPlayerCharacterGroup().isOwnerOf(item);		
		}
		return owner.includes(this);
	}
	
	/**
	 * Returns true if the look of this character is editable by the player
	 * when member of the player character group. Default is true.
	 * 
	 * @return
	 */
	public boolean isPlayerEditable() {
		return s_playerEditable;
	}
}
