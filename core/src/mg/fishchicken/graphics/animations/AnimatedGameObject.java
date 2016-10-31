package mg.fishchicken.graphics.animations;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.util.MathUtil;
import mg.fishchicken.graphics.Drawable;
import mg.fishchicken.graphics.renderers.GameMapRenderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class AnimatedGameObject extends GameObject implements Drawable {

	private float s_stateTime = 0;;
	
	private Animation animation; 
	private float xOffset, yOffset;
	private boolean s_playingAnimation = false;
	
	/**
	 * Empty constructor for game loading.
	 */
	public AnimatedGameObject() {
		super();
	}
	
	public AnimatedGameObject(String id,String type) {
		super(id, type);
	}
	
	@Override
	public boolean contains(float x, float y) {
		boolean quickCheck = super.contains(x, y);
		if (quickCheck) {
			return quickCheck;
		}
		Animation animation = getAnimation();
		if (animation != null) {
			Rectangle bounds = animation.getBounds(s_stateTime);
			if (bounds != null) {
				Vector2 projectedCoordinates = MathUtil.getVector2().set(x, y);
				getMap().projectFromTiles(projectedCoordinates);
				float scaleX = getScaleX();
				float scaleY = getScaleY();
				float startX = getXCamera()+getXOffset()+(bounds.getX()*scaleX);
				float startY = getYCamera()+getYOffset()+(bounds.getY()*scaleY);
				float width = bounds.getWidth()*scaleX;
				float height = bounds.getHeight()*scaleY;
				
				boolean returnValue =  startX <= projectedCoordinates.x && startX + width > projectedCoordinates.x && 
						startY <= projectedCoordinates.y && startY + height> projectedCoordinates.y;
				
				MathUtil.freeVector2(projectedCoordinates);
				
				return returnValue;
			}
		}
		return quickCheck;
	}
	
	public float getScaleX() {
		if (getMap() != null) {
			return getMap().getScaleX();
		}
		return 1;
	}
	
	public float getScaleY() {
		if (getMap() != null) {
			return getMap().getScaleY();
		}
		return 1;
	}
	
	public Animation getAnimation() {
		return animation;
	}
	
	public void setAnimation(Animation animation) {
		setAnimation(animation,0);
	}
	
	public void setAnimation(Animation animation, float stateTime) {
		this.animation = animation;
		s_stateTime = stateTime;
		if (animation != null) {
			animation.resetSounds();
		}
	}
	
	public void setPlayMode(int playMode) {
		if (animation != null) {
			animation.setPlayMode(playMode);
		}
	}
	
	public void setPlayingAnimation(boolean play) {
		s_playingAnimation = play;
	}
	
	public void resetAnimation() {
		s_stateTime = 0;
	}
	/**
	 * Sets the coordinate offsets that should be used when drawing the
	 * animations.
	 * 
	 * @param xOffset
	 * @param yOffset
	 */
	public void setOffsets(float xOffset, float yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	
	public float getXOffset() {
		return xOffset;
	}
	
	public float getYOffset() {
		return yOffset;
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (s_playingAnimation) {
			s_stateTime += deltaTime;
		}
	}
	
	@Override
	public void draw(GameMapRenderer renderer, float deltaTime) {
		Animation animation = getAnimation();
		
		if (animation == null) {
			return;
		}
		
		if (s_playingAnimation) {
			animation.playSounds(s_stateTime, this);
		}
		
		Vector2 projectedCoordinates = position.setVector2(MathUtil.getVector2());
		getMap().projectFromTiles(projectedCoordinates);
		
		renderer.getSpriteBatch().draw(animation.getKeyFrame(s_stateTime),
				projectedCoordinates.x+getXOffset(), projectedCoordinates.y+getYOffset(), animation.getFrameWidth()
						* getScaleX(), animation.getFrameHeight() * getScaleY());
		MathUtil.freeVector2(projectedCoordinates);
	}

	@Override
	public boolean shouldDraw(Rectangle cullingRectangle) {
		if (getMap() != null) {
			if (!getMap().isCurrentMap()) {
				return false;
			}
			if (cullingRectangle != null && !cullingRectangle.contains(position.getX(), position.getY())) {
				return false;
			}
			return isVisibleToPC();
		}
		return true;
	}

	@Override
	public boolean isAlwaysBehind() {
		return false;
	}
	
	@Override
	public boolean isAlwaysInFront() {
		return false;
	}
	
	/**
	 * Whether the currently playing animation is finished. Always return false for looped animations. 
	 * Will also return true if currently not animating.
	 * @return
	 */
	public boolean isAnimationFinished() {
		return getMap() == null || getAnimation() == null || !s_playingAnimation || getAnimation().isAnimationFinished(s_stateTime);
	}
	
	
	public float getAnimationStateTime() {
		return s_stateTime;
	}
	
	@Override
	public float getZIndex() {
		return -getYCamera();
	}
	
	@Override
	public Color getHighlightColor(float x, float y) {
		return null;
	}
	
	@Override
	public int getHighlightAmount(float x, float y) {
		return 0;
	}
}
