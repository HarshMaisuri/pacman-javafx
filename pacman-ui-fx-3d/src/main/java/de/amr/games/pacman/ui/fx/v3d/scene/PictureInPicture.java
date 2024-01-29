/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;

import static de.amr.games.pacman.ui.fx.PacManGames2dUI.CANVAS_HEIGHT_UNSCALED;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.CANVAS_WIDTH_UNSCALED;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PIP_MIN_HEIGHT;

/**
 * @author Armin Reichert
 */
public class PictureInPicture {

	public final DoubleProperty heightPy = new SimpleDoubleProperty(PIP_MIN_HEIGHT) {
		@Override
		protected void invalidated() {
			double scaling = get() / CANVAS_HEIGHT_UNSCALED;
			canvas.setWidth(CANVAS_WIDTH_UNSCALED * scaling);
			canvas.setHeight(CANVAS_HEIGHT_UNSCALED * scaling);
			playScene2D.setScaling(scaling);
		}
	};

	public final DoubleProperty opacityPy = new SimpleDoubleProperty(1.0);

	private final PlayScene2D playScene2D;
	private final Canvas canvas;

	public PictureInPicture(GameSceneContext sceneContext) {
		double h = heightPy.doubleValue();
		double aspectRatio = (double) GameModel.TILES_X / GameModel.TILES_Y;
		canvas = new Canvas(h * aspectRatio, h);
		playScene2D = new PlayScene2D();
		playScene2D.setCanvas(canvas);
		playScene2D.setScoreVisible(true);
		playScene2D.root().opacityProperty().bind(opacityPy);
		playScene2D.root().setVisible(false);
		playScene2D.setContext(sceneContext);
	}

	public Node root() {
		return canvas;
	}

	public void draw() {
		playScene2D.draw();
	}
}