package mg.fishchicken.ui.tooltips;

import mg.fishchicken.core.GameObject;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.actions.AttackAction;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.combat.CombatManager;
import mg.fishchicken.gamelogic.combat.CombatPath;
import mg.fishchicken.gamelogic.factions.Faction;
import mg.fishchicken.gamelogic.inventory.items.Weapon;
import mg.fishchicken.gamestate.Tile;
import mg.fishchicken.pathfinding.Path.Step;
import mg.fishchicken.ui.UIManager;

import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class CombatTooltip extends CompositeTooltip  {

	private Array<Weapon> attackingWeapons;
	private CombatTooltipStyle style;
	
	public CombatTooltip() {
		super(UIManager.getSkin().get("default", CombatTooltipStyle.class));
		this.style = (CombatTooltipStyle) super.style;
		this.attackingWeapons = new Array<Weapon>();
	}
	
	public void update(String actionText, CombatPath path) {
		clear();
		addLine(actionText, style.subheadingStyle);
		
		Object target = path.getTarget();
		
		if (target != null) {
			if (target instanceof GameObject) {
				addLine(((GameObject)target).getName(), style.headingStyle);
			}
			if (target instanceof GameCharacter) {
				GameCharacter targetCharacter = (GameCharacter) target;
				if (targetCharacter.getDescription() != null && !targetCharacter.getDescription().trim().isEmpty()) {
					addLine(targetCharacter.getDescription());
				}
				addLine();
				addLine(Strings.getString(AbstractGameCharacter.STRING_TABLE, "health")+": "+getHealthDescription(targetCharacter));
				addLine(Strings.getString(Faction.STRING_TABLE, "disposition")+": "+targetCharacter.getFaction().getDispositionTowardsPlayerAsString()+" "+Strings.getString(Faction.STRING_TABLE,"you"));
				
				GameCharacter attacker = GameState.getPlayerCharacterGroup().getGroupLeader();
				if (attacker != null && AttackAction.class.equals(path.getAction())) {
					Tile targetTile = targetCharacter.position().tile();
					Tile attackerTile = attacker.position().tile();
					int fromX, fromY; 
					if (path.getLength() < 2) {
						fromX = attackerTile.getX();
						fromY = attackerTile.getY();
					} else {
						Step nextToLastStep = path.getStep(path.getLength() - 2);
						fromX = nextToLastStep.getX();
						fromY = nextToLastStep.getY();
					}
					AttackAction.determineAttackingWeapons(attacker, fromX, fromY, targetTile.getX(), targetTile.getY(), attackingWeapons);
					StringBuilder chance = StringUtil.getFSB();
					for (int i = 0; i < attackingWeapons.size; ++i) {
						chance.append(AttackAction.getChanceToHit(attacker, targetCharacter, attackingWeapons.get(i), fromX, fromY));
						chance.append("%");
						if (i < attackingWeapons.size-1) {
							chance.append(" / ");
						}
					} if (attackingWeapons.size < 1) {
						chance.append(AttackAction.getChanceToHit(attacker, targetCharacter, null, fromX, fromY));
						chance.append("%");
					}
					addLine(Strings.getString(CombatManager.STRING_TABLE, "chanceToHit")+": " + chance.toString());
					StringUtil.freeFSB(chance);
				}
			}
		}
	}
	
	private String getHealthDescription(GameCharacter character) {
		int hpPercentage = (int)((100f / character.stats().getHPMax())*character.stats().getHPAct());
		if (hpPercentage >=  100) {
			return Strings.getString(GameCharacter.STRING_TABLE, "Uninjured");
		} else if (hpPercentage > 75) {
			return Strings.getString(GameCharacter.STRING_TABLE, "bruised");
		} else if (hpPercentage > 50) {
			return Strings.getString(GameCharacter.STRING_TABLE, "wounded");
		} else if (hpPercentage > 25) {
			return Strings.getString(GameCharacter.STRING_TABLE, "severelywounded");
		} else {
			return Strings.getString(GameCharacter.STRING_TABLE, "almostdead");
		}
	}
	
	public static class CombatTooltipStyle extends SimpleTooltipStyle {
		protected LabelStyle headingStyle, subheadingStyle;
	}
}
