package mg.fishchicken.audio;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.assets.AssetContainer;
import mg.fishchicken.core.assets.AssetMap;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * A audio profile is a collection of audio tracks associated with IDs.
 * 
 * They can be used for example to define sets of sounds for
 * different characters.
 *
 */
public class AudioProfile implements XMLLoadable, AssetContainer, AudioContainer {

	public static final String MOVE = "move";
	public static final String SNEAK = "sneak";
	public static final String BARK = "bark";
	private static ObjectMap<String, String> profiles = new ObjectMap<String, String>();

	public static AudioProfile getAudioProfile(String id) {
		return Assets.get(profiles.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Returns an array of all audio profiles that can be selected by the player
	 * during character creation.
	 */
	public static Array<AudioProfile> getAllSelectableAudioProfiles() {
		Array<AudioProfile> returnValue = new Array<AudioProfile>();
		for (String profilePath : profiles.values()) {
			AudioProfile profile = Assets.get(profilePath);
			if (profile.isSelectable()) {
				returnValue.add(profile);
			}
		}
		return returnValue;
	}

	/**
	 * Gathers all Races and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherAudioProfiles() throws IOException {
		Assets.gatherAssets(Configuration.getFolderAudioProfiles(), "xml", AudioProfile.class, profiles);
	}
	
	
	public static final String XML_AUDIO = "audio";
	
	private String id;
	private String s_name;
	private boolean s_selectable;
	private ObjectMap<String, Array<AudioTrack<?>>> audio;
	
	public AudioProfile(FileHandle file) throws IOException{
		id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		s_selectable = false;
		loadFromXML(file);
	}
	
	/**
	 * Gets the track with the given id. If there are multiple tracks
	 * with the same id in this profile, it will return a random one.
	 * 
	 * This will never return null. If no track is found with the supplied
	 * id, a singleton instance of EmptyTrack will be returned instead.
	 * 
	 * @param id
	 * @return
	 */
	public AudioTrack<?> getTrack(String id) {
		Array<AudioTrack<?>> array = audio.get(id.toLowerCase(Locale.ENGLISH));
		return array == null || array.size == 0 ? EmptyTrack.INSTANCE : array.random().createCopy();
	}
	
	/**
	 * Plays a character bark from this audio profile as if it was said
	 * by the supplied character. Will not do anything if barks are turned off
	 * in the configuration.
	 * 
	 * @param id
	 * @return
	 */
	public void playCharacterBark(GameCharacter character) {
		if (Configuration.areCharacterBarksEnabled()) {
			getTrack(BARK).playIfRollSuccessfull(character);
		}
	}
	
	/**
	 * Gets the human readable name of this audio profile.
	 */
	public String getName() {
		return Strings.getString(s_name);
	}
	
	/**
	 * Returns true if this audio profile can be selected by the player
	 * during character creation.
	 */
	public boolean isSelectable() {
		return s_selectable;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public void addTrack(AudioTrack<?> track, String type) {
		Array<AudioTrack<?>> tracks = audio.get(type);
		if (tracks == null) {
			tracks = new Array<AudioTrack<?>>();
			audio.put(type, tracks);
		}
		tracks.add(track);
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		audio = new ObjectMap<String, Array<AudioTrack<?>>>();
		loadFromXMLNoInit(file);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		XMLUtil.readPrimitiveMembers(this, root);
		XMLUtil.readTracks(this, root.getChildByName(XML_AUDIO));
	}
	
	@Override
	public void gatherAssets(AssetMap assetStore) {
		for (Array<AudioTrack<?>> tracks : audio.values()) {
			for (AudioTrack<?> track : tracks) {
				track.gatherAssets(assetStore);
			}
		}
	}

	@Override
	public void clearAssetReferences() {
		for (Array<AudioTrack<?>> tracks : audio.values()) {
			for (AudioTrack<?> track : tracks) {
				track.clearAssetReferences();
			}
		}
	}
}
