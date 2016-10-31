package mg.fishchicken.ui.perks;

import java.util.Comparator;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.AbstractGameCharacter;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.characters.perks.Perk;
import mg.fishchicken.ui.TableStyle;
import mg.fishchicken.ui.perks.PerkButton.PerkButtonStyle;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class PerksComponent extends Table {

	private GameCharacter character;
	private Array<PerkButton> perkButtons;
	private Label perkPointsLabel;
	
	public PerksComponent(GameCharacter character, PerksComponentStyle style, EventListener listener) {
		this(character, style, listener, false, false, true);
	}
	
	public PerksComponent(GameCharacter character, PerksComponentStyle style, EventListener listener, boolean onlyKnown, boolean onlyActivated, boolean displayHeading) {
		super();
		perkButtons = new Array<PerkButton>();
		style.apply(this);
		this.character = character;
		
		if (displayHeading) {
			perkPointsLabel = new Label(Integer.toString(character.stats().getPerkPoints()), style.headingStyle);
			Table heading = new Table(); 
			heading.add(new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"perks")+":", style.headingStyle)).expandX().fill().align(Align.left);
			heading.add(new Label(Strings.getString(AbstractGameCharacter.STRING_TABLE,"perkPoints")+": ", style.headingStyle)).align(Align.right);
			heading.add(perkPointsLabel).align(Align.left);		
			add(heading)
					.fill()
					.top()
					.expandX()
					.pad(style.headingMarginTop, style.headingMarginLeft,
							style.headingMarginBottom, style.headingMarginRight);
			row();
		}

		Table perksTable = new Table();
		ScrollPane backScrollPane = new ScrollPane(perksTable, style.scrollPaneStyle);
		backScrollPane.setFadeScrollBars(false);		
		backScrollPane.setOverscroll(false, false);
		
		Array<Perk> perks = Perk.getAllPerks();
		
		perks.sort(new Comparator<Perk>() {
			@Override
			public int compare(Perk o1, Perk o2) {
				int result = Integer.compare(o1.getLevelRequirement(), o2.getLevelRequirement());
				if (result == 0) {
					result = Integer.compare(o1.getRank(), o2.getRank());
				}
				return result;
			}
		});
		
		int i = 0;
		for (Perk p : perks) {
			if ((!onlyKnown || character.hasPerk(p)) && (!onlyActivated || p.isActivated())) {
				if (i == style.itemsPerRow) {
					perksTable.row();
					i = 0;
				}
				PerkButton perkButton = new PerkButton(character, p, style.perkButtonStyle, onlyKnown);
				perkButton.addListener(listener);
				perkButtons.add(perkButton);
				perksTable.add(perkButton).space(style.perkButtonSpacing).top().left();
				++i;
			}
		}
		
		add(backScrollPane).fill().expand().top().width(backScrollPane.getPrefWidth());
		recomputePerks();
	}
	
	public void recomputePerks() {
		for (PerkButton button : perkButtons) {
			button.recomputeColor(character);
		}
		if (perkPointsLabel != null) {
			perkPointsLabel.setText(Integer.toString(character.stats().getPerkPoints()));
		}
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
	}
	
	static public class PerksComponentStyle extends TableStyle {
		public int itemsPerRow, perkButtonSpacing, headingMarginTop, headingMarginBottom, headingMarginLeft, headingMarginRight;
		public PerkButtonStyle perkButtonStyle;
		public LabelStyle headingStyle;
		public ScrollPaneStyle scrollPaneStyle;
	}
}
