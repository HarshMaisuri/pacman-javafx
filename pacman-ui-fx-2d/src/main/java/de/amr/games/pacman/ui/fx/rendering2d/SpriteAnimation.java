/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.model.GameModel;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	public static class Builder {

		private SpriteAnimation animation = new SpriteAnimation();
		private int frameDurationTicks = 1;
		private boolean loop = false;

		public Builder frameDurationTicks(int ticks) {
			this.frameDurationTicks = ticks;
			return this;
		}

		public Builder loop() {
			loop = true;
			return this;
		}

		public Builder sprites(Rectangle2D... sprites) {
			animation.sprites = sprites;
			return this;
		}

		public SpriteAnimation build() {
			animation.transition = new Transition() {
				{
					setCycleDuration(Duration.seconds(1.0 / GameModel.FPS * frameDurationTicks));
					setCycleCount(loop ? Animation.INDEFINITE : animation.sprites.length);
					setInterpolator(Interpolator.LINEAR);
				}

				@Override
				protected void interpolate(double frac) {
					if (frac == 1.0) {
						animation.nextFrame();
					}
				}
			};
			return animation;
		}
	}

	private Rectangle2D[] sprites = new Rectangle2D[0];
	private Transition transition;
	private int frame;

	public void reset() {
		transition.stop();
		transition.jumpTo(Duration.ZERO);
		frame = 0;
	}

	public void start() {
		transition.play();
	}

	public void stop() {
		transition.stop();
	}

	public boolean isRunning() {
		return transition.getStatus() == Status.RUNNING;
	}

	public void setDelay(Duration delay) {
		transition.setDelay(delay);
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public Rectangle2D frame() {
		return sprites[frame];
	}

	public void nextFrame() {
		frame++;
		if (frame == sprites.length) {
			frame = transition.getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
		}
	}
}