package mg.fishchicken.gamestate.characters;

import java.io.IOException;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamestate.ObservableState;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DamageInfo extends ObservableState<DamageInfo, DamageInfo> {

	private float s_damage;
	private float s_delay;
	private GameObject s_source;
	
	public DamageInfo(Element element) throws IOException {
		loadFromXML(element);
	}
	
	public DamageInfo(float damage, float delay, GameObject source) {
		s_damage = damage;
		s_delay = delay;
		s_source = source;
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
	}

	public float getDamage() {
		return s_damage;
	}

	public float getDelay() {
		return s_delay;
	}
	
	public void setDelay(float delay) {
		s_delay = delay;
	}

	public GameObject getSource() {
		return s_source;
	}

}
