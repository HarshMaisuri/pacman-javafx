/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.shell;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX extends DefaultGameEventHandler {

	public final GameController gameController;
	public final Stage stage;
	public final Canvas canvas = new Canvas();
	public final FlashMessageView flashMessageView = new FlashMessageView();
	public final HUD hud = new HUD();

	private final StackPane mainSceneRoot;
	private final Scene mainScene;
	private final Group gameSceneRoot = new Group();
	private final Background bgImage = U.imageBackkground("/common/beach.jpg");
	private final Background bgBlack = U.colorBackground(Color.BLACK);
	private final Background bgBlue = U.colorBackground(Color.CORNFLOWERBLUE);

	public GameScene currentScene;

	public PacManGameUI_JavaFX(Stage stage, GameController gameController, double height, boolean fullscreen) {
		this.stage = stage;
		this.gameController = gameController;

		Env.$drawMode3D.addListener(observable -> selectBackground(currentScene));

		ScenesPacMan.createScenes(this);
		ScenesMsPacMan.createScenes(this);

		mainSceneRoot = new StackPane(gameSceneRoot, flashMessageView, hud);
		StackPane.setAlignment(hud, Pos.TOP_LEFT);

		var gameScene = selectScene(Env.$3D.get());
		// TODO rethink this
		double aspectRatio = gameScene.aspectRatio()
				.orElse(Screen.getPrimary().getBounds().getWidth() / Screen.getPrimary().getBounds().getHeight());
		double width = aspectRatio * height;
		mainScene = new Scene(mainSceneRoot, width, height);
		updateGameScene();

		stage.titleProperty().bind(Bindings.createStringBinding(() -> {
			String gameName = gameController.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man";
			return Env.$paused.get() ? String.format("%s (PAUSED, CTRL+P: resume, P: Step)", gameName)
					: String.format("%s", gameName);
		}, Env.gameLoop.$fps));
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> Env.gameLoop.stop());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		stage.addEventHandler(ScrollEvent.SCROLL, this::onScrolled);
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toString()));
		stage.setFullScreen(fullscreen);
		stage.centerOnScreen();
		stage.setScene(mainScene);
		stage.show();
	}

	public void update() {
		flashMessageView.update();
		hud.update(this);
	}

	public void showFlashMessage(double seconds, String message, Object... args) {
		flashMessageView.showMessage(String.format(message, args), seconds);
	}

	private void stopAllSounds() {
		ScenesMsPacMan.SOUNDS.stopAll();
		ScenesPacMan.SOUNDS.stopAll();
	}

	private void toggleUse3DScenes() {
		Env.$3D.set(!Env.$3D.get());
		if (selectScene(false) != selectScene(true)) {
			stopAllSounds();
			updateGameScene();
		}
	}

	private GameScene selectScene(boolean _3D) {
		final var game = gameController.game;
		final int _2D_or_3_D = _3D ? 1 : 0;

		int sceneIndex;
		switch (gameController.currentStateID) {
		case INTRO:
			sceneIndex = 0;
			break;
		case INTERMISSION:
			sceneIndex = game.intermissionNumber(game.levelNumber);
			break;
		case INTERMISSION_TEST:
			sceneIndex = gameController.intermissionTestNumber;
			break;
		default:
			sceneIndex = 4; // play scene
			break;
		}

		switch (gameController.gameVariant) {
		case MS_PACMAN:
			return ScenesMsPacMan.SCENES[sceneIndex][_2D_or_3_D];
		case PACMAN:
			return ScenesPacMan.SCENES[sceneIndex][_2D_or_3_D];
		default:
			throw new IllegalArgumentException("Unknown game variant: " + gameController.gameVariant);
		}
	}

	private void updateGameScene() {
		GameScene nextScene = selectScene(Env.$3D.get());
		if (currentScene != nextScene) {
			if (currentScene != null) {
				log("Change scene from '%s' to '%s'", currentScene.name(), nextScene.name());
				currentScene.end();
			} else {
				log("Set scene to '%s'", nextScene.name());
			}
			nextScene.createFXSubScene(mainScene);
			gameSceneRoot.getChildren().setAll(nextScene.getSubSceneFX());
			nextScene.init();
			nextScene.getSubSceneFX().requestFocus();
			selectBackground(nextScene);
			currentScene = nextScene;
		}
	}

	private void selectBackground(GameScene scene) {
		if (scene.is3D()) {
			mainSceneRoot.setBackground(Env.$drawMode3D.get() == DrawMode.LINE ? bgBlack : bgImage);
		} else {
			mainSceneRoot.setBackground(bgBlue);
		}
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		currentScene.onGameEvent(event);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateGameScene();
	}

	private void onKeyPressed(KeyEvent e) {
		final GameModel game = gameController.game;
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		switch (e.getCode()) {
		case A: {
			gameController.autoControlled = !gameController.autoControlled;
			String message = Env.message(gameController.autoControlled ? "autopilot_on" : "autopilot_off");
			showFlashMessage(1, message);
			break;
		}

		case E:
			if (gameController.gameRunning) {
				gameController.cheatEatAllPellets();
			}
			break;

		case I: {
			game.player.immune = !game.player.immune;
			String message = Env.message(game.player.immune ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(1, message);
			break;
		}

		case L:
			game.player.lives += 3;
			showFlashMessage(2, "You have %d lives", game.player.lives);
			break;

		case N:
			if (gameController.gameRunning) {
				showFlashMessage(1, Env.CHEAT_TALK.next());
				gameController.changeState(GameState.LEVEL_COMPLETE);
			}
			break;

		case P:
			if (Env.$paused.get()) {
				Env.gameLoop.runSingleStep(true);
			}
			break;

		case Q:
			currentScene.end();
			stopAllSounds();
			gameController.changeState(GameState.INTRO);
			break;

		case V:
			if (gameController.currentStateID == GameState.INTRO) {
				gameController.selectGameVariant(gameController.gameVariant.succ());
			}
			break;

		case X:
			if (gameController.gameRunning) {
				gameController.cheatKillGhosts();
			}
			break;

		case SPACE:
			gameController.requestGame();
			break;

		case F11:
			stage.setFullScreen(true);
			break;

		default:
			break;
		}
	}

	private void onControlKeyPressed(KeyEvent e) {
		switch (e.getCode()) {

		case C:
			if (currentScene.is3D()) {
				Env.nextPerspective();
				String perspective_key = Env.message(Env.$perspective.get().name().toLowerCase());
				String message = Env.message("camera_perspective", perspective_key);
				showFlashMessage(1, message);
			}
			break;

		case H:
			if (currentScene.is3D()) {
				Env.changeMazeWallHeight(!e.isShiftDown());
			}
			break;

		case I:
			if (!hud.isVisible()) {
				hud.show();
			} else {
				hud.hide();
			}
			break;

		case L:
			if (currentScene.is3D()) {
				Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			}
			break;

		case P:
			Env.$paused.set(!Env.$paused.get());
			if (Env.$paused.get()) {
				showFlashMessage(3, "Game paused");
			} else {
				showFlashMessage(2, "Game resumed");
			}
			break;

		case R:
			if (currentScene.is3D()) {
				Env.changeMazeResolution(!e.isShiftDown());
			}
			break;

		case S:
			int currentTargetFrameRate = Env.gameLoop.getTargetFrameRate();
			if (!e.isShiftDown()) {
				Env.gameLoop.setTargetFrameRate(currentTargetFrameRate + 10);
			} else {
				Env.gameLoop.setTargetFrameRate(Math.max(10, currentTargetFrameRate - 10));
			}
			showFlashMessage(1, "Target FPS set to %d Hz", Env.gameLoop.getTargetFrameRate());
			break;

		case T:
			Env.$isTimeMeasured.set(!Env.$isTimeMeasured.get());
			break;

		case X:
			if (currentScene.is3D()) {
				Env.$axesVisible.set(!Env.$axesVisible.get());
			}
			break;

		case Y:
			if (!currentScene.is3D()) {
				Env.$tilesVisible.set(!Env.$tilesVisible.get());
			}
			break;

		case DIGIT1:
			if (gameController.currentStateID == GameState.INTRO) {
				showFlashMessage(1, "Intermission Scene Test");
				gameController.startIntermissionTest();
			}
			break;

		case DIGIT3: {
			toggleUse3DScenes();
			String message = Env.$3D.get() ? "Using 3D play scene\nCTRL+C changes perspective" : "Using 2D play scene";
			showFlashMessage(2, message);
			break;
		}

		default:
			break;
		}
	}

	private void onScrolled(ScrollEvent e) {
		boolean shift = e.isShiftDown();
		boolean up = shift ? e.getDeltaX() > 0 : e.getDeltaY() > 0;
		if (currentScene instanceof PlayScene3D) {
			if (e.isShiftDown()) {
				Env.changeMazeWallHeight(up);
			} else {
				Env.changeMazeResolution(up);
			}
		}
	}
}