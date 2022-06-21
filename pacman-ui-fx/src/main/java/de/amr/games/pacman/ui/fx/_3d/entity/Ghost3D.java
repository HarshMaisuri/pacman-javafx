/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashingTransition;
import de.amr.games.pacman.ui.fx._3d.animation.FadeInTransition3D;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import javafx.animation.Animation.Status;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D representation of a ghost. A ghost is displayed in one of 4 modes:
 * <ul>
 * <li>complete ghost with colored skin and eyes,
 * <li>complete ghost with blue skin, maybe flashing, and eyes,
 * <li>eyes only,
 * <li>number cube indicating the value of the dead ghost.
 * </ul>
 * 
 * @author Armin Reichert
 */
public class Ghost3D {

	public enum AnimationMode {
		COLORED, FRIGHTENED, EYES, NUMBER;
	}

	public static class NumberAnimation {

		private final Box numberCube;
		private final Image[] valueImages;

		public NumberAnimation(Rendering2D r2D) {
			numberCube = new Box(8, 8, 8);
			valueImages = r2D.createGhostValueList().frames().map(r2D::getSpriteImage).toArray(Image[]::new);
		}

		public Node getRoot() {
			return numberCube;
		}

		public void setNumber(int number) {
			var texture = valueImages[number];
			var material = new PhongMaterial();
			material.setBumpMap(texture);
			material.setDiffuseMap(texture);
			numberCube.setMaterial(material);
		}
	}

	public static class BodyAnimation {

		private final int ghostID;
		private final Group root;
		private final Shape3D skin;
		private final Group eyes;
		private final Shape3D eyePupils;
		private final Shape3D eyeBalls;
		private final ColorFlashingTransition flashingAnimation;
		private final FadeInTransition3D revivalAnimation;

		public BodyAnimation(PacManModel3D model3D, int ghostID) {
			this.ghostID = ghostID;
			root = model3D.createGhost(//
					faded(Rendering3D.getGhostSkinColor(ghostID)), //
					Rendering3D.getGhostEyeBallColor(), //
					Rendering3D.getGhostPupilColor());
			skin = (Shape3D) root.getChildren().get(0);
			eyes = (Group) root.getChildren().get(1);
			eyePupils = (Shape3D) eyes.getChildren().get(0);
			eyeBalls = (Shape3D) eyes.getChildren().get(1);
			flashingAnimation = new ColorFlashingTransition(//
					Rendering3D.getGhostSkinColorFrightened(), //
					Rendering3D.getGhostSkinColorFrightened2());
			revivalAnimation = new FadeInTransition3D(Duration.seconds(1.5), skin,
					faded(Rendering3D.getGhostSkinColor(ghostID)));
		}

		public Node getRoot() {
			return root;
		}

		public void setShowBody(boolean showSkin) {
			skin.setVisible(showSkin);
		}

		public void playFlashingAnimation() {
			if (flashingAnimation.getStatus() != Status.RUNNING) {
				skin.setMaterial(flashingAnimation.getMaterial());
				flashingAnimation.playFromStart();
			}
		}

		public void ensureFlashingAnimationStopped() {
			if (flashingAnimation.getStatus() == Status.RUNNING) {
				flashingAnimation.stop();
			}
		}

		public void setFrightened(boolean frightened) {
			ensureFlashingAnimationStopped();
			if (frightened) {
				setShapeColor(skin, faded(Rendering3D.getGhostSkinColorFrightened()));
				setShapeColor(eyeBalls, Rendering3D.getGhostEyeBallColorFrightened());
				setShapeColor(eyePupils, Rendering3D.getGhostPupilColorFrightened());
			} else {
				setShapeColor(skin, faded(Rendering3D.getGhostSkinColor(ghostID)));
				setShapeColor(eyeBalls, Rendering3D.getGhostEyeBallColor());
				setShapeColor(eyePupils, Rendering3D.getGhostPupilColor());
			}
		}

		private void setShapeColor(Shape3D shape, Color diffuseColor) {
			var material = new PhongMaterial(diffuseColor);
			material.setSpecularColor(diffuseColor.brighter());
			shape.setMaterial(material);
		}

		private Color faded(Color color) {
			return Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.90);
		}
	}

	public final Ghost ghost;
	private final Group root = new Group();
	private final Motion motion = new Motion();
	private final NumberAnimation numberAnimation;
	private final BodyAnimation bodyAnimation;
	private AnimationMode animationMode;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		numberAnimation = new NumberAnimation(r2D);
		bodyAnimation = new BodyAnimation(model3D, ghost.id);
		setAnimationMode(AnimationMode.COLORED);
	}

	public Node getRoot() {
		return root;
	}

	public void reset() {
		setAnimationMode(AnimationMode.COLORED);
		update();
	}

	public void update() {
		motion.update(ghost, root);
		updateAppearance();
	}

	private void updateAppearance() {
		double x = ghost.position.x;
		double leftEdge = 0;
		double rightEdge = ArcadeWorld.TILES_X * World.TS;
		boolean outside = x < leftEdge - 4 || x > rightEdge - 4;
		if (outside) {
			root.setVisible(true);
			root.setOpacity(0.5);
		} else {
			root.setVisible(ghost.visible);
			root.setOpacity(1);
		}
	}

	public AnimationMode getAnimationMode() {
		return animationMode;
	}

	public void setAnimationMode(AnimationMode animationMode) {
		if (this.animationMode != animationMode) {
			this.animationMode = animationMode;
			switch (animationMode) {
			case COLORED -> {
				bodyAnimation.setShowBody(true);
				bodyAnimation.setFrightened(false);
				root.getChildren().setAll(bodyAnimation.getRoot());
			}
			case FRIGHTENED -> {
				bodyAnimation.setShowBody(true);
				bodyAnimation.setFrightened(true);
				root.getChildren().setAll(bodyAnimation.getRoot());
			}
			case EYES -> {
				bodyAnimation.setShowBody(false);
				bodyAnimation.setFrightened(false);
				root.getChildren().setAll(bodyAnimation.getRoot());
			}
			case NUMBER -> {
				numberAnimation.setNumber(ghost.killIndex);
				// rotate node such that number can be read from left to right
				root.setRotationAxis(Rotate.X_AXIS);
				root.setRotate(0);
				root.getChildren().setAll(numberAnimation.getRoot());
			}
			}
		}
	}

	public void playFlashingAnimation() {
		bodyAnimation.playFlashingAnimation();
	}

	public void playRevivalAnimation() {
		var animation = new SequentialTransition(bodyAnimation.revivalAnimation);
		animation.setOnFinished(e -> setAnimationMode(AnimationMode.COLORED));
		animation.playFromStart();
	}
}