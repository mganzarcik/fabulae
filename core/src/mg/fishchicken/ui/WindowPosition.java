package mg.fishchicken.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public enum WindowPosition {

	QUARTER_X {
		public void position(Actor window) {
			Stage stage = window.getStage();
			if (stage == null) {
				return;
			}
			window.setX(stage.getWidth()/4-window.getWidth()/4);
			window.setY(stage.getHeight()/2 - window.getHeight()/2);
		};
	},
	
	CENTER {
		public void position(Actor window) {
			Stage stage = window.getStage();
			if (stage == null) {
				return;
			}
			window.setX(stage.getWidth()/2 - window.getWidth()/2);
			window.setY(stage.getHeight()/2 - window.getHeight()/2);
		};
	},
	
	THREE_QUARTERS_X {
		public void position(Actor window) {
			Stage stage = window.getStage();
			if (stage == null) {
				return;
			}
			window.setX((3*stage.getWidth())/4 - window.getWidth()/2);
			window.setY(stage.getHeight()/2 - window.getHeight()/2);
		};
	};
	
	public abstract void position(Actor window);
}
