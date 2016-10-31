package mg.fishchicken.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import mg.fishchicken.FishchickenGame;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(480, 320);
        }

		@Override
		public ApplicationListener createApplicationListener() {
			// TODO Auto-generated method stub
			return new FishchickenGame();
		}
}