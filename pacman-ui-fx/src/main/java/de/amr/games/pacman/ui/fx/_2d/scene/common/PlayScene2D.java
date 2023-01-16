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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	@Override
	public void update() {
		setCreditVisible(!ctx.hasCredit() || ctx.state() == GameState.GAME_OVER);
	}

	@Override
	public void draw() {
		var game = ctx.game();
		var r = ctx.r2D();
		ctx.level().ifPresent(level -> {
			drawMaze(r, level);
			r.drawBonus(g, level.bonus());
			boolean showGameOverText = ctx.state() == GameState.GAME_OVER || !ctx.hasCredit();
			if (showGameOverText) {
				r.drawGameOverMessage(g);
			} else if (ctx.state() == GameState.READY) {
				r.drawGameReadyMessage(g);
			}
			r.drawPac(g, level.pac());
			r.drawGhost(g, level.ghost(Ghost.ID_ORANGE_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_CYAN_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_PINK_GHOST));
			r.drawGhost(g, level.ghost(Ghost.ID_RED_GHOST));
			if (!isCreditVisible()) {
				int lives = game.isOneLessLifeDisplayed() ? game.lives() - 1 : game.lives();
				r.drawLivesCounter(g, lives);
			}
			r.drawLevelCounter(g, game.levelCounter());
		});
	}

	private void drawMaze(Rendering2D r, GameLevel level) {
		int mazeX = 0;
		int mazeY = 3 * TS;
		boolean energizersHidden = !level.energizerPulse().frame();
		if (level.world() instanceof ArcadeWorld arcadeWorld) {
			var flashing = arcadeWorld.flashingAnimation();
			if (flashing.isPresent() && flashing.get().isRunning()) {
				boolean flash = (boolean) flashing.get().frame();
				r.drawEmptyMaze(g, mazeX, mazeY, level.game().mazeNumber(level.number()), flash);
			} else {
				r.drawFilledMaze(g, mazeX, mazeY, level.game().mazeNumber(level.number()), level.world(), energizersHidden);
			}
		} else {
			r.drawFilledMaze(g, mazeX, mazeY, level.game().mazeNumber(level.number()), level.world(), false);
		}
	}

	@Override
	protected void drawDebugInfo(GameLevel level) {
		super.drawDebugInfo(level);
		if (level.world() instanceof ArcadeWorld arcadeWorld) {
			g.setFill(Color.RED);
			arcadeWorld.upwardBlockedTiles().forEach(tile -> g.fillRect(tile.x() * TS, tile.y() * TS, TS, 1));
		}
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			if (!ctx.hasCredit()) { // credit can only be added in attract mode
				Actions.addCredit();
			}
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.L)) {
			Actions.cheatAddLives(3);
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void onSwitchFrom3D() {
		ctx.level().ifPresent(level -> {
			level.pac().animations().ifPresent(EntityAnimationMap::ensureRunning);
			level.ghosts().map(Ghost::animations).forEach(anim -> anim.ifPresent(EntityAnimationMap::ensureRunning));
		});
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		ctx.sounds().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		ctx.sounds().play(GameSound.EXTRA_LIFE);
	}
}