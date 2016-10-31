package mg.fishchicken.ui.storysequence;

import groovy.lang.Binding;
import mg.fishchicken.audio.AudioTrack;
import mg.fishchicken.core.GameState;
import mg.fishchicken.core.actions.Action;
import mg.fishchicken.core.assets.Assets;
import mg.fishchicken.core.conditions.Condition;
import mg.fishchicken.core.configuration.KeyBindings;
import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.gamelogic.characters.groups.PlayerCharacterGroup;
import mg.fishchicken.gamelogic.story.StoryPage;
import mg.fishchicken.gamelogic.story.StorySequence;
import mg.fishchicken.ui.BorderedWindow;
import mg.fishchicken.ui.ContainerStyle;
import mg.fishchicken.ui.UIManager;
import mg.fishchicken.ui.button.TextButtonWithSound;
import mg.fishchicken.ui.button.TextButtonWithSound.TextButtonWithSoundStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

public class StorySequencePanel extends BorderedWindow {

	private StorySequencePanelStyle style;
	private int currentPage;
	private Array<StoryPage> pages;
	private Image pageImage;
	private Label pageText;
	private Table buttonsTable;
	private TextButtonWithSound nextButton, closeButton, loadGameButton,
			exitGameButton;
	private boolean isClosable;
	private AudioTrack<?> musicToPlay;
	private Action endAction;

	public StorySequencePanel(GameState gameState, StorySequencePanelStyle style) {
		super(style);
		pages = new Array<StoryPage>();
		this.style = style;
		build(gameState);
	}

	public void setStorySequence(StorySequence storySequence) {
		this.isClosable = storySequence.canContinue();
		pages.clear();
		for (StoryPage page : storySequence.getPages()) {
			if (page.isApplicable()) {
				pages.add(page);
			}
		}
		currentPage = 0;
		if (pages.size > 0) {
			setPage(currentPage);
		}
		musicToPlay = storySequence.getMusic().random();
		endAction = storySequence.getAction();
	}

	private void build(final GameState gameState) {
		clearChildren();
		pageImage = new Image();
		add(pageImage).fill().width(style.imageWidth).height(style.imageHeight);
		pageText = new Label("", style.textStyle);
		pageText.setWrap(true);
		row();
		Container<Label> labelContainer = new Container<Label>(pageText);
		if (style.textContainerStyle != null) {
			style.textContainerStyle.apply(labelContainer);
		}
		labelContainer.fill();
		add(labelContainer).fill().pad(style.textMarginTop, style.textMarginLeft,
			style.textMarginBottom, style.textMarginRight);
		row();
		buttonsTable = new Table();
		add(buttonsTable).fill().center();

		closeButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "close"), style.closeButtonStyle);
		closeButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.hideStorySequence();
				if (endAction != null) {
					PlayerCharacterGroup pcg = GameState.getPlayerCharacterGroup();
					Binding params = new Binding();
					params.setVariable(Condition.PARAM_INITIAL_OBJECT, pcg);
					endAction.execute(pcg, params);
				}
			}
		});
		exitGameButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "exit"), style.exitGameButtonStyle);
		exitGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gameState.exitGameToMainMenu();
			}
		});
		loadGameButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "loadGame"), style.loadGameButtonStyle);
		loadGameButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				UIManager.displayLoadGamePanel();
			}
		});
		nextButton = new TextButtonWithSound(Strings.getString(
				UIManager.STRING_TABLE, "next"), style.nextButtonStyle);
		nextButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setPage(++currentPage);
			}
		});
		
		addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (KeyBindings.CONFIRM.is(keycode)) {
					if (nextButton.getParent() != null) {
						nextButton.toggle();
						return true;
					} else if (loadGameButton.getParent() != null) {
						loadGameButton.toggle();
						return true;
					} else if (closeButton.getParent() != null) {
						closeButton.toggle();
						return true;
					}
				} else if (KeyBindings.CANCEL.is(keycode)) {
					if (exitGameButton.getParent() != null) {
						exitGameButton.toggle();
						return true;
					}
				}
				return false;
			}
		});

	}

	private void setPage(int pageIndex) {
		if (pageIndex < 0 || pageIndex >= pages.size) {
			return;
		}
		StoryPage page = pages.get(pageIndex);
		if (page.getTitle() != null) {
			setTitle(page.getTitle());
		}
		pageImage.setDrawable(new TextureRegionDrawable(Assets
				.getTextureRegion(page.getImage())));
		pageText.setText(page.getText());
		buttonsTable.clearChildren();
		if (pageIndex >= pages.size-1) {
			if (isClosable) {
				buttonsTable.add().expandX();
				buttonsTable.add(closeButton)
						.minWidth(style.closeButtonWidth)
						.minHeight(style.closeButtonHeight);
			} else {
				buttonsTable.add().expandX();
				buttonsTable.add(loadGameButton).center()
						.minWidth(style.loadGameButtonWidth)
						.minHeight(style.loadGameButtonHeight)
						.padRight(style.loadGameButtonMarginRight);
				buttonsTable.add(exitGameButton).center()
						.minWidth(style.exitButtonWidth)
						.minHeight(style.exitButtonHeight);
				buttonsTable.add().expandX();
			}
		} else {
			buttonsTable.add().expandX();
			buttonsTable.add(nextButton).minWidth(style.nextButtonWidth)
					.minHeight(style.nextButtonHeight);
		}
		
		pack();
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (musicToPlay != null) {
			if (visible) {
				musicToPlay.play();
			} else {
				musicToPlay.stop();
			}
		}
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if (pages.size < 1) {
			UIManager.hideStorySequence();
		}
	}

	public static class StorySequencePanelStyle extends BorderedWindowStyle {
		private LabelStyle textStyle;
		private ContainerStyle textContainerStyle;
		private TextButtonWithSoundStyle nextButtonStyle, closeButtonStyle,
				loadGameButtonStyle, exitGameButtonStyle;
		private int imageWidth, imageHeight, textMarginLeft, textMarginRight,
				textMarginTop, textMarginBottom;
		private int nextButtonWidth, nextButtonHeight, closeButtonWidth,
				closeButtonHeight, loadGameButtonWidth, loadGameButtonHeight,
				exitButtonWidth, exitButtonHeight, loadGameButtonMarginRight;
	}
}
