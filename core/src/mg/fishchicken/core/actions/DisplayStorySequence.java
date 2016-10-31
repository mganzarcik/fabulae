package mg.fishchicken.core.actions;

import groovy.lang.Binding;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Displays the StorySequence with the supplied id.
 * <br /><br />
 * Example:
 * 
 * <pre>
 * 	&lt;displayStorySequence id="StorySequenceId" /&gt;
 * </pre>
 * 
 * @author ANNUN
 *
 */
public class DisplayStorySequence extends Action {

	@Override
	protected void run(Object object, Binding parameters) {
		UIManager.displayStorySequence(getParameter(XMLUtil.XML_ATTRIBUTE_ID));
	}

	@Override
	public void validateAndLoadFromXML(Element conditionElement) {
		if (conditionElement.get(XMLUtil.XML_ATTRIBUTE_ID, null) == null) {
			throw new GdxRuntimeException(XMLUtil.XML_ATTRIBUTE_ID+" must be set for action DisplayStorySequence in element: \n\n"+conditionElement);
		}
	}

}
