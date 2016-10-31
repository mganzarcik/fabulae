package mg.fishchicken.graphics.lights;

import com.badlogic.gdx.graphics.Color;

public interface ManagedLight {

	public Color getMaxIntensity();
	public void setMaxIntensity(Color maxIntensity);
	public boolean isInteriorSunlight();
}
