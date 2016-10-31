package mg.fishchicken.gamelogic.actions;

import java.io.IOException;

import mg.fishchicken.core.ColoredThing;
import mg.fishchicken.tweening.ColorTweenAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * Makes the supplied ColoredThing fade out. If the thing is already faded out, it will be faded in.
 * 
 * Parameters:
 * <ol>
 * 		<li> duration - float - how many seconds should the fading take
 * 		<li> loop - boolean- if true, the ColoredThing will keep fading in and out in an endless loop. Optional, default is false
 * 		<li> delay - float - the delay in seconds with witch the tween should start. Optional, default is 0
 * 		<li> target - float - the target alpha value. Optional, default is 0 or 1, depending on the current alpha
 * </ol>
 * @author Annun
 * @see aurelienribon.tweenengine.TweenEquation
 *
 */
public class FadeAction extends BasicAction  implements TweenCallback {

	private float duration, originalAlpha, delay, target;
	private boolean loop;
	private boolean tweenStarted, tweenFinished;
	private ColoredThing ct;
	private ActionsContainer ac;
	private Tween tween;
	
	@Override
	public void init(ActionsContainer ac, Object... parameters) {
		if (!(ac instanceof ColoredThing)) {
			throw new GdxRuntimeException("FadeAction only works on ColoredThing!");
		}
		this.ac = ac;
		this.ct = (ColoredThing)ac;
		tweenFinished = false;
		tweenStarted = false;
		tween = null;
		originalAlpha = ct.getColor().a;
		if (parameters.length > 0) {
			duration = (Float) parameters[0];
			loop = false;
			if (parameters.length > 1) {
				loop = (Boolean) parameters[1];
			}
			delay = 0;
			if (parameters.length > 2) {
				delay = (Float) parameters[2];
			}
			target = 0;
			if (parameters.length > 3) {
				target = (Float) parameters[3];
			}
		}
		if (originalAlpha <= target) {
			originalAlpha = 1;
		}
	}
	

	@Override
	public void reset() {
		init(ac, duration, loop, delay);
	}

	@Override
	public void update(float deltaTime) {
		if (delay > 0) {
			delay -= deltaTime;
		}
		if (!tweenStarted && delay <= 0) {
			tweenStarted = true;
			createTween();
		}
		if (tweenFinished && loop) {
			tweenFinished = false;
			createTween();
		}
	}

	@Override
	public boolean isFinished() {
		return tweenFinished && !loop;
	}
	
	@Override
	public boolean isBlockingInCombat() {
		return false;
	}

	@Override
	public void readAndValidateParamateresFromXML(Element actionElement) {
		this.duration = actionElement.getInt(Action.XML_ATTRIBUTE_DURATION);
		this.delay = actionElement.getFloat(Action.XML_ATTRIBUTE_DELAY, 0);
		this.target = actionElement.getFloat(Action.XML_ATTRIBUTE_TARGET, 0);
		this.loop = actionElement.getBoolean(Action.XML_ATTRIBUTE_LOOP, false);
	}

	@Override
	public void writeParametersToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_ATTRIBUTE_LOOP, loop)
			.attribute(XML_ATTRIBUTE_DURATION, duration)
			.attribute(XML_ATTRIBUTE_DELAY, delay)
			.attribute(XML_ATTRIBUTE_TARGET, target);
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
	public void onEvent(int type, BaseTween<?> source) {
		if (type == TweenCallback.COMPLETE) {
			tweenFinished = true;
			tween = null;
		}
	}
	
	private void createTween() {
		float targetToUse = (ct.getColor().a <= target ? originalAlpha : target);
		tween = Tween.to(ct.getColor(), ColorTweenAccessor.ALPHA, duration)
			.ease(TweenEquations.easeOutSine)
			.target(targetToUse)
			.setCallback(this)
			.start(gameState.getCurrentMap().getTweenManager(false));
	}
}
