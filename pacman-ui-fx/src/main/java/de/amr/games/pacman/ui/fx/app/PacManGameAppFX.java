package de.amr.games.pacman.ui.fx.app;

import java.io.IOException;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.God;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man game app running in a JavaFX UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	private static CommandLineArgs options;

	public static void main(String[] args) {
		options = new CommandLineArgs(args);
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PacManGameController controller = new PacManGameController(options.pacman ? GameType.PACMAN : GameType.MS_PACMAN);
		PacManGameUI_JavaFX ui = new PacManGameUI_JavaFX(stage, controller, options.height);
		controller.setUserInterface(ui);
		new AnimationTimer() {

			@Override
			public void handle(long now) {
				controller.step();
				God.clock.ticksTotal++; // TODO get rid of this
			}
		}.start();
	}
}