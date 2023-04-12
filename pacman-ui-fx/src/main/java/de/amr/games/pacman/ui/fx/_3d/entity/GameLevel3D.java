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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Objects;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.ui.fx._2d.rendering.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.SpritesheetRenderer;
import de.amr.games.pacman.ui.fx.app.AppResources;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

	private final GameLevel level;
	private final Group root = new Group();
	private final World3D world3D;
	private final Pac3D pac3D;
	private final Ghost3D[] ghosts3D;
	private final Bonus3D bonus3D;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;
	private final Scores3D scores3D;

	public GameLevel3D(GameLevel level, Rendering2D r2D) {
		this.level = Objects.requireNonNull(level);
		Objects.requireNonNull(r2D);

		var gameVariant = level.game().variant();
		int mazeNumber = level.game().mazeNumber(level.number());

		var pacModel3D = AppResources.MODEL3D_PAC;
		var ghostModel3D = AppResources.MODEL3D_GHOST;
		var pelletModel3D = AppResources.MODEL3D_PELLET;
		var pacManColoring = ArcadeTheme.PACMAN_COLORING;
		var msPacManColoring = ArcadeTheme.MS_PACMAN_COLORING;
		var ghostColoring = ArcadeTheme.GHOST_COLORING;

		world3D = new World3D(level.world(), r2D.mazeColoring(mazeNumber), pelletModel3D);

		pac3D = switch (gameVariant) {
		case MS_PACMAN -> new Pac3D(level, level.pac(), pacModel3D.createMsPacManShape(9.0, msPacManColoring),
				msPacManColoring.headColor());
		case PACMAN -> new Pac3D(level, level.pac(), pacModel3D.createPacManShape(9.0, pacManColoring),
				pacManColoring.headColor());
		default -> throw new IllegalArgumentException();
		};

		ghosts3D = level.ghosts().map(ghost -> new Ghost3D(level, ghost, ghostColoring[ghost.id()], ghostModel3D, 8.5))
				.toArray(Ghost3D[]::new);

		bonus3D = createBonus3D(level.bonus(), r2D);
		levelCounter3D = createLevelCounter3D(r2D);

		livesCounter3D = switch (gameVariant) {
		case MS_PACMAN -> new LivesCounter3D(5, () -> pacModel3D.createMsPacManShape(9, msPacManColoring), true);
		case PACMAN -> new LivesCounter3D(5, () -> pacModel3D.createPacManShape(9, pacManColoring), false);
		default -> throw new IllegalArgumentException();
		};

		scores3D = new Scores3D(r2D.screenFont(8));

		// layout stuff
		scores3D.setPosition(TS, -3 * TS, -3 * TS);
		livesCounter3D.setPosition(2 * TS, 2 * TS, 0);
		// lift guys a bit over floor (especially Ms. Pac-Man will thank you for that!)
		var lift = new Translate(0, 0, -1);
		pac3D.getRoot().getTransforms().add(lift);
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.getRoot().getTransforms().add(lift));

		// populate level
		root.getChildren().add(scores3D.getRoot());
		root.getChildren().add(levelCounter3D.getRoot());
		root.getChildren().add(livesCounter3D.getRoot());
		root.getChildren().add(bonus3D.getRoot());
		root.getChildren().add(pac3D.getRoot());
		root.getChildren().add(pac3D.getLight());
		root.getChildren().add(ghosts3D[0].getRoot());
		root.getChildren().add(ghosts3D[1].getRoot());
		root.getChildren().add(ghosts3D[2].getRoot());
		root.getChildren().add(ghosts3D[3].getRoot());
		// Note: world/ghosthouse must be added after the guys if transparent ghosthouse shall be rendered correctly!
		root.getChildren().add(world3D.getRoot());

		// connect to environment
		pac3D.drawModePy.bind(Env.d3_drawModePy);
		pac3D.lightedPy.bind(Env.d3_pacLightedPy);
		ghosts3D[Ghost.ID_RED_GHOST].drawModePy.bind(Env.d3_drawModePy);
		ghosts3D[Ghost.ID_PINK_GHOST].drawModePy.bind(Env.d3_drawModePy);
		ghosts3D[Ghost.ID_CYAN_GHOST].drawModePy.bind(Env.d3_drawModePy);
		ghosts3D[Ghost.ID_ORANGE_GHOST].drawModePy.bind(Env.d3_drawModePy);
		world3D.drawModePy.bind(Env.d3_drawModePy);
		world3D.floorColorPy.bind(Env.d3_floorColorPy);
		world3D.floorTexturePy.bind(Env.d3_floorTexturePy);
		world3D.wallHeightPy.bind(Env.d3_mazeWallHeightPy);
		world3D.wallThicknessPy.bind(Env.d3_mazeWallThicknessPy);
		livesCounter3D.drawModePy.bind(Env.d3_drawModePy);

		if (Env.d3_floorTextureRandomPy.get()) {
			selectRandomFloorTexture();
		}
	}

	private Bonus3D createBonus3D(Bonus bonus, Rendering2D r2D) {
		if (r2D instanceof SpritesheetRenderer sr) {
			var symbolSprite = sr.bonusSymbolRegion(bonus.symbol());
			var pointsSprite = sr.bonusValueRegion(bonus.symbol());
			return new Bonus3D(level, bonus, sr.image(symbolSprite), sr.image(pointsSprite));
		}
		throw new UnsupportedOperationException();
	}

	private LevelCounter3D createLevelCounter3D(Rendering2D r2D) {
		if (r2D instanceof SpritesheetRenderer sr) {
			var symbolImages = level.game().levelCounter().stream().map(sr::bonusSymbolRegion).map(sr::image)
					.toArray(Image[]::new);
			var rightPosition = new Vector2f((level.world().numCols() - 2) * TS, 2 * TS);
			return new LevelCounter3D(symbolImages, rightPosition);
		}
		throw new UnsupportedOperationException();
	}

	public void update() {
		pac3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update();
		// TODO get rid of this
		int numLivesShown = level.game().isOneLessLifeDisplayed() ? level.game().lives() - 1 : level.game().lives();
		livesCounter3D.update(numLivesShown);
		livesCounter3D.getRoot().setVisible(level.game().hasCredit());
		scores3D.update(level);
		if (level.game().hasCredit()) {
			scores3D.setShowPoints(true);
		} else {
			scores3D.setShowText(Color.RED, "GAME OVER!");
		}
		updateHouseState();
	}

	public void eat(Eatable3D eatable3D) {
		if (eatable3D instanceof Energizer3D energizer3D) {
			energizer3D.stopPumping();
		}
		// Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
		// the pellet disappears too early (collision by same tile in game model is too simplistic).
		var delayHiding = Ufx.afterSeconds(0.05, () -> eatable3D.getRoot().setVisible(false));
		var eatenAnimation = eatable3D.getEatenAnimation();
		if (eatenAnimation.isPresent() && Env.d3_energizerExplodesPy.get()) {
			new SequentialTransition(delayHiding, eatenAnimation.get()).play();
		} else {
			delayHiding.play();
		}
	}

	private void selectRandomFloorTexture() {
		Env.d3_floorTexturePy.set(AppResources.randomTextureKey());
	}

	private void updateHouseState() {
		boolean isGhostNearHouse = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				.anyMatch(Ghost::isVisible);
		boolean accessGranted = isAccessGranted(level.ghosts(), level.world().ghostHouse().door().entryPosition());
		world3D.houseLighting().setLightOn(isGhostNearHouse);
		world3D.doorWings3D().forEach(door3D -> door3D.setOpen(accessGranted));
	}

	private boolean isAccessGranted(Stream<Ghost> ghosts, Vector2f doorPosition) {
		return ghosts.anyMatch(ghost -> ghost.isVisible()
				&& ghost.is(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				&& ghost.position().euclideanDistance(doorPosition) <= 1.5 * TS);
	}

	public Group getRoot() {
		return root;
	}

	public LivesCounter3D getLivesCounter3D() {
		return livesCounter3D;
	}

	public World3D world3D() {
		return world3D;
	}

	public Pac3D pac3D() {
		return pac3D;
	}

	public Ghost3D[] ghosts3D() {
		return ghosts3D;
	}

	public Bonus3D bonus3D() {
		return bonus3D;
	}

	public Scores3D scores3D() {
		return scores3D;
	}
}