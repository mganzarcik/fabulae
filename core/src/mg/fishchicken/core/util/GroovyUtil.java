package mg.fishchicken.core.util;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.gamelogic.actions.Action;
import mg.fishchicken.gamelogic.characters.AIScript;
import mg.fishchicken.gamelogic.characters.AIScriptPackage;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.Gender;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.Effect.PersistentEffect;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamestate.Position;
import mg.fishchicken.ui.UIManager;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.StringBuilder;

public class GroovyUtil {
	
	private static URLClassLoader classLoader;
	
	// VERY primitive cache to make sure we do not compile same scripts multiple times
	private static ObjectMap<String, Script> cache = new ObjectMap<String, Script>();
	
	/**
	 * Loads a precompiled script with the supplied ID. The script class file must 
	 * be stored in the pre-compiled scripts folder (see {@link Configuration#getFolderCompiledScripts()}.
	 * 
	 * The method will return null if such a folder does not exist or if it does not contain
	 * the desired script.
	 * @param id
	 * @return
	 */
	public static Script loadScript(final String id) {
		String fileName = id+".class";
		Script cached = cache.get(fileName);
		if (cached != null) {
			return cached;
		}
		try {
			URLClassLoader cl = getURLClassLoader();
			if (cl != null) {
		    	Script script = InvokerHelper.createScript(cl.loadClass(fileName), new Binding());
		    	cache.put(fileName, script);
		    	return script;
			}
		} catch (ClassNotFoundException | RuntimeException | MalformedURLException e) {
			// do nothing and just build the class from the text
		}
		return null; 
	}
	
	public static Script createScript(final String id, String scriptText) {
		return createScript(id, scriptText, null);
	}
	
	public static Script createScript(final String id, String scriptText, String targetDirectory) {
		if (scriptText == null || scriptText.isEmpty()) {
			return null;
		}
		
		return createScript(id, scriptText, targetDirectory, 
				Strings.class.getName(),GameObject.class.getName(), GameCharacter.class.getName(),
				AbstractGameCharacter.class.getName(),
				AIScriptPackage.class.getName(),
				Gender.class.getName(),
				Array.class.getName(), ObjectMap.class.getName(), ObjectSet.class.getName(), 
				GameState.class.getName(), Log.class.getName(),
				Modifier.class.getName(), AIScript.class.getName(),
				ModifiableStat.class.getName(),
				Script.class.getName(),
				Vector2.class.getName(), Vector3.class.getName(),
				MathUtil.class.getPackage().getName()+".*", 
				Action.class.getPackage().getName()+".*", 
				Weapon.class.getPackage().getName()+".*",
				Position.class.getPackage().getName()+".*",
				Effect.class.getName(), PersistentEffect.class.getName(),
				Perk.class.getName(),
				Spell.class.getName(),
				MathUtils.class.getName(),
				Inventory.class.getPackage().getName()+".*",
				ObjectMap.class.getName(), 
				PlayerCharacterGroup.class.getName(),
				Binding.class.getName(),
				Condition.class.getName(), 
				UIManager.class.getName());
	}
	
	private static Script createScript(final String scriptId, String scriptText, String targetDirectory, String... imports) {
		if (scriptText == null || scriptText.isEmpty()) {
			return null;
		}
		
		if (targetDirectory == null) {
			Script cached = cache.get(scriptText);
			if (cached != null) {
				return cached;
			}
		}
		
		StringBuilder scriptStringBuffer = StringUtil.getFSB();
		
		for (String oneimport : imports) {
			scriptStringBuffer.append("import ");
			scriptStringBuffer.append(oneimport);
			scriptStringBuffer.append(";\n");
		}
		
		scriptStringBuffer.append("\n");
		scriptStringBuffer.append(scriptText); 
		
		final String finalScriptText = scriptStringBuffer.toString();
		StringUtil.freeFSB(scriptStringBuffer);
		
		GroovyCodeSource gcs = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            public GroovyCodeSource run() {
                return new GroovyCodeSource(finalScriptText, scriptId+".groovy", GroovyShell.DEFAULT_CODE_BASE);
            }
        });
		
		GroovyClassLoader loader = null;
		try {
			loader = getLoader(targetDirectory);
			Class<?> scriptClass = loader.parseClass(gcs, false);
			Script script = InvokerHelper.createScript(scriptClass, new Binding());
			cache.put(scriptText, script);
			return script;
		} finally {
			if (loader != null) {
				StreamUtils.closeQuietly(loader);
			}
		}
	}
	
	private static URLClassLoader getURLClassLoader() throws MalformedURLException {
		if (classLoader != null) {
			return classLoader;
		}
		FileHandle scriptsFolder = Gdx.files.internal(Configuration.getFolderCompiledScripts());
		if (!scriptsFolder.isDirectory()) {
			scriptsFolder = Gdx.files.internal(Assets.BIN_FOLDER+Configuration.getFolderCompiledScripts());
		}
		if (scriptsFolder.exists()) {
			File dirFile = scriptsFolder.file();
			URL url = dirFile.toURI().toURL();
		    classLoader = new URLClassLoader(new URL[]{url});
		}
	    return classLoader;
	}
	
	private static GroovyClassLoader getLoader(String targetDirectory) {
		final CompilerConfiguration myConfiguration = new CompilerConfiguration();
		if (targetDirectory != null) {
			myConfiguration.setTargetDirectory(targetDirectory);
		}
		
		return AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
            public GroovyClassLoader run() {
                return new GroovyClassLoader(GroovyShell.class.getClassLoader(), myConfiguration);
            }
        });
	}
	
	public static boolean evaluateCondition(Script condition) {
		if (condition == null) {
			return true;
		}
		Object evaluatedValue = condition.run();
		if (evaluatedValue instanceof Boolean) {
			return (Boolean)evaluatedValue;
		} else if (evaluatedValue instanceof String) {
			return Boolean.parseBoolean((String)evaluatedValue);
		}
		return false;
	}
}
