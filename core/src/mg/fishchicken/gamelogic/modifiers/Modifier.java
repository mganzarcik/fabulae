package mg.fishchicken.gamelogic.modifiers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import mg.fishchicken.core.ThingWithId;
import mg.fishchicken.core.saveload.XMLSaveable;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * This represents a stat modifier for a character.
 * 
 * Stat modifiers are added to characters and modify (surprisingly!) their
 * stats.
 * 
 * This means that if you add a +10 HP modifier to a char, he/she will have 10 more
 * hitpoints.
 * 
 * Modifiers should ideally be used to track and store all external changes to
 * character stats, since modifiers are also visible in the UI and therefore
 * inform the player about the exact effects different things have on his
 * characters.
 * 
 * For example, a cursed pin of slaying can have a -1 AP penalty modifier. If a character
 * equips this pin, the modifier will be applied to her as well, so her AP
 * will be reduced by 1. The player will be able to see this effect if she
 * investigates the AP stat and read exactly where the modifier is coming from,
 * which is handy, because she will immediately know what is wrong and why.
 * 
 * @author Annun
 * 
 */
public class Modifier implements XMLSaveable, ThingWithId {
	
	public static String XML_MODIFIER = "modifier";
	public static final String XML_SKILLS = "Skills";
	
	private String s_id, s_name;
	private boolean s_visible; // if false, the modifier will not be shown in the effects overview of the character
	private ObjectMap<ModifiableStat, Float> mods;
	
	/**
	 * Creates a new modifier with all mods
	 * defaulted to zero. 
	 */
	public Modifier() {
		s_name = "";
		mods = new ObjectMap<ModifiableStat, Float>();
		s_visible = true;
	}
	
	/**
	 * Creates a new modifier with all mods
	 * defaulted to zero. 
	 */
	public Modifier(String id, String name) {
		this();
		s_name = name;
		s_id = id;
	}
	
	/**
	 * Creates a new modifier with all mods
	 * defaulted to zero. 
	 */
	public Modifier(String id, String name, boolean visible) {
		this();
		s_name = name;
		s_id = id;
		s_visible = false;
	}
	
	/**
	 * Adds the supplied modifier to this modifier.
	 * 
	 * This will simply add together all mods from 
	 * this Modifier and all mods from the supplied modifier.
	 * 
	 * Even multiplier mods will be added, not multiplicated!
	 * 
	 * @param mod
	 */
	public void add(Modifier modifier) {
		for (ModifiableStat stat : modifier.mods.keys()) {
			Float value = add(mods.get(stat), modifier.mods.get(stat));
			if (value != null) {
				mods.put(stat, value);
			}
		}
	}
	
	/**
	 * Substracts the supplied modifier from this modifier.
	 * 
	 * This will simply substract all mods in the supplied modifier from 
	 * this Modifier's mods.
	 * 
	 * Even multiplier mods will be substracted, not divided!
	 * 
	 * @param mod
	 */
	public void substr(Modifier modifier) {
		for (ModifiableStat stat : modifier.mods.keys()) {
			Float value = add(mods.get(stat), -modifier.mods.get(stat));
			if (value != null) {
				mods.put(stat, value);
			}
		}
	}
	
	private Float add(Float value1, Float value2) {
		if (value1 == null) {
			return value2;
		}
		if (value2 == null) {
			return value1;
		}
		
		return value1+value2;
	}
	
	public String getId() {
		return s_id;
	}

	public Modifier setId(String id) {
		s_id = id;
		return this;
	}
	
	/**
	 * Whether or not this Modifier is visible
	 * on the effects summary screen of a character.
	 * 
	 * Not visible modifiers are still shown in tooltips
	 * on the stats they modify.
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return s_visible;
	}
	
	/**
	 * Sets this modifier as visible or not.
	 * 
	 * @see Modifier#isVisible()
	 * @param visible
	 */
	public Modifier setVisible(boolean visible) {
		s_visible = visible;
		return this;
	}
	
	public String getName() {
		return s_name;
	}
	public Modifier setName(String name) {
		s_name = name;
		return this;
	}

	/**
	 * Sets the modifier for the supplied stat.
	 * 
	 * @param stat
	 * @param value
	 */
	public Modifier setMod(ModifiableStat stat, float value) {
		mods.put(stat, value);
		return this;
	}
	
	/**
	 * Gets the modifier for the supplied stat.
	 * 
	 * @param stat
	 * @return
	 */
	public float getMod(ModifiableStat stat) {
		Float value = mods.get(stat);
		if (value == null) {
			if (stat.isMultiplier()) {
				value = 1f;
			} else {
				value = 0f;
			}
		}
		return value;
	}
	
