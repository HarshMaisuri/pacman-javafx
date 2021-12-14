/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.controller.pacman.IntroController.TOP_Y;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.pacman.IntroController;
import de.amr.games.pacman.controller.pacman.IntroController.GhostPortrait;
import de.amr.games.pacman.controller.pacman.IntroController.Phase;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.fx._2d.entity.common.GameScore2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.Scenes;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card
 * and hunts the ghost himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene extends AbstractGameScene2D {

	private IntroController sceneController;

	private GameScore2D score2D;
	private GameScore2D hiscore2D;
	private Player2D pacMan2D;
	private List<Ghost2D> ghosts2D;
	private List<Ghost2D> ghostsInGallery2D;

	public PacMan_IntroScene() {
		super(Scenes.PACMAN_RENDERING, Scenes.PACMAN_SOUNDS, 28, 36);
	}

	@Override
	public void init() {
		super.init();
		sceneController = new IntroController(gameController);
		sceneController.init();

		score2D = new GameScore2D(rendering);
		score2D.title = "SCORE";
		score2D.x = t(1);
		score2D.y = t(1);
		score2D.levelSupplier = () -> game().levelNumber;
		score2D.pointsSupplier = () -> game().score;
		score2D.showPoints = false;

		hiscore2D = new GameScore2D(rendering);
		hiscore2D.title = "HIGH SCORE";
		hiscore2D.x = t(16);
		hiscore2D.y = t(1);
		hiscore2D.levelSupplier = () -> game().hiscoreLevel;
		hiscore2D.pointsSupplier = () -> game().hiscorePoints;

		pacMan2D = new Player2D(sceneController.pac, rendering);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);

		ghosts2D = Stream.of(sceneController.ghosts).map(ghost -> {
			Ghost2D ghost2D = new Ghost2D(ghost, rendering);
			ghost2D.kickingAnimations.values().forEach(TimedSequence::restart);
			ghost2D.frightenedAnimation.restart();
			return ghost2D;
		}).collect(Collectors.toList());

		ghostsInGallery2D = new ArrayList<>();
		for (int i = 0; i < 4; ++i) {
			Ghost2D ghost2D = new Ghost2D(sceneController.gallery[i].ghost, rendering);
			ghostsInGallery2D.add(ghost2D);
		}
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender() {
		score2D.render(gc);
		hiscore2D.render(gc);
		drawGallery();
		if (sceneController.phase == Phase.CHASING_PAC) {
			if (sceneController.blinking.animate()) {
				gc.setFill(Color.PINK);
				gc.fillOval(t(2), sceneController.pac.position().y, TS, TS);
			}
		}
		ghosts2D.forEach(ghost2D -> ghost2D.render(gc));
		pacMan2D.render(gc);
		if (sceneController.phase.ordinal() >= Phase.CHASING_GHOSTS.ordinal()) {
			drawPointsAnimation(11, 26);
		}
		if (sceneController.phase == Phase.READY_TO_PLAY) {
			drawPressKeyToStart(32);
		}
	}

	private void drawGallery() {
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("CHARACTER", t(6), TOP_Y);
		gc.fillText("/", t(16), TOP_Y);
		gc.fillText("NICKNAME", t(18), TOP_Y);
		for (int i = 0; i < 4; ++i) {
			GhostPortrait portrait = sceneController.gallery[i];
			if (portrait.ghost.isVisible()) {
				int y = TOP_Y + t(2 + 3 * i);
				ghostsInGallery2D.get(i).render(gc);
				gc.setFill(getGhostColor(i));
				gc.setFont(rendering.getScoreFont());
				if (portrait.characterVisible) {
					gc.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					gc.fillText("\"" + portrait.ghost.name + "\"", t(18), y + 8);
				}
			}
		}
	}

	private Color getGhostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	private void drawPressKeyToStart(int yTile) {
		if (sceneController.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			gc.setFill(Color.ORANGE);
			gc.setFont(rendering.getScoreFont());
			gc.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPointsAnimation(int tileX, int tileY) {
		if (sceneController.blinking.frame()) {
			gc.setFill(Color.PINK);
			gc.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
			gc.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("10", t(tileX + 2), t(tileY));
		gc.fillText("50", t(tileX + 2), t(tileY + 2));
		gc.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		gc.fillText("PTS", t(tileX + 5), t(tileY));
		gc.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}