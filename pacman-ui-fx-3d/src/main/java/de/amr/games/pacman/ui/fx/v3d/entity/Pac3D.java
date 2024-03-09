/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.animation.HeadBanging;
import de.amr.games.pacman.ui.fx.v3d.animation.HipSwaying;
import de.amr.games.pacman.ui.fx.v3d.animation.WalkingAnimation;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.util.Ufx.actionAfterSeconds;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSeconds;
import static de.amr.games.pacman.ui.fx.v3d.animation.Turn.angle;
import static de.amr.games.pacman.ui.fx.v3d.model.Model3D.meshView;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man. Uses the OBJ model "pacman.obj".
 *
 * <p>
 * Missing: Specific 3D model for Ms. Pac-Man, mouth animation...
 *
 * @author Armin Reichert
 */
public class Pac3D {

    public static final String MESH_ID_EYES = "PacMan.Eyes";
    public static final String MESH_ID_HEAD = "PacMan.Head";
    public static final String MESH_ID_PALATE = "PacMan.Palate";

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
    public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
    public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

    private final Pac pac;
    private final Group root;
    private final Color headColor;
    private final Translate position = new Translate();
    private final Rotate orientation = new Rotate();
    private WalkingAnimation walkingAnimation;

    static Group createPacManGroup(Model3D model3D, Theme theme, double size) {
        var body = createBody(model3D, size,
            theme.color("pacman.color.head"),
            theme.color("pacman.color.eyes"),
            theme.color("pacman.color.palate"));
        return new Group(body);
    }

    static Group createMsPacManGroup(Model3D model3D, Theme theme, double size) {
        var body = createBody(model3D, size,
            theme.color("mspacman.color.head"),
            theme.color("mspacman.color.eyes"),
            theme.color("mspacman.color.palate"));
        return new Group(body, createFeminineParts(theme, size));
    }

    public static Pac3D createPacMan3D(Model3D model3D, Theme theme, Pac pacMan, double size) {
        checkNotNull(model3D);
        checkNotNull(theme);
        checkNotNull(pacMan);
        var pac3D = new Pac3D(createPacManGroup(model3D, theme, size), pacMan, theme.color("pacman.color.head"));
        pac3D.walkingAnimation = new HeadBanging(pacMan, pac3D.root);
        return pac3D;
    }

    public static Pac3D createMsPacMan3D(Model3D model3D, Theme theme, Pac msPacMan, double size) {
        checkNotNull(model3D);
        checkNotNull(theme);
        checkNotNull(msPacMan);
        var msPac3D = new Pac3D(createMsPacManGroup(model3D, theme, size), msPacMan, theme.color("mspacman.color.head"));
        msPac3D.walkingAnimation = new HipSwaying(msPacMan, msPac3D.root);
        return msPac3D;
    }

    private static Group createBody(Model3D model3D, double size, Color headColor, Color eyesColor, Color palateColor) {
        var head = new MeshView(model3D.mesh(MESH_ID_HEAD));
        head.setId(Model3D.cssID(MESH_ID_HEAD));
        head.setMaterial(coloredMaterial(headColor));

        var eyes = new MeshView(model3D.mesh(MESH_ID_EYES));
        eyes.setId(Model3D.cssID(MESH_ID_EYES));
        eyes.setMaterial(coloredMaterial(eyesColor));

        var palate = new MeshView(model3D.mesh(MESH_ID_PALATE));
        palate.setId(Model3D.cssID(MESH_ID_PALATE));
        palate.setMaterial(coloredMaterial(palateColor));

        var centerTransform = Model3D.centerOverOrigin(head);
        Stream.of(head, eyes, palate).map(Node::getTransforms).forEach(tf -> tf.add(centerTransform));

        var root = new Group(head, eyes, palate);
        root.getTransforms().add(Model3D.scale(root, size));

        // TODO check/fix Pac-Man mesh position and rotation in .obj file
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        return root;
    }

