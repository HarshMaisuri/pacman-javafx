/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;

/**
 * @author Armin Reichert
 */
public class InfoBoxAppearance extends InfoBox {

	private final ColorPicker pickerLightColor;
	private final ColorPicker pickerFloorColor;
	private final ComboBox<String> comboFloorTexture;
	private final CheckBox cbFloorTextureRandom;

	public InfoBoxAppearance(Theme theme, String title) {
		super(theme, title);
		pickerLightColor = addColorPicker("Light Color", PacManGames3dUI.PY_3D_LIGHT_COLOR.get());
		pickerLightColor.setOnAction(e -> PacManGames3dUI.PY_3D_LIGHT_COLOR.set(pickerLightColor.getValue()));
		pickerFloorColor = addColorPicker("Floor Color", PacManGames3dUI.PY_3D_FLOOR_COLOR.get());
		pickerFloorColor.setOnAction(e -> PacManGames3dUI.PY_3D_FLOOR_COLOR.set(pickerFloorColor.getValue()));
		comboFloorTexture = addComboBox("Floor Texture", floorTextureComboBoxEntries());
		comboFloorTexture.setOnAction(e -> PacManGames3dUI.PY_3D_FLOOR_TEXTURE.set(comboFloorTexture.getValue()));
		cbFloorTextureRandom = addCheckBox("Random Floor Texture", () -> Ufx.toggle(PacManGames3dUI.PY_3D_FLOOR_TEXTURE_RND));
	}

	@Override
	public void update() {
		comboFloorTexture.setValue(PacManGames3dUI.PY_3D_FLOOR_TEXTURE.get());
		cbFloorTextureRandom.setSelected(PacManGames3dUI.PY_3D_FLOOR_TEXTURE_RND.get());
	}

	private String[] floorTextureComboBoxEntries() {
		var names = new String[] { "hexagon", "knobs", "plastic", "wood" };
		var entries = new String[names.length + 1];
		entries[0] = PacManGames3dUI.NO_TEXTURE;
		System.arraycopy(names, 0, entries, 1, names.length);
		return entries;
	}
}