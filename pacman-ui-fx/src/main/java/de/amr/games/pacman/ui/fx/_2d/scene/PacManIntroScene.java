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
package de.amr.games.pacman.ui.fx._2d.scene;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D.drawText;
import static de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D.drawTileStructure;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.PacManIntroController;
import de.amr.games.pacman.controller.pacman.PacManIntroData;
import de.amr.games.pacman.controller.pacman.PacManIntroState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.ArcadeTheme;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacManIntroScene extends GameScene2D {

	private static final String QUOTE = "\"";

	private PacManIntroController intro;

	public PacManIntroScene(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intro = new PacManIntroController(context().gameController());
		intro.changeState(PacManIntroState.START);

		intro.context().pacMan.setAnimations(context.rendering2D().createPacAnimations(intro.context().pacMan));
		Stream.of(intro.context().ghosts)
				.forEach(ghost -> ghost.setAnimations(context.rendering2D().createGhostAnimations(ghost)));
		intro.context().blinking.reset();
	}

	@Override
	public void update() {
		intro.update();
		context.setCreditVisible(intro.context().creditVisible);
	}

	@Override
	public void end() {
		Actions.stopVoiceMessage();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(Keys.ADD_CREDIT)) {
			Actions.addCredit();
		} else if (Keyboard.pressed(Keys.START_GAME)) {
			Actions.startGame();
		} else if (Keyboard.pressed(Keys.SELECT_VARIANT)) {
			Actions.selectNextGameVariant();
		} else if (Keyboard.pressed(Keys.PLAY_CUTSCENES)) {
			Actions.startCutscenesTest();
		}
	}

	@Override
	public void drawScene(GraphicsContext g) {
		var timer = intro.state().timer();
		drawGallery(g);
		switch (intro.state()) {
		case SHOWING_POINTS -> {
			drawPoints(g);
			if (timer.tick() > timer.secToTicks(1)) {
				drawBlinkingEnergizer(g);
				drawCopyright(g);
			}
		}
		case CHASING_PAC -> {
			drawPoints(g);
			drawBlinkingEnergizer(g);
			drawGuys(g, flutter(timer.tick()));
			drawCopyright(g);
		}
		case CHASING_GHOSTS -> {
			drawPoints(g);
			drawGuys(g, 0);
			drawCopyright(g);
		}
		case READY_TO_PLAY -> {
			drawPoints(g);
			drawGuys(g, 0);
			drawCopyright(g);
		}
		default -> { // nothing to do
		}
		}
		drawLevelCounter(g);
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		drawTileStructure(g, World.TILES_X, World.TILES_Y);
	}

	private void drawCopyright(GraphicsContext g) {
		var r = context.rendering2D();
		drawText(g, "\u00A9 1980 MIDWAY MFG.CO.", ArcadeTheme.PINK, r.screenFont(TS), TS * (4), TS * (32));
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery(GraphicsContext g) {
		var r = context.rendering2D();
		var col = PacManIntroData.LEFT_TILE;
		var font = r.screenFont(TS);
		if (intro.context().titleVisible) {
			drawText(g, "CHARACTER", ArcadeTheme.PALE, font, TS * (col + 3), TS * (6));
			drawText(g, "/", ArcadeTheme.PALE, font, TS * (col + 13), TS * (6));
			drawText(g, "NICKNAME", ArcadeTheme.PALE, font, TS * (col + 15), TS * (6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!intro.context().pictureVisible[id]) {
				continue;
			}
			int row = 7 + 3 * id;
			var color = r.ghostColors(id).dress();
			r.drawGhostFacingRight(g, id, TS * (col) + 4, TS * (row));
			if (intro.context().characterVisible[id]) {
				drawText(g, "-" + PacManIntroData.CHARACTERS[id], color, font, TS * (col + 3), TS * (row + 1));
			}
			if (intro.context().nicknameVisible[id]) {
				drawText(g, QUOTE + intro.context().ghosts[id].name() + QUOTE, color, font, TS * (col + 14), TS * (row + 1));
			}
		}
	}

	private void drawBlinkingEnergizer(GraphicsContext g) {
		if (Boolean.TRUE.equals(intro.context().blinking.frame())) {
			g.setFill(context.rendering2D().mazeColors(1).foodColor());
			g.fillOval(TS * (PacManIntroData.LEFT_TILE), TS * (20), TS, TS);
		}
	}

	private void drawGuys(GraphicsContext g, int offsetX) {
		var r = context.rendering2D();
		var pacMan = intro.context().pacMan;
		var ghosts = intro.context().ghosts;
		if (offsetX == 0) {
			for (var ghost : ghosts) {
				r.drawGhost(g, ghost);
			}
		} else {
			r.drawGhost(g, ghosts[0]);
			g.save();
			g.translate(offsetX, 0);
			r.drawGhost(g, ghosts[1]);
			r.drawGhost(g, ghosts[2]);
			g.restore();
			r.drawGhost(g, ghosts[3]);
		}
		r.drawPac(g, pacMan);
	}

	private void drawPoints(GraphicsContext g) {
		var r = context.rendering2D();
		int col = PacManIntroData.LEFT_TILE + 6;
		int row = 25;
		g.setFill(r.mazeColors(1).foodColor());
		g.fillRect(TS * (col) + 4, TS * (row - 1) + 4, 2, 2);
		if (Boolean.TRUE.equals(intro.context().blinking.frame())) {
			g.fillOval(TS * (col), TS * (row + 1), TS, TS);
		}
		g.setFill(ArcadeTheme.PALE);
		g.setFont(r.screenFont(TS));
		g.fillText("10", TS * (col + 2), TS * (row));
		g.fillText("50", TS * (col + 2), TS * (row + 2));
		g.setFont(r.screenFont(6));
		g.fillText("PTS", TS * (col + 5), TS * (row));
		g.fillText("PTS", TS * (col + 5), TS * (row + 2));
	}
}