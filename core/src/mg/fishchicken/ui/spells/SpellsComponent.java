package mg.fishchicken.ui.spells;

import java.util.Comparator;

import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.magic.Spell;
import mg.fishchicken.ui.perks.PerksComponent.PerksComponentStyle;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class SpellsComponent extends Table {

	GameCharacter character;
	private Array<SpellButton> spellButtons;
	
	public SpellsComponent(GameCharacter character, PerksComponentStyle style, EventListener listener) {
		super();
		spellButtons = new Array<SpellButton>();
		this.character = character;
		
		Table spellsTable = new Table();
		ScrollPane backScrollPane = new ScrollPane(spellsTable, style.scrollPaneStyle);
		backScrollPane.setFadeScrollBars(false);		
		backScrollPane.setOverscroll(false, false);
		
		Array<Spell> spells = character.getSpells();
		
		spells.sort(new Comparator<Spell>() {
			@Override
			public int compare(Spell o1, Spell o2) {
				int result = Integer.compare(o1.getLevelRequirement(), o2.getLevelRequirement());
				if (result == 0) {
					result = Integer.compare(o1.getRank(), o2.getRank());
				}
				return result;
			}
		});
		
		int i = 0;
		for (Spell s : spells) {
			if (i == style.itemsPerRow) {
				spellsTable.row();
				i = 0;
			}
			SpellButton spellButton = new SpellButton(character, s, style.perkButtonStyle);
			spellButton.addListener(listener);
			spellButtons.add(spellButton);
			spellsTable.add(spellButton).space(5).top().left();
			++i;
		}
		
		add(backScrollPane).fill().expand().top().width(backScrollPane.getPrefWidth());
		recomputeSpells();
	}
	
	public void recomputeSpells() {
		for (SpellButton button : spellButtons) {
			button.recomputeColor(character);
		}
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
	}
}
