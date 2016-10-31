package mg.fishchicken.screens;

import groovy.util.Eval;

import java.io.IOException;
import java.util.HashSet;

import mg.fishchicken.FishchickenGame;
import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.projectiles.ProjectileType;
import mg.fishchicken.gamelogic.characters.AIScript;
import mg.fishchicken.gamelogic.characters.Race;
import mg.fishchicken.gamelogic.characters.Role;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.dialogue.Chatter;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemGroup;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.gamelogic.quests.Quest;
import mg.fishchicken.gamelogic.story.StorySequence;
import mg.fishchicken.gamelogic.traps.TrapType;
import mg.fishchicken.gamelogic.weather.WeatherProfile;
import mg.fishchicken.graphics.ParticleEffectManager;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.graphics.models.CharacterModel;
import mg.fishchicken.graphics.models.ItemModel;
import mg.fishchicken.screens.start.StartGameScreen;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.WindowPosition;
import mg.fishchicken.ui.configuration.LoadingScreens.LoadingScreensImagesList;
import mg.fishchicken.ui.loading.LoadingIndicator;
import mg.fishchicken.ui.loading.LoadingIndicator.LoadingIndicatorStyle;
import mg.fishchicken.ui.loading.LoadingScreenBackground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.SerializationException;

public class ModuleLoadingScreen extends BasicStageScreen {

	private enum AssetsToLoad {
		Configuration, Strings, UI, StorySequences, ParticleEffects, Lights, Races, Roles, AudioProfiles, Models, Projectiles,
		Effects, Chatters, AIScripts, Items, ItemGroups,
		Factions, Quests, Perks, PerkImages, Spells, SpellImages, Traps, Weather, GlobalAssets, GROOVY
	};

	private final FishchickenGame game;
	private HashSet<AssetsToLoad> assetsGathered;
	private final AssetManager am;
	private HashSet<AssetsToLoad> loaded;
	private GameState createdGameState;
	private Skin loadedUISkin;
	private LoadingIndicator loadingIndicator;
	private LoadingScreenBackground background;
	private LoadingScreensImagesList loadingScreenConfiguration;
	
	public ModuleLoadingScreen(final FishchickenGame game) {
		this.game = game;
		am = Assets.getAssetManager();
		loadedUISkin = null;
		assetsGathered =  new HashSet<AssetsToLoad>();
		loaded = new HashSet<AssetsToLoad>();
	}
	
	@Override
	public void render(final float delta) {
		try {
			final AssetsToLoad loadingWhat = loadGame();
			final boolean loaded = loadingWhat == null;
			if (!loaded) {
				if (this.loaded.contains(AssetsToLoad.UI) && loadingIndicator == null) {
					loadedUISkin = Assets.get(Configuration.getFolderUI()+"uiStyle.json", Skin.class);
					build();
				}
				if (loadingIndicator != null) {
					loadingIndicator.setWhat(Strings.getString(UIManager.STRING_TABLE, loadingWhat.toString()));
				}
				super.render(delta);
			} else {
				loadingIndicator.remove();
				loadingIndicator = null;
				game.setGameState(createdGameState);
				UIManager.init(createdGameState, loadedUISkin);
				game.setScreen(new StartGameScreen(game, createdGameState));
			}
		} catch (NoModuleDefinedException e) {
			game.displayModuleSelectionScreen();
		} catch (SerializationException | GdxRuntimeException e) {
			final ErrorScreen errorScreen = new ErrorScreen("Error loading module: "+e.getMessage(), " Choose another module ");
			errorScreen.getOptionalButton().addListener(new EventListener() {
				@Override
				public boolean handle(Event event) {
					if (event instanceof ChangeEvent) {
						game.displayModuleSelectionScreen();
						errorScreen.dispose();
						return true;
					}
					return false;
				}
			});
			e.printStackTrace();
			game.setScreen(errorScreen);
		} 
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (loadingIndicator != null) {
			WindowPosition.CENTER.position(loadingIndicator);
		}
	}
	
	private void build() {
		LoadingScreenStyle style = loadedUISkin.get(LoadingScreenStyle.class);
		background = new LoadingScreenBackground(loadingScreenConfiguration, "default");
		stage.addActor(background);
		loadingIndicator = new LoadingIndicator(style.loadingIndicatorStyle);
		stage.addActor(loadingIndicator);
		WindowPosition.CENTER.position(loadingIndicator);
	}

