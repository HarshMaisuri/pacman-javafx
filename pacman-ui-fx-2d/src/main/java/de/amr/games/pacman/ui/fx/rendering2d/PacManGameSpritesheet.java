/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class PacManGameSpritesheet extends GameSpritesheet {

	public static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	public PacManGameSpritesheet(Image source, int raster) {
		super(source, raster);
	}

	@Override
	public Rectangle2D[] ghostNumberSprites() {
		return tilesRightOf(0, 8, 4);
	}

	@Override
	public Rectangle2D bonusSymbolSprite(int symbol) {
		return tile(2 + symbol, 3);
	}

	@Override
	public Rectangle2D bonusValueSprite(int symbol) {
		if (symbol <= 3) {
			return tile(symbol, 9);
		}
		if (symbol == 4) {
			var region = tiles(4, 9, 2, 1);
			return region(region.getMinX(), region.getMinY(), region.getWidth() - 13, region.getHeight()); // WTF
		}
		return tiles(3, 5 + symbol, 3, 1);
	}

	public Rectangle2D ghostFacingRight(int ghostID) {
		return tile(2 * DIR_ORDER.index(Direction.RIGHT), 4 + ghostID);
	}

	@Override
	public Rectangle2D livesCounterSprite() {
		return region(129, 16, 15, 15); // WTF
	}

	@Override
	public Rectangle2D[] pacMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		var wide = tile(0, d);
		var middle = region(16, d * 16, 16, 16); // WTF
		var closed = tile(2, 0);
		return new Rectangle2D[] { closed, closed, middle, middle, wide, wide, middle, middle };
	}

	@Override
	public Rectangle2D[] pacDyingSprites() {
		return tilesRightOf(3, 0, 11);
	}

	@Override
	public Rectangle2D[] ghostNormalSprites(byte ghostID, Direction dir) {
		int d = DIR_ORDER.index(dir);
		return tilesRightOf(2 * d, 4 + ghostID, 2);
	}

	@Override
	public Rectangle2D[] ghostFrightenedSprites() {
		return new Rectangle2D[] { tile(8, 4), tile(9, 4) };
	}

	@Override
	public Rectangle2D[] ghostFlashingSprites() {
		return tilesRightOf(8, 4, 4);
	}

	@Override
	public Rectangle2D[] ghostEyesSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		return new Rectangle2D[] { tile(8 + d, 5) };
	}

	// Pac-Man specific:

	public Rectangle2D[] bigPacManSprites() {
		return new Rectangle2D[] { region(31, 15, 32, 34), region(63, 15, 32, 34), region(95, 15, 34, 34) };
	}

	public Rectangle2D[] blinkyDamagedSprites() {
		return new Rectangle2D[] { tile(8, 7), tile(9, 7) };
	}

	public Rectangle2D[] blinkyStretchedSprites() {
		return tilesRightOf(8, 6, 5);
	}

	public Rectangle2D[] blinkyPatchedSprites() {
		return tilesRightOf(10, 7, 2);
	}

	public Rectangle2D[] blinkyNakedSprites() {
		return new Rectangle2D[] { tiles(8, 8, 2, 1), tiles(10, 8, 2, 1) };
	}
}