package mg.fishchicken.ui.effects;

import java.util.Comparator;
import java.util.Iterator;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.effects.Effect.PersistentEffect;
import mg.fishchicken.gamelogic.modifiers.Modifier;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.effects.ActiveEffectLabel.ActiveEffectLabelStyle;

import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ForcedScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class ActiveEffectsComponent extends Table {

	private ActiveEffectsComponentStyle style;
	
	public ActiveEffectsComponent(GameCharacter character, ActiveEffectsComponentStyle style, int height) {
		
		this.style = style;
		
		Table descriptionTable = new Table();
		
		descriptionTable.add(new Label("", style.headingStyle)).width(style.textPaddingLeft).fill();
		Cell<Label> headingCell = descriptionTable.add(new Label(Strings.getString(UIManager.STRING_TABLE, "activeEffects"), style.headingStyle)).fillX().left().prefWidth(style.width-2*style.textPaddingLeft-2*style.textPaddingRight);
		descriptionTable.add(new Label("", style.headingStyle)).width(style.textPaddingRight).fill().padRight(style.borderWidth);
		
		descriptionTable.row();
		
		descriptionTable.add(new ForcedScrollPane(buildEffects(character), style.scrollPaneStyle)).fill().expand().prefHeight(height-headingCell.getPrefHeight()).colspan(3);
		
		add(descriptionTable);
	}
	
	private Table buildEffects(GameCharacter character) {
		Table effectsTable = new Table();
		Array<EffectDescription> effects = getActiveEffects(character);
		
		for (EffectDescription ed : effects) {
			effectsTable.add(new Label("", style.textStyle)).width(style.textPaddingLeft).fill();
			effectsTable.add(new ActiveEffectLabel(ed.name+": ",ed, style.textStyle, false)).fill();
			effectsTable.add(new ActiveEffectLabel(ed.description, ed, style.textStyle, true)).expandX().fill();
			effectsTable.add(new Label("", style.textStyle)).width(style.textPaddingRight).fill();
			effectsTable.row();
		}
		
		effectsTable.add(new Label(" ", style.textStyle)).fill().expand().colspan(4);
		
		return effectsTable;
	}
	
	public static Array<EffectDescription> getActiveEffects(GameCharacter character) {
		Array<EffectDescription> effectDescriptions = new Array<EffectDescription>();

		Iterator<Modifier> modifiers = character.stats().getModifiers();
		while (modifiers.hasNext()) {
			Modifier mod = modifiers.next();

			if (mod.isVisible() && mod.isNonZero()) {
				effectDescriptions.add(new EffectDescription(mod));
			}
		}

		Array<PersistentEffect> pes = character.getPersistentEffectsByType();
		for (PersistentEffect pe : pes) {
			if (pe.isVisible()) {
				effectDescriptions.add(new EffectDescription(pe));
			}
		}

		effectDescriptions.sort(new Comparator<EffectDescription>() {
			@Override
			public int compare(EffectDescription ed1, EffectDescription ed2) {
				return ed1.name.compareTo(ed2.name);
			}

		});

		return effectDescriptions;
	}
	
	static class EffectDescription {
		public String name, description;
		
		private EffectDescription(Modifier mod) {
			this.name = mod.getName();
			this.description = mod.toString(false);
		}
		
		private EffectDescription(PersistentEffect effect) {
			this.name = effect.getName();
			this.description = effect.getDescription();
		}
	}
	
	public static class ActiveEffectsComponentStyle {
		private int borderWidth;
		private float width;
		private float textPaddingLeft, textPaddingRight;
		private ActiveEffectLabelStyle textStyle;
		private LabelStyle headingStyle;
		private ScrollPaneStyle scrollPaneStyle;
	}
	
}
