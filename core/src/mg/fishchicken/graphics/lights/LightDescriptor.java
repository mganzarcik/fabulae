package mg.fishchicken.graphics.lights;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class LightDescriptor implements XMLLoadable {

	public static ObjectMap<String, String> lights = new ObjectMap<String, String>();
	
	private String id;
	@XMLField(fieldPath="properties.color")
	public Color lightColor;
	@XMLField(fieldPath="properties.radius")
	public float lightRadius;
	@XMLField(fieldPath="properties.coneDegree")
	public float coneDegree;
	@XMLField(fieldPath="properties.isSunlight")
	public boolean isSunlight;
	@XMLField(fieldPath="properties.isConeLight")
	public boolean isConeLight;
	public Element actionsElement;
		
	public static LightDescriptor getLightDescriptor(String id) {
		return Assets.get(lights.get(id.toLowerCase(Locale.ENGLISH)), LightDescriptor.class);
	}
	/**
	 * Gathers all LightDescriptors and registers them in the AssetManager
	 * so that they can be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherLights() throws IOException {
		Assets.gatherAssets(Configuration.getFolderLights(), "xml", LightDescriptor.class, lights);
	}

	
	public LightDescriptor(FileHandle file) throws IOException {
		loadFromXML(file);
	}
	
	/** 
	 * Creates a new, anonymous light descriptor.
	 * 
	 * The descriptor will be without an assigned ID and won't be retrieved using
	 * {@link LightDescriptor#getLightDescriptor(String)}
	 * 
	 * @param color
	 * @param radius
	 * @param actions
	 */
	public LightDescriptor(Color color, int radius, Element actions) {
		this.lightColor = color;
		this.lightRadius = radius;
		this.actionsElement = actions;
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		actionsElement = null;
		loadFromXMLNoInit(file);
		
	}
	
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		XMLUtil.readPrimitiveMembers(this, root);
		if (root.getChildByName(XMLUtil.XML_ACTIONS) != null) {
			actionsElement = root.getChildByName(XMLUtil.XML_ACTIONS);
		}
	}
	
	@Override
	public String toString() {
		return id;
	}
	

}
