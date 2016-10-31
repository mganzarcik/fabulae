package mg.fishchicken.ui;

import mg.fishchicken.ui.selectbox.SelectBoxWithSound;
import mg.fishchicken.ui.selectbox.SelectOption;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class ModalEnabledSelectBox<T> extends SelectBoxWithSound<SelectOption<T>> {
	
	public ModalEnabledSelectBox(SelectBoxWithSoundStyle style) {
		super(style);
	}
	
	@Override
	public boolean isAscendantOf(Actor actor) {
		if (actor instanceof List) {
			if (getScrollPane() == actor.getParent()) {
				return true;
			}
		} else if (actor instanceof ScrollPane) {
			if (getScrollPane() == actor) {
				return true;
			}
		}
		return super.isAscendantOf(actor);
	}

	
	

}
