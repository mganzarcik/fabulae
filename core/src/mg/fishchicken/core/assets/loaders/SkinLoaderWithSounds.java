package mg.fishchicken.core.assets.loaders;

import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.ui.SkinWithTrueTypeFonts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

/**
 * This will also load any files loaded in the "sounds" directory located with 
 * the skin file into the skin as Sound instances.
 *  
 * @author ANNUN
 *
 */
public class SkinLoaderWithSounds extends SkinLoader {

	private Array<AssetDescriptor<Sound>> sounds;
	
	public SkinLoaderWithSounds(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Skin loadSync (AssetManager manager, String fileName, FileHandle file, SkinParameter parameter) {
		String textureAtlasPath = file.pathWithoutExtension() + ".atlas";
		ObjectMap<String, Object> resources = null;
		if (parameter != null) {
			if (parameter.textureAtlasPath != null){
				textureAtlasPath = parameter.textureAtlasPath;
			}
			if (parameter.resources != null){
				resources = parameter.resources;
			}
		}
		TextureAtlas atlas = manager.get(textureAtlasPath, TextureAtlas.class);
		Skin skin = new SkinWithTrueTypeFonts(atlas);
		if (resources != null) {
			for (Entry<String, Object> entry : resources.entries()) {
				skin.add(entry.key, entry.value);
			}
		}
		
		for (AssetDescriptor<Sound> sound : sounds) {
			skin.add(sound.file.nameWithoutExtension(), Gdx.audio.newSound(sound.file), Sound.class);
		}
		
		skin.load(file);
		return skin;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, SkinParameter parameter) {
		sounds = new Array<AssetDescriptor<Sound>>();
		
		ObjectMap<String, FileHandle> soundFiles = Assets.getAssetFiles(
				file.parent().child("sounds").path(), null, file.type());
		
		for (FileHandle soundFile : soundFiles.values()) {
			AssetDescriptor<Sound> ad = new AssetDescriptor<Sound>(soundFile, Sound.class);
			sounds.add(ad);
		}
		
		return super.getDependencies(fileName, file, parameter);
	}
	
}
