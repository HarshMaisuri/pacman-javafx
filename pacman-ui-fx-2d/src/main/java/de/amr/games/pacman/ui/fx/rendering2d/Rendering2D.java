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
package de.amr.games.pacman.ui.fx.rendering2d;

import static de.amr.games.pacman.lib.Globals.TS;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Common interface for all 2D renderers.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	AnimationMap createPacAnimations(Pac pac);

	AnimationMap createGhostAnimations(Ghost ghost);

	AnimationMap createWorldAnimations(World world);

	static void drawTileStructure(GraphicsContext g, int tilesX, int tilesY) {
		g.save();
		g.translate(0.5, 0.5);
		g.setStroke(ArcadeTheme.PALE);
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, TS * (row), tilesX * TS, TS * (row));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(TS * (col), 0, TS * (col), tilesY * TS);
		}
		g.restore();
	}

	static void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

	static void hideTileContent(GraphicsContext g, Vector2i tile) {
		g.setFill(ArcadeTheme.BLACK);
		g.fillRect(TS * (tile.x()), TS * (tile.y()), TS, TS);
	}

	static void drawScore(GraphicsContext g, Score score, String title, Font font, Color color, double x, double y) {
		drawText(g, title, color, font, x, y);
		var pointsText = "%02d".formatted(score.points());
		drawText(g, "%7s".formatted(pointsText), color, font, x, y + TS + 1);
		if (score.points() != 0) {
			drawText(g, "L%d".formatted(score.levelNumber()), color, font, x + TS * (8), y + TS + 1);
		}
	}

	void drawPac(GraphicsContext g, Pac pac);

	void drawGhost(GraphicsContext g, Ghost ghost);

	void drawGhostFacingRight(GraphicsContext g, int id, int x, int y);

	void drawBonus(GraphicsContext g, Bonus bonus);

	void drawLevelCounter(GraphicsContext g, Optional<Integer> levelNumber, List<Byte> levelSymbols);

	void drawLivesCounter(GraphicsContext g, int numLivesDisplayed);

	void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world);
}