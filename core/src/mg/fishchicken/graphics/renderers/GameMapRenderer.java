package mg.fishchicken.graphics.renderers;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.input.PlayerCharacterController;
import mg.fishchicken.core.input.tools.Tool;
import mg.fishchicken.core.util.ColorUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroupGameObject;
import mg.fishchicken.gamelogic.characters.los.LineOfSight;
import mg.fishchicken.gamelogic.combat.CombatPath;
import mg.fishchicken.gamelogic.effects.targets.TargetType;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamelogic.locations.GameMapLoader;
import mg.fishchicken.gamelogic.traps.TrapLocation;
import mg.fishchicken.gamelogic.weather.Weather.WeatherRenderer;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.graphics.Drawable;
import mg.fishchicken.graphics.ShapeDrawer;
import mg.fishchicken.graphics.TextDrawer;
import mg.fishchicken.maps.AnimatedTile;
import mg.fishchicken.ui.UIManager;
import shaders.WaterShader;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public abstract class GameMapRenderer implements Disposable {

	public static final String PROP_RENDER_GRID = "renderGrid";
	public static final String PROP_WATER = "water";

	protected GameMap map;
	private SpriteBatch textBatch;
	protected PolygonSpriteBatch spriteBatch;
	protected Rectangle viewBounds;
	protected float unitScaleX, unitScaleY;
	private CombatPathRenderer combatPathRenderer;
	private EffectTargetRenderer effectTargetRenderer;
	private ShapeRenderer shapeRenderer;
	private PlayerCharacterController controller;
	private Rectangle cullingRectangle;
	private ObjectMap<Drawable, Color> highlightedDrawables;
	private GameState gameState;
	private ObjectMap<TrapLocation, FilledPolygonRenderer> trapsToDraw;
	private Array<int[]> trapTiles;
	private float normalColor, fogColor;
	private boolean targetSelectionInProgress;
	private GameCharacter leader;
	//private float amplitudeWave, angleWave, angleWaveSpeed;
	private ShaderProgram waterShader;
	private PlayerCharacterController pcc;
	private FrameBuffer frameBuffer; 
	private TextureRegion frameBufferTextureRegion;
	private PlayerCharacterGroup pcg;
	
	public GameMapRenderer(GameState gameState, GameMap map,
			PlayerCharacterController controller) {
		this.gameState = gameState;
		this.pcc = gameState.getPlayerCharacterController();
		this.pcg = GameState.getPlayerCharacterGroup();
		/*amplitudeWave = 0.5f;
		angleWave = 0;
		angleWaveSpeed = 1f;*/
		waterShader = WaterShader.createWaterShader();
		unitScaleX = map.getScaleX();
		unitScaleY = map.getScaleY();
		this.map = map;
		this.viewBounds = new Rectangle();
		this.spriteBatch = new PolygonSpriteBatch();
		this.controller = controller;
		textBatch = new SpriteBatch();
		textBatch.getProjectionMatrix().setToOrtho2D(0, 0,
				Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		combatPathRenderer = new CombatPathRenderer(controller);
		effectTargetRenderer = new EffectTargetRenderer(controller);
		shapeRenderer = new ShapeRenderer(100);
		cullingRectangle = new Rectangle();
		highlightedDrawables = new ObjectMap<Drawable, Color>();
		trapsToDraw = new ObjectMap<TrapLocation, FilledPolygonRenderer>(); 
		trapTiles = new Array<int[]>();
		normalColor = Color.toFloatBits(1f, 1f, 1f, 1f);
		fogColor = Color.toFloatBits(Configuration.getFogColor().r,
				Configuration.getFogColor().g, Configuration.getFogColor().b,
				1);
		frameBuffer = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		frameBufferTextureRegion = new TextureRegion();
	}

	public GameMap getMap() {
		return map;
	}

	public void setMap(GameMap map) {
		this.map = map;
	}
	
	
	/**
	 * Returns a 32 bit packed color that the specified tile should
	 * be rendered with.
	 * 
	 * @param tileNumber - the ID of the tile to check. Usually calculated as x + y*mapWidth
	 * @return
	 */
	protected Float getTileColor(int tileNumber) {
		return shouldRenderFog(tileNumber) ? fogColor : normalColor;
	}
	
	/**
	 * Returns true if the supplied tile 
	 * is not directly visible by any PC and therefore
	 * should be rendered under a fog.
	 * 
	 * @param tileNumber - the ID of the tile to check. Usually calculated as x + y*mapWidth
	 * @return
	 */
	public boolean shouldRenderFog(int tileNumber) {
		if ((!targetSelectionInProgress && map.isTileVisibleToPC(tileNumber))
				|| (targetSelectionInProgress && leader != null && leader
						.canSeeTile(tileNumber))) {
			return false;
		}

		return true;
	}

	protected boolean shouldRenderGrid() {
		return map.getRenderGrid();
	}
	
	protected void enableWaterShader() {
		spriteBatch.setShader(waterShader);
	}
	
	protected void disableWaterShader() {
		spriteBatch.setShader(null);
	}

	public void render(float deltaTime, OrthographicCamera camera) {
		AnimatedTile.updateAnimationBaseTime();
		
		setView(camera);

		computeCullingRectangle();
		
		targetSelectionInProgress = controller.isTargetSelectionInProgress();
		leader = GameState.getPlayerCharacterGroup().getGroupLeader();

		spriteBatch.begin();
		renderTileLayers(deltaTime, map.getGroundLayers());
		spriteBatch.end();
		
		renderTraps(deltaTime);
		
		if (GameState.isAnySelectedPCSneaking()) {
			map.getViewConesRayHandler().setCombinedMatrix(camera.combined);
			map.getViewConesRayHandler().updateAndRender();
		}
		
		renderPaths(textBatch);
		
		spriteBatch.begin();
		renderLayersExcludingSpecial(deltaTime);
		renderDrawables(deltaTime);
		renderTileLayers(deltaTime, map.getAlwaysAboveLayers());
		if (!map.isInterior()) {
			WeatherRenderer weather = gameState.getCurrentWeatherRenderer();
			if (weather != null) {
				weather.draw(map, spriteBatch, deltaTime);
			}
		}
		spriteBatch.end();

		// draw the transparent part of the path that is "behind" other objects
		renderPaths(textBatch);
	
		RayHandler rayhandler = map.getLightsRayHandler();
		rayhandler.setCombinedMatrix(camera.combined);
		rayhandler.updateAndRender();

		spriteBatch.begin();
		renderTileLayers(deltaTime, map.getOverheadLayers());
		spriteBatch.flush();
		renderHightlights(deltaTime);
		spriteBatch.end();

		renderTexts(deltaTime);

		renderSelectionRectangle();
		renderShapes(deltaTime);
		
		if (Configuration.shouldRenderLightsDebug()) {
			map.getBox2DDebugRenderer().render(map.getLightsWorld(), camera.combined);
		}
		if (Configuration.shouldRenderLOSDebug()) {
			map.getBox2DDebugRenderer().render(map.getFogOfWarWorld(), camera.combined);
			renderLOSDebug(camera);
		}
	}
	
	private void renderHightlights(float deltaTime) {
		int src = spriteBatch.getBlendSrcFunc();
		int dst = spriteBatch.getBlendDstFunc();
		
		int maxHighlightAmount = highlightedDrawables.size > 0 ? 1 : 0;
		
		for (int pass = 0; pass < maxHighlightAmount; ++pass) {
			frameBuffer.begin();
			Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
			Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
			
			float mouseX = pcc.getMouseTileX();
			float mouseY = pcc.getMouseTileY();
			for (Entry<Drawable, Color> entry : highlightedDrawables.entries()) {
				spriteBatch.setColor(entry.value);
				int amount = entry.key.getHighlightAmount(mouseX, mouseY);
				if (pass < amount) {
					entry.key.draw(this, deltaTime);
					if (pass < amount - 1) {
						maxHighlightAmount = amount;
					}
				}
			}
			spriteBatch.flush();
			frameBuffer.end();
			
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			frameBufferTextureRegion.setRegion(frameBuffer.getColorBufferTexture());
            frameBufferTextureRegion.flip(false, true);
			spriteBatch.draw(frameBufferTextureRegion, viewBounds.x, viewBounds.y, viewBounds.width, viewBounds.height);
			spriteBatch.setBlendFunction(src, dst);
			spriteBatch.flush();
		}
		
		// draw player controlled characters as well so that they can still be seen behind walls
		frameBuffer.begin();
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		if (map.isWorldMap()) {
			PlayerCharacterGroupGameObject go = GameState.getPlayerCharacterGroup().getGroupGameObject();
			spriteBatch.setColor(go.getColor());
			go.draw(this, deltaTime);
		} else {
			Array<GameCharacter> members = pcg.getMembers();
			for (int i = 0; i < members.size; ++i) {
				GameCharacter character = members.get(i);
				spriteBatch.setColor(character.getColor());
				character.draw(this, deltaTime);
			}
		}
		spriteBatch.flush();
		frameBuffer.end();
		frameBufferTextureRegion.setRegion(frameBuffer.getColorBufferTexture());
        frameBufferTextureRegion.flip(false, true);
        spriteBatch.setColor(1, 1, 1, 0.3f);
		spriteBatch.draw(frameBufferTextureRegion, viewBounds.x, viewBounds.y, viewBounds.width, viewBounds.height);
		spriteBatch.flush();
	}

	private void renderLOSDebug(Camera camera) {
		shapeRenderer.setProjectionMatrix(camera.combined);
		if (map.isWorldMap()) {
			LineOfSight los = GameState.getPlayerCharacterGroup().getGroupGameObject().getLineOfSight();
			if (los != null) {
				los.renderDebug(shapeRenderer);
			}
		} else {
			for (GameCharacter character : GameState.getPlayerCharacterGroup().getSelected()) {
				LineOfSight los = character.getLineOfSight();
				if (los != null) {
					los.renderDebug(shapeRenderer);
				}
			}
		}
	}

	private void renderTraps(float delta) {
		trapTiles.clear();
		map.getTrapsToDraw(trapsToDraw, getCullingRectangle());
		
		if (trapsToDraw.size < 1) {
			return;
		}
		
		int src = spriteBatch.getBlendSrcFunc();
		int dst = spriteBatch.getBlendDstFunc();
		spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		spriteBatch.begin();
		spriteBatch.setColor(ColorUtil.RED_FIFTY);
		TrapLocation hoveredLocation = gameState.getPlayerCharacterController().getTrapLocationCurrentlyHovered();
		Tool activeTool = gameState.getPlayerCharacterController().getActiveTool();
		for (Entry<TrapLocation, FilledPolygonRenderer> entries : trapsToDraw.entries()) {
			boolean hoveredTrap = hoveredLocation == entries.key && Tool.DISARM == activeTool; 
			if (hoveredTrap) {
				spriteBatch.setColor(ColorUtil.GREEN_FIFTY);
			}
			entries.value.render(spriteBatch);
			if (hoveredTrap) {
				spriteBatch.setColor(ColorUtil.RED_FIFTY);
			}
			trapTiles.add(entries.value.getBoundingRectangleTiles());
		}
		
		spriteBatch.setBlendFunction(src, dst);
		
		if (map.getGroundLayers().size < 1) {
			spriteBatch.end();
			return;
		}
		
		// render black tiles on those parts of the trap polygons that are not visible
		TiledMapTileLayer layer = map.getGroundLayers().get(0);
		float tileWidth = getTileWidth(layer);
		float tileHeight = getTileHeight(layer);
		float color = Color.toFloatBits(0f,0f,0f,1f);
		TextureRegion texture = map.getSolidTileTexture();
		for (int[] tiles : trapTiles) {
			for (int i = 0; i < tiles.length; i += 2) {
				if (!map.shouldRenderTile(tiles[i], tiles[i+1])) {
					renderTile(delta, layer, tiles[i], tiles[i+1], color, tileWidth, tileHeight, texture);
				}
			}
			
		}
		spriteBatch.end();
	}

	/**
	 * Draws the selection rectangle using the supplied renderer if selection is
	 * currently in progress.
	 * 
	 * @param renderer
	 */
	private void renderSelectionRectangle() {

		Rectangle selection = gameState.getPlayerCharacterController()
				.getSelectionRectangle();
		if (selection != null
				&& Math.abs(selection.getHeight()) > Configuration
						.getSelectionTolerance()
				&& Math.abs(selection.getWidth()) > Configuration
						.getSelectionTolerance()) {
			// need to call the setter to make sure the matrix gets marked as dirty
			shapeRenderer.setProjectionMatrix(shapeRenderer
					.getProjectionMatrix().setToOrtho2D(0, 0,
							Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.rect(selection.x, selection.y, selection.width,
					selection.height);
			shapeRenderer.end();
		}
	}

	/**
	 * Draws the door the player is currently hovering his mouse over.
	 * 
	 * @param renderer
	 */
	private void renderShapes(float deltaTime) {
		if (UIManager.isAnythingOpen()) {
			return;
		}
		shapeRenderer.setProjectionMatrix(map.getCamera().combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE);
		for (ShapeDrawer doorTransition : map.getShapeDrawers()) {
			if (doorTransition.shouldDrawShape()) {
				doorTransition.drawShape(shapeRenderer, deltaTime);
			}
		}
		shapeRenderer.end();
	}

	private void renderPaths(Batch batch) {
		CombatPath combatPath = controller.getCombatPath();
		TargetType effectTarget = controller.getEffectTarget();
		if (combatPath == null && effectTarget == null) {
			return;
		}

		batch.begin();
		combatPathRenderer.draw(map, batch, 0.5f);
		effectTargetRenderer.draw(map, batch, 0.5f);
		batch.end();
	}

	private void renderDrawables(float deltaTime) {
		if (GameState.isPaused()) {
			deltaTime = 0;
		}

		Array<Drawable> drawables = sortDrawables(map.getDrawables());
		highlightedDrawables.clear();

		PlayerCharacterController pcg = gameState.getPlayerCharacterController();
		float mouseX = pcg.getMouseTileX();
		float mouseY = pcg.getMouseTileY();
		for (int i = 0; i < drawables.size; ++i) {
			Drawable drawable = drawables.get(i);
			if (drawable.shouldDraw(cullingRectangle)) {
				spriteBatch.setColor(drawable.getColor());
				drawable.draw(this, deltaTime);
				Color hightlightColor = drawable.getHighlightColor(mouseX, mouseY);
				if (hightlightColor != null) {
					highlightedDrawables.put(drawable, hightlightColor);
				}
			}
		}
	}

	private void renderTexts(float deltaTime) {
		textBatch.begin();
		Array<TextDrawer> textDrawers = map.getTextDrawers();
		for (int i = 0; i < textDrawers.size; ++i) {
			TextDrawer text = textDrawers.get(i);
			Position textPosition = text.position();
			if (text.shouldDrawText()
					&& map.isTileVisibleToPC((int) textPosition.getX(),
							(int) textPosition.getY())) {
				text.drawText(textBatch, deltaTime);
			}
		}
		textBatch.end();
	}

	protected void renderTileLayers(float delta, Array<TiledMapTileLayer> layers) {
		for (MapLayer layer : layers) {
			if (layer.isVisible()) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer(delta, (TiledMapTileLayer) layer);
				}
			}
		}
	}

	protected void renderLayersExcludingSpecial(float delta) {
		for (MapLayer layer : map.getTiledMap().getLayers()) {

			boolean groundLayer = layer.getProperties().get(
					GameMapLoader.PROPERTY_GROUND_LAYER) != null;
			boolean collisionLayer = layer.getProperties().get(
					GameMapLoader.PROPERTY_COLISIONS_LAYER) != null;
			boolean objectLayer = layer.getProperties().get(
					GameMapLoader.PROPERTY_OBJECT_LAYER) != null;
			boolean overheadLayer = layer.getProperties().get(
					GameMapLoader.PROPERTY_OVERHEAD_LAYER) != null;
			boolean alwaysAboveLayer = layer.getProperties().get(
					GameMapLoader.PROPERTY_ALWAYSABOVE_LAYER) != null;

			if (groundLayer || collisionLayer || objectLayer || overheadLayer || alwaysAboveLayer) {
				continue;
			}
			if (layer.isVisible()) {
				if (layer instanceof TiledMapTileLayer) {
					renderTileLayer(delta, (TiledMapTileLayer) layer);
				}
			}
		}
	}

	public void computeCullingRectangle() {
		Camera camera = map.getCamera();
		Vector3 tempVector = MathUtil.getVector3();
		tempVector.set(0, 0, 0);
		camera.unproject(tempVector);
		map.projectToTiles(tempVector);
		int x = (int) MathUtils.clamp(tempVector.x, 0, map.getMapWidth() - 1);

		tempVector.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0);
		camera.unproject(tempVector);
		map.projectToTiles(tempVector);
		int width = (int) MathUtils.clamp(tempVector.x, 0,
				map.getMapWidth() - 1) - x;

		tempVector.set(0, Gdx.graphics.getHeight(), 0);
		camera.unproject(tempVector);
		map.projectToTiles(tempVector);
		int y = (int) MathUtils.clamp(tempVector.y, 0, map.getMapHeight() - 1);

		tempVector.set(Gdx.graphics.getWidth(), 0, 0);
		camera.unproject(tempVector);
		map.projectToTiles(tempVector);
		int height = (int) MathUtils.clamp(tempVector.y, 0,
				map.getMapHeight() - 1) - y;

		MathUtil.freeVector3(tempVector);

		getCullingRectangle().set(x - 1, y - 1, width + 2, height + 2);
	}

	public abstract float getTileWidth(TiledMapTileLayer layer);
	
	public abstract float getTileHeight(TiledMapTileLayer layer);
	
	/**
	 * Renders the tile on the supplied layer, at the col, row coordinates, using the supplied color.
	 * 
	 * @param layer
	 * @param col
	 * @param row
	 * @param color
	 * @param layerTileWidth - width of the tile
	 * @param layerTileHeight - height of the tile
	 * @param texture - optional. If not null, this texture will be used instead of the tiles own texture
	 */
	public abstract void renderTile(float delta, TiledMapTileLayer layer, int col, int row,
			float color, float layerTileWidth, float layerTileHeight, TextureRegion texture);

	public abstract void renderTileLayer(float delta, TiledMapTileLayer layer);

	public Rectangle getCullingRectangle() {
		return cullingRectangle;
	}

	public void setView(OrthographicCamera camera) {
		spriteBatch.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom;
		float height = camera.viewportHeight * camera.zoom;
		viewBounds.set(camera.position.x - width / 2, camera.position.y
				- height / 2, width, height);
	}

	/**
	 * Called when the game window is resized.
	 */
	public void resize() {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		textBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		frameBuffer.dispose();
		frameBuffer = new FrameBuffer(Format.RGBA8888, width, height, false);
	}

	public PolygonSpriteBatch getSpriteBatch() {
		return spriteBatch;
	}
	
	protected abstract Array<Drawable> sortDrawables(Array<Drawable> drawables);

	public void dispose() {
		shapeRenderer.dispose();
		spriteBatch.dispose();
		textBatch.dispose();
		frameBuffer.dispose();
	}
}
