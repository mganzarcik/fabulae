package mg.fishchicken.core.assets.loaders;

import java.io.IOException;

import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader.Element;

public class LoaderUtil {

	public static <T, P extends AssetLoaderParameters<T>> void handleImports(
			AssetLoader<T, P> loader, P parameter,
			@SuppressWarnings("rawtypes") Array<AssetDescriptor> dependencies, FileHandle parentFile,
			Element root) throws IOException {
		Array<Element> imports = root.getChildrenByName(XMLUtil.XML_IMPORT);
		for (Element singleImport : imports) {
			String filename = singleImport.get(XMLUtil.XML_FILENAME);
			FileHandle file = parentFile.parent().child(filename);
			if (!file.exists()) {
				throw new GdxRuntimeException("Import " + file.path()
						+ " from import for " + parentFile.name()
						+ " does not exist.");
			}
			dependencies.addAll(loader.getDependencies(filename, file, parameter));
		}
	}
}
