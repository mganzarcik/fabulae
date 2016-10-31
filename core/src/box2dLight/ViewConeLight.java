package box2dLight;

import mg.fishchicken.core.OrientedThing;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.tweening.ViewConeTweenAccessor;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import box2dLight.FixtureUserData.UserDataType;
import box2dLight.RaycastCallbackHandler.CollisionInfo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ViewConeLight extends ConeLight implements OrientedThing {

	private AbstractGameCharacter character;
	private boolean isIsometric;
	protected RaycastCallbackHandler raycastCallbackHandler = null;
	private Vector2 tmpEnd;
	private Orientation orientation;
	private Tween viewConeTween;

	public ViewConeLight(AbstractGameCharacter character, GameMap map,
			int rays, Color color, float distance, float coneDegree) {
		super(map.getViewConesRayHandler(), rays, color, distance, character
				.position().getX(), character.position().getY(),
				MathUtil.normalizeDegrees(-(character.getOrientation()
						.getDegrees() - 90)), coneDegree);
		this.character = character;
		isIsometric = map.isIsometric();
		orientation = character.getOrientation();
		if (isIsometric) {
			setDistance((float) (distance * MathUtil.SQRT_TWO));
		}
		setSoft(false);
		// have to do this again, since isometric was not set when super first
		// called it
		setDirection(MathUtil.normalizeDegrees(getDirection()));
		super.setStaticLight(false);
	}

	@Override
	public void setStaticLight(boolean staticLight) {
		throw new GdxRuntimeException("View cone lights cannot be static!");
	}
	
	@Override
	public void render() {
		if (character.belongsToPlayerFaction() || !character.isActive()
				|| !character.shouldDraw(null)) {
			return;
		}
		super.render();
	}

	public void setOrientation(Orientation o) {
		setDirection(MathUtil.normalizeDegrees(getDirection()));
		int currentDegrees = (int) MathUtil.normalizeDegrees(-(orientation
				.getDegrees() - 90));
		int newDegrees = (int) MathUtil
				.normalizeDegrees(-(o.getDegrees() - 90));

		if ((newDegrees >= 270 && currentDegrees <= 45)) {
			newDegrees = newDegrees - 360;
		} else if (currentDegrees >= 270 && newDegrees <= 45) {
			newDegrees = newDegrees + 360;
		}
		if (viewConeTween != null && !viewConeTween.isFinished()) {
			viewConeTween.free();
		}
		viewConeTween = Tween.to(this, ViewConeTweenAccessor.DIRECTION, 0.2f)
				.target(newDegrees).ease(TweenEquations.easeInSine)
				.start();
		orientation = o;
	}

	public void setDirection(float direction) {

		this.direction = direction;
		for (int i = 0; i < rayNum; i++) {
			float angle = direction + coneDegree - 2f * coneDegree * i
					/ (rayNum - 1f);
			final float s = sin[i] = MathUtils.sinDeg(angle);
			final float c = cos[i] = MathUtils.cosDeg(angle);
			endX[i] = distance * c;
			endY[i] = isIsometric ? distance / 2 * s : distance * s;
		}
	}

	@Override
	public void attachToBody(Body body, float offsetX, float offSetY) {
		// does nothing
	}

	@Override
	public void update() {
		if (viewConeTween != null) {
			viewConeTween.update(Gdx.graphics.getDeltaTime());
			if (viewConeTween.isFinished()) {
				viewConeTween.free();
				viewConeTween = null;
			}
		}
		if (rayHandler.culling) {
			culled = ((!rayHandler.intersect(start.x, start.y, distance
					+ softShadowLenght)));
			if (culled)
				return;
		}

		if (tmpEnd == null) {
			tmpEnd = new Vector2();
		}

		for (int i = 0; i < rayNum; i++) {
			m_index = i;
			f[i] = 1f;
			tmpEnd.x = endX[i] + start.x;
			mx[i] = tmpEnd.x;
			tmpEnd.y = endY[i] + start.y;
			my[i] = tmpEnd.y;
			if (rayHandler.world != null && !xray) {
				getRayCastCallback().reset();
				rayHandler.world.rayCast(getRayCastCallback(), start, tmpEnd);
				getRayCastCallback().collisions.sort(CollisionFractionComparator.singleton());
				for (CollisionInfo ci : getRayCastCallback().collisions) {
					FixtureUserData userData = (FixtureUserData) ci.fixture
							.getUserData();
					if (userData.type == UserDataType.LOS_BLOCKER_TILE
							|| userData.type == UserDataType.LOS_BLOCKER_POLYGON_GROUND) {
						mx[m_index] = ci.point.x;
						my[m_index] = ci.point.y;
						f[m_index] = ci.fraction;
						break;
					}
				}
			}
		}
		setMesh();
	}

	@Override
	protected void setMesh() {
		// this is overridden, because we do not want the view cones to fade out
		// with distance,
		// so we set the "s" parameters for the shader to be a constant 1,
		// instead of being distance dependent
		// ray starting point
		int nonSoftIndex = 0;
		int softIndex = 0;

		nonSoftSegments[nonSoftIndex++] = start.x;
		nonSoftSegments[nonSoftIndex++] = start.y;
		nonSoftSegments[nonSoftIndex++] = colorF;
		nonSoftSegments[nonSoftIndex++] = 1;
		// rays ending points.
		for (int i = 0; i < rayNum; i++) {
			nonSoftSegments[nonSoftIndex++] = mx[i];
			nonSoftSegments[nonSoftIndex++] = my[i];
			nonSoftSegments[nonSoftIndex++] = colorF;
			nonSoftSegments[nonSoftIndex++] = 1;
			if (soft && !xray) {
				softSegments[softIndex++] = mx[i];
				softSegments[softIndex++] = my[i];
				softSegments[softIndex++] = colorF;

				softSegments[softIndex++] = 1;
				softSegments[softIndex++] = mx[i] + softShadowLenght * cos[i];
				softSegments[softIndex++] = my[i] + softShadowLenght * sin[i];
				softSegments[softIndex++] = zero;
				softSegments[softIndex++] = 0f;
			}
		}
		lightMesh.setVertices(nonSoftSegments, 0, nonSoftIndex);

		if (soft && !xray) {
			softShadowMesh.setVertices(softSegments, 0, softIndex);;
		}		
	}

	public float getDirection() {
		return direction;
	}

	@Override
	protected RaycastCallbackHandler getRayCastCallback() {
		if (raycastCallbackHandler == null) {
			raycastCallbackHandler = new RaycastCallbackHandler();
		}
		return raycastCallbackHandler;
	}

}
