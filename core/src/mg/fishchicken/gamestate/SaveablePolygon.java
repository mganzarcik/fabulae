package mg.fishchicken.gamestate;

import mg.fishchicken.core.util.StringUtil;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.StringBuilder;

public class SaveablePolygon extends Polygon {

	/** Constructs a new polygon with no vertices. */
	public SaveablePolygon () {
		super();
	}
	
	/** Constructs a new polygon by deserializing it from the supplied string
	 * which was created by the {@link #toSaveableString()} method. */
	public SaveablePolygon (String saveableString) {
		super();
		String[] splits = saveableString.split(";");
		
		int i = 0;
		setOrigin(Float.valueOf(splits[i++]), Float.valueOf(splits[i++]));
		setPosition(Float.valueOf(splits[i++]), Float.valueOf(splits[i++]));
		setScale(Float.valueOf(splits[i++]), Float.valueOf(splits[i++]));
		setRotation(Float.valueOf(splits[i++]));
		
		float[] vertices = new float[splits.length-i];
		
		for (int j = 0; i < splits.length; ++i, ++j) {
			vertices[j] = Float.valueOf(splits[i]);
		}
		setVertices(vertices);
	}

	/** Constructs a new polygon from a float array of parts of vertex points.
	 * 
	 * @param vertices an array where every even element represents the horizontal part of a point, and the following element
	 *           representing the vertical part
	 * 
	 * @throws IllegalArgumentException if less than 6 elements, representing 3 points, are provided */
	public SaveablePolygon (float[] vertices) {
		super(vertices);
	}
	
	public String toSaveableString() {
		StringBuilder builder = StringUtil.getFSB();
		builder.append(getOriginX());
		builder.append(";");
		builder.append(getOriginY());
		builder.append(";");
		builder.append(getX());
		builder.append(";");
		builder.append(getY());
		builder.append(";");
		builder.append(getScaleX());
		builder.append(";");
		builder.append(getScaleY());
		builder.append(";");
		builder.append(getRotation());
		builder.append(";");
		
		float[] vertices = getVertices();
		for (int i = 0; i < vertices.length; ++i) {
			builder.append(vertices[i]);
			if (i != vertices.length-1) {
				builder.append(";");
			}
			
		}
		
		String returnValue = builder.toString();
		StringUtil.freeFSB(builder);
		return returnValue;
	}
	
	@Override
	public String toString() {
		return toSaveableString();
	}
	
}
