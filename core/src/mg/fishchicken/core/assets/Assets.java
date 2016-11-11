package mg.fishchicken.core.assets;

import java.util.Locale;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import mg.fishchicken.audio.AudioProfile;
import mg.fishchicken.core.assets.loaders.AIScriptsLoader;
import mg.fishchicken.core.assets.loaders.AudioProfileLoader;
import mg.fishchicken.core.assets.loaders.CharacterModelLoader;
import mg.fishchicken.core.assets.loaders.ChatterLoader;
import mg.fishchicken.core.assets.loaders.DialogueLoader;
import mg.fishchicken.core.assets.loaders.EffectsLoader;
import mg.fishchicken.core.assets.loaders.FactionLoader;
import mg.fishchicken.core.assets.loaders.InventoryItemLoader;
import mg.fishchicken.core.assets.loaders.ItemGroupLoader;
import mg.fishchicken.core.assets.loaders.ItemModelLoader;
import mg.fishchicken.core.assets.loaders.LightDescriptorLoader;
import mg.fishchicken.core.assets.loaders.PerkLoader;
import mg.fishchicken.core.assets.loaders.ProjectileTypeLoader;
import mg.fishchicken.core.assets.loaders.QuestLoader;
import mg.fishchicken.core.assets.loaders.RaceLoader;
import mg.fishchicken.core.assets.loaders.RoleLoader;
import mg.fishchicken.core.assets.loaders.SaveGameDetailsLoader;
import mg.fishchicken.core.assets.loaders.SkinLoaderWithSounds;
import mg.fishchicken.core.assets.loaders.SpellLoader;
import mg.fishchicken.core.assets.loaders.StorySequenceLoader;
import mg.fishchicken.core.assets.loaders.StringTableLoader;
import mg.fishchicken.core.assets.loaders.TrapLoader;
import mg.fishchicken.core.assets.loaders.WeatherProfileLoader;
import mg.fishchicken.core.i18n.Strings.StringTable;
import mg.fishchicken.core.projectiles.ProjectileType;
import mg.fishchicken.gamelogic.characters.AIScript;
import mg.fishchicken.gamelogic.characters.Race;
import mg.fishchicken.gamelogic.characters.Role;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.gamelogic.dialogue.Chatter;
import mg.fishchicken.gamelogic.dialogue.Dialogue;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.items.InventoryItem;
import mg.fishchicken.gamelogic.inventory.items.ItemGroup;
import mg.fishchicken.gamelogic.locations.GameMapLoader;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.gamelogic.quests.Quest;
import mg.fishchicken.gamelogic.story.StorySequence;
import mg.fishchicken.gamelogic.traps.TrapType;
import mg.fishchicken.gamelogic.weather.WeatherProfile;
import mg.fishchicken.graphics.lights.LightDescriptor;
import mg.fishchicken.graphics.models.CharacterModel;
import mg.fishchicken.graphics.models.ItemModel;
import mg.fishchicken.ui.saveload.SaveGameDetails;

public class Assets {

	public static final String BIN_FOLDER = "bin/";
	
	private static AtlassedAssetManager assetManager;
	
	static {
		InternalFileHandleResolver resolver = new InternalFileHandleResolver();
		assetManager = new AtlassedAssetManager();
		assetManager.setLoader(TiledMap.class, new GameMapLoader(resolver));
		assetManager.setLoader(Dialogue.class, new DialogueLoader(resolver));
		assetManager.setLoader(LightDescriptor.class, new LightDescriptorLoader(resolver));
		assetManager.setLoader(ProjectileType.class, new ProjectileTypeLoader(resolver));
		assetManager.setLoader(Race.class, new RaceLoader(resolver));
		assetManager.setLoader(Quest.class, new QuestLoader(resolver));
		assetManager.setLoader(Effect.class, new EffectsLoader(resolver));
		assetManager.setLoader(Chatter.class, new ChatterLoader(resolver));
		assetManager.setLoader(ItemGroup.class, new ItemGroupLoader(resolver));
		assetManager.setLoader(InventoryItem.class, new InventoryItemLoader(resolver));
		assetManager.setLoader(Faction.class, new FactionLoader(resolver));
		assetManager.setLoader(AIScript.class, new AIScriptsLoader(resolver));
		assetManager.setLoader(Skin.class, new SkinLoaderWithSounds(resolver));
		assetManager.setLoader(TextureAtlas.class, new TextureAtlasLoader(resolver));
		assetManager.setLoader(ParticleEffect.class, new ParticleEffectLoader(resolver));
		assetManager.setLoader(Perk.class, new PerkLoader(resolver));
		assetManager.setLoader(Spell.class, new SpellLoader(resolver));
		assetManager.setLoader(StringTable.class, new StringTableLoader(resolver));
		assetManager.setLoader(WeatherProfile.class, new WeatherProfileLoader(resolver));
		assetManager.setLoader(TrapType.class, new TrapLoader(resolver));
		assetManager.setLoader(AudioProfile.class, new AudioProfileLoader(resolver));
		assetManager.setLoader(CharacterModel.class, new CharacterModelLoader(resolver));
		assetManager.setLoader(ItemModel.class, new ItemModelLoader(resolver));
		assetManager.setLoader(Role.class, new RoleLoader(resolver));
		assetManager.setLoader(SaveGameDetails.class, new SaveGameDetailsLoader(resolver));
		assetManager.setLoader(StorySequence.class, new StorySequenceLoader(resolver));
	}
	
