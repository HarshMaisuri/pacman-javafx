/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.util.FadingPane;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.border;

/**
 * @author Armin Reichert
 */
public class GamePage extends CanvasContainer implements Page {

	protected final GameSceneContext sceneContext;
	protected final FlashMessageView flashMessageLayer = new FlashMessageView();
	protected final Pane popupLayer = new Pane();
	protected final FadingPane helpInfoPopUp = new FadingPane();
	protected final ImageView helpIcon = new ImageView();
	protected TextFlow signature;
	protected Transition signatureAnimation;

	public GamePage(GameSceneContext sceneContext, double width, double height) {
		this.sceneContext = sceneContext;
		helpIcon.setCursor(Cursor.HAND);
		helpIcon.setOnMouseClicked(e -> showHelpInfoPopUp());
		createSignature();
		popupLayer.getChildren().addAll(helpIcon, signature, helpInfoPopUp);
		layers.getChildren().addAll(popupLayer, flashMessageLayer);
		layers.setOnKeyPressed(this::handleKeyPressed);
		PY_SHOW_DEBUG_INFO.addListener((py, ov, nv) -> showDebugBorders(nv));
		setSize(width, height);
	}

	private void createSignature() {
		var remake = new Text("Remake (2023) by ");
		remake.setFill(Color.WHEAT);

		var author = new Text("Armin Reichert");
		author.setFill(Color.WHEAT);

		signature = new TextFlow(remake, author);

		var fadeIn = new FadeTransition(Duration.seconds(5), signature);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);

		var fadeOut = new FadeTransition(Duration.seconds(1), signature);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		//fadeOut.setDelay(Duration.seconds(10)); // for testing

