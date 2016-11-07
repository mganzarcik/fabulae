package mg.fishchicken.ui.dialogue;

import mg.fishchicken.core.GameState;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.logging.Log;
import mg.fishchicken.core.logging.Log.LogType;
import mg.fishchicken.core.util.StringUtil;
import mg.fishchicken.gamelogic.characters.GameCharacter;
import mg.fishchicken.gamelogic.dialogue.Banter;
import mg.fishchicken.gamelogic.dialogue.Dialogue;
import mg.fishchicken.gamelogic.dialogue.DialogueCallback;
import mg.fishchicken.gamelogic.dialogue.Greeting;
import mg.fishchicken.gamelogic.dialogue.NPCTalk;
import mg.fishchicken.gamelogic.dialogue.PCTalk;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.ContainerStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

public class DialoguePanel extends BorderedWindow implements EventListener {
	
	private Label npcTalkPanel;
	private Label bantersPanel;
	private Container<Label> banterContainer;
	private Cell<Container<Label>> banterCell;
	private Array<TextButtonWithSound> pcTalk;
	private Table npcTalkTable, pcTalkTable;
	private Dialogue dialogue;
	private ObjectMap<String, String> dialogueParameters;
	private DialoguePanelStyle style;
	private DialogueCallback callback;
	private Image image;
	