	private AssetsToLoad loadGame() throws NoModuleDefinedException {
		try {
			if (!loaded.contains(AssetsToLoad.Configuration)) {
				loadConfiguration();
				return AssetsToLoad.Configuration;
			} else if (!loaded.contains(AssetsToLoad.Strings)) {
				loadStrings();
				return AssetsToLoad.Strings;
			} else if (!loaded.contains(AssetsToLoad.GlobalAssets)) {
				loadGlobalAssets();
				return AssetsToLoad.GlobalAssets;
			} else if (!loaded.contains(AssetsToLoad.UI)) {
				loadUI();
				return AssetsToLoad.UI;
			} else if (!loaded.contains(AssetsToLoad.StorySequences)) {
				loadEndings();
				return AssetsToLoad.StorySequences;
			} else if (!loaded.contains(AssetsToLoad.ParticleEffects)) {
				loadParticleEffects();
				return AssetsToLoad.ParticleEffects;
			} else if (!loaded.contains(AssetsToLoad.Lights)) {
				loadLights();
				return AssetsToLoad.Lights;
			} else if (!loaded.contains(AssetsToLoad.Projectiles)) {
				loadProjectiles();
				return AssetsToLoad.Projectiles;
			} else if (!loaded.contains(AssetsToLoad.Chatters)) {
				loadChatters();
				return AssetsToLoad.Chatters;
			} else if (!loaded.contains(AssetsToLoad.AudioProfiles)) {
				loadAudioProfiles();
				return AssetsToLoad.AudioProfiles;
			} else if (!loaded.contains(AssetsToLoad.Models)) {
				loadModels();
				return AssetsToLoad.Models;
			} else if (!loaded.contains(AssetsToLoad.AIScripts)) {
				loadAIScripts();
				return AssetsToLoad.AIScripts;
			} else if (!loaded.contains(AssetsToLoad.Effects)) {
				loadEffects();
				return AssetsToLoad.Effects;
			} else if (!loaded.contains(AssetsToLoad.ItemGroups)) {
				loadItemGroups();
				return AssetsToLoad.ItemGroups;
			} else if (!loaded.contains(AssetsToLoad.Factions)) {
				loadFactions();
				return AssetsToLoad.Factions;
			} else if (!loaded.contains(AssetsToLoad.Quests)) {
				loadQuests();
				return AssetsToLoad.Quests;
			} else if (!loaded.contains(AssetsToLoad.Items)) {
				loadItems();
				return AssetsToLoad.Items;
			} else if (!loaded.contains(AssetsToLoad.Races)) {
				loadRaces();
				return AssetsToLoad.Races;
			} else if (!loaded.contains(AssetsToLoad.Roles)) {
				loadRoles();
				return AssetsToLoad.Roles;
			} else if (!loaded.contains(AssetsToLoad.Perks)) {
				loadPerks();
				return AssetsToLoad.Perks;
			} else if (!loaded.contains(AssetsToLoad.PerkImages)) {
				loadPerkImages();
				return AssetsToLoad.PerkImages;
			} else if (!loaded.contains(AssetsToLoad.Spells)) {
				loadSpells();
				return AssetsToLoad.Spells;
			} else if (!loaded.contains(AssetsToLoad.SpellImages)) {
				loadSpellImages();
				return AssetsToLoad.SpellImages;
			} else if (!loaded.contains(AssetsToLoad.Traps)) {
				loadTraps();
				return AssetsToLoad.Traps;
			}  else if (!loaded.contains(AssetsToLoad.Weather)) {
				loadWeather();
				return AssetsToLoad.Weather;
			} else if (!loaded.contains(AssetsToLoad.GROOVY)) {
				loaded.add(AssetsToLoad.GROOVY);
				return AssetsToLoad.GROOVY;
			}
		} catch (final NoModuleDefinedException e) {
			throw e;
		} catch (final IOException e) {
			throw new GdxRuntimeException(e);
		}
		// TODO: this does not work. :( This will initialize all the required Groovy classes, removing stutter the first time script is executed ingame
		Eval.me("1 == 1");
		return null;
	}
	
	private void loadConfiguration() throws NoModuleDefinedException, IOException {
		if (!assetsGathered.contains(AssetsToLoad.Configuration)) {
			assetsGathered.add(AssetsToLoad.Configuration);
		}
		if (am.update()) {
			FileHandle currentModuleFile = Gdx.files.local(Configuration.FILE_LAST_MODULE);
			String moduleName = null;
			if (!currentModuleFile.exists()) {
				Array<FileHandle> modules = ModuleSelectionScreen.getModules();
				if (modules.size == 1) {
					moduleName = modules.get(0).name();
				}
			} else {
				moduleName = currentModuleFile.readString();
			}
			if (moduleName == null) {
				throw new NoModuleDefinedException();
			}
			Configuration.loadModule(Gdx.files, moduleName);
			Gdx.graphics.setTitle(Configuration.getCurrentModuleName());
			createdGameState = new GameState(game);
			loadingScreenConfiguration = Configuration
					.getLoadingScreensConfiguration()
					.getConfigurationForScreenType("module");
			loaded.add(AssetsToLoad.Configuration);
		}
	}

