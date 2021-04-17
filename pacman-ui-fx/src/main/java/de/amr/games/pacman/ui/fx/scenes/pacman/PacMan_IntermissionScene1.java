package de.amr.games.pacman.ui.fx.scenes.pacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.pacman.BigPacMan2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Impl;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller.Phase;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge
 * Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends AbstractGameScene2D<Rendering2D_PacMan> {

	private class SceneController extends PacMan_IntermissionScene1_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_1, 2);
		}
	}

	private SceneController sceneController;
	private Player2D<Rendering2D_PacMan> pacMan2D;
	private Ghost2D<Rendering2D_PacMan> blinky2D;
	private BigPacMan2D bigPacMan2D;

	public PacMan_IntermissionScene1() {
		super(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, Rendering2D_Impl.RENDERING_PACMAN, PacManScenes.SOUNDS);
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController);
		sceneController.init();
		pacMan2D = new Player2D<>(sceneController.pac, rendering);
		blinky2D = new Ghost2D<>(sceneController.blinky, rendering);
		bigPacMan2D = new BigPacMan2D(sceneController.pac, Rendering2D_Impl.RENDERING_PACMAN);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		blinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		blinky2D.getFrightenedAnimation().restart();
		bigPacMan2D.getMunchingAnimation().restart();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render() {
		renderLevelCounter(new V2i(25, 34));
		blinky2D.render(gc);
		if (sceneController.phase == Phase.BLINKY_CHASING_PACMAN) {
			pacMan2D.render(gc);
		} else {
			bigPacMan2D.render(gc);
		}
	}
}