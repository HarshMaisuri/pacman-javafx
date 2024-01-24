package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_3D_PERSPECTIVE;

/**
 * @author Armin Reichert
 */
public class GamePageContextMenu extends ContextMenu {
	private CheckMenuItem autopilotItem;
	private CheckMenuItem immunityItem;
	private CheckMenuItem pipItem;
	private ToggleGroup perspectivesToggleGroup;

	public void rebuild(ActionHandler3D actionHandler, GameScene gameScene, List<ResourceBundle> messageBundles) {
		getItems().clear();
		getItems().add(createTitleItem(ResourceManager.message(messageBundles,"scene_display")));
		if (gameScene instanceof PlayScene2D) {
			var item = new MenuItem(ResourceManager.message(messageBundles,"use_3D_scene"));
			item.setOnAction(e -> actionHandler.toggle2D3D());
			getItems().add(item);
		} else if (gameScene instanceof PlayScene3D) {
			var item = new MenuItem(ResourceManager.message(messageBundles,"use_2D_scene"));
			item.setOnAction(e -> actionHandler.toggle2D3D());
			getItems().add(item);
			pipItem = new CheckMenuItem(ResourceManager.message(messageBundles,"pip"));
			pipItem.setOnAction(e -> actionHandler.togglePipVisible());
			getItems().add(pipItem);
			getItems().add(createTitleItem(ResourceManager.message(messageBundles,"select_perspective")));
			perspectivesToggleGroup = new ToggleGroup();
			for (var p : Perspective.values()) {
				var rmi = new RadioMenuItem(ResourceManager.message(messageBundles,p.name()));
				rmi.setUserData(p);
				rmi.setToggleGroup(perspectivesToggleGroup);
				getItems().add(rmi);
			}
			perspectivesToggleGroup.selectedToggleProperty().addListener((py, ov, nv) -> {
				if (nv != null) {
					// Note: These are the used data set for the radio menu item!
					PY_3D_PERSPECTIVE.set((Perspective) nv.getUserData());
				}
			});
		}
		getItems().add(createTitleItem(ResourceManager.message(messageBundles,"pacman")));
		autopilotItem = new CheckMenuItem(ResourceManager.message(messageBundles,"autopilot"));
		autopilotItem.setOnAction(e -> actionHandler.toggleAutopilot());
		getItems().add(autopilotItem);
		immunityItem = new CheckMenuItem(ResourceManager.message(messageBundles,"immunity"));
		immunityItem.setOnAction(e -> actionHandler.toggleImmunity());
		getItems().add(immunityItem);
	}

	private MenuItem createTitleItem(String title) {
		var text = new Text(title);
		text.setFont(Font.font("Sans", FontWeight.BOLD, 14));
		return new CustomMenuItem(text);
	}

	public void updateState() {
		if (perspectivesToggleGroup != null) {
			for (var toggle : perspectivesToggleGroup.getToggles()) {
				toggle.setSelected(PY_3D_PERSPECTIVE.get().equals(toggle.getUserData()));
			}
		}
		if (pipItem != null) {
			pipItem.setSelected(PacManGames3dUI.PY_PIP_ON.get());
		}
		if (autopilotItem != null) {
			autopilotItem.setSelected(GameController.it().isAutoControlled());
		}
		if (immunityItem != null) {
			immunityItem.setSelected(GameController.it().isImmune());
		}
	}
}
