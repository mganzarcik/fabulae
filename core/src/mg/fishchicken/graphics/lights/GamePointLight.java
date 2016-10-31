package mg.fishchicken.graphics.lights;

import mg.fishchicken.core.ColoredThing;
import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.PositionedThing;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.actions.ActionsContainer;
import mg.fishchicken.gamelogic.locations.GameMap;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.graphics.Drawable;
import box2dLight.FixtureUserData;
import box2dLight.FixtureUserData.UserDataType;
import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GamePointLight extends PointLight implements ManagedLight, ActionsContainer, PositionedThing, ColoredThing, ThingWithId {

	public static final String XML_COLOR = "color";
	public static final String XML_RADIUS = "radius";
	
	private Color maxIntensity;
	private GameObject go;
	private String id = null;
	private boolean isIsometric;
	private boolean isSunlight;
	private Array<Action> actions = new Array<Action>();
	private Position position;
	private boolean removed;
	private GameMap gameMap;
	
	/**
	 * @param rayHandler
	 * @param rays
	 * @param color - the max intensity and color of this light
	 * @param distance
	 * @param x
	 * @param y
	 */
	public GamePointLight(RayHandler rayHandler, int rays, Color color,
			float distance, GameObject go) {
		super(rayHandler, rays, color, distance, go.position().getX(), go.position().getY());
		removed = false;
		createPosition();
		this.go = go;
		isIsometric = false;
		isSunlight = false;
		if (go.getMap() != null) {
			isIsometric = go.getMap().isIsometric();
		}
		if (isIsometric) {
			setDistance((float) (distance * MathUtil.SQRT_TWO));
		}
		setEndPoints();
		setMaxIntensity(color);
	}
	
	public GamePointLight(String id, RayHandler rayHandler, GameMap gameMap, LightDescriptor descriptor, float x, float y, int rays, boolean isIsometric) {
		super(rayHandler, rays);
		removed = false;
		this.id =  id;
		this.gameMap = gameMap;
		createPosition();
		setStaticLight(true);
		setPosition(x, y);
		setMaxIntensity(new Color(descriptor.lightColor));
		setDistance(descriptor.lightRadius);
		XMLUtil.readActions(this, descriptor.actionsElement);
		setSoft(false);
		setSoftnessLenght(1);
		this.isIsometric = isIsometric;
		if (isIsometric) {
			setDistance((float) (distance * MathUtil.SQRT_TWO));
		}
		this.isSunlight = descriptor.isSunlight;
		setEndPoints();
	}
	
	@Override
	public void remove() {
		super.remove();
		removed = true;
	}
	
	public boolean wasRemoved() {
		return removed;
	}
	
	private void createPosition() {
		position = new Position() {
			@Override
			public float getX() {
				return GamePointLight.this.getX();
			}
			
			@Override
			public float getY() {
				return GamePointLight.this.getY();
			}
			
			@Override
			public void set(float x, float y) {
				setPosition(x, y);
			}
		};
	}
	
	@Override
	protected void setEndPoints() {
		float angleNum = 360f / (rayNum - 1);
		for (int i = 0; i < rayNum; i++) {
			final float angle = angleNum * i;
			sin[i] = MathUtils.sinDeg(angle);
			cos[i] = MathUtils.cosDeg(angle);
			endX[i] = distance * cos[i];
			endY[i] = isIsometric ? distance/2 * sin[i] : distance * sin[i];
		}
	}
	
	public void setId(String value) {
		this.id = value;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public Color getMaxIntensity() {
		return maxIntensity;
	}

	@Override
	public void setMaxIntensity(Color maxIntensity) {
		this.maxIntensity = maxIntensity;
	}
	
	@Override
	public void render() {
		if (go != null && (go instanceof Drawable) && !((Drawable)go).shouldDraw(null)) {
			return;
		}
		
		float deltaTime = Gdx.graphics.getDeltaTime();
		for (Action a : actions) {
			if (!a.isPaused()) {
				a.update(deltaTime);
				if (a.isFinished()) {
					removeAction(a);
				}
			}
		}
		
		super.render();
	}
	
	@Override
	public <T extends Action> T addAction(Class<T> actionClass,
			Object... parameters) {
		T action = getActionInstance(actionClass);
		actions.add(action);
		action.init(this, parameters);
		return action;
	}

	/**
	 * Remove the supplied Action from this light.
	 * 
	 * @param a
	 */
	public void removeAction(Action a) {
		if (a == null) {
			return;
		}
		a.onRemove(this);
		// remove the action from the actions list
		actions.removeValue(a, false);
	}
	
	@Override
	public Position position() {
		return position;
	}
	
	@Override
	protected RayCastCallback getRayCastCallback() {
		if (raycastCallback == null) {
			raycastCallback = new RayCastCallback() {
				@Override
				final public float reportRayFixture(Fixture fixture, Vector2 point,
						Vector2 normal, float fraction) {
					FixtureUserData userData = (FixtureUserData) fixture.getUserData();
					if (userData != null && userData.type == UserDataType.LIGHT_BLOCKER) {
						if (CoreUtil.equals(GamePointLight.this.id, userData.id)) {
							mx[m_index] = point.x;
							my[m_index] = point.y;
							f[m_index] = fraction;
							return fraction;
						}
					}
					return 1;
				}
			};
		}
		return raycastCallback;
	}
	
	private <T extends Action> T getActionInstance(Class<T> actionClass, Object... parameters) {
		try {
			return actionClass.newInstance();
		} catch (InstantiationException e) {
			throw new GdxRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public boolean isInteriorSunlight() {
		return isSunlight;
	}

	@Override
	public GameMap getMap() {
		if (go != null) {
			return go.getMap();
		}
		return gameMap;
	}
}
