/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.Settings;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

	private final Settings settings = new Settings();
	private PacManGames3dUI ui;

	@Override
	public void init() {
		if (getParameters() != null) {
			settings.merge(getParameters().getNamed());
		}
		GameController.create(settings.variant);
		int hour = LocalTime.now().getHour();
		PacManGames3dUI.PY_3D_NIGHT_MODE.set(hour >= 20 || hour <= 5);
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		ui = new PacManGames3dUI(stage, settings);
		GameController.it().addListener(ui);
		ui.showStartPage();
		Logger.info("UI created. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
	}

	@Override
	public void stop() {
		ui.gameClock().stop();
		Logger.info("Game stopped.");
	}
}