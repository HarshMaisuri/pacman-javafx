/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx._2d.rendering.pacman;

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme.Palette;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManTestRenderer implements Rendering2D {

	@Override
	public Font screenFont(double size) {
		return Font.font(ArcadeTheme.SCREEN_FONT.getFamily(), size);
	}

	@Override
	public Color ghostColor(int ghostID) {
		return ArcadeTheme.GHOST_COLORS[ghostID].normalDress();
	}

	@Override
	public Color mazeFoodColor(int mazeNumber) {
		return Color.PINK;
	}

	@Override
	public Color mazeTopColor(int mazeNumber) {
		return Color.BLUE;
	}

	@Override
	public Color mazeSideColor(int mazeNumber) {
		return Color.BLUE;
	}

	@Override
	public Color ghostHouseDoorColor() {
		return Color.PINK;
	}

	@Override
	public EntityAnimationMap<AnimKeys> createPacAnimations(Pac pac) {
		var map = new EntityAnimationMap<AnimKeys>(2);
		map.put(AnimKeys.PAC_DYING, createPacDyingAnimation());
		map.put(AnimKeys.PAC_MUNCHING, createPacMunchingAnimation(pac));
		map.select(AnimKeys.PAC_MUNCHING);
		return map;
	}

	@Override
	public EntityAnimationByDirection createPacMunchingAnimation(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		var animation = new SingleEntityAnimation<>(0, 0, 90, 90, 120, 120, 90, 90);
		animation.setFrameDuration(1);
		animation.repeatForever();
		for (var dir : Direction.values()) {
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Integer> createPacDyingAnimation() {
		var animation = new SingleEntityAnimation<>(45, 60, 75, 90, 135, 180, 225, 270, 315, 360);
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public EntityAnimationMap<AnimKeys> createGhostAnimations(Ghost ghost) {
		var map = new EntityAnimationMap<AnimKeys>(5);
//		map.put(AnimKeys.GHOST_COLOR, createGhostColorAnimation(ghost));
//		map.put(AnimKeys.GHOST_BLUE, createGhostBlueAnimation());
//		map.put(AnimKeys.GHOST_EYES, createGhostEyesAnimation(ghost));
		map.put(AnimKeys.GHOST_FLASHING, createGhostFlashingAnimation());
//		map.put(AnimKeys.GHOST_VALUE, createGhostValueSpriteList());
		return map;
	}

	@Override
	public EntityAnimationByDirection createGhostColorAnimation(Ghost ghost) {
		return null;
	}

	@Override
	public SingleEntityAnimation<?> createGhostBlueAnimation() {
		return null;
	}

	@Override
	public SingleEntityAnimation<Boolean> createGhostFlashingAnimation() {
		return new Pulse(6, true);
	}

	@Override
	public EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		return null;
	}

	@Override
	public SingleEntityAnimation<Boolean> createMazeFlashingAnimation() {
		return new Pulse(10, true);
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		if (pac.isVisible()) {
			if (pac.isAnimationSelected(AnimKeys.PAC_MUNCHING)) {
				drawPacMunching(g, pac, pac.animation().get());
			} else if (pac.isAnimationSelected(AnimKeys.PAC_DYING)) {
				drawPacDying(g, pac, pac.animation().get());
			}
		}
	}

	private void drawPacMunching(GraphicsContext g, Pac pac, EntityAnimation munching) {
		int radius = 7;
		float x = pac.position().x() - radius / 2;
		float y = pac.position().y() - radius / 2;
		int openess = (int) munching.frame();
		int start = openess / 2;
		int fromAngle = switch (pac.moveDir()) {
		case RIGHT -> start;
		case UP -> start + 90;
		case LEFT -> start + 180;
		case DOWN -> start + 270;
		};
		g.setFill(Color.YELLOW);
		g.fillArc(x, y, 2 * radius, 2 * radius, fromAngle, 360 - openess, ArcType.ROUND);
	}

	private void drawPacDying(GraphicsContext g, Pac pac, EntityAnimation dying) {
		int radius = 7;
		float x = pac.position().x() - radius / 2;
		float y = pac.position().y() - radius / 2;
		int openess = (int) (dying.isRunning() ? dying.frame() : 360);
		int start = openess / 2;
		int fromAngle = start + 90;
		g.setFill(Color.YELLOW);
		g.fillArc(x, y, 2 * radius, 2 * radius, fromAngle, 360 - openess, ArcType.ROUND);
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost) {
		if (!ghost.isVisible()) {
			return;
		}
		switch (ghost.state()) {
		case EATEN -> {
			if (ghost.killedIndex() >= 0) {
				drawGhostBounty(g, ghost);
			} else {
				drawGhostEyes(g, ghost);
			}
		}
		case RETURNING_TO_HOUSE, ENTERING_HOUSE -> {
			drawGhostEyes(g, ghost);
		}
		case FRIGHTENED -> {
			var color = Color.BLUE;
			var flashing = ghost.animation();
			if (flashing.isPresent() && (boolean) flashing.get().frame()) {
				color = Color.WHITE;
			}
			drawGhostBody(g, ghost, color);
		}
		default -> {
			drawGhostBody(g, ghost, ghostColor(ghost.id()));
		}
		}
	}

	public void drawGhostBody(GraphicsContext g, Ghost ghost, Color color) {
		g.setFill(color);
		g.fillRoundRect(ghost.position().x() - 2, ghost.position().y() - 4, 12, 16, 6, 8);
	}

	public void drawGhostBounty(GraphicsContext g, Ghost ghost) {
		g.setStroke(Palette.CYAN);
		g.setFont(Font.font("Sans", 10));
		var text = switch (ghost.killedIndex()) {
		case 0 -> "200";
		case 1 -> "400";
		case 2 -> "800";
		case 3 -> "1600";
		default -> "???";
		};
		g.strokeText(text, ghost.position().x() - 4, ghost.position().y() + 6);
	}

	public void drawGhostEyes(GraphicsContext g, Ghost ghost) {
		var color = Color.WHITE;
		g.setStroke(color);
		g.strokeOval(ghost.position().x() - 3, ghost.position().y() + 2, 4, 4);
		g.strokeOval(ghost.position().x() + 3, ghost.position().y() + 2, 4, 4);
	}

	@Override
	public void drawGhostFacingRight(GraphicsContext g, int id, int x, int y) {
		var color = ghostColor(id);
		g.setFill(color);
		g.fillRect(x - 2, y - 4, 12, 16);
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		var x = bonus.entity().position().x();
		var y = bonus.entity().position().y() + 8;
		switch (bonus.state()) {
		case EDIBLE -> drawText(g, "Bonus", Color.YELLOW, ArcadeTheme.SCREEN_FONT, x - 20, y);
		case EATEN -> drawText(g, bonus.points() + "", Color.RED, ArcadeTheme.SCREEN_FONT, x - 8, y);
		default -> {
		}
		}
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, Optional<Integer> levelNumber, List<Byte> levelCounter) {
		levelNumber.ifPresent(number -> {
			drawText(g, "Level %s".formatted(number), Color.WHITE, ArcadeTheme.SCREEN_FONT, 18 * TS, 36 * TS - 2);
		});
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		int x = t(2);
		int y = t(ArcadeWorld.SIZE_TILES.y() - 1);
		int maxLives = 5;
		int size = 14;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			g.setFill(Color.YELLOW);
			g.fillOval(x + t(2 * i) - 7, y - 7, size, size);
		}
	}

	@Override
	public void drawFlashingMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean flash) {
		drawWalls(g, mazeNumber, world, flash);
	}

	@Override
	public void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizersHidden) {
		drawWalls(g, mazeNumber, world, false);
		drawFood(g, mazeNumber, world, energizersHidden);
	}

	private void drawWalls(GraphicsContext g, int mazeNumber, World world, boolean flash) {
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				var tile = new Vector2i(col, row);
				if (world.isWall(tile)) {
					g.setFill(flash ? Color.WHITE : Color.SADDLEBROWN);
					g.fillRect(tile.x() * TS, tile.y() * TS, TS, TS);
				}
			}
		}
	}

	private void drawFood(GraphicsContext g, int mazeNumber, World world, boolean energizersHidden) {
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				var tile = new Vector2i(col, row);
				if (world.containsFood(tile)) {
					g.setFill(mazeFoodColor(mazeNumber));
					if (world.isEnergizerTile(tile)) {
						if (!energizersHidden) {
							g.fillOval(tile.x() * TS, tile.y() * TS, TS, TS);
						}
					} else {
						g.fillRect(tile.x() * TS + 3, tile.y() * TS + 3, 2, 2);
					}
				}
			}
		}
	}
}