    private static Group createFeminineParts(Theme theme, double pacSize) {
        var bowMaterial = coloredMaterial(theme.color("mspacman.color.hairbow"));

        var bowLeft = new Sphere(1.2);
        bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));
        bowLeft.setMaterial(bowMaterial);

        var bowRight = new Sphere(1.2);
        bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));
        bowRight.setMaterial(bowMaterial);

        var pearlMaterial = coloredMaterial(theme.color("mspacman.color.hairbow.pearls"));

        var pearlLeft = new Sphere(0.4);
        pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));
        pearlLeft.setMaterial(pearlMaterial);

        var pearlRight = new Sphere(0.4);
        pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));
        pearlRight.setMaterial(pearlMaterial);

        var beautySpot = new Sphere(0.25);
        beautySpot.setMaterial(coloredMaterial(Color.rgb(100, 100, 100)));
        beautySpot.getTransforms().addAll(new Translate(-1.8, -3.7, -1));

        var silicone = coloredMaterial(theme.color("mspacman.color.boobs"));

        var boobLeft = new Sphere(1.5);
        boobLeft.setMaterial(silicone);
        boobLeft.getTransforms().addAll(new Translate(-1.5, -1.2, pacSize * 0.35));

        var boobRight = new Sphere(1.5);
        boobRight.setMaterial(silicone);
        boobRight.getTransforms().addAll(new Translate(-1.5, 1.2, pacSize * 0.35));

        return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
    }

    private Pac3D(Node pacNode, Pac pac, Color headColor) {
        this.root = new Group(pacNode);
        this.pac = pac;
        this.headColor = headColor;
        pacNode.getTransforms().setAll(position, orientation);
        meshView(pacNode, MESH_ID_EYES).drawModeProperty().bind(drawModePy);
        meshView(pacNode, MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
        meshView(pacNode, MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
    }

    public Group root() {
        return root;
    }

    public Pac pac() {
        return pac;
    }

    public Rotate orientation() {
        return orientation;
    }

    public Translate position() {
        return position;
    }

    public Animation createDyingAnimation(GameVariant variant) {
        return switch (variant) {
            case MS_PACMAN -> createMsPacManDyingAnimation();
            case PACMAN -> createPacManDyingAnimation();
        };
    }

    public WalkingAnimation walkingAnimation() {
        return walkingAnimation;
    }

    public void init() {
        headColorPy.set(headColor);
        root.setScaleX(1.0);
        root.setScaleY(1.0);
        root.setScaleZ(1.0);
        update();
    }

    public void update() {
        Vector2f center = pac.center();
        position.setX(center.x());
        position.setY(center.y());
        position.setZ(-5.0);
        orientation.setAxis(Rotate.Z_AXIS);
        orientation.setAngle(angle(pac.moveDir()));
        root.setVisible(pac.isVisible() && !outsideWorld());
        if (pac.isStandingStill()) {
            walkingAnimation.stop();
        } else {
            walkingAnimation.play();
        }
    }

    private boolean outsideWorld() {
        return position.getX() < HTS || position.getX() > TS * pac.world().numCols() - HTS;
    }

    private Animation createMsPacManDyingAnimation() {
        var spin = new RotateTransition(Duration.seconds(0.5), root);
        spin.setAxis(Rotate.X_AXIS); //TODO check this
        spin.setFromAngle(0);
        spin.setToAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(4);
        spin.setRate(2);
        spin.setDelay(Duration.seconds(0.5));
        return new SequentialTransition(
            spin,
            pauseSeconds(2)
        );
    }

    private Animation createPacManDyingAnimation() {
        Duration duration = Duration.seconds(1.0);
        short numSpins = 6;

        var spinning = new RotateTransition(duration.divide(numSpins), root);
        spinning.setAxis(Rotate.Z_AXIS);
        spinning.setByAngle(360);
        spinning.setCycleCount(numSpins);
        spinning.setInterpolator(Interpolator.LINEAR);

        var shrinking = new ScaleTransition(duration, root);
        shrinking.setToX(0.5);
        shrinking.setToY(0.5);
        shrinking.setToZ(0.0);
        shrinking.setInterpolator(Interpolator.LINEAR);

        var falling = new TranslateTransition(duration, root);
        falling.setToZ(4);
        falling.setInterpolator(Interpolator.EASE_IN);

        //TODO does not yet work as I want to
        var animation = new SequentialTransition(
            actionAfterSeconds(0, this::init),
            pauseSeconds(0.5),
            new ParallelTransition(spinning, shrinking, falling),
            pauseSeconds(1.0)
        );

        animation.setOnFinished(e -> {
            root.setVisible(false);
            root.setTranslateZ(0);
        });

        return animation;
    }
}