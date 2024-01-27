/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.ui.fx.GamePage;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.dashboard.*;
import de.amr.games.pacman.ui.fx.v3d.scene.PictureInPicture;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.PacManGames2dUI.PY_SHOW_DEBUG_INFO;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.border;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

	private final BorderPane topLayer; // contains dashboard and picture-in-picture view
	private final PictureInPicture pip;
	private final Pane dashboard;
	private final List<InfoBox> infoBoxes = new ArrayList<>();
	private final GamePageContextMenu contextMenu;

	public GamePage3D(GameSceneContext sceneContext, double width, double height) {
		super(sceneContext);
		setSize(width, height);
		PY_3D_NIGHT_MODE.addListener((py, ov, nv) -> updateBackground());
		pip = createPictureInPicture();
		dashboard = createDashboard(sceneContext.theme());
		contextMenu = new GamePageContextMenu();
		topLayer = new BorderPane();
		topLayer.setLeft(dashboard);
		topLayer.setRight(pip.root());
	}

	private PictureInPicture createPictureInPicture() {
		var pip = new PictureInPicture();
		pip.gameScene().setContext(sceneContext);
		pip.opacityPy.bind(PY_PIP_OPACITY);
		pip.heightPy.bind(PY_PIP_HEIGHT);
		PY_PIP_ON.addListener((py, ov, nv) -> updateTopLayer());
		return pip;
	}

	private VBox createDashboard(Theme theme) {
		var db = new VBox();
		infoBoxes.add(new InfoBoxGeneral(theme, tt("infobox.general.title")));
		infoBoxes.add(new InfoBoxGameControl(theme, tt("infobox.game_control.title")));
		infoBoxes.add(new InfoBox3D(theme, tt("infobox.3D_settings.title")));
		infoBoxes.add(new InfoBoxGameInfo(theme, tt("infobox.game_info.title")));
		infoBoxes.add(new InfoBoxGhostsInfo(theme, tt("infobox.ghosts_info.title")));
		infoBoxes.add(new InfoBoxKeys(theme, tt("infobox.keyboard_shortcuts.title")));
		infoBoxes.add(new InfoBoxAbout(theme, tt("infobox.about.title")));
		infoBoxes.stream().map(InfoBox::getRoot).forEach(db.getChildren()::add);
		db.setVisible(false);
		db.visibleProperty().addListener((py, ov, nv) -> updateTopLayer());
		return db;
	}

	public List<InfoBox> getInfoBoxes() {
		return infoBoxes;
	}

	public GamePageContextMenu contextMenu() {
		return contextMenu;
	}

	@Override
	public void onGameSceneChanged(GameScene newGameScene) {
		//TODO this code is too difficult to understand, simplify
		if (isCurrentGameScene3D()) {
			if (newGameScene == sceneContext.sceneConfig().get("play3D")) {
				// Note: event handler is removed again in super.onGameSceneChanged() call
				layers.addEventHandler(KeyEvent.KEY_PRESSED, (KeyboardSteering) sceneContext.gameController().getManualPacSteering());
			}
			layers.getChildren().set(0, newGameScene.root());
			layers.requestFocus();
			helpButton.setVisible(false); // use data binding?
		} else {
			layers.getChildren().set(0, gameSceneLayer);
			super.onGameSceneChanged(newGameScene);
		}
		contextMenu.hide();
		updateBackground();
		updateTopLayer();
		updateDebugBorders();
	}

	public void updateBackground() {
		if (isCurrentGameScene3D()) {
			if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
				layers.setBackground(ResourceManager.coloredBackground(Color.BLACK));
			} else {
				var wallpaperKey = PY_3D_NIGHT_MODE.get() ? "model3D.wallpaper.night" : "model3D.wallpaper";
				layers.setBackground(sceneContext.theme().background(wallpaperKey));
			}
		} else {
			gameSceneLayer.setBackground(sceneContext.theme().background("wallpaper.background"));
		}
	}

	@Override
	public void render() {
		super.render();
		contextMenu.updateState();
		infoBoxes.forEach(InfoBox::update);
		pip.root().setVisible(PY_PIP_ON.get() && isCurrentGameScene3D());
		pip.render();
	}

	@Override
	protected void handleKeyboardInput() {
		var actionHandler3D = (ActionHandler3D) sceneContext.actionHandler();
		if (Keyboard.pressed(KEY_TOGGLE_2D_3D)) {
			actionHandler3D.toggle2D3D();
		} else if (Keyboard.pressed(KEYS_TOGGLE_DASHBOARD)) {
			dashboard.setVisible(!dashboard.isVisible());
		} else if (Keyboard.pressed(KEY_TOGGLE_PIP_VIEW)) {
			actionHandler3D.togglePipVisible();
		} else {
			super.handleKeyboardInput();
		}
	}

	@Override
	protected void updateDebugBorders()  {
		if (PY_SHOW_DEBUG_INFO.get() && !isCurrentGameScene3D()) {
			layers.setBorder(border(Color.RED, 3));
			gameSceneLayer.setBorder(border(Color.YELLOW, 3));
			popupLayer.setBorder(border(Color.GREENYELLOW, 3));
		} else {
			layers.setBorder(null);
			gameSceneLayer.setBorder(null);
			popupLayer.setBorder(null);
		}
	}

	@Override
	protected void updateHelpButton() {
		if (isCurrentGameScene3D()) {
			helpButton.setVisible(false);
		} else {
			super.updateHelpButton();
		}
	}

	private boolean isCurrentGameScene3D() {
		return sceneContext.currentGameScene().isPresent() && sceneContext.currentGameScene().get() instanceof PlayScene3D;
	}

	private void updateTopLayer() {
		layers.getChildren().remove(topLayer);
		if (dashboard.isVisible() || PY_PIP_ON.get()) {
			layers.getChildren().add(topLayer);
		}
		layers.requestFocus();
	}
}