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
package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission3Controller;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Pac2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.JuniorBag2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Stork2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacMansHusbandAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends GameScene2D {

	private final Intermission3Controller sceneController;
	private final Intermission3Controller.Context context;

	private LevelCounter2D levelCounter2D;
	private Pac2D msPacMan2D;
	private Pac2D msPacMansHusband2D;
	private Flap2D flap2D;
	private Stork2D stork2D;
	private JuniorBag2D bag2D;

	public MsPacMan_IntermissionScene3(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sceneController = new Intermission3Controller(gameController);
		sceneController.playIntermissionSound = () -> SoundManager.get().play(GameSound.INTERMISSION_3);
		sceneController.playFlapAnimation = () -> flap2D.animation.restart();
		context = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(Intermission3Controller.State.FLAP);
		levelCounter2D = new LevelCounter2D(game, unscaledSize.x - t(3), unscaledSize.y - t(2));
		msPacMan2D = new Pac2D(context.msPacMan, new PacAnimationSet(r2D));
		msPacMansHusband2D = new Pac2D(context.pacMan, new MsPacMansHusbandAnimations(Spritesheet_MsPacMan.get()));
		flap2D = new Flap2D(context.flap);
		stork2D = new Stork2D(context.stork);
		bag2D = new JuniorBag2D(context.bag);
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		levelCounter2D.render(g, r2D);
		flap2D.render(g, r2D);
		msPacMan2D.render(g, r2D);
		msPacMansHusband2D.render(g, r2D);
		stork2D.render(g, r2D);
		bag2D.render(g, r2D);
	}
}