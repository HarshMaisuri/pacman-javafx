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
package de.amr.games.pacman.ui.fx.v3d.app;

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.app.PacManGames2dAssets;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends PacManGames2dApp {

	@Override
	public void init() {
		settings.merge(getParameters().getNamed());
		PacManGames2d.assets = new PacManGames2dAssets();
		PacManGames3d.assets = new PacManGames3dAssets();
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		stage.setFullScreen(settings.fullScreen);
		PacManGames3d.ui = new PacManGames3dUI(stage, settings.zoom * 28 * 8, settings.zoom * 36 * 8);
		PacManGames2d.ui = PacManGames3d.ui;
		PacManGames3d.ui.init(settings);
		PacManGames3d.ui.gameController().changeState(GameState.BOOT);
		PacManGames3d.ui.showStage();
		PacManGames3d.ui.clock().start();
		Logger.info("Game started. {} Hz language={}", PacManGames3d.ui.clock().targetFrameratePy.get(),
				Locale.getDefault());
	}

	@Override
	public void stop() {
		PacManGames3d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}