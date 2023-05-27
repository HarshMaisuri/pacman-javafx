/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.v2i;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpritesheet;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManCutscene3 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;

	@Override
	protected PacManGameSpritesheet r() {
		return (PacManGameSpritesheet) super.r();
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
		pac.setPixelSpeed(1.25f);
		pac.show();

		pac.setAnimations(r().createPacAnimations(pac));
		pac.selectAndRunAnimation(GameModel.AK_PAC_MUNCHING);

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.placeAtTile(v2i(35, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(1.25f);
		blinky.show();

		var blinkyAnimations = r().createGhostAnimations(blinky);
		blinkyAnimations.put(GameModel.AK_BLINKY_PATCHED, r().createBlinkyPatchedAnimation());
		blinkyAnimations.put(GameModel.AK_BLINKY_NAKED, r().createBlinkyNakedAnimation());
		blinky.setAnimations(blinkyAnimations);
		blinky.selectAndRunAnimation(GameModel.AK_BLINKY_PATCHED);
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_3);
			}
			return;
		}
		if (context.state().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
		case 400: {
			blinky.placeAtTile(v2i(-1, 20), 0, 0);
			blinky.setMoveAndWishDir(Direction.RIGHT);
			blinky.selectAndRunAnimation(GameModel.AK_BLINKY_NAKED);
		}
			break;

		case 700: {
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
		drawPacSprite(pac);
		drawGhostSprite(blinky);
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}

	@Override
	protected void drawSceneInfo() {
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, ArcadeTheme.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}