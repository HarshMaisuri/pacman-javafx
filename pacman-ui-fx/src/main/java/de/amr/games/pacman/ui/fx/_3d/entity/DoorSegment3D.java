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
import static java.util.Objects.requireNonNull;

import de.amr.games.pacman.lib.math.Vector2i;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Single segment of a ghosthouse door.
 * 
 * @author Armin Reichert
 */
public class DoorSegment3D {

	public final DoubleProperty doorHeightPy = new SimpleDoubleProperty(this, "doorHeight", HTS);

	private final Box shape = new Box();

	public DoorSegment3D(Vector2i tile, Color color) {
		requireNonNull(tile);
		requireNonNull(color);

		shape.setWidth(TS - 1.0);
		shape.setHeight(1.0); // thickness (y-direction)
		shape.depthProperty().bind(doorHeightPy.add(2.0)); // height (z-direction)
		shape.setMaterial(new PhongMaterial(color));
		shape.setTranslateX((double) tile.x() * TS + HTS);
		shape.setTranslateY((double) tile.y() * TS + HTS);
		shape.translateZProperty().bind(doorHeightPy.divide(-2.0).subtract(0.5));
	}

	public Box getRoot() {
		return shape;
	}

	public void setOpen(boolean open) {
		shape.setVisible(!open); // TODO animate?
	}
}