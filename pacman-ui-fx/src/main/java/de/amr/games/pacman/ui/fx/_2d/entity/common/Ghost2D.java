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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import de.amr.games.pacman.lib.SpriteAnimationSet;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D {

	public final GameModel game;
	public final Ghost ghost;
	public final SpriteAnimationSet<Ghost, GhostAnimation, Rectangle2D> animations;

	public Ghost2D(Ghost ghost, GameModel game, SpriteAnimationSet<Ghost, GhostAnimation, Rectangle2D> animations) {
		this.game = game;
		this.ghost = ghost;
		this.animations = animations;
		animations.selectAnimation(GhostAnimation.COLOR);
	}

	public void updateAnimation(boolean frightened, boolean recovering) {
		GhostAnimation key = switch (ghost.state) {
		case DEAD -> ghost.bounty == 0 ? GhostAnimation.EYES : GhostAnimation.NUMBER;
		case ENTERING_HOUSE -> GhostAnimation.EYES;
		case FRIGHTENED -> recovering ? GhostAnimation.FLASHING : GhostAnimation.BLUE;
		case HUNTING_PAC, LEAVING_HOUSE -> GhostAnimation.COLOR;
		case LOCKED -> recovering ? GhostAnimation.FLASHING : frightened ? GhostAnimation.BLUE : GhostAnimation.COLOR;
		};
		animations.selectAnimation(key);
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		if (ghost.visible) {
			r2D.drawEntity(g, ghost, animations.currentSprite(ghost));
		}
	}
}