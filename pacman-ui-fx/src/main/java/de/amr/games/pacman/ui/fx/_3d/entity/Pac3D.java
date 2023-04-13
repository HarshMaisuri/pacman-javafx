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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.IllegalGameVariantException;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._3d.animation.CollapseAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.Turn;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * Missing: Specific 3D-model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Duration NODDING_DURATION = Duration.seconds(0.2);

	public final BooleanProperty noddingPy = new SimpleBooleanProperty(this, "nodding", false) {
		@Override
		protected void invalidated() {
			if (get()) {
				createNoddingAnimation();
			} else {
				endNodding();
				nodding = null;
			}
		}
	};

	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final GameLevel level;
	private final Pac pac;
	private final Group root = new Group();
	private final Color headColor;
	private final PointLight light;
	private final Rotate orientation;
	private RotateTransition nodding;

	public Pac3D(GameLevel level, Pac pac, Node pacNode, Color headColor) {
		this.level = Objects.requireNonNull(level);
		this.pac = Objects.requireNonNull(pac);
		Objects.requireNonNull(pacNode);
		this.headColor = Objects.requireNonNull(headColor);

		orientation = new Rotate(Turn.angle(pac.moveDir()), Rotate.Z_AXIS);
		pacNode.getTransforms().setAll(orientation);
		light = createLight();

		root.getChildren().add(pacNode);
		noddingPy.bind(Env.d3_pacNoddingPy);
	}

	public Node getRoot() {
		return root;
	}

	public PointLight getLight() {
		return light;
	}

	public void init() {
		headColorPy.set(headColor);
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		endNodding();
		update();
	}

	public void update() {
		if (outsideWorld()) {
			root.setVisible(false);
		} else {
			root.setVisible(pac.isVisible());
		}
		orientation.setAngle(Turn.angle(pac.moveDir()));
		root.setTranslateX(pac.center().x());
		root.setTranslateY(pac.center().y());
		root.setTranslateZ(-4.25);
		root.setRotationAxis(pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS);
		if (nodding != null) {
			updateNodding();
		}
		updateLight();
	}

	private void createNoddingAnimation() {
		nodding = new RotateTransition(NODDING_DURATION, root);
		nodding.setFromAngle(-40);
		nodding.setToAngle(20);
		nodding.setCycleCount(Animation.INDEFINITE);
		nodding.setAutoReverse(true);
		nodding.setInterpolator(Interpolator.EASE_BOTH);
	}

	private void updateNodding() {
		if (pac.velocity().length() == 0 || !pac.moveResult.moved || pac.restingTicks() == Pac.REST_FOREVER) {
			endNodding();
			root.setRotate(0);
		} else if (nodding.getStatus() != Status.RUNNING) {
			var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
			nodding.setAxis(axis);
			nodding.playFromStart();
			LOG.trace("%s: Nodding created and started", pac.name());
		}
	}

	private void endNodding() {
		if (nodding != null && nodding.getStatus() == Status.RUNNING) {
			nodding.stop();
			var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
			root.setRotationAxis(axis);
			root.setRotate(0);
			LOG.trace("%s: Nodding stopped", pac.name());
		}
	}

	public Animation createDyingAnimation() {
		var variant = level.game().variant();
		return switch (variant) {
		case MS_PACMAN -> createMsPacManDyingAnimation();
		case PACMAN -> createPacManDyingAnimation();
		default -> throw new IllegalGameVariantException(variant);
		};
	}

	private Animation createPacManDyingAnimation() {
		return new SequentialTransition(Ufx.pause(0.25), new CollapseAnimation(root).getAnimation());
	}

	private Animation createMsPacManDyingAnimation() {
		var layOnBack = new RotateTransition(Duration.seconds(0.2), root);
		layOnBack.setAxis(Rotate.Y_AXIS);
		layOnBack.setFromAngle(0);
		layOnBack.setToAngle(90);

		var spin = new RotateTransition(Duration.seconds(0.2), root);
		spin.setAxis(Rotate.Y_AXIS);
		spin.setByAngle(-180);
		spin.setCycleCount(4);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setDelay(Duration.seconds(0.3));

		return new SequentialTransition(layOnBack, spin, Ufx.pause(2.0));
	}

	private PointLight createLight() {
		var pointLight = new PointLight();
		pointLight.setColor(Color.rgb(255, 255, 0, 0.25));
		pointLight.setMaxRange(2 * TS);
		pointLight.translateXProperty().bind(root.translateXProperty());
		pointLight.translateYProperty().bind(root.translateYProperty());
		pointLight.setTranslateZ(-TS);
		return pointLight;
	}

	private void updateLight() {
		boolean isVisible = pac.isVisible();
		boolean isAlive = !pac.isDead();
		boolean hasPower = pac.powerTimer().isRunning();
		var maxRange = pac.isPowerFading(level) ? 4 : 8;
		light.setLightOn(lightedPy.get() && isVisible && isAlive && hasPower);
		light.setMaxRange(hasPower ? maxRange * TS : 0);
	}

	private boolean outsideWorld() {
		double centerX = pac.position().x() + HTS;
		return centerX < HTS || centerX > level.world().numCols() * TS - HTS;
	}
}