	public static AtlassedAssetManager getAssetManager() {
		return assetManager;
	}
	
	public static <T> T get(String fileName) {
		return assetManager.get(fileName);
	}
	
	public static <T> T get(String fileName, Class<T> type) {
		return assetManager.get(fileName, type);
	}
	
	public static boolean isLoaded(String fileName) {
		return assetManager.isLoaded(fileName);
	}

	public static boolean isLoaded(String fileName, Class<?> type) {
		return assetManager.isLoaded(fileName, type);
	}
	
	public static TextureRegion getTextureRegion(String fileName) {
		return assetManager.getTextureRegion(fileName);
	}
	
	public static ObjectMap<String, FileHandle> getAssetFiles(String assetFolderPath, String fileExtension) {
		return getAssetFiles(assetFolderPath,fileExtension, FileType.Internal);
	}

	public static ObjectMap<String, FileHandle> getAssetFiles(String assetFolderPath, String fileExtension, FileType fileType) {
		ObjectMap<String, FileHandle> returnValue = new ObjectMap<String, FileHandle>();
		boolean isInBin = false;
		FileHandle assetFolder = Gdx.files.getFileHandle(assetFolderPath, fileType);
		if (!assetFolder.isDirectory()) {
			assetFolder = Gdx.files.getFileHandle(Assets.BIN_FOLDER+assetFolderPath, fileType);
			isInBin = true;
		}
		FileHandle[] assetFiles = fileExtension != null ? assetFolder.list( "."+fileExtension) : assetFolder.list();
		for (FileHandle assetFile : assetFiles) { 
			returnValue.put(isInBin ? assetFile.path().substring(Assets.BIN_FOLDER.length()) : assetFile.path(), assetFile);
		}
		return returnValue;
	}

	public static void gatherAssets(String assetFolderPath, String fileExtension, Class<?> assetClass, ObjectMap<String, String> idFileMap) {
		gatherAssets(assetFolderPath, fileExtension, assetClass, idFileMap, FileType.Internal);
	}
	
	public static void gatherAssets(String assetFolderPath, String fileExtension, Class<?> assetClass, ObjectMap<String, String> idFileMap, FileType fileType) {
		for (Entry<String, FileHandle> entry : getAssetFiles(assetFolderPath, fileExtension, fileType)) { 
			assetManager.load(entry.key, assetClass);
			if (idFileMap != null) {
				idFileMap.put(entry.value.nameWithoutExtension().toLowerCase(Locale.ENGLISH), entry.key);
			}
		}
	}

	/**
	 * Returns true if an asset with the specified name and extension exists. 
	 * 
	 * Case insensitive. 
	 * 
	 * @param assetFolderPath
	 * @param fileName
	 * @param fileExtension
	 * @return
	 */
	public static boolean assetExists(String assetFolderPath, String fileName, String fileExtension) {
		FileHandle assetFolder = Gdx.files.internal(assetFolderPath);
		if (!assetFolder.isDirectory()) {
			assetFolder = Gdx.files.internal(Assets.BIN_FOLDER+assetFolderPath);
		}
		FileHandle[] assetFiles = assetFolder.list("."+fileExtension);
		for (FileHandle assetFile : assetFiles) { 
			if (fileName.toLowerCase().equals(assetFile.nameWithoutExtension().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

}
