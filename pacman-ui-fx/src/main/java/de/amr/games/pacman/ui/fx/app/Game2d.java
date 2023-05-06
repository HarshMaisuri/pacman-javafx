/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.RND;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.rendering2d.MazeColoring;
import de.amr.games.pacman.ui.fx.rendering2d.Spritesheet;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * This is the entry point of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application is structured according to the MVC (model-view-controller) design pattern. The model layer consists
 * of the two game models <code> PacManGame</code> and <code> MsPacManGame</code>. The controller is a finite-state
 * machine which is triggered 60 times per second by the game loop. The user interface listens to game events sent from
 * the controller/model layer. The model and controller layers are decoupled from the user interface. This allow to
 * attach different user interfaces without having to change the controller or model.
 * 
 * <p>
 * As a proof of concept I implemented also a (simpler) Swing user interface, see repository
 * <a href="https://github.com/armin-reichert/pacman-ui-swing">Pac-Man Swing UI</a>.
 * 
 * @author Armin Reichert
 */
public class Game2d extends Application {

	public static class Resources {

		public final ResourceMgr loader = new ResourceMgr("/de/amr/games/pacman/ui/fx/", Game2d.class);

		public static class PacManGameGraphics {
			public Image icon;
			public Spritesheet spritesheet;
			public Image fullMaze;
			public Image emptyMaze;
			public Image flashingMaze;
		}

		public static class MsPacManGameGraphics {
			public Image icon;
			public Spritesheet spritesheet;
			public Image logo;
			public Image[] emptyFlashingMaze;

			private Image emptyMaze(int i) {
				return spritesheet.subImage(228, 248 * i, 226, 248);
			}

			private static Image flashing(Image maze, MazeColoring colors) {
				return Ufx.colorsExchanged(maze, Map.of(//
						colors.wallBaseColor(), Color.WHITE, //
						colors.wallTopColor(), Color.BLACK));
			}
		}

		public Font arcadeFont;
		public Font handwritingFont;

		private ResourceBundle messageBundle;
		private Picker<String> messagePickerCheating;
		private Picker<String> messagePickerLevelComplete;
		private Picker<String> messagePickerGameOver;

		public final AudioClip voiceExplainKeys = loader.audioClip("sound/voice/press-key.mp3");
		public final AudioClip voiceAutopilotOff = loader.audioClip("sound/voice/autopilot-off.mp3");
		public final AudioClip voiceAutopilotOn = loader.audioClip("sound/voice/autopilot-on.mp3");
		public final AudioClip voiceImmunityOff = loader.audioClip("sound/voice/immunity-off.mp3");
		public final AudioClip voiceImmunityOn = loader.audioClip("sound/voice/immunity-on.mp3");

		public PacManGameGraphics graphicsPacMan;
		public GameSounds gameSoundsMsPacMan;

		public MsPacManGameGraphics graphicsMsPacMan;
		public GameSounds gameSoundsPacMan;

