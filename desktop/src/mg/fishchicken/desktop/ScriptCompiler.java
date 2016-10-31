package mg.fishchicken.desktop;

import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import mg.fishchicken.core.util.GroovyUtil;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.AIScript;
import mg.fishchicken.gamelogic.effects.Effect;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ScriptCompiler {

	private static String[] scriptTags = {
		"script",
		Effect.XML_ON_HIT,
		Effect.XML_DURATION,
		Effect.XML_PERSISTENT,
		Effect.XML_ON_END,
		Effect.XML_CONDITION,
		Effect.XML_EXTRA_PARAMETERS,
		AIScript.XML_COMBAT,
		AIScript.XML_PEACE,
		XMLUtil.XML_TARGET
	};
	
	private File sourceFolder;
	private File targetFolder;
	
	public ScriptCompiler(String folderName) {
		this.sourceFolder = new File(folderName);
		targetFolder = new File(sourceFolder, "compiledscripts");
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
		}
	}
	
	public void run() {
		if (!sourceFolder.isDirectory()) {
			System.out.println("Supplied folder \""+sourceFolder+"\"is not a directory, aborting.");
		}
		
		processFolder(sourceFolder);
	}
	
	private void processFolder(File folder) {
		System.out.println("Processing folder \""+folder.getAbsolutePath()+"\"");
		File[] children = folder.listFiles();
		
		for (File child : children) {
			if (child.isDirectory()) {
				processFolder(child);
			} else if (child.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml")){
				if (child.canRead() && child.canWrite()) {
					processFile(child);
				} else {
					System.out.println("Cannot process file \""+child.getAbsolutePath()+"\"");
				}
			}
		}
	}
	
	private void processFile(File file) {
		System.out.println("Processing file \""+file.getAbsolutePath()+"\"");
		
		boolean changed = false;
		XmlReader reader = new XmlReader();
		Element root = null;
		String header = "";
		try (BufferedReader fr =  new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			String content = "";
			String line = fr.readLine();
			while (line != null) {
				content += line+"\n";
				if (line.startsWith("<?") || line.startsWith("<!")) {
					header += line+"\n";
				}
				line = fr.readLine();
			}
			
			root = reader.parse(content);
			String nameWihoutExtension = file.getName();
			if (nameWihoutExtension.contains(".")) {
				nameWihoutExtension = nameWihoutExtension.substring(0, nameWihoutExtension.lastIndexOf("."));
			}
			int scriptCounter = 0;
			for (String scriptTagName : scriptTags) {
				Array<Element> foundScriptElements = root.getChildrenByNameRecursively(scriptTagName);
				for (Element scriptElement : foundScriptElements) {
					// skip anything in a variables / parameters tag, since these are not scripts
					String parentName = scriptElement.getParent().getName();
					if (XMLUtil.XML_VARIABLES.equals(parentName) || XMLUtil.XML_PARAMETERS.equals(parentName)) {
						continue;
					}
					String id = calculateScriptId(nameWihoutExtension, ++scriptCounter, scriptElement);
					System.out.println("Compiling script \""+id+"\"");
					Script script = GroovyUtil.createScript(id, scriptElement.getText(), targetFolder.getAbsolutePath());
					if (script != null) {
						scriptElement.setAttribute(XMLUtil.XML_ATTRIBUTE_ID, id);
						scriptElement.setText(scriptElement.getText() != null ? scriptElement.getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") : null);
						changed = true;
					}
				}
				
			}
		} catch (IOException | SerializationException e) {
			System.out.println("Exception while reading file \""+file.getAbsolutePath()+"\"");
		} 
		
		if (changed) {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(header+root.toString());
			} catch (IOException e) {
				System.out.println("Exception while writing file \""+file.getAbsolutePath()+"\"");
			}
		}
	}
	
	private String calculateScriptId(String rootId, int counter, Element scriptElement) {
		String id = "";
		Element parent = scriptElement;
		while (parent != null) {
			id = StringUtil.capitalizeFirstLetter(parent.getName()) + id;
			parent = parent.getParent();
		}
		
		return StringUtil.capitalizeFirstLetter(rootId) + id + counter;
	}
}
