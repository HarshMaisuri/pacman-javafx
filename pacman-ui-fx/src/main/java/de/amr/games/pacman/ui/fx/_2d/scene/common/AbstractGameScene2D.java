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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.model.world.World.TS;
import static de.amr.games.pacman.model.world.World.t;
import static de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX.TILES_X;
import static de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX.TILES_Y;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.entity.common.GameScore2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Base class of all 2D scenes that get rendered inside the canvas provided by the UI.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene2D extends DefaultGameEventHandler implements GameScene {

	protected final PacManGameUI_JavaFX ui;
	protected final GameController gameController;
	protected final GraphicsContext gc;
	protected final Rendering2D r2D;
	protected final V2i unscaledSize;

	protected SubScene fxSubScene;
	protected GameModel game;
	protected GameScore2D score2D;
	protected GameScore2D highScore2D;

	public AbstractGameScene2D(PacManGameUI_JavaFX ui, Rendering2D r2D) {
		this.ui = ui;
		this.gameController = ui.gameController;
		this.r2D = r2D;
		unscaledSize = new V2i(TILES_X * TS, TILES_Y * TS);
		gc = ui.canvas.getGraphicsContext2D();
	}

	@Override
	public void createFXSubScene(Scene parentScene) {
		fxSubScene = new SubScene(new StackPane(ui.canvas), unscaledSize.x, unscaledSize.y);
		fxSubScene.widthProperty().bind(ui.canvas.widthProperty());
		fxSubScene.heightProperty().bind(ui.canvas.heightProperty());
	}

	@Override
	public SubScene getSubSceneFX() {
		return fxSubScene;
	}

	@Override
	public void init() {
		game = gameController.game;
		score2D = new GameScore2D("SCORE", t(1), t(1), game, false, r2D);
		highScore2D = new GameScore2D("HIGH SCORE", t(16), t(1), game, true, r2D);
	}

	@Override
	public final void update() {
		if (gameController != null) {
			doUpdate();
		}
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		doRender();
		if (Env.$tilesVisible.get()) {
			drawTileBorders();
		}
	}

	@Override
	public boolean is3D() {
		return false;
	}

	private void drawTileBorders() {
		gc.setStroke(Color.rgb(160, 160, 160, 0.5));
		gc.setLineWidth(1);
		for (int row = 0; row < 36; ++row) {
			line(0, t(row), t(28), t(row));
		}
		for (int col = 0; col < 28; ++col) {
			line(t(col), 0, t(col), t(36));
		}
	}

	// WTF
	private void line(double x1, double y1, double x2, double y2) {
		double offset = 0.5;
		gc.strokeLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	protected abstract void doUpdate();

	/**
	 * Renders the scene content. Subclasses override this method.
	 */
	protected abstract void doRender();

	/**
	 * Used in play scene and intermission scenes, so define it here.
	 */
	protected void drawLevelCounter() {
		V2i rightPosition = unscaledSize.minus(t(3), t(2));
		int firstLevelNumber = Math.max(1, game.levelNumber - 6);
		double x = rightPosition.x;
		for (int levelNumber = firstLevelNumber; levelNumber <= game.levelNumber; ++levelNumber, x -= t(2)) {
			r2D.renderSprite(gc, r2D.getSymbolSprites().get(game.levelSymbol(levelNumber)), x, rightPosition.y);
		}
	}
}