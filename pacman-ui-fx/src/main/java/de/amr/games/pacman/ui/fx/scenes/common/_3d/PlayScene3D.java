package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusActivatedEvent;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.BonusExpiredEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.Energizer3D;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LivesCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.Pellet3D;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_Assets;
import de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.sound.PlaySceneSoundHandler;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final String[] CONGRATS = { "Well done", "Congrats", "Awesome", "You did it", "You're the man*in",
			"WTF", "You old cheating bastard" };

	private final SubScene fxScene;

	private final PerspectiveCamera staticCamera = new PerspectiveCamera(true);
	private final PerspectiveCamera moveableCamera = new PerspectiveCamera(true);
	private final CameraController cameraController = new CameraController(staticCamera);

	private final SoundManager sounds;
	private PlaySceneSoundHandler playSceneSoundHandler;
	private PacManGameController gameController;

	private AmbientLight ambientLight;
	private PointLight pointLight;
	private CoordinateSystem coordSystem;
	private Box ground;
	private Group tgMaze;
	private Player3D player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private Maze3D maze;
	private List<Energizer3D> energizers;
	private List<Pellet3D> pellets;
	private Bonus3D bonus3D;
	private ScoreNotReally3D score3D;
	private LivesCounter3D livesCounter3D;

	public PlayScene3D(SoundManager sounds) {
		this.sounds = sounds;
		fxScene = new SubScene(new Group(), 800, 600, true, SceneAntialiasing.BALANCED);
		fxScene.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
		useStaticCamera();
	}

	private void buildSceneGraph() {
		final GameVariant gameVariant = gameController.gameVariant();

		fxScene.setFill(Color.rgb(20, 20, 60));

		maze = new Maze3D(game(), GameRendering2D_Assets.getMazeWallColor(game().currentLevel.mazeNumber));
		PhongMaterial foodMaterial = GameRendering3D_Assets.foodMaterial(gameVariant, game().currentLevel.mazeNumber);

		energizers = game().currentLevel.world.energizerTiles()//
				.map(tile -> new Energizer3D(tile, foodMaterial))//
				.collect(Collectors.toList());

		pellets = game().currentLevel.world.tiles()//
				.filter(game().currentLevel.world::isFoodTile)//
				.filter(not(game().currentLevel.world::isEnergizerTile))//
				.map(tile -> new Pellet3D(tile, foodMaterial)).collect(Collectors.toList());

		player = new Player3D(game().player);
		ghosts3D = game().ghosts().collect(
				Collectors.toMap(Function.identity(), ghost -> new Ghost3D(ghost, GameRendering2D.rendering(gameVariant))));

		bonus3D = new Bonus3D(gameVariant, GameRendering2D.rendering(gameVariant));

		score3D = new ScoreNotReally3D();
		livesCounter3D = new LivesCounter3D(game().player, 1, 1);

		tgMaze = new Group();
		tgMaze.getTransforms().add(new Translate(-14 * 8, -18 * 8));
		tgMaze.getChildren().addAll(score3D.get(), livesCounter3D.get());
		tgMaze.getChildren().addAll(maze.getBricks());
		tgMaze.getChildren().addAll(collect(energizers));
		tgMaze.getChildren().addAll(collect(pellets));
		tgMaze.getChildren().addAll(player.get());
		tgMaze.getChildren().addAll(collect(ghosts3D.values()));
		tgMaze.getChildren().add(bonus3D.get());

		ambientLight = new AmbientLight(Color.rgb(1, 1, 1));

		pointLight = new PointLight(Color.ANTIQUEWHITE);
		pointLight.translateXProperty().bind(player.get().translateXProperty());
		pointLight.translateYProperty().bind(player.get().translateYProperty());
		pointLight.lightOnProperty().bind(player.$visible);
		pointLight.setTranslateZ(-4);

		tgMaze.getChildren().addAll(ambientLight, pointLight);

		ground = new Box(28 * 8, 36 * 8, 0.1);
		ground.setMaterial(new PhongMaterial(GameRendering3D_Assets.mazeGroundColor(gameVariant, 1)));
		ground.setTranslateX(-4);
		ground.setTranslateY(-4);
		ground.setTranslateZ(4);

		coordSystem = new CoordinateSystem(game().currentLevel.world.numRows() * TS);
		fxScene.setRoot(new Group(coordSystem.getNode(), ground, tgMaze));
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
		playSceneSoundHandler = new PlaySceneSoundHandler(gameController, sounds);
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + hashCode();
	}

	private Collection<Node> collect(Collection<? extends Supplier<Node>> items) {
		return items.stream().map(Supplier::get).collect(Collectors.toList());
	}

	@Override
	public void stretchTo(double width, double height) {
		// data binding does the job
	}

	@Override
	public SubScene getFXSubScene() {
		return fxScene;
	}

	@Override
	public Camera getActiveCamera() {
		return fxScene.getCamera();
	}

	@Override
	public void useMoveableCamera(boolean on) {
		if (on) {
			useMoveableCamera();
		} else {
			useStaticCamera();
		}
	}

	private void useStaticCamera() {
		staticCamera.setNearClip(0.1);
		staticCamera.setFarClip(10000.0);
		staticCamera.setTranslateX(0);
		staticCamera.setTranslateY(270);
		staticCamera.setTranslateZ(-460);
		staticCamera.setRotationAxis(Rotate.X_AXIS);
		staticCamera.setRotate(30);
		fxScene.setCamera(staticCamera);
	}

	private void useMoveableCamera() {
		moveableCamera.setNearClip(0.1);
		moveableCamera.setFarClip(10000.0);
		moveableCamera.setTranslateZ(-250);
		moveableCamera.setRotationAxis(Rotate.X_AXIS);
		moveableCamera.setRotate(30);
		fxScene.setCamera(moveableCamera);
	}

	private void updateCamera() {
		if (getActiveCamera() == moveableCamera) {
			double x = Math.min(10, lerp(moveableCamera.getTranslateX(), player.get().getTranslateX()));
			double y = Math.max(50, lerp(moveableCamera.getTranslateY(), player.get().getTranslateY()));
			moveableCamera.setTranslateX(x);
			moveableCamera.setTranslateY(y);
		}
	}

	private static double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}

	@Override
	public void start() {
		log("Game scene %s: start", this);
		buildSceneGraph();
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
	}

	@Override
	public void update() {
		score3D.setHiscoreOnly(gameController.isAttractMode());
		score3D.update(game());
		score3D.get().setRotationAxis(Rotate.X_AXIS);
		score3D.get().setRotate(getActiveCamera().getRotate());
		livesCounter3D.get().setVisible(!gameController.isAttractMode());
		livesCounter3D.update(game());
		energizers.forEach(energizer3D -> energizer3D.update(game()));
		pellets.forEach(pellet3D -> pellet3D.update(game()));
		player.update();
		game().ghosts().map(ghosts3D::get).forEach(Ghost3D::update);
		bonus3D.update(game().bonus);
		updateCamera();
		playSceneSoundHandler.onUpdate();
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		playSceneSoundHandler.onGameEvent(gameEvent);

		if (gameEvent instanceof PacManGameStateChangedEvent) {
			onGameStateChange((PacManGameStateChangedEvent) gameEvent);
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			gameController.userInterface.showFlashMessage("Extra life!");
		}

		else if (gameEvent instanceof BonusActivatedEvent) {
			bonus3D.showSymbol(game().bonus);
		}

		else if (gameEvent instanceof BonusExpiredEvent) {
			bonus3D.hide();
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			bonus3D.showPoints(game().bonus);
		}
	}

	private void onGameStateChange(PacManGameStateChangedEvent event) {

		playSceneSoundHandler.onGameStateChange(event.oldGameState, event.newGameState);

		// enter READY
		if (event.newGameState == PacManGameState.READY) {
			setSceneColor();
		}

		// enter HUNTING
		if (event.newGameState == PacManGameState.HUNTING) {
			energizers.forEach(Energizer3D::startPumping);
		}

		// exit HUNTING
		if (event.oldGameState == PacManGameState.HUNTING && event.newGameState != PacManGameState.GHOST_DYING) {
			energizers.forEach(Energizer3D::stopPumping);
			bonus3D.hide();
		}

		// enter PACMAN_DYING
		if (event.newGameState == PacManGameState.PACMAN_DYING) {
			playAnimationPlayerDying();
		}

		// enter LEVEL_COMPLETE
		if (event.newGameState == PacManGameState.LEVEL_COMPLETE) {
			playAnimationLevelComplete();
		}

		// enter LEVEL_STARTING
		if (event.newGameState == PacManGameState.LEVEL_STARTING) {
			playAnimationLevelStarting();
		}
	}

	private void setSceneColor() {
		if (gameController.gameVariant() == GameVariant.PACMAN) {
			Color color = Color.rgb(20, 20, 60);
			fxScene.setFill(color);
			ambientLight.setColor(Color.AZURE);
		} else {
			Color mazeColor = GameRendering2D_Assets.getMazeWallColor(gameController.game().currentLevel.mazeNumber);
//			fxScene.setFill(mazeColor);
			ambientLight.setColor(mazeColor);
		}
	}

	private void playAnimationPlayerDying() {
		PauseTransition phase1 = new PauseTransition(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		ScaleTransition expand = new ScaleTransition(Duration.seconds(1), player.get());
		expand.setToX(2);
		expand.setToY(2);
		expand.setToZ(2);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1.5), player.get());
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		SequentialTransition animation = new SequentialTransition(phase1, expand, shrink);
		animation.setOnFinished(e -> {
			player.get().setScaleX(1);
			player.get().setScaleY(1);
			player.get().setScaleZ(1);
			game().player.visible = false;
			gameController.stateTimer().forceExpiration();
		});

		animation.play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().reset();

		String congrats = CONGRATS[new Random().nextInt(CONGRATS.length)];
		String message = String.format("%s!\n\nLevel %d complete.", congrats, game().currentLevelNumber);
		gameController.userInterface.showFlashMessage(message, 2);

		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player.visible = false;
			game().ghosts().forEach(ghost -> ghost.visible = false);
		});

		PauseTransition phase2 = new PauseTransition(Duration.seconds(2));

		SequentialTransition animation = new SequentialTransition(phase1, phase2);
		animation.setOnFinished(e -> {
			gameController.stateTimer().forceExpiration();
		});
		animation.play();
	}

	private void playAnimationLevelStarting() {
		gameController.stateTimer().reset();
		gameController.userInterface.showFlashMessage("Entering Level " + gameController.game().currentLevelNumber);

		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player.visible = true;
			game().ghosts().forEach(ghost -> ghost.visible = true);
		});

		PauseTransition phase2 = new PauseTransition(Duration.seconds(2));

		SequentialTransition animation = new SequentialTransition(phase1, phase2);
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		animation.play();
	}
}