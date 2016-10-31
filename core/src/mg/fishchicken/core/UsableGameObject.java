package mg.fishchicken.core;

import groovy.lang.Binding;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.input.tools.Tool;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.projectiles.Projectile;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.statemachine.State;
import mg.fishchicken.core.statemachine.StateMachine;
import mg.fishchicken.core.statemachine.Transition;
import mg.fishchicken.core.util.ColorUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.TweenToAction;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.crime.CrimeManager;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;
import mg.fishchicken.gamelogic.inventory.UsableGameObjectInventory;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.locations.GameLocation;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locations.GameMapLoader;
import mg.fishchicken.gamelogic.locks.Lockable;
import mg.fishchicken.gamelogic.traps.Trapable;
import mg.fishchicken.gamestate.SaveablePolygon;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.gamestate.locks.Lock;
import mg.fishchicken.gamestate.locks.TrapDisarmingLockObserver;
import mg.fishchicken.gamestate.traps.Trap;
import mg.fishchicken.graphics.ShapeDrawer;
import mg.fishchicken.graphics.TextDrawer;
import mg.fishchicken.graphics.animations.AnimatedGameObject;
import mg.fishchicken.graphics.animations.Animation;
import mg.fishchicken.graphics.animations.StateAnimationMap;
import mg.fishchicken.graphics.renderers.FilledPolygonRenderer;
import mg.fishchicken.graphics.renderers.FloatingTextRenderer;
import mg.fishchicken.graphics.renderers.GameMapRenderer;
import mg.fishchicken.pathfinding.AStarPathFinder;
import mg.fishchicken.pathfinding.Path;
import mg.fishchicken.pathfinding.TileBlocker;
import mg.fishchicken.ui.UIManager;
import box2dLight.FixtureUserData;
import box2dLight.FixtureUserData.UserDataType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class UsableGameObject extends AnimatedGameObject implements Lockable, Trapable, InventoryContainer,
		OrientedThing, AssetContainer, TileBlocker, XMLLoadable, TextDrawer, ShapeDrawer {
	
	public static final String XML_STATE_MACHINE = "stateMachine";
	public static final String XML_GROUND = "ground";
	
	public static boolean shouldHighlightUsable(GameObject go, float x, float y) {
		PlayerCharacterController controller = gameState.getPlayerCharacterController();
		return !UIManager.isAnythingOpen()
				&& go.isActive()
				&& (controller.shouldHighlightUsables() || (!controller
						.onlyMoveActionAllowed() && go.contains(x, y)));
	}
	
	private String s_animationTextureFile;
	private String s_animationInfoFile;
	private Orientation s_orientation;
	private StateAnimationMap animations;
	private String s_currentTransition;
	private String s_currentUserId;
	private String s_waitingEvent;
	private String s_actionKey;
	private Inventory inventory;
	private String s_name;
	private int s_apCostToUse;
	private boolean s_blockingPath;
	private boolean s_blockingSight;
	private boolean s_drawIfNotInLOS;
	private boolean s_isUsable;
	private boolean s_notUsableDuringCombat;
	private boolean needOffsetRecalculation;
	private Body losBlockBody;
	private UsableStateMachine stateMachine;
	private Lock lock;
	private Trap trap;
	private FloatingTextRenderer floatingTextRenderer;
	private FilledPolygonRenderer polygonRenderer;
	protected SaveablePolygon s_polygon;
	private float[] cameraVertices;
	private ObjectSet<GameLocation> locations = new ObjectSet<GameLocation>();
	private Tile ground;
	
	/**
	 * Empty constructor for game loading.
	 */
	public UsableGameObject() {
		super();
		init();
	}
	
	public UsableGameObject(String id, FileHandle file) throws IOException {
		super(id, file.nameWithoutExtension());
		init();
		loadFromXML(file);
	}
	
	public UsableGameObject(String id, FileHandle file, SaveablePolygon polygon, GameMap map) throws IOException {
		super(id, file.nameWithoutExtension());
		init();
		this.s_polygon = polygon;	
		loadFromXML(file);
		setMap(map);
	}
	
	private void init() {
		setPlayingAnimation(true);
		s_animationTextureFile = null;
		s_orientation = Orientation.DOWN;
		s_currentTransition = null;
		s_isUsable = true;
		s_name = "";
		s_actionKey = "used";
		lock = new Lock();
		lock.addObserver(new TrapDisarmingLockObserver(this));
		trap = null;
		inventory = createInventory();
		floatingTextRenderer = new FloatingTextRenderer();
	}
	
	private void recalculatePolygons(GameMap map) {
		polygonRenderer = new FilledPolygonRenderer(s_polygon, map);
		Rectangle rect = s_polygon.getBoundingRectangle();
		position().set(rect.x, rect.y);
		setWidth((int)Math.ceil(rect.width));
		setHeight((int)Math.ceil(rect.height));
		
		cameraVertices = Arrays.copyOf(s_polygon.getTransformedVertices(), s_polygon.getTransformedVertices().length);
		if (map.isIsometric()) {
			Vector2 tempVector = MathUtil.getVector2();
			for (int i = 0; i < cameraVertices.length; i += 2) {
				tempVector.set(cameraVertices[i], cameraVertices[i+1]);
				map.projectFromTiles(tempVector);
				cameraVertices[i] = tempVector.x;
				cameraVertices[i+1] = tempVector.y;
			}
			MathUtil.freeVector2(tempVector);
		}
	}
	
	protected Inventory createInventory() {
		return new UsableGameObjectInventory(this);
	}
	
	public String getName() {
		return Strings.getString(s_name);
	}
	
	@Override
	public boolean isOwnerOf(InventoryItem item) {
		return false;
	}
	
	public void setOrientation(Orientation orientation) {
		this.s_orientation = orientation;
		setAnimation(null);
	}
	
	@Override
	public Lock getLock() {
		return lock;
	}
	
	@Override
	public Trap getTrap() {
		return trap;
	}
	
	/**
	 * Gets the tile that represents the ground of this Usable GO. This is usually
	 * the position of the UGO, but for UGOs that are represented by polygons only,
	 * this can be some other tile next to the polygon.
	 * @return
	 */
	public Tile getGround() {
		if (ground == null) {
			return position.tile();
		}
		return ground;
	}
	
	public void setGround(int x, int y) {
		if (ground == null) {
			ground = new Tile(x, y);
		} else {
			ground.set(x, y);
		}
	}
	
	public boolean hasVisibleTrap() {
		if (this.trap == null) {
			return false;
		}
		Trap trap = getTrap();
		return trap != null && trap.isDetected() && !trap.isDisarmed();
	}
	
	@Override
	public GameObject getOriginatorGameObject() {
		return this;
	}
	
	@Override
	public Path findSafeDisarmPath(Path path, GameObject mover, AStarPathFinder pathFinder, Class<? extends Action> action) {
		Tile src = mover.position.tile();
		Tile target = getGround();
		pathFinder.findPath(mover, this, src.getX(), src.getY(), target.getX(), target.getY(), path, true, action);
		return path;
	}
	
	public boolean use(GameObject user) {
		return processEvent("use", user);
	}
	
	public boolean processEvent(String eventId, GameObject user) {
		stateMachine.currentUser = user;
		Transition transition = stateMachine.getTransitionForEvent(eventId);
		if (transition != null) {
			String fromState = stateMachine.getStateAnimationId();
			String toState = stateMachine.getStateAnimationId(transition.getToState());
			if (!fromState.equalsIgnoreCase(toState)) {
				s_currentTransition = fromState+"To"+toState;
				s_currentUserId = user != null ? user.getInternalId() : null;
				s_waitingEvent = eventId;
				stateMachine.currentUser = null;
				setAnimation(null);
				return true;
			}
		}
		
		boolean returnValue  = stateMachine.processEvent(eventId);
		stateMachine.currentUser = null;
		return returnValue;
	}
	
	@Override
	public void update(float deltaTime) {
		if (needOffsetRecalculation) {
			recalculateOffsets();
			needOffsetRecalculation = false;
		}
		if (isBlockingSight() && losBlockBody == null) {
			recreateLightBlockingBody();
		}
		if (!stateMachine.isStarted()) {
			stateMachine.start();
		}
		
		super.update(deltaTime);
		if (s_currentTransition != null) {
			if (isAnimationFinished()) {
				s_currentTransition = null;
				stateMachine.currentUser = s_currentUserId != null ? GameState.getGameObjectByInternalId(s_currentUserId) : null;
				stateMachine.processEvent(s_waitingEvent);
				stateMachine.currentUser = null;
				s_waitingEvent = null;
				setAnimation(null);
			}
		}
	}
	
	@Override
	public Color getColor() {
		if (s_drawIfNotInLOS && !isVisibleToPC()) {
			return new Color(Configuration.getFogColor());
		}
		return super.getColor();
	}
	
	/**
	 * Returns the language key that should be used for the message displayed when this GO is used.
	 */
	public String getActionKey() {
		return s_actionKey;
	}

	@Override
	public Color getHighlightColor(float x, float y) {
		Tool activeTool = gameState.getPlayerCharacterController().getActiveTool();
		if (!isUsable()) {
			return null;
		}
		if (trap != null && trap.isDetected() && !trap.isDisarmed()) {
			return Tool.DISARM == activeTool && contains(x, y) ? Color.GREEN : Color.RED;
		}
		
		return shouldHighlightUsable(this, x, y) 
					|| (lock.isLocked() && lock.isPickable() && Tool.LOCKPICK == activeTool) ? Color.WHITE : null;
	}
	
	@Override
	public int getHighlightAmount(float x, float y) {
		if (trap == null || !trap.isDetected() || trap.isDisarmed()) {
			return 1;
		}
		boolean shouldHightLightLock = (lock.isLocked() && lock.isPickable() && Tool.LOCKPICK == gameState.getPlayerCharacterController().getActiveTool());
		if (!contains(x, y) && !shouldHighlightUsable(this, x, y) && !shouldHightLightLock) {
			return 1;
		}
		return 2;
	}
	
	@Override
	public boolean shouldDraw(Rectangle cullingRectangle) {
		if (!stateMachine.isStarted()) {
			return false;
		}
		if (getMap() != null) {
			if (!getMap().isCurrentMap()) {
				return false;
			}
			if (cullingRectangle != null && !cullingRectangle.contains(position.getX(), position.getY())) {
				return false;
			}
			if (hasSprite()) {
				if (!s_drawIfNotInLOS) {
					return isVisibleToPC();
				} else {
					return getMap().shouldRenderTile((int)position.getX(), (int)position.getY());
				}
			} else {
				if (trap != null && trap.isDetected() && !trap.isDisarmed()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasSprite() {
		return animations != null;
	}
	
	@Override
	public Animation getAnimation() {
		Animation animation = super.getAnimation();
		if (animation == null && hasSprite() && stateMachine.isStarted()) {
			animation = animations.getAnimation(getCurrentAnimationState(), s_orientation, !getMap().isDisposed());
			if (animation == null) {
				Log.log("Could not find animation for usable game object {0} and state {1}, returning null.", LogType.ERROR, this.getInternalId(), stateMachine.getState());
				return null;
			}
			setAnimation(animation);
		}
		return animation;
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}
	
	public String getState() {
		return stateMachine.getState();
	}
	
	private String getCurrentAnimationState() {
		return s_currentTransition != null ? s_currentTransition : stateMachine.getStateAnimationId();
	}
	
	@Override
	public void onHit(Projectile projectile, GameObject originator) {
		processEvent("projectileHit", originator);
	}
	
	@Override
	public boolean isBlockingPath() {
		return s_blockingPath;
	}
	
	public void setBlockingPath(boolean blockingPath) {
		s_blockingPath = blockingPath;
		if (blockingPath) {
			// any characters will be pushed back when a usable becomes impassable
			ObjectSet<GameObject> chars = getMap().getAllGameObjectsAt(
					0.5f + (int) position.getX(), 0.5f + (int) position.getY(), GameCharacter.class); 
			if (chars.size > 0) {
				Vector2 offset = MathUtil.getVector2();
				findFreeTile(s_orientation, offset);
				for (GameObject character : chars) {
					character.addAction(TweenToAction.class, offset.x, offset.y, 10f);
				}
				MathUtil.freeVector2(offset);
			}
		}
	}
	
	private void findFreeTile(Orientation o, Vector2 coordinates) {
		// check the three tiles behind us
		if (setCoordsAndCheckTile(o, coordinates)) {
			return;
		}
		if (setCoordsAndCheckTile(o.getAntiClockwise(), coordinates)) {
			return;
		}
		
		if (setCoordsAndCheckTile(o.getClockwise(), coordinates)) {
			return;
		}
		
		// no luck, check the three in front
		Orientation opposite = o.getOpposite();
		if (setCoordsAndCheckTile(opposite, coordinates)) {
			return;
		}
		if (setCoordsAndCheckTile(opposite.getAntiClockwise(), coordinates)) {
			return;
		}
		
		if (setCoordsAndCheckTile(opposite.getClockwise(), coordinates)) {
			return;
		}
		
		// still no luck? check the two on the sides
		if (setCoordsAndCheckTile(o.getAntiClockwise().getAntiClockwise(), coordinates)) {
			return;
		}
		
		if (setCoordsAndCheckTile(o.getClockwise().getClockwise(), coordinates)) {
			return;
		}
		
		// if still nothing, we just default to the one directly to the back
		setCoordsAndCheckTile(o, coordinates);
	}
	
	private boolean setCoordsAndCheckTile(Orientation o, Vector2 coordinates) {
		o.setOffsetByOrientation(coordinates, getMap().isIsometric());
		coordinates.scl(-1); // push the character back relative to our orientation
		coordinates.add((int)position.getX(), (int)position.getY());
		return !getMap().blocked(null, (int)coordinates.x, (int)coordinates.y);
	}
	
	public boolean isBlockingSight() {
		return s_blockingSight;
	}
	 
	public void setBlockingSight(boolean blockingSight) {
		s_blockingSight = blockingSight;
		if(getMap() != null) {
			if (blockingSight && losBlockBody == null) {
				createLightBlockingBody();
			}
			if (losBlockBody != null) {
				losBlockBody.setActive(blockingSight);
			}
			getMap().updateCharacterVisibleArea(position.getX(), position.getY(), true);
		}
	}

	@Override
	public void setMap(GameMap map) {
		super.setMap(map);
		if (map != null) {
			if (map.isMapLoaded()) {
				recreateLightBlockingBody();
				recalculateOffsets();
			} else {
				needOffsetRecalculation = hasSprite();
			}
			if (polygonRenderer != null) {
				polygonRenderer = null;
			}
			if (s_polygon != null) {
				recalculatePolygons(map);	
			}
		}
	}
	
	private void recalculateOffsets() {
		if (!hasSprite()) {
			return;
		}
		GameMap map = getMap();
		Vector2 offset = animations.getMiddleOffset(getCurrentAnimationState(), s_orientation);
		
		if (map.isIsometric()) {
			setOffsets((-offset.x + map.getTileSizeX())*getScaleX(), -offset.y*getScaleY());
		} else {
			setOffsets((-offset.x + map.getTileSizeX()/2)*getScaleX(), (-offset.y+map.getTileSizeY()/2)*getScaleY());
		}
	}
	
	private void recreateLightBlockingBody() {
		losBlockBody = null;
		setBlockingSight(isBlockingSight());
	}
	
	private void createLightBlockingBody() {
		
		GameMap map = getMap();
		float[]vertices = GameMapLoader.ORTHOGONAL_VERTICES;
		if (map.isIsometric()) {
			vertices = GameMapLoader.ISOMETRIC_VERTICES;
		} 
		Vector2 tempVector = MathUtil.getVector2();
			
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		tempVector.set(position.getX(), position.getY());
		map.projectFromTiles(tempVector);
		bodyDef.position.set(tempVector.x, tempVector.y);
		losBlockBody = map.getFogOfWarWorld().createBody(bodyDef);
		addEdge(losBlockBody, vertices[0], vertices[1], vertices[2], vertices[3]);
		addEdge(losBlockBody, vertices[2], vertices[3], vertices[4], vertices[5]);
		addEdge(losBlockBody, vertices[4], vertices[5], vertices[6], vertices[7]);
		addEdge(losBlockBody, vertices[6], vertices[7], vertices[0], vertices[1]);		
		
		MathUtil.freeVector2(tempVector);
	}
	
	private static void addEdge(Body body, float startX, float startY, float endX, float endY) {
		EdgeShape shape = new EdgeShape();
		shape.set(startX, startY, endX, endY);
		FixtureDef fixture = new FixtureDef();
		fixture.shape = shape;
		fixture.density = 1f;
		body.createFixture(fixture).setUserData(new FixtureUserData(UserDataType.LOS_BLOCKER_TILE));
		shape.dispose();
	}
	
	public boolean isUsable() {
		if (s_isUsable && s_notUsableDuringCombat && GameState.isCombatInProgress()) {
			return gameState.getPlayerCharacterController().getActiveTool() == Tool.DISARM && hasVisibleTrap();
		}
		return s_isUsable;
	}
	
	public void setIsUsable(boolean value) {
		s_isUsable = value;
	}
	
	public int getApCostToUse() {
		return s_apCostToUse;
	}
	
	@Override
	public void draw(GameMapRenderer renderer, float deltaTime) {
		super.draw(renderer, deltaTime);
		if (!hasSprite()) {
			PlayerCharacterController controller = gameState.getPlayerCharacterController();
			Tool activeTool = controller.getActiveTool();
			
			PolygonSpriteBatch spriteBatch = renderer.getSpriteBatch();
			int src = spriteBatch.getBlendSrcFunc();
			int dst = spriteBatch.getBlendDstFunc();
			if (src != GL20.GL_SRC_ALPHA || dst != GL20.GL_ONE) {
				spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			}
			spriteBatch.setColor(isHovered() && activeTool == Tool.DISARM ? ColorUtil.GREEN_SEVENFIVE : ColorUtil.RED_SEVENFIVE);
			polygonRenderer.render(spriteBatch);
			if (src != GL20.GL_SRC_ALPHA || dst != GL20.GL_ONE) {
				spriteBatch.setBlendFunction(src, dst);
			}
		}
	}
	
	@Override
	public void drawText(SpriteBatch spriteBatch, float deltaTime) {
		String text = getName();
		Color color = Color.WHITE;
		if (lock.isLocked()) {
			text += " ("+ Strings.getString(CrimeManager.STRING_TABLE, "locked")+")";
			locations.clear();
			getMap().getLocationsAt(locations, position.tile().getX(), position.tile().getY());
			for (GameLocation location : locations) {
				Faction ownerFaction = location.getOwnerFaction();
				if (ownerFaction != Faction.NO_FACTION && ownerFaction != Faction.PLAYER_FACTION) {
					color = Color.RED;
					break;
				}
			}
		}
		if (trap != null && trap.isDetected() && !trap.isDisarmed()) {
			text += " ("+ trap.getType().getName().toLowerCase(Locale.ENGLISH)+")";
		}
		floatingTextRenderer.setText(text);
		floatingTextRenderer.setColor(color);
		if (hasSprite()) {
			floatingTextRenderer.render(spriteBatch, 0, this, animations.getObjectWidth(), animations.getObjectHeight());
		} else {
			Rectangle rect = s_polygon.getBoundingRectangle();
			GameMap map = getMap();
			floatingTextRenderer.render(spriteBatch, 0, this, (int) (rect.getWidth() * map.getTileSizeX()),
					(int) (rect.getHeight() * map.getTileSizeY()));
		}
	}

	@Override
	public boolean shouldDrawText() {
		if (UIManager.isAnythingOpen()) {
			return false;
		}
		if (!hasSprite()) {
			return shouldDrawShape();
		} else {
			Tool activeTool = gameState.getPlayerCharacterController().getActiveTool();
			float x = gameState.getPlayerCharacterController().getMouseTileX();
			float y = gameState.getPlayerCharacterController().getMouseTileY();
			boolean contains = contains(x, y);
			if (trap != null && trap.isDetected() && !trap.isDisarmed() && Tool.DISARM == activeTool && contains) {
				return  false;
			}
			return contains && shouldDraw(null);
		}
	}
	
	@Override
	public boolean shouldDrawShape() {
		if (hasSprite() || !isUsable()) {
			return false;
		}
		
		PlayerCharacterController controller = gameState.getPlayerCharacterController();
		Tool activeTool = controller.getActiveTool();
		boolean renderPickableOnly = Tool.LOCKPICK == activeTool;
		boolean isPickable = lock != null && lock.isLocked() && lock
				.isPickable();
		
		if (renderPickableOnly && !isPickable) {
			return false;
		}
		
		boolean renderTrapsOnly = Tool.DISARM == activeTool;
		boolean isTrapped = trap != null && trap.isDetected() && !trap.isDisarmed();
		
		if (renderTrapsOnly && !isTrapped) {
			return false;
		}

		return getMap().shouldRenderTile((int)position.getX(), (int)position.getY()) && 
				(controller.shouldHighlightUsables() || isHovered());
	}
	
	@Override
	public boolean contains(float x, float y) {
		if (!hasSprite()) {
			return s_polygon.contains(x, y);
		}
		return super.contains(x, y);
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer, float deltaTime) {
		PlayerCharacterController controller = gameState.getPlayerCharacterController();
		Tool activeTool = controller.getActiveTool();
		
		Color color = Color.WHITE;
		
		Trap trap = getTrap();
		if (Tool.LOCKPICK == activeTool && lock != null && lock.isLocked() && lock.isPickable() && isHovered()) {
			color = Color.GREEN;
		} else if (trap != null && trap.isDetected() && !trap.isDisarmed()) {
			color = isHovered() && activeTool == Tool.DISARM ? Color.GREEN : Color.RED;
		}
		renderer.setColor(color);
		renderer.polygon(cameraVertices);
	}
	
	private boolean isHovered() {
		PlayerCharacterController pcc = gameState.getPlayerCharacterController();
		float mouseX = pcc.getMouseTileX();
		float mouseY = pcc.getMouseTileY();
		
		return contains(mouseX, mouseY);
	}
	
	@Override
	public void undispose() {
		super.undispose();
		losBlockBody = null; // this will be recreated in update()
	}

	@Override
	public void gatherAssets(AssetMap assetStore) {
		if (animations != null) {
			animations.gatherAssets(assetStore);
		}
		inventory.gatherAssets(assetStore);
	}
	
	@Override
	public void clearAssetReferences() {
		if (animations != null) {
			animations.clearAssetReferences();
		}
		super.setAnimation(null);
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		stateMachine = new UsableStateMachine(file);
		loadFromXMLNoInit(file);
	}
	
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		loadFromXML(root);
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		
		lock.loadFromXML(root);
		
		if (root.getChildByName(Trap.XML_TRAP) != null) {
			trap = new Trap(root);
		} else {
			trap = null;
		}
		
		Element ground = root.getChildByName(XML_GROUND);
		if (ground != null) {
			this.ground = new Tile();
			this.ground.setX(ground.getInt(XMLUtil.XML_ATTRIBUTE_X));
			this.ground.setY(ground.getInt(XMLUtil.XML_ATTRIBUTE_Y));
		}
		
		if (stateMachine == null) {
			stateMachine = new UsableStateMachine(Gdx.files.internal(Configuration.getFolderUsables()+getType()+".xml"));
			stateMachine.loadFromXML(root.getChildByName(XML_STATE_MACHINE));
		}
		
		inventory.loadFromXML(root);
		
		if (s_animationTextureFile != null) {
			if (s_animationInfoFile == null) {
				s_animationInfoFile = s_animationTextureFile.substring(0, s_animationTextureFile.lastIndexOf(".")-1)+ANIMATION_INFOS_EXTENSION;
			}
			
			s_animationTextureFile = Configuration.addModulePath(s_animationTextureFile);
			s_animationInfoFile = Configuration.addModulePath(s_animationInfoFile);
			
			try {
				animations = new StateAnimationMap(s_animationTextureFile,
						Gdx.files.internal(s_animationInfoFile));
			} catch (IOException e) {
				throw new GdxRuntimeException("Problem loading animation for usable game object "+getInternalId(),e);
			}
		}
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		lock.writeToXML(writer);
		if (trap != null) {
			trap.writeToXML(writer);
		}
		if (ground != null) {
			writer
				.element(XML_GROUND)
					.attribute(XMLUtil.XML_ATTRIBUTE_X, ground.getX())
					.attribute(XMLUtil.XML_ATTRIBUTE_Y, ground.getY())
				.pop();
		}
		inventory.writeToXML(writer);
		writer.element(XML_STATE_MACHINE);
		stateMachine.writeToXML(writer);
		writer.pop();
	}
	
	private class UsableStateMachine extends StateMachine<UsableState, UsableTransition> implements XMLLoadable {
		
		private Object currentUser;
		
		public UsableStateMachine(FileHandle file) throws IOException {
			super(file);
			currentUser = null;
		}
		
		@Override
		protected Binding buildActionBinding() {
			Binding binding = super.buildActionBinding(); 
			if (currentUser != null) {
				 binding.setVariable(Condition.PARAM_USER, currentUser);
			}
			binding.setVariable(Condition.PARAM_INITIAL_OBJECT, this);
			return binding;
		}
		
		@Override
		public String getName() {
			return s_name;
		};
		
		public String getStateAnimationId() {
			return getStateAnimationId(currentState);
		}
		
		public String getStateAnimationId(String stateId) {
			return getStateAnimationId(getStateForId(stateId));
		}
		
		public String getStateAnimationId(UsableState state) {
			if (state != null) {
				return state.getAnimationStateId();
			}
			return "";
		}
		
		@Override
		protected UsableTransition getTransitionForEvent(String eventId) {
			return super.getTransitionForEvent(eventId);
		}

		@Override
		protected UsableState createState(Element element) {
			return new UsableState(this);
		}
		
		@Override
		public void loadFromXML(FileHandle machineFile) throws IOException {
			loadFromXMLNoInit(machineFile);
		}

		@Override
		public void loadFromXMLNoInit(FileHandle file) throws IOException {
			XmlReader xmlReader = new XmlReader();
			Element root = xmlReader.parse(file);
			XMLUtil.handleImports(this, file, root);
			Element stateMachineElement = root.getChildByName(XML_STATE_MACHINE);
			if (stateMachineElement != null) {
				super.loadFromXML(stateMachineElement);
			}
		}
	}
	
	private class UsableState extends State<UsableTransition> {
		private String s_animationState = null;
		
		public UsableState(UsableStateMachine parentMachine) {
			super(parentMachine);
		}
		
		private String getAnimationStateId() {
			return s_animationState != null ? s_animationState : getId();
		}
		
		@Override
		protected Object getObjectForAction() {
			return UsableGameObject.this;
		}
		
		protected UsableTransition createTransition(Element transitionElement) {
			UsableTransition newTransition = new UsableTransition(this);
			newTransition.loadFromXML(transitionElement);
			return newTransition;
		}
	}
	
	private class UsableTransition extends Transition {
		
		public UsableTransition(State<?> parentState) {
			super(parentState);
		}

		@Override
		protected Binding buildConditionBinding() {
			Binding binding = super.buildConditionBinding();
			if (UsableGameObject.this.stateMachine.currentUser != null) {
				binding.setVariable(Condition.PARAM_USER, UsableGameObject.this.stateMachine.currentUser);
			}
			return binding;
		}
	}
	
}
