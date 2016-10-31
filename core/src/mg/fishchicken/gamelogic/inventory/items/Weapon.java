package mg.fishchicken.gamelogic.inventory.items;

import java.io.IOException;

import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter.Skill;
import mg.fishchicken.gamelogic.effects.Effect;
import mg.fishchicken.gamelogic.effects.EffectContainer;
import mg.fishchicken.gamelogic.effects.EffectParameter;
import mg.fishchicken.gamelogic.inventory.Inventory;
import mg.fishchicken.gamelogic.inventory.Inventory.BagType;
import mg.fishchicken.gamelogic.inventory.Inventory.ItemSlot;
import mg.fishchicken.gamelogic.inventory.InventoryContainer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Weapon extends InventoryItem implements EffectContainer {
	
	private int s_weaponBonus;
	private int s_weaponDamageMin;
	private int s_weaponDamageMax;
	private Skill s_skill;
	private boolean s_isTwoHanded;
	private boolean s_isRanged;
	private String s_projectile;
	private OrderedMap<Effect, Array<EffectParameter>> effects;
	
	public Weapon() {
		super();
	}
	
	public Weapon(FileHandle file) throws IOException {
		super(file);
	}
	
	public boolean isRanged() {
		return s_isRanged;
	}
	
	public boolean isTwoHanded() {
		return s_isTwoHanded;
	}
	
	public Skill getWeaponSkill() {
		return s_skill;
	}
	
	public int getWeaponDamageMin() {
		return s_weaponDamageMin;
	}
	
	public int getWeaponDamageMax() {
		return s_weaponDamageMax;
	}
	
	public int getWeaponBonus() {
		return s_weaponBonus;
	}
	
	public boolean hasProjectile() {
		return (s_projectile != null);
	}
	
	public String getProjectile() {
		return s_projectile;
	}
	
	@Override
	public ItemSlot[] getAllowedSlots() {
		ItemSlot[] returnValue = super.getAllowedSlots();
		if (returnValue.length < 1) {
			returnValue = Inventory.HAND_SLOTS;
		}
		return returnValue;
	}
	
	@Override
	public int canBeAddedTo(BagType bag, int slot, InventoryContainer container) {
		if (BagType.EQUIPPED == bag) {
			boolean freeSlot = false;
			if (!isTwoHanded()) {
				freeSlot = (ItemSlot.RIGHTHAND.getSlot() == slot
						&& (container.getInventory().getEquipped(ItemSlot.LEFTHAND) == null || !container
								.getInventory().getEquipped(ItemSlot.LEFTHAND)
								.isTwoHanded())
	
				|| ItemSlot.LEFTHAND.getSlot() == slot
						&& (container.getInventory()
								.getEquipped(ItemSlot.RIGHTHAND) == null || !container
								.getInventory().getEquipped(ItemSlot.RIGHTHAND)
								.isTwoHanded()));
			} else {
				freeSlot = (ItemSlot.RIGHTHAND.getSlot() == slot
						&& container.getInventory().getEquipped(ItemSlot.LEFTHAND) == null || ItemSlot.LEFTHAND
						.getSlot() == slot
						&& container.getInventory().getEquipped(ItemSlot.RIGHTHAND) == null);
			}
			
			if (!freeSlot) {
				return 0;
			}
		}
		
		return super.canBeAddedTo(bag, slot, container);
	}
	
	public String getWeaponDamageAsString(GameCharacter character) {
		int minDamage = Math.round(character.stats().applyDamageModifiers((s_weaponDamageMin
				+ s_weaponBonus + character.stats().skills()
				.getSkillRank(getWeaponSkill())), false));
		int maxDamage = Math.round(character.stats().applyDamageModifiers((s_weaponDamageMax
				+ s_weaponBonus + character.stats().skills()
				.getSkillRank(getWeaponSkill())), false));
		if (maxDamage < minDamage) {
			maxDamage = minDamage;
		}
		return minDamage == maxDamage ? Integer.toString(minDamage) : minDamage + " - " + maxDamage;
	}
	
	@Override
	public void addEffect(Effect effect, Array<EffectParameter> effectParameters) {
		effects.put(effect, effectParameters);
	}
	
	@Override
	public ObjectMap<Effect, Array<EffectParameter>> getEffects() {
		return effects;
	}
	
	/**
	 * Executes the effects of this Perk as if
	 * it was used by the supplied user on the
	 * supplied target.
	 * 
	 * @param user
	 * @param target
	 * @return
	 */
	public void executeEffects(GameCharacter user, GameCharacter target) {
		for (Effect effect : effects.keys()) {
			effect.executeEffect(this, user, target, effects.get(effect));
		}
	}
	
	@Override
	public void loadFromXML(FileHandle file) throws IOException {
		effects =  new OrderedMap<Effect, Array<EffectParameter>>();
		super.loadFromXML(file);
		if (s_weaponDamageMax < s_weaponDamageMin) {
			throw new GdxRuntimeException("Max damage cannot be less than min damage for "+getId());
		}
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		XMLUtil.readEffect(this, root.getChildByName(XMLUtil.XML_EFFECTS));
	}
	
	@Override
	public InventoryItem createNewInstance() {
		Weapon newInstance = (Weapon) super.createNewInstance();
		newInstance.effects = new OrderedMap<Effect, Array<EffectParameter>>();
		for (Entry<Effect, Array<EffectParameter>> entry : this.effects.entries()) {
			newInstance.effects.put(entry.key, entry.value);
		}
		return newInstance;		
	}
}
