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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.world.World.t;
import static de.amr.games.pacman.ui.fx.util.U.pauseSec;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.PacAnimationKey;
import de.amr.games.pacman.model.mspacman.MovingBonus;
import de.amr.games.pacman.model.pacman.StaticBonus;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LivesCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.World2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.BonusAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.PlaySceneSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.SequentialTransition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/**
 * 2D scene displaying the maze and the game play.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	private final GuysInfo guysInfo = new GuysInfo(this);
	private World2D world2D;
	private LivesCounter2D livesCounter2D;
	private LevelCounter2D levelCounter2D;
	private final SingleGenericAnimation<Boolean> energizerPulse = SingleGenericAnimation.pulse(10);

	@Override
	public void init() {
		guysInfo.init(game);

		boolean hasCredit = gameController.credit() > 0;

		creditVisible = !hasCredit;

		livesCounter2D = new LivesCounter2D(game);
		livesCounter2D.visible = hasCredit;

		levelCounter2D = new LevelCounter2D(game.levelCounter, r2D);
		levelCounter2D.visible = hasCredit;

		world2D = new World2D(game, r2D);

		game.pac.setAnimations(new PacAnimations(r2D));
		for (var ghost : game.ghosts) {
			ghost.setAnimations(new GhostAnimations(ghost.id, r2D));
		}
		game.bonus().setAnimations(new BonusAnimations(r2D));
		game.bonus().setInactive();
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && gameController.credit() == 0) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.L)) {
			Actions.addLives(3);
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	protected void doUpdate() {
		PlaySceneSounds.update(gameController.state());
		if (Env.$debugUI.get()) {
			guysInfo.update();
		}
	}

	@Override
	public void end() {
		log("Scene '%s' ended", getClass().getName());
	}

	@Override
	public void doRender(GraphicsContext g) {
		r2D.drawScore(g, game.scores.gameScore);
		r2D.drawScore(g, game.scores.highScore);
		livesCounter2D.render(g, r2D);
		levelCounter2D.render(g, r2D);
		r2D.drawCredit(g, gameController.credit(), creditVisible);
		world2D.render(g, r2D, !energizerPulse.animate());
		drawGameStateMessage(g);
		if (game.bonus() instanceof MovingBonus) {
			r2D.drawMovingBonus(g, (MovingBonus) game.bonus());
		} else {
			r2D.drawStaticBonus(g, (StaticBonus) game.bonus());
		}
		r2D.drawPac(g, game.pac);
		game.ghosts().forEach(ghost -> r2D.drawGhost(g, ghost));
	}

	private void drawGameStateMessage(GraphicsContext g) {
		if (gameController.state() == GameState.GAME_OVER || gameController.credit() == 0) {
			g.setFont(r2D.getArcadeFont());
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (gameController.state() == GameState.READY) {
			g.setFont(r2D.getArcadeFont());
			g.setFill(Color.YELLOW);
			g.fillText("READY!", t(11), t(21));
		}
	}

	public void onSwitchFrom3DScene() {
		game.pac.animations().get().getByKey(PacAnimationKey.ANIM_MUNCHING).restart();
		for (var ghost : game.ghosts) {
			ghost.animations().get().restart();
		}
		letEnergizersBlink(true);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {
		case READY -> {
			energizerPulse.reset();
		}
		case HUNTING -> {
			letEnergizersBlink(true);
		}
		case PACMAN_DYING -> {
			new SequentialTransition( //
					pauseSec(1, () -> game.ghosts().forEach(Ghost::hide)), //
					pauseSec(1, () -> {
						SoundManager.get().play(GameSound.PACMAN_DEATH);
						game.pac.animations().get().selectedAnimation().run();
					}), //
					pauseSec(2, () -> game.pac.hide()), //
					pauseSec(1, () -> gameController.state().timer().expire()) // exit game state
			).play();
		}
		case LEVEL_COMPLETE -> {
			gameController.state().timer().setIndefinite();
			game.pac.animations().get().reset();
			// Energizers could still remain if "next level" cheat has been used!
			energizerPulse.reset();
			new SequentialTransition( //
					pauseSec(1, () -> world2D.startFlashing(game.level.numFlashes)), //
					pauseSec(2, () -> gameController.state().timer().expire()) //
			).play();
		}
		case LEVEL_STARTING -> {
			// TODO check this
			gameController.state().timer().setSeconds(1);
			gameController.state().timer().start();
		}
		case GAME_OVER -> {
			energizerPulse.reset(); // TODO check with MAME
		}
		default -> {
			log("PlayScene entered game state %s", e.newGameState);
		}
		}
	}

	private void letEnergizersBlink(boolean blink) {
		if (blink) {
			energizerPulse.restart();
		} else {
			energizerPulse.stop();
			energizerPulse.reset();
		}
	}

}