package mg.fishchicken.desktop;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.core.configuration.Configuration;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class DesktopLauncher {
	
	public static void main (String[] arg) {
		
		boolean shouldPack = arg.length == 1 && "pack".equals(arg[0]);
		boolean shouldCompile = arg.length == 2 && "compile".equals(arg[0]);
		
		if (shouldCompile) {
			String folderName = arg[1];
			ScriptCompiler compiler = new ScriptCompiler(folderName);
			compiler.run();
			return;
		}
		
		if (shouldPack) {
			Settings settings = new Settings();
			settings.maxHeight = 2048;
			settings.maxWidth = 4096;
			TexturePacker.process("bin/images", "bin", "uiStyle");
			TexturePacker.process(settings, "bin/modules/showcase/ui/images/startGameMenu", "bin/modules/showcase/ui/", "startGameMenuStyle");
			TexturePacker.process(settings, "bin/modules/showcase/ui/images/partyCreation", "bin/modules/showcase/ui/", "partyCreationStyle");
			TexturePacker.process("bin/modules/showcase/ui/images/game", "bin/modules/showcase/ui/", "uiStyle");
			TexturePacker.process("bin/modules/showcase/perks/images/small", "bin/modules/showcase/perks/images/", "perkImages");
			TexturePacker.process("bin/modules/showcase/spells/images/small", "bin/modules/showcase/spells/images/", "spellImages");
		}
		
		Configuration.createConfiguration(new LwjglFiles());
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Fabulae";
		cfg.width = Configuration.getScreenWidth();
		cfg.height = Configuration.getScreenHeight();
		cfg.fullscreen = Configuration.isFullscreen();
		
		new LwjglApplication(new FishchickenGame(), cfg).setLogLevel(Application.LOG_DEBUG);
	}
}
