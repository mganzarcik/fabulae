package mg.fishchicken.core.assets.loaders;

import java.util.Locale;

import mg.fishchicken.core.i18n.Strings.StringTable;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class StringTableLoader extends SimpleAsynchronousLoader<StringTable, StringTableLoader.StringResourceParameter> {
	
	public StringTableLoader () {
		super(new InternalFileHandleResolver());
	}
	
	public StringTableLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public StringTable load (AssetManager assetManager, String fileName, FileHandle file, StringResourceParameter parameter) {
		FileHandle csvFile = resolve(fileName);
		StringTable returnValue = new StringTable();
		
		for (JsonValue child : new JsonReader().parse(csvFile)) {
			addValue(child, returnValue, "");
		}
		
		return returnValue;
	}
	
	private void addValue(JsonValue value, StringTable table, String keyPrefix) {
		if (value.isValue()) {
			table.add(keyPrefix+value.name().toLowerCase(Locale.ENGLISH), value.asString().trim());
		} else {
			for (JsonValue child : value) {
				addValue(child, table, keyPrefix+value.name()+".");
			}
		} 
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, StringResourceParameter parameter) {
		return null;
	}

	static public class StringResourceParameter extends AssetLoaderParameters<StringTable> {
	}
}
