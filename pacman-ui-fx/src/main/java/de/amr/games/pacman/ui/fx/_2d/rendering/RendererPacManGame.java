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
package de.amr.games.pacman.ui.fx._2d.rendering;

import static de.amr.games.pacman.model.common.actors.Ghost.ID_PINK_GHOST;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Map;

import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.FixedEntityAnimation;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class RendererPacManGame extends RendererCommon {

	private static final Color MAZE_WALL_COLOR = Color.rgb(33, 33, 255);
	private static final Color FOOD_COLOR = Color.rgb(254, 189, 180);

	private static final Spritesheet SPRITESHEET = new Spritesheet(//
			Ufx.image("graphics/pacman/sprites.png"), 16, //
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	private static final Image MAZE_FULL = Ufx.image("graphics/pacman/maze_full.png");
	private static final Image MAZE_EMPTY = Ufx.image("graphics/pacman/maze_empty.png");
	private static final Image MAZE_EMPTY_INV = Ufx.colorsExchanged(MAZE_EMPTY, Map.of(MAZE_WALL_COLOR, Color.WHITE));

	public static final RendererPacManGame THE_ONE_AND_ONLY = new RendererPacManGame();

	private RendererPacManGame() {
	}

	@Override
	public Spritesheet spritesheet() {
		return SPRITESHEET;
	}

	@Override
	public Rectangle2D ghostSprite(int ghostID, Direction dir) {
		return spritesheet().tile(2 * spritesheet().dirIndex(dir), 4 + ghostID);
	}

	@Override
	public Rectangle2D bonusSymbolSprite(int symbol) {
		return spritesheet().tile(2 + symbol, 3);
	}

	@Override
	public Rectangle2D bonusValueSprite(int symbol) {
		if (symbol <= 3) {
			return spritesheet().tile(symbol, 9);
		}
		if (symbol == 4) {
			var region = spritesheet().tiles(4, 9, 2, 1);
			return new Rectangle2D(region.getMinX(), region.getMinY(), region.getWidth() - 13, region.getHeight()); // WTF
		}
		return spritesheet().tiles(3, 5 + symbol, 3, 1);
	}

	@Override
	public SingleEntityAnimation<Boolean> createMazeFlashingAnimation() {
		return new Pulse(10, true);
	}

	@Override
	public void drawEmptyMaze(GraphicsContext g, int x, int y, int mazeNumber, boolean flash) {
		g.drawImage(flash ? MAZE_EMPTY_INV : MAZE_EMPTY, x, y);
	}

	@Override
	public void drawFilledMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizersHidden) {
		g.drawImage(MAZE_FULL, x, y);
		world.tiles().filter(world::containsEatenFood).forEach(tile -> hideTileContent(g, tile));
		if (energizersHidden) {
			world.energizerTiles().forEach(tile -> hideTileContent(g, tile));
		}
	}

	@Override
	public void drawCopyright(GraphicsContext g, int tileY) {
		var text = "\u00A9 1980 MIDWAY MFG.CO.";
		g.setFont(arcadeFont(TS));
		g.setFill(ghostColor(ID_PINK_GHOST));
		g.fillText(text, t(4), t(tileY));
	}

	@Override
	public Color mazeFoodColor(int mazeNumber) {
		return FOOD_COLOR;
	}

	@Override
	public Color mazeTopColor(int mazeNumber) {
		return MAZE_WALL_COLOR.darker().darker();
	}

	@Override
	public Color ghostHouseDoorColor() {
		return Color.rgb(252, 181, 255);
	}

	@Override
	public Color mazeSideColor(int mazeNumber) {
		return Color.rgb(33, 33, 255);
	}

	@Override
	public Rectangle2D lifeSprite() {
		return spritesheet().tile(8, 1);
	}

	@Override
	public EntityAnimationByDirection createPacMunchingAnimation(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			var wide = spritesheet().tile(0, d);
			var middle = spritesheet().tile(1, d);
			var closed = spritesheet().tile(2, 0);
			var animation = new SingleEntityAnimation<>(closed, closed, middle, middle, wide, wide, middle, middle);
			animation.setFrameDuration(1);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createPacDyingAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet().tilesRightOf(3, 0, 11));
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostColorAnimation(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			var animation = new SingleEntityAnimation<>(spritesheet().tilesRightOf(2 * d, 4 + ghost.id(), 2));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet().tile(8, 4), spritesheet().tile(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet().tilesRightOf(8, 4, 4));
		animation.setFrameDuration(6);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			animationByDir.put(dir, new SingleEntityAnimation<>(spritesheet().tile(8 + d, 5)));
		}
		return animationByDir;
	}

	@Override
	public FixedEntityAnimation<Rectangle2D> createGhostValueList() {
		return new FixedEntityAnimation<>(spritesheet().tilesRightOf(0, 8, 4));
	}

	// Pac-Man specific:

	public SingleEntityAnimation<Rectangle2D> createBigPacManMunchingAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet().tiles(2, 1, 2, 2), spritesheet().tiles(4, 1, 2, 2),
				spritesheet().tiles(6, 1, 2, 2));
		animation.setFrameDuration(3);
		animation.repeatForever();
		return animation;
	}

	public FixedEntityAnimation<Rectangle2D> createBlinkyStretchedAnimation() {
		return new FixedEntityAnimation<>(spritesheet().tilesRightOf(8, 6, 5));
	}

	public FixedEntityAnimation<Rectangle2D> createBlinkyDamagedAnimation() {
		return new FixedEntityAnimation<>(spritesheet().tile(8, 7), spritesheet().tile(9, 7));
	}

	public SingleEntityAnimation<Rectangle2D> createBlinkyPatchedAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet().tile(10, 7), spritesheet().tile(11, 7));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}

	public SingleEntityAnimation<Rectangle2D> createBlinkyNakedAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet().tiles(8, 8, 2, 1), spritesheet().tiles(10, 8, 2, 1));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}
}