	public DialoguePanel(DialoguePanelStyle style) {
		super(style);
		this.style = style;
		pcTalkTable = new Table();
		addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				int numberPressed = KeyBindings.getNumberPressed(keycode);
				if (numberPressed < 1 || numberPressed > pcTalkTable.getCells().size) {
					return false;
				}
				Actor actor = pcTalkTable.getCells().get(numberPressed-1).getActor();
				pcTalkSelected(actor.getName());
				return true;
			}
		});
	}
	
	public void setDialogue(Dialogue dialogue, GameCharacter talkingPC, GameCharacter talkingNPC, DialogueCallback callback, ObjectMap<String, String> dialogueParameters) {
		dialogue.setTalkers(talkingPC, talkingNPC);
		setDialogue(dialogue, callback, dialogueParameters);
	}
	
	private void setDialogue(Dialogue dialogue, DialogueCallback callback, ObjectMap<String, String> dialogueParameters) { 
		this.dialogue = dialogue;
		this.callback = callback;
		this.dialogueParameters = dialogueParameters;
		
		this.clearChildren();
		pcTalkTable.clearChildren();	
		setTitle(dialogue.getTitle());
		
		Greeting startingGreeting = dialogue.getStartingGreeting();
		
		if (startingGreeting == null) {
			Log.log("Could not determine any greeting for dialogue between characters {0} and {1}.", LogType.ERROR,
					dialogue.getPCAtDialogue().getInternalId(), dialogue.getNPCAtDialogue().getInternalId());
			startingGreeting = new Greeting(dialogue);
			startingGreeting.setText("Mon dieu! Ich bin completely confucio! Lorem ipsum, error error.");
			PCTalk bye = new PCTalk(dialogue);
			bye.setId("bye");
			bye.setConversationEnd(true);
			bye.setText("Eh?");
			startingGreeting.addPCTalk(bye);
		}
		
		npcTalkPanel = new Label(
				startingGreeting.getText(dialogueParameters), style.npcTalkStyle);
		npcTalkPanel.setWrap(true);
		
		bantersPanel = new Label("", style.banterStyle);
		bantersPanel.setWrap(true);
		banterContainer = new Container<Label>();
		banterContainer.setActor(bantersPanel);
		banterContainer.fill().top();
		if (style.banterContainerStyle != null) {
			style.banterContainerStyle.apply(banterContainer);
		}
		
		
		pcTalk = new Array<TextButtonWithSound>();
		
		addPCTalks(startingGreeting.getPCTalks());
		
		npcTalkTable = new Table();
		
		if (dialogue.getNPCAtDialogue() != null && dialogue.getNPCAtDialogue().getPortrait() != null) {
			Image potrait = new Image(dialogue.getNPCAtDialogue().getPortrait());
			npcTalkTable.add(potrait).width(style.portraitWidth).height(style.portraitHeigh).top();
		}
		
		Container<Label> npcTalkContainer = new Container<Label>();
		npcTalkContainer.setActor(npcTalkPanel);
		npcTalkContainer
				.fill()
				.top();
		if (style.npcTalkContainerStyle != null) {
			style.npcTalkContainerStyle.apply(npcTalkContainer);
		}
		
		npcTalkTable.add(npcTalkContainer).fill().top().expandX();
		
		
		String imagePath = startingGreeting.getImage();
		image = imagePath != null ? new Image(Assets.getTextureRegion(imagePath)) : new Image();
		this.add(image).fill().width(style.width).height(imagePath != null ? style.imageHeight : 0);
		this.row();
		this.add(npcTalkTable)
				.prefWidth(style.width)
				.minWidth(style.width)
				.pad(imagePath != null ? style.textMarginTop : 0, style.textMarginLeft,
						style.textMarginBottom, style.textMarginRight);
		this.row();
		banterCell = this.add(banterContainer).prefWidth(style.width).minWidth(style.width);
		this.row();
		this.add(pcTalkTable).fillX();
		
		loadBanters(startingGreeting);
		pack();
	}
	
	private void loadBanters(NPCTalk npcTalk) {
		Array<Banter> banters = npcTalk.getRelevantBanters();
		
		StringBuilder bantersText = StringUtil.getFSB();
		int i = 0;
		for (Banter banter : banters) {
			++i;
			bantersText.append(((GameCharacter)GameState.getGameObjectByInternalId(banter.getCharacterId())).getName());
			bantersText.append(": ");
			bantersText.append(banter.getText(dialogueParameters));
			if (i != banters.size) {
				bantersText.append("\n\n");
			}
		}
		
		if (banters.size > 0) {
			bantersPanel.setText(bantersText.toString());
			banterCell.setActor(banterContainer);
		} else {
			banterCell.setActor(null);
		}
		StringUtil.freeFSB(bantersText);
	}
	
	@Override
	public boolean handle(Event event) {
		if (!(event instanceof ChangeEvent)) {
			return false;
		}
		changed((ChangeEvent)event, event.getTarget());
		return false;
	}
	
	public void changed (ChangeEvent event, Actor actor) {
		pcTalkSelected(actor.getName());
	}
	
	private void pcTalkSelected(String pcTalkId) {
		PCTalk pcTalkSelected = dialogue.getPCTalk(pcTalkId);
		
		if (pcTalkSelected == null) {
			Log.log("Could not find any pcTalk with id {0}.", LogType.ERROR, pcTalkId);
			pcTalkSelected = new PCTalk(dialogue);
			pcTalkSelected.setId("bye");
			pcTalkSelected.setConversationEnd(true);
			pcTalkSelected.setText("Eh?");
		}
		
		if (pcTalkSelected.isConversationEnd()) {
			UIManager.hideDialogue();
		}
		
		pcTalkSelected.executeAction();
		
		if (pcTalkSelected.isConversationEnd()) {
			if (callback != null) {
				callback.onDialogueEnd(pcTalkSelected);
			}
			return;
		}
		
		NPCTalk nextNPTTalk = pcTalkSelected.executeNextValidNPCTalk();
		
		if (nextNPTTalk == null || !dialogue.getPCAtDialogue().isActive()) {
			UIManager.hideDialogue();
			return;
		}
		
		npcTalkPanel.setText(nextNPTTalk.getText(dialogueParameters));
		String imagePath = nextNPTTalk.getImage();
		image.setDrawable(imagePath != null ? new TextureRegionDrawable(Assets.getTextureRegion(imagePath)) : null);
		getCell(image).height(imagePath != null ? style.imageHeight : 0);
		getCell(npcTalkTable).padTop(imagePath != null ? style.textMarginTop : 0);
		
		pcTalkTable.clear();
		addPCTalks(nextNPTTalk.getPCTalks());
		pack();
	}
	
	private void addPCTalks(Array<PCTalk> pcTalks) {
		for (int i = 0; i < pcTalks.size; ++i) {
			PCTalk talk = pcTalks.get(i);
			TextButtonWithSound button = null;
			if (i < pcTalk.size) {
				button = pcTalk.get(i);
				button.setText(talk.getText(dialogueParameters));
			} else {
				button = new TextButtonWithSound(talk.getText(dialogueParameters), style.pcTalkStyle);
				button.addListener(this);
				button.getLabel().setWrap(true);
				button.getLabel().setAlignment(Align.left);
				pcTalk.add(button);
			}
			button.setName(talk.getId());
			Label buttonLabel = button.getLabel();
			button.clearChildren();
			button.add(
					new Label(Integer.toString(i + 1) + ".", buttonLabel
							.getStyle()))
					.top()
					.pad(style.pcTalkContainerStyle.padTop, style.pcTalkContainerStyle.padLeft,
							style.pcTalkContainerStyle.padBottom, style.itemNumberMarginRight);
			button.add(buttonLabel)
					.top()
					.fill()
					.expand()
					.pad(style.pcTalkContainerStyle.padTop, 0, style.pcTalkContainerStyle.padBottom,
							style.pcTalkContainerStyle.padRight);
			pcTalkTable.add(button).expandX().fill().prefWidth(style.width)
					.minWidth(style.width).padTop(style.buttonSpacing);
			pcTalkTable.row();
		}
	}
		
	public static class DialoguePanelStyle extends BorderedWindowStyle {
		private int buttonSpacing, width, portraitWidth, portraitHeigh;
		private int textMarginTop, textMarginBottom, textMarginLeft, textMarginRight, itemNumberMarginRight;
		private int imageHeight;
		private ContainerStyle npcTalkContainerStyle, pcTalkContainerStyle, banterContainerStyle;
		private TextButtonWithSoundStyle pcTalkStyle;
		private LabelStyle npcTalkStyle, banterStyle;
	}

}
