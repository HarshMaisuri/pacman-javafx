package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.scene.Group;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends GameScene {

	enum Phase {

		ANIMATION;

		final CountdownTimer timer = new CountdownTimer();
	}

	private static final MsPacMan_SceneRendering rendering = PacManGameFXUI.MS_PACMAN_RENDERING;
	private static final SoundManager sounds = PacManGameFXUI.MS_PACMAN_SOUNDS;
	private static final int upperY = t(12), lowerY = t(24), middleY = t(18);

	private Phase phase;

	private Flap flap;
	private Pac pacMan, msPac;

	public MsPacMan_IntermissionScene2(Group root, double width, double height, double scaling) {
		super(root, width, height, scaling);
	}

	private void enter(Phase newPhase, long ticks) {
		phase = newPhase;
		phase.timer.setDuration(ticks);
	}

	@Override
	public void start() {
		flap = new Flap();
		flap.setPosition(t(3), t(10));
		flap.sceneNumber = 2;
		flap.sceneTitle = "THE CHASE";

		pacMan = new Pac("Pac-Man", Direction.RIGHT);
		msPac = new Pac("Ms. Pac-Man", Direction.RIGHT);
		enter(Phase.ANIMATION, Long.MAX_VALUE);
	}

	@Override
	public void update() {
		switch (phase) {
		case ANIMATION:
			if (phase.timer.running() == 0) {
				sounds.play(PacManGameSound.INTERMISSION_2);
				flap.visible = true;
				flap.animation.restart();
			}
			if (phase.timer.running() == clock.sec(2)) {
				flap.visible = false;
			}
			if (phase.timer.running() == clock.sec(4.5)) {
				pacMan.visible = true;
				pacMan.setPosition(-t(2), upperY);
				msPac.visible = true;
				msPac.setPosition(-t(8), upperY);
				pacMan.dir = msPac.dir = Direction.RIGHT;
				pacMan.speed = msPac.speed = 2;
				rendering.pacManMunching().values().forEach(Animation::restart);
				rendering.pacMunching(msPac).forEach(Animation::restart);
			}
			if (phase.timer.running() == clock.sec(9)) {
				msPac.setPosition(t(30), lowerY);
				msPac.visible = true;
				pacMan.setPosition(t(36), lowerY);
				msPac.dir = pacMan.dir = Direction.LEFT;
				msPac.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(13.5)) {
				msPac.setPosition(t(-8), middleY);
				pacMan.setPosition(t(-2), middleY);
				msPac.dir = pacMan.dir = Direction.RIGHT;
				msPac.speed = pacMan.speed = 2;
			}
			if (phase.timer.running() == clock.sec(18)) {
				msPac.setPosition(t(30), upperY);
				pacMan.setPosition(t(42), upperY);
				msPac.dir = pacMan.dir = Direction.LEFT;
				msPac.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(19)) {
				msPac.setPosition(t(-14), lowerY);
				pacMan.setPosition(t(-2), lowerY);
				msPac.dir = pacMan.dir = Direction.RIGHT;
				msPac.speed = pacMan.speed = 4;
			}
			if (phase.timer.running() == clock.sec(24)) {
				game.state.timer.setDuration(0);
			}
			break;
		default:
			break;
		}
		pacMan.move();
		msPac.move();
		phase.timer.run();
	}

	@Override
	public void render() {
		clear();
		flap.draw(g);
		rendering.drawMrPacMan(g, pacMan);
		rendering.drawPac(g, msPac);
	}
}