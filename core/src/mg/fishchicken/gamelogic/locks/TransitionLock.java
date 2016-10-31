package mg.fishchicken.gamelogic.locks;

import java.io.IOException;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamestate.locks.Lock;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

/**
 * TransitionLock is a Lock wrapper that can be assigned to a
 * Transition. Unlike a normal Lock, a TransitionLock has a unique ID
 * that identifies it. 
 * @author ANNUN
 *
 */
public class TransitionLock implements XMLSaveable, ThingWithId{
 
	private String s_id;
	private Lock lock;

	
	public TransitionLock(Element transitionLockElement) throws IOException {
		lock = new Lock();
		loadFromXML(transitionLockElement);
	}
	
	public TransitionLock(String id, boolean pickable, boolean locked, int lockLevel, String keyId){
		s_id = id;
		this.lock = new Lock(pickable, locked, lockLevel, keyId);
	}
	
	public String getId() {
		return s_id;
	}
	
	public Lock getLock() {
		return lock;
	}

	/**
	 * TransitionLocks are equal if they have the same ID.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransitionLock) {
			return ((TransitionLock)obj).getId().equals(s_id);
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
		lock.writeToXML(writer);
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
		lock.loadFromXML(root);
	}
	
	
}
