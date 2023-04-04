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
package de.amr.games.pacman.ui.fx._3d.animation;

import java.util.Objects;
import java.util.function.Supplier;

import de.amr.games.pacman.lib.steering.Direction;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Animated turns of creature when changing direction.
 * 
 * @author Armin Reichert
 */
public class TurningAnimation {

	public record Turn(int fromAngle, int toAngle) {
	}

	private static final short L = 0;
	private static final short U = 90;
	private static final short R = 180;
	private static final short D = 270;

	private static double angle(Direction dir) {
		return switch (dir) {
		case LEFT -> L;
		case RIGHT -> R;
		case UP -> U;
		case DOWN -> D;
		default -> L;
		};
	}

	//@formatter:off
	private static final Turn[][] TURNS = {
		{ null,            new Turn(L, R), new Turn(L, U),  new Turn(L, -U) }, // LEFT  -> *
		{ new Turn(R, L),  null,           new Turn(R, U),  new Turn(R, D)  }, // RIGHT -> *
		{ new Turn(U, L),  new Turn(U, R), null,            new Turn(U, D)  }, // UP    -> *
		{ new Turn(-U, L), new Turn(D, R), new Turn(-U, U), null            }, // DOWN  -> *
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == null ? 0 : dir.ordinal();
	}

	private final Node turnedObject;
	private final RotateTransition rotation;
	private final Supplier<Direction> fnTargetDir;
	private Direction currentTargetDir;

	public TurningAnimation(Node turnedObject, Supplier<Direction> fnTargetDir) {
		this.turnedObject = Objects.requireNonNull(turnedObject);
		this.fnTargetDir = Objects.requireNonNull(fnTargetDir);
		rotation = new RotateTransition(Duration.seconds(0.2), turnedObject);
		rotation.setAxis(Rotate.Z_AXIS);
		rotation.setInterpolator(Interpolator.EASE_BOTH);
		init();
	}

	public void init() {
		rotation.stop();
		currentTargetDir = fnTargetDir.get();
		turnedObject.setRotationAxis(Rotate.Z_AXIS);
		turnedObject.setRotate(angle(currentTargetDir));
	}

	public void update() {
		var targetDir = fnTargetDir.get();
		if (currentTargetDir != targetDir) {
			var turn = TURNS[index(currentTargetDir)][index(targetDir)];
			rotation.stop();
			rotation.setFromAngle(turn.fromAngle);
			rotation.setToAngle(turn.toAngle);
			rotation.playFromStart();
			currentTargetDir = targetDir;
		}
	}
}