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
package de.amr.games.pacman.ui.fx._3d.scene.cams;

import static de.amr.games.pacman.lib.U.lerp;

import javafx.scene.Node;
import javafx.scene.transform.Rotate;

/**
 * @author Armin Reichert
 */
public class CamFollowingPlayer extends GameSceneCamera {

	private double speed = 0.03;

	@Override
	public String toString() {
		return "Following Player";
	}

	@Override
	public boolean isManuallyConfigurable() {
		return false;
	}

	@Override
	public void reset() {
		setNearClip(0.1);
		setFarClip(10000.0);
		setRotationAxis(Rotate.X_AXIS);
		setRotate(60);
		setTranslateZ(-160);
	}

	@Override
	public void update(Node target) {
		setTranslateX(lerp(getTranslateX(), target.getTranslateX() - 100, speed));
		setTranslateY(lerp(getTranslateY(), target.getTranslateY() + 60, speed));
	}
}