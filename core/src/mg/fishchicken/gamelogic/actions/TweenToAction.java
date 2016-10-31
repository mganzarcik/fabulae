package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.tweening.PositionedThingTweenAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the supplied PositionedThing tween to the supplied coordinates using
 * the defined tweening equation and duration.
 * 
 * Parameters:
 * <ol>
 * 		<li> x - float - x coordinate of the target
 * 		<li> y - float - y coordinate of the target
 * 		<li> speed - float - how many distance units should the thing traverse in once second
 * 		<li> delay - float - the delay in seconds with witch the tween should start. Optional, default is 0
 * </ol>
 * @author Annun
 * @see aurelienribon.tweenengine.TweenEquation
 *
 */
public class TweenToAction extends BasicAction  implements TweenCallback {

	private float targetX, targetY, speed, delay;
	private boolean tweenStarted, tweenFinished;
	private PositionedThing pt;
	private ActionsContainer ac;
	private Tween tween;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof PositionedThing)) {
			throw new GdxRuntimeException("TweenToAction only works on PositionedThing!");
		}
		this.ac = ac;
		this.pt = (PositionedThing)ac;
		tweenFinished = false;
		tweenStarted = false;
		targetX = (Float) parameters[0];
		targetY = (Float) parameters[1];
		speed = (Float) parameters[2];
		delay = 0;
		tween = null;
		if (parameters.length > 3) {
			delay = (Float) parameters[3];
		}
	}
	

	@Override
	public void reset() {
		init(ac, targetX, targetY, speed);
	}

	@Override
	public void update(float deltaTime) {
		if (delay > 0) {
			delay -= deltaTime;
		}
		if (!tweenStarted && delay <= 0) {
			tweenStarted = true;
			float duration = MathUtil.distance(pt.position().getX(), pt.position().getY(), targetX, targetY) / speed;
			tween = Tween.to(pt, PositionedThingTweenAccessor.XY, duration)
					.ease(TweenEquations.easeOutSine)
					.target(targetX, targetY)
					.setCallback(this)
					.start(gameState.getCurrentMap().getTweenManager(false));
		}
	}
	
	@Override
	public void onRemove(ActionsContainer ac) {
		super.onRemove(ac);
		if (tween != null) {
			tween.free();
			tween = null;
		}
	}

	@Override
	public boolean isFinished() {
		return tweenFinished;
	}
	
	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.targetX = actionElement.getInt(XML_ATTRIBUTE_X);
		this.targetY = actionElement.getFloat(Action.XML_ATTRIBUTE_Y);
		this.speed = actionElement.getInt(Action.XML_ATTRIBUTE_SPEED);
		this.delay = actionElement.getFloat(Action.XML_ATTRIBUTE_DELAY, 0);
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_X, targetX)
			.attribute(XML_ATTRIBUTE_Y, targetY)
			.attribute(XML_ATTRIBUTE_SPEED, speed)
			.attribute(XML_ATTRIBUTE_DELAY, delay);
	}

	@Override
	public void onEvent(int type, BaseTween<?> source) {
		if (type == TweenCallback.COMPLETE) {
			tweenFinished = true;
		}
	}
}
