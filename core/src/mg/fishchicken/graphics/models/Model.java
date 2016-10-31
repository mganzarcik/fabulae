package mg.fishchicken.graphics.models;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.saveload.XMLLoadable;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Model implements XMLLoadable, ThingWithId {
	
	private String s_id;
	private String s_name;
	private String s_animationTextureFile;
	private String s_animationInfoFile;

	public Model(FileHandle file) throws IOException {
		s_id = file.nameWithoutExtension().toLowerCase(Locale.ENGLISH);
		loadFromXML(file);
	}
	
	public String getId() {
		return s_id;
	}
	
	public String getName() {
		return s_name;
	}	

	public String getAnimationTextureFile() {
		return s_animationTextureFile;
	}

	public String getAnimationInfoFile() {
		return s_animationInfoFile;
	}

	@Override
	public void loadFromXML(FileHandle raceFile) throws IOException {
		loadFromXMLNoInit(raceFile);
	}

	@Override
	public void loadFromXMLNoInit(FileHandle file) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(file);
		XMLUtil.handleImports(this, file, root);
		XMLUtil.readPrimitiveMembers(this, root);
		s_animationTextureFile = Configuration.addModulePath(s_animationTextureFile);
		s_animationInfoFile = Configuration.addModulePath(s_animationInfoFile);
	}
	
	@Override
	public String toString() {
		return s_id;
	}
}