	private void loadStrings() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Strings)) {
			Strings.gatherStringResources();
			assetsGathered.add(AssetsToLoad.Strings);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Strings);
		}
	}

	private void loadParticleEffects() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.ParticleEffects)) {
			ParticleEffectManager.gatherParticleEffects();
			assetsGathered.add(AssetsToLoad.ParticleEffects);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.ParticleEffects);
		}
	}

	private void loadLights() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Lights)) {
			LightDescriptor.gatherLights();
			assetsGathered.add(AssetsToLoad.Lights);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Lights);
		}
	}

	private void loadProjectiles() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Projectiles)) {
			ProjectileType.gatherProjectileTypes();
			assetsGathered.add(AssetsToLoad.Projectiles);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Projectiles);
		}
	}

	private void loadAIScripts() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.AIScripts)) {
			AIScript.gatherAIScripts();
			assetsGathered.add(AssetsToLoad.AIScripts);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.AIScripts);
		}
	}

	private void loadRaces() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Races)) {
			Race.gatherRaces();
			assetsGathered.add(AssetsToLoad.Races);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Races);
		}
	}
	
	private void loadRoles() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Roles)) {
			Role.gatherRoles();
			assetsGathered.add(AssetsToLoad.Roles);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Roles);
		}
	}
	
	private void loadAudioProfiles() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.AudioProfiles)) {
			AudioProfile.gatherAudioProfiles();
			assetsGathered.add(AssetsToLoad.AudioProfiles);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.AudioProfiles);
		}
	}
	
	private void loadModels() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Models)) {
			CharacterModel.gatherModels();
			ItemModel.gatherModels();
			assetsGathered.add(AssetsToLoad.Models);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Models);
		}
	}

	private void loadWeather() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Weather)) {
			WeatherProfile.gatherWeatherProfiles();
			assetsGathered.add(AssetsToLoad.Weather);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Weather);
		}
	}

	private void loadQuests() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Quests)) {
			Quest.gatherQuests();
			assetsGathered.add(AssetsToLoad.Quests);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Quests);
		}
	}

	private void loadPerks() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Perks)) {
			Perk.gatherPerks();
			assetsGathered.add(AssetsToLoad.Perks);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Perks);
		}
	}

	private void loadPerkImages() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.PerkImages)) {
			Perk.gatherPerkImages();
			assetsGathered.add(AssetsToLoad.PerkImages);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.PerkImages);
		}
	}

	private void loadSpells() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Spells)) {
			Spell.gatherSpells();
			assetsGathered.add(AssetsToLoad.Spells);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Spells);
		}
	}

	private void loadSpellImages() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.SpellImages)) {
			Spell.gatherSpellImages();
			assetsGathered.add(AssetsToLoad.SpellImages);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.SpellImages);
		}
	}
	
	private void loadTraps() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Traps)) {
			TrapType.gatherTraps();
			assetsGathered.add(AssetsToLoad.Traps);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Traps);
		}
	}

	private void loadEffects() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Effects)) {
			Effect.gatherEffects();
			assetsGathered.add(AssetsToLoad.Effects);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Effects);
		}
	}

	private void loadEndings() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.StorySequences)) {
			StorySequence.gatherStorySequences();
			assetsGathered.add(AssetsToLoad.StorySequences);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.StorySequences);
		}
	}
	
	private void loadChatters() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Chatters)) {
			Chatter.gatherChatters();
			assetsGathered.add(AssetsToLoad.Chatters);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Chatters);
		}
	}

	private void loadItems() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Items)) {
			InventoryItem.gatherInventoryItems();
			assetsGathered.add(AssetsToLoad.Items);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Items);
		}
	}

	private void loadFactions() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.Factions)) {
			Faction.gatherFactions();
			assetsGathered.add(AssetsToLoad.Factions);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.Factions);
		}
	}

	private void loadItemGroups() throws IOException {
		if (!assetsGathered.contains(AssetsToLoad.ItemGroups)) {
			ItemGroup.loadItemGroups();
			assetsGathered.add(AssetsToLoad.ItemGroups);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.ItemGroups);
		}
	}

	private void loadGlobalAssets() {
		if (!assetsGathered.contains(AssetsToLoad.GlobalAssets)) {
			final AssetMap assetsToLoad = new AssetMap();
			Configuration.gatherGlobalAssets(assetsToLoad);
			for (final Entry<String, Class<?>> entry : assetsToLoad) {
				am.load(entry.key, entry.value);
			}
			assetsGathered.add(AssetsToLoad.GlobalAssets);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.GlobalAssets);
		}
	}

	private void loadUI() {
		if (!assetsGathered.contains(AssetsToLoad.UI)) {
			UIManager.loadUIAssets();
			assetsGathered.add(AssetsToLoad.UI);
		}
		if (am.update()) {
			loaded.add(AssetsToLoad.UI);
		}
	}

	@Override
	public void hide() {
		dispose();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (background != null) {
			background.dispose();
		}
	}
	
	private static class NoModuleDefinedException extends IOException{
		private static final long serialVersionUID = 518126695848002708L;
	}
	
	public static class LoadingScreenStyle {
		public LoadingIndicatorStyle loadingIndicatorStyle;
	}

}
