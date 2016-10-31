package mg.fishchicken.graphics.animations;

import java.io.IOException;

import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.graphics.models.Model;

public class ItemAnimationMap extends CharacterAnimationMap {

	public ItemAnimationMap(Model model, AudioProfile audioProfile, float speed) throws IOException {
		super(model, audioProfile, speed);
	}
	
	public ItemAnimationMap(CharacterAnimationMap toCopy) {
		super(toCopy);
	}
	
	@Override
	protected boolean isFailFast() {
		return false;
	}

}
