/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.v2i;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private Animated stretchedDressAnimation;
	private Animated damagedAnimation;

	@Override
	protected PacManGameRenderer r() {
		return (PacManGameRenderer) super.r();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.placeAtTile(v2i(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.15f);
		pac.show();

		var pacAnimations = r().createPacAnimations(pac);
		pacAnimations.selectAndRestart(GameModel.AK_PAC_MUNCHING);
		pac.setAnimations(pacAnimations);

		stretchedDressAnimation = r().createBlinkyStretchedAnimation();

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.placeAtTile(v2i(28, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(0);
		blinky.hide();

		var blinkyAnimations = r().createGhostAnimations(blinky);
		damagedAnimation = r().createBlinkyDamagedAnimation();
		blinkyAnimations.put(GameModel.AK_BLINKY_DAMAGED, damagedAnimation);
		blinkyAnimations.selectAndRestart(GameModel.AK_GHOST_COLOR);
		blinky.setAnimations(blinkyAnimations);
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_2);
			}
			return;
		}

		if (context.state().timer().hasExpired()) {
			return;
		}

		switch (++frame) {

		case 110: {
			blinky.setPixelSpeed(1.25f);
			blinky.show();
		}
			break;

		case 196: {
			blinky.setPixelSpeed(0.17f);
			stretchedDressAnimation.setFrameIndex(1);
		}
			break;

		case 226: {
			stretchedDressAnimation.setFrameIndex(2);
		}
			break;

		case 248: {
			blinky.setPixelSpeed(0);
			blinky.animations().ifPresent(animations -> animations.selectedAnimation().get().stop());
			stretchedDressAnimation.setFrameIndex(3);
		}
			break;

		case 328: {
			stretchedDressAnimation.setFrameIndex(4);
		}
			break;

		case 329: {
			blinky.animations().ifPresent(animations -> animations.select(GameModel.AK_BLINKY_DAMAGED));
			damagedAnimation.setFrameIndex(0);
		}
			break;

		case 389: {
			damagedAnimation.setFrameIndex(1);
		}
			break;

		case 508: {
			stretchedDressAnimation = null;
		}
			break;

		case 509: {
			context.state().timer().expire();
		}
			break;

		default: {
			pac.move();
			pac.animate();
			blinky.move();
			blinky.animate();
		}
			break;

		}
	}

	@Override
	public void drawSceneContent() {
		if (stretchedDressAnimation != null) {
			r().drawSprite(g, (Rectangle2D) stretchedDressAnimation.frame(), t(14), t(19) + 3.0);
		}
		drawGhostSprite(blinky);
		drawPacSprite(pac);
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}

	@Override
	protected void drawSceneInfo() {
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, ArcadeTheme.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}