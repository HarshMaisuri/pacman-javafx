/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.Game2d;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class Signature {

	private TextFlow text = new TextFlow();

	public Signature() {
		var part1 = new Text("Remake (2023) by ");
		part1.setFill(Color.gray(0.6));
		part1.setFont(Font.font("Helvetica", 9));

		var part2 = new Text("Armin Reichert");
		part2.setFill(Color.gray(0.6));
		part2.setFont(Game2d.assets.font(Game2d.assets.handwritingFont, 9));

		text = new TextFlow(part1, part2);
	}

	public void add(Pane parent, double x, double y) {
		text.setTranslateX(x);
		text.setTranslateY(y);
		parent.getChildren().add(text);
	}

	public void setOpacity(double opacity) {
		text.setOpacity(opacity);
	}

	public void show() {
		var fadeIn = new FadeTransition(Duration.seconds(5), text);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.setInterpolator(Interpolator.EASE_IN);
		var fadeOut = new FadeTransition(Duration.seconds(1), text);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		var fade = new SequentialTransition(fadeIn, fadeOut);
		fade.play();
	}
}