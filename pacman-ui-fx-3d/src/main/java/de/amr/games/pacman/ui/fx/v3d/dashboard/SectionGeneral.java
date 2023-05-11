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
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.app.Game3d;
import de.amr.games.pacman.ui.fx.v3d.app.GameUI3d;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

/**
 * General settings.
 * 
 * @author Armin Reichert
 */
public class SectionGeneral extends Section {

	public static final int MIN_FRAMERATE = 5;
	public static final int MAX_FRAMERATE = 120;

	private final Button[] btnsSimulation;
	private final Spinner<Integer> spinnerSimulationSteps;
	private final Slider sliderTargetFPS;
	private final CheckBox cbUsePlayScene3D;
	private final CheckBox cbPoliticallyCorrect;
	private final CheckBox cbDebugUI;
	private final CheckBox cbTimeMeasured;
	private final ImageView iconPlay = new ImageView(Game3d.ASSET_MANAGER.image("graphics/icons/play.png"));
	private final ImageView iconStop = new ImageView(Game3d.ASSET_MANAGER.image("graphics/icons/stop.png"));
	private final ImageView iconStep = new ImageView(Game3d.ASSET_MANAGER.image("graphics/icons/step.png"));
	private final Tooltip tooltipPlay = new Tooltip("Play");
	private final Tooltip tooltipStop = new Tooltip("Stop");
	private final Tooltip tooltipStep = new Tooltip("Single Step Mode");

	public SectionGeneral(GameUI3d ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		btnsSimulation = addButtonList("Simulation", "Pause", "Step(s)");
		Button btnPlayPause = btnsSimulation[0];
		Button btnStep = btnsSimulation[1];

		btnPlayPause.setText(null);
		btnPlayPause.setStyle("-fx-background-color: transparent");
		btnPlayPause.setOnAction(e -> Game2d.actions.togglePaused());

		btnStep.setGraphic(iconStep);
		btnStep.setStyle("-fx-background-color: transparent");
		btnStep.setText(null);
		btnStep.setTooltip(tooltipStep);
		btnStep.setOnAction(e -> ui.executeSteps(Game2d.simulationStepsPy.get(), true));

		spinnerSimulationSteps = addSpinner("Num Steps", 1, 50, Game2d.simulationStepsPy.get());
		spinnerSimulationSteps.valueProperty()
				.addListener((obs, oldVal, newVal) -> Game2d.simulationStepsPy.set(newVal.intValue()));

		sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAMERATE, MAX_FRAMERATE, 60);
		sliderTargetFPS.setShowTickLabels(false);
		sliderTargetFPS.setShowTickMarks(false);
		sliderTargetFPS.valueProperty()
				.addListener((obs, oldValue, newValue) -> ui.targetFrameratePy.set(newValue.intValue()));

		addInfo("", () -> String.format("Target %dHz Actual %dHz", ui.targetFrameratePy.get(), ui.getFPS()));

		addInfo("Total Updates", ui::getUpdateCount);

		cbUsePlayScene3D = addCheckBox("3D Play Scene", ui::toggle3DEnabled);
		cbPoliticallyCorrect = addCheckBox("Woke Pussy Mode", () -> Ufx.toggle(Game3d.wokePussyMode));
		cbDebugUI = addCheckBox("Show Debug Info", () -> Ufx.toggle(Game2d.showDebugInfoPy));
		cbTimeMeasured = addCheckBox("Time Measured", () -> Ufx.toggle(ui.timeMeasuredPy));
	}

	@Override
	public void update() {
		super.update();
		btnsSimulation[0].setGraphic(ui.pausedPy.get() ? iconPlay : iconStop);
		btnsSimulation[0].setTooltip(ui.pausedPy.get() ? tooltipPlay : tooltipStop);
		btnsSimulation[1].setDisable(!ui.pausedPy.get());
		spinnerSimulationSteps.getValueFactory().setValue(Game2d.simulationStepsPy.get());
		sliderTargetFPS.setValue(ui.targetFrameratePy.get());
		cbUsePlayScene3D.setSelected(Game3d.d3_enabledPy.get());
		cbPoliticallyCorrect.setSelected(Game3d.wokePussyMode.get());
		cbTimeMeasured.setSelected(ui.timeMeasuredPy.get());
		cbDebugUI.setSelected(Game2d.showDebugInfoPy.get());
	}
}