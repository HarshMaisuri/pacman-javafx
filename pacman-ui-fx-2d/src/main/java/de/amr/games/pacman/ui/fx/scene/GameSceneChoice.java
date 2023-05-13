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
package de.amr.games.pacman.ui.fx.scene;

/**
 * @author Armin Reichert
 */
public class GameSceneChoice {

	private GameScene scene2D;
	private GameScene scene3D;

	public GameSceneChoice(GameScene scene2D, GameScene scene3D) {
		this.scene2D = scene2D;
		this.scene3D = scene3D;
	}

	public GameSceneChoice(GameScene scene2D) {
		this.scene2D = scene2D;
	}

	public GameScene scene2D() {
		return scene2D;
	}

	public GameScene scene3D() {
		return scene3D;
	}

	public void setScene2D(GameScene scene2d) {
		scene2D = scene2d;
	}

	public void setScene3D(GameScene scene3d) {
		scene3D = scene3d;
	}

	public boolean includes(GameScene gameScene) {
		return gameScene == scene2D || gameScene == scene3D;
	}
}