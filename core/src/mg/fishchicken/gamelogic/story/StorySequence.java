package mg.fishchicken.gamelogic.story;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.audio.Music;
import mg.fishchicken.core.actions.Action;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class StorySequence implements XMLLoadable {
	
	public static final String ENDING_DEFEAT = "defeat";
	
	private static ObjectMap<String, String> storySequences = new ObjectMap<String, String>();

	public static StorySequence getStorySequence(String id) {
		return Assets.get(storySequences.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Gathers all StorySequences and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherStorySequences() throws IOException {
		Assets.gatherAssets(Configuration.getFolderStorySequences(), "xml", StorySequence.class, storySequences);
	}
	
	public final String XML_SCREENS = "screens";
	public final String XML_SCREEN = "screen";
	
	private boolean s_canContinue;
	private Array<AudioTrack<?>> music;
	private Array<StoryPage> screens;
	private Action action;

	public StorySequence(FileHandle file) throws IOException {
		loadFromXML(file);
	}
	
	/**
	 * Whether the game can continue after the story sequence ends.
	 * 
	 * If set to false, the game will end at the end of this sequence.
	 * @return
	 */
	public boolean canContinue() {
		return s_canContinue;
	}
	
	public Array<AudioTrack<?>> getMusic() {
		return music;
	}
	
	public Array<StoryPage> getPages() {
		return screens;
	}
	
	/**
	 * Gets the action that should be executed at the end of the action,
	 * if the sequence does not exit the game.
	 * @return
	 */
	public Action getAction() {
		return action;
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		screens = new Array<StoryPage>();
		s_canContinue = true;
		loadFromXMLNoInit(file);
	}
	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		loadFromXML(root);
	}
	
	private void loadFromXML(Element element) {
		XMLUtil.readPrimitiveMembers(this, element);
		music = XMLUtil.readTracks(element.getChildByName(XMLUtil.XML_MUSIC), Music.class);
		Element screensElement = element.getChildByName(XML_SCREENS);
		for (int i = 0; i < screensElement.getChildCount(); ++i) {
			screens.add(new StoryPage(screensElement.getChild(i)));
		}
		action = Action.getAction(element.getChildByName(XMLUtil.XML_ACTION));
	}
}
