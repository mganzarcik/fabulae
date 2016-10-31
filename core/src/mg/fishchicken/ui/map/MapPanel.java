package mg.fishchicken.ui.map;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.ui.BorderedWindow;

import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.utils.Disposable;

public class MapPanel extends BorderedWindow implements Disposable {
	private Map map;

	public MapPanel(GameMap gameMap, GameState gameState, MapPanelStyle style) {
		super(Strings.getString(GameMap.STRING_TABLE, gameMap.getId()), style);
		
		this.map = new Map(gameMap, gameState, style.mapWidth, style.mapHeight, style.mapIndicatorStyle);
		add(map);
		pack();
	}

	@Override
	public void dispose() {
		setVisible(false);
		map.dispose();
		remove();
		clear();
	}

	static public class MapPanelStyle extends BorderedWindowStyle {
		private int mapWidth = 640, mapHeight = 480;
		private ButtonStyle mapIndicatorStyle;
	}

}