	/**
	 * Gets the modifier of the supplied stat as a string.
	 * 
	 * @param stat
	 * @return
	 */
	public String getModAsString(ModifiableStat stat) {
		float value = getMod(stat);
		String returnValue = stat.getSign(value);
		if (stat.isMultiplier()) {
			returnValue += Float.toString(value);
		} else {
			returnValue += Integer.toString((int)value);
		}
		return returnValue;
	}
	
	/** Returns true if any of the
	 * modifiers properties are non zero
	 * (i.e. it actually modifies something).
	 * 
	 * @return
	 */
	public boolean isNonZero() {
		for (ModifiableStat mod : mods.keys()) {
			if (!isEmpty(mod)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the supplied stat
	 * is empty in this modifier (e.g. it does
	 * not actually modify anything).
	 * 
	 * @param stat
	 * @return
	 */
	public boolean isEmpty(ModifiableStat stat) {
		Float value = mods.get(stat);
		if (value == null) {
			return true;
			
		}
		if ((value != 0 && !stat.isMultiplier()) || (value != 1 && stat.isMultiplier())) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return toString(true);
	}
	
	/**
	 * Prints this Modifier into a user-friendly string.
	 * 
	 * If includeName is set to true, the resulting string
	 * will also include the name of the Modifier.
	 * 
	 * @param includeName
	 * @return
	 */
	public String toString(boolean includeName) {
		StringBuilder fsb = StringUtil.getFSB();
		if (includeName) {
			fsb.append(getName());
			fsb.append(": ");
		}
		
		for (ModifiableStat mod : ModifiableStat.values()) {
			fsb.append(modToString(mod, ", "));
		}
	
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		
		if (returnValue.endsWith(": ")) {
			returnValue = "";
		} else if (returnValue.endsWith(", ")) {
			returnValue = returnValue.substring(0, returnValue.lastIndexOf(", "));
		}
		
		return returnValue;
	}
	
	private String modToString(ModifiableStat mod, String separator) {
		float value = getMod(mod);
		boolean multiply = mod.isMultiplier();
		
		if ((value == 0 && !multiply) || (multiply && value == 1)) {
			return "";
		}
		
		StringBuilder fsb = StringUtil.getFSB();
		
		fsb.append(mod.toUIString());
		fsb.append(": ");
		fsb.append(getModAsString(mod));

		if (separator != null) {
			fsb.append(separator);
		}
		String returnValue = fsb.toString();
		StringUtil.freeFSB(fsb);
		
		return returnValue;
	}
	
	public void loadFromXML(Element root) {
		ObjectMap<String, String> attributes = root.getAttributes();
		if (attributes != null) {
			for (String statName : attributes.keys()) {
				ModifiableStat stat = ModifiableStat.valueOf(statName.toUpperCase(Locale.ENGLISH));
				setMod(stat, Float.parseFloat(attributes.get(statName)));
			}
		}
		
		XMLUtil.readPrimitiveMembers(this, root.getChildByName(XMLUtil.XML_PROPERTIES));
	}
	
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(XML_MODIFIER);
		for (ModifiableStat stat : mods.keys()) {
			if (!isEmpty(stat)) {
				float value = getMod(stat);
				writer.attribute(stat.toString(), value);
			}
		}
		writer.element(XMLUtil.XML_PROPERTIES);
		XMLUtil.writePrimitives(this, writer);
		writer.pop();
		
		writer.pop();
	}
	
	/**
	 * Creates a new copy of this Modifier.
	 * 
	 * @return
	 */
	public Modifier copy() {
		Modifier copy = new Modifier();
		copy.setName(getName());
		copy.setVisible(isVisible());
		for (ModifiableStat stat : mods.keys()) {
			copy.mods.put(stat, mods.get(stat));
		}
		return copy;
	}
	
	/**
	 * Prints all the Modifiers in the suppled ModifierContainer
	 * in a user-friendly String. The supplied separator
	 * is used to separate individual Modifiers. If includeNames
	 * is true, the Modifiers names will also be printed.
	 * @param mc
	 * @param separator
	 * @param includeNames
	 * @return
	 */
	public static String getModifiersAsString(ModifierContainer mc, String separator, boolean includeNames) {
		StringBuilder builder = StringUtil.getFSB();
		Iterator<Modifier> modifiers = mc.getModifiers();
		int i = 0;
		while (modifiers.hasNext()) {
			Modifier modifier = modifiers.next();
			if (modifier.isNonZero()) {
				if (i > 0) {
					builder.append(separator);
				}
				builder.append(modifier.toString(includeNames));
			}
			++i;
		}
		String returnValue = builder.toString();
		StringUtil.freeFSB(builder);
		return returnValue;
	}
}
