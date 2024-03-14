/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays the score and high score.
 *
 * @author Armin Reichert
 */
public class Scores3D {

    public ObjectProperty<Font> fontPy = new SimpleObjectProperty<>(this, "font", Font.font("Courier", 12));

    private final GridPane grid = new GridPane();
    private final Text txtScore;
    private final Text txtHighScore;
    private boolean pointsDisplayed = true;

    public Scores3D() {
        var txtScoreTitle = new Text("SCORE");
        txtScoreTitle.setFill(Color.GHOSTWHITE);
        txtScoreTitle.fontProperty().bind(fontPy);

        txtScore = new Text();
        txtScore.setFill(Color.YELLOW);
        txtScore.fontProperty().bind(fontPy);

        var txtHighScoreTitle = new Text("HIGH SCORE");
        txtHighScoreTitle.setFill(Color.GHOSTWHITE);
        txtHighScoreTitle.fontProperty().bind(fontPy);

        txtHighScore = new Text();
        txtHighScore.setFill(Color.YELLOW);
        txtHighScore.fontProperty().bind(fontPy);

        grid.setHgap(5 * TS);
        grid.add(txtScoreTitle,  0,0);
        grid.add(txtScore,       0,1);
        grid.add(txtHighScoreTitle,1,0);
        grid.add(txtHighScore,     1,1);
    }

    public Node root() {
        return grid;
    }

    public void setShowText(Color color, String text) {
        requireNonNull(color);
        txtScore.setFill(color);
        txtScore.setText(text);
        pointsDisplayed = false;
    }

    public void setPointsDisplayed(boolean state) {
        this.pointsDisplayed = state;
    }

    public void setScores(int score, int levelNumber, int highScore, int highScoreLevelNumber) {
        if (pointsDisplayed) {
            txtScore.setFill(Color.YELLOW);
            txtScore.setText(String.format("%7d L%d", score, levelNumber));
        }
        txtHighScore.setFill(Color.YELLOW);
        txtHighScore.setText(String.format("%7d L%d", highScore, highScoreLevelNumber));
    }
}