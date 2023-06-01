/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class GhostSpriteAnimationsCommon implements GhostAnimations<SpriteAnimation> {

	protected final Ghost ghost;
	protected Spritesheet spritesheet;
	protected final Map<String, SpriteAnimation> animationsByName = new HashMap<>();

	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	protected GhostSpriteAnimationsCommon(Ghost ghost, Spritesheet sprites) {
		this.ghost = ghost;
		this.spritesheet = sprites;

		var normalAnimation = SpriteAnimation.builder() //
				.frameDurationTicks(8) //
				.loop() //
				.sprites(ghostNormalSprites(ghost.id(), Direction.LEFT)) //
				.build();

		var frightenedAnimation = SpriteAnimation.builder() //
				.frameDurationTicks(8) //
				.loop() //
				.sprites(ghostFrightenedSprites()) //
				.build();

		var flashingAnimation = SpriteAnimation.builder() //
				.frameDurationTicks(6) //
				.loop() //
				.sprites(ghostFlashingSprites()) //
				.build();

		var eyesAnimation = SpriteAnimation.builder().sprites(ghostEyesSprites(Direction.LEFT)).build();

		var numberAnimation = SpriteAnimation.builder() //
				.sprites(ghostNumberSprites()) //
				.build();

		animationsByName.put(GHOST_NORMAL, normalAnimation);
		animationsByName.put(GHOST_FRIGHTENED, frightenedAnimation);
		animationsByName.put(GHOST_FLASHING, flashingAnimation);
		animationsByName.put(GHOST_EYES, eyesAnimation);
		animationsByName.put(GHOST_NUMBER, numberAnimation);

		// TODO check this
		eyesAnimation.start();
		frightenedAnimation.start();
		flashingAnimation.start();
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	protected abstract Rectangle2D[] ghostNormalSprites(byte id, Direction dir);

	protected abstract Rectangle2D[] ghostFrightenedSprites();

	protected abstract Rectangle2D[] ghostFlashingSprites();

	protected abstract Rectangle2D[] ghostEyesSprites(Direction dir);

	protected abstract Rectangle2D[] ghostNumberSprites();

	@Override
	public String selectedAnimationName() {
		return currentAnimationName;
	}

	@Override
	public SpriteAnimation selectedAnimation() {
		return currentAnimation;
	}

	@Override
	public void select(String name, Object... args) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			currentAnimation = byName(name);
			if (currentAnimation != null) {
				if (currentAnimation == byName(GHOST_NUMBER)) {
					currentAnimation.setFrameIndex((Integer) args[0]);
				} else {
					currentAnimation.setFrameIndex(0);
				}
			}
		}
	}

	public SpriteAnimation byName(String name) {
		if (animationsByName.containsKey(name)) {
			return animationsByName.get(name);
		}
		throw new IllegalArgumentException("Illegal animation name: " + name);
	}

	private void withCurrentAnimationDo(Consumer<SpriteAnimation> operation) {
		if (currentAnimation != null) {
			operation.accept(currentAnimation);
		}
	}

	@Override
	public void startSelected() {
		withCurrentAnimationDo(SpriteAnimation::start);
	}

	@Override
	public void stopSelected() {
		withCurrentAnimationDo(SpriteAnimation::stop);
	}

	@Override
	public void resetSelected() {
		withCurrentAnimationDo(SpriteAnimation::reset);
	}

	@Override
	public Rectangle2D currentSprite() {
		if (!ghost.isVisible() || currentAnimationName == null) {
			return null;
		}
		if (GHOST_NORMAL.equals(currentAnimationName)) {
			currentAnimation.setSprites(ghostNormalSprites(ghost.id(), ghost.wishDir()));
		} else if (GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation.setSprites(ghostEyesSprites(ghost.wishDir()));
		}
		return currentAnimation.currentSprite();
	}
}