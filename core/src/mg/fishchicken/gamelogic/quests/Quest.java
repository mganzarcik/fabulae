package mg.fishchicken.gamelogic.quests;

import java.io.IOException;
import java.util.Locale;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.VariableContainer;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.Configuration;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.statemachine.State;
import mg.fishchicken.core.statemachine.StateMachine;
import mg.fishchicken.core.statemachine.Transition;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.time.GameCalendarDate;
import mg.fishchicken.gamestate.Variables;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.XmlWriter;

public class Quest extends StateMachine<Quest.QuestState, Quest.QuestTransition> implements VariableContainer {

	public static final String STRING_TABLE = "quest."+Strings.RESOURCE_FILE_EXTENSION;
	private static ObjectMap<String, String> quests = new ObjectMap<String, String>();
	
	private static Array<Quest> activeQuests = new Array<Quest>();
	private static Array<Quest> finishedQuests = new Array<Quest>();

	public static Quest getQuest(String id) {
		return Assets.get(quests.get(id.toLowerCase(Locale.ENGLISH)));
	}
	
	/**
	 * Resets all quests to the initial state. They will all
	 * become not started with no story yet.
	 */
	public static void resetAllQuests() {
		activeQuests.clear();
		finishedQuests.clear();
		for (String questFile : quests.values()) {
			Assets.get(questFile, Quest.class).reset();
		}
	}

	public static Array<Quest> getActiveQuests() {
		return activeQuests;
	}
	
	public static Array<Quest> getFinishedQuests() {
		return finishedQuests;
	}
	/**
	 * Gathers all Quests and registers them in the AssetManager so that they can
	 * be later loaded by the asset loader.
	 * 
	 * @throws IOException
	 */
	public static void gatherQuests() throws IOException {
		Assets.gatherAssets(Configuration.getFolderQuests(), "xml", Quest.class, quests);
	}
	
	/**
	 * Writes all quests that have been started during the course of the game.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public static void writeAllStartedQuests(XmlWriter writer) throws IOException {
		for (String questFile : quests.values()) {
			Quest quest = Assets.get(questFile);
			if (quest.isStarted()) {
				writer.element(quest.getId());
				quest.writeToXML(writer);
				writer.pop();
			}
		}
	}
	
	public static final String XML_START_STATE = "startState";
	public static final String XML_END_STATE = "endState";
	public static final String XML_STATE = "state";
	public static final String XML_TIME = "time";
	public static final String XML_CURRENT_STATE = "currentState";
	public static final String XML_STORY = "story";
	
	private Array<QuestState> story;
	private Array<GameCalendarDate> storyTimes;
	private Variables variables;
	
	public Quest(FileHandle file) throws IOException {
		super(file);
	}
	
	public void start() {
		super.start();
		activeQuests.add(this);
		updateStory(getStartState());
		checkFinished();
	}
	
	private void reset() {
		currentState = null;
		story.clear();
		storyTimes.clear();
		variables.clear();
	}
	
	/**
	 * Returns the history of this quest (descriptions and times
	 * of all its states) in a human readable string form.
	 * 
	 * Returns an empty string if the quest is not started.
	 * 
	 * @return
	 */
	public String getStory() {
		StringBuilder builder = StringUtil.getFSB();
		int i = 0;
		for (State<QuestTransition> state : story) {
			String description = state.getDescription();
			if (description != null && !description.isEmpty()) {
				String separator = i == story.size-1 ? "" : "\n\n";
				builder.append(storyTimes.get(i).toStringNoTime());
				builder.append(": ");
				builder.append(description);
				builder.append(separator);
			}
			++i;
		}
		String returnValue = builder.toString();
		StringUtil.freeFSB(builder);
		return returnValue;
	}

	/**
	 * Returns true if this Quest is, or ever was, in the state with the
	 * supplied id.
	 * 
	 * @param stateId
	 * @return
	 */
	public boolean wasInState(String stateId) {
		return story.contains(getStateForId(stateId), false);
	}
	
	private void updateStory(QuestState state) {
		story.add(state);
		storyTimes.add(new GameCalendarDate(GameState.getCurrentGameDate()));
		if (state.getDescription() != null  && !state.getDescription().isEmpty()) {
			Log.logLocalized("journalUpdated", Log.LogType.JOURNAL);
		}
	}
	
	private void checkFinished() {
		if (isFinished()) {
			Log.log("Quest {0} completed.", Log.LogType.STATE_MACHINE, getId());
			activeQuests.removeValue(this, false);
			finishedQuests.add(this);
		}
	}
		
	protected boolean goToState(QuestTransition transition) {
		if (super.goToState(transition)) {
			if (!transition.isSilent()) {
				updateStory(getStateForId(transition.getToState()));
			}
			checkFinished();
			return true;
		}
		return false;
	}
	
	protected QuestState createState(Element element) {
		return new QuestState(this);
	}
	

	@Override
	public Variables variables() {
		return variables;
	}
	
	@Override
	public void writeToXML(XmlWriter writer) throws IOException {
		super.writeToXML(writer);
		
		variables.writeToXML(writer);
		
		writer.element(XML_STORY);
		int i = 0;
		for (QuestState state : story) {
			writer.element(XML_STATE);
			writer.attribute(XMLUtil.XML_ATTRIBUTE_ID, state.getId());
			writer.element(XML_TIME);
			storyTimes.get(i).writeToXML(writer);
			writer.pop();
			writer.pop();
			++i;
		}
		writer.pop();
	}
	
	@Override
	public void loadFromXML(Element root) throws IOException {
		super.loadFromXML(root);
		story = new Array<QuestState>();
		storyTimes = new Array<GameCalendarDate>();
		if (isFinished()) {
			finishedQuests.add(this);
		} else if (isStarted()) {
			activeQuests.add(this);
		}
		
		variables = new Variables();
		variables.loadFromXML(root);
		
		Element storyElement = root.getChildByName(XML_STORY);
		if (storyElement != null) {
			for (int i = 0; i < storyElement.getChildCount(); ++i) {
				Element storyStateElement = storyElement.getChild(i);
				story.add(getStateForId(storyStateElement.getAttribute(XMLUtil.XML_ATTRIBUTE_ID)));
				GameCalendarDate date = new GameCalendarDate(GameState.getCurrentGameDate().getCalendar());
				date.readFromXML(storyStateElement.getChildByName(XML_TIME));
				storyTimes.add(date);
			}
		}
	}
	
	public static class QuestState extends State<QuestTransition> {

		public QuestState(Quest parentMachine) {
			super(parentMachine);
		}

		protected QuestTransition createTransition(Element transitionElement) {
			QuestTransition newTransition = new QuestTransition(this);
			newTransition.loadFromXML(transitionElement);
			return newTransition;
		}
		
	}
	
	public static class QuestTransition extends Transition {

		private boolean s_silent;
		
		public QuestTransition(QuestState parentState) {
			super(parentState);
		}
		
		public boolean isSilent() {
			return s_silent;
		}
		
	}
}