		public void init() {
			// Fonts
			arcadeFont = loader.font("fonts/emulogic.ttf", 8);
			handwritingFont = loader.font("fonts/RockSalt-Regular.ttf", 8);

			// Graphics
			graphicsPacMan = new PacManGameGraphics();
			graphicsPacMan.icon = loader.image("graphics/icons/pacman.png");
			graphicsPacMan.spritesheet = new Spritesheet(loader.image("graphics/pacman/sprites.png"), 16);
			graphicsPacMan.fullMaze = loader.image("graphics/pacman/maze_full.png");
			graphicsPacMan.emptyMaze = loader.image("graphics/pacman/maze_empty.png");
			graphicsPacMan.flashingMaze = loader.image("graphics/pacman/maze_empty_flashing.png");

			graphicsMsPacMan = new MsPacManGameGraphics();
			graphicsMsPacMan.icon = loader.image("graphics/icons/mspacman.png");
			graphicsMsPacMan.spritesheet = new Spritesheet(loader.image("graphics/mspacman/sprites.png"), 16);

			graphicsMsPacMan.emptyFlashingMaze = IntStream.range(0, 6)
					.mapToObj(
							i -> MsPacManGameGraphics.flashing(graphicsMsPacMan.emptyMaze(i), ArcadeTheme.MS_PACMAN_MAZE_COLORS[i]))
					.toArray(Image[]::new);

			graphicsMsPacMan.logo = loader.image("graphics/mspacman/midway.png");

			// Texts
			messageBundle = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.texts.messages");
			messagePickerCheating = loader.createPicker(messageBundle, "cheating");
			messagePickerLevelComplete = loader.createPicker(messageBundle, "level.complete");
			messagePickerGameOver = loader.createPicker(messageBundle, "game.over");

			// Sound
			//@formatter:off
			Object[][] audioClipsMsPacman = { 
				{ AudioClipID.BONUS_EATEN,     "sound/mspacman/Fruit.mp3", 1.0 }, 
				{ AudioClipID.CREDIT,          "sound/mspacman/Credit.mp3", 1.0 }, 
				{ AudioClipID.EXTRA_LIFE,      "sound/mspacman/ExtraLife.mp3", 1.0 }, 
				{ AudioClipID.GAME_READY,      "sound/mspacman/Start.mp3", 1.0 }, 
				{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
				{ AudioClipID.GHOST_EATEN,     "sound/mspacman/Ghost.mp3", 1.0 }, 
				{ AudioClipID.GHOST_RETURNING, "sound/mspacman/GhostEyes.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_1,  "sound/mspacman/Act1TheyMeet.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_2,  "sound/mspacman/Act2TheChase.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_3,  "sound/mspacman/Act3Junior.mp3", 1.0 }, 
				{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_DEATH,    "sound/mspacman/Died.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_MUNCH,    "sound/mspacman/Pill.wav", 1.0 }, 
				{ AudioClipID.PACMAN_POWER,    "sound/mspacman/ScaredGhost.mp3", 1.0 }, 
				{ AudioClipID.SIREN_1,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SIREN_2,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
				{ AudioClipID.SIREN_3,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
				{ AudioClipID.SIREN_4,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
				{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
			};

			Object[][] audioClipsPacman = { 
				{ AudioClipID.BONUS_EATEN,     "sound/pacman/eat_fruit.mp3", 1.0 }, 
				{ AudioClipID.CREDIT,          "sound/pacman/credit.wav", 1.0 }, 
				{ AudioClipID.EXTRA_LIFE,      "sound/pacman/extend.mp3", 1.0 }, 
				{ AudioClipID.GAME_READY,      "sound/pacman/game_start.mp3", 1.0 }, 
				{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
				{ AudioClipID.GHOST_EATEN,     "sound/pacman/eat_ghost.mp3", 1.0 }, 
				{ AudioClipID.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_1,  "sound/pacman/intermission.mp3", 1.0 }, 
				{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_DEATH,    "sound/pacman/pacman_death.wav", 1.0 }, 
				{ AudioClipID.PACMAN_MUNCH,    "sound/pacman/doublemunch.wav", 1.0 }, 
				{ AudioClipID.PACMAN_POWER,    "sound/pacman/ghost-turn-to-blue.mp3", 1.0 }, 
				{ AudioClipID.SIREN_1,         "sound/pacman/siren_1.mp3", 1.0 }, 
				{ AudioClipID.SIREN_2,         "sound/pacman/siren_2.mp3", 1.0 }, 
				{ AudioClipID.SIREN_3,         "sound/pacman/siren_3.mp3", 1.0 }, 
				{ AudioClipID.SIREN_4,         "sound/pacman/siren_4.mp3", 1.0 }, 
				{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
			};
			//@formatter:on

			gameSoundsMsPacMan = new GameSounds(audioClipsMsPacman, true);
			gameSoundsPacMan = new GameSounds(audioClipsPacman, true);
		}

		public Font font(Font font, double size) {
			return Font.font(font.getFamily(), size);
		}

		/**
		 * Builds a resource key from the given key pattern and arguments and reads the corresponding message from the
		 * messages resource bundle.
		 * 
		 * @param keyPattern message key pattern
		 * @param args       arguments merged into key pattern
		 * @return message text for composed key or string indicating missing text
		 */
		public String message(String keyPattern, Object... args) {
			try {
				var pattern = messageBundle.getString(keyPattern);
				return MessageFormat.format(pattern, args);
			} catch (Exception x) {
				Logger.error("No text resource found for key '{}'", keyPattern);
				return "missing{%s}".formatted(keyPattern);
			}
		}

		public String pickCheatingMessage() {
			return messagePickerCheating.next();
		}

		public String pickGameOverMessage() {
			return messagePickerGameOver.next();
		}

		public String pickLevelCompleteMessage(int levelNumber) {
			return "%s%n%n%s".formatted(messagePickerLevelComplete.next(), message("level_complete", levelNumber));
		}

		public String randomReadyText(GameVariant variant) {
			return "READY!";
		}

		public GameSounds gameSounds(GameVariant variant) {
			return switch (variant) {
			case MS_PACMAN -> gameSoundsMsPacMan;
			case PACMAN -> gameSoundsPacMan;
			default -> throw new IllegalGameVariantException(variant);
			};
		}
	}

	public static class Properties {
	//@formatter:off
		public static final ObjectProperty<Color> mainSceneBgColorPy       = new SimpleObjectProperty<>(Color.web("0x334bd3"));
		public static final BooleanProperty       showDebugInfoPy          = new SimpleBooleanProperty(false);
		public static final IntegerProperty       simulationStepsPy        = new SimpleIntegerProperty(1);
	//@formatter:on
	}

	public static class Actions {

		private ActionContext context;

		public void init(ActionContext context) {
			this.context = context;
		}

		public void stopVoiceMessage() {
			context.ui().stopVoiceMessage();
		}

		public void showFlashMessage(String message, Object... args) {
			showFlashMessageSeconds(1, message, args);
		}

		public void showFlashMessageSeconds(double seconds, String message, Object... args) {
			context.ui().flashMessageView().showMessage(String.format(message, args), seconds);
		}

		public void startGame() {
			if (context.game().hasCredit()) {
				context.ui().stopVoiceMessage();
				context.gameController().startPlaying();
			}
		}

		public void startCutscenesTest() {
			context.gameController().startCutscenesTest();
			showFlashMessage("Cut scenes");
		}

		public void restartIntro() {
			context.ui().currentGameScene().end();
			GameEvents.setSoundEventsEnabled(true);
			if (context.game().isPlaying()) {
				context.game().changeCredit(-1);
			}
			context.gameController().restart(INTRO);
		}

		public void reboot() {
			if (context.ui().currentGameScene() != null) {
				context.ui().currentGameScene().end();
			}
			context.ui().playHelpVoiceMessageAfterSeconds(4);
			context.gameController().restart(GameState.BOOT);
		}

		public void addCredit() {
			GameEvents.setSoundEventsEnabled(true);
			context.gameController().addCredit();
		}

		public void enterLevel(int newLevelNumber) {
			if (context.gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
				return;
			}
			context.game().level().ifPresent(level -> {
				if (newLevelNumber > level.number()) {
					for (int n = level.number(); n < newLevelNumber - 1; ++n) {
						context.game().nextLevel();
					}
					context.gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
				} else if (newLevelNumber < level.number()) {
					// not implemented
				}
			});
		}

		public void togglePaused() {
			Ufx.toggle(context.ui().pausedPy);
			// TODO mute and unmute?
			if (context.ui().pausedPy.get()) {
				resources.gameSounds(context.game().variant()).stopAll();
			}
		}

		public void oneSimulationStep() {
			if (context.ui().pausedPy.get()) {
				context.ui().executeSingleStep(true);
			}
		}

		public void tenSimulationSteps() {
			if (context.ui().pausedPy.get()) {
				context.ui().executeSteps(10, true);
			}
		}

		public void changeSimulationSpeed(int delta) {
			int newFramerate = context.ui().targetFrameratePy.get() + delta;
			if (newFramerate > 0 && newFramerate < 120) {
				context.ui().targetFrameratePy.set(newFramerate);
				showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
			}
		}

		public void resetSimulationSpeed() {
			context.ui().targetFrameratePy.set(GameModel.FPS);
			showFlashMessageSeconds(0.75, "%dHz".formatted(context.ui().targetFrameratePy.get()));
		}

		public void selectNextGameVariant() {
			var gameVariant = context.game().variant().next();
			context.gameController().selectGameVariant(gameVariant);
			context.ui().playHelpVoiceMessageAfterSeconds(4);
		}

		public void toggleAutopilot() {
			context.gameController().toggleAutoControlled();
			var auto = context.gameController().isAutoControlled();
			String message = resources.message(auto ? "autopilot_on" : "autopilot_off");
			showFlashMessage(message);
			context.ui().playVoiceMessage(auto ? resources.voiceAutopilotOn : resources.voiceAutopilotOff);
		}

		public void toggleImmunity() {
			context.game().setImmune(!context.game().isImmune());
			var immune = context.game().isImmune();
			String message = resources.message(immune ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(message);
			context.ui().playVoiceMessage(immune ? resources.voiceImmunityOn : resources.voiceImmunityOff);
		}

		public void startLevelTestMode() {
			if (context.gameState() == GameState.INTRO) {
				context.gameController().restart(GameState.LEVEL_TEST);
				showFlashMessage("Level TEST MODE");
			}
		}

		public void cheatAddLives(int numLives) {
			context.game().setLives(numLives + context.game().lives());
			showFlashMessage(resources.message("cheat_add_lives", context.game().lives()));
		}

		public void cheatEatAllPellets() {
			context.gameController().cheatEatAllPellets();
			if (RND.nextDouble() < 0.1) {
				showFlashMessage(resources.pickCheatingMessage());
			}
		}

		public void cheatEnterNextLevel() {
			context.gameController().cheatEnterNextLevel();
		}

		public void cheatKillAllEatableGhosts() {
			context.gameController().cheatKillAllEatableGhosts();
			if (RND.nextDouble() < 0.1) {
				showFlashMessage(resources.pickCheatingMessage());
			}
		}
	}

	public static class Keys {

		protected static KeyCodeCombination just(KeyCode code) {
			return new KeyCodeCombination(code);
		}

		protected static KeyCodeCombination alt(KeyCode code) {
			return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
		}

		protected static KeyCodeCombination shift(KeyCode code) {
			return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
		}

		public static final KeyCodeCombination CHEAT_EAT_ALL = alt(KeyCode.E);
		public static final KeyCodeCombination CHEAT_ADD_LIVES = alt(KeyCode.L);
		public static final KeyCodeCombination CHEAT_NEXT_LEVEL = alt(KeyCode.N);
		public static final KeyCodeCombination CHEAT_KILL_GHOSTS = alt(KeyCode.X);

		public static final KeyCodeCombination AUTOPILOT = alt(KeyCode.A);
		public static final KeyCodeCombination DEBUG_INFO = alt(KeyCode.D);
		public static final KeyCodeCombination IMMUNITIY = alt(KeyCode.I);
		public static final KeyCodeCombination MUTE = alt(KeyCode.M);

		public static final KeyCodeCombination PAUSE = just(KeyCode.P);
		public static final KeyCodeCombination PAUSE_STEP = shift(KeyCode.P);
		public static final KeyCodeCombination SINGLE_STEP = just(KeyCode.SPACE);
		public static final KeyCodeCombination TEN_STEPS = shift(KeyCode.SPACE);
		public static final KeyCodeCombination SIMULATION_FASTER = alt(KeyCode.PLUS);
		public static final KeyCodeCombination SIMULATION_SLOWER = alt(KeyCode.MINUS);
		public static final KeyCodeCombination SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

		public static final KeyCodeCombination START_GAME = just(KeyCode.DIGIT1);
		public static final KeyCodeCombination ADD_CREDIT = just(KeyCode.DIGIT5);

		public static final KeyCodeCombination QUIT = just(KeyCode.Q);
		public static final KeyCodeCombination TEST_LEVELS = alt(KeyCode.T);
		public static final KeyCodeCombination SELECT_VARIANT = just(KeyCode.V);
		public static final KeyCodeCombination PLAY_CUTSCENES = alt(KeyCode.Z);

		public static final KeyCodeCombination BOOT = just(KeyCode.F3);
		public static final KeyCodeCombination FULLSCREEN = just(KeyCode.F11);
	}

	public static final Actions actions = new Actions();
	public static final Resources resources = new Resources();

	private GameUI2d ui;
	private Settings settings;

	@Override
	public void init() throws Exception {
		settings = new Settings(getParameters() != null ? getParameters().getNamed() : Collections.emptyMap());
		resources.init();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		var gameController = new GameController(settings.variant);
		ui = new GameUI2d(primaryStage, settings, gameController);
		actions.init(new ActionContext(ui));
		actions.reboot();
		ui.start();
		Logger.info("Game started. Locale: {} Framerate: {} Hz Settings: {}", Locale.getDefault(),
				ui.targetFrameratePy.get(), settings);
	}

	@Override
	public void stop() throws Exception {
		ui.stop();
		Logger.info("Game stopped");
	}
}