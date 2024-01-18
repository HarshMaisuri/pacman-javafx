/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManPacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManSpriteSheet;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private SpriteAnimation blinkyNormal;
	private SpriteAnimation blinkyStretching;
	private SpriteAnimation blinkyDamaged;

	@Override
	public void init() {
		frame = -1;
		initialDelay = 120;
		setCreditVisible(!context.gameController().hasCredit());
		setScoreVisible(true);
		var ss = context.<PacManSpriteSheet>spriteSheet();
		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacManPacAnimations(pac, ss));
		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		var blinkyAnimations = new PacManGhostAnimations(blinky, ss);
		blinkyNormal = blinkyAnimations.byName(GhostAnimations.GHOST_NORMAL);
		blinkyStretching = blinkyAnimations.byName(GhostAnimations.BLINKY_STRETCHED);
		blinkyDamaged = blinkyAnimations.byName(GhostAnimations.BLINKY_DAMAGED);
		blinky.setAnimations(blinkyAnimations);
		blinky.setPixelSpeed(0);
		blinky.hide();
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				context.gameController().publishGameEvent(GameEventType.INTERMISSION_STARTED);
			}
			return;
		}

		if (context.gameState().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
			case 1 -> blinkyStretching.setFrameIndex(0); // Show nail
			case 25 -> {
				pac.placeAtTile(28, 20, 0, 0);
				pac.setMoveDir(Direction.LEFT);
				pac.setPixelSpeed(1.15f);
				pac.selectAnimation(PacAnimations.MUNCHING);
				pac.startAnimation();
				pac.show();
			}
			case 111 -> {
				blinky.placeAtTile(28, 20, -3, 0);
				blinky.setMoveAndWishDir(Direction.LEFT);
				blinky.setPixelSpeed(1.25f);
				blinky.selectAnimation(GhostAnimations.GHOST_NORMAL);
				blinky.startAnimation();
				blinky.show();
			}
			case 194 -> {
				blinky.setPixelSpeed(0.09f);
				blinkyNormal.setFrameTicks(32);
			}
			case 198, 226, 248 -> blinkyStretching.nextFrame(); // Stretched S-M-L
			case 328 -> {
				blinky.setPixelSpeed(0);
				blinkyStretching.nextFrame(); // Rapture
			}
			case 329 ->	blinky.selectAnimation(GhostAnimations.BLINKY_DAMAGED); // Eyes up
			case 389 ->	blinkyDamaged.nextFrame(); // Eyes right-down
			case 508 -> {
				blinky.setVisible(false);
				context.gameState().timer().expire();
			}
			default -> {}
		}

		blinky.move();
		pac.move();
	}

	@Override
	public void drawSceneContent() {
		drawSprite(blinkyStretching.currentSprite(), t(14), t(19) + 3);
		drawGhost(blinky);
		drawPac(pac);
		drawLevelCounter();
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(GameModel.TILES_X, GameModel.TILES_Y);
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}