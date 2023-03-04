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

import de.amr.games.pacman.controller.mspacman.MsPacManIntermission3;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import javafx.geometry.Rectangle2D;

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
public class MsPacManIntermissionScene3 extends GameScene2D {

	private MsPacManIntermission3 intermission;
	private SingleEntityAnimation<Rectangle2D> storkAnim;

	@Override
	public void init() {
		intermission = new MsPacManIntermission3(context.gameController());
		var ic = intermission.context();
		var r = (MsPacManGameRenderer) context.r2D();
		intermission.restart(MsPacManIntermission3.IntermissionState.FLAP);
		ic.clapperboard.setAnimation(r.createClapperboardAnimation());
		ic.msPacMan.setAnimations(r.createPacAnimations(ic.msPacMan));
		ic.pacMan.setAnimations(r.createPacAnimations(ic.pacMan));
		var munching = r.createPacManMunchingAnimationMap(ic.pacMan);
		ic.pacMan.animations().ifPresent(anims -> anims.put(GameModel.AK_PAC_MUNCHING, munching));
		storkAnim = r.createStorkFlyingAnimation();
		storkAnim.ensureRunning();
	}

	@Override
	public void update() {
		intermission.update();
	}

	@Override
	public void drawSceneContent() {
		var ic = intermission.context();
		var r = (MsPacManGameRenderer) context.r2D();
		r.drawClap(g, ic.clapperboard);
		r.drawPac(g, ic.msPacMan);
		r.drawPac(g, ic.pacMan);
		r.drawEntitySprite(g, ic.stork, storkAnim.animate());
		r.drawEntitySprite(g, ic.bag, ic.bagOpen ? r.juniorPacSprite() : r.blueBagSprite());
		r.drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}
}