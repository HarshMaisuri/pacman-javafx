/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.v3d.ActionHandler3D;
import de.amr.games.pacman.ui.fx.v3d.animation.SinusCurveAnimation;
import de.amr.games.pacman.ui.fx.v3d.entity.*;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;
import static de.amr.games.pacman.ui.fx.util.Ufx.*;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be selected sequentially using keys <code>Alt+LEFT</code>
 * and <code>Alt+RIGHT</code>.</p>
 *
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

    private final Map<Perspective, CameraController> camControllerMap = new EnumMap<>(Perspective.class);
    private final SubScene fxSubScene;
    private final PerspectiveCamera camera;
    private final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective") {
        @Override
        protected void invalidated() {
            Logger.info("Perspective set to {}", get());
            currentCamController().reset(camera);
        }
    };
    private final Text3D readyMessageText3D;
    private GameLevel3D level3D;
    private GameSceneContext context;
    private boolean scoreVisible;

    public PlayScene3D() {
        camControllerMap.put(Perspective.DRONE, new CamDrone());
        camControllerMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
        camControllerMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
        camControllerMap.put(Perspective.TOTAL, new CamTotal());

        var coordSystem = new CoordSystem();
        coordSystem.visibleProperty().bind(PY_3D_AXES_VISIBLE);

        var ambientLight = new AmbientLight();
        ambientLight.colorProperty().bind(PY_3D_LIGHT_COLOR);

        readyMessageText3D = new Text3D();
        setScoreVisible(true);

        var sceneRoot = new Group(new Text("<3D game level>"), coordSystem, ambientLight, readyMessageText3D.getRoot());
        // initial sub-scene size is irrelevant, gets bound to main scene size in init method
        fxSubScene = new SubScene(sceneRoot, 42, 42, true, SceneAntialiasing.BALANCED);
        camera = new PerspectiveCamera(true);
        fxSubScene.setCamera(camera);
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    @Override
    public void setContext(GameSceneContext context) {
        this.context = context;
    }

    @Override
    public GameSceneContext context() {
        return context;
    }

    @Override
    public void init() {
        createReadyMessageText3D();
        perspectivePy.bind(PY_3D_PERSPECTIVE);
        Logger.info("3D play scene initialized.");
    }

    @Override
    public void end() {
        perspectivePy.unbind();
        Logger.info("3D play scene ended.");
    }

    @Override
    public void update() {
        if (level3D != null) {
            level3D.update();
            currentCamController().update(camera, level3D.pac3D());
            updateSound();
        }
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void setScoreVisible(boolean scoreVisible) {
        this.scoreVisible = scoreVisible;
    }

    @Override
    public Node root() {
        return fxSubScene;
    }

    public void bindSize(ObservableDoubleValue widthPy, ObservableDoubleValue heightPy) {
        fxSubScene.widthProperty().bind(widthPy);
        fxSubScene.heightProperty().bind(heightPy);
    }

    private CameraController currentCamController() {
        return camControllerMap.getOrDefault(perspectivePy.get(), camControllerMap.get(Perspective.TOTAL));
    }

    private void replaceGameLevel3D(GameLevel level) {

        //TODO In level 1 when switching between 2D and 3D view, the 3D level is always recreated. How to avoid this?
        if (level.number() > 1 && level3D != null && level3D.levelNumber() == level.number()) {
            Logger.info("3D game level up-to-date");
            return;
        }

        level3D = new GameLevel3D(level, context.theme(), context.spriteSheet());
        // replace initial placeholder or previous 3D level
        ((Group) fxSubScene.getRoot()).getChildren().set(0, level3D.root());

        // center over origin
        double centerX = level.world().numCols() * HTS;
        double centerY = level.world().numRows() * HTS;
        level3D.root().setTranslateX(-centerX);
        level3D.root().setTranslateY(-centerY);

        // keep the scores rotated such that the viewer always sees them frontally
        level3D.scores3D().getRoot().rotationAxisProperty().bind(camera.rotationAxisProperty());
        level3D.scores3D().getRoot().rotateProperty().bind(camera.rotateProperty());

        if (context.gameState() == GameState.LEVEL_TEST) {
            readyMessageText3D.setText("LEVEL %s TEST".formatted(level.number()));
        }

        if (PY_3D_FLOOR_TEXTURE_RND.get()) {
            List<String> names = context.theme().getArray("texture.names");
            PY_3D_FLOOR_TEXTURE.set(names.get(randomInt(0, names.size())));
        }
        Logger.info("3D game level {} created.", level.number());
    }

    private void createReadyMessageText3D() {
        readyMessageText3D.beginBatch();
        readyMessageText3D.setTextColor(Color.YELLOW);
        readyMessageText3D.setFont(context.theme().font("font.arcade", 6));
        readyMessageText3D.setText("READY!");
        readyMessageText3D.endBatch();
        readyMessageText3D.translate(0, 16, -4.5);
        readyMessageText3D.rotate(Rotate.X_AXIS, 90);
    }

    @Override
    public void handleKeyboardInput() {
        var actionHandler = (ActionHandler3D) context.actionHandler();
        if (Keyboard.pressed(KEYS_ADD_CREDIT) && !context.gameController().hasCredit()) {
            actionHandler.addCredit();
        } else if (Keyboard.pressed(KEY_PREV_PERSPECTIVE)) {
            actionHandler.selectPrevPerspective();
        } else if (Keyboard.pressed(KEY_NEXT_PERSPECTIVE)) {
            actionHandler.selectNextPerspective();
        } else if (Keyboard.pressed(KEY_CHEAT_EAT_ALL)) {
            actionHandler.cheatEatAllPellets();
        } else if (Keyboard.pressed(KEY_CHEAT_ADD_LIVES)) {
            actionHandler.cheatAddLives();
        } else if (Keyboard.pressed(KEY_CHEAT_NEXT_LEVEL)) {
            actionHandler.cheatEnterNextLevel();
        } else if (Keyboard.pressed(KEY_CHEAT_KILL_GHOSTS)) {
            actionHandler.cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onSceneVariantSwitch() {
        if (level3D == null) {
            Logger.error("Oh no, where is my 3D level?");
            return;
        }
        context.gameLevel().ifPresent(level -> {
            level3D.world3D().eatables3D().forEach(
                eatable3D -> eatable3D.getRoot().setVisible(!level.world().hasEatenFoodAt(eatable3D.tile())));
            if (Globals.oneOf(context.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
                level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
            }
            if (!level.isDemoLevel()) {
                context.soundHandler().ensureSirenStarted(context.gameVariant(), level.huntingPhaseIndex() / 2);
            }
        });
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        if (level3D == null) {
            Logger.error("WTF: No 3D game level exists?");
            return;
        }
        context.gameLevel().ifPresent(level -> {
            // When cheat "eat all pellets" has been used, no tile is present in the event.
            // In that case, ensure that the 3D pellets are in sync with the model.
            if (e.tile().isEmpty()) {
                level.world().tiles()
                    .filter(level.world()::hasEatenFoodAt)
                    .map(level3D.world3D()::eatableAt)
                    .flatMap(Optional::stream)
                    .forEach(Eatable3D::onEaten);
            } else {
                Vector2i tile = e.tile().get();
                level3D.world3D().eatableAt(tile).ifPresent(level3D::eat);
            }
        });
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        if (level3D != null) {
            context.gameLevel().flatMap(GameLevel::bonus).ifPresent(level3D::replaceBonus3D);
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        if (level3D != null) {
            level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        }
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        if (level3D != null) {
            level3D.bonus3D().ifPresent(Bonus3D::hide);
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        e.game.level().ifPresent(this::replaceGameLevel3D);
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        e.game.level().ifPresent(level -> {
            readyMessageText3D.setVisible(true);
            level3D.pac3D().init();
            Stream.of(level3D.ghosts3D()).forEach(Ghost3D::init);
            level3D.updateLevelCounter3D();
        });
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        if (level3D != null) {
            level3D.pac3D().walkingAnimation().setPowerWalking(true);
        }
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        if (level3D != null) {
            level3D.pac3D().walkingAnimation().setPowerWalking(false);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        switch (e.newState) {

            case READY ->
                context.gameLevel().ifPresent(level -> {
                    readyMessageText3D.setText("READY!");
                    readyMessageText3D.setVisible(true);
                    level3D.pac3D().init();
                    Stream.of(level3D.ghosts3D()).forEach(Ghost3D::init);
                });

            case HUNTING -> {
                level3D.pac3D().init();
                Stream.of(level3D.ghosts3D()).forEach(Ghost3D::init);
                level3D.livesCounter3D().startAnimation();
                level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
            }

            case PACMAN_DYING -> lockStateAndPlayAfterSeconds(1.0, level3D.pac3D().createDyingAnimation(context.gameVariant()));

            case GHOST_DYING ->
                context.gameLevel().ifPresent(level -> {
                    Rectangle2D[] sprites = switch (context.gameVariant()) {
                        case MS_PACMAN -> context.<MsPacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                        case PACMAN    -> context.<PacManGameSpriteSheet>spriteSheet().ghostNumberSprites();
                    };
                    var killedGhosts = level.thisFrame().killedGhosts;
                    killedGhosts.forEach(ghost -> {
                        Image numberImage = context.spriteSheet().subImage(sprites[ghost.killedIndex()]);
                        level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
                    });
                });

            case CHANGING_TO_NEXT_LEVEL ->
                context.gameLevel().ifPresent(level -> {
                    keepGameStateForSeconds(3);
                    replaceGameLevel3D(level);
                    level3D.pac3D().init();
                    currentCamController().reset(camera);
                });

            case LEVEL_COMPLETE ->
                context.gameLevel().ifPresent(level -> {
                    // if cheat has been used to complete level, 3D food might still exist:
                    level3D.world3D().eatables3D().forEach(level3D::eat);
                    level3D.livesCounter3D().stopAnimation();
                    boolean intermissionAfterLevel = level.data().intermissionNumber() != 0;
                    lockStateAndPlayAfterSeconds(1.0,
                        createLevelCompleteAnimation(level),
                        actionAfterSeconds(1.0, () -> {
                            level.pac().hide();
                            level3D.livesCounter3D().lightOnPy.set(false);
                            if (!intermissionAfterLevel) {
                                context.clip("audio.level_complete").play();
                                context.actionHandler().showFlashMessageSeconds(2,
                                    pickLevelCompleteMessage(level.number()));
                            }
                        }),
                        intermissionAfterLevel ? pauseSeconds(0) : createLevelChangeAnimation(),
                        immediateAction(() -> level3D.livesCounter3D().lightOnPy.set(true))
                    );
                });

            case GAME_OVER -> {
                keepGameStateForSeconds(3);
                level3D.livesCounter3D().stopAnimation();
                context.actionHandler().showFlashMessageSeconds(3, PICKER_GAME_OVER.next());
                context.clip("audio.game_over").play();
            }

            case LEVEL_TEST ->
                context.gameLevel().ifPresent(level -> {
                    PY_3D_PERSPECTIVE.set(Perspective.TOTAL);
                    level.pac().setVisible(true);
                    level3D.pac3D().update();
                });

            default -> {}
        }

        // on state exit
        if (e.oldState != null) {
            switch (e.oldState) {
                case READY -> readyMessageText3D.setVisible(false);
                case HUNTING -> {
                    if (e.newState != GameState.GHOST_DYING) {
                        level3D.world3D().energizers3D().forEach(Energizer3D::stopPumping);
                        level3D.bonus3D().ifPresent(Bonus3D::hide);
                    }
                }
                default -> {}
            }
        }
    }

    private String pickLevelCompleteMessage(int levelNumber) {
        return PICKER_LEVEL_COMPLETE.next() + "\n\n" + message(context.messageBundles(), "level_complete", levelNumber);
    }

    private Transition createLevelChangeAnimation() {
        var rotation = new RotateTransition(Duration.seconds(1.5), level3D.root());
        rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        return new SequentialTransition(
            immediateAction(() -> {
                perspectivePy.unbind();
                perspectivePy.set(Perspective.TOTAL);
            }),
            rotation,
            actionAfterSeconds(0.5, () -> context.clip("audio.sweep").play()),
            actionAfterSeconds(0.5, () -> perspectivePy.bind(PY_3D_PERSPECTIVE))
        );
    }

    private Transition createLevelCompleteAnimation(GameLevel level) {
        if (level.data().numFlashes() == 0) {
            return pauseSeconds(1.0);
        }
        double wallHeight = PY_3D_WALL_HEIGHT.get();
        var animation = new SinusCurveAnimation(level.data().numFlashes());
        animation.setAmplitude(wallHeight);
        animation.elongationPy.set(level3D.world3D().wallHeightPy.get());
        level3D.world3D().wallHeightPy.bind(animation.elongationPy);
        animation.setOnFinished(e -> {
            level3D.world3D().wallHeightPy.bind(PY_3D_WALL_HEIGHT);
            PY_3D_WALL_HEIGHT.set(wallHeight);
        });
        return animation;
    }

    private void updateSound() {
        context.gameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                return;
            }
            if (level.pac().starvingTicks() > 8) { // TODO not sure how this is done in Arcade game
                context.clip("audio.pacman_munch").stop();
            }
            if (!level.thisFrame().pacKilled && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
                .anyMatch(Ghost::isVisible)) {
                context.soundHandler().ensureLoopEndless(context.clip("audio.ghost_returning"));
            } else {
                context.clip("audio.ghost_returning").stop();
            }
        });
    }

    /**
     * Keeps the current game state for given number of seconds, then forces the state timer to expire.
     */
    private void keepGameStateForSeconds(double seconds) {
        context.gameState().timer().resetIndefinitely();
        actionAfterSeconds(seconds, () -> context.gameState().timer().expire()).play();
    }

    /**
     * Locks the current game state, waits given seconds, plays given animations and unlocks the state
     * when the animations have finished.
     */
    private void lockStateAndPlayAfterSeconds(double seconds, Animation... animations) {
        context.gameState().timer().resetIndefinitely();
        var animationSequence = new SequentialTransition(animations);
        if (seconds > 0) {
            animationSequence.setDelay(Duration.seconds(seconds));
        }
        animationSequence.setOnFinished(e -> context.gameState().timer().expire());
        animationSequence.play();
    }
}