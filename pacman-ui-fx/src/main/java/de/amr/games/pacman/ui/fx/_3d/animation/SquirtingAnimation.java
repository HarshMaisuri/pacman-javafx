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

import static de.amr.games.pacman.lib.U.randomDouble;
import static de.amr.games.pacman.lib.U.randomInt;

import de.amr.games.pacman.model.common.world.World;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class SquirtingAnimation extends Transition {

	private static final Point3D GRAVITY = new Point3D(0, 0, 0.1);

	private static class Drop extends Sphere {

		public Drop(double radius, PhongMaterial material, double x, double y, double z) {
			super(radius);
			setMaterial(material);
			setTranslateX(x);
			setTranslateY(y);
			setTranslateZ(z);
			setVisible(false);
		}
	}

	private final World world;
	private Drop[] drops;
	private Point3D[] veloc;

	public SquirtingAnimation(World world, Group parent, Node pellet) {
		this.world = world;
		createDrops(pellet, true);
		parent.getChildren().addAll(drops);
		setCycleDuration(Duration.seconds(2));
		setOnFinished(e -> parent.getChildren().removeAll(drops));
	}

	private void createDrops(Node pellet, boolean bigSquirt) {
		var numDrops = bigSquirt ? randomInt(20, 30) : randomInt(4, 8);
		var color = Color.gray(0.4, 0.25);
		var material = new PhongMaterial(color);
		drops = new Drop[numDrops];
		veloc = new Point3D[numDrops];
		for (int i = 0; i < numDrops; ++i) {
			var r = bigSquirt ? randomDouble(0.1, 1.0) : randomDouble(0.1, 0.5);
			drops[i] = new Drop(r, material, pellet.getTranslateX(), pellet.getTranslateY(), -4);
			veloc[i] = new Point3D(randomDouble(0.05, 0.25), randomDouble(0.05, 0.25), -randomDouble(1.0, 4.0));
		}
	}

	@Override
	protected void interpolate(double t) {
		for (int i = 0; i < drops.length; ++i) {
			var drop = drops[i];
			if (drop.getTranslateZ() >= -1.0 && world.insideBounds(drop.getTranslateX(), drop.getTranslateY())) {
				drop.setScaleZ(0.1);
				veloc[i] = Point3D.ZERO;
			} else {
				drop.setVisible(true);
				drop.setTranslateX(drop.getTranslateX() + veloc[i].getX());
				drop.setTranslateY(drop.getTranslateY() + veloc[i].getY());
				drop.setTranslateZ(drop.getTranslateZ() + veloc[i].getZ());
				veloc[i] = veloc[i].add(GRAVITY);
			}
		}
	}
}