package mg.fishchicken.gamelogic.traps;

import java.io.IOException;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamestate.traps.Trap;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class TransitionTrap implements XMLSaveable, ThingWithId {

	private String s_id;
	private Trap trap;

	
	public TransitionTrap(Element transitionLockElement) throws IOException {
		loadFromXML(transitionLockElement);
	}
	
	public TransitionTrap(String id, boolean detected,  boolean disarmed, String trapId){
		s_id = id;
		this.trap = new Trap(detected, disarmed, TrapType.getTrap(trapId));
	}
	
	public String getId() {
		return s_id;
	}
	
	public Trap getTrap() {
		return trap;
	}

	/**
	 * TransitionLocks are equal if they have the same ID.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransitionTrap) {
			return ((TransitionTrap)obj).getId().equals(s_id);
		}
		return false;
	}
	
	
	@Override
	public int hashCode() {
		return s_id.hashCode();
	}
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		XMLUtil.writePrimitives(this, writer);
		trap.writeToXML(writer);
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
		trap = new Trap(root);
	}


}
