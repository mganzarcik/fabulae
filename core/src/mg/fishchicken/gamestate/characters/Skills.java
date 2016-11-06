package mg.fishchicken.gamestate.characters;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.modifiers.ModifiableStat;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.gamelogic.modifiers.ModifierContainer;
import mg.fishchicken.gamestate.ObservableState;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Skills extends ObservableState<Skills, Skills.SkillChange>  {
	
	public static final String XML_SKILL = "skill";
	
	private ObjectMap<Skill, Integer> skills;
	private ModifierContainer mc;
	private SkillChange changes;
	
	public Skills() {
		this.mc = null;
		skills = new ObjectMap<Skill, Integer>();
		changes =  new SkillChange();
	}
	
	public Skills(ModifierContainer mc) {
		this();
		this.mc = mc;
	}
	
	/**
	 * Sets the values of these skills to the values of the
	 * supplied skills.
	 * 
	 * Any skills not contained in the supplied object will be 
	 * zeroed out.
	 * 
	 * @param skills
	 */
	public void set(Skills skills) {
		this.skills.clear();
		for (Entry<Skill, Integer> entry : skills.skills.entries()) {
			this.skills.put(entry.key, entry.value);
		}
	}
	
	/**
	 * Returns the rank of the supplied skill.
	 * The rank will also include any active modifiers.
	 * 
	 */
	public int getSkillRank(Skill skill) {
		int returnValue =  getBaseSkillRank(skill);
		
		if (mc != null) {		
			Iterator<Modifier> iterator = mc.getModifiers();
			while (iterator.hasNext()) {
				returnValue += iterator.next().getMod(ModifiableStat.valueOf(skill));
			}
			
			if (returnValue < 0) {
				returnValue = 0;
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns the base rank of the supplied skill.
	 * The rank will not include any active modifiers.
	 * 
	 */
	public int getBaseSkillRank(Skill skill) {
		if (!skills.containsKey(skill)) {
			skills.put(skill, 0);
		} 
		return  skills.get(skill);
	}
	
	/**
	 * Sets the rank of the supplied skill
	 * to the supplied value.
	 * 
	 */
	public void setSkillRank(String skillName, int rank) {
		setSkillRank(Skill.valueOf(skillName.toUpperCase(Locale.ENGLISH)), rank);
		
	}
	
	/**
	 * Sets the rank of the supplied skill
	 * to the supplied value.
	 * 
	 */
	public void setSkillRank(Skill skill, int rank) {
		changes.skill = skill;
		changes.oldValue = getBaseSkillRank(skill);
		skills.put(skill, rank);
		changes.newValue = rank;
		changed(changes);
	}
	
	/**
	 * Increases the rank of the supplied skill by 1.
	 * 
	 * @param skill
	 */
	public void increaseSkillRank(Skill skill) {
		setSkillRank(skill,getBaseSkillRank(skill)+1);
	}
	
	/**
	 * Decreases the rank of the supplied skill by 1.
	 * 
	 * @param skill
	 */
	public void decreaseSkillRank(Skill skill) {
		setSkillRank(skill,getBaseSkillRank(skill)-1);
	}
	
	/**
	 * Read the skills from the suppled XML element and loads them
	 * into the supplied NonPlayerCharacter.
	 * 
	 * The XML element should contain children in the following format:
	 * <pre>
	 * &lt;Skill name="skillName" value="skillValue" /&gt;
	 * </pre>
	 * @param skillsContainer
	 * @param skillsElement
	 */
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		Element skillsElement = root.getChildByName(getXMLElementName());
		if (skillsElement != null) {
			for (int i = 0; i< skillsElement.getChildCount(); ++i) {
				Element variable = skillsElement.getChild(i);
				setSkillRank(variable.getAttribute(XMLUtil.XML_ATTRIBUTE_ID).toUpperCase(Locale.ENGLISH),Integer.parseInt(variable.getAttribute(XMLUtil.XML_ATTRIBUTE_VALUE)));
			}
		}
	}
	
	/**
	 * Writes the skills from the suppled SkillContainer using the supplied
	 * XmlWriter.
	 * 
	 * The resulting XML snippet will look like this:
	 * <pre>
	 * &lt;Skills&gt;
	 * 	&lt;Skill name="skillName" value="skillValue" /&gt;
	 * 	...
	 * &lt;/Skills&gt;
	 * </pre>
	 * @param SkillsContainer
	 * @param XmlWriter
	 */
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.element(getXMLElementName());
		for (Skill skill : Skill.values()) {
			int rank = getBaseSkillRank(skill);
			writer.element(XML_SKILL).attribute(XMLUtil.XML_ATTRIBUTE_ID, skill.toString()).attribute(XMLUtil.XML_ATTRIBUTE_VALUE, rank).pop();
		}
		writer.pop();
	}
	
	public static class SkillChange {
		private Skill skill;
		private int oldValue, newValue;
		
		public SkillChange() {
		}
		
		public Skill getSkill() {
			return skill;
		}
		
		public int getOldValue() {
			return oldValue;
		}
		
		public int getNewValue() {
			return newValue;
		}
	}
}
