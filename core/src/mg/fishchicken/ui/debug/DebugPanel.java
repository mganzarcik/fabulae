package mg.fishchicken.ui.debug;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.checkbox.CheckboxWithSound;
import mg.fishchicken.ui.checkbox.CheckboxWithSound.CheckBoxWithSoundStyle;
import mg.fishchicken.ui.debug.GameObjectViewer.GameObjectViewerStyle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectSet;

public class DebugPanel extends BorderedWindow  {

	private Label fps, memoryJava, memoryNative, assetCount, position, date, temperature, selectedGos;
	private GameState gameState;
	private Vector3 tempVector;
	private DebugPanelStyle style;
	private CheckboxWithSound losDebugCheckbox, lightsDebugCheckbox, consoleCheckbox, inspectorCheckbox;
	private InputListener goInspectorListener;
	private GameObjectViewer goViewer;
	
	public DebugPanel(GameState gameState, DebugPanelStyle style) {
		super(Strings.getString(UIManager.STRING_TABLE, "debugWindow"), style);
		this.gameState = gameState;
		this.style = style;
		tempVector = new Vector3();
		goInspectorListener = new GoInspectorListerer();
		goViewer = new GameObjectViewer(style.viewerStyle);
		build();
	}
	
	private void build() {
		fps = new Label("", style.textStyle);
		memoryJava = new Label("", style.textStyle);
		memoryNative = new Label("", style.textStyle);
		position = new Label("", style.textStyle);
		date = new Label("", style.textStyle);
		temperature = new Label("", style.textStyle);
		assetCount = new Label("", style.textStyle);

		add(new Label(Strings.getString(UIManager.STRING_TABLE, "fps"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		add(fps).top().left().fill().expandX();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "memoryJava"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		add(memoryJava).top().left().fill().expandX();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "memoryNative"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		add(memoryNative).top().left().fill().expandX();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "assetCount"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		add(assetCount).top().left().fill().expandX();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "position"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		add(position).top().left().fill().expandX();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "date"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		add(date).top().left().fill().expandX();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "temperature"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		add(temperature).top().left().fill().expandX();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "losDebug"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		losDebugCheckbox = new CheckboxWithSound("", style.checkboxStyle);
		add(losDebugCheckbox).top().left();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "lightsDebug"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		lightsDebugCheckbox = new CheckboxWithSound("", style.checkboxStyle);
		add(lightsDebugCheckbox).top().left();
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "console"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		consoleCheckbox = new CheckboxWithSound("", style.checkboxStyle);
		add(consoleCheckbox).top().left();
		consoleCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (consoleCheckbox.isChecked()) {
					UIManager.displayDebugConsole();
				} else {
					UIManager.hideDebugConsole();
				}
			}
		});
		row();
		add(new Label(Strings.getString(UIManager.STRING_TABLE, "goInspector"), style.labelStyle)).top().left().fill().padRight(style.labelMarginRight);
		inspectorCheckbox = new CheckboxWithSound("", style.checkboxStyle);
		add(inspectorCheckbox).top().left();
		inspectorCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (inspectorCheckbox.isChecked()) {
					Stage stage = getStage();
					stage.addListener(goInspectorListener);
					stage.addActor(goViewer);
					WindowPosition.THREE_QUARTERS_X.position(goViewer);
				} else {
					Stage stage = getStage();
					stage.removeListener(goInspectorListener);
					goViewer.remove();
				}
			}
		});
		row();
		add();
		selectedGos = new Label("", style.labelStyle);
		add(selectedGos).top().left();
		row();
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		
		GameMap map = gameState.getCurrentMap();
		if (map == null || map.getCamera() == null) {
			return;
		}
		
		tempVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		map.getCamera().unproject(tempVector);
		float camX = tempVector.x;
		float camY = tempVector.y;
		map.projectToTiles(tempVector);
		fps.setText(Integer.toString(Gdx.graphics.getFramesPerSecond()));
		memoryJava.setText(Long.toString(Gdx.app.getJavaHeap()/1024/1024));
		memoryNative.setText(Long.toString(Gdx.app.getNativeHeap()/1024/1024));
		position.setText(MathUtil.toUIString(tempVector.x) + ", " + MathUtil.toUIString(tempVector.y) + " ("
				+ MathUtil.toUIString(camX) + ", " + MathUtil.toUIString(camY) + ")");
		date.setText(GameState.getCurrentGameDate().toString());
		temperature.setText(Integer.toString(GameState.getCurrentTemperature()));
		assetCount.setText(Integer.toString(Assets.getAssetManager().getLoadedAssets()));
		Configuration.setRenderLightsDebug(lightsDebugCheckbox.isChecked());
		Configuration.setRenderLOSDebug(losDebugCheckbox.isChecked());
		if (getWidth() < getPrefWidth()) {
			pack();
		}
	}
	
	public void updatePosition(int screenWidth, int screenHeight) {
		setY(style.y);
		setX(style.x);
	}
	
	public static class DebugPanelStyle extends BorderedWindowStyle {
		private LabelStyle textStyle, labelStyle;
		private CheckBoxWithSoundStyle checkboxStyle;
		private GameObjectViewerStyle viewerStyle;
		private int x, y, labelMarginRight;
	}

	private class GoInspectorListerer extends InputListener {
		private ObjectSet<GameObject> foundGos = new ObjectSet<GameObject>();
		private Vector2 tempVector = new Vector2();
		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			GameMap map = gameState.getCurrentMap();
			if (map == null) {
				return false;
			}
			tempVector.set(x, y);
			getStage().stageToScreenCoordinates(tempVector);
			DebugPanel.this.tempVector.set(tempVector.x, tempVector.y, 0);
			map.getCamera().unproject(DebugPanel.this.tempVector);
			map.projectToTiles(DebugPanel.this.tempVector);
			foundGos.clear();
			map.getAllGameObjectsAt(foundGos, DebugPanel.this.tempVector.x, DebugPanel.this.tempVector.y, false, true);
			if (foundGos.size > 0) {
				goViewer.setGo(foundGos.first());
			}
			return true;
		}
	}
}
