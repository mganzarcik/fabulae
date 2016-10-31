package mg.fishchicken.gamelogic.characters.groups;

import java.io.IOException;

import mg.fishchicken.core.OrientedThing;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.Orientation;
import mg.fishchicken.gamestate.Tile;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Formation implements XMLSaveable, OrientedThing {

	public static final String XML_FORMATION = "formation";
	public static final String XML_FORMATION_ORIENTATION = "formationOrientation";
	public static final String XML_ATTRIBUTE_INDEX = "index";
	public static final String XML_ATTRIBUTE_XOFFSET = "xOffset";
	public static final String XML_ATTRIBUTE_YOFFSET = "yOffset";
	
	private ObjectMap<Integer, Tile> formation;
	private ObjectMap<Integer, Tile> rotatedFormationOrtho;
	private ObjectMap<Integer, Tile> rotatedFormationIso;
	private Orientation orientation;
	
	
	public Formation() {
		formation = new ObjectMap<Integer, Tile>();
		rotatedFormationOrtho = new ObjectMap<Integer, Tile>();
		rotatedFormationIso = new ObjectMap<Integer, Tile>();
		orientation = Orientation.UP;
		formation.put(0, new Tile(0, 0));
		for (int i = 0; i < Configuration.getMaxCharactersInGroup()-1; ++i) {
			formation.put(i+1, new Tile(0, -1-i));
		}
		recalculateOrthoFormation();
		recalculateIsoFormation();
	}
	
	public Formation(Element rootElement) throws IOException {
		formation = new ObjectMap<Integer, Tile>();
		rotatedFormationOrtho = new ObjectMap<Integer, Tile>();
		rotatedFormationIso = new ObjectMap<Integer, Tile>();
		orientation = Orientation.UP;
		loadFromXML(rootElement);
	}
	
	public Formation(Formation formationToCopy) {
		this.formation =  new ObjectMap<Integer, Tile>(formationToCopy.formation);
		this.rotatedFormationIso = new ObjectMap<Integer, Tile>(formationToCopy.rotatedFormationIso);
		this.orientation = formationToCopy.orientation;
		this.rotatedFormationOrtho =  new ObjectMap<Integer, Tile>(formationToCopy.rotatedFormationOrtho);
	}
	
	public Tile getOffset(int index, boolean isometric) {
		return isometric ? rotatedFormationIso.get(index) : rotatedFormationOrtho.get(index);
	}
	
	/**
	 * Gets the current formation. The returned formation is oriented UP and orthogonal.
	 * 
	 * @param newFormation
	 */
	public ObjectMap<Integer, Tile> getFormation() {
		return formation;
	}
	
	/**
	 * Sets a new formation to the group. The supplied formation is assumed to be rotated UP and orthogonal.
	 * 
	 * @param newFormation
	 */
	public void setFormation(ObjectMap<Integer, Tile> newFormation) {
		formation = newFormation;
		recalculateOrthoFormation();
		recalculateIsoFormation();
	}
	
	private void recalculateOrthoFormation() {
		int rotationDegrees = orientation.getDegrees();
		recalculateFormation(rotatedFormationOrtho, rotationDegrees);
	}
	
	private void recalculateIsoFormation() {
		int rotationDegrees = orientation.getAntiClockwise().getDegrees();
		recalculateFormation(rotatedFormationIso, rotationDegrees);
	}
	
	private void recalculateFormation(ObjectMap<Integer, Tile> formationToRecalculate, int rotationDegrees) {
		for (Integer i : formation.keys()) {
			Tile formationPosition = formationToRecalculate.get(i);
			if (formationPosition == null) {
				formationPosition = new Tile();
				formationToRecalculate.put(i, formationPosition);
			}
			formationPosition.set(formation.get(i));
			// we only rotate in multiplies of 45
			// first we do as many 90 degrees rotations as needed
			// and then do the final 45 rotation if required
			int rotAmount = rotationDegrees / 90;
			for (int j = 0; j < rotAmount; ++j) {
				formationPosition.rotate90CW();
			}
			if (rotationDegrees % 90 == 45) {
				rotateTile45CW(formationPosition);
			}
		}
	}
	
	/**
	 * Rotate the supplied tile 45 degrees clockwise around 0,0 origin.
	 * 
	 * See http://math.stackexchange.com/questions/732679/how-to-rotate-a-matrix-by-45-degrees
	 * for details how and why.
	 */
	private void rotateTile45CW(Tile tile) {
		tile.set(tile.getX() + tile.getY(), -tile.getX() + tile.getY());
	}
	
	/**
	 * Sets the Orientation of this CharacterGroup.
	 * 
	 * If the supplied orientation is null, nothing is set and the
	 * old Orientation is kept.
	 * 
	 * @param newOrientation
	 */
	public void setOrientation(Orientation newOrientation) {
		if (newOrientation == null || orientation.equals(newOrientation)) {
			return;
		}
		orientation = newOrientation;
		recalculateOrthoFormation();
		recalculateIsoFormation();
	}

	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_FORMATION);
		writer.attribute(XML_FORMATION_ORIENTATION, orientation);
		for(Entry<Integer, Tile> entry : formation.entries()) {
			writer.element(CharacterGroup.XML_MEMBER).attribute(XML_ATTRIBUTE_INDEX, entry.key.toString()).attribute(XML_ATTRIBUTE_XOFFSET, entry.value.getX()).attribute(XML_ATTRIBUTE_YOFFSET, entry.value.getY()).pop();
		}
		writer.pop();
	}

	@Override
	public void loadFromXML(Element root) throws IOException {
		Element formationElement = root.getChildByName(XML_FORMATION);
		if (formationElement == null) {
			return;
		}
		orientation = Orientation.valueOf(formationElement.getAttribute(XML_FORMATION_ORIENTATION, "UP"));
		for (int i = 0; i <formationElement.getChildCount(); ++i) {
			Element memberElement = formationElement.getChild(i);
			Integer index = Integer.valueOf(memberElement.getAttribute(XML_ATTRIBUTE_INDEX));
			int xOffset = Integer.parseInt(memberElement.getAttribute(XML_ATTRIBUTE_XOFFSET, "0"));
			int yOffset = Integer.parseInt(memberElement.getAttribute(XML_ATTRIBUTE_YOFFSET, "0"));
			formation.put(index, new Tile(xOffset,yOffset));
		}
		recalculateOrthoFormation();
		recalculateIsoFormation();
	}
}
