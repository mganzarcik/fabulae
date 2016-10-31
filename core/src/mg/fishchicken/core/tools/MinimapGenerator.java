package mg.fishchicken.core.tools;

import java.nio.ByteBuffer;

import mg.fishchicken.core.GameState;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.graphics.renderers.GameMapRenderer;
import mg.fishchicken.graphics.renderers.IsometricMinimapRenderer;
import mg.fishchicken.graphics.renderers.OrthogonalMinimapRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;

public class MinimapGenerator {
 
	/**
	 * Returns an UNMANAGED texture that of the supplied size that
	 * contains a map of the supplied game map.
	 * 
	 * @param map
	 * @param gameState
	 * @param width
	 * @param height
	 * @return
	 */
	public static Texture generate(GameMap map, GameState gameState, int width, int height) {

		boolean isIsometric = map.isIsometric();
		GameMapRenderer renderer = isIsometric ? new IsometricMinimapRenderer(gameState, map) : new OrthogonalMinimapRenderer(gameState, map);
		
		FrameBuffer fb = new FrameBuffer(Format.RGB565, width, height, false);
		float cameraWidth = map.getWidth();
		float cameraHeight = map.getHeight();
		if (isIsometric) {
			cameraWidth = cameraHeight = (float)(Math.sqrt(cameraWidth*cameraWidth + cameraHeight*cameraHeight) * Math.sqrt(2));
			cameraHeight /= 2;
		}
		
		float cameraPositionX = cameraWidth / 2;
		float cameraPositionY = isIsometric ? 0 : cameraHeight / 2;
		
		int tileSizeX = (int) map.getTileSizeX();
		int tileSizeY = (int) map.getTileSizeY();
		if (cameraWidth * tileSizeX < width || cameraHeight * tileSizeY < height) {
			cameraWidth = (float) width / tileSizeX;
			cameraHeight = (float) height / tileSizeY;
		}
        OrthographicCamera camera = new OrthographicCamera(cameraWidth, cameraHeight);
        camera.position.x = cameraPositionX;
        camera.position.y = cameraPositionY;
        camera.update();
		
		fb.begin();
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		renderer.render(0, camera);
		byte[] bytes = ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);
		fb.end();
		
		fb.dispose();
		renderer.dispose();
		
		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		ByteBuffer pixels = pixmap.getPixels();
		pixels.clear();
		pixels.put(bytes);
		pixels.position(0);
		
		Texture returnValue = new Texture(pixmap);
		pixmap.dispose();
		return returnValue;
		
	}
}