		signatureAnimation = new SequentialTransition(fadeIn, fadeOut);
	}

	protected void updateSignatureLayout() {
		Text remake = (Text) signature.getChildren().get(0);
		remake.setFont(Font.font("Helvetica", Math.floor(10 * scaling)));
		Text author = (Text) signature.getChildren().get(1);
		author.setFont(sceneContext.theme().font("font.handwriting", Math.floor(12 * scaling)));
		signature.setTranslateX((canvasContainer.getWidth() - signature.getWidth()) * 0.5);
		switch (sceneContext.gameVariant()) {
			case MS_PACMAN -> signature.setTranslateY(40 * scaling); // TODO fixme
			case PACMAN    -> signature.setTranslateY(28 * scaling); // TODO fixme
		}
		Logger.trace("Signature layout updated, scaling={}", scaling);
	}

	@Override
	public Pane rootPane() {
		return layers;
	}

	protected void updateHelpIconLayout() {
		double size = Math.ceil(12 * scaling);
		var icon = switch (sceneContext.gameVariant()) {
			case MS_PACMAN -> sceneContext.theme().image("mspacman.helpButton.icon");
			case PACMAN    -> sceneContext.theme().image("pacman.helpButton.icon");
		};
		helpIcon.setImage(icon);
		helpIcon.setFitHeight(size);
		helpIcon.setFitWidth(size);
		helpIcon.setTranslateX(unscaledCanvasWidth * scaling);
		helpIcon.setTranslateY(10 * scaling);
		helpIcon.setVisible(isHelpIconVisible());
		Logger.trace("Updated help icon, scaling: {}", scaling);
	}

	protected boolean isHelpIconVisible() {
		if (sceneContext.currentGameScene().isEmpty()) {
			return false;
		}
		var gameScene = sceneContext.currentGameScene().get();
		return gameScene != sceneContext.sceneConfig().get("boot");
	}

	protected void rescale(double newScaling, boolean always) {
		super.rescale(newScaling, always);
		resizeRegion(popupLayer, canvasContainer.getWidth(), canvasContainer.getHeight());
		updateHelpIconLayout();
		updateSignatureLayout();
		sceneContext.currentGameScene().ifPresent(gameScene -> {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.setScaling(scaling);
			}
		});
	}

	public void onGameSceneChanged(GameScene newGameScene) {
		var config = sceneContext.sceneConfig();

		//TODO: find a better solution than adding/removing key handler, maybe adapter class?
		if (sceneContext.gameController().getManualPacSteering() instanceof KeyboardSteering keyboardSteering) {
			// if play scene gets active/inactive, add/remove key handler
			if (newGameScene == config.get("play")) {
				layers.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
			} else {
				layers.removeEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
			}
		}
		if (newGameScene == config.get("intro")) {
			signatureAnimation.setDelay(Duration.seconds(3));
			signatureAnimation.play();
		} else {
			signatureAnimation.stop();
			signature.setOpacity(0);
		}
		if (newGameScene instanceof GameScene2D scene2D) {
			scene2D.setCanvas(canvas);
		}
		helpIcon.setVisible(isHelpIconVisible());
		showDebugBorders(PY_SHOW_DEBUG_INFO.get());

		rescale(scaling, true);
	}

	protected void showDebugBorders(boolean on)  {
		if (on) {
			int w = 3;
			layers.setBorder(border(Color.RED, w));
			canvasLayer.setBorder(border(Color.YELLOW, w));
			popupLayer.setBorder(border(Color.GREENYELLOW, w));
		} else {
			layers.setBorder(null);
			canvasLayer.setBorder(null);
			popupLayer.setBorder(null);
		}
	}

	public FlashMessageView flashMessageView() {
		return flashMessageLayer;
	}

	public void render() {
		sceneContext.currentGameScene().ifPresent(gameScene -> {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.draw();
			}
		});
		flashMessageLayer.update();
		popupLayer.setVisible(true);
	}

	protected void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.handleKeyEvent(keyEvent);
		handleKeyboardInput();
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		var actionHandler = sceneContext.actionHandler();
		var gameState = sceneContext.gameState();
		if (Keyboard.pressed(KEY_AUTOPILOT)) {
			actionHandler.toggleAutopilot();
		} else if (Keyboard.pressed(KEY_BOOT)) {
			if (gameState != GameState.BOOT) {
				actionHandler.reboot();
			}
		} else if (Keyboard.pressed(KEY_DEBUG_INFO)) {
			Ufx.toggle(PY_SHOW_DEBUG_INFO);
		} else if (Keyboard.pressed(KEY_FULLSCREEN)) {
			actionHandler.setFullScreen(true);
		} else if (Keyboard.pressed(KEY_IMMUNITY)) {
			actionHandler.toggleImmunity();
		} else if (Keyboard.pressed(KEY_SHOW_HELP)) {
			showHelpInfoPopUp();
		} else if (Keyboard.pressed(KEY_PAUSE)) {
			actionHandler.togglePaused();
		} else if (Keyboard.pressed(KEYS_SINGLE_STEP)) {
			actionHandler.doSimulationSteps(1);
		} else if (Keyboard.pressed(KEY_TEN_STEPS)) {
			actionHandler.doSimulationSteps(10);
		} else if (Keyboard.pressed(KEY_SIMULATION_FASTER)) {
			actionHandler.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(KEY_SIMULATION_SLOWER)) {
			actionHandler.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(KEY_SIMULATION_NORMAL)) {
			actionHandler.resetSimulationSpeed();
		} else if (Keyboard.pressed(KEY_QUIT)) {
			if (gameState != GameState.BOOT && gameState != GameState.INTRO) {
				actionHandler.restartIntro();
			}
		} else if (Keyboard.pressed(KEY_TEST_LEVELS)) {
			actionHandler.startLevelTestMode();
		} else {
			sceneContext.currentGameScene().ifPresent(GameScene::handleKeyboardInput);
		}
	}

	// Help Info stuff

	public class HelpInfo extends PageInfo {

		public void addLocalizedEntry(String lhsKey, String keyboardKey) {
			addRow(
				label(sceneContext.tt(lhsKey), Color.gray(0.9)),
				text("[" + keyboardKey + "]", Color.YELLOW)
			);
		}

		@Override
		public Pane createPane(Color backgroundColor, Font font) {
			var pane = super.createPane(backgroundColor, font);
			var grid = (GridPane) pane.getChildren().get(0); // TODO improve
			// add default entries:
			int nextFreeIndex = grid.getRowCount();
			if (sceneContext.gameController().isAutoControlled()) {
				var autoPilotEntry = text(sceneContext.tt("help.autopilot_on"), Color.ORANGE);
				autoPilotEntry.setFont(font);
				GridPane.setColumnSpan(autoPilotEntry, 2);
				grid.add(autoPilotEntry, 0, nextFreeIndex);
				nextFreeIndex += 1;
			}
			if (sceneContext.gameController().isImmune()) {
				var immunityEntry = text(sceneContext.tt("help.immunity_on"), Color.ORANGE);
				immunityEntry.setFont(font);
				GridPane.setColumnSpan(immunityEntry, 2);
				grid.add(immunityEntry, 0, nextFreeIndex);
				nextFreeIndex += 1;
			}
			return pane;
		}
	}

	private HelpInfo currentHelpInfo() {
		HelpInfo helpInfo = new HelpInfo();
		switch (sceneContext.gameState()) {
			case INTRO -> addInfoForIntroScene(helpInfo);
			case CREDIT -> addInfoForCreditScene(helpInfo);
			case READY, HUNTING, PACMAN_DYING, GHOST_DYING -> {
				if (sceneContext.gameLevel().isPresent()) {
					if (sceneContext.gameLevel().get().isDemoLevel()) {
						addInfoForDemoLevel(helpInfo);
					} else {
						addInfoForPlayScene(helpInfo);
					}
				}
			}
			default -> addInfoForQuittingScene(helpInfo);
		}
		return helpInfo;
	}

	private void showHelpInfoPopUp() {
		var bgColor = sceneContext.gameVariant() == GameVariant.MS_PACMAN
			? Color.rgb(255, 0, 0, 0.8)
			: Color.rgb(33, 33, 255, 0.8);
		var font = sceneContext.theme().font("font.monospaced", Math.max(6, 14 * scaling));
		var pane = currentHelpInfo().createPane(bgColor, font);
		helpInfoPopUp.setTranslateX(10 * scaling);
		helpInfoPopUp.setTranslateY(30 * scaling);
		helpInfoPopUp.setContent(pane);
		helpInfoPopUp.show(Duration.seconds(1.5));
	}

	private void addInfoForIntroScene(HelpInfo info) {
		if (sceneContext.gameController().hasCredit()) {
			info.addLocalizedEntry("help.start_game", "1");
		}
		info.addLocalizedEntry("help.add_credit", "5");
		info.addLocalizedEntry(sceneContext.gameVariant() == GameVariant.MS_PACMAN ? "help.pacman" : "help.ms_pacman", "V");
	}

	private void addInfoForQuittingScene(HelpInfo info) {
		info.addLocalizedEntry("help.show_intro", "Q");
	}

	private void addInfoForCreditScene(HelpInfo info) {
		if (sceneContext.gameController().hasCredit()) {
			info.addLocalizedEntry("help.start_game", "1");
		}
		info.addLocalizedEntry("help.add_credit", "5");
		info.addLocalizedEntry("help.show_intro", "Q");
	}

	private void addInfoForPlayScene(HelpInfo info) {
		info.addLocalizedEntry("help.move_left",  sceneContext.tt("help.cursor_left"));
		info.addLocalizedEntry("help.move_right", sceneContext.tt("help.cursor_right"));
		info.addLocalizedEntry("help.move_up",    sceneContext.tt("help.cursor_up"));
		info.addLocalizedEntry("help.move_down",  sceneContext.tt("help.cursor_down"));
		info.addLocalizedEntry("help.show_intro", "Q");
	}

	private void addInfoForDemoLevel(HelpInfo info) {
		info.addLocalizedEntry("help.add_credit", "5");
		info.addLocalizedEntry("help.show_intro", "Q");
	}
}