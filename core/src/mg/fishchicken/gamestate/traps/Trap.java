package mg.fishchicken.gamestate.traps;

import java.io.IOException;

import mg.fishchicken.gamelogic.traps.TrapType;
import mg.fishchicken.gamestate.ObservableState;

import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * A trap. Each trap is of a defined type (see {@link TrapType}),
 * and can be detected and disarmed.
 * 
 * Traps can be attached to Trapables and can produce a variety of fun
 * events when triggered.
 * 
 */
public class Trap extends ObservableState<Trap, Trap.TrapChanges> {

	public static final String XML_TRAP = "trap";
	
	private TrapChanges changes;
	private boolean s_detected;
	private boolean s_disarmed;
	private TrapType s_trapId;
	
	/**
	 * Creates a new trap by loading it from the supplied xml element.
	 * 
	 * @param parentElement
	 * @throws IOException
	 */
	public Trap(Element parentElement) throws IOException {
		this(false, false, null);
		loadFromXML(parentElement);
	}
	
	/**
	 * Creates a new trap of the supplied type.
	 * 
	 * @param detected
	 * @param disarmed
	 * @param trap
	 */
	public Trap(boolean detected, boolean disarmed, TrapType trap){
		s_detected = detected;
		s_disarmed = disarmed;
		s_trapId = trap;
		changes = new TrapChanges();
	}
	
	public Trap(Trap trapToCopy) {
		this(trapToCopy.s_detected, trapToCopy.s_disarmed, trapToCopy.s_trapId);
	}
	
	public boolean isDetected() {
		return s_detected;
	}

	public void setDetected(boolean isDetected) {
		changes.setOld(this);
		s_detected = isDetected;
		changed(changes);
	}

	public boolean isDisarmed() {
		return s_disarmed;
	}

	public void setDisarmed(boolean isDisarmed) {
		changes.setOld(this);
		s_disarmed = isDisarmed;
		changed(changes);
	}

	public TrapType getType() {
		return s_trapId;
	}
	
	@Override
	public String getXMLElementName() {
		return XML_TRAP;
	}
	
	public static class TrapChanges {
		public boolean wasDetected;
		public boolean wasDisarmed;
		
		private void setOld(Trap trap) {
			wasDisarmed = trap.s_disarmed;
			wasDetected = trap.s_detected;
		}
		
		public boolean wasDetected() {
			return wasDetected;
		}
		
		public boolean wasDisarmed() {
			return wasDisarmed;
		}
	}

}

