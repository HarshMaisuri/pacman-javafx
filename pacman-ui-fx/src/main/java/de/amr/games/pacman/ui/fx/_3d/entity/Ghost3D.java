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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.world.World.HTS;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.BlueFlashingAnimation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

/**
 * 3D-representation of a ghost. A ghost is displayed in one of 3 modes: as a full ghost, as eyes only or as a bonus
 * symbol indicating the bounty paid for killing the ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Creature3D {

	private enum DisplayMode {
		FULL, EYES, BOUNTY
	}

	public final Ghost ghost;

	private final Rendering2D r2D;
	private final Group completeGhost3D;
	private final Shape3D skin3D;
	private final Group eyes3D;
	private final Box cube3D;
	private final BlueFlashingAnimation flashing = new BlueFlashingAnimation();

	private DisplayMode displayMode;

	public Ghost3D(Ghost ghost, Group completeGhost3D, Group eyes3D, Rendering2D r2D) {
		this.targetDir = ghost.dir();
		this.ghost = ghost;
		this.r2D = r2D;
		this.completeGhost3D = completeGhost3D;
		this.skin3D = (Shape3D) completeGhost3D.getChildren().get(0); // TODO cleanup
		this.eyes3D = eyes3D;

		Group ghostVariants = new Group(completeGhost3D, eyes3D);
		ghostVariants.setRotationAxis(Rotate.Z_AXIS);
		ghostVariants.setRotate(rotationAngle(ghost.dir()));

		// TODO checkme
		turningAnimation.setNode(ghostVariants);

		cube3D = new Box(8, 8, 8);

		getChildren().addAll(ghostVariants, cube3D);

		displayCompleteGhost();
		setNormalSkinColor();
		setTranslateZ(-HTS);
	}

	private void displayNumberCube() {
		if (displayMode == DisplayMode.BOUNTY) {
			return;
		}
		Rectangle2D sprite = r2D.getBountyNumberSprites().get(ghost.bounty);
		Image image = r2D.extractRegion(sprite);
		PhongMaterial material = new PhongMaterial();
		material.setBumpMap(image);
		material.setDiffuseMap(image);
		cube3D.setMaterial(material);
		cube3D.setVisible(true);
		completeGhost3D.setVisible(false);
		eyes3D.setVisible(false);
		setRotationAxis(Rotate.X_AXIS);
		setRotate(0);
		displayMode = DisplayMode.BOUNTY;
	}

	private void displayEyes() {
		if (displayMode != DisplayMode.EYES) {
			completeGhost3D.setVisible(false);
			eyes3D.setVisible(true);
			cube3D.setVisible(false);
			displayMode = DisplayMode.EYES;
		}
		updateDirection(ghost);
	}

	private void displayCompleteGhost() {
		if (displayMode != DisplayMode.FULL) {
			completeGhost3D.setVisible(true);
			eyes3D.setVisible(false);
			cube3D.setVisible(false);
			displayMode = DisplayMode.FULL;
		}
		updateDirection(ghost);
	}

	public void update() {
		super.update(ghost);
		if (ghost.bounty > 0) {
			displayNumberCube();
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			displayEyes();
		} else {
			displayCompleteGhost();
		}
	}

	public void playFlashingAnimation() {
		skin3D.setMaterial(flashing.getMaterial());
		flashing.playFromStart();
	}

	public void setNormalSkinColor() {
		flashing.stop();
		setSkinColor(r2D.getGhostColor(ghost.id));
	}

	public void setBlueSkinColor() {
		flashing.stop();
		setSkinColor(Color.CORNFLOWERBLUE);
	}

	private void setSkinColor(Color color) {
		PhongMaterial material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		skin3D.setMaterial(material);
	}
}