package mg.fishchicken.gamelogic.characters;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.actions.Action;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class AIScript implements XMLLoadable, ThingWithId {
	public static final String XML_COMBAT= "combat";
	public static final String XML_PEACE = "peace";
	
	private static ObjectMap<String, String> scripts = new ObjectMap<String, String>();

	public static AIScript getAIScript(String id) {
		return Assets.get(scripts.get(id.toLowerCase(Locale.ENGLISH)));
	}

	/**
	 * Gathers all Races and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherAIScripts() throws IOException {
		Assets.gatherAssets(Configuration.getFolderAIScripts(), "xml", AIScript.class, scripts);
	}
	
	private String id;
	private Script combatScript;
	private Script peaceScript;
	
	public AIScript(FileHandle file) throws IOException {
		this.id = file.nameWithoutExtension();
		loadFromXML(file);
	}
	
	public Action run(Binding binding) {
		Script script = getScript();
		if (script != null) {
			script.setBinding(binding);
			return (Action) script.run();
		}
		return null;
	}
	
	private Script getScript() {
		if (GameState.isCombatInProgress()) {
			return combatScript;
		}
		return peaceScript;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return getId();
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		combatScript = null;
		peaceScript = null;
		loadFromXMLNoInit(file);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		Element root = new XmlReader().parse(file);
		XMLUtil.handleImports(this, file, root);
		combatScript = XMLUtil.readScript(id, root.getChildByName(XML_COMBAT), combatScript);
		peaceScript = XMLUtil.readScript(id, root.getChildByName(XML_PEACE), peaceScript);
	}
}
