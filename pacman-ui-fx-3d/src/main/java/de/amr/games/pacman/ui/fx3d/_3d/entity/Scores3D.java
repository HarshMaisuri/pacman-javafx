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
package de.amr.games.pacman.ui.fx3d._3d.entity;

import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

import de.amr.games.pacman.model.GameLevel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Displays the score and high score.
 * 
 * @author Armin Reichert
 */
public class Scores3D {

	private final Group root = new Group();
	private final Text txtScoreTitle;
	private final Text txtScore;
	private final Text txtHiscoreTitle;
	private final Text txtHiscore;
	private Color titleColor = Color.GHOSTWHITE;
	private Color scoreColor = Color.YELLOW;
	private Font font = Font.font("Courier", 12);
	private boolean pointsDisplayed = true;

	public Scores3D(Font font) {
		requireNonNull(font);

		this.font = font;
		txtScoreTitle = new Text("SCORE");
		txtScore = new Text();
		txtHiscoreTitle = new Text("HIGH SCORE");
		txtHiscore = new Text();
		GridPane grid = new GridPane();
		grid.setHgap(5 * TS);
		grid.add(txtScoreTitle, 0, 0);
		grid.add(txtScore, 0, 1);
		grid.add(txtHiscoreTitle, 1, 0);
		grid.add(txtHiscore, 1, 1);
		root.getChildren().add(grid);
	}

	public Node getRoot() {
		return root;
	}

	public void setPosition(double x, double y, double z) {
		root.setTranslateX(x);
		root.setTranslateY(y);
		root.setTranslateZ(z);
	}

	public void setShowText(Color color, String text) {
		requireNonNull(color);

		txtScore.setFill(color);
		txtScore.setText(text);
		pointsDisplayed = false;
	}

	public void setShowPoints(boolean show) {
		this.pointsDisplayed = show;
	}

	public void update(GameLevel level) {
		requireNonNull(level);

		txtScoreTitle.setFill(titleColor);
		txtScoreTitle.setFont(font);
		if (pointsDisplayed) {
			level.game().score().ifPresent(score -> {
				txtScore.setFont(font);
				txtScore.setText(String.format("%7d L%d", score.points(), score.levelNumber()));
				txtScore.setFill(Color.YELLOW);
			});
		}
		level.game().highScore().ifPresent(hiscore -> {
			txtHiscoreTitle.setFill(titleColor);
			txtHiscoreTitle.setFont(font);
			txtHiscore.setFill(scoreColor);
			txtHiscore.setFont(font);
			txtHiscore.setText(String.format("%7d L%d", hiscore.points(), hiscore.levelNumber()));
		});
	}
}