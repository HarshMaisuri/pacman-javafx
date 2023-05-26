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

package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.RND;

import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	// canvas where next scene content is created
	private final Canvas contentCanvas;
	private final GraphicsContext ctx;

	// current scene content
	private final WritableImage sceneImage;

	public BootScene() {
		contentCanvas = new Canvas(WIDTH_UNSCALED, HEIGHT_UNSCALED);
		ctx = contentCanvas.getGraphicsContext2D();
		sceneImage = new WritableImage(WIDTH_UNSCALED, HEIGHT_UNSCALED);
	}

	@Override
	public void init() {
		setSceneCanvasScaled(false);

		context.setScoreVisible(false);
		eraseContent();
		updateSceneImage();
	}

	@Override
	public void drawSceneContent() {
		g.drawImage(sceneImage, 0, 0);
	}

	@Override
	public void update() {
		var timer = context.state().timer();
		if (timer.betweenSeconds(1.0, 2.0) && timer.tick() % 4 == 0) {
			paintRandomHexCodes();
		} else if (timer.betweenSeconds(2.0, 3.5) && timer.tick() % 4 == 0) {
			paintRandomSprites();
		} else if (timer.atSecond(3.5)) {
			paintGrid();
		} else if (timer.atSecond(4.0)) {
			context.gameController().terminateCurrentState();
		}
	}

	private void eraseContent() {
		ctx.setFill(ArcadeTheme.BLACK);
		ctx.fillRect(0, 0, contentCanvas.getWidth(), contentCanvas.getHeight());
	}

	private void updateSceneImage() {
		contentCanvas.snapshot(null, sceneImage);
	}

	private void paintRandomHexCodes() {
		var theme = context.renderer().theme();
		var font8 = theme.font("font.arcade", 8);
		eraseContent();
		ctx.setFill(ArcadeTheme.PALE);
		ctx.setFont(font8);
		for (int row = 0; row < TILES_Y; ++row) {
			for (int col = 0; col < TILES_X; ++col) {
				var hexCode = Integer.toHexString(RND.nextInt(16));
				ctx.fillText(hexCode, t(col), t(row + 1));
			}
		}
		updateSceneImage();
	}

	private void paintRandomSprites() {
		var ss = context.renderer().spritesheet();
		eraseContent();
		for (int row = 0; row < TILES_Y / 2; ++row) {
			if (RND.nextInt(100) > 10) {
				var region1 = randomSquare(ss);
				var region2 = randomSquare(ss);
				var splitX = TILES_X / 8 + RND.nextInt(TILES_X / 4);
				for (int col = 0; col < TILES_X / 2; ++col) {
					var region = col < splitX ? region1 : region2;
					context.renderer().drawSprite(ctx, region, region.getWidth() * col, region.getHeight() * row);
				}
			}
		}
		updateSceneImage();
	}

	private Rectangle2D randomSquare(Spritesheet ss) {
		var source = ss.source();
		var raster = ss.raster();
		double x = RND.nextDouble() * (source.getWidth() - raster);
		double y = RND.nextDouble() * (source.getHeight() - raster);
		return new Rectangle2D(x, y, raster, raster);
	}

	private void paintGrid() {
		eraseContent();
		var cellSize = 16;
		var numRows = TILES_Y / 2;
		var numCols = TILES_X / 2;
		ctx.setStroke(ArcadeTheme.PALE);
		ctx.setLineWidth(2.0);
		for (int row = 0; row <= numRows; ++row) {
			ctx.strokeLine(0, row * cellSize, WIDTH_UNSCALED, row * cellSize);
		}
		for (int col = 0; col <= numCols; ++col) {
			ctx.strokeLine(col * cellSize, 0, col * cellSize, HEIGHT_UNSCALED);
		}
		updateSceneImage();
	}
}