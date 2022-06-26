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
package de.amr.games.pacman.ui.fx._3d.model;

import java.util.stream.Stream;

import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * This uses part of a Blender model created by Gianmarco Cavallaccio (https://www.artstation.com/gianmart).
 * <p>
 * 
 * One mesh for Pac-Man and one for a ghost are sufficient because their appearance is managed programmatically.
 * 
 * @author Armin Reichert
 */
public class Model3D {

	private static Model3D theThing;

	public static Model3D get() {
		if (theThing == null) {
			theThing = new Model3D();
		}
		return theThing;
	}

	private static Translate centerOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	private ObjModel pacManModel;
	private ObjModel ghostModel;

	private Model3D() {
		pacManModel = new ObjModel(getClass().getResource("/common/gianmarco/pacman.obj"));
		ghostModel = new ObjModel(getClass().getResource("/common/gianmarco/ghost.obj"));
	}

	public Group createPac(Color faceColor, Color eyesColor, Color palateColor) {
		var face = pacManModel.createMeshView("Sphere_yellow_packman");
		face.setMaterial(new PhongMaterial(faceColor));

		var eyes = pacManModel.createMeshView("Sphere.008_Sphere.010_grey_wall");
		eyes.setMaterial(new PhongMaterial(eyesColor));

		var palate = pacManModel.createMeshView("Sphere_grey_wall");
		palate.setMaterial(new PhongMaterial(palateColor));

		var center = centerOverOrigin(face);
		Stream.of(face, eyes, palate).forEach(meshView -> {
			meshView.getTransforms().add(center);
			meshView.drawModeProperty().bind(Env.drawMode3D);
		});

		var root3D = new Group(face, eyes, palate);
		root3D.getTransforms().add(scale(root3D, 8.5));
		root3D.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

		return root3D;
	}

	public Shape3D pacFace(Group pac) {
		return (Shape3D) pac.getChildren().get(0);
	}

	public Shape3D pacEyes(Group pac) {
		return (Shape3D) pac.getChildren().get(1);
	}

	public Shape3D pacPalate(Group pac) {
		return (Shape3D) pac.getChildren().get(2);
	}

	public Group createGhost(Color dressColor, Color eyeBallColor, Color pupilColor) {
		var dress = ghostModel.createMeshView("Sphere.004_Sphere.034_light_blue_ghost");
		dress.setMaterial(new PhongMaterial(dressColor));

		var eyeBalls = ghostModel.createMeshView("Sphere.009_Sphere.036_white");
		eyeBalls.setMaterial(new PhongMaterial(eyeBallColor));

		var pupils = ghostModel.createMeshView("Sphere.010_Sphere.039_grey_wall");
		pupils.setMaterial(new PhongMaterial(pupilColor));

		Stream.of(dress, eyeBalls, pupils).forEach(meshView -> meshView.drawModeProperty().bind(Env.drawMode3D));

		var center = centerOverOrigin(dress);
		dress.getTransforms().add(center);

		var eyes = new Group(pupils, eyeBalls);
		eyes.getTransforms().add(center);

		var ghost = new Group(dress, eyes);
		ghost.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		ghost.getTransforms().add(scale(ghost, World.TS));

		return ghost;
	}

	public Shape3D ghostDress(Group ghost) {
		return (Shape3D) ghost.getChildren().get(0);
	}

	public Group ghostEyes(Group ghost) {
		return (Group) ghost.getChildren().get(1);
	}

	public Shape3D ghostEyePupils(Group ghost) {
		return (Shape3D) ghostEyes(ghost).getChildren().get(0);
	}

	public Shape3D ghostEyeBalls(Group ghost) {
		return (Shape3D) ghostEyes(ghost).getChildren().get(1);
	}
}