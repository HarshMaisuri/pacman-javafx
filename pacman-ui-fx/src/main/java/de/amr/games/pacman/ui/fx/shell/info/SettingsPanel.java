/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell.info;

import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.layout.VBox;

/**
 * Panel for configuring the game and the UI.
 * 
 * @author Armin Reichert
 */
public class SettingsPanel extends VBox {

	private final SectionCommands sectionCommands;
	private final SectionGeneral sectionGeneral;
	private final Section3D section3D;
	private final Section2D section2D;

	public SettingsPanel(GameUI ui) {
		sectionCommands = new SectionCommands(ui);
		sectionGeneral = new SectionGeneral(ui);
		section3D = new Section3D(ui);
		section2D = new Section2D(ui);
		setVisible(false);
		getChildren().addAll(sectionCommands, sectionGeneral, section3D, section2D);
	}

	public void update() {
		sectionCommands.update();
		sectionGeneral.update();
		section3D.update();
		section2D.update();
	}
}