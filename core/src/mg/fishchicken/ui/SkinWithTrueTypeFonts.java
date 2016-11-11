package mg.fishchicken.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.SkylineStrategy;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;

public class SkinWithTrueTypeFonts extends Skin {
	private PixmapPacker packer = null;
	
	/** Creates a skin containing the resources in the specified skin JSON file. If a file in the same directory with a ".atlas"
	 * extension exists, it is loaded as a {@link TextureAtlas} and the texture regions added to the skin. The atlas is
	 * automatically disposed when the skin is disposed. */
	public SkinWithTrueTypeFonts(FileHandle skinFile) {
		super(skinFile);
	}
	
	/**
	 * Creates a skin containing the texture regions from the specified atlas.
	 * The atlas is automatically disposed when the skin is disposed.
	 */
	public SkinWithTrueTypeFonts(TextureAtlas atlas) {
		super(atlas);
	}

	protected Json getJsonLoader (final FileHandle skinFile) {
		Json loader = super.getJsonLoader(skinFile);
		
		final Serializer<BitmapFont> originialSerializer = loader.getSerializer(BitmapFont.class);
		loader.setSerializer(BitmapFont.class, new ReadOnlySerializer<BitmapFont>() {
			@SuppressWarnings("rawtypes")
			public BitmapFont read (Json json, JsonValue jsonData, Class type) {
				String path = json.readValue("file", String.class, jsonData);
				FileHandle fontFile = skinFile.parent().child(path);
				if (!fontFile.exists()) {
					fontFile = Gdx.files.internal(path);
				}
				if (!fontFile.exists()) {
					throw new SerializationException("Font file not found: " + fontFile);
				}
				
				boolean isTrueType = "ttf".equals(fontFile.extension());

				if (isTrueType) {
					Boolean markupEnabled = json.readValue("markupEnabled", Boolean.class, false, jsonData);
					FreeTypeFontParameter parameter = new FreeTypeFontParameter();
					parameter.size = json.readValue("size", int.class, -1, jsonData);
					parameter.borderWidth = json.readValue("borderWidth", int.class, 0, jsonData);
					parameter.color= json.readValue("color", Color.class, Color.WHITE, jsonData);
					parameter.borderColor= json.readValue("borderColor", Color.class, Color.BLACK, jsonData);
					parameter.borderStraight = json.readValue("borderStraight", boolean.class, false, jsonData);
					parameter.shadowOffsetX  = json.readValue("shadowOffsetX", int.class, 0, jsonData);
					parameter.shadowOffsetY  = json.readValue("shadowOffsetY", int.class, 0, jsonData);
					parameter.shadowColor  = json.readValue("shadowColor", Color.class, new Color(0, 0, 0, 0.75f), jsonData);
					parameter.flip = json.readValue("flip", Boolean.class, false, jsonData);
					parameter.packer = getPacker();
					
					FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
					BitmapFont font = generator.generateFont(parameter);
					generator.dispose();
					font.getData().markupEnabled = markupEnabled;
					return font;
				} else {
					return originialSerializer.read(json, jsonData, type);
				}
			}
		});
		
		return loader;
	}
	
	/**
	 * Due to a weird bug during ttf font disposal, we have to create and maintain our
	 * own packer. Otherwise the whole thing will crash once we have more than 4 fonts
	 * created and added to the skin via FreeTypeFontGenerator and then try to dispose
	 * the skin. This is very baffling.
	 * 
	 * Having our own packer seems to fix the issue, but honestly I have no idea why.
	 * 
	 * @return
	 */
	private PixmapPacker getPacker() {
		if (packer == null) {
			packer = new PixmapPacker(1024, 512, Format.RGBA8888, 1, false, new SkylineStrategy());
		}
		return packer;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (packer != null) {
			packer.dispose();
		}
	}
}
