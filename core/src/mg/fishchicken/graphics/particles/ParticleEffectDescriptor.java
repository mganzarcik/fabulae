package mg.fishchicken.graphics.particles;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

import mg.fishchicken.core.saveload.XMLField;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.XMLUtil;

/**
 * Simple class that carries some additional information about a particle effect.
 * 
 * @author Annun
 *
 */
public class ParticleEffectDescriptor implements XMLSaveable {
	public static final String XML_OFFSET = "offset";
	
	private String s_effectId = null;
	private float s_delay = 0f;
	@XMLField(fieldPath="offset.x")
	private float xOffset = 0f;
	@XMLField(fieldPath="offset.y")
	private float yOffset = 0f;
	
	public ParticleEffectDescriptor(Element root) throws IOException {
		this.loadFromXML(root);
	}
	
	public ParticleEffectDescriptor(String id, float delay, float x, float y) throws IOException {
		s_effectId = id;
		s_delay = delay;
		xOffset = x;
		yOffset = y;
	}
	
	/**
	 * Returns the ParticleEffect id.
	 * @return
	 */
	public String getEffectId() {
		return s_effectId;
	}
	
	/**
	 * Returns the delay in seconds after which the particle effect
	 * should be started.
	 * @return
	 */
	public float getDelay() {
		return s_delay;
	}
	
	/**
	 * Returns the x offset in tiles with which the effect should be rendered when
	 * attached to a GameObject.
	 * 
	 * @return
	 */
	public float getXOffset() {
		return xOffset;
	}
	
	/**
	 * Returns the y offset in tiles with which the effect should be rendered when
	 * attached to a GameObject.
	 * 
	 * @return
	 */
	public float getYOffset() {
		return yOffset;
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		XMLUtil.writePrimitives(this, writer, true);
		writer.element(XML_OFFSET);
		writer.element(XMLUtil.XML_ATTRIBUTE_X, xOffset);
		writer.element(XMLUtil.XML_ATTRIBUTE_Y, yOffset);
		writer.pop();
	}
	@Override
	public void loadFromXML(Element root) throws IOException {
		XMLUtil.readPrimitiveMembers(this, root);
	}

}
