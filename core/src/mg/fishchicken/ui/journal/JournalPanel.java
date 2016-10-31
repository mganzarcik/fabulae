package mg.fishchicken.ui.journal;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.quests.Quest;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.TableStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ForcedScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class JournalPanel extends BorderedWindow {

	private JournalPanelStyle style;
	private TextButtonWithSound activeQuestsButton;
	private TextButtonWithSound completedQuestsButton;
	private Table questList;
	private Label questDescription, questName;
	private boolean built;
	private ScrollPane descriptionScollPane;
	
	public JournalPanel(JournalPanelStyle style) {
		super(style);
		this.style = style;
		built = false;
	}
	
	public void refresh() {
		if (!built) {
			clearChildren();
			setTitle(Strings.getString(UIManager.STRING_TABLE, "journalHeading"));
		
			add(buildTabs()).fill().colspan(2);
			row();
			add(buildQuestList()).prefWidth(style.questListWidth).height(style.questListHeight);
			add(buildQuestDescription()).prefWidth(style.questDescriptionWidth).prefHeight(style.questListHeight);
			
			activeQuestsButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (activeQuestsButton.isChecked()) {
						reloadQuests(Quest.getActiveQuests());
					}
				}});
			
			completedQuestsButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (completedQuestsButton.isChecked()) {
						reloadQuests(Quest.getFinishedQuests());
					}
				}});
			built = true;
		}
		if (!activeQuestsButton.isChecked()) {
			activeQuestsButton.setChecked(true);
		} else {
			reloadQuests(Quest.getActiveQuests());
		}
	}
	
	private Actor buildQuestDescription() {
		Table descriptionTable = new Table();
		
		if (style.descriptionTableStyle != null) {
			style.descriptionTableStyle.apply(descriptionTable);
		}
		
		questName = new Label(Strings.getString(UIManager.STRING_TABLE,
				"questDescription"), style.subheadingStyle);
		questName.setWrap(true);
		questDescription = new Label("", style.textStyle);
		questDescription.setWrap(true);
		
		descriptionTable
				.add(questName).fill().expandX().padLeft(style.textPaddingLeft).padRight(style.textPaddingRight);
		
		
		descriptionTable.row();
		
		descriptionTable.add(questDescription).fill().expandX().padLeft(style.textPaddingLeft).padRight(style.textPaddingRight);
		descriptionTable.row();
		descriptionTable.add().expandY();
		
		descriptionScollPane = new ForcedScrollPane(descriptionTable,  style.descriptionPaneStyle);
		return descriptionScollPane;
	}
	
	private Actor buildQuestList() {
		questList = new Table();
		questList.top();
		return new ForcedScrollPane(questList, style.questListPaneStyle);
	}
	
	private Actor buildTabs() {
		Table tabs = new Table();
		activeQuestsButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "activeQuestsButton"), style.tabHeadingStyle);
		activeQuestsButton.padLeft(style.textPaddingLeft).padRight(style.textPaddingRight);
		completedQuestsButton = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "completedQuestsButton"), style.tabHeadingStyle);
		completedQuestsButton.padLeft(style.textPaddingLeft).padRight(style.textPaddingRight);
		ButtonGroup<TextButtonWithSound> group = new ButtonGroup<TextButtonWithSound>(activeQuestsButton, completedQuestsButton);
		group.setUncheckLast(true);
		group.setMaxCheckCount(1);
		tabs.add(activeQuestsButton);
		tabs.add().prefWidth(style.tabSpacing).fill();
		tabs.add(completedQuestsButton);
		tabs.add().expand();
		return tabs;
	}
	
	private void reloadQuests(Array<Quest> questsToLoad) {
		questList.clear();
		questName.setText("");
		questDescription.setText("");
		ButtonGroup<TextButtonWithSound> group = new ButtonGroup<TextButtonWithSound>();
		group.setUncheckLast(true);
		group.setMaxCheckCount(1);
		for (final Quest quest : questsToLoad) {
			final TextButtonWithSound questButton = new TextButtonWithSound(quest.getName(), style.questNameStyle);
			questButton.padLeft(style.textPaddingLeft).padRight(style.textPaddingRight);
			questButton.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (questButton.isChecked()) {
						questName.setText(quest.getName());
						questDescription.setText(quest.getStory());
						descriptionScollPane.layout();
						descriptionScollPane.setScrollY(descriptionScollPane.getMaxY());
					}
				}});
			group.add(questButton);
			questButton.getLabel().setWrap(true);
			questButton.getLabel().setAlignment(Align.left);
			questList.add(questButton).fill().prefWidth(style.questListWidth-style.textPaddingLeft-style.textPaddingRight).left();
			questList.row();
		}
		if (questsToLoad.size == 0) {
			TextButtonWithSound button = new TextButtonWithSound(Strings.getString(UIManager.STRING_TABLE, "noQuests"), style.questNameStyle);
			button.padLeft(style.textPaddingLeft).padRight(style.textPaddingRight);
			button.getLabel().setWrap(true);
			button.getLabel().setAlignment(Align.left);
			button.setDisabled(true);
			group.setMaxCheckCount(0);
			group.setMinCheckCount(0);
			group.add(button);
			questList.add(button).prefWidth(style.questListWidth-style.textPaddingLeft-style.textPaddingRight).fill().left();
			questList.row();
		}
	}
	
	public static class JournalPanelStyle extends BorderedWindowStyle{
		private int textPaddingLeft = 0, textPaddingRight = 0, tabSpacing = 10,
				questListWidth, questDescriptionWidth, questListHeight;
		private TextButtonWithSoundStyle tabHeadingStyle, questNameStyle;
		private LabelStyle textStyle, subheadingStyle;
		private ScrollPaneStyle descriptionPaneStyle, questListPaneStyle;
		private TableStyle descriptionTableStyle;
	}
}
