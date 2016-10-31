package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.tweening.PositionedThingTweenAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the supplied PositionedThing wiggle in the supplied radius.
 * 
 * Parameters:
 * <ol>
 * 		<li> duration - int - duration of the wiggle in seconds. If negative, wiggle will never end. Default is -1
 * 		<li> radius - int - in what radius should the wiggle occur. In pixels. Default is 10.
 * 		<li> speed - float - how many pixels per second can the PositionedThing traverse. Default is 10.
 * </ol>
 * @author Annun
 *
 */
public class WiggleAction extends BasicAction  implements TweenCallback {

	private float radius;
	private float centerX, centerY, speed;
	private float stateTime = 0;
	private int duration = 1;
	private boolean tweenFinished;
	private PositionedThing pt;
	private ActionsContainer ac;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof PositionedThing)) {
			throw new GdxRuntimeException("WiggleAction only works on PositionedThing!");
		}
		this.ac = ac;
		this.pt = (PositionedThing)ac;
		
		centerX = pt.position().getX();
		centerY = pt.position().getY();
		stateTime = 0;
		duration = -1;
		tweenFinished = true;
		
		GameMap map = pt.getMap();
		if (map != null) {
			float tileSize = (map.getTileSizeX() + map.getTileSizeY()) / 2;
			radius = 10f/tileSize;
			speed = 10f/tileSize;
		} else {
			radius = 0.3f;
			speed = 0.3f;
		}
		
		if (parameters.length > 0) { 
			radius = (Integer) parameters[0];
		}
		if (parameters.length > 1) { 
			speed = (Integer) parameters[1];
		}
		if (parameters.length > 2) {
			duration = (Integer) parameters[0];
		}
	}
	

	@Override
	public void reset() {
		init(ac, radius, speed, duration);
	}

	@Override
	public void update(float deltaTime) {
		GameMap map = pt.getMap();
		if (tweenFinished && map != null) {
			tweenFinished = false;
			float pixelRadius = radius / ((map.getTileSizeX() + map.getTileSizeY()) / 2);
			Tween.to(pt, PositionedThingTweenAccessor.XY, radius / speed)
					.ease(TweenEquations.easeOutSine)
					.target(MathUtils.random(centerX - pixelRadius, centerX + pixelRadius),
							MathUtils.random(centerY - pixelRadius, centerY + pixelRadius))
					.setCallback(this)
					.start(map.getTweenManager(false));
		}
	}

	@Override
	public boolean isFinished() {
		if (duration == -1) {
			return false;
		}
		return stateTime > duration;
	}
	
	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.radius = actionElement.getInt(XML_ATTRIBUTE_RADIUS, 10);
		this.speed = actionElement.getFloat(Action.XML_ATTRIBUTE_SPEED, 10);
		this.duration = actionElement.getInt(Action.XML_ATTRIBUTE_DURATION, -1);
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_RADIUS, radius)
			.attribute(XML_ATTRIBUTE_SPEED, speed)
			.attribute(XML_ATTRIBUTE_DURATION, duration);
	}

	@Override
	public void onEvent(int type, BaseTween<?> source) {
		if (type == TweenCallback.COMPLETE) {
			tweenFinished = true;
		}
	}
}
